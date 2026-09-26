/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import com.ubertob.kondor.json.obj
import top.fifthlight.touchcontroller.common.config.GlobalConfig
import top.fifthlight.touchcontroller.common.config.ItemConfig
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.data.*
import top.fifthlight.touchcontroller.common.config.data.serialization.*
import top.fifthlight.touchcontroller.common.config.item.serialization.JItemList
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfig
import top.fifthlight.touchcontroller.common.config.platform.serialization.JPlatformConfig
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.serialization.JBuiltinPresetKey
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.sealedByName

object JItemConfig : JObj<ItemConfig>() {
    val usableItems by obj(JItemList, ItemConfig::usableItems)
    val showCrosshairItems by obj(JItemList, ItemConfig::showCrosshairItems)
    val usingAimingItems by obj(JItemList, ItemConfig::usingAimingItems)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ItemConfig(
        usableItems = +usableItems,
        showCrosshairItems = +showCrosshairItems,
        usingAimingItems = +usingAimingItems,
    )
}

object JBuiltinPresetConfig : JObj<PresetConfig.BuiltIn>() {
    val key by JFieldMaybe(PresetConfig.BuiltIn::key, JBuiltinPresetKey)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) =
        PresetConfig.BuiltIn(key = (+key) ?: BuiltinPresetKey())
}

object JCustomPresetConfig : JObj<PresetConfig.Custom>() {
    val uuid by JFieldMaybe(PresetConfig.Custom::uuid, JUuid)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = PresetConfig.Custom(uuid = +uuid)
}

val JPresetConfig = sealedByName {
    "builtin" encodes subtype<PresetConfig.BuiltIn>(JBuiltinPresetConfig)
    "custom" encodes subtype<PresetConfig.Custom>(JCustomPresetConfig)
}

object JGlobalConfig : JObj<GlobalConfig>() {
    val status by JFieldMaybe(GlobalConfig::status, JStatusConfig)
    val regular by JFieldMaybe(GlobalConfig::regular, JRegularConfig)
    val control by JFieldMaybe(GlobalConfig::control, JControlConfig)
    val platform by JFieldMaybe(GlobalConfig::platform, JPlatformConfig)
    val touchRing by JFieldMaybe(GlobalConfig::touchRing, JTouchRingConfig)
    val debug by JFieldMaybe(GlobalConfig::debug, JDebugConfig)
    val item by JFieldMaybe(GlobalConfig::item, JItemConfig)
    val preset by JFieldMaybe(GlobalConfig::preset, JPresetConfig)
    val chat by JFieldMaybe(GlobalConfig::chat, JChatConfig)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = GlobalConfig(
        status = (+status) ?: StatusConfig(),
        regular = (+regular) ?: RegularConfig(),
        control = (+control) ?: ControlConfig(),
        platform = (+platform) ?: PlatformConfig(),
        touchRing = (+touchRing) ?: TouchRingConfig(),
        debug = (+debug) ?: DebugConfig(),
        item = (+item) ?: ItemConfig.default(DefaultItemListProvider),
        preset = (+preset) ?: PresetConfig.BuiltIn(),
        chat = (+chat) ?: ChatConfig(),
    )
}
