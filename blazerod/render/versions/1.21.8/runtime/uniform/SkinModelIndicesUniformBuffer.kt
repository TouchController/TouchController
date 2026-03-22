package top.fifthlight.blazerod.render.common.runtime.uniform

import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.layout.LayoutStrategy

object SkinModelIndicesUniformBuffer :
    UniformBuffer<SkinModelIndicesUniformBuffer, SkinModelIndicesUniformBuffer.SkinModelIndicesLayout>(
        name = "SkinModelIndicesUniformBuffer",
    ) {
    override val layout: SkinModelIndicesLayout
        get() = SkinModelIndicesLayout

    object SkinModelIndicesLayout : GpuDataLayout<SkinModelIndicesLayout>() {
        override val strategy: LayoutStrategy
            get() = LayoutStrategy.Std140LayoutStrategy
        var skinJoints by int()
    }
}
