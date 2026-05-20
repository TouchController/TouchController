load("@rules_java//java:defs.bzl", "JavaInfo")

def _extract_resources_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")
    ctx.actions.run(
        inputs = [ctx.file.src],
        outputs = [output_jar],
        executable = ctx.executable._extract_resources,
        arguments = [ctx.file.src.path, output_jar.path],
        mnemonic = "ExtractResources",
    )
    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ]

extract_resources = rule(
    implementation = _extract_resources_impl,
    attrs = {
        "src": attr.label(mandatory = True, allow_single_file = [".jar", ".zip", ".srcjar"]),
        "_extract_resources": attr.label(
            default = "@//repo/neoforge/rule/extract_resources",
            executable = True,
            cfg = "exec",
        ),
    },
)
