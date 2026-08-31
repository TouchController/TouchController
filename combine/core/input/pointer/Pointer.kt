package top.fifthlight.combine.core.input.pointer

import androidx.compose.runtime.Immutable
import top.fifthlight.data.Offset

@JvmInline
value class PointerButton private constructor(internal val value: Int) {
    companion object {
        val Unknown = PointerButton(0)
        val Left = PointerButton(1)
        val Middle = PointerButton(2)
        val Right = PointerButton(3)
    }

    override fun toString(): String =
        when (this) {
            Left -> "Left"
            Middle -> "Middle"
            Right -> "Right"
            else -> "Unknown"
        }
}

@JvmInline
value class PointerEventType private constructor(internal val value: Int) {
    companion object {
        val Unknown = PointerEventType(0)
        val Press = PointerEventType(1)
        val Release = PointerEventType(2)
        val Move = PointerEventType(3)
        val Scroll = PointerEventType(4)
        val Enter = PointerEventType(5)
        val Leave = PointerEventType(6)
        val Cancel = PointerEventType(7)
    }

    override fun toString(): String =
        when (this) {
            Press -> "Press"
            Release -> "Release"
            Move -> "Move"
            Scroll -> "Scroll"
            Enter -> "Enter"
            Leave -> "Leave"
            Cancel -> "Cancel"
            else -> "Unknown"
        }
}

@JvmInline
value class PointerType private constructor(internal val value: Int) {
    companion object {
        val Unknown = PointerType(0)
        val Mouse = PointerType(1)
        val Touch = PointerType(2)
    }

    override fun toString(): String =
        when (this) {
            Mouse -> "Mouse"
            Touch -> "Touch"
            else -> "Unknown"
        }
}

@Immutable
data class PointerEvent(
    val id: Int,
    val position: Offset,
    val pointerType: PointerType = PointerType.Mouse,
    val button: PointerButton? = null,
    val scrollDelta: Offset = Offset.ZERO,
    val holdingShift: Boolean = false,
    val type: PointerEventType,
) {
    val effectiveDelta: Offset
        get() = if (holdingShift) {
            Offset(
                x = scrollDelta.y,
                y = scrollDelta.x,
            )
        } else {
            scrollDelta
        }
}
