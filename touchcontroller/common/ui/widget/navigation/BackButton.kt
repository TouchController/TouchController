package top.fifthlight.touchcontroller.common.ui.widget.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.screen.LocalCloseHandler
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.combine.widget.ui.TextButton
import top.fifthlight.touchcontroller.assets.Texts

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    screenName: Text,
    onClick: (() -> Unit)? = null,
) {
    val closeHandler = LocalCloseHandler.current
    val navigator = LocalNavigator.current
    TextButton(
        modifier = modifier,
        onClick = {
            if (onClick != null) {
                onClick()
            } else {
                if (navigator?.pop() != true) {
                    closeHandler.close()
                }
            }
        }
    ) {
        Text(Text.format(Texts.BACK, screenName.string))
    }
}
