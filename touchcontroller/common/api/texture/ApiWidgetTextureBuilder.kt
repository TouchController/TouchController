/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.texture

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.combine.core.paint.TextureFactory
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
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

    private fun createSprite(namespace: String, path: String, width: Int, height: Int) = TextureFactory.createSprite(
        Texture.SpriteTextureData.Native(
            identifier = Identifier.of(namespace, path),
            spriteSize = IntSize(width, height),
        ),
        IntPadding.ZERO,
    )

    override fun classic(namespace: String, path: String, width: Int, height: Int) =
        apply { classic = createSprite(namespace, path, width, height) }

    override fun classicExtended(namespace: String, path: String, width: Int, height: Int) =
        apply { classicExtended = createSprite(namespace, path, width, height) }

    override fun newStyle(namespace: String, path: String, width: Int, height: Int) =
        apply { newStyle = createSprite(namespace, path, width, height) }

    override fun newRegression(namespace: String, path: String, width: Int, height: Int) =
        apply { newStyleExtended = createSprite(namespace, path, width, height) }

    private fun createAtlasSprite(
        namespace: String,
        path: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        atlasWidth: Int,
        atlasHeight: Int
    ) = TextureFactory.createSprite(
        Texture.SpriteTextureData.Atlas(
            identifier = Identifier.of(namespace, path),
            spriteSize = IntSize(width, height),
            atlasOffset = IntOffset(left, top),
            atlasSize = IntSize(atlasWidth, atlasHeight),
        ),
        IntPadding.ZERO,
    )

    override fun classic(
        namespace: String,
        path: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        atlasWidth: Int,
        atlasHeight: Int
    ): WidgetTextureBuilder = apply {
        classic = createAtlasSprite(namespace, path, left, top, width, height, atlasWidth, atlasHeight)
    }

    override fun classicExtended(
        namespace: String,
        path: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        atlasWidth: Int,
        atlasHeight: Int
    ): WidgetTextureBuilder = apply {
        classicExtended = createAtlasSprite(namespace, path, left, top, width, height, atlasWidth, atlasHeight)
    }

    override fun newStyle(
        namespace: String,
        path: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        atlasWidth: Int,
        atlasHeight: Int
    ): WidgetTextureBuilder = apply {
        newStyle = createAtlasSprite(namespace, path, left, top, width, height, atlasWidth, atlasHeight)
    }

    override fun newRegression(
        namespace: String,
        path: String,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        atlasWidth: Int,
        atlasHeight: Int
    ): WidgetTextureBuilder = apply {
        newStyleExtended = createAtlasSprite(namespace, path, left, top, width, height, atlasWidth, atlasHeight)
    }

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
