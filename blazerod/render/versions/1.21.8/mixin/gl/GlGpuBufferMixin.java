package top.fifthlight.blazerod.render.version_1_21_8.mixin.gl;

import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import top.fifthlight.blazerod.render.version_1_21_8.extension.internal.GpuBufferExtInternal;

@Mixin(GlBuffer.class)
public abstract class GlGpuBufferMixin implements GpuBufferExtInternal {
    @Unique
    private int blazerod$extraUsage;

    @Override
    public int blazerod$getExtraUsage() {
        return blazerod$extraUsage;
    }

    @Override
    public void blazerod$setExtraUsage(int extraUsage) {
        this.blazerod$extraUsage = extraUsage;
    }
}
