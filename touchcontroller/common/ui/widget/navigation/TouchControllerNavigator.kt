package top.fifthlight.touchcontroller.common.ui.widget.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import top.fifthlight.combine.screen.DismissHandler

@Composable
fun TouchControllerNavigator(
    screen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
    content: @Composable (Navigator) -> Unit = { CurrentScreen() },
) {
    Navigator(
        screen = screen,
        disposeBehavior = disposeBehavior,
    ) { navigator ->
        val parent = navigator.parent
        DismissHandler(navigator.canPop || parent?.canPop == true) {
            if (navigator.canPop) {
                navigator.pop()
            } else if (parent != null && parent.canPop) {
                parent.pop()
            }
        }
        content(navigator)
    }
}
