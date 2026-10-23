package top.fifthlight.combine.backend.drawable.resourcelocation.v1_20_1

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.backend.minecraft.render.v1_20_1.TextureImpl
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.core.util.atlas.AtlasTexture
import top.fifthlight.combine.core.util.ninepatch.NinePatchTexture
import top.fifthlight.combine.theme.vanilla.VanillaDrawableProvider
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(VanillaDrawableProvider::class)
object VanillaDrawableProviderImpl : VanillaDrawableProvider {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    private val atlas = AtlasTexture(
        texture = TextureImpl(
            resourceLocation = AbstractWidget.WIDGETS_LOCATION,
            size = IntSize(256, 256),
        ),
    )

    override val buttonDrawableSet = DrawableSet(
        normal = NinePatchTexture(
            texture = atlas.createPart(
                offset = IntOffset(
                    x = 0,
                    y = 66,
                ),
                size = IntSize(200, 20),
                padding = IntPadding(2),
            ),
            scaleArea = IntRect(
                offset = IntOffset(
                    x = 2,
                    y = 2,
                ),
                size = IntSize(
                    width = 196,
                    height = 16,
                ),
            ),
        ),
        focus = NinePatchTexture(
            texture = atlas.createPart(
                offset = IntOffset(
                    x = 0,
                    y = 86,
                ),
                size = IntSize(200, 20),
                padding = IntPadding(2),
            ),
            scaleArea = IntRect(
                offset = IntOffset(
                    x = 2,
                    y = 2,
                ),
                size = IntSize(
                    width = 196,
                    height = 16,
                ),
            ),
        ),
        disabled = NinePatchTexture(
            texture = atlas.createPart(
                offset = IntOffset(
                    x = 0,
                    y = 46,
                ),
                size = IntSize(200, 20),
                padding = IntPadding(2),
            ),
            scaleArea = IntRect(
                offset = IntOffset(
                    x = 2,
                    y = 2,
                ),
                size = IntSize(
                    width = 196,
                    height = 16,
                ),
            ),
        ),
    )
}
