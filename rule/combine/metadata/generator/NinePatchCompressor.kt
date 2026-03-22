package top.fifthlight.combine.resources.generator

import top.fifthlight.combine.resources.NinePatch
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import java.awt.Graphics2D
import java.awt.image.BufferedImage

fun BufferedImage.getRGB(offset: IntOffset) = getRGB(offset.x, offset.y)
fun Graphics2D.drawImage(image: BufferedImage, srcRect: IntRect, dstRect: IntRect) = drawImage(
    image,
    dstRect.left,
    dstRect.top,
    dstRect.right,
    dstRect.bottom,
    srcRect.left,
    srcRect.top,
    srcRect.right,
    srcRect.bottom,
    null
)

private fun isCenterCompressible(ninePatch: NinePatch, image: BufferedImage): Boolean {
    val centerColor = image.getRGB(ninePatch.scaleArea.offset)
    return ninePatch.scaleArea.all { image.getRGB(it) == centerColor }
}

private fun isHorizontalCompressible(ninePatch: NinePatch, image: BufferedImage): Boolean {
    // Top border
    if (ninePatch.scaleArea.top > 0) {
        for (y in 0 until ninePatch.scaleArea.top) {
            val firstColor = image.getRGB(ninePatch.scaleArea.left, y)
            for (x in (ninePatch.scaleArea.left + 1) until ninePatch.scaleArea.right) {
                if (image.getRGB(x, y) != firstColor) {
                    return false
                }
            }
        }
    }

    // Bottom border
    if (ninePatch.scaleArea.bottom < image.height) {
        for (y in ninePatch.scaleArea.bottom until image.height) {
            val firstColor = image.getRGB(ninePatch.scaleArea.left, y)
            for (x in (ninePatch.scaleArea.left + 1) until ninePatch.scaleArea.right) {
                if (image.getRGB(x, y) != firstColor) {
                    return false
                }
            }
        }
    }

    return true
}

private fun isVerticalCompressible(ninePatch: NinePatch, image: BufferedImage): Boolean {
    // Left border
    if (ninePatch.scaleArea.left > 0) {
        for (x in 0 until ninePatch.scaleArea.left) {
            val topColor = image.getRGB(x, ninePatch.scaleArea.top)
            for (y in (ninePatch.scaleArea.top + 1) until ninePatch.scaleArea.bottom) {
                if (image.getRGB(x, y) != topColor) {
                    return false
                }
            }
        }
    }

    // Right border
    if (ninePatch.scaleArea.right < image.width) {
        for (x in ninePatch.scaleArea.right until image.width) {
            val topColor = image.getRGB(x, ninePatch.scaleArea.top)
            for (y in (ninePatch.scaleArea.top + 1) until ninePatch.scaleArea.bottom) {
                if (image.getRGB(x, y) != topColor) {
                    return false
                }
            }
        }
    }

    return true
}

