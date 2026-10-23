"""Rules for generating mod loader metadata JAR for themes."""

load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load("//combine:properties.bzl", "combine_home_page", "combine_issue_tracker", "combine_license", "combine_sources_page", "combine_version")
load("//rule:mod_info_jar.bzl", "mod_info_jar")

predefined_substitutions = {
    "${version}": combine_version,
    "${license}": combine_license,
    "${home_page}": combine_home_page,
    "${sources_page}": combine_sources_page,
    "${issue_tracker}": combine_issue_tracker,
}

def _theme_info_jar_impl(name, visibility, substitutions):
    mod_info_jar(
        name = name,
        visibility = visibility,
        fabric = "//combine/theme:resources/fabric.mod.json",
        neoforge = "//combine/theme:resources/META-INF/neoforge.mods.toml",
        forge = "//combine/theme:resources/META-INF/mods.toml",
        pack_mcmeta = "//combine/theme:resources/pack.mcmeta",
        substitutions = predefined_substitutions | substitutions,
    )

theme_info_jar = macro(
    attrs = {
        "substitutions": attr.string_dict(
            mandatory = True,
            configurable = False,
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _theme_info_jar_impl,
)

def _theme_vanilla_info_jar_impl(name, visibility, substitutions):
    mod_info_jar(
        name = name,
        visibility = visibility,
        fabric = "//combine/theme:resources/fabric.mod.vanilla.json",
        neoforge = "//combine/theme:resources/META-INF/neoforge.mods.vanilla.toml",
        substitutions = predefined_substitutions | substitutions,
    )

theme_vanilla_info_jar = macro(
    attrs = {
        "substitutions": attr.string_dict(
            mandatory = True,
            configurable = False,
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _theme_vanilla_info_jar_impl,
)

def _theme_atlas_info_jar_impl(name, visibility, substitutions):
    mod_info_jar(
        name = name,
        visibility = visibility,
        fabric = "//combine/theme:resources/fabric.mod.atlas.json",
        neoforge = "//combine/theme:resources/META-INF/neoforge.mods.atlas.toml",
        forge = "//combine/theme:resources/META-INF/mods.atlas.toml",
        pack_mcmeta = "//combine/theme:resources/pack.atlas.mcmeta",
        substitutions = predefined_substitutions | substitutions,
    )

theme_atlas_info_jar = macro(
    attrs = {
        "substitutions": attr.string_dict(
            mandatory = True,
            configurable = False,
            doc = "A dictionary mapping strings to their substitutions.",
        ),
    },
    implementation = _theme_atlas_info_jar_impl,
)
