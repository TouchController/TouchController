"Macro to declare game versions"

load("@rules_java//java:defs.bzl", "java_binary")
load("@rules_java//java:java_import.bzl", "java_import")
load("//rule:decompile_jar.bzl", "decompile_jar")
load("//rule:extract_jar.bzl", "extract_jar")
load("//rule:jar.bzl", "jar")
load("//rule:merge_mapping.bzl", "merge_mapping", "merge_mapping_input")
load("//rule:remap_jar.bzl", "remap_jar")

def _game_version_impl(
        name,
        visibility,
        version,
        split_source_namespace,
        client,
        client_legacy,
        client_mappings,
        client_legacy_assets,
        client_native_manifest,
        client_parchment,
        client_assets,
        client_assets_version,
        client_libraries,
        server,
        server_legacy,
        neoforge,
        intermediary,
        yarn,
        sodium_intermediary,
        iris_intermediary):
    intermediary_mapping = name + "_intermediary_mapping"
    intermediary_input = name + "_intermediary_input"
    yarn_mapping = name + "_yarn_mapping"
    named_input = name + "_named_input"
    merged_mapping = name + "_merged_mapping"
    mapping_jar = name + "_mapping_jar"
    client_intermediary = name + "_client_intermediary"
    client_named = name + "_client_named"
    client_source = name + "_client_source"
    client_named_source = name + "_client_named_source"
    client_neoforge = name + "_client_neoforge"
    server_jar_file = name + "_server_jar_file"
    server_jar = name + "_server_jar"
    server_named = name + "_server_named"
    sodium_named = name + "_sodium_named"
    iris_named = name + "_iris_named"
    vanilla_client = name + "_vanilla_client"
    parchment_input = name + "_parchment_input"

    client_namespace = "client" if split_source_namespace else "official"
    server_namespace = "server" if split_source_namespace else "official"

    if intermediary:
        extract_jar(
            name = intermediary_mapping,
            entry_path = "mappings/mappings.tiny",
            filename = "intermediary.tiny",
            input = intermediary,
        )

        merge_mapping_input(
            name = intermediary_input,
            file = ":" + intermediary_mapping,
            format = "tinyv2",
        )

    if client_mappings:
        merge_mapping_input(
            name = named_input,
            file = client_mappings,
            format = "proguard",
            namespace_mappings = {
                "source": "named",
                "target": "official",
            },
        )
    elif yarn:
        if not intermediary:
            fail("Yarn requires intermediary")
        extract_jar(
            name = yarn_mapping,
            entry_path = "mappings/mappings.tiny",
            filename = "yarn.tiny",
            input = yarn,
        )

        merge_mapping_input(
            name = named_input,
            file = ":" + yarn_mapping,
            format = "tinyv2",
        )

    if client_parchment:
        if yarn:
            fail("Parchment cannot be used with yarn")
        merge_mapping_input(
            name = parchment_input,
            file = client_parchment,
            format = "parchment",
        )

    native.alias(
        name = name + "_client",
        actual = client,
        visibility = visibility,
    )

    if client_assets:
        native.alias(
            name = name + "_client_assets",
            actual = client_assets,
            visibility = visibility,
        )

    if client_assets_version:
        native.alias(
            name = name + "_client_assets_version",
            actual = client_assets_version,
            visibility = visibility,
        )

    native.alias(
        name = name + "_client_libraries",
        actual = client_libraries,
        visibility = visibility,
    )

    decompile_jar(
        name = client_source,
        inputs = [client],
        tags = ["manual"],
    )

    if client_mappings or yarn:
        inputs = {}
        if client_mappings:
            inputs["mojmap"] = ":" + named_input
        elif yarn:
            inputs["yarn"] = ":" + named_input
        if intermediary:
            inputs["intermediary"] = ":" + intermediary_input
        if client_parchment:
            inputs["parchment"] = ":" + parchment_input

        operations = []
        if client_mappings:
            operations.append(">mojmap")
            if client_parchment:
                operations.append(">parchment")
            operations.append("changeSrc(official)")
            if intermediary:
                operations.append(">intermediary")
                # Filter out entries that don't have intermediary names
                # https://github.com/FabricMC/fabric-loom/blob/6b7a0251db4fb9b8b37b81a71bf582aa1d993b3b/src/main/java/net/fabricmc/loom/configuration/providers/mappings/mojmap/MojangMappingLayer.java#L76
                operations.append("changeSrc(intermediary, true)")
                operations.append("changeSrc(official)")
                operations.append("completeNamespace(named -> intermediary)")
        elif yarn:
            operations.append(">intermediary")
            operations.append("changeSrc(intermediary)")
            operations.append(">yarn")
            operations.append("completeNamespace(named -> intermediary)")
            operations.append("changeSrc(%s)" % client_namespace)

        merge_mapping(
            name = merged_mapping,
            inputs = inputs,
            output = "merged.tiny",
            operations = operations,
            visibility = visibility,
        )

        jar(
            name = mapping_jar,
            resources = [":" + merged_mapping],
            resource_rename = {
                "merged.tiny": "mappings.tiny",
            },
            resource_prefix = "mappings",
            visibility = visibility,
        )

        if intermediary:
            remap_jar(
                name = client_intermediary,
                from_namespace = client_namespace,
                inputs = [client],
                mapping = ":" + merged_mapping,
                to_namespace = "intermediary",
                visibility = visibility,
            )

        remap_jar(
            name = client_named,
            from_namespace = client_namespace,
            inputs = [client],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
        )

        decompile_jar(
            name = client_named_source,
            inputs = [":" + client_named],
            mappings = ":" + merged_mapping,
            tags = ["manual"],
        )

    if neoforge:
        native.alias(
            name = client_neoforge,
            actual = neoforge,
            visibility = visibility,
        )

    if server:
        if server_legacy:
            native.alias(
                name = server_jar,
                actual = server,
                visibility = visibility,
            )
        else:
            extract_jar(
                name = server_jar_file,
                entry_path = "META-INF/versions/%s/server-%s.jar" % (version, version),
                filename = "_minecraft/server.jar",
                input = server,
            )

            java_import(
                name = server_jar,
                jars = [
                    ":" + server_jar_file,
                ],
                visibility = visibility,
            )

    if server and (client_mappings or yarn):
        remap_jar(
            name = server_named,
            from_namespace = server_namespace,
            inputs = [":" + server_jar],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
        )

    if intermediary and sodium_intermediary:
        remap_jar(
            name = sodium_named,
            from_namespace = "intermediary",
            inputs = [sodium_intermediary],
            classpath = [":" + client_intermediary],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

    if intermediary and sodium_intermediary and iris_intermediary:
        remap_jar(
            name = iris_named,
            from_namespace = "intermediary",
            inputs = [iris_intermediary],
            classpath = [
                ":" + client_intermediary,
                sodium_intermediary,
            ],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

    if client_assets and client_assets_version and client_libraries:
        java_binary(
            name = vanilla_client,
            srcs = [],
            data = [
                client_assets,
                client_assets_version,
            ] + ([client_native_manifest] if client_native_manifest else []),
            env = {
                "LANG": "en_US.UTF8",
            },
            jvm_flags = [
                "-Ddev.launch.version=%s" % version,
                "-Ddev.launch.type=client",
                "-Ddev.launch.assetsVersion=$(rlocationpath %s)" % client_assets_version,
                "-Ddev.launch.mainClass=%s" % ("net.minecraft.client.Minecraft" if client_legacy else "net.minecraft.client.main.Main"),
                "-Ddev.launch.legacyAssets=%s" % ("true" if client_legacy_assets else "false"),
                "-Ddev.launch.legacyHome=%s" % ("true" if client_legacy else "false"),
                "-Xmx4G",
            ] + select({
                "@platforms//os:macos": ["-XstartOnFirstThread"],
                "//conditions:default": [],
            }) + (["-Ddev.launch.nativeManifest=$(rlocationpath %s)" % client_native_manifest] if client_native_manifest else []),
            main_class = "top.fifthlight.fabazel.devlaunchwrapper.DevLaunchWrapper",
            runtime_deps = [
                client,
                client_libraries,
                "//rule/dev_launch_wrapper",
            ],
        )

game_version = macro(
    implementation = _game_version_impl,
    attrs = {
        "version": attr.string(
            mandatory = True,
            doc = "Minecraft version",
            configurable = False,
        ),
        "split_source_namespace": attr.bool(
            mandatory = False,
            doc = "Use 'client' and 'server' source namespace in mappings. Used by babric.",
            default = False,
            configurable = False,
        ),
        "client": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Client JAR file",
            configurable = False,
        ),
        "client_legacy": attr.bool(
            default = False,
            doc = "Use legacy main class",
            configurable = False,
        ),
        "client_mappings": attr.label(
            mandatory = False,
            allow_single_file = True,
            doc = "Client mappings file",
            configurable = False,
        ),
        "client_legacy_assets": attr.bool(
            mandatory = False,
            default = False,
            doc = "Use legacy assets",
            configurable = False,
        ),
        "client_native_manifest": attr.label(
            mandatory = False,
            allow_single_file = ["json"],
            doc = "Client natives file",
            configurable = False,
        ),
        "client_parchment": attr.label(
            mandatory = False,
            allow_single_file = True,
            doc = "Parchment mappings file",
            configurable = False,
        ),
        "client_assets": attr.label(
            mandatory = False,
            doc = "Client assets for the game",
            configurable = False,
        ),
        "client_assets_version": attr.label(
            mandatory = False,
            doc = "Client assets version file for the game",
            configurable = False,
        ),
        "client_libraries": attr.label(
            mandatory = False,
            doc = "Client libraries for the game",
            configurable = False,
        ),
        "server": attr.label(
            mandatory = False,
            allow_single_file = True,
            doc = "Server JAR file",
            configurable = False,
        ),
        "server_legacy": attr.bool(
            mandatory = False,
            default = False,
            doc = "Mark the version's server JAR as legacy JARs that directly shadows libraries into main JAR",
            configurable = False,
        ),
        "neoforge": attr.label(
            mandatory = False,
            doc = "NeoForge compiled target",
            configurable = False,
        ),
        "intermediary": attr.label(
            mandatory = False,
            doc = "Intermediary mappings",
            configurable = False,
        ),
        "yarn": attr.label(
            mandatory = False,
            doc = "Yarn mappings. For those versions without official mapping.",
            configurable = False,
        ),
        "sodium_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
        "iris_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
    },
)
