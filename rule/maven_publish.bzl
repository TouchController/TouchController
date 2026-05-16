"""Rules for publishing artifacts to Maven repositories."""

load("@bazel_lib//lib:windows_utils.bzl", "create_windows_native_launcher_script")
load("@rules_java//java:defs.bzl", "java_library")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("//rule:merge_jar.bzl", "merge_jar_action")

_SH_TOOLCHAIN_TYPE = "@rules_shell//shell:toolchain_type"

_MavenCoordinateInfo = provider(
    fields = {
        "coordinates": "Maven coordinate in format 'groupId:artifactId:version'",
    },
)

def _maven_coordinate_collector_impl(target, ctx):
    coordinate = None
    for tag in ctx.rule.attr.tags:
        prefix = "maven_coordinates="
        if tag.startswith(prefix):
            coordinate = tag[len(prefix):]
            break
    if coordinate:
        return [_MavenCoordinateInfo(coordinates = [coordinate])]
    return []

_maven_coordinate_collector = aspect(
    implementation = _maven_coordinate_collector_impl,
    required_providers = [JavaInfo],
)

def _parse_maven_coordinates(coordinates, scope = "compile"):
    parts = coordinates.split(":")
    if len(parts) < 3:
        fail("coordinate must be in format 'groupId:artifactId:version[:classifier]'")
    group_id, artifact_id, version = parts[0], parts[1], parts[2]
    classifier = parts[3] if len(parts) > 3 else None
    return struct(
        group_id = group_id,
        artifact_id = artifact_id,
        version = version,
        classifier = classifier,
        scope = scope,
    )

def _maven_publish_impl(ctx):
    parts = ctx.attr.coordinates.split(":")
    if len(parts) != 3:
        fail("coordinate must be in format 'groupId:artifactId:version'")
    group_id, artifact_id, version = parts

    artifact_specs = {}
    input_files = []

    if ctx.attr.src:
        java_info = ctx.attr.src[JavaInfo]
        main_jars_depset = java_info.full_compile_jars
        main_jar = ctx.actions.declare_file(ctx.label.name + "_classes.jar")
        merge_jar_action(
            ctx.actions,
            ctx.executable._merge_jar_executable,
            main_jar,
            main_jars_depset,
            plugins = ["manifest", "services"],
        )
        artifact_specs[":jar"] = main_jar.short_path
        input_files.append(main_jar)

        source_jars = java_info.source_jars
        source_jar = None
        if source_jars:
            source_jar = ctx.actions.declare_file(ctx.label.name + "_sources.jar")
            merge_jar_action(
                ctx.actions,
                ctx.executable._merge_jar_executable,
                source_jar,
                depset(source_jars),
                plugins = ["manifest", "services"],
            )
            artifact_specs["sources:jar"] = source_jar.short_path
            input_files.append(source_jar)

    for classifier_ext, target in ctx.attr.artifacts.items():
        # Parse key: "classifier[:extension]"
        if ":" in classifier_ext:
            classifier, explicit_ext = classifier_ext.split(":", 1)
        else:
            classifier = classifier_ext
            explicit_ext = ""

        file_list = target[DefaultInfo].files.to_list()
        if not file_list:
            fail("No files found for artifact: " + classifier_ext)
        if len(file_list) != 1:
            fail("Expected exactly one file for artifact: " + classifier_ext + ", got " + str(len(file_list)))
        file = file_list[0]
        input_files.append(file)

        if not explicit_ext:
            extension = file.extension

        if extension == "pom":
            fail("Cannot override POM artifact. Use pom_template attribute instead.")

        artifact_specs[classifier + ":" + extension] = file.short_path

    dependencies_coordinates = []
    for dep in ctx.attr.deps:
        if _MavenCoordinateInfo in dep:
            dependencies_coordinates.extend(dep[_MavenCoordinateInfo].coordinates)
    dependencies_items = [_parse_maven_coordinates(coordinate, "compile") for coordinate in dependencies_coordinates]

    runtime_dependencies_coordinates = []
    for runtime_dep in ctx.attr.runtime_deps:
        if _MavenCoordinateInfo in runtime_dep:
            runtime_dependencies_coordinates.extend(runtime_dep[_MavenCoordinateInfo].coordinates)
    runtime_dependencies_items = [_parse_maven_coordinates(coordinate, "runtime") for coordinate in runtime_dependencies_coordinates if not coordinate in dependencies_coordinates]

    dependencies = []
    for item in dependencies_items + runtime_dependencies_items:
        if item.classifier:
            dependencies.append("""        <dependency>
            <groupId>%s</groupId>
            <artifactId>%s</artifactId>
            <version>%s</version>
            <classifier>%s</classifier>
            <scope>%s</scope>
        </dependency>""" % (item.group_id, item.artifact_id, item.version, item.classifier, item.scope))
        else:
            dependencies.append("""        <dependency>
            <groupId>%s</groupId>
            <artifactId>%s</artifactId>
            <version>%s</version>
            <scope>%s</scope>
        </dependency>""" % (item.group_id, item.artifact_id, item.version, item.scope))

    substitutions = {
        "{groupId}": group_id,
        "{artifactId}": artifact_id,
        "{version}": version,
        "{dependencies}": "\n".join(dependencies),
    }
    substitutions.update(ctx.attr.pom_substitutions)

    pom_template = ctx.file.pom_template if ctx.attr.pom_template else ctx.file._default_pom_template
    output_pom = ctx.actions.declare_file(ctx.label.name + ".pom")
    ctx.actions.expand_template(
        output = output_pom,
        template = pom_template,
        substitutions = substitutions,
    )

    artifact_ids = []
    artifact_paths = []
    for id, path in artifact_specs.items():
        artifact_ids.append(id)
        artifact_paths.append(path)

    wrapper_substitutions = {
        "{WORKSPACE_NAME}": ctx.workspace_name,
        "{EXEC_PATH}": ctx.executable._maven_publisher_binary.short_path,
        "{POM_PATH}": output_pom.short_path,
        "{GROUP_ID}": group_id,
        "{ARTIFACT_ID}": artifact_id,
        "{VERSION}": version,
        "{ARTIFACT_IDS}": " ".join([("'%s'" % id) for id in artifact_ids]),
        "{ARTIFACT_PATHS}": " ".join([("'%s'" % path) for path in artifact_paths]),
    }

    if ctx.target_platform_has_constraint(ctx.attr._windows_constraint[platform_common.ConstraintValueInfo]):
        output_script = ctx.actions.declare_file(ctx.attr.name + ".sh")
        ctx.actions.expand_template(
            output = output_script,
            template = ctx.file._wrapper_template,
            substitutions = wrapper_substitutions,
            is_executable = True,
        )
        output_executable = create_windows_native_launcher_script(ctx, output_script)

        runfiles = ctx.runfiles(
            files = [
                output_pom,
                output_script,
            ] + input_files,
        ).merge_all([
            ctx.attr._maven_publisher_binary[DefaultInfo].default_runfiles,
            ctx.attr._rlocation_library[DefaultInfo].default_runfiles,
        ])
    else:
        output_executable = ctx.actions.declare_file(ctx.attr.name + ".sh")
        runfiles = ctx.runfiles(
            files = [output_pom] + input_files,
        ).merge_all([
            ctx.attr._maven_publisher_binary[DefaultInfo].default_runfiles,
            ctx.attr._rlocation_library[DefaultInfo].default_runfiles,
        ])

        ctx.actions.expand_template(
            output = output_executable,
            template = ctx.file._wrapper_template,
            substitutions = wrapper_substitutions,
            is_executable = True,
        )

    return [DefaultInfo(
        runfiles = runfiles,
        executable = output_executable,
    )]

