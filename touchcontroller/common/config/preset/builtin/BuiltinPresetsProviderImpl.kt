/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.builtin

import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.common.config.layout.controllerLayoutOf
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetsProvider
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.config.preset.topbar.TopBarWidgets
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick

@ActualImpl(BuiltinPresetsProvider::class)
object BuiltinPresetsProviderImpl : BuiltinPresetsProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): BuiltinPresetsProvider = BuiltinPresetsProviderImpl

    override fun generate(key: BuiltinPresetKey): LayoutPreset {
        val textureSet = key.textureSet
        val layers = BuiltinLayers(
            textureSet = textureSet,
            topBarWidgets = key.topBar.widgets ?: TopBarWidgets.defaultAdded.toPersistentList(),
        )
        val sprintButton = when (key.sprintButtonLocation) {
            BuiltinPresetKey.SprintButtonLocation.NONE -> null
            BuiltinPresetKey.SprintButtonLocation.RIGHT_TOP -> layers.sprintRightTopButton
            BuiltinPresetKey.SprintButtonLocation.RIGHT -> layers.sprintRightButton
        }
        val controlStyle = key.controlStyle
        return LayoutPreset(
            name = "Built-in preset",
            controlInfo = PresetControlInfo(
                splitControls = controlStyle is BuiltinPresetKey.ControlStyle.SplitControls,
                disableCrosshair = controlStyle !is BuiltinPresetKey.ControlStyle.SplitControls,
                disableTouchGesture = controlStyle is BuiltinPresetKey.ControlStyle.SplitControls && controlStyle.buttonInteraction,
            ),
            layout = controllerLayoutOf(
                layers.controlLayer,
                layers.interactionLayer.takeIf {
                    controlStyle is BuiltinPresetKey.ControlStyle.SplitControls && controlStyle.buttonInteraction
                },
                layers.normalLayer.getByKey(key) + sprintButton,
                layers.swimmingLayer.getByKey(key),
                layers.flyingLayer.getByKey(key),
                layers.onBoatLayer?.getByKey(key),
                layers.onMinecartLayer?.getByKey(key),
                layers.ridingOnEntityLayer.getByKey(key),
            )
        ).mapWidgets { widget ->
            when (widget) {
                is CustomWidget -> {
                    if ((widget.normalTexture as? ButtonTexture.Fixed)?.texture?.textureItem == BuiltInTextureItems.inventory) {
                        return@mapWidgets widget
                    }

                    fun scaleTexture(scale: Float, texture: ButtonTexture) = when (texture) {
                        is ButtonTexture.Fixed -> texture.copy(scale = texture.scale * scale)
                        else -> texture
                    }

                    fun scaleActiveTexture(scale: Float, texture: ButtonActiveTexture) = when (texture) {
                        is ButtonActiveTexture.Texture -> ButtonActiveTexture.Texture(
                            scaleTexture(
                                scale,
                                texture.texture
                            )
                        )

                        else -> texture
                    }
                    widget.copy(
                        normalTexture = scaleTexture(key.scale, widget.normalTexture),
                        activeTexture = scaleActiveTexture(key.scale, widget.activeTexture),
                    )
                }

                is DPad -> run {
                    val isClassic = textureSet.classic
                    widget.copy(
                        size = widget.size * key.scale,
                        textureSet = textureSet,
                        padding = if (isClassic) 4 else -1,
                        showBackwardButton = !isClassic,
                    )
                }

                is Joystick -> widget.copy(
                    size = widget.size * key.scale,
                    stickSize = widget.stickSize * key.scale,
                    textureSet = textureSet,
                )

                is BoatButton -> widget.copy(
                    size = widget.size * key.scale,
                    textureSet = textureSet,
                )

                else -> widget
            }.cloneBase(
                opacity = key.opacity,
                offset = (widget.offset.toOffset() * key.scale).toIntOffset(),
            )
        }
    }
}
