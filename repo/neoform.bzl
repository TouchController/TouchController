"""NeoForm repository rule, fetches NeoForm artifact and setup Bazel rules"""

load("@//private:maven_coordinate.bzl", _convert_maven_coordinate = "convert_maven_coordinate", _convert_maven_coordinate_to_repo = "convert_maven_coordinate_to_repo", _convert_maven_coordinate_to_url = "convert_maven_coordinate_to_url")
load("@//private:pin_file.bzl", _parse_pin_file = "parse_pin_file")
load("@//private:snake_case.bzl", _camel_case_to_snake_case = "camel_case_to_snake_case")
load("@//repo/neoform:definitions.bzl", _default_function_special = "default_function_special", _function_specials = "function_specials", _names = "names", _output_placeholder_map = "output_placeholder_map", _placeholder_config = "placeholder_config", _step_handlers = "step_handlers")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")
load("@rules_java//java:defs.bzl", "JavaInfo")

def _parse_version_info(rctx):
    """Extract information from context"""
    version_name = rctx.attr.version
    version_sha256 = rctx.attr.sha256
    version_legacy = rctx.attr.legacy

    names = _names[version_legacy]

    return struct(
        version = version_name,
        sha256 = version_sha256,
        legacy = version_legacy,
        repository_url = names.url,
        repository_prefix = names.prefix,
    )

def _download_and_extract_config(rctx, version_info):
    """Download and extract NeoForm"""
    neoform_zip = "neoform.zip"
    output_prefix = "neoform/%s" % version_info.version
    config_link = _names[version_info.legacy].config

    rctx.report_progress("Downloading NeoForm JAR %s" % version_info.version)
    rctx.download(
        url = config_link % (version_info.repository_url, version_info.version, version_info.version),
        sha256 = version_info.sha256,
        output = neoform_zip,
    )
    rctx.extract(
        archive = neoform_zip,
        output = output_prefix,
    )

    config_data = json.decode(rctx.read("%s/config.json" % output_prefix))

    return config_data, output_prefix, neoform_zip

def _download_function_jars(rctx, version_info, config_data, pin_content):
    """Download all classpath JAR files"""
    rctx.report_progress("Downloading classpath JARs for functions of NeoForm %s" % version_info.version)

    function_jars = {}
    for function_name, function in config_data["functions"].items():
        entries = []

        def append_classpath(version):
            name = _convert_maven_coordinate(version)
            path = "function_jars/%s/%s.jar" % (function_name, name)
            token = rctx.download(
                url = _convert_maven_coordinate_to_url(version_info.repository_url, version),
                output = path,
                sha256 = pin_content.get(_convert_maven_coordinate_to_url(version_info.repository_url, version), ""),
                block = False,
            )
            entries.append(struct(
                repo = _convert_maven_coordinate_to_repo(version_info.repository_prefix, name),
                name = name,
                path = path,
                token = token,
            ))

        if "version" in function:
            append_classpath(function["version"])
        if "classpath" in function:
            for classpath in function["classpath"]:
                append_classpath(classpath)
        function_jars[function_name] = entries

    return function_jars

def _generate_root_build_file(rctx, config_data, output_prefix, neoform_zip):
    """Generate root BUILD.bazel"""
    root_build_content = [
        'package(default_visibility = ["//visibility:public"])',
        "",
        "alias(",
        '    name = "neoform",',
        '    actual = "%s",' % neoform_zip,
        ")",
    ]

    data_paths = {}
    for name, path in config_data["data"].items():
        if name == "patches":
            continue
        data_paths[name] = path
        root_build_content += [
            "alias(",
            '    name = "%s",' % name,
            '    actual = "%s",' % "%s/%s" % (output_prefix, path.removesuffix("/")),
            ")",
        ]

    rctx.file(
        "BUILD.bazel",
        "\n".join(root_build_content),
    )

    return data_paths

def _extract_main_class_from_manifest(manifest):
    """Extract main class from MANIFEST.MF in manifest"""
    main_class = None
    lines = manifest.split("\n")
    for line in lines:
        if line.startswith(" "):
            if main_class != None:
                main_class += line[1:].strip()
                continue
            else:
                continue
        elif main_class != None:
            break
        index = line.find(":")
        if index == -1:
            continue
        key = line[0:index].strip()
        value = line[index + 1:].strip()
        if key == "Main-Class":
            main_class = value
    if not main_class:
        fail("Failed to extract main class from manifest")
    return main_class

