package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.input.focus.LocalFocusManager
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.focus.FocusInteraction
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.pointer.consumePress
import top.fifthlight.combine.modifier.pointer.draggable
import top.fifthlight.combine.paint.*
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.Spacer
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.Offset

@Composable
private fun HsvPicker(
    modifier: Modifier = Modifier,
    handleDrawable: Drawable = LocalTheme.current.drawables.colorPickerHandleChoice,
    baseColor: Color,
    value: HsvColor,
    onValueChanged: (HsvColor) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Canvas(
        modifier = Modifier
            .draggable(pointerIcon = PointerIcon.ResizeAll) { _, absolute ->
                focusManager.requestBlur()
                val s = (absolute.x / size.width).coerceIn(0f..1f)
                val v = 1 - (absolute.y / size.height).coerceIn(0f..1f)
                onValueChanged(value.copy(s = s, v = v))
            }
            .then(modifier),
    ) { canvas, node ->
        canvas.fillGradientRect(
            size = node.size.toSize(),
            leftTopColor = Colors.WHITE,
            rightTopColor = baseColor,
        )
        canvas.fillGradientRect(
            size = node.size.toSize(),
            leftTopColor = Color(0x00000000u),
            leftBottomColor = Color(0xFF000000u),
            rightTopColor = Color(0x00000000u),
            rightBottomColor = Color(0xFF000000u)
        )
        val offset = Offset(
            x = value.s * node.width,
            y = (1 - value.v) * node.height,
        )
        canvas.withTranslate(offset - (handleDrawable.size / 2).toSize()) {
            handleDrawable.draw(
                canvas = canvas,
                dstRect = IntRect(
                    offset = IntOffset.ZERO,
                    size = handleDrawable.size,
                ),
            )
        }
    }
}

@Composable
private fun OverlaySlider(
    modifier: Modifier = Modifier,
    overlayDrawable: Drawable,
    handleDrawable: DrawableSet,
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChanged: (Float) -> Unit,
) {
    val sliderDrawable = SliderDrawableSet.current
    val overlaySliderDrawableSet = remember(sliderDrawable, overlayDrawable) {
        val paddedDrawable = PaddingDrawable(
            drawable = overlayDrawable,
            extraPadding = IntPadding(1, 4),
        )

        fun Drawable.overlay() = LayeredDrawable(persistentListOf(this, paddedDrawable))
        sliderDrawable.copy(
            activeTrack = sliderDrawable.activeTrack.copy(
                normal = sliderDrawable.activeTrack.normal.overlay(),
                focus = sliderDrawable.activeTrack.focus.overlay(),
                hover = sliderDrawable.activeTrack.hover.overlay(),
                active = sliderDrawable.activeTrack.active.overlay(),
                disabled = sliderDrawable.activeTrack.disabled.overlay(),
            ),
            inactiveTrack = null,
            handle = handleDrawable,
        )
    }
    Slider(
        modifier = modifier,
        drawableSet = overlaySliderDrawableSet,
        range = range,
        value = value,
        onValueChanged = onValueChanged,
    )
}

private val hueSliderTrackDrawable = GradientDrawable(
    (0 until 7).map {
        val hueStart = 60f * it
        HsvColor(hueStart, 1.0f, 1.0f).toColor()
    }.toPersistentList()
)

private val presetColors = persistentListOf(
    Color(0xFFFED83Du),
    Color(0xFF80C71Fu),
    Color(0xFF3AB3DAu),
    Color(0xFFF38BAAu),
    Color(0xFFF9801Du),
    Color(0xFF5E7C16u),
    Color(0xFF169C9Cu),
    Color(0xFFC74EBDu),
    Color(0xFFB02E26u),
    Color(0xFF835432u),
    Color(0xFF3C44AAu),
    Color(0xFF8932B8u),
    Color(0xFFF9FFFEu),
    Color(0xFF9D9D97u),
    Color(0xFF474F52u),
    Color(0xFF1D1D21u),
)

