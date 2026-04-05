/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.builtin

import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.data.IntOffset
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.BuiltinLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.condition.RidingEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.layerConditionsOf
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.control.*
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets.dpadDismountButton
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets.dpadJumpButton
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets.dpadJumpButtonWithoutLocking
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets.dpadSneakButton
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButtonSide
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider
import top.fifthlight.touchcontroller.common.layout.align.Align
import java.util.concurrent.ConcurrentHashMap

@ConsistentCopyVisibility
data class BuiltinLayers private constructor(
    private val textureSet: TextureSet,
) {
    data class Layers(
        val name: String,
        val conditions: LayerConditions,
        val dpadNormal: PersistentList<ControllerWidget>,
        val dpadSwap: PersistentList<ControllerWidget> = dpadNormal,
        val dpadNormalButtonInteract: PersistentList<ControllerWidget> = dpadNormal,
        val dpadSwapButtonInteract: PersistentList<ControllerWidget> = dpadSwap,
        val joystick: PersistentList<ControllerWidget> = dpadNormal,
    ) {
        fun getByKey(key: BuiltinPresetKey) = LayoutLayer(
            name = name,
            conditions = conditions,
            widgets = when (val moveMethod = key.moveMethod) {
                is BuiltinPresetKey.MoveMethod.Dpad -> {
                    val controlStyle = key.controlStyle
                    if (controlStyle is BuiltinPresetKey.ControlStyle.SplitControls && controlStyle.buttonInteraction) {
                        if (moveMethod.swapJumpAndSneak) {
                            dpadSwapButtonInteract
                        } else {
                            dpadNormalButtonInteract
                        }
                    } else {
                        if (moveMethod.swapJumpAndSneak) {
                            dpadSwap
                        } else {
                            dpadNormal
                        }
                    }
                }

                is BuiltinPresetKey.MoveMethod.Joystick -> joystick.map {
                    if (it is Joystick) {
                        it.copy(triggerSprint = moveMethod.triggerSprint)
                    } else {
                        it
                    }
                }.toPersistentList()
            }
        )
    }

    val controlLayer = LayoutLayer(
        name = "Control",
        conditions = layerConditionsOf(),
        widgets = persistentListOf(
            BuiltinWidgets.pause[textureSet].cloneBase(
                align = Align.CENTER_TOP,
                offset = IntOffset(-9, 0),
            ),
            BuiltinWidgets.chat[textureSet].cloneBase(
                align = Align.CENTER_TOP,
                offset = IntOffset(9, 0),
            ),
            BuiltinWidgets.inventory[textureSet],
        )
    )

    val vanillaChatControlLayer = LayoutLayer(
        name = "Control",
        conditions = layerConditionsOf(),
        widgets = persistentListOf(
            BuiltinWidgets.pause[textureSet].cloneBase(
                align = Align.CENTER_TOP,
                offset = IntOffset(-9, 0),
            ),
            BuiltinWidgets.vanillaChat[textureSet].cloneBase(
                align = Align.CENTER_TOP,
                offset = IntOffset(9, 0),
            ),
            BuiltinWidgets.inventory[textureSet],
        )
    )

    val interactionLayer = LayoutLayer(
        name = "Interaction",
        conditions = LayerConditions(),
        widgets = persistentListOf(
            BuiltinWidgets.attack[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(86, 70),
            ),
            BuiltinWidgets.use[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 37),
            ),
        )
    )

    val sprintRightButton = BuiltinWidgets.sprint[textureSet].cloneBase(
        align = Align.RIGHT_BOTTOM,
        offset = IntOffset(42, 131),
    )

    val sprintRightTopButton = BuiltinWidgets.sprint[textureSet].cloneBase(
        align = Align.RIGHT_TOP,
        offset = IntOffset(42, 44),
    )

    val normalLayer = Layers(
        name = "Normal",
        conditions = layerConditionsOf(
            BuiltinLayerConditionKey(BuiltinLayerCondition.SWIMMING) to LayerConditions.Value.NEVER,
            BuiltinLayerConditionKey(BuiltinLayerCondition.FLYING) to LayerConditions.Value.NEVER,
            BuiltinLayerConditionKey(BuiltinLayerCondition.RIDING) to LayerConditions.Value.NEVER,
        ),
        dpadNormal = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
                extraButton = textureSet.dpadSneakButton(),
            ),
            BuiltinWidgets.jump[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
        ),
        dpadSwap = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
                extraButton = textureSet.dpadJumpButton(),
            ),
            BuiltinWidgets.sneak[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
        ),
        dpadNormalButtonInteract = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.jump[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.sneak[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
        dpadSwapButtonInteract = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.jump[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.sneak[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
        joystick = persistentListOf(
            Joystick(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(29, 32),
            ),
            BuiltinWidgets.jump[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.sneak[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
    )

    val swimmingLayer = Layers(
        name = "Swimming",
        conditions = layerConditionsOf(
            BuiltinLayerConditionKey(BuiltinLayerCondition.RIDING) to LayerConditions.Value.NEVER,
            BuiltinLayerConditionKey(BuiltinLayerCondition.FLYING) to LayerConditions.Value.NEVER,
            BuiltinLayerConditionKey(BuiltinLayerCondition.SWIMMING) to LayerConditions.Value.WANT,
            BuiltinLayerConditionKey(BuiltinLayerCondition.UNDERWATER) to LayerConditions.Value.WANT,
        ),
        dpadNormal = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.ascendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
            BuiltinWidgets.descendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 18),
            )
        ),
        dpadNormalButtonInteract = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.ascendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.descendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            )
        ),
        joystick = persistentListOf(
            Joystick(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(29, 32),
            ),
            BuiltinWidgets.ascendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.descendSwimming[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            )
        ),
    )

    val flyingLayer = Layers(
        name = "Flying",
        conditions = layerConditionsOf(
            BuiltinLayerConditionKey(BuiltinLayerCondition.FLYING) to LayerConditions.Value.REQUIRE,
            BuiltinLayerConditionKey(BuiltinLayerCondition.RIDING) to LayerConditions.Value.NEVER,
        ),
        dpadNormal = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.ascendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
            BuiltinWidgets.descendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 18),
            )
        ),
        dpadNormalButtonInteract = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.ascendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 164),
            ),
            BuiltinWidgets.descendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            )
        ),
        joystick = persistentListOf(
            Joystick(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(29, 32),
            ),
            BuiltinWidgets.ascendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.descendFlying[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
    )

    val onMinecartLayer = EntityTypeProvider.minecart?.let { minecart ->
        Layers(
            name = "On minecart",
            conditions = layerConditionsOf(
                RidingEntityLayerConditionKey(minecart) to LayerConditions.Value.REQUIRE,
            ),
            dpadNormal = persistentListOf(
                BuiltinWidgets.forward[textureSet].cloneBase(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(59, 111),
                ),
                BuiltinWidgets.dismount[textureSet].cloneBase(
                    align = Align.RIGHT_BOTTOM,
                    offset = IntOffset(42, 68),
                ),
            ),
            dpadSwap = persistentListOf(
                BuiltinWidgets.forward[textureSet].cloneBase(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(59, 111)
                ),
                BuiltinWidgets.dismount[textureSet].cloneBase(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(59, 63),
                ),
            ),
            joystick = persistentListOf(
                Joystick(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(29, 32),
                ),
                BuiltinWidgets.dismount[textureSet].cloneBase(
                    align = Align.RIGHT_BOTTOM,
                    offset = IntOffset(22, 165),
                ),
            ),
        )
    }

    // TODO: use tag to filter
    val onBoatLayer = EntityTypeProvider.boats.takeIf { it.isNotEmpty() }?.let {
        Layers(
            name = "On boat",
            conditions = LayerConditions(it.map {
                LayerConditions.Item(
                    key = RidingEntityLayerConditionKey(it),
                    value = LayerConditions.Value.WANT,
                )
            }.toPersistentList()),
            dpadNormal = persistentListOf(
                BoatButton(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(16, 16),
                    side = BoatButtonSide.LEFT,
                ),
                BoatButton(
                    align = Align.RIGHT_BOTTOM,
                    offset = IntOffset(16, 16),
                    side = BoatButtonSide.RIGHT,
                ),
                BuiltinWidgets.dismount[textureSet].cloneBase(
                    align = Align.CENTER_BOTTOM,
                    offset = IntOffset(0, 24),
                ),
            ),
            joystick = persistentListOf(
                Joystick(
                    align = Align.LEFT_BOTTOM,
                    offset = IntOffset(29, 32),
                ),
                BuiltinWidgets.dismount[textureSet].cloneBase(
                    align = Align.RIGHT_BOTTOM,
                    offset = IntOffset(22, 165),
                ),
            ),
        )
    }

    private inline fun <reified T> T?.itemToPersistentList() =
        this?.let { persistentListOf(this) } ?: persistentListOf<T>()

    val ridingOnEntityLayer = Layers(
        name = "Riding on entity",
        conditions = LayerConditions(
            persistentListOf(
                LayerConditions.Item(
                    key = BuiltinLayerConditionKey(BuiltinLayerCondition.RIDING),
                    value = LayerConditions.Value.REQUIRE,
                )
            ) + (EntityTypeProvider.boats + EntityTypeProvider.minecart.itemToPersistentList()).map {
                LayerConditions.Item(
                    key = RidingEntityLayerConditionKey(it),
                    value = LayerConditions.Value.NEVER,
                )
            }),
        dpadNormal = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
                extraButton = textureSet.dpadDismountButton(),
            ),
            BuiltinWidgets.jumpHorse[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
        ),
        dpadSwap = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
                extraButton = textureSet.dpadJumpButtonWithoutLocking(),
            ),
            BuiltinWidgets.dismount[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(42, 68),
            ),
        ),
        dpadNormalButtonInteract = persistentListOf(
            DPad.create(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(12, 16),
            ),
            BuiltinWidgets.jumpHorse[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.dismount[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
        joystick = persistentListOf(
            Joystick(
                align = Align.LEFT_BOTTOM,
                offset = IntOffset(29, 32),
            ),
            BuiltinWidgets.jumpHorse[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 165),
            ),
            BuiltinWidgets.dismount[textureSet].cloneBase(
                align = Align.RIGHT_BOTTOM,
                offset = IntOffset(22, 102),
            ),
        ),
    )

    companion object {
        private val cache = ConcurrentHashMap<TextureSet, BuiltinLayers>()
        operator fun get(textureSet: TextureSet): BuiltinLayers =
            cache.computeIfAbsent(textureSet, ::BuiltinLayers)
    }
}
