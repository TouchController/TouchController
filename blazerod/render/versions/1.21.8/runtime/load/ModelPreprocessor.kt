package top.fifthlight.blazerod.render.common.runtime.load

import com.mojang.blaze3d.textures.TextureFormat
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import kotlinx.coroutines.*
import top.fifthlight.blazerod.common.resource.MorphTargetGroup
import top.fifthlight.blazerod.common.resource.RenderPhysicsJoint
import top.fifthlight.blazerod.common.resource.RenderSkin
import top.fifthlight.blazerod.model.*
import top.fifthlight.blazerod.render.api.resource.RenderExpression
import top.fifthlight.blazerod.render.api.resource.RenderExpressionGroup
import top.fifthlight.blazerod.render.version_1_21_8.extension.NativeImageExt
import top.fifthlight.blazerod.render.version_1_21_8.extension.TextureFormatExt
import top.fifthlight.blazerod.render.version_1_21_8.render.BlazerodVertexFormatElements
import top.fifthlight.blazerod.render.version_1_21_8.render.BlazerodVertexFormats
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelPreprocessor private constructor(
    private val coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val model: Model,
) {
    data class SkinJointData(
        val skinIndex: Int,
        val jointIndex: Int,
        val humanoidTag: HumanoidTag?,
    )

    private lateinit var skinsList: List<RenderSkin>
    private lateinit var skinJointsData: Map<NodeId, List<SkinJointData>>

    private fun loadSkins() {
        val skinJointMap = mutableMapOf<NodeId, MutableList<SkinJointData>>()
        val skinsList = mutableListOf<RenderSkin>()
        for ((index, skin) in model.skins.withIndex()) {
            val renderSkin = RenderSkin(
                name = skin.name,
                inverseBindMatrices = skin.inverseBindMatrices,
                jointSize = skin.joints.size,
            )
            for ((jointIndex, joint) in skin.joints.withIndex()) {
                skinJointMap.getOrPut(joint) { mutableListOf() }.add(
                    SkinJointData(
                        skinIndex = index,
                        jointIndex = jointIndex,
                        humanoidTag = skin.jointHumanoidTags?.getOrNull(jointIndex),
                    )
                )
            }
            skinsList.add(renderSkin)
        }
        this.skinsList = skinsList
        this.skinJointsData = skinJointMap
    }

    private val textures = mutableListOf<Deferred<TextureLoadData?>>()
    private val textureIndexMap = mutableMapOf<Texture, Int>()
    private fun loadTextureIndex(texture: Texture) = textureIndexMap.getOrPut(texture) {
        val texture = coroutineScope.async(dispatcher) {
            val bufferView = texture.bufferView ?: return@async null
            val byteBuffer = bufferView.buffer.buffer
                .slice(bufferView.byteOffset, bufferView.byteLength)
                .order(ByteOrder.nativeOrder())
            val nativeImage = try {
                NativeImageExt.read(null, texture.type, byteBuffer)
            } catch (ex: Exception) {
                throw Exception("Failed to load texture ${texture.name ?: "unnamed"}", ex)
            }
            TextureLoadData(
                name = texture.name,
                nativeImage = nativeImage,
                sampler = texture.sampler,
            )
        }
        val index = textures.size
        textures.add(texture)
        index
    }

    private fun loadTextureInfo(textureInfo: Material.TextureInfo?) = textureInfo?.let {
        MaterialLoadInfo.TextureInfo(
            textureIndex = loadTextureIndex(textureInfo.texture),
            textureCoordinate = textureInfo.textureCoordinate,
        )
    }

    private fun loadMaterial(
        material: Material,
        skinned: Boolean,
        morphed: Boolean,
    ) = when (material) {
        // TODO: Pbr is not really supported for now
        is Material.Pbr -> MaterialLoadInfo.Unlit(
            name = material.name,
            baseColor = material.baseColor,
            baseColorTexture = loadTextureInfo(material.baseColorTexture),
            alphaMode = material.alphaMode,
            alphaCutoff = material.alphaCutoff,
            doubleSided = material.doubleSided,
            skinned = skinned,
            morphed = morphed,
        )

        is Material.Unlit -> MaterialLoadInfo.Unlit(
            name = material.name,
            baseColor = material.baseColor,
            baseColorTexture = loadTextureInfo(material.baseColorTexture),
            alphaMode = material.alphaMode,
            alphaCutoff = material.alphaCutoff,
            doubleSided = material.doubleSided,
            skinned = skinned,
            morphed = morphed,
        )

        is Material.Vanilla -> MaterialLoadInfo.Vanilla(
            name = material.name,
            baseColor = material.baseColor,
            baseColorTexture = loadTextureInfo(material.baseColorTexture),
            alphaMode = material.alphaMode,
            alphaCutoff = material.alphaCutoff,
            doubleSided = material.doubleSided,
            skinned = skinned,
            morphed = morphed,
        )
    }

    private data class IndexBufferLoadInfo(
        val type: VertexFormat.IndexType,
        val bufferIndex: Int,
    )

    private val indexBuffers = mutableListOf<Deferred<IndexBufferLoadData>>()
    private fun loadIndexBuffer(accessor: Accessor): IndexBufferLoadInfo {
        check(accessor.type == Accessor.AccessorType.SCALAR) { "Index buffer must be scalar" }
        return when (accessor.componentType) {
            Accessor.ComponentType.UNSIGNED_SHORT, Accessor.ComponentType.UNSIGNED_INT -> {
                val byteBuffer = accessor.readByteBuffer()
                val index = indexBuffers.size
                indexBuffers.add(
                    CompletableDeferred(
                        IndexBufferLoadData(
                            type = when (accessor.componentType) {
                                Accessor.ComponentType.UNSIGNED_SHORT -> VertexFormat.IndexType.SHORT
                                Accessor.ComponentType.UNSIGNED_INT -> VertexFormat.IndexType.INT
                                else -> error("Bad index type: ${accessor.componentType}")
                            },
                            length = accessor.count,
                            buffer = byteBuffer,
                        )
                    )
                )
                IndexBufferLoadInfo(
                    type = when (accessor.componentType) {
                        Accessor.ComponentType.UNSIGNED_SHORT -> VertexFormat.IndexType.SHORT
                        Accessor.ComponentType.UNSIGNED_INT -> VertexFormat.IndexType.INT
                        else -> throw AssertionError()
                    },
                    bufferIndex = index,
                )
            }

            Accessor.ComponentType.UNSIGNED_BYTE -> {
                val loadData = coroutineScope.async(dispatcher) {
                    val byteBuffer =
                        ByteBuffer.allocateDirect(2 * accessor.count).order(ByteOrder.nativeOrder()).apply {
                            accessor.read { input ->
                                val index = input.get().toUByte()
                                putShort(index.toShort())
                            }
                            flip()
                        }
                    IndexBufferLoadData(
                        type = VertexFormat.IndexType.SHORT,
                        length = accessor.count,
                        buffer = byteBuffer,
                    )
                }
                val index = indexBuffers.size
                indexBuffers.add(loadData)
                IndexBufferLoadInfo(
                    type = VertexFormat.IndexType.SHORT,
                    bufferIndex = index,
                )
            }

            else -> throw IllegalArgumentException("Unsupported component type for index: ${accessor.componentType}")
        }
    }

    private val vertexBuffers = mutableListOf<Deferred<ByteBuffer>>()
    private fun loadVertexBuffer(
        material: MaterialLoadInfo?,
        skinned: Boolean,
        attributes: Primitive.Attributes.Primitive,
    ): Int {
        val vertexFormat = material?.getVertexFormat(skinned) ?: BlazerodVertexFormats.POSITION_COLOR_TEXTURE
        val vertexBuffer = coroutineScope.async(dispatcher) {
            val vertices = attributes.position.count
            val stride = vertexFormat.vertexSize
            val buffer = ByteBuffer.allocateDirect(stride * vertices).order(ByteOrder.nativeOrder())

            for (element in vertexFormat.elements) {
                val dstOffset = vertexFormat.getOffset(element)

                when (element) {
                    VertexFormatElement.POSITION -> {
                        val srcAttribute = attributes.position
                        VertexLoadUtil.copyAttributeData(
                            vertices = vertices,
                            stride = stride,
                            element = element,
                            normalized = false,
                            srcAttribute = srcAttribute,
                            dstBuffer = buffer,
                            dstOffset = dstOffset,
                        )
                    }

                    VertexFormatElement.UV0 -> {
                        val srcAttribute = attributes.texcoords.firstOrNull() ?: continue
                        VertexLoadUtil.copyAttributeData(
                            vertices = vertices,
                            stride = stride,
                            element = element,
                            normalized = false,
                            srcAttribute = srcAttribute,
                            dstBuffer = buffer,
                            dstOffset = dstOffset,
                        )
                    }

                    VertexFormatElement.NORMAL -> {
                        val srcAttribute = attributes.normal ?: continue
                        VertexLoadUtil.copyAttributeData(
                            vertices = vertices,
                            stride = stride,
                            element = element,
                            normalized = true,
                            srcAttribute = srcAttribute,
                            dstBuffer = buffer,
                            dstOffset = dstOffset,
                        )
                    }

                    VertexFormatElement.COLOR -> {
                        val srcAttribute = attributes.colors.firstOrNull()
                        if (srcAttribute != null) {
                            VertexLoadUtil.copyAttributeData(
                                vertices = vertices,
                                stride = stride,
                                element = element,
                                normalized = true,
                                srcAttribute = srcAttribute,
                                dstBuffer = buffer,
                                dstOffset = dstOffset,
                            )
                        } else {
                            repeat(vertices) {
                                buffer.putInt(dstOffset + it * stride, 0xFFFFFFFF.toInt())
                            }
                        }
                    }

                    BlazerodVertexFormatElements.JOINT -> {
                        val srcAttribute = attributes.joints.firstOrNull() ?: continue
                        VertexLoadUtil.copyAttributeData(
                            vertices = vertices,
                            stride = stride,
                            element = element,
                            normalized = false,
                            srcAttribute = srcAttribute,
                            dstBuffer = buffer,
                            dstOffset = dstOffset,
                        )
                    }

                    BlazerodVertexFormatElements.WEIGHT -> {
                        val srcAttribute = attributes.weights.firstOrNull() ?: continue
                        VertexLoadUtil.copyAttributeData(
                            vertices = vertices,
                            stride = stride,
                            element = element,
                            normalized = true,
                            srcAttribute = srcAttribute,
                            dstBuffer = buffer,
                            dstOffset = dstOffset,
                        )
                    }

                    else -> {}
                }
            }

            buffer
        }
        val index = vertexBuffers.size
        vertexBuffers.add(vertexBuffer)
        return index
    }

    private var morphTargetInfos = mutableListOf<Deferred<MorphTargetsLoadData<MorphTargetsLoadData.TargetInfo>>>()

    @ConsistentCopyVisibility
    private data class BuildingTarget private constructor(
        val buffer: ByteBuffer,
        val itemStride: Int,
        val targetsCount: Int,
    ) {
        companion object {
            fun of(
                textureFormat: TextureFormat,
                itemCount: Int,
                targetsCount: Int,
            ) = BuildingTarget(
                buffer = ByteBuffer.allocateDirect(textureFormat.pixelSize() * itemCount * targetsCount)
                    .order(ByteOrder.nativeOrder()),
                itemStride = textureFormat.pixelSize(),
                targetsCount = targetsCount,
            )
        }

        fun toLoadData(): MorphTargetsLoadData.TargetInfo {
            buffer.position(0)
            return MorphTargetsLoadData.TargetInfo(
                buffer = buffer,
                itemStride = itemStride,
                targetsCount = targetsCount,
            )
        }
    }

    private fun loadMorphTargets(
        primitive: Primitive,
        weights: List<Float>? = null,
    ): Int {
        val verticesCount = primitive.attributes.position.count
        val targets = primitive.targets
        val loadedTargets = coroutineScope.async(dispatcher) {
            val positionTarget = BuildingTarget.of(
                textureFormat = TextureFormatExt.RGBA32F,
                itemCount = verticesCount,
                targetsCount = targets.count { it.position != null },
            )
            val colorTarget = BuildingTarget.of(
                textureFormat = TextureFormatExt.RGBA32F,
                itemCount = verticesCount,
                targetsCount = targets.count { it.colors.isNotEmpty() },
            )
            val texCoordTarget = BuildingTarget.of(
                textureFormat = TextureFormatExt.RG32F,
                itemCount = verticesCount,
                targetsCount = targets.count { it.texcoords.isNotEmpty() },
            )
            var posIndex = 0
            var colorIndex = 0
            var texCoordIndex = 0
            var posElements = 0
            val groups = mutableListOf<MorphTargetGroup>()
            for ((index, target) in targets.withIndex()) {
                val position = target.position?.let { position ->
                    position.read { input ->
                        positionTarget.buffer.position(posElements * 16)
                        positionTarget.buffer.put(input)
                        posElements++
                    }
                    posIndex++
                }
                val color = target.colors.firstOrNull()?.let { color ->
                    when (color.type) {
                        Accessor.AccessorType.VEC3 -> {
                            var index = 0
                            color.readNormalized { input ->
                                colorTarget.buffer.putFloat(input)
                                index++
                                if (index == 3) {
                                    // For padding
                                    colorTarget.buffer.putFloat(0f)
                                    index = 0
                                }
                            }
                        }

                        Accessor.AccessorType.VEC4 -> color.readNormalized {
                            colorTarget.buffer.putFloat(it)
                        }

                        else -> throw AssertionError("Bad morph target: accessor type of color is ${color.type}")
                    }
                    colorIndex++
                }
                val texCoord = target.texcoords.firstOrNull()?.let { texCoord ->
                    texCoord.readNormalized {
                        texCoordTarget.buffer.putFloat(it)
                    }
                    texCoordIndex++
                }
                groups.add(
                    MorphTargetGroup(
                        position = position,
                        color = color,
                        texCoord = texCoord,
                        weight = weights?.getOrNull(index) ?: 0f,
                    )
                )
            }
            MorphTargetsLoadData(
                targetGroups = groups,
                position = positionTarget.toLoadData(),
                color = colorTarget.toLoadData(),
                texCoord = texCoordTarget.toLoadData(),
            )
        }
        val targetIndex = morphTargetInfos.size
        morphTargetInfos.add(loadedTargets)
        return targetIndex
    }

    private val nodeToMorphedPrimitiveMap = mutableMapOf<NodeId, MutableList<Int>>()
    private val meshToMorphedPrimitiveMap = mutableMapOf<MeshId, MutableList<Int>>()
    private fun loadPrimitive(
        node: Node,
        mesh: Mesh,
        skinIndex: Int?,
        primitive: Primitive,
    ): PrimitiveLoadInfo? {
        val vertexFormatMode = when (primitive.mode) {
            Primitive.Mode.POINTS -> return null
            Primitive.Mode.LINE_STRIP -> VertexFormat.Mode.LINE_STRIP
            Primitive.Mode.LINE_LOOP -> return null
            Primitive.Mode.LINES -> VertexFormat.Mode.LINES
            Primitive.Mode.TRIANGLES -> VertexFormat.Mode.TRIANGLES
            Primitive.Mode.TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP
            Primitive.Mode.TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN
        }
        val skinned =
            skinIndex != null && primitive.attributes.joints.isNotEmpty() && primitive.attributes.weights.isNotEmpty()
        val morphed = primitive.targets.isNotEmpty()
        if (primitive.material?.baseColor?.a == 0f) {
            return null
        }
        val material = primitive.material?.let {
            loadMaterial(
                material = it,
                skinned = skinned,
                morphed = morphed,
            )
        }
        val morphedPrimitiveIndex = if (material?.morphed == true) {
            loadMorphTargets(primitive, mesh.weights)
        } else {
            null
        }
        if (morphedPrimitiveIndex != null) {
            meshToMorphedPrimitiveMap.getOrPut(mesh.id) { mutableListOf() }.add(morphedPrimitiveIndex)
            nodeToMorphedPrimitiveMap.getOrPut(node.id) { mutableListOf() }.add(morphedPrimitiveIndex)
        }
        return PrimitiveLoadInfo(
            vertices = primitive.attributes.position.count,
            vertexFormatMode = vertexFormatMode,
            materialInfo = material,
            indexBufferIndex = primitive.indices?.let { loadIndexBuffer(it).bufferIndex },
            vertexBufferIndex = loadVertexBuffer(
                material = material,
                skinned = skinned,
                attributes = primitive.attributes,
            ),
            skinIndex = skinIndex.takeIf { skinned },
            morphedPrimitiveIndex = morphedPrimitiveIndex,
        )
    }

    private val primitiveInfos = mutableListOf<PrimitiveLoadInfo>()
    private fun loadMesh(
        node: Node,
        mesh: Mesh,
        skinIndex: Int?,
    ) = mesh.primitives.mapNotNull { primitive ->
        val primitiveInfo = loadPrimitive(
            node = node,
            mesh = mesh,
            skinIndex = skinIndex,
            primitive = primitive,
        ) ?: return@mapNotNull null
        val index = primitiveInfos.size
        primitiveInfos.add(primitiveInfo)
        NodeLoadInfo.Component.Primitive(index)
    }

    private var ikCount = 0
    private var rigidBodyCount = 0
    private val rigidBodyIdToIndexMap = mutableMapOf<RigidBodyId, Int>()
    private val nodes = mutableListOf<NodeLoadInfo>()
    private fun loadNode(node: Node): Int {
        val skinJointData = skinJointsData[node.id]
        val node = NodeLoadInfo(
            nodeId = node.id,
            nodeName = node.name,
            humanoidTags = skinJointData?.mapNotNull { it.humanoidTag } ?: listOf(),
            transform = node.transform,
            components = buildList {
                skinJointData?.forEach { (skinIndex, jointIndex) ->
                    add(
                        NodeLoadInfo.Component.Joint(
                            skinIndex = skinIndex,
                            jointIndex = jointIndex,
                        )
                    )
                }
                node.components.forEach { component ->
                    when (component) {
                        is NodeComponent.MeshComponent -> {
                            val skin = node.meshIdToSkinMap[component.mesh.id]
                            val skinIndex = skin?.skin?.let { skin -> model.skins.indexOf(skin) }?.takeIf { it >= 0 }
                            addAll(
                                loadMesh(
                                    node = node,
                                    mesh = component.mesh,
                                    skinIndex = skinIndex,
                                )
                            )
                        }

                        is NodeComponent.CameraComponent -> {
                            add(
                                NodeLoadInfo.Component.Camera(
                                    camera = component.camera,
                                )
                            )
                        }

                        is NodeComponent.IkTargetComponent -> {
                            add(
                                NodeLoadInfo.Component.IkTarget(
                                    ikIndex = ikCount++,
                                    ikTarget = component.ikTarget,
                                    transformId = component.transformId,
                                )
                            )
                        }

                        is NodeComponent.InfluenceSourceComponent -> {
                            add(
                                NodeLoadInfo.Component.InfluenceSource(
                                    influence = component.influence,
                                    transformId = component.transformId,
                                )
                            )
                        }

                        is NodeComponent.RigidBodyComponent -> {
                            rigidBodyIdToIndexMap[component.rigidBodyId] = rigidBodyCount
                            add(
                                NodeLoadInfo.Component.RigidBody(
                                    rigidBodyIndex = rigidBodyCount++,
                                    rigidBody = component.rigidBody,
                                )
                            )
                        }

                        else -> {}
                    }
                }
            },
            childrenIndices = node.children.map { loadNode(it) },
        )
        val nodeIndex = nodes.size
        nodes.add(node)
        return nodeIndex
    }

    private fun loadExpressions(modelExpressions: List<Expression>): Pair<List<RenderExpression>, List<RenderExpressionGroup>> {
        val expressionTargetMap = mutableMapOf<Int, Int>()
        val expressions = buildList {
            for ((expressionIndex, expression) in modelExpressions.withIndex()) {
                if (expression !is Expression.Target) {
                    continue
                }
                val expression = RenderExpression(
                    name = expression.name,
                    tag = expression.tag,
                    isBinary = expression.isBinary,
                    bindings = expression.bindings.flatMap {
                        when (it) {
                            is Expression.Target.Binding.MeshMorphTarget -> {
                                meshToMorphedPrimitiveMap[it.meshId]?.map { index ->
                                    RenderExpression.Binding.MorphTarget(
                                        morphedPrimitiveIndex = index,
                                        groupIndex = it.index,
                                        weight = it.weight,
                                    )
                                } ?: listOf()
                            }

                            is Expression.Target.Binding.NodeMorphTarget -> {
                                nodeToMorphedPrimitiveMap[it.nodeId]?.map { index ->
                                    RenderExpression.Binding.MorphTarget(
                                        morphedPrimitiveIndex = index,
                                        groupIndex = it.index,
                                        weight = it.weight,
                                    )
                                } ?: listOf()
                            }
                        }
                    }
                )
                if (expression.bindings.isEmpty()) {
                    continue
                }
                expressionTargetMap[expressionIndex] = this.size
                add(expression)
            }
        }
        val expressionGroups = modelExpressions.mapNotNull { expression ->
            if (expression !is Expression.Group) {
                return@mapNotNull null
            }
            RenderExpressionGroup(
                name = expression.name,
                tag = expression.tag,
                items = expression.targets.mapNotNull {
                    val targetIndex =
                        modelExpressions.indexOf(it.target).takeIf { index -> index != -1 } ?: return@mapNotNull null
                    RenderExpressionGroup.Item(
                        expressionIndex = expressionTargetMap[targetIndex] ?: return@mapNotNull null,
                        influence = it.influence,
                    )
                }
            )
        }
        return Pair(expressions, expressionGroups)
    }

    private fun loadPhysicalJoints(modelPhysicalJoints: List<PhysicalJoint>) =
        modelPhysicalJoints.mapIndexedNotNull { index, joint ->
            val rigidBodyAIndex = rigidBodyIdToIndexMap[joint.rigidBodyA] ?: return@mapIndexedNotNull null
            val rigidBodyBIndex = rigidBodyIdToIndexMap[joint.rigidBodyB] ?: return@mapIndexedNotNull null

            val position = joint.position
            val rotation = joint.rotation
            val positionMin = joint.positionMin
            val positionMax = joint.positionMax
            val rotationMin = joint.rotationMin
            val rotationMax = joint.rotationMax
            val positionSpring = joint.positionSpring
            val rotationSpring = joint.rotationSpring

            RenderPhysicsJoint(
                name = joint.name,
                type = joint.type,
                rigidBodyAIndex = rigidBodyAIndex,
                rigidBodyBIndex = rigidBodyBIndex,
                position = position,
                rotation = rotation,
                positionMin = positionMin,
                positionMax = positionMax,
                rotationMin = rotationMin,
                rotationMax = rotationMax,
                positionSpring = positionSpring,
                rotationSpring = rotationSpring,
            )
        }

    private fun loadScene(scene: Scene, expressions: List<Expression>): PreProcessModelLoadInfo {
        val rootNode = NodeLoadInfo(
            nodeId = null,
            nodeName = "Root node",
            humanoidTags = listOf(),
            transform = null,
            components = listOf(),
            childrenIndices = scene.nodes.map { loadNode(it) },
        )
        val rootNodeIndex = nodes.size
        nodes.add(rootNode)
        val (expressions, expressionGroups) = loadExpressions(expressions)
        val physicalJoints = loadPhysicalJoints(model.physicalJoints)
        return PreProcessModelLoadInfo(
            textures = textures,
            indexBuffers = indexBuffers,
            vertexBuffers = vertexBuffers,
            primitiveInfos = primitiveInfos,
            nodes = nodes,
            rootNodeIndex = rootNodeIndex,
            skins = skinsList,
            morphTargetInfos = morphTargetInfos,
            expressions = expressions,
            expressionGroups = expressionGroups,
            physicalJoints = physicalJoints,
            renderTransform = scene.transform,
        )
    }

    private fun loadModel(): PreProcessModelLoadInfo? {
        loadSkins()
        val scene = model.defaultScene ?: model.scenes.firstOrNull() ?: return null
        return loadScene(scene, model.expressions)
    }

    companion object {
        fun preprocess(
            scope: CoroutineScope,
            loadDispatcher: CoroutineDispatcher,
            model: Model,
        ) = ModelPreprocessor(scope, loadDispatcher, model).loadModel()
    }
}
