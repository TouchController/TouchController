/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.SDLUtil;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Inject(method = "extractWidgetRenderState", at = @At(value = "TAIL"))
    private void updateTextPos(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        var editBox = (EditBox) (Object) this;
        if (!editBox.isVisible()) {
            return;
        }
        if (!editBox.isFocused()) {
            return;
        }
        if (editBox.preeditOverlay == null) {
            var window = Minecraft.getInstance().getWindow();
            SDLUtil.updateTextInputAreaScaled(window, editBox.getX(), editBox.getY(), editBox.getWidth(),
                    editBox.getHeight(), 0);
        }
    }
}
