/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.api.v1.widget;

import org.jetbrains.annotations.ApiStatus;
import top.fifthlight.touchcontroller.api.v1.text.Text;

@ApiStatus.NonExtendable
public interface TopBarWidgetBuilder {
    TopBarWidgetBuilder id(String id);

    TopBarWidgetBuilder name(Text name);

    TopBarWidgetBuilder normalTexture(WidgetTexture texture);

    TopBarWidgetBuilder down(WidgetTriggerAction action);

    TopBarWidgetBuilder release(WidgetTriggerAction action);

    TopBarWidgetBuilder doubleClick(WidgetTriggerAction action);
}
