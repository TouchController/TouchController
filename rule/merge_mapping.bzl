"""Rules for merging mapping files."""

MappingInfo = provider(
    doc = "Information about mapping file format and namespace mappings.",
    fields = ["format", "namespace_mappings", "file"],
)

def _merge_mapping_input_impl(ctx):
    valid_formats = ["tiny", "tinyv2", "proguard", "parchment", "tsrg"]
    if ctx.attr.format not in valid_formats:
        fail("Invalid format: {}. Valid formats are: {}".format(
            ctx.attr.format,
            ", ".join(valid_formats),
        ))

    return [MappingInfo(
        format = ctx.attr.format,
        namespace_mappings = ctx.attr.namespace_mappings,
        file = ctx.file.file,
    )]

merge_mapping_input = rule(
    implementation = _merge_mapping_input_impl,
    attrs = {
        "file": attr.label(
            allow_single_file = [".tiny", ".txt", ".json", ".tsrg"],
            mandatory = True,
            doc = "Input mapping file",
        ),
        "format": attr.string(
            mandatory = True,
            doc = "Mapping format (tiny / tinyv2 / proguard)",
        ),
        "namespace_mappings": attr.string_dict(
            default = {},
            doc = "Namespace mappings for this input file",
        ),
    },
    doc = "Defines a single mapping file input with its parameters",
)

def _merge_mapping_impl(ctx):
    output_file = ctx.actions.declare_file(ctx.attr.output)

    args = ctx.actions.args()
    inputs = []

    for name, target in ctx.attr.inputs.items():
        info = target[MappingInfo]
        args.add("--mapping")
        args.add("name=" + name)
        args.add("format=" + info.format)
        for from_ns, to_ns in info.namespace_mappings.items():
            args.add("namespace-mapping={}:{}".format(from_ns, to_ns))
        args.add(info.file, format = "path=%s")
        inputs.append(info.file)

    args.add("--output", output_file.path)
    args.add("--")

    for operation in ctx.attr.operations:
        args.add(operation)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = inputs,
        outputs = [output_file],
        executable = ctx.executable._mapping_merger,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        progress_message = "Merging mapping files to %s" % output_file.short_path,
    )

    target = {
        "DefaultInfo": DefaultInfo(files = depset([output_file])),
        "MappingInfo": MappingInfo(
            format = "tinyv2",
            namespace_mappings = {},
            file = output_file,
        ),
    }
    return target.values()

merge_mapping = rule(
    implementation = _merge_mapping_impl,
    attrs = {
        "inputs": attr.string_keyed_label_dict(
            providers = [MappingInfo],
            mandatory = True,
            doc = "List of mapping inputs to merge",
        ),
        "output": attr.string(
            mandatory = True,
            doc = "Output file name",
        ),
        "operations": attr.string_list(
            mandatory = True,
            doc = "Operation list. Using > to import a mapping, and name(args) to call an operation.",
        ),
        "_mapping_merger": attr.label(
            default = Label("//rule/mapping_merger"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Merges multiple mapping files into a single mapping",
)
