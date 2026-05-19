combine_fabric_libraries = {
    "@maven//:androidx_compose_runtime_runtime_desktop": "androidx_compose_runtime_runtime_desktop:1.10.2",
    "@maven//:androidx_collection_collection_jvm": "androidx_collection_collection_jvm:1.5.0",
    "@maven//:org_mini2Dx_universal_tween_engine": "org_mini2dx_universal_tween_engine:6.3.3",
}

combine_unified_deps = {
    "androidx_compose_runtime_runtime_desktop": "@maven//:androidx_compose_runtime_runtime_desktop",
    "androidx_collection_collection_jvm": "@maven//:androidx_collection_collection_jvm",
    "org_mini2dx_universal_tween_engine": "@maven//:org_mini2Dx_universal_tween_engine",
}

combine_unified_neoforge = {modid: ["common"] for modid in combine_unified_deps.keys()}

combine_unified_fabric = {
    "androidx_compose_runtime_runtime_desktop": "1.10.2",
    "androidx_collection_collection_jvm": "1.5.0",
    "org_mini2Dx_universal_tween_engine": "6.3.3",
}
