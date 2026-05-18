package top.fifthlight.touchcontroller.common.api.texture

import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.combine.core.paint.TextureFactory
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTextureBuilder
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureItems

class ApiWidgetTextureBuilder : WidgetTextureBuilder {
    private var id: String? = null
    private var classic: Texture? = null
    private var classicExtended: Texture? = null
    private var newStyle: Texture? = null
    private var newStyleExtended: Texture? = null

    override fun id(id: String) =
        apply { this.id = id }

    override fun classic(namespace: String, path: String, width: Int, height: Int) =
        apply { classic = TextureFactory.create(namespace, path, width, height, IntPadding.ZERO) }

    override fun classicExtended(namespace: String, path: String, width: Int, height: Int) =
        apply { classicExtended = TextureFactory.create(namespace, path, width, height, IntPadding.ZERO) }

    override fun newStyle(namespace: String, path: String, width: Int, height: Int) =
        apply { newStyle = TextureFactory.create(namespace, path, width, height, IntPadding.ZERO) }

    override fun newRegression(namespace: String, path: String, width: Int, height: Int) =
        apply { newStyleExtended = TextureFactory.create(namespace, path, width, height, IntPadding.ZERO) }

    fun build(): ApiWidgetTexture {
        val id = checkNotNull(id) { "id cannot be null" }
        val default = classic ?: newStyle ?: classicExtended ?: newStyleExtended
        ?: throw IllegalStateException("No texture is set")
        val classic = classic ?: classicExtended ?: default
        val classicExtended = classicExtended ?: classic
        val newStyle = newStyle ?: newStyleExtended ?: default
        val newRegression = newStyleExtended ?: newStyle
        return ApiWidgetTexture(
            textureItem = TextureItems.register(
                id = id,
                name = id,
                get = {
                    when (it) {
                        BuiltInTextureSets.classic -> classic
                        BuiltInTextureSets.classicExtension -> classicExtended
                        BuiltInTextureSets.new -> newStyle
                        BuiltInTextureSets.newRegression -> newRegression
                        else -> default
                    }
                },
            )
        )
    }
}
