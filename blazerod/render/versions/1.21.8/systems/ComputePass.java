package top.fifthlight.blazerod.render.version_1_21_8.systems;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ComputePass extends AutoCloseable {
    void pushDebugGroup(@NotNull Supplier<String> label);

    void popDebugGroup();

    void setPipeline(@NotNull ComputePipeline pipeline);

    void bindSampler(@NotNull String name, @Nullable GpuTextureView view);

    void setUniform(@NotNull String name, @NotNull GpuBuffer buffer);

    void setUniform(@NotNull String name, @NotNull GpuBufferSlice slice);

    void setStorageBuffer(@NotNull String name, @NotNull GpuBufferSlice buffer);

    void dispatch(int x, int y, int z);

    @Override
    void close();
}
