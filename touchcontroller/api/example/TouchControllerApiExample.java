/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.api.example;

import top.fifthlight.touchcontroller.api.v1.TouchControllerApi;
import top.fifthlight.touchcontroller.api.v1.fabric.TouchControllerApiEntrypoint;

public class TouchControllerApiExample implements TouchControllerApiEntrypoint {
    @Override
    public void preTouchControllerInitialize(TouchControllerApi api) {
        System.out.println(api.getTextFactory().literal("Hello, world!"));
    }
}
