package top.fifthlight.blazerod.model.bedrock.molang.value

import team.unnamed.mocha.parser.ast.Expression

sealed interface MolangValue {
    data class Plain(val value: Float) : MolangValue
    data class Molang(val molang: List<Expression>) : MolangValue

    companion object {
        val ZERO = Plain(0f)
    }
}
