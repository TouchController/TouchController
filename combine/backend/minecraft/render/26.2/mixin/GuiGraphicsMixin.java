package top.fifthlight.combine.backend.minecraft.render.v26_2.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SpriteAccessibleGuiGraphics;
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SubmittableGuiGraphics;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin implements SubmittableGuiGraphics, SpriteAccessibleGuiGraphics {
    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Shadow
    @Final
    private GuiGraphicsExtractor.ScissorStack scissorStack;

    @Shadow
    @Final
    private TextureAtlas guiSprites;

    @ModifyArg(
            method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;II)V",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;<init>(I)V", ordinal = 0))
    private static int
    modifyStackLimit(int stackSize) {
        return Math.max(stackSize, 64);
    }

    @Override
    public void combine$addGuiElement(GuiElementRenderState guiElementRenderState) {
        guiRenderState.addGuiElement(guiElementRenderState);
    }

    @Override
    public ScreenRectangle combine$peekScissorStack() {
        return scissorStack.peek();
    }

    @Override
    public TextureAtlasSprite combine$getSprite(Identifier identifier) {
        return guiSprites.getSprite(identifier);
    }
}
