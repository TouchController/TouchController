load("//rule:libraries.bzl", _library = "library")

_libraries = [
    _library("androidx.compose.runtime:runtime-saveable-desktop:1.10.0"),
    _library("androidx.savedstate:savedstate-desktop:1.3.3"),
    _library("androidx.savedstate:savedstate-compose-desktop:1.3.3"),
    _library("androidx.lifecycle:lifecycle-common-jvm:2.9.4"),
    _library("androidx.lifecycle:lifecycle-runtime-desktop:2.9.4", "androidx.lifecycle:lifecycle-common-jvm:2.9.4"),
    _library("androidx.lifecycle:lifecycle-runtime-compose-desktop:2.9.4"),
    _library("androidx.arch.core:core-common:2.2.0"),
    _library("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.4.0"),
    _library("cafe.adriel.voyager:voyager-core-desktop:1.1.0-beta03"),
    _library("cafe.adriel.voyager:voyager-navigator-desktop:1.1.0-beta03"),
    _library("cafe.adriel.voyager:voyager-screenmodel-desktop:1.1.0-beta03"),
]

touchcontroller_libraries = [lib.label for lib in _libraries]
touchcontroller_fabric_libraries = {lib.label: (lib.name + ":" + lib.version) for lib in _libraries}
touchcontroller_neoforge_libraries = {lib.label: (lib.coordinate + ":LIBRARY") for lib in _libraries}
touchcontroller_merge_deps = {lib.name: lib.merge_dep for lib in _libraries if lib.merge_dep}
touchcontroller_unified_deps = {lib.name: lib.label for lib in _libraries}
touchcontroller_unified_neoforge = {lib.name: lib.constraints for lib in _libraries}
touchcontroller_unified_fabric = {lib.name: lib.version for lib in _libraries}

touchcontroller_common_deps = {
    "combine-common": "//combine/backend/minecraft:minecraft_common_standalone",
    "combine-26-1": "//combine/backend/minecraft/versions/26.1:26.1_merged",
    "combine-26-2": "//combine/backend/minecraft/versions/26.2:26.2_merged",
    "combine-theme-blackstone": "//combine/theme/blackstone:blackstone_common",
    "combine-theme-blackstone-atlas": "//combine/theme/blackstone:blackstone_atlas",
    "combine-theme-blackstone-vanilla": "//combine/theme/blackstone:blackstone_vanilla",
    "touchcontroller-api": "//touchcontroller/api",
    "touchcontroller-common": "//touchcontroller/common:common_merged",
    "touchcontroller-lang-modern": "//touchcontroller/resources/lang:lang",
    "touchcontroller-texture": "//touchcontroller/resources/texture:texture_common",
    "touchcontroller-texture-atlas": "//touchcontroller/resources/texture:texture_atlas",
    "touchcontroller-texture-vanilla": "//touchcontroller/resources/texture:texture_vanilla",
    "touchcontroller-26-1": "//touchcontroller/versions/26.1:26.1_merged",
    "touchcontroller-26-2": "//touchcontroller/versions/26.2:26.2_merged",
}

touchcontroller_neoforge_deps = {
    "combine-neoforge-1-21-1": "//combine/backend/minecraft/neoforge/1.21.1",
    "combine-neoforge-1-21-10": "//combine/backend/minecraft/neoforge/1.21.10",
    "combine-neoforge-1-21-11": "//combine/backend/minecraft/neoforge/1.21.11",
    "touchcontroller-1-21-1-neoforge": "//touchcontroller/versions/neoforge/1.21.1",
    "touchcontroller-1-21-10-neoforge": "//touchcontroller/versions/neoforge/1.21.10",
    "touchcontroller-1-21-11-neoforge": "//touchcontroller/versions/neoforge/1.21.11",
    "touchcontroller-26-1-neoforge": "//touchcontroller/versions/neoforge/26.1",
    "touchcontroller-26-1-2-neoforge": "//touchcontroller/versions/neoforge/26.1.2",
    "touchcontroller-26-2-neoforge": "//touchcontroller/versions/neoforge/26.2",
}

touchcontroller_neoforge_config = {
    "combine-common": "",
    "combine-neoforge-1-21-1": "minecraft: +[1.21.1]",
    "combine-neoforge-1-21-10": "minecraft: +[1.21.10]",
    "combine-neoforge-1-21-11": "minecraft: +[1.21.11]",
    "combine-26-1": "minecraft: +[26.1,26.2)",
    "combine-26-2": "minecraft: +[26.2]",
    "combine-theme-blackstone": "",
    "combine-theme-blackstone-atlas": "minecraft: +(,1.21.1]",
    "combine-theme-blackstone-vanilla": "minecraft: +(1.21.1,)",
    "touchcontroller-api": "",
    "touchcontroller-common": "",
    "touchcontroller-lang-modern": "minecraft: +[1.13,)",
    "touchcontroller-texture": "",
    "touchcontroller-texture-atlas": "minecraft: +[1.21.1]",
    "touchcontroller-texture-vanilla": "minecraft: +[1.21.10,26.2]",
    "touchcontroller-1-21-1-neoforge": "minecraft: +[1.21.1]",
    "touchcontroller-1-21-10-neoforge": "minecraft: +[1.21.10]",
    "touchcontroller-1-21-11-neoforge": "minecraft: +[1.21.11]",
    "touchcontroller-26-1": "minecraft: +[26.1,26.2)",
    "touchcontroller-26-2": "minecraft: +[26.2]",
    "touchcontroller-26-1-neoforge": "minecraft: +[26.1,26.1.2)",
    "touchcontroller-26-1-2-neoforge": "minecraft: +[26.1.2]",
    "touchcontroller-26-2-neoforge": "minecraft: +[26.2]",
}

touchcontroller_fabric_deps = {
    "combine-fabric-1-21-1": "//combine/backend/minecraft/fabric/1.21.1",
    "combine-fabric-1-21-10": "//combine/backend/minecraft/fabric/1.21.10",
    "combine-fabric-1-21-11": "//combine/backend/minecraft/fabric/1.21.11",
    "touchcontroller-1-21-1-fabric": "//touchcontroller/versions/fabric/1.21.1",
    "touchcontroller-1-21-10-fabric": "//touchcontroller/versions/fabric/1.21.10",
    "touchcontroller-1-21-11-fabric": "//touchcontroller/versions/fabric/1.21.11",
    "touchcontroller-26-1-fabric": "//touchcontroller/versions/fabric/26.1",
    "touchcontroller-26-2-fabric": "//touchcontroller/versions/fabric/26.2",
}

touchcontroller_fabric_config = {
    "combine-common": "=",
    "combine-fabric-1-21-1": "=",
    "combine-fabric-1-21-10": "=",
    "combine-fabric-1-21-11": "=",
    "combine-26-1": "=",
    "combine-26-2": "=",
    "combine-theme-blackstone": "=",
    "combine-theme-blackstone-atlas": "=",
    "combine-theme-blackstone-vanilla": "=",
    "touchcontroller-common": "=",
    "touchcontroller-api": "=",
    "touchcontroller-lang-modern": "=",
    "touchcontroller-texture": "=",
    "touchcontroller-texture-atlas": "=",
    "touchcontroller-texture-vanilla": "=",
    "touchcontroller-1-21-1-fabric": "=",
    "touchcontroller-1-21-10-fabric": "=",
    "touchcontroller-1-21-11-fabric": "=",
    "touchcontroller-26-1": "=",
    "touchcontroller-26-1-fabric": "=",
    "touchcontroller-26-2": "=",
    "touchcontroller-26-2-fabric": "=",
}
