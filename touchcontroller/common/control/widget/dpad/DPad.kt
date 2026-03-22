package top.fifthlight.touchcontroller.common.control.widget.dpad

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.BooleanProperty
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.FloatProperty
import top.fifthlight.touchcontroller.common.control.IntProperty
import top.fifthlight.touchcontroller.common.control.TextureSetProperty
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.data.DPadDirection
import top.fifthlight.touchcontroller.common.layout.widget.Texture
import top.fifthlight.touchcontroller.common.layout.widget.button.Button
import top.fifthlight.touchcontroller.common.layout.widget.button.SwipeButton
import top.fifthlight.touchcontroller.common.layout.withAlign
import top.fifthlight.touchcontroller.common.state.PointerState
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.math.round
import kotlin.uuid.Uuid

@Serializable
@SerialName("dpad")
@ConsistentCopyVisibility
data class DPad private constructor(
    val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
    val size: Float = 2f,
    val padding: Int = 4,
    val extraButton: DPadExtraButton,
    val showBackwardButton: Boolean = false,
    val idForward: Uuid = fastRandomUuid(),
    val idBackward: Uuid = fastRandomUuid(),
    val idLeft: Uuid = fastRandomUuid(),
    val idRight: Uuid = fastRandomUuid(),
    val idLeftForward: Uuid = fastRandomUuid(),
    val idRightForward: Uuid = fastRandomUuid(),
    val idLeftBackward: Uuid = fastRandomUuid(),
    val idRightBackward: Uuid = fastRandomUuid(),
    val idExtraButton: Uuid = fastRandomUuid(),
    override val id: Uuid = fastRandomUuid(),
    override val name: Name = Name.Translatable(Texts.WIDGET_DPAD_NAME),
    override val align: Align = Align.LEFT_BOTTOM,
    override val offset: IntOffset = IntOffset.ZERO,
    override val opacity: Float = 1f,
    override val lockMoving: Boolean = false,
) : ControllerWidget() {
    companion object {
        private val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()

        @Suppress("UNCHECKED_CAST")
        private val _properties = properties + persistentListOf<Property<DPad, *>>(
            TextureSetProperty(
                getValue = { it.textureSet },
                setValue = { config, value -> config.copy(textureSet = value) },
                name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_TEXTURE_SET),
            ),
            BooleanProperty(
                getValue = { it.showBackwardButton },
                setValue = { config, value -> config.copy(showBackwardButton = value) },
                name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_SHOW_BACKWARD_BUTTON),
            ),
            FloatProperty(
                getValue = { it.size },
                setValue = { config, value -> config.copy(size = value) },
                range = .5f..4f,
                messageFormatter = {
                    Text.format(
                        Texts.WIDGET_DPAD_PROPERTY_SIZE,
                        round(it * 100f).toString()
                    )
                },
            ),
            IntProperty(
                getValue = { it.padding },
                setValue = { config, value -> config.copy(padding = value) },
                range = -1..16,
                messageFormatter = { Text.format(Texts.WIDGET_DPAD_PROPERTY_PADDING, it) }
            ),
            DPadExtraButtonProperty(
                getValue = { it.extraButton },
                setValue = { config, value -> config.copy(extraButton = value) },
            ),
        ) as PersistentList<Property<ControllerWidget, *>>

        val default: DPad by lazy {
            DPad(
                extraButton = DPadExtraButton.Normal(
                    trigger = ButtonTrigger(
                        doubleClick = ButtonTrigger.DoubleClickTrigger(
                            action = WidgetTriggerAction.Key.Lock(
                                keyBinding = keyBindingHandler.mapDefaultType(DefaultKeyBindingType.SNEAK),
                            ),
                        ),
                    ),
                    info = DPadExtraButton.ButtonInfo(
                        texture = TextureCoordinate(
                            textureSet = TextureSet.TextureSetKey.CLASSIC,
                            textureItem = TextureSet.TextureKey.Sneak,
                        ),
                        activeTexture = DPadExtraButton.ActiveTexture.Gray,
                    )
                ),
            )
        }

        fun create(
            textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
            size: Float = 2f,
            padding: Int = if (textureSet == TextureSet.TextureSetKey.CLASSIC || textureSet == TextureSet.TextureSetKey.CLASSIC_EXTENSION) 4 else -1,
            extraButton: DPadExtraButton = DPadExtraButton.None,
            name: Name = Name.Translatable(Texts.WIDGET_DPAD_NAME),
            align: Align = Align.LEFT_BOTTOM,
            offset: IntOffset = IntOffset.ZERO,
            opacity: Float = 1f,
            lockMoving: Boolean = false,
        ) = default.copy(
            textureSet = textureSet,
            size = size,
            padding = padding,
            extraButton = extraButton,
            name = name,
            align = align,
            offset = offset,
            opacity = opacity,
            lockMoving = lockMoving,
        )
    }

    override val properties
        get() = _properties

    fun buttonSize() = IntSize((textureSet.textureSet.up.size.width * size).toInt())

    val paddingSize = (padding * size).toInt()

    override fun size(): IntSize = buttonSize() * 3 + paddingSize * 2

    override fun cloneBase(
        id: Uuid,
        name: Name,
        align: Align,
        offset: IntOffset,
        opacity: Float,
        lockMoving: Boolean,
    ) = copy(
        id = id,
        name = name,
        align = align,
        offset = offset,
        opacity = opacity,
        lockMoving = lockMoving,
    )

    override fun newId(): ControllerWidget = copy(
        idForward = fastRandomUuid(),
        idBackward = fastRandomUuid(),
        idLeft = fastRandomUuid(),
        idRight = fastRandomUuid(),
        idLeftForward = fastRandomUuid(),
        idRightForward = fastRandomUuid(),
        idLeftBackward = fastRandomUuid(),
        idRightBackward = fastRandomUuid(),
        idExtraButton = fastRandomUuid(),
        id = fastRandomUuid(),
    )

    fun copy(
        textureSet: TextureSet.TextureSetKey = this.textureSet,
        size: Float = this.size,
        padding: Int = this.padding,
        extraButton: DPadExtraButton = this.extraButton,
        showBackwardButton: Boolean = this.showBackwardButton,
        name: Name = this.name,
        align: Align = this.align,
        offset: IntOffset = this.offset,
        opacity: Float = this.opacity,
        lockMoving: Boolean = this.lockMoving,
    ) = copy(
        textureSet = textureSet,
        size = size,
        padding = padding,
        extraButton = extraButton,
        showBackwardButton = showBackwardButton,
        id = id,
        name = name,
        align = align,
        offset = offset,
        opacity = opacity,
        lockMoving = lockMoving
    )

    override fun layout(context: Context): Unit = with(context) {
        val config = this@DPad
        val buttonSize = buttonSize()
        val smallDisplaySize = IntSize((textureSet.textureSet.upLeft.size.width * config.size).toInt())
        val smallButtonOffset = IntOffset(paddingSize)
        val classicTrigger =
            textureSet == TextureSet.TextureSetKey.CLASSIC || textureSet == TextureSet.TextureSetKey.CLASSIC_EXTENSION
        val texturePadding = if (padding == -1 && extraButton.info?.size == 22) {
            1
        } else {
            0
        }
        val smallTexturePadding = if (padding == -1) {
            1
        } else {
            0
        }

        val forward = withRect(
            x = buttonSize.width + paddingSize,
            y = 0,
            width = buttonSize.width,
            height = buttonSize.height + paddingSize,
        ) {
            val padding = IntPadding(bottom = texturePadding)
            SwipeButton(id = config.idForward) { clicked ->
                withAlign(
                    align = Align.CENTER_TOP,
                    size = buttonSize,
                ) {
                    when (Pair(classicTrigger, clicked)) {
                        Pair(true, false) -> Texture(
                            texture = config.textureSet.textureSet.up,
                        )

                        Pair(true, true) -> Texture(
                            texture = config.textureSet.textureSet.up,
                            tint = Color(0xFFAAAAAAu)
                        )

                        Pair(false, false) -> Texture(
                            texture = config.textureSet.textureSet.up,
                            padding = padding,
                        )

                        Pair(false, true) -> Texture(
                            texture = config.textureSet.textureSet.upActive,
                            padding = padding,
                        )
                    }
                }
            }.clicked
        }

        val backward = withRect(
            x = buttonSize.width + paddingSize,
            y = buttonSize.height * 2 + paddingSize,
            width = buttonSize.width,
            height = buttonSize.height + paddingSize,
        ) {
            SwipeButton(id = config.idBackward) { clicked ->
                withAlign(
                    align = Align.CENTER_BOTTOM,
                    size = buttonSize,
                ) {
                    val padding = IntPadding(top = texturePadding)
                    when (Pair(classicTrigger, clicked)) {
                        Pair(true, false) -> Texture(texture = config.textureSet.textureSet.down)
                        Pair(true, true) -> Texture(
                            texture = config.textureSet.textureSet.down,
                            tint = Color(0xFFAAAAAAu)
                        )

                        Pair(false, false) -> Texture(
                            texture = config.textureSet.textureSet.down,
                            padding = padding
                        )

                        Pair(false, true) -> Texture(
                            texture = config.textureSet.textureSet.downActive,
                            padding = padding
                        )
                    }
                }
            }.clicked
        }

        val left = withRect(
            x = 0,
            y = buttonSize.height + paddingSize,
            width = buttonSize.width + paddingSize,
            height = buttonSize.height
        ) {
            SwipeButton(id = config.idLeft) { clicked ->
                withAlign(
                    align = Align.LEFT_CENTER,
                    size = buttonSize,
                ) {
                    val padding = IntPadding(right = texturePadding)
                    when (Pair(classicTrigger, clicked)) {
                        Pair(true, false) -> Texture(texture = config.textureSet.textureSet.left)
                        Pair(true, true) -> Texture(
                            texture = config.textureSet.textureSet.left,
                            tint = Color(0xFFAAAAAAu)
                        )

                        Pair(false, false) -> Texture(
                            texture = config.textureSet.textureSet.left,
                            padding = padding
                        )

                        Pair(false, true) -> Texture(
                            texture = config.textureSet.textureSet.leftActive,
                            padding = padding
                        )
                    }
                }
            }.clicked
        }

        val right = withRect(
            x = buttonSize.width * 2 + paddingSize,
            y = buttonSize.height + paddingSize,
            width = buttonSize.width + paddingSize,
            height = buttonSize.height
        ) {
            SwipeButton(id = config.idRight) { clicked ->
                withAlign(
                    align = Align.RIGHT_CENTER,
                    size = buttonSize,
                ) {
                    val padding = IntPadding(left = texturePadding)
                    when (Pair(classicTrigger, clicked)) {
                        Pair(true, false) -> Texture(texture = config.textureSet.textureSet.right)
                        Pair(true, true) -> Texture(
                            texture = config.textureSet.textureSet.right,
                            tint = Color(0xFFAAAAAAu)
                        )

                        Pair(false, false) -> Texture(
                            texture = config.textureSet.textureSet.right,
                            padding = padding
                        )

                        Pair(false, true) -> Texture(
                            texture = config.textureSet.textureSet.rightActive,
                            padding = padding
                        )
                    }
                }
            }.clicked
        }

        val showLeftForward = forward || left || status.dpadLeftForwardShown
        val showRightForward = forward || right || status.dpadRightForwardShown
        val showLeftBackward = showBackwardButton && (backward || left || status.dpadLeftBackwardShown)
        val showRightBackward = showBackwardButton && (backward || right || status.dpadRightBackwardShown)

        val leftForward = if (showLeftForward) {
            withRect(
                x = 0,
                y = 0,
                width = buttonSize.width + paddingSize,
                height = buttonSize.height + paddingSize,
            ) {
                val padding = IntPadding(
                    right = smallTexturePadding,
                    bottom = smallTexturePadding,
                )
                SwipeButton(id = config.idLeftForward) { clicked ->
                    withAlign(
                        align = Align.RIGHT_BOTTOM,
                        size = smallDisplaySize,
                        offset = smallButtonOffset,
                    ) {
                        when (Pair(classicTrigger, clicked)) {
                            Pair(true, false) -> Texture(texture = config.textureSet.textureSet.upLeft)
                            Pair(true, true) -> Texture(
                                texture = config.textureSet.textureSet.upLeft,
                                tint = Color(0xFFAAAAAAu)
                            )

                            Pair(false, false) -> Texture(
                                texture = config.textureSet.textureSet.upLeft,
                                padding = padding,
                            )

                            Pair(false, true) -> Texture(
                                texture = config.textureSet.textureSet.upLeftActive,
                                padding = padding,
                            )
                        }
                    }
                }.clicked
            }
        } else {
            false
        }

        val rightForward = if (showRightForward) {
            withRect(
                x = buttonSize.width * 2 + paddingSize,
                y = 0,
                width = buttonSize.width + paddingSize,
                height = buttonSize.height + paddingSize,
            ) {
                val padding = IntPadding(
                    left = smallTexturePadding,
                    bottom = smallTexturePadding,
                )
                SwipeButton(id = config.idRightForward) { clicked ->
                    withAlign(
                        align = Align.LEFT_BOTTOM,
                        size = smallDisplaySize,
                        offset = smallButtonOffset,
                    ) {
                        when (Pair(classicTrigger, clicked)) {
                            Pair(true, false) -> Texture(texture = config.textureSet.textureSet.upRight)
                            Pair(true, true) -> Texture(
                                texture = config.textureSet.textureSet.upRight,
                                tint = Color(0xFFAAAAAAu)
                            )

                            Pair(false, false) -> Texture(
                                texture = config.textureSet.textureSet.upRight,
                                padding = padding,
                            )

                            Pair(false, true) -> Texture(
                                texture = config.textureSet.textureSet.upRightActive,
                                padding = padding,
                            )
                        }
                    }
                }.clicked
            }
        } else {
            false
        }

        val leftBackward = if (showLeftBackward) {
            withRect(
                x = 0,
                y = buttonSize.height * 2 + paddingSize,
                width = buttonSize.width + paddingSize,
                height = buttonSize.height + paddingSize,
            ) {
                val padding = IntPadding(
                    right = smallTexturePadding,
                    top = smallTexturePadding,
                )
                SwipeButton(id = config.idLeftBackward) { clicked ->
                    withAlign(
                        align = Align.RIGHT_TOP,
                        size = smallDisplaySize,
                        offset = smallButtonOffset,
                    ) {
                        when (Pair(classicTrigger, clicked)) {
                            Pair(true, false) -> Texture(texture = config.textureSet.textureSet.downLeft)
                            Pair(true, true) -> Texture(
                                texture = config.textureSet.textureSet.downLeft,
                                tint = Color(0xFFAAAAAAu)
                            )

                            Pair(false, false) -> Texture(
                                texture = config.textureSet.textureSet.downLeft,
                                padding = padding,
                            )

                            Pair(false, true) -> Texture(
                                texture = config.textureSet.textureSet.downLeftActive,
                                padding = padding,
                            )
                        }
                    }
                }.clicked
            }
        } else {
            false
        }

        val rightBackward = if (showRightBackward) {
            withRect(
                x = buttonSize.width * 2 + paddingSize,
                y = buttonSize.width * 2 + paddingSize,
                width = buttonSize.width + paddingSize,
                height = buttonSize.height + paddingSize,
            ) {
                val padding = IntPadding(
                    left = smallTexturePadding,
                    top = smallTexturePadding,
                )
                SwipeButton(id = config.idRightBackward) { clicked ->
                    withAlign(
                        align = Align.LEFT_TOP,
                        size = smallDisplaySize,
                        offset = smallButtonOffset,
                    ) {
                        when (Pair(classicTrigger, clicked)) {
                            Pair(true, false) -> Texture(texture = config.textureSet.textureSet.downRight)
                            Pair(true, true) -> Texture(
                                texture = config.textureSet.textureSet.downRight,
                                tint = Color(0xFFAAAAAAu)
                            )

                            Pair(false, false) -> Texture(
                                texture = config.textureSet.textureSet.downRight,
                                padding = padding,
                            )

                            Pair(false, true) -> Texture(
                                texture = config.textureSet.textureSet.downRightActive,
                                padding = padding,
                            )
                        }
                    }
                }.clicked
            }
        } else {
            false
        }

        status.dpadLeftForwardShown = left || forward || leftForward
        status.dpadRightForwardShown = right || forward || rightForward
        status.dpadLeftBackwardShown = showBackwardButton && (left || backward || leftBackward)
        status.dpadRightBackwardShown = showBackwardButton && (right || backward || rightBackward)

        when (Pair(forward || leftForward || rightForward, backward || leftBackward || rightBackward)) {
            Pair(true, false) -> result.forward = 1f
            Pair(false, true) -> result.forward = -1f
        }

        when (Pair(left || leftForward || leftBackward, right || rightForward || rightBackward)) {
            Pair(true, false) -> result.left = 1f
            Pair(false, true) -> result.left = -1f
        }

        when {
            forward -> DPadDirection.UP
            backward -> DPadDirection.DOWN
            left -> DPadDirection.LEFT
            right -> DPadDirection.RIGHT
            else -> null
        }?.let { status.lastDpadDirection = it }

        fun Context.buttonContent(
            info: DPadExtraButton.ButtonInfo,
            clicked: Boolean,
        ) {
            withAlign(
                align = Align.CENTER_CENTER,
                size = IntSize((info.size * this@DPad.size).toInt()),
            ) {
                if (clicked) {
                    when (val activeTexture = info.activeTexture) {
                        DPadExtraButton.ActiveTexture.Gray -> {
                            Texture(
                                texture = info.texture.texture,
                                tint = Color(0xFFAAAAAAu),
                            )
                        }

                        DPadExtraButton.ActiveTexture.Same -> Texture(texture = info.texture.texture)
                        is DPadExtraButton.ActiveTexture.Texture -> Texture(texture = activeTexture.texture.texture)
                    }
                } else {
                    Texture(texture = info.texture.texture)
                }
            }
        }

        withRect(
            x = buttonSize.width + paddingSize,
            y = buttonSize.height + paddingSize,
            width = buttonSize.width,
            height = buttonSize.height
        ) {
            var hasPointer = false
            for (pointer in getPointersInRect(size)) {
                val state = (pointer.state as? PointerState.Button) ?: continue
                if (state.id == config.idForward || state.id == config.idBackward || state.id == config.idLeft || state.id == config.idRight) {
                    hasPointer = true
                }
            }

            when (val extraButton = config.extraButton) {
                DPadExtraButton.None -> {}

                is DPadExtraButton.Normal -> {
                    context.status.doubleClickCounter.update(context.timer.renderTick, idExtraButton)
                    val result = Button(config.idExtraButton) { clicked ->
                        buttonContent(
                            info = extraButton.info,
                            clicked = clicked || extraButton.trigger.hasLock(idExtraButton),
                        )
                    }
                    extraButton.trigger.refresh(context, idExtraButton)
                    extraButton.trigger.trigger(this, result, idExtraButton)
                }

                is DPadExtraButton.Swipe -> {
                    context.status.doubleClickCounter.update(context.timer.renderTick, idExtraButton)
                    val buttonResult = SwipeButton(config.idExtraButton) { clicked ->
                        buttonContent(
                            info = extraButton.info,
                            clicked = clicked,
                        )
                    }
                    extraButton.trigger.refresh(context, idExtraButton)
                    extraButton.trigger.trigger(this, buttonResult, idExtraButton)
                    if (buttonResult.clicked) {
                        when (status.lastDpadDirection) {
                            DPadDirection.UP -> result.forward = 1f
                            DPadDirection.DOWN -> result.forward = -1f
                            DPadDirection.LEFT -> result.left = 1f
                            DPadDirection.RIGHT -> result.left = -1f
                            null -> {}
                        }
                    }
                }

                is DPadExtraButton.SwipeLocking -> {
                    context.status.doubleClickCounter.update(context.timer.renderTick, idExtraButton)
                    val (_, clicked, _) = SwipeButton(config.idExtraButton) { clicked ->
                        buttonContent(
                            info = extraButton.info,
                            clicked = clicked,
                        )
                    }
                    extraButton.press?.let { keyBindingHandler.getState(it) }?.let { state ->
                        if (clicked) {
                            if (!hasPointer) {
                                state.clicked = true
                            } else if (!status.dpadJumping) {
                                state.clicked = true
                                status.dpadJumping = true
                            }
                            if (hasPointer) {
                                when (status.lastDpadDirection) {
                                    DPadDirection.UP -> result.forward = 1f
                                    DPadDirection.DOWN -> result.forward = -1f
                                    DPadDirection.LEFT -> result.left = 1f
                                    DPadDirection.RIGHT -> result.left = -1f
                                    null -> {}
                                }
                            }
                        } else {
                            status.dpadJumping = false
                        }
                    }
                }
            }
        }
    }
}
