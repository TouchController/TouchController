package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.api.v1.action.PlayerActionInstance
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle

class PlayerActionInstanceImpl(
    val hidden: Boolean = false,
    val name: Text,
    val action: (PlayerHandle) -> Unit,
) : PlayerActionInstance {
    operator fun invoke(player: PlayerHandle) = action(player)
}
