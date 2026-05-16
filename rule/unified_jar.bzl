"""Rules for creating unified multi-version multi-loader JAR files."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _unified_jar_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    args = ctx.actions.args()
    args.add("--plugin", "manifest")
    args.add("--plugin", "services")
    args.add("--plugin", "resource")
    args.add("--plugin", "jar_in_jar")
    args.add("--plugin", "fabric_jij")
    args.add("--plugin", "unified_jar")
    args.add(output_jar)

    input_files = []
    template_files = []

    # Loader JAR as regular input
    loader_jar = ctx.file.loader_jar
    args.add(loader_jar)
    input_files.append(loader_jar)

    args.add("--jij-base-path")
    args.add("META-INF/jars/")

    # Template files via ResourcePlugin
    if ctx.file.fabric_mod_json:
        args.add("--resource-path")
        args.add("fabric.mod.json")
        args.add(ctx.file.fabric_mod_json)
        template_files.append(ctx.file.fabric_mod_json)
    if ctx.file.neoforge_mod_toml:
        args.add("--resource-path")
        args.add("META-INF/neoforge.mods.toml")
        args.add(ctx.file.neoforge_mod_toml)
        template_files.append(ctx.file.neoforge_mod_toml)
    if ctx.file.forge_mods_toml:
        args.add("--resource-path")
        args.add("META-INF/mods.toml")
        args.add(ctx.file.forge_mods_toml)
        template_files.append(ctx.file.forge_mods_toml)

    # deps: modid -> label (jar-in-jar entries)
    dep_files = []
    for key, target in ctx.attr.deps.items():
        jar = target.files.to_list()[0]
        dep_files.append(jar)
        args.add("--jar-in-jar")
        args.add(key)
        args.add(jar)

    # fabric_deps: modid -> version (for FabricJijPlugin)
    for key, value in ctx.attr.fabric_deps.items():
        args.add("--jij-fabric")
        args.add(key)
        args.add(value)

    # neoforge_deps: modid -> list of mcvers (for UnifiedJarManifestPlugin)
    for key, values in ctx.attr.neoforge_deps.items():
        for value in values:
            args.add("--unified-neoforge")
            args.add(key)
            args.add(value)

    # forge_deps: modid -> list of mcvers (for UnifiedJarManifestPlugin)
    for key, values in ctx.attr.forge_deps.items():
        for value in values:
            args.add("--unified-forge")
            args.add(key)
            args.add(value)

    resource_files = []
    for resource, strip in ctx.attr.resources.items():
        args.add("--resource-strip")
        args.add(strip)
        for file in resource.files.to_list():
            resource_files.append(file)
            args.add("--resource")
            args.add(file)

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = depset(input_files + dep_files + template_files + resource_files),
        outputs = [output_jar],
        executable = ctx.executable._merger,
        arguments = [args],
        progress_message = "Creating unified JAR %s" % ctx.label.name,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ]

unified_jar = rule(
    implementation = _unified_jar_impl,
    attrs = {
        "deps": attr.string_keyed_label_dict(
            mandatory = True,
            allow_files = [".jar"],
            doc = "All JARs to embed: modid -> label",
        ),
        "fabric_deps": attr.string_dict(
            default = {},
            doc = "Fabric entries: modid -> version",
        ),
        "neoforge_deps": attr.string_list_dict(
            default = {},
            doc = "NeoForge entries: modid -> list of MC versions (e.g. ['common'] or ['1.21.1', '1.21.4'])",
        ),
        "forge_deps": attr.string_list_dict(
            default = {},
            doc = "Forge entries: modid -> list of MC versions (e.g. ['common'] or ['1.21.1', '1.21.4'])",
        ),
        "fabric_mod_json": attr.label(
            allow_single_file = [".json"],
            doc = "fabric.mod.json template",
        ),
        "neoforge_mod_toml": attr.label(
            allow_single_file = [".toml"],
            doc = "neoforge.mods.toml template",
        ),
        "forge_mods_toml": attr.label(
            allow_single_file = [".toml"],
            doc = "mods.toml template for Forge",
        ),
        "resources": attr.label_keyed_string_dict(
            mandatory = False,
            allow_files = True,
            default = {},
            doc = "Resource files to include; key=label, value=strip prefix ('.' for basename only)",
        ),
        "loader_jar": attr.label(
            allow_single_file = [".jar"],
            doc = "multijar loader JAR",
            default = "//multijar",
        ),
        "_merger": attr.label(
            default = Label("@//rule/mergetool:merger"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Create unified multi-version multi-loader JAR",
)
