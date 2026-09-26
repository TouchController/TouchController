/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.platform.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JFloat
import com.ubertob.kondor.json.JInt
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfig
import top.fifthlight.touchcontroller.common.config.platform.SdlPlatformConfig

object JSdlPlatformConfig : JObj<SdlPlatformConfig>() {
    val vibrationStrength by JFieldMaybe(SdlPlatformConfig::vibrationStrength, JFloat)
    val vibrationLength by JFieldMaybe(SdlPlatformConfig::vibrationLength, JInt)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = SdlPlatformConfig(
        vibrationStrength = (+vibrationStrength) ?: 0.5f,
        vibrationLength = (+vibrationLength) ?: 200,
    )
}

object JPlatformConfig : JObj<PlatformConfig>() {
    val sdl by JFieldMaybe(PlatformConfig::sdl, JSdlPlatformConfig)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = PlatformConfig(
        sdl = (+sdl) ?: SdlPlatformConfig(),
    )
}
