"""Rules for creating JAR file."""

load("@rules_java//java:defs.bzl", "JavaInfo")
load("//rule:merge_library.bzl", "MergeLibraryInfo")

def _impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    strip_prefix = ctx.attr.resource_strip_prefix

    args = ctx.actions.args()
    args.add("--output")
    args.add(output_jar)

    input_file_depsets = []
    for label in ctx.attr.resources:
        files = label.files
        input_file_depsets.append(label.files)

        for file in sorted(files.to_list(), key = lambda f: f.path):
            dirname = file.dirname

            if strip_prefix == ".":
                dirname = ""
            elif dirname.startswith(strip_prefix):
                dirname = dirname.removeprefix(strip_prefix)
            dirname = ctx.attr.resource_prefix + "/" + dirname
            if dirname.endswith("/"):
                dirname = dirname[:-1]
            if dirname.startswith("/"):
                dirname = dirname[1:]

            basename = ctx.attr.resource_rename.get(file.basename, file.basename)

            jar_entry_path = dirname + "/" + basename

            args.add("--entry")
            args.add(jar_entry_path)
            args.add(file)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = depset(transitive = input_file_depsets),
        outputs = [output_jar],
        executable = ctx.executable._create_jar_executable,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        progress_message = "Creating JAR %s" % ctx.label.name,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        MergeLibraryInfo(merge_jars = depset([output_jar])),
        DefaultInfo(files = depset([output_jar])),
    ]

jar = rule(
    implementation = _impl,
    provides = [JavaInfo, DefaultInfo, MergeLibraryInfo],
    attrs = {
        "resources": attr.label_list(
            mandatory = False,
            allow_files = True,
            default = [],
            doc = "Resource files to include in JAR",
        ),
        "resource_strip_prefix": attr.string(
            mandatory = False,
            default = ".",
            doc = "Strip prefix from resource paths",
        ),
        "resource_rename": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "Map file basename to new basename",
        ),
        "resource_prefix": attr.string(
            mandatory = False,
            default = "",
            doc = "Prefix to add to resource paths",
        ),
        "_create_jar_executable": attr.label(
            default = Label("@//rule/create_jar:create_jar_worker"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Create a JAR file from files",
)
