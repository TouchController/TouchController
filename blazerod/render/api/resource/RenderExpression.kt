package top.fifthlight.blazerod.render.api.resource

import top.fifthlight.blazerod.model.Expression.Tag

data class RenderExpression(
    val name: String? = null,
    val tag: Tag? = null,
    val isBinary: Boolean = false,
    val bindings: List<Binding>,
) {
    sealed class Binding {
        data class MorphTarget(
            val morphedPrimitiveIndex: Int,
            val groupIndex: Int,
            val weight: Float,
        ) : Binding()
    }
}

data class RenderExpressionGroup(
    val name: String? = null,
    val tag: Tag? = null,
    val items: List<Item>,
) {
    data class Item(
        val expressionIndex: Int,
        val influence: Float,
    )
}
