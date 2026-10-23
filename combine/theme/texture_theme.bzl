load("//combine/theme:theme_info_jar.bzl", "theme_atlas_info_jar", "theme_info_jar", "theme_vanilla_info_jar")
load("//rule:merge_library.bzl", "kt_merge_library", "merge_library_jar")
load("//rule/combine:library.bzl", "kt_texture_lib")
load("//rule/combine:minecraft.bzl", "atlas_pack", "kt_atlas_lib", "kt_vanilla_lib", "vanilla_pack")
load("//rule/fabric:merge_jij.bzl", "fabric_merge_jij")

def _combine_theme_impl(
        name,
        visibility,
        mod_id,
        substitutions,
        aspect_class,
        srcs,
        texture,
        namespace,
        deps,
        icon,
        banner,
        vanilla_package,
        atlas_package,
        atlas_width,
        atlas_height):
    kt_texture_lib(
        name = name + "_texture_lib",
        dep = texture,
        visibility = visibility,
    )

    kt_merge_library(
        name = name,
        srcs = srcs,
        merge_deps = [":" + name + "_texture_lib"],
        visibility = visibility,
        javac_opts = "//rule:javac_opts_jvm_8",
        kotlinc_opts = "//rule:kotlinc_opts_jvm_8",
        deps = [
            "//combine/core/paint",
            "//combine/style",
            "//combine/theme/base",
        ] + deps,
    )

    theme_info_jar(
        name = name + "_mod_info_json",
        substitutions = substitutions,
    )

    icon_resources = {icon: "."} if icon else {}
    banner_resources = {banner: "."} if banner else {}
    merge_library_jar(
        name = name + "_common",
        aspect = True,
        aspect_class = aspect_class,
        resources = icon_resources | banner_resources | {
            "//combine/theme:resources/pack.mcmeta": "combine/theme/resources",
        },
        visibility = visibility,
        deps = [
            ":" + name,
            ":" + name + "_mod_info_json",
        ],
    )

    vanilla_pack(
        name = name + "_vanilla_pack",
        dep = texture,
        namespace = namespace,
    )

    kt_vanilla_lib(
        name = name + "_vanilla_lib",
        dep = ":" + name + "_texture_lib",
        pack = ":" + name + "_vanilla_pack",
        resource_jars = [":" + name + "_vanilla_pack"],
        visibility = visibility,
        package = vanilla_package,
    )

    theme_vanilla_info_jar(
        name = name + "_mod_info_json_vanilla",
        substitutions = substitutions,
    )

    merge_library_jar(
        name = name + "_vanilla",
        aspects = [":" + name + "_common"],
        aspect_impl_package_suffix = name + ".vanilla",
        visibility = visibility,
        resources = icon_resources | banner_resources,
        deps = [
            ":" + name + "_mod_info_json_vanilla",
            ":" + name + "_vanilla_lib",
        ],
    )

    atlas_pack(
        name = name + "_atlas_pack",
        dep = texture,
        namespace = namespace,
        width = atlas_width,
        height = atlas_height,
    )

    kt_atlas_lib(
        name = name + "_atlas_lib",
        dep = ":" + name + "_texture_lib",
        pack = ":" + name + "_atlas_pack",
        resource_jars = [":" + name + "_atlas_pack"],
        visibility = visibility,
        package = atlas_package,
    )

    theme_atlas_info_jar(
        name = name + "_mod_info_json_atlas",
        substitutions = substitutions,
    )

    merge_library_jar(
        name = name + "_atlas",
        aspects = [":" + name + "_common"],
        aspect_impl_package_suffix = name + ".atlas",
        visibility = visibility,
        resources = icon_resources,
        deps = [
            ":" + name + "_mod_info_json_atlas",
            ":" + name + "_atlas_lib",
        ],
    )

    fabric_merge_jij(
        name = name + "_fabric",
        input = ":" + name + "_common",
        deps = {
            ":" + name + "_vanilla": "%s-vanilla:=" % mod_id,
            ":" + name + "_atlas": "%s-atlas:=" % mod_id,
        },
    )

combine_theme = macro(
    attrs = {
        "mod_id": attr.string(mandatory = True, configurable = False),
        "substitutions": attr.string_dict(mandatory = True, configurable = False),
        "aspect_class": attr.string(mandatory = True, configurable = False),
        "srcs": attr.label_list(mandatory = True, allow_files = [".kt"]),
        "texture": attr.label(mandatory = True),
        "namespace": attr.string(default = "combine", configurable = False),
        "deps": attr.label_list(default = [], configurable = False),
        "icon": attr.label(allow_single_file = True, configurable = False),
        "banner": attr.label(allow_single_file = True, configurable = False),
        "vanilla_package": attr.string(mandatory = False),
        "atlas_package": attr.string(mandatory = False),
        "atlas_width": attr.int(mandatory = False, default = 128),
        "atlas_height": attr.int(mandatory = False, default = 128),
    },
    implementation = _combine_theme_impl,
)
