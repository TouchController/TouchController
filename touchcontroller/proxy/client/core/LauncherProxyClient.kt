package top.fifthlight.touchcontroller.proxy.client

import top.fifthlight.touchcontroller.proxy.message.*
import top.fifthlight.touchcontroller.proxy.message.input.TextInputState
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 与 TouchController 交互的接口。
 *
 * 创建好 LauncherProxyClient 对象后，必须要调用 run() 方法才会工作。
 *
 * @param transport 使用到的消息运输层
 */
class LauncherProxyClient @JvmOverloads constructor(
    transport: MessageTransport,
    capabilities: Set<PlatformCapability> = setOf(),
) : AutoCloseable {
    private val running = AtomicBoolean(false)
    private val messageClient = LauncherProxyMessageClient(transport)
    private var capabilities = HashSet(capabilities)

    /**
     * 处理震动事件的处理器。
     */
    interface VibrationHandler {
        /**
         * 当震动事件发生时会调用这个方法。
         *
         * @param kind 震动事件的种类。
         */
        fun vibrate(kind: VibrateMessage.Kind)
    }

    /**
     * 处理震动事件的处理器，为 null 时则会忽略震动事件。
     */
    var vibrationHandler: VibrationHandler? = null

    /**
     * 处理输入事件的处理器。
     */
    interface InputHandler {
        fun updateState(textInputState: TextInputState?)
        fun updateCursor(cursorRect: FloatRect?)
        fun updateArea(inputAreaRect: FloatRect?)
    }

    /**
     * 处理输入事件的处理器。声明 TEXT_STATUS 能力时必须提供，否则会忽略输入事件。
     */
    var inputHandler: InputHandler? = null

    /**
     * 处理键盘显示事件的处理器。
     */
    interface KeyboardShowHandler {
        fun showKeyboard()
        fun hideKeyboard()
    }

    /**
     * 处理键盘显示事件的处理器，声明 KEYBOARD_SHOW 能力时必须提供，否则会忽略键盘显示事件。
     */
    var keyboardShowHandler: KeyboardShowHandler? = null

    /**
     * 开始处理消息，会新建线程用于处理，不会阻塞当前线程。
     */
    fun run() {
        if (running.compareAndSet(false, true)) {
            messageClient.run()
            Thread {
                while (true) {
                    val message = messageClient.receive() ?: break
                    when (message) {
                        is VibrateMessage -> vibrationHandler?.vibrate(message.kind)
                        is InitializeMessage -> sendCapabilities()
                        is InputStatusMessage -> {
                            inputHandler?.updateState(message.status)
                        }

                        is InputCursorMessage -> {
                            inputHandler?.updateCursor(message.cursorRect)
                        }

                        is InputAreaMessage -> {
                            inputHandler?.updateArea(message.inputAreaRect)
                        }

                        is KeyboardShowMessage -> {
                            if (message.show) {
                                keyboardShowHandler?.showKeyboard()
                            } else {
                                keyboardShowHandler?.hideKeyboard()
                            }
                        }

                        else -> {
                            // Ignore
                        }
                    }
                }
            }.start()
        }
    }

    /**
     * 添加或者移动一个指针。
     *
     * @param index 指针的序号。新指针的序号必须从一开始单调递增。
     * @param x 指针的 X 坐标，范围为相对游戏区域的 [0, 1]。
     * @param y 指针的 Y 坐标，范围为相对游戏区域的 [0, 1]。
     */
    fun addPointer(index: Int, x: Float, y: Float) {
        messageClient.send(AddPointerMessage(index, x, y))
    }

    /**
     * 移除一个指针。
     *
     * @param index 指针的序号。
     */
    fun removePointer(index: Int) {
        messageClient.send(RemovePointerMessage(index = index))
    }

    /**
     * 清除所有的指针。
     */
    fun clearPointer() {
        messageClient.send(ClearPointerMessage)
    }

    /**
     * 移动游戏视角。
     *
     * @param screenBased 是否以屏幕坐标系为基准。如果为 true，则以屏幕移动的比例来计算旋转角度（即与 addPointer 的坐标一致），否则按角度计算。
     * @param deltaPitch 以屏幕移动的比例或者角度为单位的俯仰角增量。也就是鼠标沿 Y 轴移动的方向。
     * @param deltaYaw 以屏幕移动的比例或者角度为单位的偏航角增量。也就是鼠标沿 X 轴移动的方向。
     */
    fun moveView(screenBased: Boolean, deltaPitch: Float, deltaYaw: Float) {
        messageClient.send(MoveViewMessage(screenBased, deltaPitch, deltaYaw))
    }

    private fun sendCapabilities() {
        for (capability in capabilities) {
            messageClient.send(CapabilityMessage(capability.id, true))
        }
    }

    private fun updateCapabilities(newCapabilities: Set<PlatformCapability>) {
        for (item in capabilities) {
            if (item !in newCapabilities) {
                messageClient.send(CapabilityMessage(item.id, false))
            }
        }
        for (item in newCapabilities) {
            if (item !in capabilities) {
                messageClient.send(CapabilityMessage(item.id, true))
            }
        }
        capabilities = HashSet(newCapabilities)
    }

    /**
     * 更新输入状态到游戏内。
     *
     * @param newState 新的输入状态。
     */
    fun updateTextInputState(newState: TextInputState) {
        if (PlatformCapability.TEXT_STATUS !in capabilities) {
            return
        }
        messageClient.send(InputStatusMessage(newState))
    }

    override fun close() {
        messageClient.close()
    }
}
