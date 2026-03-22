package top.fifthlight.blazerod.render.version_1_21_8.runtime.renderer

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.opengl.GlRenderPass
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.VertexFormat
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector4f
import top.fifthlight.blazerod.render.common.BlazeRod
import top.fifthlight.blazerod.render.api.resource.RenderTask
import top.fifthlight.blazerod.render.common.runtime.data.MorphTargetBuffer
import top.fifthlight.blazerod.render.common.runtime.data.RenderSkinBuffer
import top.fifthlight.blazerod.render.common.runtime.uniform.ComputeDataUniformBuffer
import top.fifthlight.blazerod.render.common.runtime.uniform.MorphDataUniformBuffer
import top.fifthlight.blazerod.render.common.runtime.uniform.SkinModelIndicesUniformBuffer
import top.fifthlight.blazerod.render.common.util.bitmap.BitmapItem
import top.fifthlight.blazerod.render.common.util.gpushaderpool.GpuShaderDataPool
import top.fifthlight.blazerod.render.common.util.gpushaderpool.ofSsbo
import top.fifthlight.blazerod.render.common.util.gpushaderpool.upload
import top.fifthlight.blazerod.render.version_1_21_8.extension.*
import top.fifthlight.blazerod.model.toVector4f
import top.fifthlight.blazerod.render.common.util.math.ceilDiv
import top.fifthlight.blazerod.render.version_1_21_8.api.render.Renderer
import top.fifthlight.blazerod.render.version_1_21_8.expect.IrisApis
import top.fifthlight.blazerod.render.version_1_21_8.render.BlazerodVertexFormats
import top.fifthlight.blazerod.render.version_1_21_8.render.setIndexBuffer
import top.fifthlight.blazerod.render.version_1_21_8.runtime.RenderSceneImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.RenderTaskImpl
import top.fifthlight.blazerod.render.version_1_21_8.runtime.node.component.PrimitiveComponent
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderMaterial
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderPrimitive
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePass
import top.fifthlight.blazerod.render.version_1_21_8.systems.ComputePipeline
import java.util.*

