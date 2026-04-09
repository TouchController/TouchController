/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.BackendOptions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.*;
import top.fifthlight.blazesdl.SDLError;

import java.util.function.LongSupplier;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Redirect(method = "initBackendSystem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GLX;_initGlfw(Lcom/mojang/blaze3d/platform/BackendOptions;)Ljava/util/function/LongSupplier;"))
    private static LongSupplier redirectInitGlfw(BackendOptions options) {
        if (SDLInit.SDL_WasInit(SDLInit.SDL_INIT_VIDEO) == 0) {
            SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO);
        }

        SDLWindow.useExclusiveFullscreen = options.exclusiveFullScreen();

        final var freq = SDLTimer.SDL_GetPerformanceFrequency();
        var multiplier = 1_000_000_000.0 / freq;
        return () -> (long) (SDLTimer.SDL_GetPerformanceCounter() * multiplier);
    }

    @Inject(method = "setErrorCallback", at = @At(value = "HEAD"), cancellable = true)
    private static void skipGlfwErrorCallback(GLFWErrorCallbackI onFullscreenError, CallbackInfo ci) {
        if (Minecraft.getInstance().getWindow() instanceof SDLWindow) {
            ci.cancel();
        }
    }

    @Unique
    private static long windowIdToHandle(int id) {
        return SDLVideo.SDL_GetWindowFromID(id);
    }

    @Redirect(method = "pollEvents", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwPollEvents()V"))
    private static void overridePollEvents() {
        var minecraft = Minecraft.getInstance();
        if (!(minecraft.getWindow() instanceof SDLWindow sdlWindow)) {
            throw new IllegalStateException("Current window is not SDLWindow!");
        }

        SDLUtil.keyboardState = SDLKeyboard.SDL_GetKeyboardState();

        try (var stack = MemoryStack.stackPush()) {
            var event = SDL_Event.malloc(stack);
            while (SDLEvents.SDL_PollEvent(event)) {
                if (BlazeSDL.API.handleEvent(event)) {
                    continue;
                }

                var eventType = event.type();
                switch (eventType) {
                    case SDLEvents.SDL_EVENT_DISPLAY_ADDED, SDLEvents.SDL_EVENT_DISPLAY_REMOVED ->
                            sdlWindow.getScreenManager().onMonitorChange(event.display().displayID(), eventType);

                    case SDLEvents.SDL_EVENT_WINDOW_MOVED -> {
                        var callback = EventCallback.onWindowMove;
                        var windowHandle = windowIdToHandle(event.window().windowID());
                        if (windowHandle == 0L) {
                            continue;
                        }
                        if (callback != null) {
                            callback.invoke(windowHandle, event.window().data1(),
                                    event.window().data2());
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_RESIZED -> {
                        var windowHandle = windowIdToHandle(event.window().windowID());
                        if (windowHandle == 0L) {
                            continue;
                        }
                        var windowCallback = EventCallback.onWindowResize;
                        if (windowCallback != null) {
                            windowCallback.invoke(windowHandle, event.window().data1(), event.window().data2());
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED -> {
                        var windowHandle = windowIdToHandle(event.window().windowID());
                        if (windowHandle == 0L) {
                            continue;
                        }
                        var framebufferCallback = EventCallback.onFramebufferResize;
                        if (framebufferCallback != null) {
                            var w = stack.ints(0);
                            var h = stack.ints(0);

                            if (!SDLVideo.SDL_GetWindowSizeInPixels(windowHandle, w, h)) {
                                throw SDLError.handleError("SDL_GetWindowSizeInPixels");
                            }
                            framebufferCallback.invoke(windowHandle, w.get(), h.get());
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED, SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> {
                        var windowHandle = windowIdToHandle(event.window().windowID());
                        if (windowHandle == 0L) {
                            continue;
                        }
                        var callback = EventCallback.onWindowFocus;
                        if (callback != null) {
                            var focused = eventType == SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED;
                            callback.invoke(windowHandle, focused);
                        }
                    }
                    case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> sdlWindow.shouldClose = true;

                    case SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> {
                        var callback = EventCallback.keyPressCallback;
                        if (callback != null) {
                            var key = event.key();
                            var windowHandle = windowIdToHandle(key.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            int action;
                            if (eventType == SDLEvents.SDL_EVENT_KEY_DOWN) {
                                if (key.repeat()) {
                                    action = GLFW.GLFW_REPEAT;
                                } else {
                                    action = GLFW.GLFW_PRESS;
                                }
                            } else {
                                action = GLFW.GLFW_RELEASE;
                            }
                            var keyCode = SDLKeyMapping.toGlfwKey(key.key());
                            var modifier = SDLKeyMapping.getGlfwModifiers(key.mod());
                            callback.invoke(windowHandle, keyCode, key.scancode(), action, modifier);
                        }
                    }
                    case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                        var callback = EventCallback.charTypedCallback;
                        if (callback != null) {
                            var text = event.text();
                            var windowHandle = windowIdToHandle(text.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            var textStr = text.textString();
                            if (textStr != null) {
                                textStr.codePoints().forEach(ch -> callback.invoke(windowHandle, ch));
                            }
                        }
                    }
                    case SDLEvents.SDL_EVENT_TEXT_EDITING -> {
                        var callback = EventCallback.preeditCallback;
                        if (callback == null) {
                            break;
                        }

                        var edit = event.edit();
                        var windowHandle = windowIdToHandle(edit.windowID());
                        if (windowHandle == 0L) {
                            continue;
                        }
                        var textString = edit.textString();
                        if (textString == null || textString.isEmpty()) {
                            callback.invoke(windowIdToHandle(edit.windowID()), 0, 0L, 0, 0L, 0, 0);
                            break;
                        }

                        var codePoints = stack.mallocInt(textString.length()); // At most textString.length codepoints
                        textString.codePoints().forEachOrdered(codePoints::put);
                        codePoints.flip();

                        var totalCodePoints = codePoints.limit();
                        var rawStart = edit.start();
                        var rawLength = edit.length();

                        int codePointStart;
                        int codePointLength;
                        if (SDLUtil.IS_WINDOWS && rawStart != -1 && rawLength != -1) {
                            var charLimit = textString.length();
                            var charStart = Math.clamp(rawStart, 0, charLimit);
                            var charEnd = Math.clamp(charStart + rawLength, charStart, charLimit);

                            codePointStart = textString.codePointCount(0, charStart);
                            codePointLength = textString.codePointCount(charStart, charEnd);
                        } else {
                            codePointStart = rawStart;
                            codePointLength = rawLength;
                        }

                        if (codePointStart == -1 || codePointLength == -1) {
                            codePointStart = 0;
                            codePointLength = totalCodePoints;
                        } else {
                            codePointStart = Math.min(codePointStart, totalCodePoints);
                            codePointLength = Math.min(codePointLength, totalCodePoints - codePointStart);
                        }

                        // Populate text blocks
                        var blockSizes = stack.mallocInt(3);
                        var blockCount = 0;
                        var focusedBlock = 0;
                        if (codePointStart > 0) {
                            blockSizes.put(codePointStart);
                            blockCount++;
                        }
                        if (codePointLength > 0) {
                            focusedBlock = blockCount;
                            blockSizes.put(codePointLength);
                            blockCount++;
                        }
                        var afterSelectedLength = totalCodePoints - (codePointStart + codePointLength);
                        if (afterSelectedLength > 0) {
                            blockSizes.put(afterSelectedLength);
                            blockCount++;
                        }
                        blockSizes.flip();

                        var caretPosition = codePointStart + codePointLength;
                        callback.invoke(windowHandle,
                                codePoints.limit(), // preedit_count
                                MemoryUtil.memAddress(codePoints), // preedit_string
                                blockCount, // block_count
                                MemoryUtil.memAddress(blockSizes), // block_sizes
                                focusedBlock, // focused_block
                                caretPosition // caret
                        );
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                        var callback = EventCallback.onMoveCallback;
                        if (callback != null) {
                            var motion = event.motion();
                            var windowHandle = windowIdToHandle(motion.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            SDLUtil.realMouseX = motion.x();
                            SDLUtil.realMouseY = motion.y();
                            if (SDLUtil.isMouseGrabbed) {
                                SDLUtil.virtualMouseX += motion.xrel();
                                SDLUtil.virtualMouseY += motion.yrel();
                                callback.invoke(windowHandle, SDLUtil.virtualMouseX, SDLUtil.virtualMouseY);
                            } else {
                                callback.invoke(windowHandle, SDLUtil.realMouseX, SDLUtil.realMouseY);
                            }
                        }
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                        var callback = EventCallback.onPressCallback;
                        if (callback != null) {
                            var action = (eventType == SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN) ? GLFW.GLFW_PRESS
                                    : GLFW.GLFW_RELEASE;
                            var button = event.button();
                            var windowHandle = windowIdToHandle(button.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            int sdlButton = button.button();
                            var buttonId = switch (sdlButton) {
                                case SDLMouse.SDL_BUTTON_LEFT -> GLFW.GLFW_MOUSE_BUTTON_LEFT;
                                case SDLMouse.SDL_BUTTON_MIDDLE -> GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
                                case SDLMouse.SDL_BUTTON_RIGHT -> GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                                default -> sdlButton - 1;
                            };
                            var modifier = SDLKeyMapping.getGlfwModifiers(SDLKeyboard.SDL_GetModState());
                            callback.invoke(windowHandle, buttonId, action, modifier);
                        }
                    }

                    case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                        var callback = EventCallback.onScrollCallback;
                        if (callback != null) {
                            var wheel = event.wheel();
                            var windowHandle = windowIdToHandle(wheel.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            callback.invoke(windowHandle, wheel.x(), wheel.y());
                        }
                    }

                    case SDLEvents.SDL_EVENT_DROP_FILE -> {
                        var callback = EventCallback.onDropCallback;
                        if (callback != null) {
                            var drop = event.drop();
                            var windowHandle = windowIdToHandle(drop.windowID());
                            if (windowHandle == 0L) {
                                continue;
                            }
                            var dataAddress = MemoryUtil.memGetAddress(drop.address() + SDL_DropEvent.DATA);
                            if (dataAddress == 0L) {
                                continue;
                            }
                            var buffer = stack.mallocPointer(1);
                            buffer.put(0, dataAddress);
                            callback.invoke(windowHandle, 1, buffer.address());
                        }
                    }
                }
            }
        }
    }
}
