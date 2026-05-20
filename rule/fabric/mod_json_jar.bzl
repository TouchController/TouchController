"""Rules for generating fabric.mod.json JAR files."""

load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load("//rule:jar.bzl", "jar")

def _fabric_mod_json_jar_impl(name, visibility, src, substitutions):
    expand_template(
        name = name + "_expanded",
        template = src,
        substitutions = substitutions,
        out = name + "/fabric.mod.json",
    )
    jar(
        name = name,
        visibility = visibility,
        resource_paths = {":" + name + "_expanded": "fabric.mod.json"},
    )

fabric_mod_json_jar = macro(
    attrs = {
        "src": attr.label(
            mandatory = True,
            allow_single_file = [".json"],
            doc = "Input fabric.mod.json file",
        ),
        "substitutions": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _fabric_mod_json_jar_impl,
)
