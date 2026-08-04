/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.extension.v26_3;

import net.minecraft.client.multiplayer.chat.GuiMessage;

import java.util.List;

public interface ChatComponentWithMessages {
    List<GuiMessage> touchcontroller$getMessages();
}
