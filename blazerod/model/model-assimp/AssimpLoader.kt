package top.fifthlight.blazerod.model.assimp

import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.floats.FloatList
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.assimp.*
import org.lwjgl.system.MemoryUtil
import org.slf4j.LoggerFactory
import top.fifthlight.blazerod.model.*
import top.fifthlight.blazerod.model.animation.*
import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.LoadParam
import top.fifthlight.blazerod.model.loader.LoadResult
import top.fifthlight.blazerod.model.loader.ModelFileLoader
import top.fifthlight.blazerod.model.loader.util.readToBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.extension
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

class AssimpLoadException(message: String) : Exception(message)

private fun Path.read() = FileChannel.open(this, StandardOpenOption.READ).use { channel ->
    channel.readToBuffer(readSizeLimit = 256 * 1024 * 1024)
}

class AssimpLoader : ModelFileLoader {
    companion object {
        private val logger = LoggerFactory.getLogger(AssimpLoader::class.java)
    }

    override fun initialize() {
        try {
            Assimp.getLibrary()
            available = true
        } catch (ex: ExceptionInInitializerError) {
            logger.warn("Failed to load assimp library", ex)
        } catch (ex: NoClassDefFoundError) {
            logger.warn("Assimp library not available", ex)
        }
    }

    override var available: Boolean = false
        private set

    override val extensions: Map<String, Set<ModelFileLoader.Ability>> = mapOf(
        "3ds" to setOf(ModelFileLoader.Ability.MODEL),
        "ac" to setOf(ModelFileLoader.Ability.MODEL),
        "ase" to setOf(ModelFileLoader.Ability.MODEL),
        "bvh" to setOf(ModelFileLoader.Ability.MODEL),
        "cob" to setOf(ModelFileLoader.Ability.MODEL),
        "csm" to setOf(ModelFileLoader.Ability.MODEL),
        "dae" to setOf(ModelFileLoader.Ability.MODEL),
        "dxf" to setOf(ModelFileLoader.Ability.MODEL),
        "dxf" to setOf(ModelFileLoader.Ability.MODEL),
        "fbx" to setOf(ModelFileLoader.Ability.MODEL),
        "hmp" to setOf(ModelFileLoader.Ability.MODEL),
        "ifc" to setOf(ModelFileLoader.Ability.MODEL),
        "irr" to setOf(ModelFileLoader.Ability.MODEL),
        "irrmesh" to setOf(ModelFileLoader.Ability.MODEL),
        "lwo" to setOf(ModelFileLoader.Ability.MODEL),
        "lws" to setOf(ModelFileLoader.Ability.MODEL),
        "lxo" to setOf(ModelFileLoader.Ability.MODEL),
        "md2" to setOf(ModelFileLoader.Ability.MODEL),
        "md3" to setOf(ModelFileLoader.Ability.MODEL),
        "md5mesh" to setOf(ModelFileLoader.Ability.MODEL),
        "mdc" to setOf(ModelFileLoader.Ability.MODEL),
        "mdl" to setOf(ModelFileLoader.Ability.MODEL),
        "mdl" to setOf(ModelFileLoader.Ability.MODEL),
        "mesh" to setOf(ModelFileLoader.Ability.MODEL),
        "ms3d" to setOf(ModelFileLoader.Ability.MODEL),
        "nff" to setOf(ModelFileLoader.Ability.MODEL),
        "obj" to setOf(ModelFileLoader.Ability.MODEL),
        "off" to setOf(ModelFileLoader.Ability.MODEL),
        "pk3" to setOf(ModelFileLoader.Ability.MODEL),
        "ply" to setOf(ModelFileLoader.Ability.MODEL),
        "ply" to setOf(ModelFileLoader.Ability.MODEL),
        "q3o" to setOf(ModelFileLoader.Ability.MODEL),
        "q3s" to setOf(ModelFileLoader.Ability.MODEL),
        "raw" to setOf(ModelFileLoader.Ability.MODEL),
        "scn" to setOf(ModelFileLoader.Ability.MODEL),
        "smd" to setOf(ModelFileLoader.Ability.MODEL),
        "stl" to setOf(ModelFileLoader.Ability.MODEL),
        "ter" to setOf(ModelFileLoader.Ability.MODEL),
        "usd" to setOf(ModelFileLoader.Ability.MODEL),
        "x" to setOf(ModelFileLoader.Ability.MODEL, ModelFileLoader.Ability.EMBED_ANIMATION),
        "xml" to setOf(ModelFileLoader.Ability.MODEL),
    )

    override val probeLength: Int? = null
    override fun probe(buffer: ByteBuffer) = false