@Composable
fun ColorPickerPanel(
    modifier: Modifier = Modifier,
    value: Color,
    onValueChanged: (Color) -> Unit,
) {
    val prevColor = remember { value }
    Column(
        modifier = Modifier
            .consumePress()
            .padding(4)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(4),
    ) {
        var rgbColor by remember { mutableStateOf(value) }
        var hsvColor by remember { mutableStateOf(value.toHsv()) }
        val baseColor = remember(hsvColor) { hsvColor.copy(v = 1f, s = 1f, a = 255).toColor() }

        fun changeRgbColor(color: Color) {
            rgbColor = color
            hsvColor = color.toHsv()
            onValueChanged(color)
        }

        fun changeHsvColor(color: HsvColor) {
            rgbColor = color.toColor()
            hsvColor = color
            onValueChanged(color.toColor())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            HsvPicker(
                modifier = Modifier
                    .border(1, Colors.BLACK)
                    .size(72),
                baseColor = baseColor,
                value = hsvColor,
                onValueChanged = ::changeHsvColor,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OverlaySlider(
                        modifier = Modifier.weight(1f),
                        overlayDrawable = hueSliderTrackDrawable,
                        handleDrawable = LocalTheme.current.drawables.colorPickerSliderHandleHollow,
                        range = 0f..360f,
                        value = hsvColor.h,
                        onValueChanged = { changeHsvColor(hsvColor.copy(h = it)) },
                    )
                    Text("H")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OverlaySlider(
                        modifier = Modifier.weight(1f),
                        overlayDrawable = remember(baseColor) {
                            GradientDrawable(persistentListOf(Colors.WHITE, baseColor))
                        },
                        handleDrawable = LocalTheme.current.drawables.colorPickerSliderHandleHollow,
                        range = 0f..1f,
                        value = hsvColor.s,
                        onValueChanged = { changeHsvColor(hsvColor.copy(s = it)) },
                    )
                    Text("S")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OverlaySlider(
                        modifier = Modifier.weight(1f),
                        overlayDrawable = remember(baseColor) {
                            GradientDrawable(persistentListOf(Colors.BLACK, baseColor))
                        },
                        handleDrawable = LocalTheme.current.drawables.colorPickerSliderHandleHollow,
                        range = 0f..1f,
                        value = hsvColor.v,
                        onValueChanged = { changeHsvColor(hsvColor.copy(v = it)) },
                    )
                    Text("V")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IntSlider(
                        modifier = Modifier.weight(1f),
                        range = 0..255,
                        value = hsvColor.a,
                        onValueChanged = { changeHsvColor(hsvColor.copy(a = it)) },
                    )
                    Text("A")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8),
        ) {
            Column(
                modifier = Modifier.width(72),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                var text by remember { mutableStateOf(rgbColor.toString()) }
                val interactionSource = remember { MutableInteractionSource() }
                var focused by remember { mutableStateOf(false) }
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        when (it) {
                            FocusInteraction.Blur -> focused = false
                            FocusInteraction.Focus -> focused = true
                        }
                    }
                }
                if (focused) {
                    LaunchedEffect(text) {
                        val newValue = Color.parse(text) ?: return@LaunchedEffect
                        if (newValue == rgbColor) {
                            return@LaunchedEffect
                        }
                        changeRgbColor(newValue)
                    }
                } else {
                    LaunchedEffect(rgbColor) {
                        text = value.toString()
                    }
                }
                EditText(
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChanged = { text = it }
                )

                Row(
                    modifier = Modifier.border(size = 1, color = Colors.BLACK)
                ) {
                    Spacer(
                        Modifier
                            .background(prevColor)
                            .weight(1f)
                            .height(12)
                    )
                    Spacer(
                        Modifier
                            .background(rgbColor)
                            .weight(1f)
                            .height(12)
                    )
                }
            }

            Column(Modifier.border(1, Colors.BLACK)) {
                for (row in presetColors.chunked(4)) {
                    Row {
                        for (color in row) {
                            Spacer(
                                Modifier
                                    .background(color)
                                    .size(24, 12)
                                    .clickable {
                                        changeRgbColor(color)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    value: Color,
    onValueChanged: (Color) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Select(
        modifier = modifier,
        expanded = expanded,
        onExpandedChanged = { expanded = it },
        dropDownContent = {
            ColorPickerPanel(
                modifier = Modifier.width(192),
                value = value,
                onValueChanged = onValueChanged,
            )
        }
    ) {
        Spacer(Modifier.background(value).size(18, 9).padding(right = 4))
        SelectIcon(expanded = expanded)
    }
}
