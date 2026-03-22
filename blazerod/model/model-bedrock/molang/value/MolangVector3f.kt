package top.fifthlight.blazerod.model.bedrock.molang.value

import org.joml.Vector3f
import org.joml.Vector3fc
import team.unnamed.mocha.parser.ast.Expression

sealed interface MolangVector3f {
    data class Plain(val value: Vector3fc) : MolangVector3f {
        constructor(value: Float) : this(Vector3f(value))
    }

    data class Molang(
        val x: MolangValue,
        val y: MolangValue,
        val z: MolangValue,
    ) : MolangVector3f {
        constructor(molang: List<Expression>) : this(MolangValue.Molang(molang))
        constructor(molang: MolangValue.Molang) : this(molang, molang, molang)
    }
}