    private class Context(
        private val scene: AIScene,
        private val context: LoadContext,
        private val param: LoadParam,
    ) : AutoCloseable {
        private val modelUuid = UUID.randomUUID()
        private val skins = mutableListOf<MeshSkinInfo>()
        private var nextNodeIndex = 0
        private val nodeIdMap = mutableMapOf<Long, NodeId>()
        private val meshToSkins = mutableMapOf<Int, Int>()
        private val textures = mutableMapOf<String, Optional<Texture>>()
        private lateinit var materials: List<Material>
        private lateinit var meshes: List<Mesh>

        @Suppress("UNCHECKED_CAST")
        private val keyFrameSplit: List<Pair<String, Int>> =
            param.loaderParams["keyFrameSplit"] as? List<Pair<String, Int>> ?: listOf()

        private val useVanillaMaterial: Boolean =
            param.loaderParams["useVanillaMaterial"] as? Boolean ?: false

        @Suppress("UNCHECKED_CAST")
        private val diffuseTextureOverride: Map<Int, String> =
            param.loaderParams["diffuseTextureOverride"] as? Map<Int, String> ?: mapOf()

        companion object {
            private val EMPTY_LOAD_RESULT = LoadResult(
                metadata = null,
                model = null,
                animations = listOf(),
            )
        }

        override fun close() {
            Assimp.aiReleaseImport(scene)
        }

        private fun allocateNodeId(node: AINode) = nodeIdMap.getOrPut(node.address()) {
            NodeId(modelUuid, nextNodeIndex++)
        }

        private data class MeshSkinInfo(
            val skin: Skin,
            val nodeToJointIndexMap: Map<Long, Int>,
        )

        private fun loadSkins() {
            val meshes = scene.mMeshes() ?: return
            repeat(scene.mNumMeshes()) { meshIndex ->
                val mesh = AIMesh.create(meshes.get(meshIndex))
                val bonesCount = mesh.mNumBones()
                if (bonesCount == 0) {
                    return@repeat
                }
                val bones = mesh.mBones() ?: return@repeat

                val jointNodeIds = mutableListOf<NodeId>()
                val inverseBindMatrices = mutableListOf<Matrix4f>()
                val nodeToJointIndexMap = mutableMapOf<Long, Int>()
                val jointHumanoidTags = mutableListOf<HumanoidTag?>()
                repeat(bonesCount) { boneIndex ->
                    val bone = AIBone.create(bones.get(boneIndex))
                    val node = bone.mNode()
                    val nodeId = allocateNodeId(node)
                    nodeToJointIndexMap[node.address()] = jointNodeIds.size
                    jointNodeIds += nodeId
                    inverseBindMatrices += bone.mOffsetMatrix().toJoml()
                    jointHumanoidTags += guessHumanoidTagFromName(node.mName().dataString())
                }
                meshToSkins[meshIndex] = skins.size
                skins += MeshSkinInfo(
                    skin = Skin(
                        name = "Skin for #${meshIndex}",
                        joints = jointNodeIds,
                        inverseBindMatrices = inverseBindMatrices,
                        jointHumanoidTags = jointHumanoidTags,
                    ),
                    nodeToJointIndexMap = nodeToJointIndexMap,
                )
            }
        }

        private fun loadTexture(pathString: String) = textures.getOrPut(pathString) {
            try {
                val buffer =
                    context.loadExternalResource(pathString, LoadContext.ResourceType.TEXTURE, true, 256 * 1024 * 1024)
                Optional.of(
                    Texture(
                        name = pathString,
                        bufferView = BufferView(
                            buffer = Buffer(
                                name = "Texture $pathString",
                                buffer = buffer,
                            ),
                            byteLength = buffer.remaining(),
                            byteOffset = 0,
                            byteStride = 0,
                        ),
                        sampler = Texture.Sampler(
                            magFilter = param.samplerMagFilter ?: Texture.Sampler.MagFilter.LINEAR,
                            minFilter = param.samplerMinFilter ?: Texture.Sampler.MinFilter.LINEAR,
                        ),
                    )
                )
            } catch (ex: Exception) {
                logger.warn("Failed to load texture", ex)
                return Optional.empty<Texture>()
            }
        }

        private fun loadMaterials() {
            val materials = scene.mMaterials() ?: run {
                materials = listOf()
                return
            }
            this.materials = (0 until scene.mNumMaterials()).map { index ->
                val materialPtr = materials[index]
                val material = AIMaterial.create(materialPtr)

                val diffuseColor = material.getColor(Assimp.AI_MATKEY_COLOR_DIFFUSE, 0, 0)
                val diffuseTexture = diffuseTextureOverride[index] ?: material.getTexturePath(Assimp.aiTextureType_DIFFUSE, 0)

                if (useVanillaMaterial) {
                    Material.Vanilla(
                        name = "Material #${index}",
                        baseColor = diffuseColor ?: RgbaColor(1f, 1f, 1f, 1f),
                        baseColorTexture = diffuseTexture
                            ?.let { loadTexture(it).getOrNull() }
                            ?.let { texture ->
                                Material.TextureInfo(
                                    texture = texture,
                                )
                            },
                    )
                } else {
                    Material.Unlit(
                        name = "Material #${index}",
                        baseColor = diffuseColor ?: RgbaColor(1f, 1f, 1f, 1f),
                        baseColorTexture = diffuseTexture
                            ?.let { loadTexture(it).getOrNull() }
                            ?.let { texture ->
                                Material.TextureInfo(
                                    texture = texture,
                                )
                            },
                    )
                }
            }
        }

        private fun loadMeshes() {
            val meshes = scene.mMeshes() ?: run {
                meshes = listOf()
                return
            }
            this.meshes = (0 until scene.mNumMeshes()).map { index ->
                val meshPtr = meshes[index]
                val mesh = AIMesh.create(meshPtr)

                /*
                    struct aiVector3D {
                        float x;
                        float y;
                        float z;
                    }
                    Used for vertices and normals
                */
                val vertices = mesh.mVertices()
                val verticesLength = mesh.mNumVertices() * vertices.sizeof()
                val vertexBuffer = ByteBuffer.allocateDirect(verticesLength).order(ByteOrder.nativeOrder())
                MemoryUtil.memCopy(vertices.address(), MemoryUtil.memAddress(vertexBuffer), verticesLength.toLong())

                val normalBuffer = run {
                    val normals = mesh.mNormals() ?: return@run null
                    val normalsLength = mesh.mNumVertices() * normals.sizeof()
                    val normalBuffer = ByteBuffer.allocateDirect(normalsLength).order(ByteOrder.nativeOrder())
                    MemoryUtil.memCopy(normals.address(), MemoryUtil.memAddress(normalBuffer), normalsLength.toLong())
                    normalBuffer
                }

                val texCoordBuffer = run {
                    // This buffer is Vector3! we need to convert it to vec2
                    val texCoord = mesh.mTextureCoords(0) ?: return@run null
                    val texCoordLength = mesh.mNumVertices() * 8
                    val texCoordBuffer = ByteBuffer.allocateDirect(texCoordLength).order(ByteOrder.nativeOrder())
                    val bufferAddress = MemoryUtil.memAddress(texCoordBuffer)
                    repeat(mesh.mNumVertices()) { vertexIndex ->
                        MemoryUtil.memCopy(texCoord.address(vertexIndex), bufferAddress + vertexIndex * 8, 8L)
                    }
                    texCoordBuffer
                }

                val skin = meshToSkins[index]?.let { skins.getOrNull(it) }
                val bonesCount = mesh.mNumBones()
                val bones = mesh.mBones()
                var jointsBuffer: ByteBuffer? = null
                var weightsBuffer: ByteBuffer? = null
                if (skin != null && bonesCount != 0 && bones != null) {
                    val maxBonesPerVertex = 4
                    jointsBuffer = ByteBuffer.allocateDirect(mesh.mNumVertices() * maxBonesPerVertex * 2)
                        .order(ByteOrder.nativeOrder())
                    weightsBuffer = ByteBuffer.allocateDirect(mesh.mNumVertices() * maxBonesPerVertex * 4)
                        .order(ByteOrder.nativeOrder())
                    val boneInfluenceCount = IntArray(mesh.mNumVertices())
                    repeat(bonesCount) { boneIndex ->
                        val bone = AIBone.create(bones.get(boneIndex))
                        val node = bone.mNode()
                        val jointIndex = skin.nodeToJointIndexMap[node.address()] ?: return@repeat
                        val weights = bone.mWeights()
                        val weightsCount = bone.mNumWeights()
                        if (weightsCount != 0 && weights != null) {
                            repeat(weightsCount) { weightIndex ->
                                val vertexWeightAddress = weights.address(weightIndex)
                                val vertexId = AIVertexWeight.nmVertexId(vertexWeightAddress)
                                val weight = AIVertexWeight.nmWeight(vertexWeightAddress)

                                val jointVertexIndex = boneInfluenceCount[vertexId]
                                if (jointVertexIndex < maxBonesPerVertex) {
                                    jointsBuffer.putShort(
                                        (vertexId * maxBonesPerVertex + jointVertexIndex) * 2,
                                        jointIndex.toShort()
                                    )
                                    weightsBuffer.putFloat(
                                        (vertexId * maxBonesPerVertex + jointVertexIndex) * 4,
                                        weight
                                    )
                                    boneInfluenceCount[vertexId]++
                                }
                            }
                        }
                    }
                }

                val faces = mesh.mFaces()
                val facesCount = mesh.mNumFaces()
                val indexSize = facesCount * 12
                val indexBuffer = ByteBuffer.allocateDirect(indexSize).order(ByteOrder.nativeOrder())
                val indexAddress = MemoryUtil.memAddress(indexBuffer)
                var indexOffset = 0
                repeat(facesCount) {
                    /*
                        struct aiFace {
                            unsigned int mNumIndices;
                            unsigned int * mIndices;
                        }
                     */
                    // AVOID OBJECT ALLOCATION!
                    val faceAddress = faces.address(it)
                    if (faceAddress == 0L) {
                        throw NullPointerException("Face #$it of mesh $index is null")
                    }
                    val numIndices = AIFace.nmNumIndices(faceAddress)

                    val indexArrayAddress = MemoryUtil.memGetAddress(faceAddress + AIFace.MINDICES)
                    if (indexArrayAddress == 0L) {
                        throw NullPointerException("Index array of face $it in mesh $index is null")
                    }
                    val indexDataLength = numIndices * 4
                    Objects.checkFromIndexSize(indexOffset, indexDataLength, indexSize)
                    MemoryUtil.memCopy(indexArrayAddress, indexAddress + indexOffset, indexDataLength.toLong())
                    indexOffset += indexDataLength
                }

                val primitive = Primitive(
                    mode = Primitive.Mode.TRIANGLES,
                    material = materials.getOrNull(mesh.mMaterialIndex())
                        ?: throw AssimpLoadException("No material #${mesh.mMaterialIndex()} for mesh #${index}"),
                    attributes = Primitive.Attributes.Primitive(
                        position = Accessor(
                            bufferView = BufferView(
                                buffer = Buffer(
                                    name = "Position",
                                    buffer = vertexBuffer,
                                ),
                                byteLength = verticesLength,
                                byteOffset = 0,
                                byteStride = 0,
                            ),
                            componentType = Accessor.ComponentType.FLOAT,
                            type = Accessor.AccessorType.VEC3,
                            count = mesh.mNumVertices(),
                        ),
                        normal = normalBuffer?.let {
                            Accessor(
                                bufferView = BufferView(
                                    buffer = Buffer(
                                        name = "Normal",
                                        buffer = it,
                                    ),
                                    byteLength = normalBuffer.remaining(),
                                    byteOffset = 0,
                                    byteStride = 0,
                                ),
                                componentType = Accessor.ComponentType.FLOAT,
                                type = Accessor.AccessorType.VEC3,
                                count = mesh.mNumVertices(),
                            )
                        },
                        texcoords = texCoordBuffer?.let {
                            listOf(
                                Accessor(
                                    bufferView = BufferView(
                                        buffer = Buffer(
                                            name = "TexCoord",
                                            buffer = it,
                                        ),
                                        byteLength = texCoordBuffer.remaining(),
                                        byteOffset = 0,
                                        byteStride = 0,
                                    ),
                                    componentType = Accessor.ComponentType.FLOAT,
                                    type = Accessor.AccessorType.VEC2,
                                    count = mesh.mNumVertices(),
                                )
                            )
                        } ?: listOf(),
                        joints = jointsBuffer?.let {
                            listOf(
                                Accessor(
                                    bufferView = BufferView(
                                        buffer = Buffer(
                                            name = "Joints",
                                            buffer = jointsBuffer,
                                        ),
                                        byteLength = jointsBuffer.remaining(),
                                        byteOffset = 0,
                                        byteStride = 0,
                                    ),
                                    componentType = Accessor.ComponentType.SHORT,
                                    type = Accessor.AccessorType.VEC4,
                                    count = mesh.mNumVertices(),
                                )
                            )
                        } ?: listOf(),
                        weights = weightsBuffer?.let {
                            listOf(
                                Accessor(
                                    bufferView = BufferView(
                                        buffer = Buffer(
                                            name = "Weights",
                                            buffer = weightsBuffer,
                                        ),
                                        byteLength = weightsBuffer.remaining(),
                                        byteOffset = 0,
                                        byteStride = 0,
                                    ),
                                    componentType = Accessor.ComponentType.FLOAT,
                                    type = Accessor.AccessorType.VEC4,
                                    count = mesh.mNumVertices(),
                                )
                            )
                        } ?: listOf(),
                    ),
                    indices = Accessor(
                        bufferView = BufferView(
                            buffer = Buffer(
                                name = "Indices",
                                buffer = indexBuffer,
                            ),
                            byteLength = indexSize,
                            byteOffset = 0,
                            byteStride = 0,
                        ),
                        componentType = Accessor.ComponentType.UNSIGNED_INT,
                        type = Accessor.AccessorType.SCALAR,
                        count = mesh.mNumFaces() * 3,
                    ),
                    targets = listOf(),
                )

                Mesh(
                    id = MeshId(modelUuid, index),
                    primitives = listOf(primitive),
                    weights = null,
                )
            }
        }

        private data class ChannelPart(
            var positionChannel: Pair<FloatList, FloatList>? = null,
            var scaleChannel: Pair<FloatList, FloatList>? = null,
            var rotationChannel: Pair<FloatList, FloatList>? = null,
        )

        private fun loadAnimations(): List<Animation> = scene.mAnimations()?.let { animations ->
            if (scene.mNumAnimations() == 1 && keyFrameSplit.isNotEmpty()) {
                val animation = AIAnimation.create(animations.get(0))
                val ticksPerSecond = animation.mTicksPerSecond().takeIf { it > 0f }?.toFloat() ?: 30f
                val animationChannels = mutableListOf<MutableList<KeyFrameAnimationChannel<*, *>>>()
                val nodeAnimations = animation.mChannels() ?: return@let emptyList()
                (0 until animation.mNumChannels()).forEach { channelIndex ->
                    val nodeAnimPtr = nodeAnimations[channelIndex]
                    val nodeAnimation = AINodeAnim.create(nodeAnimPtr)

                    val channelParts = mutableListOf<ChannelPart>()

                    run {
                        var currentAnimationPart = 0
                        var currentAnimationStartPoint = 0
                        var currentAnimationEndPoint = keyFrameSplit.first().second
                        nodeAnimation.mPositionKeys()?.let { keys ->
                            val numKeys = nodeAnimation.mNumPositionKeys()
                            for (i in 0 until numKeys) {
                                val time =
                                    AIVectorKey.nmTime(keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MTIME)
                                if (time > currentAnimationEndPoint) {
                                    currentAnimationStartPoint += keyFrameSplit[currentAnimationPart].second
                                    currentAnimationPart++
                                    if (currentAnimationPart in keyFrameSplit.indices) {
                                        currentAnimationEndPoint += keyFrameSplit[currentAnimationPart].second
                                    } else {
                                        break
                                    }
                                }
                                while (channelParts.size <= currentAnimationPart) {
                                    channelParts += ChannelPart()
                                }

                                val valueAddress = keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MVALUE
                                val timeInSeconds = (time - currentAnimationStartPoint) / ticksPerSecond.toDouble()
                                val (timeList, valueList) = channelParts[currentAnimationPart].positionChannel
                                    ?: Pair(FloatArrayList(), FloatArrayList()).also {
                                        channelParts[currentAnimationPart].positionChannel = it
                                    }
                                timeList.add(timeInSeconds.toFloat())
                                valueList.add(AIVector3D.nx(valueAddress))
                                valueList.add(AIVector3D.ny(valueAddress))
                                valueList.add(AIVector3D.nz(valueAddress))
                            }
                        }
                    }

                    run {
                        var currentAnimationPart = 0
                        var currentAnimationStartPoint = 0
                        var currentAnimationEndPoint = keyFrameSplit.first().second
                        nodeAnimation.mScalingKeys()?.let { keys ->
                            val numKeys = nodeAnimation.mNumScalingKeys()
                            for (i in 0 until numKeys) {
                                val time =
                                    AIVectorKey.nmTime(keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MTIME)
                                if (time > currentAnimationEndPoint) {
                                    currentAnimationStartPoint += keyFrameSplit[currentAnimationPart].second
                                    currentAnimationPart++
                                    if (currentAnimationPart in keyFrameSplit.indices) {
                                        currentAnimationEndPoint += keyFrameSplit[currentAnimationPart].second
                                    } else {
                                        break
                                    }
                                }
                                while (channelParts.size <= currentAnimationPart) {
                                    channelParts += ChannelPart()
                                }

                                val valueAddress = keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MVALUE
                                val timeInSeconds = (time - currentAnimationStartPoint) / ticksPerSecond.toDouble()
                                val (timeList, valueList) = channelParts[currentAnimationPart].scaleChannel
                                    ?: Pair(FloatArrayList(), FloatArrayList()).also {
                                        channelParts[currentAnimationPart].scaleChannel = it
                                    }
                                timeList.add(timeInSeconds.toFloat())
                                valueList.add(AIVector3D.nx(valueAddress))
                                valueList.add(AIVector3D.ny(valueAddress))
                                valueList.add(AIVector3D.nz(valueAddress))
                            }
                        }
                    }

                    run {
                        var currentAnimationPart = 0
                        var currentAnimationStartPoint = 0
                        var currentAnimationEndPoint = keyFrameSplit.first().second
                        nodeAnimation.mRotationKeys()?.let { keys ->
                            val numKeys = nodeAnimation.mNumRotationKeys()
                            for (i in 0 until numKeys) {
                                val time =
                                    AIQuatKey.nmTime(keys.address() + i * AIQuatKey.SIZEOF + AIQuatKey.MTIME)
                                if (time > currentAnimationEndPoint) {
                                    currentAnimationStartPoint += keyFrameSplit[currentAnimationPart].second
                                    currentAnimationPart++
                                    if (currentAnimationPart in keyFrameSplit.indices) {
                                        currentAnimationEndPoint += keyFrameSplit[currentAnimationPart].second
                                    } else {
                                        break
                                    }
                                }
                                while (channelParts.size <= currentAnimationPart) {
                                    channelParts += ChannelPart()
                                }

                                val valueAddress = keys.address() + i * AIQuatKey.SIZEOF + AIQuatKey.MVALUE
                                val timeInSeconds = (time - currentAnimationStartPoint) / ticksPerSecond.toDouble()
                                val (timeList, valueList) = channelParts[currentAnimationPart].rotationChannel
                                    ?: Pair(FloatArrayList(), FloatArrayList()).also {
                                        channelParts[currentAnimationPart].rotationChannel = it
                                    }
                                timeList.add(timeInSeconds.toFloat())
                                valueList.add(AIQuaternion.nx(valueAddress))
                                valueList.add(AIQuaternion.ny(valueAddress))
                                valueList.add(AIQuaternion.nz(valueAddress))
                                valueList.add(AIQuaternion.nw(valueAddress))
                            }
                        }
                    }

                    channelParts.forEachIndexed { animationIndex, part ->
                        while (animationChannels.size <= animationIndex) {
                            animationChannels += mutableListOf<KeyFrameAnimationChannel<*, *>>()
                        }
                        val channels = animationChannels[animationIndex]
                        part.positionChannel?.let { (times, values) ->
                            channels.add(
                                KeyFrameAnimationChannel(
                                    type = AnimationChannel.Type.Translation,
                                    typeData = AnimationChannel.Type.TransformData(
                                        node = AnimationChannel.Type.NodeData(
                                            targetNode = null,
                                            targetNodeName = nodeAnimation.mNodeName().dataString(),
                                            targetHumanoidTag = null,
                                        ),
                                        transformId = TransformId.ABSOLUTE,
                                    ),
                                    indexer = ListAnimationKeyFrameIndexer(times),
                                    keyframeData = AnimationKeyFrameData.ofVector3f(values, 1),
                                    interpolation = AnimationInterpolation.linear,
                                )
                            )
                        }
                        part.scaleChannel?.let { (times, values) ->
                            channels.add(
                                KeyFrameAnimationChannel(
                                    type = AnimationChannel.Type.Scale,
                                    typeData = AnimationChannel.Type.TransformData(
                                        node = AnimationChannel.Type.NodeData(
                                            targetNode = null,
                                            targetNodeName = nodeAnimation.mNodeName().dataString(),
                                            targetHumanoidTag = null,
                                        ),
                                        transformId = TransformId.ABSOLUTE,
                                    ),
                                    indexer = ListAnimationKeyFrameIndexer(times),
                                    keyframeData = AnimationKeyFrameData.ofVector3f(values, 1),
                                    interpolation = AnimationInterpolation.linear,
                                )
                            )
                        }
                        part.rotationChannel?.let { (times, values) ->
                            channels.add(
                                KeyFrameAnimationChannel(
                                    type = AnimationChannel.Type.Rotation,
                                    typeData = AnimationChannel.Type.TransformData(
                                        node = AnimationChannel.Type.NodeData(
                                            targetNode = null,
                                            targetNodeName = nodeAnimation.mNodeName().dataString(),
                                            targetHumanoidTag = null,
                                        ),
                                        transformId = TransformId.ABSOLUTE,
                                    ),
                                    indexer = ListAnimationKeyFrameIndexer(times),
                                    keyframeData = AnimationKeyFrameData.ofQuaternionf(values, 1),
                                    interpolation = AnimationInterpolation.linear,
                                )
                            )
                        }
                    }
                }
                animationChannels.mapIndexedNotNull { index, channels ->
                    val animationName = (keyFrameSplit.getOrNull(index) ?: return@mapIndexedNotNull null).first
                    SimpleAnimation(
                        name = animationName.takeIf { it.isNotEmpty() },
                        channels = channels,
                    )
                }
            } else {
                (0 until scene.mNumAnimations()).map { animIndex ->
                    val animation = AIAnimation.create(animations.get(animIndex))
                    val ticksPerSecond = animation.mTicksPerSecond().takeIf { it > 0f }?.toFloat() ?: 30f
                    val channels = buildList {
                        val nodeAnimations = animation.mChannels()
                        if (nodeAnimations != null) {
                            (0 until animation.mNumChannels()).forEach { channelIndex ->
                                val nodeAnimPtr = nodeAnimations[channelIndex]
                                val nodeAnimation = AINodeAnim.create(nodeAnimPtr)
                                nodeAnimation.mPositionKeys()?.let { keys ->
                                    add(
                                        loadVector3AnimationChannel(
                                            nodeAnimation = nodeAnimation,
                                            keys = keys,
                                            numKeys = nodeAnimation.mNumPositionKeys(),
                                            channelType = AnimationChannel.Type.Translation,
                                            ticksPerSecond = ticksPerSecond,
                                        )
                                    )
                                }

                                nodeAnimation.mScalingKeys()?.let { keys ->
                                    add(
                                        loadVector3AnimationChannel(
                                            nodeAnimation = nodeAnimation,
                                            keys = keys,
                                            numKeys = nodeAnimation.mNumScalingKeys(),
                                            channelType = AnimationChannel.Type.Scale,
                                            ticksPerSecond = ticksPerSecond,
                                        )
                                    )
                                }

                                nodeAnimation.mRotationKeys()?.let { keys ->
                                    add(
                                        loadQuaternionfAnimationChannel(
                                            nodeAnimation = nodeAnimation,
                                            keys = keys,
                                            numKeys = nodeAnimation.mNumRotationKeys(),
                                            channelType = AnimationChannel.Type.Rotation,
                                            ticksPerSecond = ticksPerSecond,
                                        )
                                    )
                                }
                            }
                        }
                    }

                    SimpleAnimation(
                        name = animation.mName().dataString().takeIf { it.isNotEmpty() },
                        channels = channels,
                    )
                }
            }
        } ?: listOf()

        private fun loadVector3AnimationChannel(
            nodeAnimation: AINodeAnim,
            keys: AIVectorKey.Buffer,
            numKeys: Int,
            ticksPerSecond: Float,
            channelType: AnimationChannel.Type<Vector3f, AnimationChannel.Type.TransformData>,
        ): KeyFrameAnimationChannel<Vector3f, AnimationChannel.Type.TransformData> {
            val times = FloatArrayList(numKeys)
            val values = FloatArrayList(numKeys * 3)

            repeat(numKeys) { i ->
                val time = AIVectorKey.nmTime(keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MTIME)
                val timeInSeconds = time / ticksPerSecond.toDouble()
                val valueAddress = keys.address() + i * AIVectorKey.SIZEOF + AIVectorKey.MVALUE

                times.add(timeInSeconds.toFloat())
                values.add(AIVector3D.nx(valueAddress))
                values.add(AIVector3D.ny(valueAddress))
                values.add(AIVector3D.nz(valueAddress))
            }

            return KeyFrameAnimationChannel(
                type = channelType,
                typeData = AnimationChannel.Type.TransformData(
                    node = AnimationChannel.Type.NodeData(
                        targetNode = null,
                        targetNodeName = nodeAnimation.mNodeName().dataString(),
                        targetHumanoidTag = null,
                    ),
                    transformId = TransformId.ABSOLUTE,
                ),
                indexer = ListAnimationKeyFrameIndexer(times),
                keyframeData = AnimationKeyFrameData.ofVector3f(values, 1),
                interpolation = AnimationInterpolation.linear,
            )
        }

        private fun loadQuaternionfAnimationChannel(
            nodeAnimation: AINodeAnim,
            keys: AIQuatKey.Buffer,
            numKeys: Int,
            ticksPerSecond: Float,
            channelType: AnimationChannel.Type<Quaternionf, AnimationChannel.Type.TransformData>,
        ): KeyFrameAnimationChannel<Quaternionf, AnimationChannel.Type.TransformData> {
            val times = FloatArrayList(numKeys)
            val values = FloatArrayList(numKeys * 4)

            repeat(numKeys) { i ->
                val time = AIQuatKey.nmTime(keys.address() + i * AIQuatKey.SIZEOF + AIQuatKey.MTIME)
                val timeInSeconds = time / ticksPerSecond.toDouble()
                val valueAddress = keys.address() + i * AIQuatKey.SIZEOF + AIQuatKey.MVALUE

                times.add(timeInSeconds.toFloat())
                values.add(AIQuaternion.nx(valueAddress))
                values.add(AIQuaternion.ny(valueAddress))
                values.add(AIQuaternion.nz(valueAddress))
                values.add(AIQuaternion.nw(valueAddress))
            }

            return KeyFrameAnimationChannel(
                type = channelType,
                typeData = AnimationChannel.Type.TransformData(
                    node = AnimationChannel.Type.NodeData(
                        targetNode = null,
                        targetNodeName = nodeAnimation.mNodeName().dataString(),
                        targetHumanoidTag = null,
                    ),
                    transformId = TransformId.ABSOLUTE,
                ),
                indexer = ListAnimationKeyFrameIndexer(times),
                keyframeData = AnimationKeyFrameData.ofQuaternionf(values, 1),
                interpolation = AnimationInterpolation.linear,
            )
        }

        private fun loadNode(node: AINode): Node {
            val children = node.mChildren()
            val nodeName = node.mName().dataString()
            val loadedNode = Node(
                name = nodeName,
                id = allocateNodeId(node),
                transform = NodeTransform.Matrix(node.mTransformation().toJoml()),
                components = buildList {
                    val meshIndices = node.mMeshes()
                    meshIndices?.let { meshIndices ->
                        repeat(node.mNumMeshes()) { index ->
                            val meshIndex = meshIndices.get(index)
                            val mesh = meshes.getOrNull(meshIndex)
                                ?: throw AssimpLoadException("No mesh #$meshIndex for node")
                            add(NodeComponent.MeshComponent(mesh))

                            meshToSkins[meshIndex]?.let { skins.getOrNull(it) }?.let { skin ->
                                add(
                                    NodeComponent.SkinComponent(
                                        skin = skin.skin,
                                        meshIds = listOf(mesh.id),
                                    )
                                )
                            }
                        }
                    }
                },
                children = children?.let { children ->
                    (0 until node.mNumChildren()).map { nodeIndex ->
                        val node = AINode.create(children.get(nodeIndex))
                        loadNode(node)
                    }
                } ?: listOf()
            )
            return loadedNode
        }

        fun load(): LoadResult {
            loadSkins()
            loadMaterials()
            loadMeshes()
            val rootNode = scene.mRootNode()?.let { loadNode(it) } ?: return EMPTY_LOAD_RESULT
            val animations = loadAnimations()
            val scene = Scene(nodes = listOf(rootNode))
            val model = Model(
                scenes = listOf(scene),
                defaultScene = scene,
                skins = skins.map { it.skin },
                expressions = listOf(),
            )
            return LoadResult(
                metadata = null,
                model = model,
                animations = animations,
            )
        }
    }

