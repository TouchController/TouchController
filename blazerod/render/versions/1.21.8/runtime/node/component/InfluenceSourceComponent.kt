package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import org.joml.Quaternionf
import org.joml.Quaternionfc
import top.fifthlight.blazerod.model.TransformId
import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase

class InfluenceSourceComponent(
    val target: TransformId,
    val targetNodeIndex: Int,
    val influence: Float,
    val influenceRotation: Boolean = false,
    val influenceTranslation: Boolean = false,
    val appendLocal: Boolean = false,
) : RenderNodeComponent<InfluenceSourceComponent>() {
    override fun onClosed() {}

    override val type: Type<InfluenceSourceComponent>
        get() = Type.InfluenceSource

    companion object {
        private val updatePhases =
            listOf(UpdatePhase.Type.INFLUENCE_TRANSFORM_UPDATE)

        private val identity: Quaternionfc = Quaternionf()
    }

    override val updatePhases
        get() = Companion.updatePhases

    private val sourceIkRotation = Quaternionf()
    override fun update(phase: UpdatePhase, node: RenderNodeImpl, instance: ModelInstanceImpl) {
        if (phase is UpdatePhase.InfluenceTransformUpdate) {
            val sourceTransformMap = instance.modelData.transformMaps[node.nodeIndex]
            instance.setTransformDecomposed(targetNodeIndex, target) {
                if (influenceRotation) {
                    val nestedAppend = sourceTransformMap.get(target)
                    if (appendLocal || nestedAppend == null) {
                        sourceTransformMap.get(TransformId.RELATIVE_ANIMATION)?.getRotation(rotation)
                            ?: rotation.identity()
                    } else {
                        nestedAppend.getRotation(rotation)
                    }
                    val sourceIk = sourceTransformMap.get(TransformId.IK)
                    if (sourceIk != null) {
                        sourceIk.getRotation(sourceIkRotation)
                        rotation.mul(sourceIkRotation)
                    }
                    identity.slerp(rotation, influence, rotation)
                }
                if (influenceTranslation) {
                    val nestedAppend = sourceTransformMap.get(target)
                    if (appendLocal || nestedAppend == null) {
                        sourceTransformMap.get(TransformId.RELATIVE_ANIMATION)?.getTranslation(translation)
                            ?: translation.set(0f)
                    } else {
                        nestedAppend.getTranslation(translation)
                    }
                    translation.mul(influence)
                }
            }
        }
    }
}
