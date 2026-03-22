package top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component

import top.fifthlight.blazerod.render.version_1_21_8.runtime.ModelInstanceImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.RenderNodeImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.UpdatePhase
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.getWorldTransform

class CameraComponent : RenderNodeComponent<CameraComponent> {
    val cameraIndex: Int

    constructor(cameraIndex: Int) : super() {
        this.cameraIndex = cameraIndex
    }

    override fun onClosed() {}

    override val type: Type<CameraComponent>
        get() = Type.Camera

    companion object {
        private val updatePhases = listOf(UpdatePhase.Type.CAMERA_UPDATE)
    }

    override val updatePhases
        get() = Companion.updatePhases

    override fun update(phase: UpdatePhase, node: RenderNodeImpl, instance: ModelInstanceImpl) {
        if (phase is UpdatePhase.CameraUpdate) {
            val cameraTransform = instance.modelData.cameraTransforms[cameraIndex]
            cameraTransform.update(instance.getWorldTransform(node))
        }
    }
}
