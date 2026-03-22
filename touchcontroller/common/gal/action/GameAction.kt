package top.fifthlight.touchcontroller.common.gal.action

import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.assets.Texts

interface GameAction {
    fun openChatScreen()
    fun openGameMenu()
    fun sendMessage(text: Text)
    fun nextPerspective()
    fun takeScreenshot()
    fun takePanorama() {
        val textFactory = TextFactoryFactory.of()
        sendMessage(textFactory.of(Texts.WARNING_TAKE_PANORAMA_UNSUPPORTED))
    }
    var hudHidden: Boolean

    @ExpectFactory
    interface Factory {
        fun of(): GameAction
    }

    companion object : GameAction by GameActionFactory.of()
}
