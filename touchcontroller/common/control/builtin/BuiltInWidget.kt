package top.fifthlight.touchcontroller.common.control.builtin

import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import java.util.concurrent.ConcurrentHashMap
import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidget as ApiBuiltInWidget

data class BuiltInWidget(
    private val getter: (TextureSet) -> ControllerWidget,
    val hero: Boolean = false,
    val hidden: ((TextureSet) -> Boolean)? = null,
) : ApiBuiltInWidget {
    private val cache = ConcurrentHashMap<TextureSet, ControllerWidget>()
    operator fun get(textureSet: TextureSet): ControllerWidget {
        return cache.getOrPut(textureSet) {
            getter(textureSet)
        }
    }
}
