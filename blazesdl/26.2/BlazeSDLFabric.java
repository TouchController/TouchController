/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BlazeSDLFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("imblocker")) {
            BlazeSDL.isInputMixinDisabled = true;
            BlazeSDL.isInputHandlingDisabled = true;
        }
    }
}