    private class BufferBackedFile private constructor(
        address: Long,
        private val readProc: AIFileReadProc,
        private val tellProc: AIFileTellProc,
        private val fileSizeProc: AIFileTellProc,
        private val seekProc: AIFileSeek,
    ) : AIFile(address, null) {
        companion object {
            fun create(buffer: ByteBuffer) = MemoryUtil.nmemAlignedAlloc(ALIGNOF.toLong(), SIZEOF.toLong()).let {
                val readProc = AIFileReadProc.create { _, dstAddress, size, count ->
                    val availableItems = buffer.remaining() / size
                    val readItems = min(availableItems, count)
                    if (readItems == 0L) {
                        return@create 0
                    }
                    val readSize = readItems * size
                    if (buffer.isDirect) {
                        val srcAddress = MemoryUtil.memAddress(buffer)
                        MemoryUtil.memCopy(srcAddress, dstAddress, readSize)
                        buffer.position(buffer.position() + readSize.toInt())
                    } else {
                        // Byte to byte copy
                        for (i in 0 until readSize) {
                            MemoryUtil.memPutByte(dstAddress + i, buffer.get())
                        }
                    }
                    readItems
                }
                val tellProc = AIFileTellProc.create { _ ->
                    buffer.position().toLong()
                }
                val fileSizeProc = AIFileTellProc.create { _ ->
                    buffer.limit().toLong()
                }
                val seekProc = AIFileSeek.create { _, offset, origin ->
                    val newPosition = when (origin) {
                        Assimp.aiOrigin_SET -> offset.toInt()
                        Assimp.aiOrigin_CUR -> buffer.position() + offset.toInt()
                        Assimp.aiOrigin_END -> buffer.limit() + offset.toInt()
                        else -> return@create Assimp.aiReturn_FAILURE
                    }
                    if (newPosition !in 0..buffer.limit()) {
                        return@create Assimp.aiReturn_FAILURE
                    }
                    buffer.position(newPosition)
                    Assimp.aiReturn_SUCCESS
                }
                BufferBackedFile(it, readProc, tellProc, fileSizeProc, seekProc)
            }
        }

        init {
            clear()
            ReadProc(readProc)
            TellProc(tellProc)
            FileSizeProc(fileSizeProc)
            SeekProc(seekProc)
        }

        override fun free() {
            readProc.free()
            tellProc.free()
            fileSizeProc.free()
            seekProc.free()
            super.free()
        }
    }

