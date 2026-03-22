package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import top.fifthlight.blazerod.model.Mesh
import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.getWorldTransform
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderPrimitive

class PrimitiveComponent(
    val primitiveIndex: Int,
    val primitive: RenderPrimitive,
    val skinIndex: Int?,
    val morphedPrimitiveIndex: Int?,
    val firstPersonFlag: Mesh.FirstPersonFlag = Mesh.FirstPersonFlag.BOTH,
) : RenderNodeComponent<PrimitiveComponent>() {
    init {
        primitive.increaseReferenceCount()
    }

    override fun onClosed() {
        primitive.decreaseReferenceCount()
    }

    override val type: Type<PrimitiveComponent>
        get() = Type.Primitive

    companion object {
        private val updatePhases = listOf(UpdatePhase.Type.RENDER_DATA_UPDATE)
    }

    override val updatePhases
        get() = Companion.updatePhases

    override fun update(
        phase: UpdatePhase,
        node: RenderNodeImpl,
        instance: ModelInstanceImpl,
    ) {
        if (phase is UpdatePhase.RenderDataUpdate) {
            if (skinIndex != null) {
                return
            }
            instance.modelData.localMatricesBuffer.edit {
                setMatrix(primitiveIndex, instance.getWorldTransform(node))
            }
        }
    }
}
