package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.core.input.interaction.InteractionSource
import top.fifthlight.combine.core.modifier.focus.FocusInteraction
import top.fifthlight.combine.core.modifier.pointer.ClickInteraction
import top.fifthlight.combine.core.modifier.pointer.DragInteraction
import top.fifthlight.combine.ui.style.StyleSet

val LocalWidgetState = staticCompositionLocalOf { WidgetState.NORMAL }

enum class WidgetState(val priority: Int) : Comparable<WidgetState> {
    NORMAL(0),
    FOCUS(1),
    HOVER(2),
    ACTIVE(3);

    operator fun plus(other: WidgetState): WidgetState = if (other.priority > priority) {
        other
    } else {
        this
    }
}

fun <T> StyleSet<T>.getByState(state: WidgetState, enabled: Boolean = true) = if (enabled) {
    when (state) {
        WidgetState.NORMAL -> normal
        WidgetState.HOVER -> hover
        WidgetState.ACTIVE -> active
        WidgetState.FOCUS -> focus
    }
} else {
    this.disabled
}

val ClickInteraction.state
    get() = when (this) {
        ClickInteraction.Empty -> WidgetState.NORMAL
        ClickInteraction.Hover -> WidgetState.HOVER
        ClickInteraction.Active -> WidgetState.ACTIVE
    }

val DragInteraction.state
    get() = when (this) {
        DragInteraction.Empty -> WidgetState.NORMAL
        DragInteraction.Hover -> WidgetState.HOVER
        DragInteraction.Active -> WidgetState.ACTIVE
    }

val FocusInteraction.state
    get() = when (this) {
        FocusInteraction.Blur -> WidgetState.NORMAL
        FocusInteraction.Focus -> WidgetState.FOCUS
    }

@Composable
fun widgetState(interactionSource: InteractionSource): State<WidgetState> {
    val state = remember { mutableStateOf(WidgetState.NORMAL) }
    var lastClickInteraction by remember { mutableStateOf<ClickInteraction>(ClickInteraction.Empty) }
    var lastDragInteraction by remember { mutableStateOf<DragInteraction>(DragInteraction.Empty) }
    var lastFocusInteraction by remember { mutableStateOf<FocusInteraction>(FocusInteraction.Blur) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is ClickInteraction) {
                lastClickInteraction = it
            }
            if (it is DragInteraction) {
                lastDragInteraction = it
            }
            if (it is FocusInteraction) {
                lastFocusInteraction = it
            }
            val clickState = lastClickInteraction.state
            val dragState = lastDragInteraction.state
            val focusState = lastFocusInteraction.state
            state.value = clickState + dragState + focusState
        }
    }
    return state
}
