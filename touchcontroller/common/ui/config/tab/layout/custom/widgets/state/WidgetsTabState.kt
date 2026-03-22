package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick

data class WidgetsTabState(
    val listContent: ListContent,
    val tabState: TabState = TabState(),
) {
    data class TabState(
        val listState: ListState = ListState.BUILTIN,
        val dialogState: DialogState = DialogState.Empty,
        val newWidgetParams: NewWidgetParams = NewWidgetParams(),
    )

    data class NewWidgetParams(
        val opacity: Float = .6f,
        val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
    )

    sealed class DialogState {
        data object Empty : DialogState()

        data class ChangeNewWidgetParams(
            val opacity: Float = .6f,
            val textureSet: TextureSet.TextureSetKey = TextureSet.TextureSetKey.CLASSIC,
        ) : DialogState() {
            constructor(params: NewWidgetParams) : this(opacity = params.opacity, textureSet = params.textureSet)

            fun toParams() = NewWidgetParams(
                opacity = opacity,
                textureSet = textureSet,
            )
        }

        data class RenameWidgetPresetItem(
            val index: Int,
            val widget: ControllerWidget,
            val name: ControllerWidget.Name = widget.name,
        ) : DialogState()
    }

    enum class ListState {
        BUILTIN,
        CUSTOM
    }

    sealed class ListContent {
        data class BuiltIn(private val builtIn: BuiltinWidgets) : ListContent() {
            val heroes: PersistentList<ControllerWidget> = persistentListOf<ControllerWidget>(
                builtIn.dpad,
                Joystick(),
            )
            val widgets: PersistentList<ControllerWidget> = persistentListOf<ControllerWidget>(
                builtIn.jump,
                builtIn.sneak,
                builtIn.ascendFlying,
                builtIn.descendFlying,
                builtIn.sprint,
                builtIn.attack,
                builtIn.use,
                BoatButton(),
                builtIn.inventory,
                builtIn.pause,
                builtIn.chat,
                builtIn.hideHud,
                builtIn.switchPerspective,
                builtIn.playerList,
                builtIn.screenshot,
                builtIn.custom,
            )
        }

        data class Custom(
            val widgets: PersistentList<ControllerWidget>,
        ) : ListContent()
    }
}
