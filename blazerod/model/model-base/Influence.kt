package top.fifthlight.blazerod.model

data class Influence(
    val target: NodeId,
    val influence: Float,
    val influenceRotation: Boolean,
    val influenceTranslation: Boolean,
    val appendLocal: Boolean,
)
