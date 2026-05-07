"""Rules for merging libraries."""

load("@rules_java//java:defs.bzl", _JavaInfo = "JavaInfo")
load("@rules_java//java/bazel/rules:bazel_java_library.bzl", _java_library = "java_library")
load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_kotlin//kotlin:jvm.bzl", _kt_jvm_library = "kt_jvm_library")
load("@rules_kotlin//src/main/starlark/core/compile:common.bzl", _KtJvmInfo = "KtJvmInfo")
load("//rule:merge_jar.bzl", "merge_jar_action")

def _merge_library_info_init(*, merge_jars = depset([]), merge_source_jars = depset([]), deps = []):
    if type(merge_jars) != "depset":
        fail("merge_jars must be type of depset, but it is %s" % type(merge_jars))
    if type(merge_source_jars) != "depset":
        fail("merge_source_jars must be type of depset, but it is %s" % type(merge_source_jars))
    return {
        "merge_jars": merge_jars,
        "merge_source_jars": merge_source_jars,
        "transitive_merge_jars": depset(
            transitive = [dep.transitive_merge_jars for dep in deps] + [merge_jars],
        ),
        "transitive_merge_source_jars": depset(
            transitive = [dep.transitive_merge_source_jars for dep in deps] + [merge_source_jars],
        ),
    }

MergeLibraryInfo, _ = provider(
    doc = "A structure for storing libraries to be merged.",
    fields = [
        "merge_jars",
        "merge_source_jars",
        "transitive_merge_jars",
        "transitive_merge_source_jars",
    ],
    init = _merge_library_info_init,
)

AspectJarInfo = provider(
    doc = "Provider for Aspect JAR outputs.",
    fields = ["jar"],
)

def _merge_library_group_impl(ctx):
    return [MergeLibraryInfo(
        merge_jars = depset(),
        merge_source_jars = depset(),
        deps = [dep[MergeLibraryInfo] for dep in ctx.attr.deps],
    ), _JavaInfo(
        output_jar = ctx.file._empty_jar,
        compile_jar = ctx.file._empty_jar,
        runtime_deps = [dep[_JavaInfo] for dep in ctx.attr.deps],
    ), DefaultInfo(
        files = depset(transitive = [dep[DefaultInfo].files for dep in ctx.attr.deps]),
    )]

merge_library_group = rule(
    implementation = _merge_library_group_impl,
    attrs = {
        "deps": attr.label_list(
            providers = [MergeLibraryInfo],
            mandatory = True,
        ),
        "_empty_jar": attr.label(
            default = "//rule/mergetool:empty_jar",
            allow_single_file = True,
        ),
    },
)

def _modify_deps(deps, runtime_deps, associates, merge_deps, merge_runtime_deps, plugins, expect, actual):
    real_deps = [dep for dep in deps]
    for merge_dep in merge_deps:
        if not merge_dep in associates:
            real_deps.append(merge_dep)
    real_plugins = [plugin for plugin in plugins]
    if expect or actual:
        real_deps.append("//rule/expect_actual_tools/api:api")
    if expect:
        real_plugins.append("//rule/expect_actual_tools/processor/java/expect")
    if actual:
        real_plugins.append("//rule/expect_actual_tools/processor/java/actual")
    runtime_deps = runtime_deps + merge_runtime_deps
    args = {"deps": real_deps, "plugins": real_plugins, "runtime_deps": runtime_deps}
    if associates != []:
        args["associates"] = associates
    return args

def _merge_library_macro(**kwargs):
    deps = kwargs.get("deps", [])
    runtime_deps = kwargs.get("runtime_deps", [])
    associates = kwargs.get("associates", [])
    merge_deps = kwargs.get("merge_deps", [])
    merge_runtime_deps = kwargs.get("merge_runtime_deps", [])
    plugins = kwargs.get("plugins", [])
    expect = kwargs.get("expect", False)
    actual = kwargs.get("actual", False)
    return kwargs | _modify_deps(deps, runtime_deps, associates, merge_deps, merge_runtime_deps, plugins, expect, actual)

