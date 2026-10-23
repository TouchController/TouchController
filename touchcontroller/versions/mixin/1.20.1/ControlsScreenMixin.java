/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_20_1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.combine.backend.minecraft.text.v1_20_1.TextImpl;
import top.fifthlight.touchcontroller.common.ui.config.screen.ConfigScreenKt;

@Mixin(ControlsScreen.class)
public abstract class ControlsScreenMixin extends Screen {
    protected ControlsScreenMixin() {
        super(null);
    }

    @Inject(at = @At("TAIL"), method = "init")
    protected void init(CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var screen = (ControlsScreen) (Object) this;

        var doneButton = (Button) screen.children().get(screen.children().size() - 1);
        doneButton.setPosition(doneButton.getX(), doneButton.getY() + 24);

        var lastActionButton = (LayoutElement) screen.children().get(screen.children().size() - 2);

        var text = ConfigScreenKt.getConfigScreenButtonText();
        var component = ((TextImpl) text).getInner();
        var button = Button.builder(
                component, btn -> client.setScreen((Screen) ConfigScreenKt.getConfigScreen(screen)))
                .bounds(screen.width / 2 - 155, lastActionButton.getY() + 24, 150, 20)
                .build();
        addRenderableWidget(button);
    }
}
