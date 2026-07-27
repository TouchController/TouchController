load("//rule:libraries.bzl", _library = "library")

_libraries = [
    _library("androidx.compose.runtime:runtime-desktop:1.10.0"),
    _library("androidx.collection:collection-jvm:1.5.0"),
    _library("org.mini2Dx:universal-tween-engine:6.3.3", constraints = "quark: -[0,)"),
]

combine_libraries = [lib.label for lib in _libraries]
combine_fabric_libraries = {lib.label: (lib.name + ":" + lib.version) for lib in _libraries}
combine_unified_deps = {lib.name: lib.label for lib in _libraries}
combine_unified_neoforge = {lib.name: lib.constraints for lib in _libraries}
combine_unified_fabric = {lib.name: lib.version for lib in _libraries}
