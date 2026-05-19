load("@//repo/neoform:java_source_info.bzl", "JavaSourceInfo")
load("@rules_java//java:defs.bzl", "JavaInfo")

def _inject_zip_content_impl(ctx):
    if ctx.attr.extension:
        input_extension = ctx.attr.extension
    else:
        input_extension = ctx.file.input.extension
    output_jar = ctx.actions.declare_file(ctx.label.name + "." + input_extension)

    input_deps = []
    if JavaSourceInfo in ctx.attr.input:
        input_deps.append(ctx.attr.input[JavaSourceInfo])

    args = ctx.actions.args()
    args.add(ctx.file.input.path)
    args.add(output_jar.path)
    for dep in ctx.files.deps:
        args.add(dep.path)

    ctx.actions.run(
        executable = ctx.executable._inject_jar_tool,
        arguments = [args],
        inputs = [ctx.file.input] + ctx.files.deps,
        outputs = [output_jar],
    )

    return [
        DefaultInfo(files = depset([output_jar])),
        JavaSourceInfo(
            source_jar = output_jar,
            deps = input_deps,
        ),
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
    ]

inject_zip_content = rule(
    implementation = _inject_zip_content_impl,
    attrs = {
        "input": attr.label(
            doc = "The zip file to inject into",
            allow_single_file = [".zip", ".jar", ".srcjar"],
            providers = [[], [JavaSourceInfo]],
            mandatory = True,
        ),
        "deps": attr.label_list(
            doc = "The dependencies to inject into the zip file",
            allow_files = True,
        ),
        "extension": attr.string(
            doc = "Output file's extension",
            mandatory = False,
        ),
        "_inject_jar_tool": attr.label(
            default = Label("//repo/neoform/rule/inject_zip_content"),
            executable = True,
            cfg = "exec",
        ),
    },
)
