package top.fifthlight.blazerod.render.api.event

object RenderEvents {
    @JvmField
    val FLIP_FRAME = Event<FlipFrame> { callbacks ->
        FlipFrame {
            for (callback in callbacks) {
                callback.onFrameFlipped()
            }
        }
    }

    fun interface FlipFrame {
        fun onFrameFlipped()
    }

    @JvmField
    val INITIALIZE_DEVICE = Event<InitializeDevice> { callbacks ->
        InitializeDevice {
            for (callback in callbacks) {
                callback.onDeviceInitialized()
            }
        }
    }

    fun interface InitializeDevice {
        fun onDeviceInitialized()
    }
}
