package top.fifthlight.blazerod.render.common.runtime.load

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.TextureFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import top.fifthlight.blazerod.render.version_1_21_8.extension.GpuBufferExt
import top.fifthlight.blazerod.render.version_1_21_8.extension.TextureFormatExt
import top.fifthlight.blazerod.render.version_1_21_8.extension.createBuffer
import top.fifthlight.blazerod.render.version_1_21_8.render.GpuIndexBuffer
import top.fifthlight.blazerod.render.version_1_21_8.render.RefCountedGpuBuffer
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderPrimitive
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderTexture
import top.fifthlight.blazerod.render.version_1_21_8.util.blaze3d.blaze3d
import top.fifthlight.blazerod.render.version_1_21_8.util.blaze3d.useMipmap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object ModelResourceLoader {
    private fun <T, R> Deferred<T>.map(
        scope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (T) -> R,
    ): Deferred<R> = scope.async(context) {
        block(await())
    }

    private fun <T, R> Iterable<Deferred<T>>.mapAll(
        scope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (T) -> R,
    ): List<Deferred<R>> = map {
        it.map(scope, context, block)
    }

    fun load(
        scope: CoroutineScope,
        gpuDispatcher: CoroutineDispatcher,
        info: PreProcessModelLoadInfo,
    ): GpuLoadModelLoadInfo {
        val textures = info.textures.mapAll(scope, gpuDispatcher) { info ->
            val info = info ?: return@mapAll null
            info.use { info ->
                val (name, nativeImage, sampler) = info
                val device = RenderSystem.getDevice()
                val commandEncoder = device.createCommandEncoder()
                val gpuTexture: GpuTexture
                nativeImage.use {
                    gpuTexture = device.createTexture(
                        name,
                        GpuTexture.USAGE_TEXTURE_BINDING or GpuTexture.USAGE_COPY_DST,
                        when (it.format()) {
                            NativeImage.Format.RGBA -> TextureFormat.RGBA8
                            NativeImage.Format.RGB -> TextureFormatExt.RGB8
                            else -> throw IllegalArgumentException("Unknown texture format: ${it.format()}")
                        },
                        nativeImage.width,
                        nativeImage.height,
                        1,
                        1
                    )
                    gpuTexture.setAddressMode(sampler.wrapS.blaze3d, sampler.wrapT.blaze3d)
                    gpuTexture.setTextureFilter(
                        sampler.minFilter.blaze3d,
                        sampler.magFilter.blaze3d,
                        sampler.minFilter.useMipmap,
                    )
                    commandEncoder.writeToTexture(gpuTexture, nativeImage)
                }
                val textureView = device.createTextureView(gpuTexture)
                RenderTexture(gpuTexture, textureView)
            }
        }
        val indexBuffers = info.indexBuffers.mapAll(scope, gpuDispatcher) { indexData ->
            val device = RenderSystem.getDevice()
            val buffer = RefCountedGpuBuffer(
                device.createBuffer(
                    null,
                    GpuBuffer.USAGE_INDEX,
                    indexData.buffer,
                )
            )
            GpuIndexBuffer(
                type = indexData.type,
                length = indexData.length,
                buffer = buffer,
            )
        }
        val vertexBuffers = info.vertexBuffers.mapAll(scope, gpuDispatcher) {
            val device = RenderSystem.getDevice()
            val buffer = RefCountedGpuBuffer(
                device.createBuffer(
                    labelGetter = null,
                    usage = GpuBuffer.USAGE_VERTEX,
                    extraUsage = GpuBufferExt.EXTRA_USAGE_STORAGE_BUFFER,
                    data = it,
                )
            )
            GpuLoadVertexData(
                gpuBuffer = buffer,
                cpuBuffer = it,
            )
        }
        val morphTargetInfos = info.morphTargetInfos.mapAll(scope, gpuDispatcher) {
            fun loadTarget(target: MorphTargetsLoadData.TargetInfo): RenderPrimitive.Target {
                val targetBuffer = if (target.targetsCount == 0) {
                    // No targets, but we can't create an empty buffer, so let's create a dummy one
                    ByteBuffer.allocateDirect(target.itemStride).order(ByteOrder.nativeOrder())
                } else {
                    target.buffer
                }
                val device = RenderSystem.getDevice()
                val gpuBuffer = device.createBuffer(
                    labelGetter = { "Morph target buffer" },
                    usage = GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
                    extraUsage = GpuBufferExt.EXTRA_USAGE_STORAGE_BUFFER,
                    data = targetBuffer,
                )
                return RenderPrimitive.Target(
                    gpuBuffer = gpuBuffer,
                    cpuBuffer = targetBuffer,
                    targetsCount = target.targetsCount,
                )
            }
            MorphTargetsLoadData(
                targetGroups = it.targetGroups,
                position = loadTarget(it.position),
                color = loadTarget(it.color),
                texCoord = loadTarget(it.texCoord),
            )
        }
        return GpuLoadModelLoadInfo(
            textures = textures,
            indexBuffers = indexBuffers,
            vertexBuffers = vertexBuffers,
            morphTargetInfos = morphTargetInfos,
            primitiveInfos = info.primitiveInfos,
            rootNodeIndex = info.rootNodeIndex,
            nodes = info.nodes,
            skins = info.skins,
            expressions = info.expressions,
            expressionGroups = info.expressionGroups,
            physicalJoints = info.physicalJoints,
            renderTransform = info.renderTransform,
        )
    }
}