class ComputeShaderTransformRenderer private constructor() :
    ScheduledRendererImpl<ComputeShaderTransformRenderer, ComputeShaderTransformRenderer.Type>() {

    @Suppress("NOTHING_TO_INLINE")
    @JvmInline
    private value class PipelineInfo(val bitmap: BitmapItem = BitmapItem()) {
        constructor(
            skinned: Boolean = false,
            irisVertexFormat: Boolean = false,
            morphed: Boolean = false,
        ) : this(Unit.run {
            var item = BitmapItem()
            if (skinned) {
                item += ELEMENT_SKINNED
            }
            if (irisVertexFormat) {
                item += ELEMENT_IRIS_VERTEX_FORMAT
            }
            if (morphed) {
                item += ELEMENT_MORPHED
            }
            item
        })

        constructor(
            material: RenderMaterial<*>,
            irisVertexFormat: Boolean,
        ) : this(
            skinned = material.skinned,
            irisVertexFormat = irisVertexFormat,
            morphed = material.morphed
        )

        val skinned
            get() = ELEMENT_SKINNED in bitmap
        val irisVertexFormat
            get() = ELEMENT_IRIS_VERTEX_FORMAT in bitmap
        val morphed
            get() = ELEMENT_MORPHED in bitmap

        fun nameSuffix() = buildString {
            if (skinned) {
                append("_skinned")
            }
            if (irisVertexFormat) {
                append("_iris_vertex_format")
            }
            if (morphed) {
                append("_morphed")
            }
        }

        companion object {
            val ELEMENT_SKINNED = BitmapItem.Element.of(0)
            val ELEMENT_IRIS_VERTEX_FORMAT = BitmapItem.Element.of(1)
            val ELEMENT_MORPHED = BitmapItem.Element.of(2)
        }

        inline operator fun plus(element: BitmapItem.Element) =
            PipelineInfo(bitmap + element)

        inline operator fun minus(element: BitmapItem.Element) =
            PipelineInfo(bitmap - element)

        inline operator fun contains(element: BitmapItem.Element) =
            element in bitmap

        override fun toString(): String {
            return "PipelineInfo(skinned=$skinned, irisVertexFormat=$irisVertexFormat, morphed=$morphed)"
        }
    }

    companion object Type : Renderer.Type<ComputeShaderTransformRenderer, Type>() {
        override val id: String
            get() = "compute_shader"
        override val isAvailable: Boolean by lazy {
            val device = RenderSystem.getDevice()
            device.supportSsbo && device.supportComputeShader && device.supportMemoryBarrier && device.supportShaderPacking
        }

        override val supportScheduling: Boolean
            get() = true

        override fun create() = ComputeShaderTransformRenderer()

        private val pipelineCache = mutableMapOf<RenderMaterial.Descriptor, Int2ReferenceMap<ComputePipeline>>()

        private fun getPipeline(material: RenderMaterial<*>, irisVertexFormat: Boolean): ComputePipeline {
            val pipelineInfo = PipelineInfo(
                material = material,
                irisVertexFormat = irisVertexFormat,
            )
            val materialMap = pipelineCache.getOrPut(material.descriptor) { Int2ReferenceAVLTreeMap() }
            return materialMap.getOrPut(pipelineInfo.bitmap.inner) {
                ComputePipeline.builder().apply {
                    withLocation(ResourceLocation.fromNamespaceAndPath("blazerod", "vertex_transform" + pipelineInfo.nameSuffix()))
                    withComputeShader(ResourceLocation.fromNamespaceAndPath("blazerod", "compute/vertex_transform"))
                    withShaderDefine("SUPPORT_SSBO")
                    withShaderDefine("COMPUTE_SHADER")
                    withStorageBuffer("SourceVertexData")
                    withStorageBuffer("TargetVertexData")
                    if (pipelineInfo.irisVertexFormat) {
                        withShaderDefine("IRIS_VERTEX_FORMAT")
                    }
                    if (pipelineInfo.morphed) {
                        withShaderDefine("MORPHED")
                        withShaderDefine("MAX_ENABLED_MORPH_TARGETS", BlazeRod.MAX_ENABLED_MORPH_TARGETS)
                        withUniform("MorphData", UniformType.UNIFORM_BUFFER)
                        withStorageBuffer("MorphPositionBlock")
                        withStorageBuffer("MorphColorBlock")
                        withStorageBuffer("MorphTexCoordBlock")
                        withStorageBuffer("MorphTargetIndicesData")
                        withStorageBuffer("MorphWeightsData")
                    }
                    if (pipelineInfo.skinned) {
                        withShaderDefine("SKINNED")
                        withUniform("SkinModelIndices", UniformType.UNIFORM_BUFFER)
                        withStorageBuffer("JointsData")
                    }
                    withUniform("ComputeData", UniformType.UNIFORM_BUFFER)
                    withShaderDefine("INSTANCE_SIZE", BlazeRod.INSTANCE_SIZE)
                    withShaderDefine("COMPUTE_LOCAL_SIZE", BlazeRod.COMPUTE_LOCAL_SIZE)
                    withShaderDefine("INPUT_MATERIAL", material.descriptor.id)
                }.build()
            }
        }
    }

    override val type: Type
        get() = Type

    private val dataPool = GpuShaderDataPool.ofSsbo()
    private val vertexDataPool = GpuShaderDataPool.create(
        usage = GpuBuffer.USAGE_VERTEX,
        extraUsage = GpuBufferExt.EXTRA_USAGE_STORAGE_BUFFER,
        alignment = RenderSystem.getDevice().ssboOffsetAlignment,
        supportSlicing = false,
    )

    private fun dispatchCompute(
        primitive: RenderPrimitive,
        task: RenderTaskImpl,
        skinBuffer: RenderSkinBuffer?,
        targetBuffer: MorphTargetBuffer?,
        targetVertexFormat: VertexFormat,
        irisVertexFormat: Boolean,
        modelNormalMatrix: Matrix4fc,
    ): GpuBufferSlice {
        val device = RenderSystem.getDevice()
        val commandEncoder = device.createCommandEncoder()
        val material = primitive.material
        var computePass: ComputePass? = null
        var targetVertexData: GpuBufferSlice
        val computeDataUniformBufferSlice: GpuBufferSlice
        var skinModelIndicesBufferSlice: GpuBufferSlice? = null
        var skinJointBufferSlice: GpuBufferSlice? = null
        var morphDataUniformBufferSlice: GpuBufferSlice? = null
        var morphWeightsBufferSlice: GpuBufferSlice? = null
        var morphTargetIndicesBufferSlice: GpuBufferSlice? = null

        try {
            targetVertexData = vertexDataPool.allocate(targetVertexFormat.vertexSize * primitive.vertices)
            computeDataUniformBufferSlice = ComputeDataUniformBuffer.write {
                this.modelNormalMatrix = modelNormalMatrix
                totalVertices = primitive.vertices.toUInt()
                uv1 = OverlayTexture.NO_OVERLAY.toUInt()
                uv2 = task.light.toUInt()
            }
            skinBuffer?.let { skinBuffer ->
                skinModelIndicesBufferSlice = SkinModelIndicesUniformBuffer.write {
                    skinJoints = skinBuffer.jointSize
                }
                skinJointBufferSlice = dataPool.upload(skinBuffer.buffer)
            }
            targetBuffer?.let { targetBuffer ->
                primitive.targets?.let { targets ->
                    morphDataUniformBufferSlice = MorphDataUniformBuffer.write {
                        totalVertices = primitive.vertices
                        posTargets = targets.position.targetsCount
                        colorTargets = targets.color.targetsCount
                        texCoordTargets = targets.texCoord.targetsCount
                        totalTargets =
                            targets.position.targetsCount + targets.color.targetsCount + targets.texCoord.targetsCount
                    }
                }
                morphWeightsBufferSlice = dataPool.upload(targetBuffer.weightsBuffer)
                morphTargetIndicesBufferSlice = dataPool.upload(targetBuffer.indicesBuffer)
            }

            val pipeline = getPipeline(
                material = material,
                irisVertexFormat = irisVertexFormat,
            )

            computePass = commandEncoder.createComputePass { "BlazeRod compute pass" }

            with(computePass) {
                setPipeline(pipeline)

                if (GlRenderPass.VALIDATION) {
                    require(material.skinned == (skinBuffer != null)) {
                        "Primitive's skin data ${skinBuffer != null} and material skinned ${material.skinned} not matching"
                    }
                }
                setStorageBuffer("SourceVertexData", primitive.gpuVertexBuffer!!.inner.slice())
                setStorageBuffer("TargetVertexData", targetVertexData)
                setUniform("ComputeData", computeDataUniformBufferSlice)
                skinJointBufferSlice?.let { skinJointBuffer ->
                    if (device.supportSsbo) {
                        setStorageBuffer("JointsData", skinJointBuffer)
                    } else {
                        setUniform("Joints", skinJointBuffer)
                    }
                }
                skinModelIndicesBufferSlice?.let { skinModelIndices ->
                    setUniform("SkinModelIndices", skinModelIndices)
                }
                morphDataUniformBufferSlice?.let { morphDataUniformBuffer ->
                    setUniform("MorphData", morphDataUniformBuffer)
                }
                morphWeightsBufferSlice?.let { morphWeightsBuffer ->
                    if (device.supportSsbo) {
                        setStorageBuffer("MorphWeightsData", morphWeightsBuffer)
                    } else {
                        setUniform("MorphWeights", morphWeightsBuffer)
                    }
                }
                morphTargetIndicesBufferSlice?.let { morphTargetIndicesBuffer ->
                    if (device.supportSsbo) {
                        setStorageBuffer("MorphTargetIndicesData", morphTargetIndicesBuffer)
                    } else {
                        setUniform("MorphTargetIndices", morphTargetIndicesBuffer)
                    }
                }
                primitive.targets?.let { targets ->
                    setStorageBuffer("MorphPositionBlock", targets.position.slice!!)
                    setStorageBuffer("MorphColorBlock", targets.color.slice!!)
                    setStorageBuffer("MorphTexCoordBlock", targets.texCoord.slice!!)
                }
                val totalWorkSize = primitive.vertices ceilDiv BlazeRod.COMPUTE_LOCAL_SIZE
                computePass.dispatch(totalWorkSize, 1, 1)
            }
        } finally {
            computePass?.close()
        }

        return targetVertexData
    }

    private class ComputeItem(
        val primitiveComponent: PrimitiveComponent,
        val renderTask: RenderTaskImpl,
        val vertexFormat: VertexFormat,
        val vertexBuffer: GpuBufferSlice,
    )

    private val modelMatrix = Matrix4f()
    private val modelNormalMatrix = Matrix4f()
    private val renderTasks = mutableListOf<RenderTaskImpl>()
    private val computeItems = mutableListOf<ComputeItem>()

    override fun schedule(task: RenderTask) {
        val task = task as RenderTaskImpl
        val instance = task.instance
        val scene = instance.scene
        renderTasks.add(task)
        for (primitiveComponent in scene.primitiveComponents) {
            val primitive = primitiveComponent.primitive

            if (!primitive.gpuComplete) {
                return
            }

            task.localMatricesBuffer.content.getPositionMatrix(
                primitiveComponent.primitiveIndex,
                modelMatrix,
            )
            modelMatrix.mulLocal(task.modelMatrix)
            modelMatrix.normal(modelNormalMatrix)

            val irisVertexFormat = IrisApis.shaderPackInUse
            val targetVertexFormat = if (irisVertexFormat) {
                BlazerodVertexFormats.IRIS_ENTITY_PADDED
            } else {
                BlazerodVertexFormats.ENTITY_PADDED
            }
            val vertexBuffer = dispatchCompute(
                primitive = primitive,
                task = task,
                skinBuffer = primitiveComponent.skinIndex?.let { task.skinBuffer[it] }?.content,
                targetBuffer = primitiveComponent.morphedPrimitiveIndex?.let { task.morphTargetBuffer[it] }?.content,
                targetVertexFormat = targetVertexFormat,
                irisVertexFormat = irisVertexFormat,
                modelNormalMatrix = modelNormalMatrix,
            )

            val item = ComputeItem(
                primitiveComponent = primitiveComponent,
                renderTask = task,
                vertexFormat = targetVertexFormat,
                vertexBuffer = vertexBuffer,
            )

            computeItems.add(item)
        }
    }

    private val baseColor = Vector4f()
    override fun executeTasks(
        colorFrameBuffer: GpuTextureView,
        depthFrameBuffer: GpuTextureView?,
    ) {
        if (computeItems.isEmpty()) {
            return
        }

        val device = RenderSystem.getDevice()
        val commandEncoder = device.createCommandEncoder()
        commandEncoder.memoryBarrier(CommandEncoderExt.BARRIER_STORAGE_BUFFER_BIT or CommandEncoderExt.BARRIER_VERTEX_BUFFER_BIT)

        for (item in computeItems) {
            val task = item.renderTask
            val primitiveComponent = item.primitiveComponent
            val primitive = primitiveComponent.primitive
            val material = primitive.material

            task.localMatricesBuffer.content.getPositionMatrix(
                primitiveComponent.primitiveIndex,
                modelMatrix,
            )
            modelMatrix.mulLocal(task.modelMatrix)
            modelMatrix.mulLocal(RenderSystem.getModelViewStack())

            val dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                modelMatrix,
                material.baseColor.toVector4f(baseColor),
                RenderSystem.getModelOffset(),
                RenderSystem.getTextureMatrix(),
                RenderSystem.getShaderLineWidth()
            )

            commandEncoder.createRenderPass(
                { "BlazeRod render pass" },
                colorFrameBuffer,
                OptionalInt.empty(),
                depthFrameBuffer,
                OptionalDouble.empty()
            ).use {
                with(it) {
                    setPipeline(RenderPipelines.ENTITY_TRANSLUCENT)
                    RenderSystem.bindDefaultUniforms(this)
                    setUniform("DynamicTransforms", dynamicUniforms)
                    bindSampler(
                        "Sampler2",
                        Minecraft.getInstance().gameRenderer.lightTexture().textureView
                    )
                    bindSampler(
                        "Sampler1",
                        Minecraft.getInstance().gameRenderer.overlayTexture().texture.textureView
                    )
                    when (material) {
                        is RenderMaterial.Pbr -> {}
                        is RenderMaterial.Unlit -> {
                            bindSampler("Sampler0", material.baseColorTexture.view)
                        }

                        is RenderMaterial.Vanilla -> {
                            bindSampler("Sampler0", material.baseColorTexture.view)
                        }
                    }

                    setVertexFormat(item.vertexFormat)
                    setVertexFormatMode(primitive.vertexFormatMode)
                    setVertexBuffer(0, item.vertexBuffer.buffer())
                    primitive.indexBuffer?.let { indices ->
                        setIndexBuffer(indices)
                        drawIndexed(0, 0, indices.length, 1)
                    } ?: run {
                        draw(0, primitive.vertices)
                    }
                }
            }
        }
        computeItems.clear()
        renderTasks.forEach { it.release() }
        renderTasks.clear()
    }

    override fun render(
        colorFrameBuffer: GpuTextureView,
        depthFrameBuffer: GpuTextureView?,
        scene: RenderSceneImpl,
        primitive: RenderPrimitive,
        primitiveIndex: Int,
        task: RenderTaskImpl,
        skinBuffer: RenderSkinBuffer?,
        targetBuffer: MorphTargetBuffer?,
    ) {
        if (!primitive.gpuComplete) {
            return
        }

        val device = RenderSystem.getDevice()
        val commandEncoder = device.createCommandEncoder()
        val material = primitive.material

        task.localMatricesBuffer.content.getPositionMatrix(primitiveIndex, modelMatrix)
        modelMatrix.mulLocal(task.modelMatrix)
        modelMatrix.normal(modelNormalMatrix)
        modelMatrix.mulLocal(RenderSystem.getModelViewStack())

        val irisVertexFormat = IrisApis.shaderPackInUse
        val targetVertexFormat = if (irisVertexFormat) {
            BlazerodVertexFormats.IRIS_ENTITY_PADDED
        } else {
            BlazerodVertexFormats.ENTITY_PADDED
        }
        val vertexBuffer = dispatchCompute(
            primitive = primitive,
            task = task,
            skinBuffer = skinBuffer,
            targetBuffer = targetBuffer,
            targetVertexFormat = targetVertexFormat,
            irisVertexFormat = irisVertexFormat,
            modelNormalMatrix = modelNormalMatrix,
        )

        commandEncoder.memoryBarrier(CommandEncoderExt.BARRIER_STORAGE_BUFFER_BIT or CommandEncoderExt.BARRIER_VERTEX_BUFFER_BIT)

        val dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
            modelMatrix,
            material.baseColor.toVector4f(baseColor),
            RenderSystem.getModelOffset(),
            RenderSystem.getTextureMatrix(),
            RenderSystem.getShaderLineWidth()
        )

        commandEncoder.createRenderPass(
            { "BlazeRod render pass" },
            colorFrameBuffer,
            OptionalInt.empty(),
            depthFrameBuffer,
            OptionalDouble.empty()
        ).use {
            with(it) {
                setPipeline(RenderPipelines.ENTITY_TRANSLUCENT)
                RenderSystem.bindDefaultUniforms(this)
                setUniform("DynamicTransforms", dynamicUniforms)
                bindSampler("Sampler2", Minecraft.getInstance().gameRenderer.lightTexture().textureView)
                bindSampler("Sampler1", Minecraft.getInstance().gameRenderer.overlayTexture().texture.textureView)
                when (material) {
                    is RenderMaterial.Pbr -> {}
                    is RenderMaterial.Unlit -> {
                        bindSampler("Sampler0", material.baseColorTexture.view)
                    }

                    is RenderMaterial.Vanilla -> {
                        bindSampler("Sampler0", material.baseColorTexture.view)
                    }
                }

                setVertexFormat(targetVertexFormat)
                setVertexFormatMode(primitive.vertexFormatMode)
                setVertexBuffer(0, vertexBuffer.buffer())
                primitive.indexBuffer?.let { indices ->
                    setIndexBuffer(indices)
                    drawIndexed(0, 0, indices.length, 1)
                } ?: run {
                    draw(0, primitive.vertices)
                }
            }
        }
    }

    override fun rotate() {
        computeItems.clear()
        renderTasks.forEach { it.release() }
        renderTasks.clear()
        dataPool.rotate()
        vertexDataPool.rotate()
    }

    override fun close() {
        computeItems.clear()
        renderTasks.forEach { it.release() }
        renderTasks.clear()
        dataPool.close()
        vertexDataPool.close()
    }
}
