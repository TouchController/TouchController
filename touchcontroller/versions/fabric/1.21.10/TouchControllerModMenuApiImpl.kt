/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.fabric.v1_21_10

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import top.fifthlight.touchcontroller.common.ui.config.screen.getConfigScreen

class TouchControllerModMenuApiImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory<Screen> { parent -> getConfigScreen(parent) as Screen }
}