def _extract_function_classpaths(rctx, function_name, function_jar):
    """Extract classpath JAR, find main class"""
    main_class = None
    for entry in function_jar:
        entry.token.wait()
        extracted = "function_extracted/%s/%s" % (function_name, entry.name)
        rctx.extract(
            archive = entry.path,
            output = extracted,
            strip_prefix = "META-INF",
        )
        manifest = rctx.read(extracted + "/MANIFEST.MF")
        main_class = _extract_main_class_from_manifest(manifest)
        break
    return main_class

def _generate_function_build_file(rctx, version_info, java_target, function_name, function, classpath, main_class):
    """Generate BUILD.bazel file for function"""
    jvm_flags = []
    if "jvmargs" in function:
        for flag in function["jvmargs"]:
            jvm_flags.append('"%s"' % flag)

    spec = _function_specials.get(function_name, _default_function_special)
    main_class = spec.main_class_override or main_class
    runtime_deps = ['"@%s//jar"' % _convert_maven_coordinate_to_repo(version_info.repository_prefix, entry) for entry in classpath]
    runtime_deps.extend(spec.extra_runtime_deps)

    function_build = [
        'load("@//repo/neoform:java_version_binary.bzl", "java_%s_binary")' % java_target,
        "",
        "java_%s_binary(" % java_target,
        '    name = "%s",' % function_name,
        '    visibility = ["//visibility:public"],',
        '    main_class = "%s",' % main_class,
        "    runtime_deps = [",
        "        %s," % ",\n        ".join(runtime_deps),
        "    ],",
        "    jvm_flags = [%s]," % ", ".join(jvm_flags),
        ")",
    ]
    rctx.file(
        "functions/%s/BUILD.bazel" % function_name,
        "\n".join(function_build),
    )

def _determine_placeholder_type(name):
    if name == "libraries" or name.endswith("Libraries"):
        return "jar_list"
    elif name == "version":
        return "string"
    elif name == "output":
        return "output"
    elif name == "log":
        return "log"
    else:
        return "file"

def _extract_placeholders(arg):
    placeholders = []
    i = 0
    for idx in range(len(arg)):
        if idx < i:
            continue
        if arg[idx] == "{":
            for j in range(idx + 1, len(arg)):
                if arg[j] == "}":
                    placeholder = arg[idx:j + 1]
                    name = arg[idx + 1:j]
                    placeholders.append(struct(
                        placeholder = placeholder,
                        name = name,
                    ))
                    i = j + 1
                    break
    return placeholders

def _parse_function_arguments(args):
    arg_entries = []
    placeholder_types = {}
    output_entries = []

    for arg in args:
        placeholders = _extract_placeholders(arg)
        replacements = {}
        for placeholder in placeholders:
            if placeholder.name not in placeholder_types:
                placeholder_type = _determine_placeholder_type(placeholder.name)
                placeholder_types[placeholder.name] = placeholder_type
                if placeholder_type == "log":
                    output_entries.append(struct(
                        name = placeholder.name,
                        type = "log",
                    ))
            if placeholder.name not in replacements:
                replacements[placeholder.name] = struct(
                    name = placeholder.name,
                    type = placeholder_types[placeholder.name],
                )
        arg_entries.append(struct(
            arg = arg,
            placeholders = placeholders,
            replacements = replacements,
        ))

    return arg_entries, placeholder_types, output_entries

