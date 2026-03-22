package top.fifthlight.combine.node

import androidx.compose.runtime.CompositionLocalMap
import top.fifthlight.combine.input.focus.FocusNode
import top.fifthlight.combine.input.focus.FocusStateListener
import top.fifthlight.combine.input.key.KeyEvent
import top.fifthlight.combine.input.key.KeyEventReceiver
import top.fifthlight.combine.input.pointer.PointerEvent
import top.fifthlight.combine.input.pointer.PointerEventReceiver
import top.fifthlight.combine.input.pointer.PointerEventType
import top.fifthlight.combine.input.text.TextInputReceiver
import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.*
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.withState
import top.fifthlight.data.Offset

private fun interface Renderable {
    fun render(canvas: Canvas, cursorPos: Offset)
}

abstract class WrapperLayoutNode(
    val node: LayoutNode,
) : Measurable, Placeable, Renderable, PointerEventReceiver, FocusStateListener, TextInputReceiver, KeyEventReceiver {
    var parent: WrapperLayoutNode? = null

    val parentPlaceable: Placeable?
        get() = parent ?: node.parent?.initialWrapper

    fun coerceConstraintBounds(constraints: Constraints, parent: Placeable) = object : Placeable by this {
        override var width: Int = parent.width.coerceIn(constraints.minWidth..constraints.maxWidth)
        override var height: Int = parent.height.coerceIn(constraints.minHeight..constraints.maxHeight)
    }

    class Node(node: LayoutNode) : WrapperLayoutNode(node), FocusStateListener, TextInputReceiver, KeyEventReceiver {
        override val parentData: Any? = node.parentData

        override var width: Int = 0
        override var height: Int = 0
        override var x: Int = 0
        override var y: Int = 0
        override val absoluteX: Int
            get() = (parentPlaceable?.absoluteX ?: 0) + x
        override val absoluteY: Int
            get() = (parentPlaceable?.absoluteY ?: 0) + y

        override fun measure(constraints: Constraints): Placeable {
            val result = node.measurePolicy.measure(node.children, constraints)

            width = result.width
            height = result.height
            result.placer.placeChildren()

            return coerceConstraintBounds(constraints, this)
        }

        override fun minIntrinsicWidth(height: Int): Int = node.measurePolicy.minIntrinsicWidth(node.children, height)
        override fun minIntrinsicHeight(width: Int): Int = node.measurePolicy.minIntrinsicHeight(node.children, width)
        override fun maxIntrinsicWidth(height: Int): Int = node.measurePolicy.maxIntrinsicWidth(node.children, height)
        override fun maxIntrinsicHeight(width: Int): Int = node.measurePolicy.maxIntrinsicHeight(node.children, width)

        override fun placeAt(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        override fun render(canvas: Canvas, cursorPos: Offset) {
            canvas.withState {
                canvas.translate(x, y)
                node.renderer.render(canvas, this)
                node.children.forEach { child ->
                    child.render(canvas, cursorPos)
                }
            }
        }

        var pressEventTarget: LayoutNode? = null
        var moveEventTarget: LayoutNode? = null
        override fun onPointerEvent(event: PointerEvent): Boolean {
            val pressTarget = pressEventTarget
            val moveTarget = moveEventTarget
            var haveMoveChildren = false
            fun process(): Boolean {
                if (pressTarget != null) {
                    haveMoveChildren = true
                    return pressTarget.onPointerEvent(event)
                }
                for (child in node.children.asReversed()) {
                    if (event.position !in child) {
                        continue
                    }
                    if (event.type == PointerEventType.Move && !haveMoveChildren) {
                        if (moveTarget == null) {
                            moveEventTarget = child
                            child.onPointerEvent(event.copy(type = PointerEventType.Enter))
                        } else if (moveTarget != child) {
                            moveTarget.onPointerEvent(event.copy(type = PointerEventType.Leave))
                            child.onPointerEvent(event.copy(type = PointerEventType.Enter))
                            moveEventTarget = child
                        }
                        haveMoveChildren = true
                    }
                    if (child.onPointerEvent(event)) {
                        if (event.type == PointerEventType.Press) {
                            pressEventTarget = child
                        }
                        return true
                    }
                }
                return false
            }

            val result = process()
            if (event.type == PointerEventType.Release || event.type == PointerEventType.Cancel) {
                pressEventTarget = null
            }
            if (pressTarget == null) {
                if (event.type == PointerEventType.Move && !haveMoveChildren && moveTarget != null) {
                    moveTarget.onPointerEvent(event.copy(type = PointerEventType.Leave))
                    moveEventTarget = null
                }
                if (event.type == PointerEventType.Leave && moveTarget != null) {
                    moveTarget.onPointerEvent(event.copy(type = PointerEventType.Leave))
                    moveEventTarget = null
                }
            } else if (pressTarget == moveTarget && event.position !in pressTarget) {
                pressTarget.onPointerEvent(event.copy(type = PointerEventType.Leave))
                moveEventTarget = null
            }
            return result
        }

        override fun onFocusStateChanged(focused: Boolean) {}
        override fun onTextInput(string: String) {}
        override fun onKeyEvent(event: KeyEvent) {}
    }

    abstract class PositionWrapper(
        node: LayoutNode,
        val children: WrapperLayoutNode,
    ) : WrapperLayoutNode(node),
        Measurable, Placeable, Renderable {
        override val parentData: Any? = children.parentData

        override val width: Int
            get() = children.width
        override val height: Int
            get() = children.height
        override var x: Int = 0
        override var y: Int = 0
        override val absoluteX: Int
            get() = (parentPlaceable?.absoluteX ?: 0) + x
        override val absoluteY: Int
            get() = (parentPlaceable?.absoluteY ?: 0) + y

        override fun placeAt(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        override fun measure(constraints: Constraints): Placeable {
            children.measure(constraints).placeAt(0, 0)
            return coerceConstraintBounds(constraints, this)
        }

        override fun minIntrinsicWidth(height: Int): Int = children.minIntrinsicWidth(height)
        override fun minIntrinsicHeight(width: Int): Int = children.minIntrinsicHeight(width)
        override fun maxIntrinsicWidth(height: Int): Int = children.maxIntrinsicWidth(height)
        override fun maxIntrinsicHeight(width: Int): Int = children.maxIntrinsicHeight(width)

        override fun render(canvas: Canvas, cursorPos: Offset) {
            canvas.withState {
                canvas.translate(x, y)
                children.run { render(canvas, cursorPos) }
            }
        }
    }
}

class LayoutNode : Measurable, Placeable, Renderable, PointerEventReceiver,
    FocusStateListener, TextInputReceiver, KeyEventReceiver, FocusNode {
    var parent: LayoutNode? = null
    val children = mutableListOf<LayoutNode>()
    var measurePolicy: MeasurePolicy = DefaultMeasurePolicy
    var renderer: NodeRenderer = NodeRenderer.EmptyRenderer
    override var focusable: Boolean = false
    var compositionLocalMap: CompositionLocalMap = CompositionLocalMap.Empty
    override var parentData: Any? = null
    internal val initialWrapper = WrapperLayoutNode.Node(this)
    private var wrappedNode: WrapperLayoutNode = initialWrapper
    var modifier: Modifier = Modifier
        set(value) {
            field = value
            parentData = null
            focusable = false
            wrappedNode = buildWrapperLayoutNode(value)
        }

    private fun buildWrapperLayoutNode(modifier: Modifier): WrapperLayoutNode =
        modifier.foldIn<WrapperLayoutNode>(initialWrapper) { wrapper, node ->
            var currentWrapper = wrapper
            if (node is WrapperModifierNode) {
                val newWrapper = node.wrapperFactory.unsafeCreateWrapper(this, currentWrapper, node)
                currentWrapper.parent = newWrapper
                currentWrapper = newWrapper
            }
            if (node is AttachListenerModifierNode) {
                node.onAttachedToNode(this)
            }
            if (node is ParentDataModifierNode) {
                parentData = node.modifierParentData(parentData)
            }
            currentWrapper
        }

    override fun measure(constraints: Constraints) = wrappedNode.measure(constraints)
    override fun minIntrinsicWidth(height: Int): Int = wrappedNode.minIntrinsicWidth(height)
    override fun minIntrinsicHeight(width: Int): Int = wrappedNode.minIntrinsicHeight(width)
    override fun maxIntrinsicWidth(height: Int): Int = wrappedNode.maxIntrinsicWidth(height)
    override fun maxIntrinsicHeight(width: Int): Int = wrappedNode.maxIntrinsicHeight(width)


    override val width: Int
        get() = wrappedNode.width
    override val height: Int
        get() = wrappedNode.height
    override val x: Int
        get() = wrappedNode.x
    override val y: Int
        get() = wrappedNode.y
    override val absoluteX: Int
        get() = wrappedNode.absoluteX
    override val absoluteY: Int
        get() = wrappedNode.absoluteY

    override fun placeAt(x: Int, y: Int) = wrappedNode.placeAt(x, y)

    override fun render(canvas: Canvas, cursorPos: Offset) = wrappedNode.render(canvas, cursorPos)

    override fun onPointerEvent(event: PointerEvent) = wrappedNode.onPointerEvent(event)

    override fun onFocusStateChanged(focused: Boolean) = wrappedNode.onFocusStateChanged(focused)

    override fun onTextInput(string: String) = wrappedNode.onTextInput(string)

    override fun onKeyEvent(event: KeyEvent) = wrappedNode.onKeyEvent(event)

    internal companion object {
        val DefaultMeasurePolicy = MeasurePolicy { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            MeasureResult(placeables.maxOfOrNull { it.width } ?: 0, placeables.maxOfOrNull { it.height } ?: 0) {
                placeables.forEach { it.placeAt(0, 0) }
            }
        }
    }

    override fun toString(): String = "LayoutNode@${hashCode()} {children: ${children.size}, modifier: $modifier}"
}
