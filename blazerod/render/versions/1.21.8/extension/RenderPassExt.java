package top.fifthlight.blazerod.render.version_1_21_8.extension;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;

public interface RenderPassExt {
    void blazerod$setVertexFormat(VertexFormat vertexFormat);

    void blazerod$setVertexFormatMode(VertexFormat.Mode vertexFormatMode);

    void blazerod$setStorageBuffer(@NotNull String name, GpuBufferSlice buffer);

    void blazerod$draw(int baseVertex, int firstIndex, int count, int instanceCount);
}
