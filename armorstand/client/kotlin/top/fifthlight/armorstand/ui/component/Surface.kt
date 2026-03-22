package top.fifthlight.armorstand.ui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractSelectionList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.ResourceLocation

fun interface Surface {
    fun draw(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int)

    operator fun plus(other: Surface) = combine(this, other)

    companion object {
        val empty = Surface { graphics, x, y, width, height -> }

        fun combine(vararg surfaces: Surface) = Surface { graphics, x, y, width, height ->
            for (surface in surfaces) {
                surface.draw(graphics, x, y, width, height)
            }
        }

        fun color(color: Int) = color(color.toUInt())
        fun color(color: UInt) = Surface { graphics, x, y, width, height ->
            graphics.fill(x, y, x + width, y + height, color.toInt())
        }

        fun border(color: Int) = border(color.toUInt())
        fun border(color: UInt) = Surface { graphics, x, y, width, height ->
            graphics.renderOutline(x, y, width, height, color.toInt())
        }

        fun padding(padding: Insets, surface: Surface) = Surface { graphics, x, y, width, height ->
            surface.draw(
                graphics = graphics,
                x = x + padding.left,
                y = y + padding.top,
                width = (width - padding.left - padding.right).coerceAtLeast(0),
                height = (height - padding.top - padding.bottom).coerceAtLeast(0),
            )
        }

        fun texture(
            identifier: ResourceLocation,
            textureWidth: Int = 256,
            textureHeight: Int = 256,
            u: Float = 0f,
            v: Float = 0f,
            regionWidth: Int = textureWidth,
            regionHeight: Int = textureHeight,
        ) = Surface { graphics, x, y, width, height ->
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                identifier,
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight,
            )
        }

        fun headerSeparator() = if (Minecraft.getInstance().level != null) {
            Screen.INWORLD_HEADER_SEPARATOR
        } else {
            Screen.HEADER_SEPARATOR
        }.let { texture ->
            Surface { graphics, x, y, width, height ->
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    width,
                    2,
                    32,
                    2
                )
            }
        }

        fun footerSeparator() = if (Minecraft.getInstance().level != null) {
            Screen.INWORLD_FOOTER_SEPARATOR
        } else {
            Screen.FOOTER_SEPARATOR
        }.let { texture ->
            Surface { graphics, x, y, width, height ->
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    x,
                    y + height - 2,
                    0.0f,
                    0.0f,
                    width,
                    2,
                    32,
                    2
                )
            }
        }

        fun separator() = combine(headerSeparator(), footerSeparator())

        fun listBackground() = if (Minecraft.getInstance().level != null) {
            AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND
        } else {
            AbstractSelectionList.MENU_LIST_BACKGROUND
        }.let { texture(it) }

        fun listBackgroundWithSeparator() = combine(
            padding(Insets(2, 0), listBackground()),
            separator(),
        )
    }
}