def _java_merge_library_import_impl(ctx):
    return [
        MergeLibraryInfo(
            merge_jars = ctx.attr.src[_JavaInfo].full_compile_jars,
            merge_source_jars = depset(ctx.attr.src[_JavaInfo].source_jars),
            deps = [dep[MergeLibraryInfo] for dep in ctx.attr.deps],
        ),
        ctx.attr.src[_JavaInfo],
    ]

java_merge_library_import = rule(
    implementation = _java_merge_library_import_impl,
    attrs = {
        "src": attr.label(
            providers = [_JavaInfo],
        ),
        "deps": attr.label_list(
            providers = [MergeLibraryInfo],
        ),
    },
)

def _java_merge_library_impl(ctx):
    target = ctx.super()
    java_info = target[0]
    return [
        MergeLibraryInfo(
            merge_jars = java_info.full_compile_jars,
            merge_source_jars = depset(java_info.source_jars),
            deps = [dep[MergeLibraryInfo] for dep in ctx.attr.merge_deps] +
                   [dep[MergeLibraryInfo] for dep in ctx.attr.merge_runtime_deps] +
                   [dep[MergeLibraryInfo] for dep in ctx.attr.merge_only_deps],
        ),
    ] + target

java_merge_library = rule(
    parent = _java_library,
    implementation = _java_merge_library_impl,
    initializer = _merge_library_macro,
    attrs = {
        "merge_deps": attr.label_list(
            providers = [[_JavaInfo, MergeLibraryInfo]],
        ),
        "merge_runtime_deps": attr.label_list(
            providers = [[_JavaInfo, MergeLibraryInfo]],
        ),
        "merge_only_deps": attr.label_list(
            providers = [MergeLibraryInfo],
        ),
        "expect": attr.bool(
            mandatory = False,
            default = False,
        ),
        "actual": attr.bool(
            mandatory = False,
            default = False,
        ),
    },
)

def _kt_merge_library_import_impl(ctx):
    return [
        MergeLibraryInfo(
            merge_jars = ctx.attr.src[_JavaInfo].full_compile_jars,
            merge_source_jars = ctx.attr.src[_JavaInfo].source_jars,
            deps = [dep[MergeLibraryInfo] for dep in ctx.attr.deps],
        ),
        ctx.attr.src[_JavaInfo],
        ctx.attr.src[_KtJvmInfo],
    ]

kt_merge_library_import = rule(
    implementation = _kt_merge_library_import_impl,
    attrs = {
        "src": attr.label(
            providers = [_JavaInfo, _KtJvmInfo],
        ),
        "deps": attr.label_list(
            providers = [MergeLibraryInfo],
        ),
    },
)

def _kt_merge_library_impl(ctx):
    target = ctx.super()
    java_info = target[0]
    kt_info = target[1]
    return [
        MergeLibraryInfo(
            merge_jars = depset(kt_info.all_output_jars),
            merge_source_jars = depset(java_info.source_jars),
            deps = [dep[MergeLibraryInfo] for dep in ctx.attr.merge_deps] +
                   [dep[MergeLibraryInfo] for dep in ctx.attr.merge_runtime_deps] +
                   [dep[MergeLibraryInfo] for dep in ctx.attr.merge_only_deps],
        ),
    ] + target

kt_merge_library = rule(
    parent = _kt_jvm_library,
    initializer = _merge_library_macro,
    implementation = _kt_merge_library_impl,
    attrs = {
        "merge_deps": attr.label_list(
            providers = [[_JavaInfo, MergeLibraryInfo]],
        ),
        "merge_runtime_deps": attr.label_list(
            providers = [[_JavaInfo, MergeLibraryInfo]],
        ),
        "merge_only_deps": attr.label_list(
            providers = [MergeLibraryInfo],
        ),
        "expect": attr.bool(
            mandatory = False,
            default = False,
        ),
        "actual": attr.bool(
            mandatory = False,
            default = False,
        ),
    },
)

