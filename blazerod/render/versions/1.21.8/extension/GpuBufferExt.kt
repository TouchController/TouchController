package top.fifthlight.blazerod.render.version_1_21_8.extension

import com.mojang.blaze3d.buffers.GpuBuffer

val GpuBuffer.extraUsage
    get() = (this as GpuBufferExt).`blazerod$getExtraUsage`()
