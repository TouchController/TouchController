package top.fifthlight.blazerod.render.common.runtime.uniform

import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.layout.LayoutStrategy

object ComputeDataUniformBuffer : UniformBuffer<ComputeDataUniformBuffer, ComputeDataUniformBuffer.ComputeDataLayout>(
    name = "ComputeDataUniformBuffer",
) {
    override val layout: ComputeDataLayout
        get() = ComputeDataLayout

    object ComputeDataLayout : GpuDataLayout<ComputeDataLayout>() {
        override val strategy: LayoutStrategy
            get() = LayoutStrategy.Std140LayoutStrategy
        var modelNormalMatrix by mat4()
        var totalVertices by uint()
        var uv1 by uint()
        var uv2 by uint()
    }
}
