"""Rules for generating Forge mods.toml JAR files."""

load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load("//rule:jar.bzl", "jar")

def _forge_mod_toml_jar_impl(name, visibility, src, pack_mcmeta, substitutions):
    resource_paths = {}
    if pack_mcmeta:
        pack_mcmeta_name = name + "_pack_mcmeta_expanded"
        expand_template(
            name = pack_mcmeta_name,
            template = pack_mcmeta,
            substitutions = substitutions,
            out = "%s/pack.mcmeta" % name,
        )
        resource_paths[":" + pack_mcmeta_name] = "pack.mcmeta"

    expand_template(
        name = name + "_expanded",
        template = src,
        substitutions = substitutions,
        out = name + "/META-INF/mods.toml",
    )
    jar(
        name = name,
        visibility = visibility,
        resource_paths = resource_paths | {":" + name + "_expanded": "META-INF/mods.toml"},
    )

forge_mod_toml_jar = macro(
    attrs = {
        "src": attr.label(
            mandatory = True,
            allow_single_file = [".toml"],
            doc = "Input mods.toml file",
        ),
        "pack_mcmeta": attr.label(
            mandatory = False,
            allow_single_file = [".mcmeta"],
            doc = "Input pack.mcmeta file",
        ),
        "substitutions": attr.string_dict(
            mandatory = False,
            default = {},
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _forge_mod_toml_jar_impl,
)
