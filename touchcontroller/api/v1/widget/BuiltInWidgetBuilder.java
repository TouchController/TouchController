/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.api.v1.widget;

import org.jetbrains.annotations.ApiStatus;
import top.fifthlight.touchcontroller.api.v1.text.Text;

@ApiStatus.NonExtendable
public interface BuiltInWidgetBuilder {
    BuiltInWidgetBuilder id(String id);

    BuiltInWidgetBuilder name(Text name);

    BuiltInWidgetBuilder normalTexture(WidgetTexture texture);

    BuiltInWidgetBuilder activeTexture(WidgetTexture texture);

    BuiltInWidgetBuilder activeGray();

    BuiltInWidgetBuilder down(WidgetTriggerAction action);

    BuiltInWidgetBuilder press(String keyMapping);

    BuiltInWidgetBuilder release(WidgetTriggerAction action);

    BuiltInWidgetBuilder doubleClick(WidgetTriggerAction action);
}
