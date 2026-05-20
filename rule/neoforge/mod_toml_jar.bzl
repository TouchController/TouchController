"""Rules for generating NeoForge mods.toml JAR files."""

load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load("//rule:jar.bzl", "jar")

def _neoforge_mod_toml_jar_impl(name, visibility, src, substitutions):
    expand_template(
        name = name + "_expanded",
        template = src,
        substitutions = substitutions,
        out = name + "/META-INF/neoforge.mods.toml",
    )
    jar(
        name = name,
        visibility = visibility,
        resource_paths = {":" + name + "_expanded": "META-INF/neoforge.mods.toml"},
    )

neoforge_mod_toml_jar = macro(
    attrs = {
        "src": attr.label(
            mandatory = True,
            allow_single_file = [".toml"],
            doc = "Input neoforge.mods.toml file",
        ),
        "substitutions": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _neoforge_mod_toml_jar_impl,
)
