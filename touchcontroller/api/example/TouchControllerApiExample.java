/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.api.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fifthlight.touchcontroller.api.v1.TouchControllerApi;
import top.fifthlight.touchcontroller.api.v1.fabric.TouchControllerApiEntrypoint;

public class TouchControllerApiExample implements TouchControllerApiEntrypoint {
    private static final Logger logger = LoggerFactory.getLogger(TouchControllerApiExample.class);

    @Override
    public void preTouchControllerInitialize(TouchControllerApi api) {
        var gameActionName = api.getTextFactory().literal("Example game action");
        var gameAction = api.registerGameAction("touchcontroller-api-example:example", gameActionName, () -> logger.info("Game action triggered"));

        var playerActionName = api.getTextFactory().literal("Example player action");
        var playerAction = api.registerPlayerAction("touchcontroller-api-example:example", playerActionName, player -> logger.info("Player action triggered"));

        var texture = api.registerWidgetTexture(textureBuilder -> textureBuilder.id("touchcontroller-api-example:taichi")
                .classic("touchcontroller_api_example", "classic_taichi", 18, 18)
                .newStyle("touchcontroller_api_example", "new_taichi", 22, 22));

        api.registerBuiltInWidget(widgetBuilder -> widgetBuilder.id("touchcontroller-api-example:taichi")
                .name(api.getTextFactory().literal("Taichi"))
                .down(api.getWidgetTriggerActionProvider().gameAction(gameAction))
                .release(api.getWidgetTriggerActionProvider().playerAction(playerAction))
                .normalTexture(texture)
                .activeTexture(texture)
                .activeGray()
        );
    }
}
