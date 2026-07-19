/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.state

import top.fifthlight.touchcontroller.common.config.GlobalConfig

data class ConfigScreenState(
    val originalConfig: GlobalConfig,
    val config: GlobalConfig = originalConfig,
    val developmentWarningDialog: Boolean = false,
)
