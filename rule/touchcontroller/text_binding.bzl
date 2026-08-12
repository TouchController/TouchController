"""Rules for text binding generation."""

load("//rule:merge_library.bzl", "kt_merge_library")

def _text_binding_source_impl(ctx):
    input_file = ctx.file.input
    output_file = ctx.actions.declare_file(ctx.attr.class_name + ".kt")

    args = ctx.actions.args()
    args.add(input_file)
    args.add(output_file)
    args.add(ctx.attr.class_name)
    args.add(ctx.attr.package_name)

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = [input_file],
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        arguments = [args],
        progress_message = "Generating text binding file %s" % output_file,
    )

    return [DefaultInfo(files = depset([output_file]))]

_text_binding_source = rule(
    implementation = _text_binding_source_impl,
    attrs = {
        "input": attr.label(
            allow_single_file = [".json"],
            mandatory = True,
        ),
        "class_name": attr.string(
            default = "Texts",
            mandatory = False,
        ),
        "package_name": attr.string(
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/touchcontroller/text_binding_generator"),
            executable = True,
            cfg = "exec",
        ),
    },
)

def _kt_text_binding_lib_impl(name, visibility, input, class_name, package_name):
    source_lib = name + "_source"
    _text_binding_source(
        name = source_lib,
        input = input,
        class_name = class_name,
        package_name = package_name,
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        javac_opts = "//rule:javac_opts_jvm_8",
        kotlinc_opts = "//rule:kotlinc_opts_jvm_8",
        deps = [
            "//combine/core/data",
            "//touchcontroller/common/annotations",
        ],
    )

kt_text_binding_lib = macro(
    implementation = _kt_text_binding_lib_impl,
    attrs = {
        "input": attr.label(
            allow_single_file = [".json"],
            mandatory = True,
        ),
        "class_name": attr.string(
            default = "Texts",
            mandatory = False,
        ),
        "package_name": attr.string(
            mandatory = True,
        ),
    },
)
