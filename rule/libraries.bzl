load("@rules_jvm_external//:defs.bzl", "artifact")

def _split_coordinate(coordinate):
    [group, artifact_id, version] = coordinate.split(":")
    return struct(
        name = (group + "_" + artifact_id).replace(".", "_").replace("-", "_").lower(),
        group = group,
        artifact_id = artifact_id,
        version = version,
    )

def library(coordinate, merge_dep = None):
    coordinate_info = _split_coordinate(coordinate)
    return struct(
        name = coordinate_info.name,
        label = artifact(coordinate),
        version = coordinate_info.version,
        coordinate = coordinate,
        merge_dep = _split_coordinate(merge_dep).name if merge_dep else None,
    )
