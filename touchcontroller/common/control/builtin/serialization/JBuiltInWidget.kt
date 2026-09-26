/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.builtin.serialization

import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.util.registry.serialization.registryConverter

val JBuiltInWidget = registryConverter(BuiltinWidgets.registry) { BuiltinWidgets.custom }
