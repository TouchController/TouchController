/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data


data class StatusConfig(
    val status: Status = Status.ENABLED,
) {
    enum class Status {
        DISABLED,
        ONLY_VIEW_CLICK_TO_INTERACT,
        ONLY_VIEW_AIMING_BY_CROSSHAIR,
        ENABLED,
    }
}
