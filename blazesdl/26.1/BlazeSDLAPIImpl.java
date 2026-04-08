/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import org.jspecify.annotations.NonNull;
import org.lwjgl.sdl.SDL_Event;
import top.fifthlight.blazesdl.api.BlazeSDLAPI;
import top.fifthlight.blazesdl.api.BlazeSDLEventHandler;

import java.util.ArrayList;

public class BlazeSDLAPIImpl implements BlazeSDLAPI {
    private static final ArrayList<BlazeSDLEventHandler> eventHandlers = new ArrayList<>();

    @Override
    public synchronized void registerEventHandler(@NonNull BlazeSDLEventHandler handler) {
        var priority = handler.getPriority();
        var i = 0;
        for (; i < eventHandlers.size(); i++) {
            if (eventHandlers.get(i).getPriority() < priority) {
                break;
            }
        }
        eventHandlers.add(i, handler);
    }

    public boolean handleEvent(SDL_Event event) {
        for (var handler : eventHandlers) {
            if (handler.handleEvent(event)) {
                return true;
            }
        }
        return false;
    }
}
