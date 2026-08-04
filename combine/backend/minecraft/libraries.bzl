load("//rule:libraries.bzl", "fabric_jij_deps")

combine_common_deps = {
    "combine-common": "//combine/backend/minecraft:minecraft_common",
    "combine-26-1": "//combine/backend/minecraft/versions/26.1:26.1_merged",
    "combine-26-2": "//combine/backend/minecraft/versions/26.2:26.2_merged",
    "combine-26-3": "//combine/backend/minecraft/versions/26.3:26.3_merged",
}

combine_fabric_deps = {
    "combine-fabric-1-21-1": "//combine/backend/minecraft/fabric/1.21.1",
    "combine-fabric-1-21-8": "//combine/backend/minecraft/fabric/1.21.8",
    "combine-fabric-1-21-10": "//combine/backend/minecraft/fabric/1.21.10",
    "combine-fabric-1-21-11": "//combine/backend/minecraft/fabric/1.21.11",
}

combine_neoforge_deps = {
    "combine-neoforge-1-21-1": "//combine/backend/minecraft/neoforge/1.21.1",
    "combine-neoforge-1-21-8": "//combine/backend/minecraft/neoforge/1.21.8",
    "combine-neoforge-1-21-10": "//combine/backend/minecraft/neoforge/1.21.10",
    "combine-neoforge-1-21-11": "//combine/backend/minecraft/neoforge/1.21.11",
}

combine_fabric_config = {
    "combine-common": "=",
    "combine-26-1": "=",
    "combine-26-2": "=",
    "combine-26-3": "=",
    "combine-fabric-1-21-1": "=",
    "combine-fabric-1-21-8": "=",
    "combine-fabric-1-21-10": "=",
    "combine-fabric-1-21-11": "=",
}

combine_neoforge_config = {
    "combine-common": "",
    "combine-neoforge-1-21-1": "minecraft: +[1.21.1]",
    "combine-neoforge-1-21-8": "minecraft: +[1.21.8]",
    "combine-neoforge-1-21-10": "minecraft: +[1.21.10]",
    "combine-neoforge-1-21-11": "minecraft: +[1.21.11]",
    "combine-26-1": "minecraft: +[26.1,26.2)",
    "combine-26-2": "minecraft: +[26.2]",
    "combine-26-3": "minecraft: +[26.3]",
}

combine_fabric_jij_deps = fabric_jij_deps(
    combine_common_deps | combine_fabric_deps,
    renames = {
        "combine-fabric-1-21-1": "combine-1-21-1",
        "combine-fabric-1-21-8": "combine-1-21-8",
        "combine-fabric-1-21-10": "combine-1-21-10",
        "combine-fabric-1-21-11": "combine-1-21-11",
    },
    excludes = ["combine-common"],
)
