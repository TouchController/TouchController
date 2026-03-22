package top.fifthlight.blazerod.render.common.runtime.uniform

import top.fifthlight.blazerod.render.common.BlazeRod
import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.layout.LayoutStrategy

object InstanceDataUniformBuffer :
    UniformBuffer<InstanceDataUniformBuffer, InstanceDataUniformBuffer.InstanceDataLayout>(
        name = "InstanceDataUniformBuffer",
    ) {
    override val layout: InstanceDataLayout
        get() = InstanceDataLayout

    object InstanceDataLayout : GpuDataLayout<InstanceDataLayout>() {
        override val strategy: LayoutStrategy
            get() = LayoutStrategy.Std140LayoutStrategy
        var primitiveSize by int()
        var primitiveIndex by int()
        var viewMatrix by mat4()
        var modelMatrices by mat4Array(BlazeRod.INSTANCE_SIZE)
        var modelNormalMatrices by mat4Array(BlazeRod.INSTANCE_SIZE)
        var lightMapUvs by ivec2Array(BlazeRod.INSTANCE_SIZE)
        var overlayUvs by ivec2Array(BlazeRod.INSTANCE_SIZE)
    }
}