    private class ContextFileIO private constructor(
        address: Long,
        private val openProc: AIFileOpenProc,
        private val closeProc: AIFileCloseProc,
    ) : AIFileIO(address, null) {
        companion object {
            fun create(context: LoadContext) = MemoryUtil.nmemAlignedAlloc(ALIGNOF.toLong(), SIZEOF.toLong()).let {
                // FIXME: get away from this map
                val files = mutableMapOf<Long, BufferBackedFile>()
                val openProc = AIFileOpenProc.create { _, fileNameAddress, _ ->
                    val fileName = MemoryUtil.memUTF8(fileNameAddress)
                    val buffer = try {
                        context.loadExternalResource(
                            fileName,
                            LoadContext.ResourceType.PLAIN_DATA,
                            true,
                            256 * 1024 * 1024
                        )
                    } catch (ex: IOException) {
                        logger.warn("Failed to load resource $fileName")
                        return@create 0L
                    }
                    val file = BufferBackedFile.create(buffer)
                    file.address().also { fileAddress ->
                        files[fileAddress] = file
                    }
                }
                val closeProc = AIFileCloseProc.create { _, fileAddress ->
                    files.remove(fileAddress)?.close()
                }
                ContextFileIO(it, openProc, closeProc)
            }
        }

        init {
            OpenProc(openProc)
            CloseProc(closeProc)
        }

        override fun free() {
            openProc.free()
            closeProc.free()
            super.free()
        }
    }

