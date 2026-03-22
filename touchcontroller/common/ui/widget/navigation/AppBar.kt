package top.fifthlight.touchcontroller.common.ui.widget.navigation

import androidx.compose.runtime.Composable
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxHeight
import top.fifthlight.combine.modifier.placement.height
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.RowScope
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .height(20)
            .border(LocalTouchControllerTheme.current.appBarBackground)
            .then(modifier),
    ) {
        Row(
            modifier = Modifier
                .alignment(Alignment.CenterLeft)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            leading()
        }

        Row(
            modifier = Modifier
                .padding(top = 1)
                .alignment(Alignment.Center)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            title()
        }

        Row(
            modifier = Modifier
                .alignment(Alignment.CenterRight)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            trailing()
        }
    }
}
