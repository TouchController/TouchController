"""Rules for generating mod information JAR files."""

load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load("//rule:jar.bzl", "jar")

def _mod_info_jar_impl(name, visibility, fabric, neoforge, forge, pack_mcmeta, resources, substitutions):
    resource_paths = {}
    if fabric:
        fabric_name = name + "_fabric_expanded"
        expand_template(
            name = fabric_name,
            template = fabric,
            substitutions = substitutions,
            out = "%s/fabric.mod.json" % name,
        )
        resource_paths[":" + fabric_name] = "fabric.mod.json"
    if neoforge:
        neoforge_name = name + "_neoforge_expanded"
        mod_id = substitutions.get("${mod_id}", None)
        if mod_id:
            substitutions = substitutions | {"${mod_id}": mod_id.replace("-", "_")}
        expand_template(
            name = neoforge_name,
            template = neoforge,
            substitutions = substitutions,
            out = "%s/neoforge.mods.toml" % name,
        )
        resource_paths[":" + neoforge_name] = "META-INF/neoforge.mods.toml"
    if forge:
        forge_name = name + "_forge_expanded"
        mod_id = substitutions.get("${mod_id}", None)
        if mod_id:
            substitutions = substitutions | {"${mod_id}": mod_id.replace("-", "_")}
        expand_template(
            name = forge_name,
            template = forge,
            substitutions = substitutions,
            out = "%s/mods.toml" % name,
        )
        resource_paths[":" + forge_name] = "META-INF/mods.toml"
    if pack_mcmeta:
        pack_mcmeta_name = name + "_pack_mcmeta_expanded"
        mod_id = substitutions.get("${mod_id}", None)
        if mod_id:
            substitutions = substitutions | {"${mod_id}": mod_id.replace("-", "_")}
        expand_template(
            name = pack_mcmeta_name,
            template = pack_mcmeta,
            substitutions = substitutions,
            out = "%s/pack.mcmeta" % name,
        )
        resource_paths[":" + pack_mcmeta_name] = "pack.mcmeta"

    jar(
        name = name,
        visibility = visibility,
        resource_paths = resource_paths | resources,
    )

mod_info_jar = macro(
    attrs = {
        "fabric": attr.label(
            mandatory = False,
            allow_single_file = [".json"],
            doc = "Input fabric.mod.json file",
        ),
        "neoforge": attr.label(
            mandatory = False,
            allow_single_file = [".toml"],
            doc = "Input neoforge.mods.toml file",
        ),
        "forge": attr.label(
            mandatory = False,
            allow_single_file = [".toml"],
            doc = "Input mods.toml file",
        ),
        "pack_mcmeta": attr.label(
            mandatory = False,
            allow_single_file = [".mcmeta"],
            doc = "Input pack.mcmeta file",
        ),
        "resources": attr.label_keyed_string_dict(
            mandatory = False,
            allow_files = True,
            default = {},
            doc = "Extra resource files to include in JAR, as final paths",
        ),
        "substitutions": attr.string_dict(
            mandatory = True,
            configurable = False,
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _mod_info_jar_impl,
)
