package top.fifthlight.armorstand.ui.component

import com.mojang.blaze3d.platform.NativeImage
import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import top.fifthlight.armorstand.manage.ModelManagerHolder
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.manage.model.ModelThumbnail
import top.fifthlight.armorstand.util.BlockableEventLoopDispatcher
import top.fifthlight.blazerod.model.Texture
import top.fifthlight.blazerod.model.loader.util.readToBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.util.function.Consumer

class ModelIcon(
    private val modelItem: ModelItem,
) : AutoCloseable, LayoutElement, Renderable, ResizableLayout {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ModelIcon::class.java)
        private val LOADING_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "loading")
        private const val ICON_WIDTH = 32
        private const val ICON_HEIGHT = 32
        private const val SMALL_ICON_WIDTH = 16
        private const val SMALL_ICON_HEIGHT = 16
    }

    private var closed = false
    private fun requireOpen() = require(!closed) { "Model icon already closed" }

    private var x: Int = 0
    private var y: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    override fun setX(x: Int) {
        this.x = x
    }

    override fun setY(y: Int) {
        this.y = y
    }

    override fun getX(): Int = this.x
    override fun getY(): Int = this.y
    override fun getWidth(): Int = this.width
    override fun getHeight(): Int = this.height
    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }
    override fun visitWidgets(consumer: Consumer<AbstractWidget>) = Unit

    private data class ModelTexture(
        val textureManager: TextureManager,
        val resourceLocation: ResourceLocation,
        val texture: DynamicTexture,
        val width: Int,
        val height: Int,
    ) : AutoCloseable {
        private var closed = false

        override fun close() {
            if (closed) {
                return
            }
            closed = true
            textureManager.release(resourceLocation)
        }
    }

    private sealed class ModelIconState : AutoCloseable {
        override fun close() = Unit

        data object Loading : ModelIconState()
        data object None : ModelIconState()
        data object Failed : ModelIconState()
        data class Loaded(
            val icon: ModelTexture,
        ) : ModelIconState() {
            override fun close() = icon.close()
        }
    }

    private val scope = CoroutineScope(BlockableEventLoopDispatcher(Minecraft.getInstance()) + Job())
    private var iconState: ModelIconState = ModelIconState.Loading

    private suspend fun loadIcon(
        path: Path,
        offsetAndLength: Pair<Long, Long>?,
        readSizeLimit: Int = 32 * 1024 * 1024,
        type: Texture.TextureType? = null,
    ): ModelTexture {
        val buffer = withContext(Dispatchers.IO) {
            FileChannel.open(path).use {
                offsetAndLength?.let { (offset, length) ->
                    it.readToBuffer(
                        offset = offset,
                        length = length,
                        readSizeLimit = readSizeLimit,
                    )
                } ?: it.readToBuffer(
                    readSizeLimit = readSizeLimit,
                )
            }
        }

        val width: Int
        val height: Int
        val identifier = ResourceLocation.fromNamespaceAndPath("armorstand", "models/${modelItem.hash}")
        val texture = withContext(Dispatchers.Default) {
            NativeImage.read(buffer)
        }.use { image ->
            width = image.width
            height = image.height
            DynamicTexture({ "Model icon for ${modelItem.hash}" }, image)
        }
        texture.setClamp(true)
        texture.setFilter(true, false)
        return try {
            val textureManager = Minecraft.getInstance().textureManager
            textureManager.register(identifier, texture)
            ModelTexture(
                textureManager = textureManager,
                resourceLocation = identifier,
                texture = texture,
                width = width,
                height = height,
            )
        } catch (ex: Throwable) {
            texture.close()
            throw ex
        }
    }

    init {
        scope.launch {
            val path = withContext(Dispatchers.IO) {
                ModelManagerHolder.modelDir.resolve(modelItem.path).toAbsolutePath()
            }
            val thumbnail = ModelManagerHolder.instance.getModelThumbnail(modelItem)
            try {
                when (thumbnail) {
                    is ModelThumbnail.Embed -> {
                        val icon = loadIcon(
                            path = path,
                            offsetAndLength = Pair(thumbnail.offset, thumbnail.length),
                            type = thumbnail.type,
                        )
                        iconState = ModelIconState.Loaded(icon)
                    }

                    is ModelThumbnail.External -> {
                        val icon = loadIcon(
                            path = thumbnail.path,
                            offsetAndLength = null,
                        )
                        iconState = ModelIconState.Loaded(icon)
                    }

                    ModelThumbnail.None -> {
                        iconState = ModelIconState.None
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                LOGGER.warn("Failed to read model icon", ex)
                iconState = ModelIconState.Failed
            }
        }
    }

    override fun render(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        renderIconInternal(graphics, x, y, width, height)
    }

    private fun renderIconInternal(
        graphics: GuiGraphics,
        targetX: Int,
        targetY: Int,
        targetWidth: Int,
        targetHeight: Int,
    ) {
        requireOpen()
        val imageWidth = targetWidth
        val imageHeight = targetHeight
        val left = targetX
        val top = targetY

        when (val state = iconState) {
            is ModelIconState.Loaded -> {
                val icon = state.icon
                val iconAspect = icon.width.toFloat() / icon.height.toFloat()
                val targetAspect = imageWidth.toFloat() / imageHeight.toFloat()

                if (iconAspect > targetAspect) {
                    val scaledHeight = (imageWidth / iconAspect).toInt()
                    val yOffset = (imageHeight - scaledHeight) / 2

                    graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        icon.resourceLocation,
                        left,
                        top + yOffset,
                        0f,
                        0f,
                        imageWidth,
                        scaledHeight,
                        icon.width,
                        icon.height,
                        icon.width,
                        icon.height,
                    )

                    graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        modelItem.type.icon,
                        left + imageWidth - SMALL_ICON_WIDTH / 2,
                        top + yOffset + scaledHeight - SMALL_ICON_HEIGHT / 2,
                        SMALL_ICON_WIDTH,
                        SMALL_ICON_HEIGHT,
                    )
                } else {
                    val scaledWidth = (imageHeight * iconAspect).toInt()
                    val xOffset = (imageWidth - scaledWidth) / 2

                    graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        icon.resourceLocation,
                        left + xOffset,
                        top,
                        0f,
                        0f,
                        scaledWidth,
                        imageHeight,
                        icon.width,
                        icon.height,
                        icon.width,
                        icon.height,
                    )

                    graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        modelItem.type.icon,
                        left + xOffset + scaledWidth - SMALL_ICON_WIDTH / 2,
                        top + imageHeight - SMALL_ICON_HEIGHT / 2,
                        SMALL_ICON_WIDTH,
                        SMALL_ICON_HEIGHT,
                    )
                }
            }

            ModelIconState.Loading -> {
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    LOADING_ICON,
                    left + (imageWidth - ICON_WIDTH) / 2,
                    top + (imageHeight - ICON_HEIGHT) / 2,
                    ICON_WIDTH,
                    ICON_HEIGHT,
                )
            }

            ModelIconState.None -> {
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    modelItem.type.icon,
                    left + (imageWidth - ICON_WIDTH) / 2,
                    top + (imageHeight - ICON_HEIGHT) / 2,
                    ICON_WIDTH,
                    ICON_HEIGHT,
                )
            }

            ModelIconState.Failed -> {}
        }
    }

    override fun close() {
        scope.cancel()
        iconState.close()
        closed = true
    }
}
