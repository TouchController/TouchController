package top.fifthlight.touchcontroller.mixin.v1_21_11;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker
    float callGetFov(Camera camera, float partialTicks, boolean applyEffects);
}
