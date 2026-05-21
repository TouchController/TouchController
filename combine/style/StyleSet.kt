package top.fifthlight.combine.ui.style

data class StyleSet<T>(
    val normal: T,
    val focus: T = normal,
    val hover: T = focus,
    val active: T = hover,
    val disabled: T = normal,
)
