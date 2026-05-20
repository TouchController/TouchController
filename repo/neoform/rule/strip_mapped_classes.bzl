def _strip_mapped_classes_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    args = ctx.actions.args()
    args.add("--input", ctx.file.input)
    args.add("--output", output_jar)
    args.add("--mappings", ctx.file.mappings)
    args.add("--mode", ctx.attr.mode)

    ctx.actions.run(
        inputs = [ctx.file.input, ctx.file.mappings],
        outputs = [output_jar],
        executable = ctx.executable._tool,
        arguments = [args],
        mnemonic = "StripMappedClasses",
        progress_message = "Filtering classes for %s" % ctx.label.name,
    )

    return [
        DefaultInfo(files = depset([output_jar])),
    ]

strip_mapped_classes = rule(
    implementation = _strip_mapped_classes_impl,
    attrs = {
        "input": attr.label(
            allow_single_file = [".jar"],
            mandatory = True,
            doc = "Input JAR file to filter",
        ),
        "mappings": attr.label(
            allow_single_file = True,
            mandatory = True,
            doc = "TSrg mappings file for filter whitelist",
        ),
        "mode": attr.string(
            default = "whitelist",
            doc = "Filter mode: whitelist or blacklist",
        ),
        "_tool": attr.label(
            default = Label("//repo/neoform/rule/strip_mapped_classes"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Filters JAR entries to only those whose names appear in a TSrg mapping file",
)