maven_publish = rule(
    implementation = _maven_publish_impl,
    executable = True,
    toolchains = [
        config_common.toolchain_type(_SH_TOOLCHAIN_TYPE, mandatory = False),
    ],
    attrs = {
        "coordinates": attr.string(
            mandatory = True,
            doc = "Maven coordinate in format 'groupId:artifactId:version'",
        ),
        "src": attr.label(
            mandatory = False,
            providers = [JavaInfo],
            doc = "JavaInfo target providing jar and sources",
        ),
        "deps": attr.label_list(
            mandatory = False,
            providers = [JavaInfo],
            aspects = [_maven_coordinate_collector],
            doc = "JavaInfo targets providing dependencies for POM",
        ),
        "runtime_deps": attr.label_list(
            mandatory = False,
            providers = [JavaInfo],
            aspects = [_maven_coordinate_collector],
            doc = "JavaInfo targets providing dependencies for POM, for scope runtime",
        ),
        "artifacts": attr.string_keyed_label_dict(
            mandatory = False,
            default = {},
            doc = "Custom artifacts. Key format: 'classifier[:extension]', Value: label",
        ),
        "pom_template": attr.label(
            mandatory = False,
            default = "//rule/maven_publisher:pom_template",
            allow_single_file = [".xml", ".pom"],
            doc = "Custom POM template file",
        ),
        "pom_substitutions": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "Template variable substitutions",
        ),
        "_maven_publisher_binary": attr.label(
            default = "//rule/maven_publisher:maven_publisher",
            executable = True,
            cfg = "exec",
        ),
        "_wrapper_template": attr.label(
            default = "//rule/maven_publisher:maven_publisher_wrapper",
            allow_single_file = [".bash"],
        ),
        "_rlocation_library": attr.label(
            default = "@rules_shell//shell/runfiles",
        ),
        "_windows_constraint": attr.label(
            default = "@platforms//os:windows",
        ),
        "_merge_jar_executable": attr.label(
            default = "@//rule/mergetool:merger",
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Publishes Java artifacts to a Maven repository",
)

def kt_jvm_export(**kwargs):
    name = kwargs["name"]
    maven_publish(
        name = name + ".publish",
        src = ":" + name,
        deps = kwargs["deps"] if "deps" in kwargs else [],
        runtime_deps = kwargs["runtime_deps"] if "runtime_deps" in kwargs else [],
        coordinates = kwargs["coordinates"],
    )

    args = {key: value for key, value in kwargs.items() if key != "coordinates" and key != "tags"}
    tags = kwargs["tags"] if "tags" in kwargs else []
    tags.append("maven_coordinates=%s" % kwargs["coordinates"])
    kt_jvm_library(
        tags = tags,
        **args
    )

def java_export(**kwargs):
    name = kwargs["name"]
    maven_publish(
        name = name + ".publish",
        src = ":" + name,
        deps = kwargs["deps"] if "deps" in kwargs else [],
        runtime_deps = kwargs["runtime_deps"] if "runtime_deps" in kwargs else [],
        coordinates = kwargs["coordinates"],
    )

    args = {key: value for key, value in kwargs.items() if key != "coordinates" and key != "tags"}
    tags = kwargs["tags"] if "tags" in kwargs else []
    tags.append("maven_coordinates=%s" % kwargs["coordinates"])
    java_library(
        tags = tags,
        **args
    )
