package top.fifthlight.touchcontroller.proxy.client.android

import android.os.Vibrator
import android.util.Log
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage

private val TAG = "SimpleVibrationHandler"

/**
 * 一个简单的震动事件处理器。
 *
 * @param service Vibrator 系统服务。
 */
class SimpleVibrationHandler(private val service: Vibrator) : LauncherProxyClient.VibrationHandler {
    override fun vibrate(kind: VibrateMessage.Kind) {
        try {
            @Suppress("DEPRECATION")
            service.vibrate(100)
        } catch (ex: Exception) {
            Log.w(TAG, "Failed to trigger vibration", ex)
        }
    }
}
