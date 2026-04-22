package top.fifthlight.combine.backend.minecraft.drawable.v26_2

import net.minecraft.resources.Identifier
import top.fifthlight.combine.backend.minecraft.render.v26_2.TextureImpl
import top.fifthlight.combine.theme.vanilla.VanillaDrawableProvider
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(VanillaDrawableProvider::class)
object VanillaDrawableProviderImpl : VanillaDrawableProvider {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    override val buttonDrawableSet = DrawableSet(
        normal = TextureImpl(
            identifier = Identifier.withDefaultNamespace("widget/button"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
        focus = TextureImpl(
            identifier = Identifier.withDefaultNamespace("widget/button_highlighted"),
            size = IntSize(150, 20),
            padding = top.fifthlight.data.IntPadding(2),
        ),
        disabled = TextureImpl(
            identifier = Identifier.withDefaultNamespace("widget/button_disabled"),
            size = IntSize(150, 20),
            padding = top.fifthlight.data.IntPadding(2),
        ),
    )
}