def _generate_function_impl(function_name, impl_name, output_type, main_class, arg_entries, placeholder_types, output_entries, context):
    output_extension = ".%s" % output_type
    rule_impl = []

    if output_type == "jar":
        rule_impl.append('load("@//repo/neoform:java_source_info.bzl", "JavaSourceInfo")')

    # 1. output_file + log files
    rule_impl += [
        "def %s(ctx):" % impl_name,
        '    output_file = ctx.actions.declare_file("_neoform_%s/" + ctx.label.name + "%s")' % (function_name, output_extension),
    ]

    for entry in output_entries:
        rule_impl.append('    %s_file = ctx.actions.declare_file("_neoform_%s/" + ctx.label.name + "_%s")' % (entry.name, function_name, entry.name))

    # 2. input_deps / input_libraries (if jar output)
    if output_type == "jar":
        rule_impl.append("    input_deps = []")
        rule_impl.append("    input_libraries = []")

    # 3. var_decl phase
    for name, type in placeholder_types.items():
        config = _placeholder_config.get(type)
        if config and config.var_decl:
            rule_impl += config.var_decl(function_name, name, output_type)

    # 4. args / action_inputs separator
    rule_impl += [
        "    args = ctx.actions.args()",
        "",
        "    action_inputs = []",
    ]

    # 5. JavaSourceInfo check (if jar output and "input" placeholder exists)
    if output_type == "jar":
        if "input" in placeholder_types:
            rule_impl.append("    if JavaSourceInfo in ctx.attr.input:")
            rule_impl.append("        input_deps.append(ctx.attr.input[JavaSourceInfo])")

    # 6. before_template_args
    spec = _function_specials.get(function_name, _default_function_special)
    if spec.before_template_args:
        rule_impl += spec.before_template_args(main_class, context)

    # 7. action_input phase
    for name, type in placeholder_types.items():
        config = _placeholder_config.get(type)
        if config and config.action_input:
            rule_impl += config.action_input(name)

    # 8. arg assembly
    for entry in arg_entries:
        arg_template = entry.arg
        parts = []
        last_end = 0

        for placeholder in entry.placeholders:
            start = arg_template.find(placeholder.placeholder, last_end)
            replacement = entry.replacements[placeholder.name]
            if start != -1:
                if start > last_end:
                    parts.append('"%s"' % arg_template[last_end:start])

                config = _placeholder_config[replacement.type]
                parts.append(config.arg(replacement.name, None))

                last_end = start + len(placeholder.placeholder)

        if last_end < len(arg_template):
            parts.append('"%s"' % arg_template[last_end:])

        if len(parts) == 1:
            rule_impl.append("    args.add(%s)" % parts[0])
        else:
            rule_impl.append("    args.add(" + " + ".join(parts) + ")")

    # 9. ctx.actions.run + return
    rule_impl.append("    ")
    rule_impl.append("    ctx.actions.run(")
    rule_impl.append("        inputs = action_inputs,")
    rule_impl.append("        outputs = [")
    rule_impl.append("            output_file,")
    for entry in output_entries:
        rule_impl.append("            %s_file," % entry.name)
    rule_impl.append("        ],")
    rule_impl.append("        executable = ctx.executable._%s_bin," % function_name)
    rule_impl.append('        mnemonic = "NeoForm%s",' % function_name.capitalize())
    rule_impl.append('        progress_message = "Running NeoForm function %s to create " + output_file.short_path,' % function_name)
    rule_impl.append("        arguments = [args],")
    rule_impl.append("    )")
    rule_impl.append("    ")
    rule_impl.append("    return [")
    rule_impl.append("        DefaultInfo(files = depset([")
    rule_impl.append("            output_file,")
    rule_impl.append("        ])),")
    if output_type == "jar":
        rule_impl.append("        JavaSourceInfo(")
        rule_impl.append("            source_jar = output_file,")
        rule_impl.append("            deps = input_deps,")
        rule_impl.append("            libraries = input_libraries,")
        rule_impl.append("        ),")
    rule_impl.append("    ]")

    return "\n".join(rule_impl)

def _generate_function_definition(data_paths, function_name, rule_impl_name, output_type, placeholder_types, context):
    rule_def = [
        "%s = rule(" % function_name,
        "    implementation = %s," % rule_impl_name,
        "    attrs = {",
    ]

    for placeholder_name, placeholder_type in placeholder_types.items():
        config = _placeholder_config.get(placeholder_type)
        if config and config.attr:
            rule_def.extend(config.attr(placeholder_name, data_paths, output_type))

    spec = _function_specials.get(function_name, _default_function_special)
    if spec.extra_attrs:
        rule_def.extend(spec.extra_attrs(context))

    rule_def.append('        "_%s_bin": attr.label(' % function_name)
    rule_def.append('            default = "//functions/%s",' % function_name)
    rule_def.append("            executable = True,")
    rule_def.append('            cfg = "exec",')
    rule_def.append("        )")
    rule_def.append("    },")
    rule_def.append(")")

    return "\n".join(rule_def)

