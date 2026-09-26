/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.builtin.key

import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.builtin.BuiltInWidget

data class BuiltinPresetKey(
    val textureSet: TextureSet = BuiltInTextureSets.classic,
    val controlStyle: ControlStyle = ControlStyle.TouchGesture,
    val moveMethod: MoveMethod = MoveMethod.Dpad(),
    val sprintButtonLocation: SprintButtonLocation = SprintButtonLocation.NONE,
    val opacity: Float = .6f,
    val scale: Float = 1f,
    val topBar: TopBarConfig = TopBarConfig(),
) {
    sealed class ControlStyle {
        data object TouchGesture : ControlStyle()

        data class SplitControls(
            val buttonInteraction: Boolean = true,
        ) : ControlStyle()
    }

    enum class SprintButtonLocation(
        val nameId: Identifier,
    ) {
        NONE(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_NONE),

        RIGHT_TOP(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT_TOP),

        RIGHT(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT),
    }

    sealed class MoveMethod {
        data class Dpad(
            val swapJumpAndSneak: Boolean = false,
        ) : MoveMethod()

        data class Joystick(
            val triggerSprint: Boolean = false,
        ) : MoveMethod()
    }

    data class TopBarConfig(
        val widgets: PersistentList<BuiltInWidget>? = null,
    )

    val preset by lazy {
        BuiltinPresetsProvider.generate(this)
    }

    companion object {
        val DEFAULT = BuiltinPresetKey()
    }
}
