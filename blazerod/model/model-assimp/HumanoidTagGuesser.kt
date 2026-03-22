package top.fifthlight.blazerod.model.assimp

import top.fifthlight.blazerod.model.HumanoidTag

private fun String.fixDirectionSuffix() = if (endsWith(".L", ignoreCase = true)) {
    val mainName = substringBeforeLast(".")
    "left$mainName"
} else if (endsWith(".R", ignoreCase = true)) {
    val mainName = substringBeforeLast(".")
    "right$mainName"
} else {
    this
}

internal fun guessHumanoidTagFromName(name: String): HumanoidTag? {
    val fixedDirection = name.fixDirectionSuffix()
    return HumanoidTag.fromVrmName(fixedDirection) ?: HumanoidTag.fromPmxEnglish(name)
}
