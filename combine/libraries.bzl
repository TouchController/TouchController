load("@rules_jvm_external//:defs.bzl", "artifact")

def _library(coordinate):
    [group, artifact_id, version] = coordinate.split(":")
    return struct(
        name = (group + "_" + artifact_id).replace(".", "_").replace("-", "_").lower(),
        label = artifact(coordinate),
        version = version,
    )

_libraries = [
    _library("androidx.compose.runtime:runtime-desktop:1.10.0"),
    _library("androidx.collection:collection-jvm:1.5.0"),
    _library("org.mini2Dx:universal-tween-engine:6.3.3"),
]

combine_libraries = [lib.label for lib in _libraries]
combine_fabric_libraries = {lib.label: (lib.name + ":" + lib.version) for lib in _libraries}
combine_unified_deps = {lib.name: lib.label for lib in _libraries}
combine_unified_neoforge = {lib.name: "" for lib in _libraries}
combine_unified_fabric = {lib.name: lib.version for lib in _libraries}
