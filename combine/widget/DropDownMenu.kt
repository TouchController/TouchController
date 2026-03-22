package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.animation.animateFloatAsState
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.drawing.clip
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.widget.Popup
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

@JvmName("DropdownMenuListString")
@Composable
fun DropdownMenuScope.DropdownItemList(
    modifier: Modifier = Modifier,
    onItemSelected: (Int) -> Unit = {},
    items: PersistentList<Pair<Text, () -> Unit>>,
) {
    DropdownItemList(
        modifier = modifier,
        items = items,
        textProvider = { it.first },
        onItemSelected = {
            onItemSelected(it)
            items[it].second()
        },
    )
}

@Composable
fun <T> DropdownMenuScope.DropdownItemList(
    modifier: Modifier = Modifier,
    drawableSet: SelectDrawableSet = SelectDrawableSet.current,
    items: List<T>,
    textProvider: (T) -> Text,
    selectedIndex: Int = -1,
    onItemSelected: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .minWidth(contentWidth)
            .then(modifier)
    ) {
        for ((index, item) in items.withIndex()) {
            val text = textProvider(item)
            val interactionSource = remember { MutableInteractionSource() }
            val state by widgetState(interactionSource)
            val drawable = if (index == selectedIndex) {
                drawableSet.itemSelected
            } else {
                drawableSet.itemUnselected
            }.getByState(state)
            Text(
                modifier = Modifier
                    .border(drawable)
                    .clickable(interactionSource) {
                        onItemSelected(index)
                    }
                    .focusable(interactionSource)
                    .fillMaxWidth(),
                color = if (index == selectedIndex) {
                    Colors.BLACK
                } else {
                    Colors.WHITE
                },
                text = text,
            )
        }
    }
}

interface DropdownMenuScope {
    val anchor: IntRect
    val panelBorder: Drawable
    val contentWidth: Int
}

private data class DropdownMenuScopeImpl(
    override val anchor: IntRect,
    override val panelBorder: Drawable,
) : DropdownMenuScope {
    override val contentWidth = anchor.size.width - panelBorder.padding.width
}

@Composable
fun DropDownMenu(
    anchor: IntRect,
    border: Drawable = SelectDrawableSet.current.floatPanel,
    expandProgress: Float = 1f,
    onDismissRequest: () -> Unit,
    content: @Composable DropdownMenuScope.() -> Unit,
) {
    Popup(onDismissRequest = onDismissRequest) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            measurePolicy = { measurables, constraints ->
                val screenSize = IntSize(constraints.maxWidth, constraints.maxHeight)
                val childConstraints = constraints.copy(
                    minWidth = anchor.size.width,
                    minHeight = 0,
                    maxWidth = screenSize.width,
                    maxHeight = screenSize.height,
                )
                val placeables = measurables.map { it.measure(childConstraints) }
                val width = placeables.maxOfOrNull { it.width } ?: 0
                val height = (placeables.maxOfOrNull { it.height } ?: 0)
                val realHeight = (height * expandProgress).toInt()
                val left = if (anchor.left + width < screenSize.width) {
                    anchor.left
                } else {
                    (anchor.right - width).coerceAtLeast(0)
                }
                val top = if (height + anchor.bottom < screenSize.height) {
                    anchor.bottom
                } else {
                    (anchor.top - realHeight).coerceAtLeast(0)
                }
                layout(width, realHeight) {
                    placeables.forEach { it.placeAt(left, top) }
                }
            },
        ) {
            val scope = DropdownMenuScopeImpl(anchor, border)
            Box(
                modifier = Modifier
                    .border(border)
                    .clip(width = 1f, height = expandProgress, anchorOffset = anchor.offset)
            ) {
                content(scope)
            }
        }
    }
}

@Composable
fun DropDownMenu(
    anchor: IntRect,
    border: Drawable = SelectDrawableSet.current.floatPanel,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable DropdownMenuScope.() -> Unit,
) {
    val expandProgress by animateFloatAsState(if (expanded) 1f else 0f)
    if (expandProgress != 0f) {
        DropDownMenu(
            anchor = anchor,
            border = border,
            expandProgress = expandProgress,
            onDismissRequest = onDismissRequest,
            content = content,
        )
    }
}