def _generate_function(rctx, version_info, java_target, data_paths, function_name, function, function_jar, context):
    classpath = function["classpath"] if "classpath" in function else []
    if "version" in function:
        classpath.append(function["version"])
    if len(classpath) < 0:
        fail("Neoform function %s has no classpath" % function_name)

    main_class = _extract_function_classpaths(rctx, function_name, function_jar)
    _generate_function_build_file(rctx, version_info, java_target, function_name, function, classpath, main_class)

    arg_entries, placeholder_types, output_entries = _parse_function_arguments(function.get("args", []))
    rule_impl_name = "_%s_impl" % function_name
    spec = _function_specials.get(function_name, _default_function_special)
    output_type = spec.output_type
    rule_impl = _generate_function_impl(function_name, rule_impl_name, output_type, main_class, arg_entries, placeholder_types, output_entries, context)
    rule_def = _generate_function_definition(data_paths, function_name, rule_impl_name, output_type, placeholder_types, context)

    header = 'load("@rules_java//java:defs.bzl", "JavaInfo")'

    rctx.file(
        "functions/%s.bzl" % (function_name),
        header + "\n" + rule_impl + "\n" + "\n" + rule_def,
    )

def _convert_task_name(side, name):
    return "%s_%s" % (side, _camel_case_to_snake_case(name))

def _generate_task_build_file(rctx, version_info, config_data, task_name, side_name, step, step_type):
    spec = config_data.get("spec", None)
    task_def = ['package(default_visibility = ["//visibility:public"])']

    handler = _step_handlers.get(step_type)
    if handler:
        task_def.append(handler.load_line(spec))
        task_def.append("")
        task_def.append("%s(" % step_type)
        task_def.append('    name = "%s",' % task_name)
        handler.build(task_def, struct(
            rctx = rctx,
            version_info = version_info,
            config_data = config_data,
            task_name = task_name,
            side_name = side_name,
            step = step,
            spec = spec,
        ))
    else:
        task_def.append('load("//functions:%s.bzl", "%s")' % (step_type, step_type))
        task_def.append("")
        task_def.append("%s(" % step_type)
        task_def.append('    name = "%s",' % task_name)

    for item_key in step:
        if item_key == "type" or item_key == "name":
            continue

        item_value = step[item_key]
        if item_value.startswith("{") and item_value.endswith("Output}"):
            output_task = item_value[1:-7]
            resolver = _output_placeholder_map.get(output_task)
            if resolver:
                task_def.append(resolver(struct(
                    rctx = rctx,
                    item_key = item_key,
                    side_name = side_name,
                    version_info = version_info,
                    config_data = config_data,
                )))
            else:
                output_task_name = _convert_task_name(side_name, output_task)
                task_def.append('    %s = "//tasks/%s",' % (item_key, output_task_name))
        else:
            task_def.append('    %s = "%s",' % (item_key, item_value))

    task_def.append(")")

    rctx.file(
        "tasks/%s/BUILD.bazel" % task_name,
        "\n".join(task_def),
    )

def _generate_steps(rctx, version_info, config_data):
    rctx.file("tasks/BUILD.bazel", "")

    for side_name, steps in config_data["steps"].items():
        for step in steps:
            step_type = step["type"]
            if step_type.startswith("download") or step_type == "listLibraries":
                continue

            name = step.get("name", step_type)
            task_name = _convert_task_name(side_name, name)
            _generate_task_build_file(rctx, version_info, config_data, task_name, side_name, step, step_type)

def _neoform_repo_impl(rctx):
    pin_content = {}
    if rctx.attr.pin_file != None:
        pin_content = _parse_pin_file(rctx.read(rctx.attr.pin_file))

    version_info = _parse_version_info(rctx)
    config_data, output_prefix, neoform_zip = _download_and_extract_config(rctx, version_info)
    function_jars = _download_function_jars(rctx, version_info, config_data, pin_content)
    data_paths = _generate_root_build_file(rctx, config_data, output_prefix, neoform_zip)

    java_target = config_data.get("java_target", "8")

    context = struct(
        sas_data = rctx.attr.sas_data,
    )

    rctx.file("functions/BUILD.bazel", "")
    for function_name, function in config_data["functions"].items():
        _generate_function(rctx, version_info, java_target, data_paths, function_name, function, function_jars[function_name], context)

    _generate_steps(rctx, version_info, config_data)

    for jar in function_jars.values():
        for entry in jar:
            entry.token.wait()

