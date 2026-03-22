package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.model.ViewModel
import top.fifthlight.armorstand.util.BlockableEventLoopDispatcher

abstract class ArmorStandScreen<T: ArmorStandScreen<T, M>, M: ViewModel>(
    parent: Screen? = null,
    viewModelFactory: (CoroutineScope) -> M,
    title: Component,
): BaseArmorStandScreen<T>(parent, title) {
    val scope = CoroutineScope(BlockableEventLoopDispatcher(Minecraft.getInstance()) + SupervisorJob())
    val viewModel = viewModelFactory(scope)

    override fun onClose() {
        super.onClose()
        scope.cancel()
    }
}
