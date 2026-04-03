"""Rules for Minecraft-specific build targets."""

load("@rules_java//java:defs.bzl", "JavaInfo")
load("//rule:merge_library.bzl", "MergeLibraryInfo", "kt_merge_library")
load("//rule/combine:texture.bzl", "TextureLibraryInfo")

def _texture_to_arg(texture):
    return ["--texture", texture.identifier, texture.texture.path, texture.metadata.path]

def _nine_patch_texture_to_arg(texture):
    return ["--ninepatch", texture.identifier, texture.texture.path, texture.metadata.path]

AtlasPackInfo = provider(
    doc = "Information about a Minecraft atlas pack including namespace, JAR file, and metadata.",
    fields = ["namespace", "atlas_jar", "atlas_metadata"],
)

def _atlas_pack_impl(ctx):
    texture_info = ctx.attr.dep[TextureLibraryInfo]
    output_file = ctx.actions.declare_file(ctx.attr.name + ".zip")
    metadata_file = ctx.actions.declare_file(ctx.attr.name + ".json")

    args = ctx.actions.args()
    args.add(ctx.attr.namespace)
    args.add(texture_info.prefix)
    args.add(output_file.path)
    args.add(metadata_file.path)
    args.add("--width", ctx.attr.width)
    args.add("--height", ctx.attr.height)
    args.add_all(texture_info.textures, map_each = _texture_to_arg)
    args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file, metadata_file],
        executable = ctx.executable._generator_bin,
        arguments = [args],
    )

    return [
        DefaultInfo(files = depset([output_file])),
        AtlasPackInfo(
            namespace = ctx.attr.namespace,
            atlas_jar = output_file,
            atlas_metadata = metadata_file,
        ),
    ]

atlas_pack = rule(
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
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/texture/atlas"),
            cfg = "exec",
            executable = True,
        ),
    },
)

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

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
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
    args.add(output_file.path)
    args.add(texture_info.package)
    args.add(texture_info.class_name)
    args.add(texture_info.prefix)
    args.add(pack_info.namespace)
    args.add_all(texture_info.textures, map_each = _texture_to_arg)
    args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
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
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/library/vanilla"),
            cfg = "exec",
            executable = True,
        ),
    },
)

def _kt_vanilla_lib_impl(name, visibility, pack, dep, resource_jars):
    source_lib = name + "_source"
    _kt_vanilla_source(
        name = source_lib,
        pack = pack,
        tags = ["manual"],
    )

    kt_merge_library(
        name = name,
        srcs = [source_lib],
        visibility = visibility,
        actual = True,
        deps = [
            "//combine/data",
            "//combine/core/paint",
            dep,
        ],
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
        "resource_jars": attr.label_list(
            allow_files = [".jar"],
            default = [],
            doc = "Resource JARs to be merged into the output JAR.",
        ),
    },
)
