package top.fifthlight.blazerod.render.common.runtime.uniform

import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.layout.LayoutStrategy

object VanillaDataUniformBuffer : UniformBuffer<VanillaDataUniformBuffer, VanillaDataUniformBuffer.VanillaDataLayout>(
    name = "VanillaDataUniformBuffer",
) {
    override val layout: VanillaDataLayout
        get() = VanillaDataLayout

    object VanillaDataLayout : GpuDataLayout<VanillaDataLayout>() {
        override val strategy: LayoutStrategy
            get() = LayoutStrategy.Std140LayoutStrategy
        var baseColor by rgba()
    }
}
