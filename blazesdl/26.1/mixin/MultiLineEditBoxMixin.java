/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.client.gui.components.MultiLineEditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.SDLUtil;

@Mixin(MultiLineEditBox.class)
public abstract class MultiLineEditBoxMixin {
    @Shadow
    private IMEPreeditOverlay preeditOverlay;

    @Inject(method = "extractContents", at = @At(value = "TAIL"))
    private void updateTextPos(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci, @Local(name = "cursorX") int cursorX, @Local(name = "hasDrawnCursor") boolean hasDrawnCursor) {
        var editBox = (MultiLineEditBox) (Object) this;
        if (!editBox.visible) {
            return;
        }
        if (!editBox.isFocused()) {
            return;
        }
        if (!hasDrawnCursor) {
            cursorX = editBox.getX();
        }
        if (preeditOverlay == null) {
            var window = Minecraft.getInstance().getWindow();
            SDLUtil.updateTextInputAreaScaled(window, editBox.getX(), editBox.getY(), editBox.getWidth(), editBox.getHeight(), cursorX - editBox.getX());
        }
    }
}
