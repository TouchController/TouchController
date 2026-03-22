package top.fifthlight.blazerod.render.version_1_21_8.render

import com.mojang.blaze3d.vertex.VertexFormatElement

object BlazerodVertexFormatElements {
    val JOINT: VertexFormatElement = register(0, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.GENERIC, 4)
    val WEIGHT: VertexFormatElement = register(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4)

    private fun register(index: Int, type: VertexFormatElement.Type, usage: VertexFormatElement.Usage, count: Int) =
        VertexFormatElement.register(VertexFormatElement.ELEMENTS.size, index, type, usage, count)
}
