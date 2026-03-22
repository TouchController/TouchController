package top.fifthlight.blazerod.model.loader

import top.fifthlight.blazerod.model.Metadata

sealed class MetadataResult {
    object Unsupported : MetadataResult()

    object None : MetadataResult()

    data class Success(
        val metadata: Metadata,
    ) : MetadataResult()
}
