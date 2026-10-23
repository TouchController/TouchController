package top.fifthlight.touchcontroller.forge.v1_20_1.mixin;

import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.forge.v1_20_1.TouchController;

@Mixin(KeyModifier.class)
public class KeyModifierMixin {
    @Inject(method = "getActiveModifier", at = @At("HEAD"), cancellable = true, remap = false)
    private static void overrideGetActiveModifier(CallbackInfoReturnable<KeyModifier> cir) {
        var modifier = TouchController.getCurrentModifier();
        if (modifier != null) {
            cir.setReturnValue(modifier);
        }
    }
}
