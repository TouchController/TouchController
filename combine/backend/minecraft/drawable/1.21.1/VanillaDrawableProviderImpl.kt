package top.fifthlight.combine.backend.drawable.resourcelocation.v1_21_1

import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.backend.minecraft.render.v1_21_1.TextureImpl
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
        normal = TextureImpl.Sprite(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
        focus = TextureImpl.Sprite(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button_highlighted"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
        disabled = TextureImpl.Sprite(
            resourceLocation = ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            size = IntSize(150, 20),
            padding = IntPadding(2),
        ),
    )
}
