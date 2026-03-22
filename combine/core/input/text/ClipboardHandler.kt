package top.fifthlight.combine.input.text

import androidx.compose.runtime.staticCompositionLocalOf

val LocalClipboard = staticCompositionLocalOf<ClipboardHandler> { EmptyClipboardHandler }

interface ClipboardHandler {
    var text: String
}

private object EmptyClipboardHandler : ClipboardHandler {
    override var text: String
        get() = ""
        set(_) {}
}
