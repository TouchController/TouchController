/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.platform

import kotlinx.serialization.Serializable

@Serializable
data class PlatformConfig(
    val sdl: SdlPlatformConfig = SdlPlatformConfig(),
)
