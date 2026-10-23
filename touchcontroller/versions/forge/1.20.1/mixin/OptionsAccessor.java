/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.forge.v1_20_1.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Options.class)
public interface OptionsAccessor {
    @Accessor("keyAttack")
    KeyMapping touchController$getKeyAttack();

    @Accessor("keyUse")
    KeyMapping touchController$getKeyUse();

    @Accessor("keyHotbarSlots")
    KeyMapping[] touchController$getKeyHotbarSlots();
}
