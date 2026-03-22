package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import net.minecraft.util.CommonColors
import org.joml.Matrix4f
import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.getWorldTransform

class JointComponent(
    val skinIndex: Int,
    val jointIndex: Int,
) : RenderNodeComponent<JointComponent>() {
    override fun onClosed() {}

    override val type: Type<JointComponent>
        get() = Type.Joint

    companion object {
        private val updatePhases = listOf(UpdatePhase.Type.RENDER_DATA_UPDATE, UpdatePhase.Type.DEBUG_RENDER)
    }

    override val updatePhases
        get() = Companion.updatePhases

    private val cacheMatrix = Matrix4f()

    override fun update(phase: UpdatePhase, node: RenderNodeImpl, instance: ModelInstanceImpl) {
        when (phase) {
            is UpdatePhase.RenderDataUpdate -> {
                val cacheMatrix = cacheMatrix
                cacheMatrix.set(instance.getWorldTransform(node))
                val skin = instance.scene.skins[skinIndex]
                val skinBuffer = instance.modelData.skinBuffers[skinIndex]
                val inverseMatrix = skin.inverseBindMatrices?.get(jointIndex)
                skinBuffer.edit {
                    inverseMatrix?.let { cacheMatrix.mul(it) }
                    setMatrix(jointIndex, cacheMatrix)
                }
            }

            is UpdatePhase.DebugRender -> {
                if (node.hasComponentOfType(Type.InfluenceSource)) {
                    return
                }
                val consumers = phase.multiBufferSource
                // TODO: find the real parent joint
                node.parent?.let { parentJoint ->
                    val buffer = consumers.getBuffer(DEBUG_RENDER_LAYER)

                    val parent =
                        phase.viewProjectionMatrix.mul(instance.getWorldTransform(parentJoint), Matrix4f())
                    buffer.addVertex(parent, 0f, 0f, 0f).setColor(CommonColors.YELLOW).setNormal(0f, 1f, 0f)
                    val self = phase.viewProjectionMatrix.mul(instance.getWorldTransform(node), Matrix4f())
                    buffer.addVertex(self, 0f, 0f, 0f).setColor(CommonColors.RED).setNormal(0f, 1f, 0f)
                }
            }

            else -> {}
        }
    }
}
