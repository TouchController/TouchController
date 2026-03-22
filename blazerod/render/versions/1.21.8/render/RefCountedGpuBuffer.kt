package top.fifthlight.blazerod.render.version_1_21_8.render

import com.mojang.blaze3d.buffers.GpuBuffer
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount

class RefCountedGpuBuffer(val inner: GpuBuffer) : AbstractRefCount() {
    override val typeId: String
        get() = "gpu_buffer"

    override fun onClosed() {
        inner.close()
    }
}
