package top.fifthlight.combine.backend.minecraft.screen.v1_21_1

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.backend.minecraft.clipboard.v1_21_1.ClipboardHandlerImpl
import top.fifthlight.combine.backend.minecraft.dispatcher.v1_21_1.GameDispatcher
import top.fifthlight.combine.backend.minecraft.keycode.v1_21_1.mapKeyCode
import top.fifthlight.combine.backend.minecraft.keycode.v1_21_1.mapModifier
import top.fifthlight.combine.backend.minecraft.render.v1_21_1.CanvasImpl
import top.fifthlight.combine.backend.minecraft.sound.v1_21_1.SoundManagerImpl
import top.fifthlight.combine.backend.minecraft.text.v1_21_1.toMinecraft
import top.fifthlight.combine.core.input.key.KeyEvent
import top.fifthlight.combine.core.input.pointer.PointerButton
import top.fifthlight.combine.core.input.pointer.PointerEvent
import top.fifthlight.combine.core.input.pointer.PointerEventType
import top.fifthlight.combine.core.input.pointer.PointerType
import top.fifthlight.combine.core.input.text.LocalClipboard
import top.fifthlight.combine.core.node.CombineOwner
import top.fifthlight.combine.core.screen.*
import top.fifthlight.combine.core.sound.LocalSoundManager
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.combine.core.data.Text as CombineText

class CombineScreen(
    title: Component,
    private val renderBackground: Boolean,
    private val parent: Screen?,
) : Screen(title), CloseHandler {
    private val client: Minecraft = Minecraft.getInstance()
    private var initialized = false
    private val dismissDispatcher = OnDismissRequestDispatcher()
    private val soundManager = SoundManagerImpl(client.soundManager)
    private val owner = CombineOwner(dispatcher = GameDispatcher)

    override fun close() = onClose()

    fun setContent(content: @Composable () -> Unit) {
        owner.setContent {
            CompositionLocalProvider(
                LocalSoundManager provides soundManager,
                LocalClipboard provides ClipboardHandlerImpl,
                LocalScreenFactory provides ScreenFactoryImpl,
                LocalOnDismissRequestDispatcher provides dismissDispatcher,
                LocalCloseHandler provides this,
            ) {
                content()
            }
        }
    }

    override fun init() {
        super.init()
        if (!initialized) {
            initialized = true
            owner.start()
        }
    }

    private fun mapMouseButton(button: Int) = when (button) {
        0 -> PointerButton.Left
        1 -> PointerButton.Middle
        2 -> PointerButton.Right
        else -> null
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mouseButton = mapMouseButton(button) ?: return true
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = mouseButton,
                type = PointerEventType.Press,
            ),
        )
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mouseButton = mapMouseButton(button) ?: return true
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = mouseButton,
                type = PointerEventType.Release,
            ),
        )
        return true
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = null,
                type = PointerEventType.Move,
            ),
        )
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double,
    ): Boolean {
        owner.onPointerEvent(
            PointerEvent(
                id = 0,
                position = Offset(
                    x = mouseX.toFloat(),
                    y = mouseY.toFloat(),
                ),
                pointerType = PointerType.Mouse,
                button = null,
                scrollDelta = Offset(
                    x = horizontalAmount.toFloat(),
                    y = verticalAmount.toFloat(),
                ),
                holdingShift = hasShiftDown(),
                type = PointerEventType.Scroll,
            ),
        )
        return true
    }

    override fun charTyped(char: Char, modifiers: Int): Boolean {
        owner.onTextInput(char.toString())
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (dismissDispatcher.hasEnabledCallbacks()) {
                dismissDispatcher.dispatchOnDismissed()
            } else {
                onClose()
            }
            return true
        }
        owner.onKeyEvent(
            KeyEvent(
                key = mapKeyCode(keyCode),
                modifier = mapModifier(modifiers),
                pressed = true,
            ),
        )
        return true
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        owner.onKeyEvent(
            KeyEvent(
                key = mapKeyCode(keyCode),
                modifier = mapModifier(modifiers),
                pressed = false,
            ),
        )
        return true
    }

    override fun insertText(string: String, override: Boolean) {
        owner.onTextInput(string)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (renderBackground) {
            super.render(guiGraphics, mouseX, mouseY, delta)
        }

        owner.render(
            size = IntSize(width, height),
            cursorPos = Offset(mouseX.toFloat(), mouseY.toFloat()),
            canvas = CanvasImpl(guiGraphics),
        )
    }

    override fun onClose() {
        owner.close()
        client.setScreen(parent)
    }
}

@ActualImpl(ScreenFactory::class)
object ScreenFactoryImpl : ScreenFactory {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    override fun openScreen(
        renderBackground: Boolean,
        title: CombineText,
        content: @Composable () -> Unit,
    ) {
        val client = Minecraft.getInstance()
        val screen = getScreen(client.screen, renderBackground, title, content)
        client.setScreen(screen as Screen)
    }

    override fun getScreen(
        parent: Any?,
        renderBackground: Boolean,
        title: CombineText,
        content: @Composable () -> Unit,
    ): Any {
        val screen = CombineScreen(
            title.toMinecraft(),
            renderBackground = renderBackground,
            parent?.let { it as Screen },
        )
        screen.setContent {
            content()
        }
        return screen
    }
}
