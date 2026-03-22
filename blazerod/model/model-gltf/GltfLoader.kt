package top.fifthlight.blazerod.model.gltf

import kotlinx.serialization.json.Json
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.blazerod.model.*
import top.fifthlight.blazerod.model.animation.*
import top.fifthlight.blazerod.model.gltf.format.*
import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.LoadParam
import top.fifthlight.blazerod.model.loader.LoadResult
import top.fifthlight.blazerod.model.loader.ThumbnailResult
import top.fifthlight.blazerod.model.util.getSByteNormalized
import top.fifthlight.blazerod.model.util.getSShortNormalized
import top.fifthlight.blazerod.model.util.getUByteNormalized
import top.fifthlight.blazerod.model.util.getUShortNormalized
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.*

class GltfLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal class GltfLoader(
    private val buffer: ByteBuffer?,
    private val filePath: Path,
    private val context: LoadContext,
    private val param: LoadParam,
) {
    companion object {
        private val format = Json {
            ignoreUnknownKeys = true
        }
    }

    private val defaultSampler = Texture.Sampler(
        magFilter = param.samplerMagFilter ?: Texture.Sampler.MagFilter.LINEAR,
        minFilter = param.samplerMinFilter ?: Texture.Sampler.MinFilter.LINEAR,
        wrapS = Texture.Sampler.WrapMode.REPEAT,
        wrapT = Texture.Sampler.WrapMode.REPEAT,
    )

    private val uuid = UUID.randomUUID()
    private var loaded = false
    private lateinit var gltf: Gltf
    private val externalBuffers = mutableMapOf<URI, ByteBuffer>()
    private lateinit var buffers: List<Buffer>
    private lateinit var bufferViews: List<BufferView>
    private lateinit var accessors: List<Accessor>
    private lateinit var samplers: List<Texture.Sampler>
    private lateinit var textures: List<Texture>
    private lateinit var materials: List<Material>
    private lateinit var meshes: List<Mesh>
    private lateinit var skins: List<Skin>
    private lateinit var cameras: List<Camera>
    private val nodes = mutableMapOf<Int, Node>()
    private lateinit var scenes: List<Scene>
    private lateinit var animations: List<Animation>
    private lateinit var expressions: List<Expression>

    private fun parseDataUri(dataUri: URI): ByteBuffer {
        require(dataUri.scheme.equals("data", ignoreCase = true)) { "Bad scheme: ${dataUri.scheme}" }
        val data = dataUri.rawSchemeSpecificPart
        val commaIndex = data.indexOf(',').takeIf { it >= 0 } ?: throw GltfLoadException("No comma in data URI")

        val metadataPart = data.substring(0, commaIndex)
        val byteArray = when (val encoding = metadataPart.substringAfter(";", "").takeIf(String::isNotEmpty)) {
            null -> data.substring(commaIndex + 1).encodeToByteArray()
            "base64" -> try {
                Base64.getDecoder().decode(data.substring(commaIndex + 1))
            } catch (e: IllegalArgumentException) {
                throw GltfLoadException("Bad base64 data URI", e)
            }

            else -> throw GltfLoadException("Unknown encoding: $encoding")
        }
        val directBuffer = ByteBuffer.allocateDirect(byteArray.size)
        directBuffer.put(byteArray)
        directBuffer.flip()
        return directBuffer
    }

    private fun loadExternalUri(uri: URI): ByteBuffer = if (uri.scheme.equals("data", ignoreCase = true)) {
        parseDataUri(uri)
    } else {
        externalBuffers.getOrPut(uri) {
            TODO("External URI resources is not supported for now")
        }
    }

    private fun loadBuffers() {
        buffers = gltf.buffers?.mapIndexed { index, buffer ->
            if (buffer.uri == null) {
                val contextBuffer = this@GltfLoader.buffer
                if (index != 0 || contextBuffer == null) {
                    throw GltfLoadException("Buffer at $index missing URI")
                }
                Buffer(
                    name = "GLB built-in buffer",
                    buffer = contextBuffer,
                )
            } else {
                Buffer(
                    name = buffer.uri,
                    buffer = loadExternalUri(URI.create(buffer.uri)),
                )
            }
        } ?: listOf()
    }

    private fun loadBufferViews() {
        bufferViews = gltf.bufferViews?.map {
            BufferView(
                buffer = buffers.getOrNull(it.buffer)
                    ?: throw GltfLoadException("Invalid buffer view: buffer ${it.buffer} not found"),
                byteLength = it.byteLength,
                byteOffset = it.byteOffset ?: 0,
                byteStride = it.byteStride ?: 0,
            )
        } ?: listOf()
    }

    private fun loadAccessors() {
        accessors = gltf.accessors?.map {
            Accessor(
                bufferView = it.bufferView?.let { index ->
                    bufferViews.getOrNull(index)
                        ?: throw GltfLoadException("Invalid accessor: buffer view ${it.bufferView} not found")
                },
                byteOffset = it.byteOffset,
                componentType = it.componentType,
                normalized = it.normalized,
                count = it.count,
                type = it.type,
                max = it.max,
                min = it.min,
                name = it.name,
            )
        } ?: listOf()
    }

    private fun loadSamplers() {
        samplers = gltf.samplers?.map {
            Texture.Sampler(
                magFilter = it.magFilter,
                minFilter = it.minFilter,
                wrapS = it.wrapS,
                wrapT = it.wrapT,
            )
        } ?: listOf()
    }

    private fun parseMimeType(mimeType: String) = Texture.TextureType.entries.firstOrNull { mimeType == it.mimeType }

    private fun loadTextures() {
        val images = gltf.images ?: listOf()
        textures = gltf.textures?.map { texture ->
            val image = texture.source?.let { index ->
                images.getOrNull(index)
                    ?: throw GltfLoadException("Bad texture: image $index not found")
            }
            val bufferView = image?.bufferView?.let { index ->
                bufferViews.getOrNull(index) ?: throw GltfLoadException("")
            } ?: image?.uri?.let { uri ->
                val buffer = loadExternalUri(URI.create(uri))
                BufferView(
                    buffer = Buffer(buffer = buffer),
                    byteLength = buffer.remaining(),
                    byteOffset = 0,
                    byteStride = 0,
                )
            }
            Texture(
                name = texture.name,
                sampler = texture.sampler?.let { index ->
                    samplers.getOrNull(index)?.let {
                        it.copy(
                            magFilter = param.samplerMagFilter ?: it.magFilter,
                            minFilter = param.samplerMinFilter ?: it.minFilter,
                        )
                    } ?: throw GltfLoadException("Bad texture: sampler $index not found")
                } ?: defaultSampler,
                bufferView = bufferView,
                type = image?.mimeType?.let { mime -> parseMimeType(mime) },
            )
        } ?: listOf()
    }

    private fun loadMaterials() {
        fun loadTextureInfo(info: GltfTextureInfo?) = info?.let {
            Material.TextureInfo(
                texture = textures.getOrNull(info.index)
                    ?: throw GltfLoadException("Bad texture info: texture ${info.index} not found"),
                textureCoordinate = info.texCoord,
            )
        }
        materials = gltf.materials?.map {
            val unlit = it.extensions.unlit
            when {
                unlit != null -> Material.Unlit(
                    name = it.name,
                    baseColor = it.pbrMetallicRoughness.baseColorFactor,
                    baseColorTexture = loadTextureInfo(it.pbrMetallicRoughness.baseColorTexture),
                    alphaMode = it.alphaMode,
                    alphaCutoff = it.alphaCutoff,
                    doubleSided = it.doubleSided,
                )

                else -> Material.Pbr(
                    name = it.name,
                    baseColor = it.pbrMetallicRoughness.baseColorFactor,
                    baseColorTexture = loadTextureInfo(it.pbrMetallicRoughness.baseColorTexture),
                    metallicFactor = it.pbrMetallicRoughness.metallicFactor,
                    metallicRoughnessTexture = loadTextureInfo(it.pbrMetallicRoughness.metallicRoughnessTexture),
                    // TODO more attributes
                    alphaMode = it.alphaMode,
                    alphaCutoff = it.alphaCutoff,
                    doubleSided = it.doubleSided,
                )
            }
        } ?: listOf()
    }

    private fun loadMeshes() {
        fun loadAttributes(
            attributes: Map<GltfAttributeKey, Int>,
            morph: Boolean,
        ): Primitive.Attributes {
            var position: Accessor? = null
            var normal: Accessor? = null
            var tangent: Accessor? = null
            val texcoords = mutableMapOf<Int, Accessor>()
            val colors = mutableMapOf<Int, Accessor>()
            val joints = mutableMapOf<Int, Accessor>()
            val weights = mutableMapOf<Int, Accessor>()
            for ((key, value) in attributes) {
                val accessor = accessors.getOrNull(value)
                    ?: throw GltfLoadException("Bad attributes: unknown accessor index: $value")
                when (key) {
                    GltfAttributeKey.Normal -> normal = accessor
                    GltfAttributeKey.Position -> position = accessor
                    GltfAttributeKey.Tangent -> tangent = accessor
                    is GltfAttributeKey.Color -> colors[key.index] = accessor
                    is GltfAttributeKey.TexCoord -> texcoords[key.index] = accessor
                    is GltfAttributeKey.Joints -> joints[key.index] = accessor
                    is GltfAttributeKey.Weights -> weights[key.index] = accessor
                    else -> continue
                }
            }
            fun <T> loadAttributeMap(map: Map<Int, T>): List<T> = if (map.isEmpty()) {
                listOf()
            } else {
                (0..map.keys.max()).map {
                    map[it] ?: throw GltfLoadException("Bad attribute map: missing index $it")
                }
            }
            if (morph) {
                return Primitive.Attributes.MorphTarget(
                    position = position,
                    normal = normal,
                    tangent = tangent,
                    texcoords = loadAttributeMap(texcoords),
                    colors = loadAttributeMap(colors),
                    joints = loadAttributeMap(joints),
                    weights = loadAttributeMap(weights)
                )
            } else {
                check(position != null) { "No position attribute in primitive" }
                return Primitive.Attributes.Primitive(
                    position = position,
                    normal = normal,
                    tangent = tangent,
                    texcoords = loadAttributeMap(texcoords),
                    colors = loadAttributeMap(colors),
                    joints = loadAttributeMap(joints),
                    weights = loadAttributeMap(weights)
                )
            }
        }

        fun loadPrimitive(primitive: GltfPrimitive) = Primitive(
            mode = primitive.mode,
            material = primitive.material?.let {
                materials.getOrNull(it) ?: throw GltfLoadException("Bad primitive: unknown material $it")
            },
            attributes = loadAttributes(primitive.attributes, false) as Primitive.Attributes.Primitive,
            indices = primitive.indices?.let { indices ->
                accessors.getOrNull(indices)
                    ?: throw GltfLoadException("Bad primitive: unknown accessor index: $indices")
            },
            targets = primitive.targets?.map { loadAttributes(it, true) as Primitive.Attributes.MorphTarget }
                ?: listOf(),
        )

        meshes = gltf.meshes?.mapIndexed { index, mesh ->
            Mesh(
                id = MeshId(uuid, index),
                primitives = mesh.primitives.map { primitive -> loadPrimitive(primitive) },
                weights = mesh.weights,
            )
        } ?: listOf()
    }

    private fun loadSkins() {
        val boneMapping = run {
            val vrmV0 = gltf.extensions?.vrmV0?.humanoid?.humanBones
            if (vrmV0 != null) {
                return@run vrmV0
                    .asSequence()
                    .mapNotNull { HumanoidTag.fromVrmName(it.bone)?.let { tag -> Pair(it.node, tag) } }
                    .associate { it }
            }
            val vrmV1 = gltf.extensions?.vrmV1?.humanoid?.humanBones
            if (vrmV1 != null) {
                return@run vrmV1.entries.asSequence()
                    .mapNotNull { (boneName, boneItem) ->
                        HumanoidTag.fromVrmName(boneName)?.let { tag -> Pair(tag, boneItem.node) }
                    }
                    .associate { (tag, index) -> Pair(index, tag) }
            }
            null
        }
        skins = gltf.skins?.map { skin ->
            if (skin.joints.isEmpty()) {
                throw GltfLoadException("Bad skin: no joints")
            }
            val inverseBindMatrices = run {
                val accessor = skin.inverseBindMatrices?.let {
                    accessors.getOrNull(it) ?: throw GltfLoadException("Bad skin: no accessor at index $it")
                } ?: return@run null
                if (accessor.componentType != Accessor.ComponentType.FLOAT) {
                    throw GltfLoadException("Bad component type in skin's inverseBindMatrices: ${accessor.componentType}, should be FLOAT")
                }
                if (accessor.type != Accessor.AccessorType.MAT4) {
                    throw GltfLoadException("Bad type in skin's inverseBindMatrices: ${accessor.type}, should be MAT4")
                }
                if (accessor.count != skin.joints.size) {
                    throw GltfLoadException("Bad size of inverseBindMatrices: get ${accessor.count}, should be ${skin.joints.size}")
                }
                buildList<Matrix4f>(accessor.count) {
                    accessor.read { buffer ->
                        add(Matrix4f().set(buffer))
                    }
                }
            }
            Skin(
                name = skin.name,
                joints = skin.joints.map { NodeId(uuid, it) },
                skeleton = skin.skeleton?.let { NodeId(uuid, it) },
                inverseBindMatrices = inverseBindMatrices,
                jointHumanoidTags = skin.joints.map { boneMapping?.get(it) },
            )
        } ?: listOf()
    }

    private fun loadCameras() {
        cameras = gltf.cameras?.map { camera ->
            when (camera.type) {
                GltfCameraType.PERSPECTIVE -> {
                    camera.perspective?.let { perspective ->
                        Camera.Perspective(
                            name = null,
                            aspectRatio = perspective.aspectRatio,
                            yfov = perspective.yfov,
                            znear = perspective.znear,
                        )
                    } ?: throw GltfLoadException("Bad perspective camera: no perspective")
                }

                GltfCameraType.ORTHOGRAPHIC -> {
                    camera.orthographic?.let { orthographic ->
                        Camera.Orthographic(
                            name = null,
                            xmag = orthographic.xmag,
                            ymag = orthographic.ymag,
                            zfar = orthographic.zfar,
                            znear = orthographic.znear,
                        )
                    } ?: throw GltfLoadException("Bad orthographic camera: no orthographic")
                }
            }
        } ?: listOf()
    }

    private fun loadNode(index: Int): Node = nodes.getOrPut(index) {
        // TODO avoid stack overflow on bad models
        val node = gltf.nodes?.getOrNull(index) ?: throw GltfLoadException("No node at index $index")
        Node(
            name = node.name,
            id = NodeId(
                modelId = uuid,
                index = index,
            ),
            transform = when {
                node.matrix != null -> NodeTransform.Matrix(node.matrix)
                node.translation != null || node.rotation != null || node.scale != null -> NodeTransform.Decomposed(
                    translation = node.translation ?: Vector3f(),
                    rotation = node.rotation ?: Quaternionf(),
                    scale = node.scale ?: Vector3f(1f),
                )

                else -> null
            },
            children = (node.children ?: listOf()).map(::loadNode),
            components = buildList {
                node.mesh?.let { mesh ->
                    val mesh = meshes.getOrNull(mesh) ?: throw GltfLoadException("Bad node: unknown mesh $mesh")
                    add(NodeComponent.MeshComponent(mesh))
                    node.skin?.let { skin ->
                        val skin = skins.getOrNull(skin) ?: throw GltfLoadException("Bad node: unknown skin $skin")
                        add(
                            NodeComponent.SkinComponent(
                                skin = skin,
                                meshIds = listOf(mesh.id),
                            )
                        )
                    }
                }
                node.camera?.let { camera ->
                    val camera =
                        cameras.getOrNull(camera) ?: throw GltfLoadException("Bad node: unknown camera $camera")
                    add(NodeComponent.CameraComponent(camera))
                }
            },
        )
    }

    private fun loadScenes() {
        scenes = gltf.scenes?.map {
            Scene(
                nodes = it.nodes?.map(::loadNode) ?: listOf(),
            )
        } ?: listOf()
    }

    private fun loadAnimations() {
        animations = gltf.animations?.map { animation ->
            SimpleAnimation(
                name = animation.name,
                channels = animation.channels.mapNotNull { channel ->
                    val targetNodeId = channel.target.node ?: return@mapNotNull null
                    val targetNode = nodes[targetNodeId]
                        ?: throw GltfLoadException("Bad animation channel: target node $targetNodeId not found")
                    val targetNodeName = targetNode.name
                    val targetHumanoidTag = targetNode.name?.let { HumanoidTag.fromVrmName(it) }
                    val sampler = animation.samplers.getOrNull(channel.sampler)
                        ?: throw GltfLoadException("Bad animation channel: unknown sampler ${channel.sampler}")
                    val inputAccessor = accessors.getOrNull(sampler.input)
                        ?: throw GltfLoadException("Bad animation sampler: unknown input accessor ${sampler.input}")
                    val outputAccessor = accessors.getOrNull(sampler.output)
                        ?: throw GltfLoadException("Bad animation sampler: unknown output accessor ${sampler.output}")
                    channel.target.path.check(outputAccessor)
                    when (channel.target.path) {
                        GltfAnimationTarget.Path.TRANSLATION -> KeyFrameAnimationChannel(
                            type = AnimationChannel.Type.Translation,
                            typeData = AnimationChannel.Type.TransformData(
                                node = AnimationChannel.Type.NodeData(
                                    targetNode = targetNode,
                                    targetNodeName = targetNodeName,
                                    targetHumanoidTag = targetHumanoidTag,
                                ),
                                transformId = TransformId.ABSOLUTE,
                            ),
                            indexer = AccessorAnimationKeyFrameIndexer(inputAccessor),
                            keyframeData = AccessorAnimationKeyFrameData(
                                accessor = outputAccessor,
                                elements = sampler.interpolation.elements,
                                elementGetter = { buffer, result -> result.set(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()) },
                            ),
                            interpolation = sampler.interpolation,
                        )

                        GltfAnimationTarget.Path.SCALE -> KeyFrameAnimationChannel(
                            type = AnimationChannel.Type.Scale,
                            typeData = AnimationChannel.Type.TransformData(
                                node = AnimationChannel.Type.NodeData(
                                    targetNode = targetNode,
                                    targetNodeName = targetNodeName,
                                    targetHumanoidTag = targetHumanoidTag,
                                ),
                                transformId = TransformId.ABSOLUTE,
                            ),
                            indexer = AccessorAnimationKeyFrameIndexer(inputAccessor),
                            keyframeData = AccessorAnimationKeyFrameData(
                                accessor = outputAccessor,
                                elements = sampler.interpolation.elements,
                                elementGetter = { buffer, result -> result.set(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()) },
                            ),
                            interpolation = sampler.interpolation,
                        )

                        GltfAnimationTarget.Path.ROTATION -> KeyFrameAnimationChannel(
                            type = AnimationChannel.Type.Rotation,
                            typeData = AnimationChannel.Type.TransformData(
                                node = AnimationChannel.Type.NodeData(
                                    targetNode = targetNode,
                                    targetNodeName = targetNodeName,
                                    targetHumanoidTag = targetHumanoidTag,
                                ),
                                transformId = TransformId.ABSOLUTE,
                            ),
                            indexer = AccessorAnimationKeyFrameIndexer(inputAccessor),
                            keyframeData = AccessorAnimationKeyFrameData(
                                accessor = outputAccessor,
                                elements = sampler.interpolation.elements,
                                elementGetter = when (outputAccessor.componentType) {
                                    Accessor.ComponentType.BYTE -> { buffer, result ->
                                        result.set(
                                            buffer.getSByteNormalized(),
                                            buffer.getSByteNormalized(),
                                            buffer.getSByteNormalized(),
                                            buffer.getSByteNormalized(),
                                        )
                                    }

                                    Accessor.ComponentType.UNSIGNED_BYTE -> { buffer, result ->
                                        result.set(
                                            buffer.getUByteNormalized(),
                                            buffer.getUByteNormalized(),
                                            buffer.getUByteNormalized(),
                                            buffer.getUByteNormalized(),
                                        )
                                    }

                                    Accessor.ComponentType.SHORT -> { buffer, result ->
                                        result.set(
                                            buffer.getSShortNormalized(),
                                            buffer.getSShortNormalized(),
                                            buffer.getSShortNormalized(),
                                            buffer.getSShortNormalized(),
                                        )
                                    }

                                    Accessor.ComponentType.UNSIGNED_SHORT -> { buffer, result ->
                                        result.set(
                                            buffer.getUShortNormalized(),
                                            buffer.getUShortNormalized(),
                                            buffer.getUShortNormalized(),
                                            buffer.getUShortNormalized(),
                                        )
                                    }

                                    Accessor.ComponentType.FLOAT -> { buffer, result ->
                                        result.set(
                                            buffer.getFloat(),
                                            buffer.getFloat(),
                                            buffer.getFloat(),
                                            buffer.getFloat(),
                                        )
                                    }

                                    else -> throw AssertionError()
                                },
                            ),
                            interpolation = sampler.interpolation,
                        )

                        else -> return@mapNotNull null
                    }
                }
            )
        } ?: listOf()
    }

    private fun loadExpressions() {
        val vrmV0 = gltf.extensions?.vrmV0?.blendShapeMaster?.blendShapeGroups
        if (vrmV0 != null) {
            expressions = vrmV0.map { expression ->
                Expression.Target(
                    name = expression.name,
                    tag = expression.presetName?.let(Expression.Tag::fromVrm0Name),
                    bindings = expression.binds?.map { bind ->
                        Expression.Target.Binding.MeshMorphTarget(
                            meshId = MeshId(uuid, bind.mesh),
                            index = bind.index,
                            weight = bind.weight?.let { it / 100f } ?: 1f,
                        )
                    } ?: listOf()
                )
            }
            return
        }

        val vrmV1 = gltf.extensions?.vrmV1?.expressions
        if (vrmV1 != null) {
            expressions = ((vrmV1.preset?.entries?.asSequence() ?: sequenceOf()) + (vrmV1.custom?.entries?.asSequence()
                ?: sequenceOf())).map { (name, value) ->
                Expression.Target(
                    name = name,
                    isBinary = value.isBinary ?: false,
                    tag = Expression.Tag.fromVrm1Name(name),
                    bindings = value.morphTargetBinds?.map { bind ->
                        Expression.Target.Binding.NodeMorphTarget(
                            nodeId = NodeId(uuid, bind.node),
                            index = bind.index,
                            weight = bind.weight?.let { it / 100f } ?: 1f,
                        )
                    } ?: listOf()
                )
            }.toList()
            return
        }
        expressions = listOf()
    }

    fun load(json: String): LoadResult {
        if (loaded) {
            throw GltfLoadException("Already loaded. Please don't load again.")
        }
        loaded = true

        gltf = format.decodeFromString(json)

        loadBuffers()
        loadBufferViews()
        loadAccessors()
        loadSamplers()
        loadTextures()
        loadMaterials()
        loadMeshes()
        loadSkins()
        loadCameras()
        loadScenes()
        loadAnimations()
        loadExpressions()

        val metadata = gltf.extensions?.vrmV0?.meta?.toMetadata { textures.getOrNull(it) }
            ?: gltf.extensions?.vrmV1?.meta?.toMetadata { textures.getOrNull(it) }

        val model = Model(
            scenes = scenes,
            defaultScene = gltf.scene?.let {
                scenes.getOrNull(it) ?: throw GltfLoadException("Bad model: unknown scene index ${gltf.scene}")
            },
            skins = skins,
            expressions = expressions,
        )

        return LoadResult(
            metadata = metadata,
            model = model,
            animations = animations,
        )
    }

    fun getThumbnail(json: String, binaryChunkData: GltfBinaryLoader.ChunkData): ThumbnailResult {
        if (loaded) {
            throw GltfLoadException("Already loaded. Please don't load again.")
        }
        loaded = true

        gltf = format.decodeFromString(json)

        val textureIndex = run {
            val vrmV0Meta = gltf.extensions?.vrmV0?.meta
            if (vrmV0Meta != null) {
                return@run vrmV0Meta.texture
            }
            val vrmV1Meta = gltf.extensions?.vrmV1?.meta
            if (vrmV1Meta != null) {
                return@run vrmV1Meta.thumbnailImage
            }
            null
        } ?: return ThumbnailResult.None

        val texture =
            gltf.textures?.getOrNull(textureIndex) ?: throw GltfLoadException("No texture index $textureIndex")
        val imageIndex = texture.source ?: return ThumbnailResult.None
        val image = gltf.images?.getOrNull(texture.source) ?: throw GltfLoadException("No image index $imageIndex")
        // All images should be embed in VRM
        return if (image.bufferView != null) {
            val bufferView = gltf.bufferViews?.getOrNull(image.bufferView)
                ?: throw GltfLoadException("No bufferView index ${image.bufferView}")
            if (bufferView.buffer != 0) {
                // External buffer? Not in VRM.
                return ThumbnailResult.None
            }
            val bufferViewOffset = bufferView.byteOffset ?: 0
            if (bufferViewOffset < 0) {
                throw GltfLoadException("Bad bufferView offset: $bufferViewOffset")
            }
            if (bufferView.byteLength < 0) {
                throw GltfLoadException("Bad bufferView length: ${bufferView.byteLength}")
            }
            if (bufferViewOffset + bufferView.byteLength > binaryChunkData.length) {
                throw GltfLoadException("Bad offset: total ${binaryChunkData.length}, but offset $bufferViewOffset length ${bufferView.byteLength}")
            }
            val finalOffset = binaryChunkData.offset + bufferViewOffset
            ThumbnailResult.Embed(
                offset = finalOffset,
                length = bufferView.byteLength.toLong(),
                type = image.mimeType?.let { mime -> parseMimeType(mime) },
            )
        } else {
            ThumbnailResult.None
        }
    }
}
