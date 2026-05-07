load("@rules_java//java:defs.bzl", "JavaInfo", "java_common")
load("//rule:merge_jar.bzl", "merge_jar_action")
load("//rule:merge_library.bzl", "MergeLibraryInfo")

def _text_resource_jar_impl(ctx):
    text_jar = ctx.actions.declare_file(ctx.label.name + "_text.jar")

    format = ctx.attr.format
    prefix = ctx.attr.resource_prefix

    transformed_files = []

    for src_file in ctx.files.srcs:
        if format == "lang":
            output_basename = src_file.basename.removesuffix(".json") + ".lang"
        else:
            output_basename = src_file.basename

        output_file = ctx.actions.declare_file(output_basename)

        args = ctx.actions.args()
        args.add("-i", src_file)
        args.add("-o", output_file)
        args.add("-f", format)

        ctx.actions.run(
            executable = ctx.executable._text_transformer,
            arguments = [args],
            inputs = [src_file],
            outputs = [output_file],
            mnemonic = "TextTransform",
            progress_message = "Transforming text resource %s" % src_file.short_path,
        )

        transformed_files.append(output_file)

    jar_args = ctx.actions.args()
    jar_args.add(text_jar)

    for output_file in transformed_files:
        if prefix:
            jar_entry_path = prefix + "/" + output_file.basename
        else:
            jar_entry_path = output_file.basename

        jar_args.add("--resource-path")
        jar_args.add(jar_entry_path)
        jar_args.add(output_file)

    jar_args.use_param_file("@%s", use_always = True)
    jar_args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = transformed_files,
        outputs = [text_jar],
        executable = ctx.executable._create_jar_executable,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [jar_args],
        progress_message = "Creating text resource JAR %s" % ctx.label.name,
    )

    merged_resource_jars = java_common.merge([dep[JavaInfo] for dep in ctx.attr.resource_jars])
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")
    merge_jar_action(
        ctx.actions,
        ctx.executable._merge_jar_executable,
        output_jar,
        depset([text_jar], transitive = [merged_resource_jars.full_compile_jars]),
        ctx.attr.resources,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        MergeLibraryInfo(merge_jars = depset([output_jar])),
        DefaultInfo(files = depset([output_jar])),
    ]

text_resource_jar = rule(
    implementation = _text_resource_jar_impl,
    attrs = {
        "srcs": attr.label_list(
            mandatory = True,
            allow_files = [".json"],
            doc = "Input JSON text resource files",
        ),
        "format": attr.string(
            mandatory = True,
            values = ["json", "lang"],
            doc = "Output format: json (minified, sorted) or lang (legacy .lang format)",
        ),
        "resource_prefix": attr.string(
            mandatory = False,
            default = "",
            doc = "Prefix to add to resource paths in JAR",
        ),
        "resources": attr.label_keyed_string_dict(
            mandatory = False,
            allow_files = True,
            default = {},
            doc = "Resource to be merged, with perfix to strip",
        ),
        "resource_jars": attr.label_list(
            mandatory = False,
            providers = [JavaInfo],
            doc = "Resource JARs to be merged",
        ),
        "_text_transformer": attr.label(
            default = Label("@//rule/text_transformer"),
            executable = True,
            cfg = "exec",
        ),
        "_create_jar_executable": attr.label(
            default = Label("@//rule/mergetool:core"),
            executable = True,
            cfg = "exec",
        ),
        "_merge_jar_executable": attr.label(
            default = "@//rule/mergetool:core",
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Transform JSON text resource files and package into a JAR",
)
