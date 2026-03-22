package top.fifthlight.touchcontroller.mixin.v1_21_11;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;

@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Shadow
    @Final
    public static int CURSOR_NORMAL;

    @ModifyArg(method = "grabOrReleaseMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetInputMode(JII)V"), index = 2)
    private static int setCursorParameters(long window, int mode, int value) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return value;
        }
        if (!config.getRegular().getDisableMouseLock()) {
            return value;
        }
        return CURSOR_NORMAL;
    }
}
