/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.api.v1.widget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface WidgetTextureBuilder {
    WidgetTextureBuilder id(String id);

    WidgetTextureBuilder classic(String namespace, String path, int width, int height);

    WidgetTextureBuilder classicExtended(String namespace, String path, int width, int height);

    WidgetTextureBuilder newStyle(String namespace, String path, int width, int height);

    WidgetTextureBuilder newRegression(String namespace, String path, int width, int height);

    /**
     * Attach a texture to the <code>Classic</code> texture set by an atlas texture location.
     * <p>
     * Calling this will override {@link WidgetTextureBuilder#classic(String, String, int, int)}. The best
     * practice is only call the atlas version on <1.21.1.
     *
     * @param namespace Namespace of the atlas texture
     * @param path Path of the atlas texture
     * @param left X coordinate of this texture in the atlas
     * @param top Y coordinate of this texture int the atlas
     * @param width Width of this texture
     * @param height Height of this texture
     * @param atlasWidth Width of this atlas texture
     * @param atlasHeight Height of this atlas texture
     * @since API Level 2
     * @return The builder itself
     */
    WidgetTextureBuilder classic(String namespace, String path, int left, int top, int width, int height, int atlasWidth, int atlasHeight);

    /**
     * Attach a texture to the <code>Classic Extended</code> texture set by an atlas texture location.
     * <p>
     * Calling this will override {@link WidgetTextureBuilder#classicExtended(String, String, int, int)}. The best
     * practice is only call the atlas version on <1.21.1.
     *
     * @param namespace Namespace of the atlas texture
     * @param path Path of the atlas texture
     * @param left X coordinate of this texture in the atlas
     * @param top Y coordinate of this texture int the atlas
     * @param width Width of this texture
     * @param height Height of this texture
     * @param atlasWidth Width of this atlas texture
     * @param atlasHeight Height of this atlas texture
     * @since API Level 2
     * @return The builder itself
     */
    WidgetTextureBuilder classicExtended(String namespace, String path, int left, int top, int width, int height, int atlasWidth, int atlasHeight);

    /**
     * Attach a texture to the <code>New Style</code> texture set by an atlas texture location.
     * <p>
     * Calling this will override {@link WidgetTextureBuilder#newStyle(String, String, int, int)}. The best
     * practice is only call the atlas version on <1.21.1.
     *
     * @param namespace Namespace of the atlas texture
     * @param path Path of the atlas texture
     * @param left X coordinate of this texture in the atlas
     * @param top Y coordinate of this texture int the atlas
     * @param width Width of this texture
     * @param height Height of this texture
     * @param atlasWidth Width of this atlas texture
     * @param atlasHeight Height of this atlas texture
     * @since API Level 2
     * @return The builder itself
     */
    WidgetTextureBuilder newStyle(String namespace, String path, int left, int top, int width, int height, int atlasWidth, int atlasHeight);

    /**
     * Attach a texture to the <code>New Regression</code> texture set by an atlas texture location.
     * <p>
     * Calling this will override {@link WidgetTextureBuilder#newRegression(String, String, int, int)}. The best
     * practice is only call the atlas version on <1.21.1.
     *
     * @param namespace Namespace of the atlas texture
     * @param path Path of the atlas texture
     * @param left X coordinate of this texture in the atlas
     * @param top Y coordinate of this texture int the atlas
     * @param width Width of this texture
     * @param height Height of this texture
     * @param atlasWidth Width of this atlas texture
     * @param atlasHeight Height of this atlas texture
     * @since API Level 2
     * @return The builder itself
     */
    WidgetTextureBuilder newRegression(String namespace, String path, int left, int top, int width, int height, int atlasWidth, int atlasHeight);
}
