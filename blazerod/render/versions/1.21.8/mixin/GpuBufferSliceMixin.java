package top.fifthlight.blazerod.render.version_1_21_8.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GpuBufferSlice.class)
public abstract class GpuBufferSliceMixin {
    // Fix bug: newOffset + newLength should be less or *equals* to original length
    @Redirect(method = "slice", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;length:I", opcode = Opcodes.GETFIELD))
    public int getLength(GpuBufferSlice instance) {
        return instance.length() + 1;
    }
}
