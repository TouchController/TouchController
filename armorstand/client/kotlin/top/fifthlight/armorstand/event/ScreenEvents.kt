package top.fifthlight.armorstand.event

import net.minecraft.client.gui.screens.Screen
import top.fifthlight.blazerod.render.api.event.Event

object ScreenEvents {
    @JvmField
    val UNLOCK_CURSOR = Event<UnlockMouse> { callbacks ->
        UnlockMouse { screen ->
            var unlock = true
            for (callback in callbacks) {
                unlock = callback.onMouseUnlocked(screen) && unlock
            }
            unlock
        }
    }

    fun interface UnlockMouse {
        fun onMouseUnlocked(screen: Screen): Boolean
    }

    @JvmField
    val MOVE_VIEW = Event<MoveView> { callbacks ->
        MoveView { screen ->
            var moved = true
            for (callback in callbacks) {
                moved = callback.onViewMoved(screen) && moved
            }
            moved
        }
    }

    fun interface MoveView {
        fun onViewMoved(screen: Screen?): Boolean
    }
}
