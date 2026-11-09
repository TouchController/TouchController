/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v26_2;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.combine.backend.minecraft.render.v26_2.CanvasImpl;
import top.fifthlight.touchcontroller.common.event.render.RenderEvents;
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void onExtractRenderStateWithTooltipAndSubtitles(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!GameState.Companion.getInGame() || !GameState.Companion.getInGui()) {
            return;
        }
        graphics.nextStratum();
        var canvas = new CanvasImpl(graphics);
        RenderEvents.onScreenRender(canvas);
    }
}
