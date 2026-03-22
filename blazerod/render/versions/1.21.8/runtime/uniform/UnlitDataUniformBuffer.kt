package top.fifthlight.blazerod.render.common.runtime.uniform

import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.layout.LayoutStrategy

object UnlitDataUniformBuffer : UniformBuffer<UnlitDataUniformBuffer, UnlitDataUniformBuffer.UnlitDataLayout>(
    name = "UnlitDataUniformBuffer",
) {
    override val layout: UnlitDataLayout
        get() = UnlitDataLayout

    object UnlitDataLayout : GpuDataLayout<UnlitDataLayout>() {
        override val strategy: LayoutStrategy
            get() = LayoutStrategy.Std140LayoutStrategy
        var baseColor by rgba()
    }
}
