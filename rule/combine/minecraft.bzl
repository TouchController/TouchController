"""Rules for Minecraft-specific build targets."""

load("@rules_java//java:defs.bzl", "JavaInfo")
load("//rule:merge_library.bzl", "MergeLibraryInfo", "kt_merge_library")
load("//rule:pngcrush.bzl", "pngcrush_action")
load("//rule/combine:texture.bzl", "TextureLibraryInfo")

_OPTIMIZE_DEFAULT = select({
    "//:config_release": True,
    "//conditions:default": False,
})

def _texture_to_arg(texture):
    return ["--texture", texture.identifier, texture.texture.path, texture.metadata.path]

def _nine_patch_texture_to_arg(texture):
    return ["--ninepatch", texture.identifier, texture.texture.path, texture.metadata.path]

AtlasPackInfo = provider(
    doc = "Information about a Minecraft atlas pack including namespace, JAR file, and metadata.",
    fields = ["namespace", "atlas_jar", "atlas_metadata", "texture_lib"],
)

def _atlas_pack_impl(ctx):
    texture_info = ctx.attr.dep[TextureLibraryInfo]
    namespace = ctx.attr.namespace
    prefix = texture_info.prefix

    bg_textures = [t for t in texture_info.textures if t.background]
    non_bg_textures = [t for t in texture_info.textures if not t.background]

    atlas_png = ctx.actions.declare_file(ctx.attr.name + "/atlas.png")
    metadata_file = ctx.actions.declare_file(ctx.attr.name + ".json")

    gen_args = ctx.actions.args()
    gen_args.add(atlas_png.path)
    gen_args.add(metadata_file.path)
    gen_args.add("--width", ctx.attr.width)
    gen_args.add("--height", ctx.attr.height)
    gen_args.add_all(non_bg_textures, map_each = _texture_to_arg)
    gen_args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    gen_args.use_param_file("@%s", use_always = True)
    gen_args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [atlas_png, metadata_file],
        executable = ctx.executable._generator_bin,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [gen_args],
    )

    final_atlas = atlas_png
    if ctx.attr.optimize:
        optimized = ctx.actions.declare_file(ctx.attr.name + "/atlas_optimized.png")
        pngcrush_action(ctx.actions, ctx.executable._pngcrush, atlas_png, optimized)
        final_atlas = optimized

    output_file = ctx.actions.declare_file(ctx.attr.name + ".zip")

    merge_args = ctx.actions.args()
    merge_args.add("--plugin", "resource")
    merge_args.add(output_file.path)
    merge_args.add("--resource-path")
    merge_args.add("assets/%s/textures/gui/%s/atlas.png" % (namespace, prefix))
    merge_args.add(final_atlas.path)
    for bg in bg_textures:
        merge_args.add("--resource-path")
        merge_args.add("assets/%s/textures/gui/%s/%s.png" % (namespace, prefix, bg.identifier))
        merge_args.add(bg.texture.path)

    merge_args.use_param_file("@%s", use_always = True)
    merge_args.set_param_file_format("multiline")

    all_inputs = depset(
        direct = [final_atlas] + [bg.texture for bg in bg_textures],
        transitive = [texture_info.files],
    )

    ctx.actions.run(
        inputs = all_inputs,
        outputs = [output_file],
        executable = ctx.executable._mergetool,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [merge_args],
    )

    return [
        DefaultInfo(files = depset([output_file])),
        AtlasPackInfo(
            namespace = namespace,
            atlas_jar = output_file,
            atlas_metadata = metadata_file,
            texture_lib = texture_info,
        ),
    ]