    private class LoadContextWrapper(
        private val plainPath: Path,
        private val realPath: Path,
        private val inner: LoadContext,
    ) : LoadContext {
        override fun loadExternalResource(
            path: String,
            type: LoadContext.ResourceType,
            caseInsensitive: Boolean,
            maxSize: Int,
        ): ByteBuffer = if (plainPath.toString() == path) {
            realPath.read()
        } else {
            inner.loadExternalResource(path, type, caseInsensitive, maxSize)
        }
    }

    override fun load(
        path: Path,
        context: LoadContext,
        param: LoadParam,
    ): LoadResult {
        val flags = Assimp.aiProcess_Triangulate or
                Assimp.aiProcess_FlipUVs or
                Assimp.aiProcess_LimitBoneWeights or
                Assimp.aiProcess_PopulateArmatureData
        return if (context != LoadContext.Empty) {
            val plainPath = path.last()
            val context = LoadContextWrapper(plainPath, path, context)
            ContextFileIO.create(context).use { fileIO ->
                Context(
                    scene = Assimp.aiImportFileEx(plainPath.toString(), flags, fileIO) ?: throw AssimpLoadException(
                        Assimp.aiGetErrorString() ?: "Unknown error"
                    ),
                    context = context,
                    param = param,
                ).use {
                    it.load()
                }
            }
        } else {
            Context(
                scene = Assimp.aiImportFileFromMemory(path.read(), flags, path.extension)
                    ?: throw AssimpLoadException(Assimp.aiGetErrorString() ?: "Unknown error"),
                context = context,
                param = param,
            ).use {
                it.load()
            }
        }
    }
}
