package top.fifthlight.blazerod.render.version_1_21_8.extension

import com.mojang.blaze3d.platform.NativeImage
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import top.fifthlight.blazerod.model.Texture
import java.io.IOException
import java.nio.ByteBuffer

object NativeImageExt {
    @JvmStatic
    fun read(textureType: Texture.TextureType?, buffer: ByteBuffer): NativeImage {
        require(MemoryUtil.memAddress(buffer) != 0L) { "Invalid buffer" }
        textureType?.let { textureType ->
            require(buffer.remaining() >= textureType.magic.size) { "Bad image size: ${buffer.remaining()}, type: $textureType" }
            val magicBuffer = ByteBuffer.wrap(textureType.magic)
            require(
                buffer.slice(0, textureType.magic.size).mismatch(magicBuffer) == -1
            ) { "Bad image magic for type $textureType" }
        }

        return NativeImage.read(buffer)
    }

    @JvmStatic
    fun read(pixelFormat: NativeImage.Format?, textureType: Texture.TextureType?, buffer: ByteBuffer): NativeImage {
        require(pixelFormat?.supportedByStb() != false) { throw UnsupportedOperationException("Don't know how to read format $pixelFormat") }
        require(MemoryUtil.memAddress(buffer) != 0L) { "Invalid buffer" }
        textureType?.let { textureType ->
            require(buffer.remaining() >= textureType.magic.size) { "Bad image size: ${buffer.remaining()}, type: $textureType" }
            val magicBuffer = ByteBuffer.wrap(textureType.magic)
            require(
                buffer.slice(0, textureType.magic.size).mismatch(magicBuffer) == -1
            ) { "Bad image magic for type $textureType" }
        }

        return MemoryStack.stackPush().use { memoryStack ->
            val x = memoryStack.mallocInt(1)
            val y = memoryStack.mallocInt(1)
            val channels = memoryStack.mallocInt(1)

            val imageBuffer = STBImage.stbi_load_from_memory(
                buffer,
                x,
                y,
                channels,
                pixelFormat?.components() ?: 0,
            ) ?: throw IOException("Could not load image: " + STBImage.stbi_failure_reason())

            val address = MemoryUtil.memAddress(imageBuffer)
            NativeImage(
                pixelFormat ?: NativeImage.Format.getStbFormat(channels.get(0)),
                x.get(0),
                y.get(0),
                true,
                address,
            )
        }
    }
}
