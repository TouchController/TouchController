package top.fifthlight.combine.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.animation.animateFloatAsState
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.layout.Layout
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.drawing.clip
import top.fifthlight.combine.core.modifier.focus.focusable
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.pointer.clickable
import top.fifthlight.combine.core.modifier.pointer.consumePress
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.core.widget.Popup
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.OnOffColorSet
import top.fifthlight.combine.ui.style.OnOffDrawableSet
import top.fifthlight.combine.ui.style.get
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
        onItemSelected = { item, index ->
            onItemSelected(index)
            item.second()
        },
    )
}

@Composable
fun <T> DropdownMenuScope.DropdownItemList(
    modifier: Modifier = Modifier,
    drawableSet: OnOffDrawableSet = LocalTheme.current.drawables.selectItem,
    colorThemeSet: OnOffColorSet = LocalTheme.current.colors.selectItem,
    items: Collection<T>,
    textProvider: (T) -> Text,
    selectedIndex: Int = -1,
    onItemSelected: (T, Int) -> Unit = { _, _ ->},
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
            val drawable = drawableSet[index == selectedIndex].getByState(state)
            val colorTheme = colorThemeSet[index == selectedIndex].getByState(state)
            Text(
                modifier = Modifier
                    .border(drawable)
                    .clickable(interactionSource) {
                        onItemSelected(item, index)
                    }
                    .focusable(interactionSource)
                    .fillMaxWidth(),
                color = colorTheme.foreground,
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
    drawableSet: Drawable = LocalTheme.current.drawables.selectFloatPanel,
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
            val scope = DropdownMenuScopeImpl(anchor, drawableSet)
            Box(
                modifier = Modifier
                    .border(drawableSet)
                    .consumePress()
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
    drawableSet: Drawable = LocalTheme.current.drawables.selectFloatPanel,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable DropdownMenuScope.() -> Unit,
) {
    val expandProgress by animateFloatAsState(if (expanded) 1f else 0f)
    if (expandProgress != 0f) {
        DropDownMenu(
            anchor = anchor,
            drawableSet = drawableSet,
            expandProgress = expandProgress,
            onDismissRequest = onDismissRequest,
            content = content,
        )
    }
}
