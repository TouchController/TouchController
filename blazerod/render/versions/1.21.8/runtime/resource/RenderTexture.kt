package top.fifthlight.blazerod.render.version_1_21_8.runtime.resource

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.textures.TextureFormat
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RenderTexture(
    val texture: GpuTexture,
    val view: GpuTextureView,
) : AbstractRefCount() {

    override fun onClosed() {
        view.close()
        texture.close()
    }

    override val typeId: String
        get() = "gpu_texture"

    companion object {
        val WHITE_RGBA_TEXTURE by lazy {
            val buffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).asIntBuffer().apply {
                repeat(4) { put(0xFFFFFFFFu.toInt()) }
                flip()
            }
            RenderSystem.getDevice().let { device ->
                val texture = device.createTexture(
                    "White RGBA texture",
                    GpuTexture.USAGE_TEXTURE_BINDING or GpuTexture.USAGE_COPY_DST,
                    TextureFormat.RGBA8,
                    1,
                    1,
                    1,
                    1
                )
                device.createCommandEncoder().writeToTexture(texture, buffer, NativeImage.Format.RGBA, 0, 0, 0, 0, 1, 1)
                RenderTexture(texture, device.createTextureView(texture))
            }.apply {
                // Increase ref count to keep it from being closed
                increaseReferenceCount()
            }
        }
    }
}
