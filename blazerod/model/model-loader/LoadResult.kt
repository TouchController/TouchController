package top.fifthlight.blazerod.model.loader

import top.fifthlight.blazerod.model.Metadata
import top.fifthlight.blazerod.model.Model
import top.fifthlight.blazerod.model.animation.Animation

data class LoadResult(
    val metadata: Metadata?,
    val model: Model? = null,
    val animations: List<Animation>?,
)