_atlas_pack = rule(
    implementation = _atlas_pack_impl,
    provides = [DefaultInfo, AtlasPackInfo],
    attrs = {
        "dep": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
        ),
        "namespace": attr.string(
            mandatory = True,
        ),
        "width": attr.int(
            mandatory = False,
            default = 128,
        ),
        "height": attr.int(
            mandatory = False,
            default = 128,
        ),
        "optimize": attr.bool(
            mandatory = False,
            default = False,
            doc = "Optimize atlas PNG with pngcrush",
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/texture/atlas"),
            cfg = "exec",
            executable = True,
        ),
        "_pngcrush": attr.label(
            default = "@pngcrush//:pngcrush",
            executable = True,
            cfg = "exec",
        ),
        "_mergetool": attr.label(
            default = Label("//rule/mergetool:merger"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def atlas_pack(name, optimize = _OPTIMIZE_DEFAULT, **kwargs):
    _atlas_pack(name = name, optimize = optimize, **kwargs)

VanillaPackInfo = provider(
    doc = "Information about a vanilla Minecraft pack including namespace and texture library.",
    fields = ["namespace", "texture_lib"],
)

def _vanilla_pack_impl(ctx):
    texture_info = ctx.attr.dep[TextureLibraryInfo]
    output_file = ctx.actions.declare_file(ctx.attr.name + ".zip")

    args = ctx.actions.args()
    args.add(ctx.attr.namespace)
    args.add(texture_info.prefix)
    args.add(output_file.path)
    args.add_all(texture_info.textures, map_each = _texture_to_arg)
    args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
    )

    return [
        DefaultInfo(files = depset([output_file])),
        MergeLibraryInfo(merge_jars = depset([output_file])),
        VanillaPackInfo(
            namespace = ctx.attr.namespace,
            texture_lib = texture_info,
        ),
    ]

vanilla_pack = rule(
    implementation = _vanilla_pack_impl,
    provides = [DefaultInfo, VanillaPackInfo],
    attrs = {
        "dep": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
        ),
        "namespace": attr.string(
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/texture/vanilla"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def _kt_vanilla_source_impl(ctx):
    pack_info = ctx.attr.pack[VanillaPackInfo]
    texture_info = pack_info.texture_lib
    output_file = ctx.actions.declare_file(ctx.attr.name + ".kt")

    args = ctx.actions.args()
    args.add("--output", output_file.path)
    args.add("--package", texture_info.package)
    args.add("--class-name", texture_info.class_name)
    args.add("--prefix", texture_info.prefix)
    args.add("--namespace", pack_info.namespace)
    if ctx.attr.package:
        args.add("--output-package", ctx.attr.package)
    args.add_all(texture_info.textures, map_each = _texture_to_arg)
    args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
    )

    return [DefaultInfo(files = depset([output_file]))]

_kt_vanilla_source = rule(
    implementation = _kt_vanilla_source_impl,
    attrs = {
        "pack": attr.label(
            providers = [VanillaPackInfo],
            mandatory = True,
        ),
        "package": attr.string(
            mandatory = False,
            doc = "Override output package for the generated impl class (default: same as interface package)",
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/library/vanilla"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def _kt_vanilla_lib_impl(name, visibility, pack, dep, resources, resource_strip_prefix, resource_jars, package = ""):
    source_lib = name + "_source"
    _kt_vanilla_source(
        name = source_lib,
        pack = pack,
        package = package,
        tags = ["manual"],
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        actual = True,
        javac_opts = "//rule:javac_opts_jvm_8",
        kotlinc_opts = "//rule:kotlinc_opts_jvm_8",
        deps = [
            "//combine/core/data",
            "//combine/data",
            "//combine/core/paint",
            dep,
        ],
        resources = resources,
        resource_strip_prefix = resource_strip_prefix,
        resource_jars = resource_jars,
    )

kt_vanilla_lib = macro(
    implementation = _kt_vanilla_lib_impl,
    attrs = {
        "pack": attr.label(
            providers = [VanillaPackInfo],
            mandatory = True,
        ),
        "dep": attr.label(
            providers = [JavaInfo],
            mandatory = False,
            configurable = False,
        ),
        "resources": attr.label_list(
            mandatory = False,
            allow_files = True,
            default = [],
            doc = "Resources to be packed into JAR",
        ),
        "resource_strip_prefix": attr.label(
            mandatory = False,
            doc = "Prefix to strip from resource paths.",
            allow_single_file = True,
        ),
        "resource_jars": attr.label_list(
            allow_files = [".jar"],
            default = [],
            doc = "Resource JARs to be merged into the output JAR.",
        ),
        "package": attr.string(
            mandatory = False,
            doc = "Override output package for the generated impl class (default: same as interface package)",
        ),
    },
)

def _background_texture_to_arg(texture):
    return ["--texture", texture.identifier, texture.texture.path, texture.metadata.path]

def _kt_atlas_source_impl(ctx):
    pack_info = ctx.attr.pack[AtlasPackInfo]
    texture_info = pack_info.texture_lib
    output_file = ctx.actions.declare_file(ctx.attr.name + ".kt")

    background_textures = [t for t in texture_info.textures if t.background]

    args = ctx.actions.args()
    args.add("--output", output_file.path)
    args.add("--package", texture_info.package)
    args.add("--class-name", texture_info.class_name)
    args.add("--prefix", texture_info.prefix)
    args.add("--namespace", pack_info.namespace)
    args.add("--atlas-metadata", pack_info.atlas_metadata)
    if ctx.attr.package:
        args.add("--output-package", ctx.attr.package)
    args.add_all(background_textures, map_each = _background_texture_to_arg)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = depset([pack_info.atlas_metadata], transitive = [texture_info.files]),
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
    )

    return [DefaultInfo(files = depset([output_file]))]

_kt_atlas_source = rule(
    implementation = _kt_atlas_source_impl,
    attrs = {
        "pack": attr.label(
            providers = [AtlasPackInfo],
            mandatory = True,
        ),
        "package": attr.string(
            mandatory = False,
            doc = "Override output package for the generated impl class (default: same as interface package)",
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/library/atlas"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def _kt_atlas_lib_impl(name, visibility, pack, dep, resources, resource_strip_prefix, resource_jars, package = ""):
    source_lib = name + "_source"
    _kt_atlas_source(
        name = source_lib,
        pack = pack,
        package = package,
        tags = ["manual"],
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        actual = True,
        javac_opts = "//rule:javac_opts_jvm_8",
        kotlinc_opts = "//rule:kotlinc_opts_jvm_8",
        deps = [
            "//combine/core/data",
            "//combine/data",
            "//combine/core/paint",
            "//combine/core/util/atlas",
            "//combine/core/util/ninepatch",
            dep,
        ],
        resources = resources,
        resource_strip_prefix = resource_strip_prefix,
        resource_jars = resource_jars,
    )

kt_atlas_lib = macro(
    implementation = _kt_atlas_lib_impl,
    attrs = {
        "pack": attr.label(
            providers = [AtlasPackInfo],
            mandatory = True,
        ),
        "dep": attr.label(
            providers = [JavaInfo],
            mandatory = False,
            configurable = False,
        ),
        "resources": attr.label_list(
            mandatory = False,
            allow_files = True,
            default = [],
            doc = "Resources to be packed into JAR",
        ),
        "resource_strip_prefix": attr.label(
            mandatory = False,
            doc = "Prefix to strip from resource paths.",
            allow_single_file = True,
        ),
        "resource_jars": attr.label_list(
            allow_files = [".jar"],
            default = [],
            doc = "Resource JARs to be merged into the output JAR.",
        ),
        "package": attr.string(
            mandatory = False,
            doc = "Override output package for the generated impl class (default: same as interface package)",
        ),
    },
)
