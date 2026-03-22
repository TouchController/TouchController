package top.fifthlight.blazerod.model.assimp

import org.joml.Matrix4f
import org.lwjgl.assimp.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import top.fifthlight.blazerod.model.RgbaColor
import java.nio.IntBuffer

internal fun AIMatrix4x4.toJoml() = Matrix4f().setTransposed(MemoryUtil.memByteBuffer(address(), sizeof()))

internal fun AIMaterial.getColor(
    key: String,
    type: Int = 0,
    index: Int = 0,
): RgbaColor? = MemoryStack.stackPush().use { stack ->
    val color = AIColor4D.malloc(stack)
    val result = Assimp.aiGetMaterialColor(this, key, type, index, color)
    if (result == Assimp.aiReturn_SUCCESS) {
        RgbaColor(color.r(), color.g(), color.b(), color.a())
    } else {
        null
    }
}

internal fun AIMaterial.getTexturePath(
    texType: Int,
    slot: Int = 0,
) = MemoryStack.stackPush().use { stack ->
    val path = AIString.malloc(stack)
    val result = Assimp.aiGetMaterialTexture(
        this,
        texType,
        slot,
        path,
        null as IntBuffer?,
        null,
        null,
        null,
        null,
        null
    )
    if (result == Assimp.aiReturn_SUCCESS) {
        path.dataString()
    } else {
        null
    }
}
