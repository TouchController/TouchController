def _libraries_conf_impl(ctx):
    output_file = ctx.actions.declare_file(ctx.label.name + ".conf")
    lines = ["{}={}".format(name, url) for name, url in ctx.attr.repositories.items()]
    lines.append("")
    lines.extend(ctx.attr.libraries)
    ctx.actions.write(output_file, "\n".join(lines) + "\n")
    return [DefaultInfo(files = depset([output_file]))]

libraries_conf = rule(
    implementation = _libraries_conf_impl,
    attrs = {
        "libraries": attr.string_list(
            mandatory = True,
            doc = "Maven coordinates (group:artifact:version) to list in the config.",
        ),
        "repositories": attr.string_dict(
            mandatory = True,
            doc = "Repository names mapped to their URLs.",
        ),
    },
    doc = "Generate a libraries.conf file from a list of Maven coordinates.",
)
