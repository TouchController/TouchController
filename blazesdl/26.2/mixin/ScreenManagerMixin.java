/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.ScreenManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMonitorCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.fifthlight.blazesdl.SDLScreenManager;

@Mixin(ScreenManager.class)
public abstract class ScreenManagerMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetMonitorCallback(Lorg/lwjgl/glfw/GLFWMonitorCallbackI;)Lorg/lwjgl/glfw/GLFWMonitorCallback;"))
    public GLFWMonitorCallback cancelSetMonitorCallback(GLFWMonitorCallbackI cbfun, Operation<GLFWMonitorCallback> original) {
        if ((Object) this instanceof SDLScreenManager) {
            return null;
        }
        return original.call(cbfun);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetMonitors()Lorg/lwjgl/PointerBuffer;"))
    public PointerBuffer cancelGetMonitors(Operation<PointerBuffer> original) {
        if ((Object) this instanceof SDLScreenManager) {
            return null;
        }
        return original.call();
    }
}
