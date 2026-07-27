/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod("blazesdl")
public class BlazeSDLNeoForge {
    public BlazeSDLNeoForge() {
        if (ModList.get().isLoaded("imblocker")) {
            BlazeSDL.isInputMixinDisabled = true;
        }
    }
}