def _path_to_name(path):
    return ["--resource-strip", path.dirname, "--resource", path.path]

def _merge_library_jar_impl(ctx):
    if ctx.attr.aspect and not ctx.attr.aspect_class:
        fail("aspect_class is required when aspect is True")
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    merged_deps_depset = depset(transitive = [dep[MergeLibraryInfo].transitive_merge_jars for dep in ctx.attr.deps])
    merged_deps = merged_deps_depset.to_list()
    merged_srcs_depset = depset(transitive = [dep[MergeLibraryInfo].transitive_merge_source_jars for dep in ctx.attr.deps])

    args = ctx.actions.args()
    args.add(output_jar)

    if ctx.attr.aspect:
        args.add("--aspect-mode", "true")
        args.add("--aspect-class", ctx.attr.aspect_class)

    aspect_jar_files = [dep[AspectJarInfo].jar for dep in ctx.attr.aspects]
    for jar in aspect_jar_files:
        args.add("--aspect", jar.path)
    args.add_all(merged_deps)
    resource_files = []
    for resource in ctx.attr.resources.keys():
        strip = ctx.attr.resources[resource]
        files = resource.files.to_list()
        resource_files = resource_files + files
        if strip == ".":
            if len(files) == 0:
                fail("Resource label without resource: " + str(resource.label))
            args.add_all(files, map_each = _path_to_name)
        else:
            args.add("--resource-strip", strip)
            if len(files) == 0:
                fail("Resource label without resource: " + str(resource.label))
            args.add_all(files, before_each = "--resource")

    for key, value in ctx.attr.manifest_entries.items():
        args.add("--manifest")
        args.add(key)
        args.add(value)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = depset(
            direct = resource_files + aspect_jar_files,
            transitive = [merged_deps_depset],
        ),
        outputs = [output_jar],
        executable = ctx.executable._mergetool_executable,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        mnemonic = "MergeJar",
        progress_message = "Merging JAR %s" % ctx.label.name,
        toolchain = "@bazel_tools//tools/jdk:toolchain_type",
    )

    merge_jar_action(
        ctx.actions,
        ctx.executable._mergetool_executable,
        ctx.outputs.sources_jar,
        merged_srcs_depset,
    )

    return [
        _JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
            source_jar = ctx.outputs.sources_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ] + ([AspectJarInfo(jar = output_jar)] if ctx.attr.aspect else [])

_merge_library_jar = rule(
    implementation = _merge_library_jar_impl,
    attrs = {
        "deps": attr.label_list(
            mandatory = False,
            providers = [MergeLibraryInfo],
            doc = "Input libraries to be merged",
        ),
        "resources": attr.label_keyed_string_dict(
            mandatory = False,
            allow_files = True,
            default = {},
            doc = "Resource to be merged, with perfix to strip",
        ),
        "manifest_entries": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "Manifest entries to be include in final JAR.",
        ),
        "sources_jar": attr.output(),
        "_mergetool_executable": attr.label(
            default = Label("@//rule/mergetool:expect_actual"),
            executable = True,
            cfg = "exec",
        ),
        "aspect": attr.bool(
            mandatory = False,
            default = False,
            doc = "Whether to build this JAR as an Aspect JAR.",
        ),
        "aspect_class": attr.string(
            mandatory = False,
            default = "",
            doc = "Dotted fully qualified name of the AspectProvider interface (e.g. 'top.fifthlight.combine.CombineAspectProvider').",
        ),
        "aspects": attr.label_list(
            mandatory = False,
            providers = [AspectJarInfo],
            default = [],
            doc = "External Aspect JARs to consume.",
        ),
    },
    doc = "Merge libraries into a single JAR",
)

def merge_library_jar(**kwargs):
    name = kwargs["name"]
    _merge_library_jar(
        sources_jar = "%s_sources.jar" % name,
        **kwargs
    )
