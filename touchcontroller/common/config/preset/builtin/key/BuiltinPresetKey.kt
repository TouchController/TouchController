/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.builtin.key

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.builtin.BuiltInWidget

@Serializable
data class BuiltinPresetKey(
    @SerialName("texture_set")
    val textureSet: TextureSet = BuiltInTextureSets.classic,
    @SerialName("control_style")
    val controlStyle: ControlStyle = ControlStyle.TouchGesture,
    @SerialName("move_method")
    val moveMethod: MoveMethod = MoveMethod.Dpad(),
    @SerialName("sprint_button_location")
    val sprintButtonLocation: SprintButtonLocation = SprintButtonLocation.NONE,
    @SerialName("opacity")
    val opacity: Float = .6f,
    @SerialName("scale")
    val scale: Float = 1f,
    @SerialName("top_bar")
    val topBar: TopBarConfig = TopBarConfig(),
) {
    @Serializable
    sealed class ControlStyle {
        @Serializable
        @SerialName("touch_gesture")
        data object TouchGesture : ControlStyle()

        @Serializable
        @SerialName("split_controls")
        data class SplitControls(
            val buttonInteraction: Boolean = true,
        ) : ControlStyle()
    }

    @Serializable
    enum class SprintButtonLocation(
        val nameId: Identifier,
    ) {
        @SerialName("none")
        NONE(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_NONE),

        @SerialName("right_top")
        RIGHT_TOP(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT_TOP),

        @SerialName("right")
        RIGHT(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_RIGHT),
    }

    @Serializable
    sealed class MoveMethod {
        @Serializable
        @SerialName("dpad")
        data class Dpad(
            val swapJumpAndSneak: Boolean = false,
        ) : MoveMethod()

        @Serializable
        @SerialName("joystick")
        data class Joystick(
            val triggerSprint: Boolean = false,
        ) : MoveMethod()
    }

    @Serializable(with = LayoutPresetsSerializer::class)
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

class LayoutPresetsSerializer : KSerializer<BuiltinPresetKey.TopBarConfig> {
    private val widgetSerializer = serializer<BuiltInWidget>()
    private val widgetsSerializer = ListSerializer(widgetSerializer)

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = "top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey.TopBarConfig",
    ) {
        element("widgets", widgetsSerializer.descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: BuiltinPresetKey.TopBarConfig) =
        encoder.encodeStructure(descriptor) {
            value.widgets?.let { encodeSerializableElement(descriptor, 0, widgetsSerializer, it) }
        }

    override fun deserialize(decoder: Decoder): BuiltinPresetKey.TopBarConfig {
        var widgets: PersistentList<BuiltInWidget>? = null
        return decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> widgets =
                        decodeSerializableElement(descriptor, 0, widgetsSerializer, widgets).toPersistentList()

                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }
            BuiltinPresetKey.TopBarConfig(
                widgets = widgets,
            )
        }
    }
}
