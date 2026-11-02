"""Rules for creating unified multi-version multi-loader JAR files."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _version_included(versions, dep_versions):
    for version in dep_versions:
        if version in versions:
            return True
    return False

def _filter_by_versions(entries, versions, deps_versions):
    filtered = {}
    for key, value in entries.items():
        dep_versions = deps_versions.get(key)
        if dep_versions == None or _version_included(versions, dep_versions):
            filtered[key] = value
    return filtered

def _unified_jar_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")

    versions = {version: True for version in ctx.attr.versions}
    if ctx.attr.deps_versions and not versions:
        fail("deps_versions requires a non-empty versions list")

    deps = _filter_by_versions(ctx.attr.deps, versions, ctx.attr.deps_versions)
    fabric_deps = _filter_by_versions(ctx.attr.fabric_deps, versions, ctx.attr.deps_versions)
    forge_rules = _filter_by_versions(ctx.attr.forge_rules, versions, ctx.attr.deps_versions)
    neoforge_rules = _filter_by_versions(ctx.attr.neoforge_rules, versions, ctx.attr.deps_versions)
    merge_deps = _filter_by_versions(ctx.attr.merge_deps, versions, ctx.attr.deps_versions)

    args = ctx.actions.args()
    args.add("--plugin", "manifest")
    args.add("--plugin", "services")
    args.add("--plugin", "resource")
    args.add("--plugin", "jar_in_jar")
    args.add("--plugin", "fabric_jij")
    args.add("--plugin", "unified_jar")
    args.add(output_jar)

    args.add("--manifest-mode", "use-first-by-alphabet")

    input_files = []
    template_files = []

    # Loader JAR as regular input
    loader_jar = ctx.file.loader_jar
    args.add(loader_jar)
    input_files.append(loader_jar)

    args.add("--jij-base-path", "META-INF/jars/")

    # Template files via ResourcePlugin
    if ctx.file.fabric_mod_json:
        args.add("--resource-path", "fabric.mod.json")
        args.add(ctx.file.fabric_mod_json)
        template_files.append(ctx.file.fabric_mod_json)
    if ctx.file.neoforge_mod_toml:
        args.add("--resource-path", "META-INF/neoforge.mods.toml")
        args.add(ctx.file.neoforge_mod_toml)
        template_files.append(ctx.file.neoforge_mod_toml)
    if ctx.file.forge_mods_toml:
        args.add("--resource-path", "META-INF/mods.toml")
        args.add(ctx.file.forge_mods_toml)
        template_files.append(ctx.file.forge_mods_toml)

    # deps: modid -> label (jar-in-jar entries)
    dep_files = []
    for key, target in deps.items():
        jar = target.files.to_list()[0]
        dep_files.append(jar)
        args.add("--jar-in-jar")
        args.add(key)
        args.add(jar)

    # fabric_deps: modid -> version (for FabricJijPlugin)
    for key, value in fabric_deps.items():
        args.add("--jij-fabric")
        args.add(key)
        args.add(value)

    # neoforge_rules: modid -> condition spec string (for UnifiedJarManifestPlugin)
    for key, value in neoforge_rules.items():
        args.add("--unified-neoforge-rule")
        args.add(key)
        args.add(value)

    # forge_rules: modid -> condition spec string (for UnifiedJarManifestPlugin)
    for key, value in forge_rules.items():
        args.add("--unified-forge-rule")
        args.add(key)
        args.add(value)

    # merge_deps: modid -> modid (for UnifiedJarManifestPlugin)
    for key, value in merge_deps.items():
        args.add("--merge-deps")
        args.add(key)
        args.add(value)

    resource_files = []
    for resource, strip in ctx.attr.resources.items():
        args.add("--resource-strip", strip)
        for file in resource.files.to_list():
            resource_files.append(file)
            args.add("--resource", file)

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
        "versions": attr.string_list(
            default = [],
            doc = "Game versions this JAR is built for; dependencies with a deps_versions entry are only included when at least one of their versions is listed here",
        ),
        "deps_versions": attr.string_list_dict(
            default = {},
            doc = "Game versions supported by each dependency: modid -> version list; dependencies not listed are always included",
        ),
        "fabric_deps": attr.string_dict(
            default = {},
            doc = "Fabric entries: modid -> version",
        ),
        "neoforge_rules": attr.string_dict(
            default = {},
            doc = "NeoForge rules: modid -> condition spec string (e.g. 'minecraft: +[26.1,26.2)' or '' for always)",
        ),
        "merge_deps": attr.string_dict(
            default = {},
            doc = "Merge JAR entries on NeoForge/Forge: modid -> modid",
        ),
        "forge_rules": attr.string_dict(
            default = {},
            doc = "Forge rules: modid -> condition spec string (e.g. 'minecraft: +[26.1,26.2)' or '' for always)",
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
