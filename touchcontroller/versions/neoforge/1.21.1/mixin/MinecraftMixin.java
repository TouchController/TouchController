package top.fifthlight.touchcontroller.neoforge.v1_21_1.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.neoforge.v1_21_1.TouchController;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        TouchController.clientClose();
    }
}