_neoform_repo = repository_rule(
    implementation = _neoform_repo_impl,
    attrs = {
        "version": attr.string(
            doc = "Version of NeoForm",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA-256 of the NeoForm JAR file",
            mandatory = True,
        ),
        "legacy": attr.bool(
            doc = "Use legacy minecraftforge(lexforge) instead neoforged repository.",
            mandatory = False,
            default = False,
        ),
        "client_jar": attr.label(
            doc = "Client JAR file",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "server_jar": attr.label(
            doc = "Server JAR file",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "client_mapping": attr.label(
            doc = "Client mapping file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
        "server_mapping": attr.label(
            doc = "Server mapping file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
        "client_libraries": attr.label(
            doc = "Client libraries",
            mandatory = True,
            providers = [JavaInfo],
        ),
        "sas_data": attr.label(
            doc = "SAS (Side Annotation Stripper) data filegroup",
            mandatory = False,
            default = None,
        ),
        "strip_deny_patterns": attr.string_list(
            doc = "Additional deny regex patterns for the split_resources strip step (spec=2). " +
                  "Needed for <=1.17.1 where server jars contain shaded third-party deps.",
            mandatory = False,
            default = [],
        ),
        "pin_file": attr.label(
            doc = "Pin file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
    },
)

def _neoform_pin_impl(rctx):
    url_lines = ['"%s"' % url for url in rctx.attr.urls]
    pin_target = str(rctx.path(rctx.attr.pin_file)) if rctx.attr.pin_file else "neoform_pin.txt"
    rctx.template("PinGenerator.java", rctx.attr._pinner_source, {
        "/*INJECT HERE*/": ", ".join(url_lines),
        "$PIN_TARGET": pin_target,
    })

    build_bazel_contents = [
        'load("@rules_java//java:defs.bzl", "java_binary")',
        'package(default_visibility = ["//visibility:public"])',
        "",
        "java_binary(",
        '    name = "pin",',
        '    srcs = ["PinGenerator.java"],',
        '    main_class = "PinGenerator",',
        ")",
    ]
    rctx.file("BUILD.bazel", "\n".join(build_bazel_contents))

neoform_pin = repository_rule(
    implementation = _neoform_pin_impl,
    attrs = {
        "urls": attr.string_list(
            doc = "List of URLs to pin",
        ),
        "pin_file": attr.label(
            doc = "Pin file output path",
            allow_single_file = True,
            mandatory = False,
        ),
        "_pinner_source": attr.label(
            allow_single_file = [".java"],
            default = "@//repo/neoform/pin_generator:PinGenerator.java",
        ),
    },
)

version = tag_class(
    attrs = {
        "version": attr.string(
            doc = "Version of NeoForm",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA-256 of the NeoForm JAR file",
            mandatory = True,
        ),
        "legacy": attr.bool(
            doc = "Use legacy minecraftforge(lexforge) instead neoforged repository.",
            mandatory = False,
            default = False,
        ),
        "client_jar": attr.label(
            doc = "Client JAR file",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "server_jar": attr.label(
            doc = "Server JAR file",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "client_mapping": attr.label(
            doc = "Client mapping file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
        "server_mapping": attr.label(
            doc = "Server mapping file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
        "client_libraries": attr.label(
            doc = "Client libraries",
            mandatory = True,
            providers = [JavaInfo],
        ),
        "sas_data": attr.label(
            doc = "SAS (Side Annotation Stripper) data filegroup",
            mandatory = False,
            default = None,
        ),
        "strip_deny_patterns": attr.string_list(
            doc = "Additional deny regex patterns for the split_resources strip step (spec=2). " +
                  "Needed for <=1.17.1 where server jars contain shaded third-party deps.",
            mandatory = False,
            default = [],
        ),
    },
)

pin = tag_class(
    attrs = {
        "pin_file": attr.label(
            doc = "Pin file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
    },
)

def _neoform_impl(mctx):
    versions = {}
    pin_file = None
    for module in mctx.modules:
        for pin in module.tags.pin:
            if pin_file != None:
                fail("Multiple pins found")
            else:
                pin_file = pin.pin_file
        for version in module.tags.version:
            if version in versions:
                if versions[version.version].sha256 != version.sha256:
                    fail("NeoForm version %s already exists with a different SHA-256" % version.version)
                elif versions[version.version].client_jar != version.client_jar:
                    fail("NeoForm version %s already exists with a different client JAR" % version.version)
                elif versions[version.version].server_jar != version.server_jar:
                    fail("NeoForm version %s already exists with a different server JAR" % version.version)
                elif versions[version.version].client_mapping != version.client_mapping:
                    fail("NeoForm version %s already exists with a different client mapping" % version.version)
                elif versions[version.version].server_mapping != version.server_mapping:
                    fail("NeoForm version %s already exists with a different server mapping" % version.version)
                elif versions[version.version].legacy != version.legacy:
                    fail("NeoForm version %s already exists with a different legacy flag" % version.version)
                elif versions[version.version].client_libraries != version.client_libraries:
                    fail("NeoForm version %s already exists with a different client libraries" % version.version)
                elif versions[version.version].sas_data != version.sas_data:
                    fail("NeoForm version %s already exists with a different sas_data" % version.version)
                elif versions[version.version].strip_deny_patterns != version.strip_deny_patterns:
                    fail("NeoForm version %s already exists with a different strip_deny_patterns" % version.version)
            else:
                versions[version.version] = {
                    "version": version.version,
                    "sha256": version.sha256,
                    "legacy": version.legacy,
                    "client_jar": version.client_jar,
                    "server_jar": version.server_jar,
                    "client_mapping": version.client_mapping,
                    "server_mapping": version.server_mapping,
                    "client_libraries": version.client_libraries,
                    "sas_data": version.sas_data,
                    "strip_deny_patterns": version.strip_deny_patterns,
                }
    versions = versions.values()

    libraries = []

    def append_library(coordinate, legacy = False):
        item = {
            "coordinate": coordinate,
            "legacy": legacy,
        }
        if item not in libraries:
            libraries.append(item)

    for version in versions:
        version_name = version["version"]
        version_legacy = version["legacy"]
        version_sha256 = version["sha256"]
        output_prefix = "neoform/%s" % version_name

        names = _names[version_legacy]

        mctx.report_progress("Downloading NeoForm JAR %s" % version_name)
        mctx.download_and_extract(
            url = names.config % (names.url, version_name, version_name),
            type = "zip",
            sha256 = version_sha256,
            output = output_prefix,
        )

        config_data = json.decode(mctx.read("%s/config.json" % output_prefix))
        for function in config_data["functions"].values():
            if "version" in function:
                append_library(function["version"], version_legacy)
            if "classpath" in function:
                for classpath in function["classpath"]:
                    append_library(classpath, version_legacy)
        for libraries_side in config_data["libraries"]:
            for library in config_data["libraries"][libraries_side]:
                append_library(library, version_legacy)

        repo_name = names.repo_fmt % _convert_maven_coordinate(version_name)
        _neoform_repo(
            name = repo_name,
            version = version_name,
            legacy = version_legacy,
            sha256 = version_sha256,
            client_jar = version["client_jar"],
            server_jar = version["server_jar"],
            client_mapping = version["client_mapping"],
            server_mapping = version["server_mapping"],
            client_libraries = version["client_libraries"],
            sas_data = version.get("sas_data"),
            strip_deny_patterns = version.get("strip_deny_patterns", []),
            pin_file = pin_file,
        )

    pin_content = {}
    if pin_file != None:
        pin_content = _parse_pin_file(mctx.read(pin_file))
    for library in libraries:
        coordinate = library["coordinate"]
        legacy = library["legacy"]
        names = _names[legacy]
        http_jar(
            name = _convert_maven_coordinate_to_repo(names.prefix, coordinate),
            url = _convert_maven_coordinate_to_url(names.url, coordinate),
            sha256 = pin_content.get(_convert_maven_coordinate_to_url(names.url, coordinate), None),
        )

    neoform_pin(
        name = "neoform_pin",
        urls = [
            _convert_maven_coordinate_to_url(_names[library["legacy"]].url, library["coordinate"])
            for library in libraries
        ],
        pin_file = pin_file,
    )

neoform = module_extension(
    implementation = _neoform_impl,
    tag_classes = {
        "version": version,
        "pin": pin,
    },
)
