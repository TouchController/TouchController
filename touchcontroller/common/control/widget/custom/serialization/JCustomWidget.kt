package top.fifthlight.touchcontroller.common.control.widget.custom.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.serialization.JButtonTrigger
import top.fifthlight.touchcontroller.common.control.name.serialization.JControllerWidgetName
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.align.serialization.JAlign
import top.fifthlight.touchcontroller.common.serialization.JColor
import top.fifthlight.touchcontroller.common.serialization.JIntOffset
import top.fifthlight.touchcontroller.common.serialization.JUuid

object JCustomWidget : JObj<CustomWidget>() {
    val normalTexture by JFieldMaybe(CustomWidget::normalTexture, JButtonTexture)
    val activeTexture by JFieldMaybe(CustomWidget::activeTexture, JButtonActiveTexture)
    val centerText by JFieldMaybe(CustomWidget::centerText, JString)
    val textColor by JFieldMaybe(CustomWidget::textColor, JColor)
    val swipeTrigger by JFieldMaybe(CustomWidget::swipeTrigger, JBoolean)
    val grabTrigger by JFieldMaybe(CustomWidget::grabTrigger, JBoolean)
    val moveView by JFieldMaybe(CustomWidget::moveView, JBoolean)
    val action by JFieldMaybe(CustomWidget::action, JButtonTrigger)
    val id by str(JUuid, CustomWidget::id)
    val name by JFieldMaybe(CustomWidget::name, JControllerWidgetName)
    val align by JFieldMaybe(CustomWidget::align, JAlign)
    val autoAlign by JFieldMaybe(CustomWidget::autoAlign, JBoolean)
    val offset by JFieldMaybe(CustomWidget::offset, JIntOffset)
    val opacity by JFieldMaybe(CustomWidget::opacity, JFloat)
    val lockMoving by JFieldMaybe(CustomWidget::lockMoving, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = CustomWidget(
        normalTexture = (+normalTexture) ?: ButtonTexture.NinePatch(extraPadding = IntPadding(8)),
        activeTexture = (+activeTexture) ?: ButtonActiveTexture.Same,
        centerText = +centerText,
        textColor = (+textColor) ?: Colors.BLACK,
        swipeTrigger = (+swipeTrigger) ?: false,
        grabTrigger = (+grabTrigger) ?: false,
        moveView = (+moveView) ?: false,
        action = (+action) ?: ButtonTrigger(),
        id = +id,
        name = (+name) ?: CustomWidget().name,
        align = (+align) ?: Align.CENTER_CENTER,
        autoAlign = (+autoAlign) ?: true,
        offset = (+offset) ?: IntOffset.ZERO,
        opacity = (+opacity) ?: 1f,
        lockMoving = (+lockMoving) ?: false,
    )
}
