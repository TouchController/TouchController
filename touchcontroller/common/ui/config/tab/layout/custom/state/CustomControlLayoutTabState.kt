package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetsContainer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import kotlin.collections.isNotEmpty
import kotlin.uuid.Uuid

sealed class CustomControlLayoutTabState {
    data object Disabled: CustomControlLayoutTabState()

    data class Enabled(
        val allPresets: PresetsContainer,
        val selectedPresetUuid: Uuid? = null,
        val selectedPreset: LayoutPreset? = null,
        val selectedLayer: LayoutLayer? = null,
        val selectedWidget: ControllerWidget? = null,
        val pageState: PageState = PageState(),
    ): CustomControlLayoutTabState() {
        data class UndoStack(
            val undoStack: PersistentList<LayoutPreset>,
            val undoStackIndex: Int,
        ) {
            companion object {
                const val MAX_SIZE = 64
            }

            init {
                require(undoStack.isNotEmpty()) { "Undo stack is empty" }
                require(undoStackIndex in undoStack.indices) { "Undo stack index $undoStackIndex not in stack size: [0, ${undoStack.size})" }
            }

            constructor(preset: LayoutPreset) : this(
                undoStack = persistentListOf(preset),
                undoStackIndex = 0,
            )

            val currentItem
                get() = undoStack[undoStackIndex]

            val haveUndoItem: Boolean
                get() = undoStackIndex > 0

            val haveRedoItem: Boolean
                get() = undoStackIndex < undoStack.size - 1

            operator fun plus(preset: LayoutPreset): UndoStack {
                if (preset == currentItem) {
                    return this
                }

                val newList = if (haveRedoItem) {
                    undoStack.subList(0, undoStackIndex + 1)
                } else {
                    undoStack
                }.toPersistentList() + preset

                val trimmedList = if (newList.size > MAX_SIZE) {
                    newList.subList(newList.size - MAX_SIZE, newList.size).toPersistentList()
                } else {
                    newList
                }

                return UndoStack(
                    undoStack = trimmedList,
                    undoStackIndex = trimmedList.lastIndex,
                )
            }

            fun undo(): UndoStack {
                require(haveUndoItem) { "No undo item available" }
                return copy(undoStackIndex = undoStackIndex - 1)
            }

            fun redo(): UndoStack {
                require(haveRedoItem) { "No redo item available" }
                return copy(undoStackIndex = undoStackIndex + 1)
            }
        }

        data class EditState(
            val presetUuid: Uuid,
            val undoStack: UndoStack,
        )

        data class PageState(
            val selectedLayerIndex: Int = 0,
            val selectedWidgetIndex: Int = -1,
            val moveLocked: Boolean = false,
            val highlight: Boolean = false,
            val showSideBar: Boolean = false,
            val sideBarAutoToggle: Boolean = false,
            val copiedWidget: ControllerWidget? = null,
            val editState: EditState? = null,
        )
    }
}