fun compressNinePatch(ninePatch: NinePatch, image: BufferedImage): Pair<NinePatch, BufferedImage> {
    val centerCompressible = isCenterCompressible(ninePatch, image)
    if (!centerCompressible) {
        return Pair(ninePatch, image)
    }

    val horizontalCompressible = isHorizontalCompressible(ninePatch, image)
    val verticalCompressible = isVerticalCompressible(ninePatch, image)

    if (!horizontalCompressible && !verticalCompressible) {
        return Pair(ninePatch, image)
    }

    val newWidth = if (horizontalCompressible) {
        ninePatch.scaleArea.left + 1 + (image.width - ninePatch.scaleArea.right)
    } else {
        image.width
    }
    val newHeight = if (verticalCompressible) {
        ninePatch.scaleArea.top + 1 + (image.height - ninePatch.scaleArea.bottom)
    } else {
        image.height
    }

    val outputImage = BufferedImage(newWidth, newHeight, image.type)
    with(outputImage.createGraphics()) {
        fun Graphics2D.copyImage(
            image: BufferedImage,
            srcOffset: IntOffset,
            dstOffset: IntOffset = srcOffset,
            size: IntSize
        ) =
            drawImage(
                image = image,
                srcRect = IntRect(
                    offset = srcOffset,
                    size = size,
                ),
                dstRect = IntRect(
                    offset = dstOffset,
                    size = size,
                ),
            )

        val haveLeftBorder = ninePatch.scaleArea.left > 0
        val haveTopBorder = ninePatch.scaleArea.top > 0
        val haveRightBorder = ninePatch.scaleArea.right < image.width
        val haveBottomBorder = ninePatch.scaleArea.bottom < image.height

        // Left top edge
        if (haveLeftBorder && haveTopBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset.ZERO,
                size = IntSize(ninePatch.scaleArea.left, ninePatch.scaleArea.top)
            )
        }

        // Top border
        if (haveTopBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = ninePatch.scaleArea.left,
                    y = 0,
                ),
                size = if (horizontalCompressible) {
                    IntSize(
                        width = 1,
                        height = ninePatch.scaleArea.top,
                    )
                } else {
                    IntSize(
                        width = ninePatch.scaleArea.size.width,
                        height = ninePatch.scaleArea.top,
                    )
                }
            )
        }

        // Right top edge
        if (haveTopBorder && haveRightBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = ninePatch.scaleArea.right,
                    y = 0
                ),
                dstOffset = IntOffset(
                    x = if (horizontalCompressible) {
                        ninePatch.scaleArea.left + 1
                    } else {
                        ninePatch.scaleArea.right
                    },
                    y = 0
                ),
                size = IntSize(
                    width = image.width - ninePatch.scaleArea.right,
                    height = ninePatch.scaleArea.top
                )
            )
        }

        // Left border
        if (haveLeftBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = 0,
                    y = ninePatch.scaleArea.top,
                ),
                size = IntSize(
                    width = ninePatch.scaleArea.left,
                    height = if (verticalCompressible) {
                        1
                    } else {
                        ninePatch.scaleArea.size.height
                    }
                )
            )
        }

        // Center
        val centerSize = IntSize(
            width = if (horizontalCompressible) {
                1
            } else {
                ninePatch.scaleArea.size.width
            },
            height = if (verticalCompressible) {
                1
            } else {
                ninePatch.scaleArea.size.height
            },
        )
        drawImage(
            image = image,
            srcRect = ninePatch.scaleArea,
            dstRect = IntRect(
                offset = ninePatch.scaleArea.offset,
                size = centerSize,
            ),
        )

        // Right border
        if (haveRightBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = ninePatch.scaleArea.right,
                    y = ninePatch.scaleArea.top,
                ),
                dstOffset = IntOffset(
                    x = if (horizontalCompressible) {
                        ninePatch.scaleArea.left + 1
                    } else {
                        ninePatch.scaleArea.right
                    },
                    y = ninePatch.scaleArea.top,
                ),
                size = IntSize(
                    width = image.width - ninePatch.scaleArea.right,
                    height = if (verticalCompressible) {
                        1
                    } else {
                        ninePatch.scaleArea.size.height
                    },
                )
            )
        }

        // Left bottom edge
        if (haveLeftBorder && haveBottomBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = 0,
                    y = ninePatch.scaleArea.bottom,
                ),
                dstOffset = IntOffset(
                    x = 0,
                    y = if (verticalCompressible) {
                        ninePatch.scaleArea.top + 1
                    } else {
                        ninePatch.scaleArea.bottom
                    },
                ),
                size = IntSize(
                    width = ninePatch.scaleArea.size.width,
                    height = image.height - ninePatch.scaleArea.bottom,
                )
            )
        }

        // Bottom border
        if (haveBottomBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = ninePatch.scaleArea.left,
                    y = ninePatch.scaleArea.bottom,
                ),
                dstOffset = IntOffset(
                    x = ninePatch.scaleArea.left,
                    y = if (verticalCompressible) {
                        ninePatch.scaleArea.top + 1
                    } else {
                        ninePatch.scaleArea.bottom
                    }
                ),
                size = IntSize(
                    width = if (horizontalCompressible) {
                        1
                    } else {
                        ninePatch.scaleArea.size.width
                    },
                    height = image.height - ninePatch.scaleArea.bottom
                )
            )
        }

        // Right bottom edge
        if (haveRightBorder && haveBottomBorder) {
            copyImage(
                image = image,
                srcOffset = IntOffset(
                    x = ninePatch.scaleArea.right,
                    y = ninePatch.scaleArea.bottom,
                ),
                dstOffset = IntOffset(
                    x = if (horizontalCompressible) {
                        ninePatch.scaleArea.left + 1
                    } else {
                        ninePatch.scaleArea.right
                    },
                    y = if (verticalCompressible) {
                        ninePatch.scaleArea.top + 1
                    } else {
                        ninePatch.scaleArea.bottom
                    },
                ),
                size = IntSize(
                    width = image.width - ninePatch.scaleArea.right,
                    height = image.height - ninePatch.scaleArea.bottom,
                )
            )
        }

        dispose()
    }

    return Pair(
        ninePatch.copy(
            scaleArea = ninePatch.scaleArea.copy(
                size = IntSize(
                    width = if (horizontalCompressible) {
                        1
                    } else {
                        ninePatch.scaleArea.size.width
                    },
                    height = if (verticalCompressible) {
                        1
                    } else {
                        ninePatch.scaleArea.size.height
                    }
                )
            )
        ),
        outputImage
    )
}
