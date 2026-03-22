package top.fifthlight.blazerod.model.pmx

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.slf4j.LoggerFactory
import top.fifthlight.blazerod.model.*
import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.LoadParam
import top.fifthlight.blazerod.model.loader.LoadResult
import top.fifthlight.blazerod.model.loader.ModelFileLoader
import top.fifthlight.blazerod.model.loader.util.MMD_SCALE
import top.fifthlight.blazerod.model.loader.util.readAll
import top.fifthlight.blazerod.model.pmx.format.*
import top.fifthlight.blazerod.model.pmx.format.PmxMorphGroup.MorphItem
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.channels.FileChannel
import java.nio.charset.CodingErrorAction
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

class PmxLoadException(message: String) : Exception(message)

// PMX loader.
// Format from https://gist.github.com/felixjones/f8a06bd48f9da9a4539f
class PmxLoader : ModelFileLoader {
    override val extensions = mapOf(
        "pmx" to setOf(ModelFileLoader.Ability.MODEL),
    )

    companion object {
        private val PMX_SIGNATURE = byteArrayOf(0x50, 0x4D, 0x58, 0x20)
        private val VALID_INDEX_SIZES = listOf(1, 2, 4)

        //                                             POS NORM UV
        private const val BASE_VERTEX_ATTRIBUTE_SIZE = (3 + 3 + 2) * 4

        //                                           JOINT WEIGHT
        private const val SKIN_VERTEX_ATTRIBUTE_SIZE = (4 + 4) * 4
        private const val VERTEX_ATTRIBUTE_SIZE = BASE_VERTEX_ATTRIBUTE_SIZE + SKIN_VERTEX_ATTRIBUTE_SIZE
        private val logger = LoggerFactory.getLogger(PmxLoader::class.java)
    }

    override val probeLength = PMX_SIGNATURE.size
    override fun probe(buffer: ByteBuffer): Boolean {
        if (buffer.remaining() < PMX_SIGNATURE.size) return false
        val signatureBytes = ByteArray(PMX_SIGNATURE.size)
        buffer.get(signatureBytes, 0, PMX_SIGNATURE.size)
        return signatureBytes.contentEquals(PMX_SIGNATURE)
    }

    private class MaterialData(
        val material: PmxMaterial,
        val vertexAttributes: Primitive.Attributes.Primitive,
        val indexBufferView: BufferView,
        val vertices: Int,
    )

    private class Context(
        private val context: LoadContext,
        private val param: LoadParam,
    ) {
        private var version: Float = 0f
        private lateinit var globals: PmxGlobals
        private val decoder by lazy {
            globals.textEncoding.charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
        }

        private lateinit var vertexBuffer: ByteBuffer
        private var vertices: Int = -1

        private lateinit var indexBuffer: IntArray
        private lateinit var indexBufferType: Accessor.ComponentType
        private var indices: Int = -1

        private lateinit var textures: List<Texture?>
        private lateinit var materials: List<MaterialData?>
        private lateinit var vertexToMaterialMap: VertexMaterialTable
        private lateinit var bones: List<PmxBone>
        private val effectorToIkDataMap = mutableMapOf<Int, MutableList<PmxBone.IkData>>()
        private val sourceToInheritMap = mutableMapOf<Int, MutableList<PmxBone.InheritData>>()
        private lateinit var morphTargets: List<PmxMorph>
        private lateinit var morphTargetGroups: List<PmxMorphGroup>
        private val childBoneMap = mutableMapOf<Int, MutableList<Int>>()
        private val rootBones = mutableListOf<Int>()
        private lateinit var rigidBodies: List<PmxRigidBody>
        private var boneToRigidBodyMap = mutableMapOf<Int, MutableList<Int>>()
        private lateinit var joints: List<PmxJoint>

        private fun loadRgbColor(buffer: ByteBuffer): RgbColor {
            if (buffer.remaining() < 3 * 4) {
                throw PmxLoadException("Bad file: want to read Vec3 (12 bytes), but only have ${buffer.remaining()} bytes available")
            }
            return RgbColor(
                r = buffer.getFloat(),
                g = buffer.getFloat(),
                b = buffer.getFloat(),
            )
        }

        private fun loadRgbaColor(buffer: ByteBuffer): RgbaColor {
            if (buffer.remaining() < 4 * 4) {
                throw PmxLoadException("Bad file: want to read Vec4 (16 bytes), but only have ${buffer.remaining()} bytes available")
            }
            return RgbaColor(
                r = buffer.getFloat(),
                g = buffer.getFloat(),
                b = buffer.getFloat(),
                a = buffer.getFloat(),
            )
        }

        private fun loadVector3f(buffer: ByteBuffer): Vector3f {
            if (buffer.remaining() < 3 * 4) {
                throw PmxLoadException("Bad file: want to read Vec3 (12 bytes), but only have ${buffer.remaining()} bytes available")
            }
            return Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat())
        }

        private fun loadSignature(buffer: ByteBuffer) {
            if (buffer.remaining() < PMX_SIGNATURE.size) {
                throw PmxLoadException("Bad file: signature is ${PMX_SIGNATURE.size} bytes, but only ${buffer.remaining()} bytes in buffer")
            }
            if (PMX_SIGNATURE.any { buffer.get() != it }) {
                throw PmxLoadException("Bad PMX signature")
            }
        }

        private fun loadGlobal(buffer: ByteBuffer) = PmxGlobals(
            textEncoding = when (val encoding = buffer.get().toUByte().toInt()) {
                0 -> PmxGlobals.TextEncoding.UTF16LE
                1 -> PmxGlobals.TextEncoding.UTF8
                else -> throw PmxLoadException("Bad text encoding: $encoding")
            },
            additionalVec4Count = buffer.get().toUByte().toInt().also {
                if (it !in 0..4) {
                    throw PmxLoadException("Bad additional vec4 count: $it, should be in [0, 4]")
                }
            },
            vertexIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad vertex index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            },
            textureIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad texture index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            },
            materialIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad material index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            },
            boneIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad bone index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            },
            morphIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad morph index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            },
            rigidBodyIndexSize = buffer.get().toUByte().toInt().also {
                if (it !in VALID_INDEX_SIZES) {
                    throw PmxLoadException("Bad rigid body index size: $it, should be ${VALID_INDEX_SIZES.joinToString(", ")}")
                }
            }
        )

        private fun loadBoneIndex(buffer: ByteBuffer): Int = when (globals.boneIndexSize) {
            1 -> buffer.get().toInt()
            2 -> buffer.getShort().toInt()
            4 -> buffer.getInt()
            else -> throw PmxLoadException("Bad bone index size: ${globals.boneIndexSize}")
        }

        private fun loadTextureIndex(buffer: ByteBuffer): Int = when (globals.textureIndexSize) {
            1 -> buffer.get().toInt()
            2 -> buffer.getShort().toInt()
            4 -> buffer.getInt()
            else -> throw PmxLoadException("Bad texture index size: ${globals.textureIndexSize}")
        }

        private fun loadMorphIndex(buffer: ByteBuffer) = when (globals.morphIndexSize) {
            1 -> buffer.get().toUByte().toInt()
            2 -> buffer.getShort().toUShort().toInt()
            4 -> buffer.getInt()
            else -> throw PmxLoadException("Bad morph index size: ${globals.morphIndexSize}")
        }

        private fun loadVertexIndex(buffer: ByteBuffer): Int = when (globals.vertexIndexSize) {
            1 -> buffer.get().toUByte().toInt()
            2 -> buffer.getShort().toUShort().toInt()
            4 -> buffer.getInt()
            else -> throw PmxLoadException("Bad vertex index size: ${globals.vertexIndexSize}")
        }

        private fun loadRigidBodyIndex(buffer: ByteBuffer): Int = when (globals.rigidBodyIndexSize) {
            1 -> buffer.get().toUByte().toInt()
            2 -> buffer.getShort().toUShort().toInt()
            4 -> buffer.getInt()
            else -> throw PmxLoadException("Bad rigid body index size: ${globals.rigidBodyIndexSize}")
        }

        private fun loadString(buffer: ByteBuffer): String {
            if (buffer.remaining() < 4) {
                throw PmxLoadException("No space for string index: want at least 4, but got ${buffer.remaining()}")
            }
            val length = buffer.getInt()
            if (length < 0) {
                throw PmxLoadException("Bad string size, should be at least 0: $length")
            }
            if (buffer.remaining() < length) {
                throw PmxLoadException("No enough data for string: want $length bytes, but only have ${buffer.remaining()} bytes")
            }
            val stringBuffer = buffer.slice(buffer.position(), length).order(ByteOrder.LITTLE_ENDIAN)
            return decoder.decode(stringBuffer).toString().also {
                buffer.position(buffer.position() + length)
            }
        }

        private fun loadHeader(buffer: ByteBuffer): PmxHeader {
            loadSignature(buffer)
            if (buffer.remaining() < 5) {
                throw PmxLoadException("Bad PMX signature")
            }
            val version = buffer.getFloat()
            if (version < 2.0f) {
                throw PmxLoadException("Bad PMX version: at least 2.0, but get $version")
            }
            this.version = version
            val globalsCount = buffer.get().toUByte().toInt()
            if (globalsCount < 8) {
                throw PmxLoadException("Bad global count: $globalsCount, at least 8")
            }
            globals = loadGlobal(buffer.slice(buffer.position(), globalsCount).order(ByteOrder.LITTLE_ENDIAN))
            buffer.position(buffer.position() + globalsCount)
            return PmxHeader(
                version = version,
                globals = globals,
                modelNameLocal = loadString(buffer),
                modelNameUniversal = loadString(buffer),
                commentLocal = loadString(buffer),
                commentUniversal = loadString(buffer),
            )
        }

        private fun wrapVertexBuffer(
            bufferName: String,
            buffer: ByteBuffer,
            vertexCount: Int,
        ): Primitive.Attributes.Primitive {
            val buffer = Buffer(
                name = bufferName,
                buffer = buffer,
            )
            val bufferView = BufferView(
                buffer = buffer,
                byteLength = buffer.buffer.remaining(),
                byteOffset = 0,
                byteStride = VERTEX_ATTRIBUTE_SIZE,
            )
            return Primitive.Attributes.Primitive(
                position = Accessor(
                    bufferView = bufferView,
                    byteOffset = 0,
                    componentType = Accessor.ComponentType.FLOAT,
                    normalized = false,
                    count = vertexCount,
                    type = Accessor.AccessorType.VEC3,
                ),
                normal = Accessor(
                    bufferView = bufferView,
                    byteOffset = 3 * 4,
                    componentType = Accessor.ComponentType.FLOAT,
                    normalized = false,
                    count = vertexCount,
                    type = Accessor.AccessorType.VEC3,
                ),
                texcoords = listOf(
                    Accessor(
                        bufferView = bufferView,
                        byteOffset = (3 + 3) * 4,
                        componentType = Accessor.ComponentType.FLOAT,
                        normalized = false,
                        count = vertexCount,
                        type = Accessor.AccessorType.VEC2,
                    )
                ),
                joints = listOf(
                    Accessor(
                        bufferView = bufferView,
                        byteOffset = (3 + 3 + 2) * 4,
                        componentType = Accessor.ComponentType.UNSIGNED_INT,
                        normalized = false,
                        count = vertexCount,
                        type = Accessor.AccessorType.VEC4,
                    )
                ),
                weights = listOf(
                    Accessor(
                        bufferView = bufferView,
                        byteOffset = (3 + 3 + 2 + 4) * 4,
                        componentType = Accessor.ComponentType.FLOAT,
                        normalized = false,
                        count = vertexCount,
                        type = Accessor.AccessorType.VEC4,
                    )
                )
            )
        }

        // Read all vertices from PMX file.
        private fun loadVertices(buffer: ByteBuffer) {
            val vertexCount = buffer.getInt()
            if (vertexCount <= 0) {
                throw PmxLoadException("Bad vertex count: $vertexCount, should be greater than 0")
            }

            val additionalVec4Size = globals.additionalVec4Count * 4 * 4
            val boneIndexSize = globals.boneIndexSize

            val outputBuffer =
                ByteBuffer.allocateDirect(vertexCount * VERTEX_ATTRIBUTE_SIZE).order(ByteOrder.nativeOrder())
            var outputPosition = 0
            var inputPosition = buffer.position()

            fun readFloat(): Float = buffer.getFloat(inputPosition).also {
                inputPosition += 4
            }

            fun readBoneIndex(): Int {
                val index = when (boneIndexSize) {
                    1 -> buffer.get(inputPosition).toInt()
                    2 -> buffer.getShort(inputPosition).toInt()
                    4 -> buffer.getInt(inputPosition)
                    else -> throw AssertionError()
                }
                inputPosition += boneIndexSize
                return index
            }

            fun readWeight(): Float = buffer.getFloat(inputPosition).also { inputPosition += 4 }
            fun readVector3f(dst: Vector3f) = dst.also {
                dst.set(
                    buffer.getFloat(inputPosition),
                    buffer.getFloat(inputPosition + 4),
                    buffer.getFloat(inputPosition + 8)
                )
                inputPosition += 12
            }

            val copyBaseVertexSize = BASE_VERTEX_ATTRIBUTE_SIZE - 24
            for (i in 0 until vertexCount) {
                // Read position data, transform xyz
                val x = buffer.getFloat(inputPosition)
                val y = buffer.getFloat(inputPosition + 4)
                val z = buffer.getFloat(inputPosition + 8)
                outputBuffer.putFloat(outputPosition, x * -MMD_SCALE)
                outputBuffer.putFloat(outputPosition + 4, y * MMD_SCALE)
                outputBuffer.putFloat(outputPosition + 8, z * MMD_SCALE)
                outputPosition += 12
                inputPosition += 12

                // Read normal data, transform x
                val nx = buffer.getFloat(inputPosition)
                val ny = buffer.getFloat(inputPosition + 4)
                val nz = buffer.getFloat(inputPosition + 8)
                outputBuffer.putFloat(outputPosition, -nx)
                outputBuffer.putFloat(outputPosition + 4, ny)
                outputBuffer.putFloat(outputPosition + 8, nz)
                outputPosition += 12
                inputPosition += 12

                // POSITION_NORMAL_UV_JOINT_WEIGHT
                outputBuffer.put(outputPosition, buffer, inputPosition, copyBaseVertexSize)
                outputPosition += copyBaseVertexSize
                inputPosition += copyBaseVertexSize

                // Skip additionalVec4
                inputPosition += additionalVec4Size

                // Weight deform type
                val weightDeformType = buffer.get(inputPosition).toUByte().toInt()
                inputPosition += 1

                val vec = Vector3f()
                // TODO: keep track of vertices without bone, to exclude non-skinned vertices out
                when (weightDeformType) {
                    // BDEF1
                    0 -> {
                        val index1 = readBoneIndex()
                        outputBuffer.putInt(outputPosition, index1)
                        if (index1 != -1) {
                            outputBuffer.putFloat(outputPosition + 16, 1f)
                        }
                    }
                    // BDEF2
                    1 -> {
                        val index1 = readBoneIndex()
                        val index2 = readBoneIndex()
                        val weight1 = readWeight()
                        outputBuffer.putInt(outputPosition, index1)
                        outputBuffer.putInt(outputPosition + 4, index2)
                        if (index1 != -1) {
                            outputBuffer.putFloat(outputPosition + 16, weight1)
                        }
                        if (index2 != -1) {
                            outputBuffer.putFloat(outputPosition + 20, 1f - weight1)
                        }
                    }
                    // BDEF4, or not really supported QDEF
                    2, 4 -> {
                        val index1 = readBoneIndex()
                        val index2 = readBoneIndex()
                        val index3 = readBoneIndex()
                        val index4 = readBoneIndex()
                        val weight1 = readWeight()
                        val weight2 = readWeight()
                        val weight3 = readWeight()
                        val weight4 = readWeight()
                        outputBuffer.putInt(outputPosition, index1)
                        outputBuffer.putInt(outputPosition + 4, index2)
                        outputBuffer.putInt(outputPosition + 8, index3)
                        outputBuffer.putInt(outputPosition + 12, index4)
                        if (index1 != -1) {
                            outputBuffer.putFloat(outputPosition + 16, weight1)
                        }
                        if (index2 != -1) {
                            outputBuffer.putFloat(outputPosition + 20, weight2)
                        }
                        if (index3 != -1) {
                            outputBuffer.putFloat(outputPosition + 24, weight3)
                        }
                        if (index4 != -1) {
                            outputBuffer.putFloat(outputPosition + 28, weight4)
                        }
                    }

                    3 -> {
                        // SDEF, not really supported, just treat as BDEF2
                        val index1 = readBoneIndex()
                        val index2 = readBoneIndex()
                        val weight1 = readWeight()
                        outputBuffer.putInt(outputPosition, index1)
                        outputBuffer.putInt(outputPosition + 4, index2)
                        if (index1 != -1) {
                            outputBuffer.putFloat(outputPosition + 16, weight1)
                        }
                        if (index2 != -1) {
                            outputBuffer.putFloat(outputPosition + 20, 1f - weight1)
                        }
                        val c = readVector3f(vec)
                        val r0 = readVector3f(vec)
                        val r1 = readVector3f(vec)
                    }
                }
                outputPosition += SKIN_VERTEX_ATTRIBUTE_SIZE

                // Skin edge scale
                inputPosition += 4
            }
            require(outputPosition == outputBuffer.capacity()) { "Bug: Not filled the entire output buffer" }

            vertices = vertexCount
            vertexBuffer = outputBuffer
            buffer.position(inputPosition)
        }

        private fun loadSurfaces(buffer: ByteBuffer) {
            val surfaceCount = buffer.getInt()
            if (surfaceCount % 3 != 0) {
                throw PmxLoadException("Bad surface count: $surfaceCount % 3 != 0")
            }
            val triangleCount = surfaceCount / 3
            val vertexIndexSize = globals.vertexIndexSize
            val indexBufferSize = vertexIndexSize * surfaceCount
            if (buffer.remaining() < indexBufferSize) {
                throw PmxLoadException("Bad surface data: should have $indexBufferSize bytes, but only ${buffer.remaining()} bytes available")
            }

            val outputIndicesArray = IntArray(surfaceCount)
            val outputIndices = IntBuffer.wrap(outputIndicesArray)
            // PMX use clockwise indices, but OpenGL use counterclockwise indices, so let's invert the order here.
            when (vertexIndexSize) {
                1 -> {
                    indexBufferType = Accessor.ComponentType.UNSIGNED_BYTE
                    for (i in 0 until triangleCount) {
                        outputIndices.put(buffer.get().toUByte().toInt())
                        val a = buffer.get().toUByte().toInt()
                        val b = buffer.get().toUByte().toInt()
                        outputIndices.put(b)
                        outputIndices.put(a)
                    }
                }

                2 -> {
                    indexBufferType = Accessor.ComponentType.UNSIGNED_SHORT
                    for (i in 0 until triangleCount) {
                        outputIndices.put(buffer.getShort().toUShort().toInt())
                        val a = buffer.getShort()
                        val b = buffer.getShort()
                        outputIndices.put(b.toUShort().toInt())
                        outputIndices.put(a.toUShort().toInt())
                    }
                }

                4 -> {
                    indexBufferType = Accessor.ComponentType.UNSIGNED_INT
                    for (i in 0 until triangleCount) {
                        outputIndices.put(buffer.getInt())
                        val a = buffer.getInt()
                        val b = buffer.getInt()
                        outputIndices.put(b)
                        outputIndices.put(a)
                    }
                }

                else -> throw AssertionError()
            }
            indexBuffer = outputIndicesArray
            indices = surfaceCount
        }

        private fun loadTextures(buffer: ByteBuffer) {
            val textureCount = buffer.getInt()
            if (textureCount < 0) {
                throw PmxLoadException("Bad texture count: $textureCount, should be at least zero")
            }
            textures = (0 until textureCount).map {
                try {
                    val pathString = loadString(buffer)
                    val buffer = context.loadExternalResource(
                        path = pathString,
                        type = LoadContext.ResourceType.TEXTURE,
                        caseInsensitive = true,
                        maxSize = 256 * 1024 * 1024,
                    )
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
                } catch (ex: Exception) {
                    logger.warn("Failed to load PMX texture", ex)
                    return@map null
                }
            }
        }

        private fun loadMaterials(buffer: ByteBuffer) {
            val materialCount = buffer.getInt()

            fun loadDrawingFlags(buffer: ByteBuffer): PmxMaterial.DrawingFlags {
                val byte = buffer.get().toUByte().toInt()
                fun loadBitfield(index: Int): Boolean = (byte and (1 shl index)) != 0
                return PmxMaterial.DrawingFlags(
                    noCull = loadBitfield(0),
                    groundShadow = loadBitfield(1),
                    drawShadow = loadBitfield(2),
                    receiveShadow = loadBitfield(3),
                    hasEdge = loadBitfield(4),
                    vertexColor = loadBitfield(5),
                    pointDrawing = loadBitfield(6),
                    lineDrawing = loadBitfield(7),
                )
            }

            val vertexToMaterialMap = VertexMaterialTable(vertices, materialCount)

            var indexOffset = 0
            materials = (0 until materialCount).map { materialIndex ->
                val pmxMaterial = PmxMaterial(
                    nameLocal = loadString(buffer),
                    nameUniversal = loadString(buffer),
                    diffuseColor = loadRgbaColor(buffer),
                    specularColor = loadRgbColor(buffer),
                    specularStrength = buffer.getFloat(),
                    ambientColor = loadRgbColor(buffer),
                    drawingFlags = loadDrawingFlags(buffer),
                    edgeColor = loadRgbaColor(buffer),
                    edgeScale = buffer.getFloat(),
                    textureIndex = loadTextureIndex(buffer),
                    environmentIndex = loadTextureIndex(buffer),
                    environmentBlendMode = when (val mode = buffer.get().toInt()) {
                        0 -> PmxMaterial.EnvironmentBlendMode.DISABLED
                        1 -> PmxMaterial.EnvironmentBlendMode.MULTIPLY
                        2 -> PmxMaterial.EnvironmentBlendMode.ADDICTIVE
                        3 -> PmxMaterial.EnvironmentBlendMode.ADDITIONAL_VEC4
                        else -> throw PmxLoadException("Unsupported environment blend mode: $mode")
                    },
                    toonReference = when (val type = buffer.get().toInt()) {
                        0 -> PmxMaterial.ToonReference.Texture(index = loadTextureIndex(buffer))
                        1 -> PmxMaterial.ToonReference.Internal(index = buffer.get().toUByte())
                        else -> throw PmxLoadException("Unsupported toon reference: $type")
                    },
                    metadata = loadString(buffer),
                    surfaceCount = buffer.getInt().also {
                        if (it < 0) {
                            throw PmxLoadException("Material with $it vertices. Should be greater than zero.")
                        }
                        if (it % 3 != 0) {
                            throw PmxLoadException("Material with $it % 3 != 0 vertices.")
                        }
                    },
                )

                if (pmxMaterial.surfaceCount == 0) {
                    return@map null
                }

                var nextRemappedVertexIndex = 0
                val remappedIndices = ByteBuffer.allocateDirect(pmxMaterial.surfaceCount * indexBufferType.byteLength)
                    .order(ByteOrder.nativeOrder())
                val remappedVertices = ByteBuffer.allocateDirect(pmxMaterial.surfaceCount * VERTEX_ATTRIBUTE_SIZE)
                for (index in indexOffset until (indexOffset + pmxMaterial.surfaceCount)) {
                    val vertexIndex = indexBuffer[index]
                    if (vertexIndex >= vertices) {
                        throw PmxLoadException("Vertex index $vertexIndex out of bounds")
                    }
                    var remappedIndex = vertexToMaterialMap.getLocalIndex(vertexIndex, materialIndex)
                    if (remappedIndex == -1) {
                        remappedIndex = nextRemappedVertexIndex++
                        vertexToMaterialMap.setLocalIndex(vertexIndex, materialIndex, remappedIndex)

                        vertexBuffer.position(vertexIndex * VERTEX_ATTRIBUTE_SIZE)
                        vertexBuffer.limit(vertexBuffer.position() + VERTEX_ATTRIBUTE_SIZE)
                        remappedVertices.put(vertexBuffer)
                        vertexBuffer.clear()
                    }
                    when (indexBufferType) {
                        Accessor.ComponentType.UNSIGNED_BYTE -> remappedIndices.put(remappedIndex.toByte())
                        Accessor.ComponentType.UNSIGNED_SHORT -> remappedIndices.putShort(remappedIndex.toShort())
                        Accessor.ComponentType.UNSIGNED_INT -> remappedIndices.putInt(remappedIndex)
                        else -> throw AssertionError()
                    }
                }

                indexOffset += pmxMaterial.surfaceCount
                remappedVertices.flip()
                remappedIndices.flip()
                vertexBuffer.clear()

                MaterialData(
                    material = pmxMaterial,
                    vertexAttributes = wrapVertexBuffer(
                        bufferName = "Vertex buffer for material ${pmxMaterial.nameLocal}",
                        buffer = remappedVertices,
                        vertexCount = nextRemappedVertexIndex,
                    ),
                    indexBufferView = BufferView(
                        buffer = Buffer(
                            name = "Index buffer for material ${pmxMaterial.nameLocal}",
                            buffer = remappedIndices,
                        ),
                        byteLength = remappedIndices.remaining(),
                        byteOffset = 0,
                        byteStride = 0,
                    ),
                    vertices = nextRemappedVertexIndex,
                )
            }

            this.vertexToMaterialMap = vertexToMaterialMap
        }

        private fun Vector3f.transformPosition() = also {
            mul(MMD_SCALE)
            x = -x
        }

        private fun loadBones(buffer: ByteBuffer) {
            val boneCount = buffer.getInt()
            if (boneCount < 0) {
                throw PmxLoadException("Bad PMX model: bones count less than zero")
            }

            fun loadBoneFlags(buffer: ByteBuffer): PmxBone.Flags {
                val flags = buffer.getShort().toInt()
                fun loadBitfield(index: Int): Boolean = (flags and (1 shl index)) != 0
                return PmxBone.Flags(
                    indexedTailPosition = loadBitfield(0),
                    rotatable = loadBitfield(1),
                    translatable = loadBitfield(2),
                    isVisible = loadBitfield(3),
                    enabled = loadBitfield(4),
                    ik = loadBitfield(5),
                    inheritLocal = loadBitfield(7),
                    inheritRotation = loadBitfield(8),
                    inheritTranslation = loadBitfield(9),
                    fixedAxis = loadBitfield(10),
                    localCoordinate = loadBitfield(11),
                    physicsAfterDeform = loadBitfield(12),
                    externalParentDeform = loadBitfield(13),
                )
            }

            fun loadBone(index: Int, buffer: ByteBuffer): PmxBone {
                val nameLocal = loadString(buffer)
                val nameUniversal = loadString(buffer)
                val position = loadVector3f(buffer).transformPosition()
                val parentBoneIndex = loadBoneIndex(buffer)
                val layer = buffer.getInt()
                val flags = loadBoneFlags(buffer)
                val tailPosition = if (flags.indexedTailPosition) {
                    PmxBone.TailPosition.Indexed(loadBoneIndex(buffer))
                } else {
                    PmxBone.TailPosition.Scalar(loadVector3f(buffer).transformPosition())
                }
                val inheritParent = if (flags.inheritRotation || flags.inheritTranslation) {
                    Pair(loadBoneIndex(buffer), buffer.getFloat())
                } else {
                    null
                }
                val axisDirection = if (flags.fixedAxis) {
                    loadVector3f(buffer).transformPosition()
                } else {
                    null
                }
                val localCoordinate = if (flags.localCoordinate) {
                    PmxBone.LocalCoordinate(
                        loadVector3f(buffer).transformPosition(),
                        loadVector3f(buffer).transformPosition()
                    )
                } else {
                    null
                }
                val externalParentIndex = if (flags.externalParentDeform) {
                    loadBoneIndex(buffer)
                } else {
                    null
                }
                val ikData = if (flags.ik) {
                    val targetIndex = loadBoneIndex(buffer)
                    val loopCount = buffer.getInt()
                    val limitRadian = buffer.getFloat()
                    val linkCount = buffer.getInt()
                    val links = (0 until linkCount).map {
                        val index = loadBoneIndex(buffer)
                        val limits = if (buffer.get() != 0.toByte()) {
                            PmxBone.IkLink.Limits(
                                limitMin = loadVector3f(buffer),
                                limitMax = loadVector3f(buffer),
                            )
                        } else {
                            null
                        }
                        PmxBone.IkLink(
                            index = index,
                            limits = limits,
                        )
                    }
                    PmxBone.IkData(
                        effectorIndex = index,
                        targetIndex = targetIndex,
                        loopCount = loopCount,
                        limitRadian = limitRadian,
                        links = links,
                    ).also {
                        effectorToIkDataMap.getOrPut(index, ::mutableListOf).add(it)
                    }
                } else {
                    null
                }
                return PmxBone(
                    index = index,
                    nameLocal = nameLocal,
                    nameUniversal = nameUniversal,
                    position = position,
                    parentBoneIndex = parentBoneIndex.takeIf { it >= 0 },
                    layer = layer,
                    flags = flags,
                    tailPosition = tailPosition,
                    inheritParentIndex = inheritParent?.first,
                    inheritParentInfluence = inheritParent?.second,
                    axisDirection = axisDirection,
                    localCoordinate = localCoordinate,
                    externalParentIndex = externalParentIndex,
                    ikData = ikData,
                ).also { bone ->
                    bone.inheritData?.let { inheritData ->
                        sourceToInheritMap.getOrPut(inheritData.sourceIndex) { mutableListOf() }.add(inheritData)
                    }
                }
            }

            bones = (0 until boneCount).map { index ->
                loadBone(index, buffer).also { bone ->
                    bone.parentBoneIndex?.let { parentBoneIndex ->
                        childBoneMap.getOrPut(parentBoneIndex) { mutableListOf() }.add(index)
                    } ?: run {
                        rootBones.add(index)
                    }
                }
            }
        }

        private fun loadMorphTargets(buffer: ByteBuffer) {
            val morphTargetCount = buffer.getInt()
            if (morphTargetCount < 0) {
                throw PmxLoadException("Bad PMX model: morph targets count less than zero")
            }

            val targets = mutableListOf<PmxMorph>()
            val morphGroups = mutableListOf<PmxMorphGroup>()
            for (index in 0 until morphTargetCount) {
                val nameLocal = loadString(buffer)
                val nameUniversal = loadString(buffer)
                val expressionTag =
                    Expression.Tag.fromPmxJapanese(nameLocal) ?: Expression.Tag.fromPmxEnglish(nameUniversal)
                val panelType = buffer.get().toInt()
                    .let { type -> PmxMorphPanelType.entries.firstOrNull { it.value == type } }
                    ?: throw PmxLoadException("Unknown panel type")
                val morphType = buffer.get().toInt()
                    .let { type -> PmxMorphType.entries.firstOrNull { it.value == type } }
                    ?: throw PmxLoadException("Unknown morph type")
                val offsetSize = buffer.getInt()
                if (offsetSize < 1) {
                    continue
                }
                when (morphType) {
                    PmxMorphType.VERTEX -> {
                        val dataMap = mutableMapOf<Int, BuildingVertexMorphTarget>()
                        for (i in 0 until offsetSize) {
                            // Get vertex index
                            val vertexIndex = loadVertexIndex(buffer)

                            // Push data into corresponding building morph target
                            val x = buffer.getFloat() * -MMD_SCALE
                            val y = buffer.getFloat() * MMD_SCALE
                            val z = buffer.getFloat() * MMD_SCALE

                            // Lookup each material
                            for (materialIndex in materials.indices) {
                                val material = materials[materialIndex] ?: continue
                                // Map global vertex index to material local
                                val materialLocalIndex = vertexToMaterialMap.getLocalIndex(vertexIndex, materialIndex)
                                if (materialLocalIndex == -1) {
                                    continue
                                }

                                // Fetch building morph target
                                val buildingTarget = dataMap.getOrPut(materialIndex) {
                                    BuildingVertexMorphTarget(material.vertices)
                                }
                                buildingTarget.setVertex(materialLocalIndex, x, y, z)
                            }
                        }
                        targets.add(
                            PmxMorph(
                                pmxIndex = index,
                                targetIndex = targets.size,
                                nameLocal = nameLocal.takeIf(String::isNotBlank),
                                nameUniversal = nameUniversal.takeIf(String::isNotBlank),
                                tag = expressionTag,
                                data = dataMap.mapNotNull { (materialIndex, value) ->
                                    val morphBuffer = value.finish()
                                    val material = materials[materialIndex] ?: return@mapNotNull null
                                    materialIndex to Primitive.Attributes.MorphTarget(
                                        position = Accessor(
                                            name = "Morph #$index material #$materialIndex vertex buffer",
                                            bufferView = BufferView(
                                                buffer = Buffer(
                                                    buffer = morphBuffer,
                                                ),
                                                byteLength = morphBuffer.capacity(),
                                                byteOffset = 0,
                                                byteStride = 12,
                                            ),
                                            componentType = Accessor.ComponentType.FLOAT,
                                            count = material.vertices,
                                            type = Accessor.AccessorType.VEC3,
                                        )
                                    )
                                }.toMap(),
                            )
                        )
                    }

                    PmxMorphType.GROUP -> {
                        morphGroups.add(
                            PmxMorphGroup(
                                nameLocal = nameLocal.takeIf(String::isNotBlank),
                                nameUniversal = nameUniversal.takeIf(String::isNotBlank),
                                tag = expressionTag,
                                items = (0 until offsetSize).map {
                                    MorphItem(
                                        index = loadMorphIndex(buffer),
                                        influence = buffer.getFloat(),
                                    )
                                },
                            )
                        )
                    }

                    PmxMorphType.UV, PmxMorphType.UV_EXT1, PmxMorphType.UV_EXT2, PmxMorphType.UV_EXT3, PmxMorphType.UV_EXT4 -> {
                        // Just skip, not really supported
                        val itemSize = globals.vertexIndexSize + 16
                        buffer.position(buffer.position() + itemSize * offsetSize)
                    }

                    PmxMorphType.BONE -> {
                        // Just skip, not really supported
                        val itemSize = globals.boneIndexSize + 28
                        buffer.position(buffer.position() + itemSize * offsetSize)
                    }

                    PmxMorphType.MATERIAL -> {
                        // Just skip, not really supported
                        val itemSize = globals.materialIndexSize + 113
                        buffer.position(buffer.position() + itemSize * offsetSize)
                    }

                    PmxMorphType.FLIP -> {
                        // Just skip, not really supported
                        val itemSize = globals.morphIndexSize + 4
                        buffer.position(buffer.position() + itemSize * offsetSize)
                    }

                    PmxMorphType.IMPULSE -> {
                        // Just skip, not really supported
                        val itemSize = globals.rigidBodyIndexSize + 25
                        buffer.position(buffer.position() + itemSize * offsetSize)
                    }
                }
            }
            morphTargets = targets
            morphTargetGroups = morphGroups
        }

        private fun loadDisplayFrames(buffer: ByteBuffer) {
            val displayFrameCount = buffer.getInt()
            if (displayFrameCount < 0) {
                throw PmxLoadException("Bad PMX model: display frames count less than zero")
            }
            val displayFrames = mutableListOf<PmxDisplayFrame>()
            repeat(displayFrameCount) {
                val nameLocal = loadString(buffer)
                val nameUniversal = loadString(buffer)
                val isSpecial = buffer.get() != 0.toByte()
                val frameCount = buffer.getInt()
                val frames = (0 until frameCount).map {
                    when (val type = buffer.get()) {
                        0.toByte() -> PmxDisplayFrame.FrameData.Bone(
                            boneIndex = loadBoneIndex(buffer),
                        )

                        1.toByte() -> PmxDisplayFrame.FrameData.Morph(
                            morphIndex = loadMorphIndex(buffer),
                        )

                        else -> throw PmxLoadException("Unknown frame type: $type")
                    }
                }
                displayFrames.add(
                    PmxDisplayFrame(
                        nameLocal = nameLocal,
                        nameUniversal = nameUniversal,
                        isSpecial = isSpecial,
                        frames = frames,
                    )
                )
            }
        }

        private fun loadRigidBodies(buffer: ByteBuffer) {
            val rigidBodyCount = buffer.getInt()
            if (rigidBodyCount < 0) {
                throw PmxLoadException("Bad PMX model: rigid bodies count less than zero")
            }

            fun loadShapeType(byte: Byte): PmxRigidBody.ShapeType = when (byte.toInt()) {
                0 -> PmxRigidBody.ShapeType.SPHERE
                1 -> PmxRigidBody.ShapeType.BOX
                2 -> PmxRigidBody.ShapeType.CAPSULE
                else -> throw PmxLoadException("Unsupported rigid body shape type: $byte")
            }

            fun loadPhysicsMode(byte: Byte): PmxRigidBody.PhysicsMode = when (byte.toInt()) {
                0 -> PmxRigidBody.PhysicsMode.FOLLOW_BONE
                1 -> PmxRigidBody.PhysicsMode.PHYSICS
                2 -> PmxRigidBody.PhysicsMode.PHYSICS_PLUS_BONE
                else -> throw PmxLoadException("Unsupported rigid body physics mode: $byte")
            }

            rigidBodies = (0 until rigidBodyCount).map { index ->
                PmxRigidBody(
                    nameLocal = loadString(buffer),
                    nameUniversal = loadString(buffer),
                    relatedBoneIndex = loadBoneIndex(buffer),
                    groupId = buffer.get().toUByte().toInt(),
                    nonCollisionGroup = buffer.getShort().toUShort().toInt(),
                    shape = loadShapeType(buffer.get()),
                    shapeSize = loadVector3f(buffer).mul(MMD_SCALE),
                    shapePosition = loadVector3f(buffer).transformPosition(),
                    shapeRotation = loadVector3f(buffer).also {
                        it.y *= -1
                        it.z *= -1
                    },
                    mass = buffer.getFloat(),
                    moveAttenuation = buffer.getFloat(),
                    rotationDamping = buffer.getFloat(),
                    repulsion = buffer.getFloat(),
                    frictionForce = buffer.getFloat(),
                    physicsMode = loadPhysicsMode(buffer.get())
                ).also {
                    if (it.relatedBoneIndex in bones.indices) {
                        boneToRigidBodyMap.getOrPut(it.relatedBoneIndex, ::mutableListOf).add(index)
                    } else if (bones.isNotEmpty()) {
                        // Allocate to first bone
                        // https://github.com/benikabocha/saba/blob/29b8efa8b31c8e746f9a88020fb0ad9dcdcf3332/src/Saba/Model/MMD/MMDPhysics.cpp#L434
                        boneToRigidBodyMap.getOrPut(0, ::mutableListOf).add(index)
                    } else {
                        // No bone? Ignore
                    }
                }
            }
        }

        private fun loadJoints(buffer: ByteBuffer) {
            val jointCount = buffer.getInt()
            if (jointCount < 0) {
                throw PmxLoadException("Bad PMX model: joints count less than zero")
            }

            fun loadJointType(byte: Byte): PmxJoint.JointType = PmxJoint.JointType.entries.firstOrNull {
                byte.toInt() == it.value
            } ?: throw PmxLoadException("Unsupported joint type: $byte")

            joints = (0 until jointCount).map {
                val nameLocal = loadString(buffer)
                val nameUniversal = loadString(buffer)
                val type = loadJointType(buffer.get())
                val rigidBodyIndexA = loadRigidBodyIndex(buffer)
                val rigidBodyIndexB = loadRigidBodyIndex(buffer)
                val position = loadVector3f(buffer).transformPosition()
                val rotation = loadVector3f(buffer).also {
                    it.y *= -1
                    it.z *= -1
                }

                val positionMinimumOrig = loadVector3f(buffer).transformPosition()
                val positionMaximumOrig = loadVector3f(buffer).transformPosition()
                val positionMinimum = Vector3f(positionMaximumOrig.x, positionMinimumOrig.y, positionMinimumOrig.z)
                val positionMaximum = Vector3f(positionMinimumOrig.x, positionMaximumOrig.y, positionMaximumOrig.z)

                val rotationMinimumOrig = loadVector3f(buffer)
                val rotationMaximumOrig = loadVector3f(buffer)
                val rotationMinimum = Vector3f(
                    rotationMinimumOrig.x,
                    -rotationMaximumOrig.y,
                    -rotationMaximumOrig.z,
                )
                val rotationMaximum = Vector3f(
                    rotationMaximumOrig.x,
                    -rotationMinimumOrig.y,
                    -rotationMinimumOrig.z,
                )
                val positionSpring = loadVector3f(buffer)
                val rotationSpring = loadVector3f(buffer)

                PmxJoint(
                    nameLocal = nameLocal,
                    nameUniversal = nameUniversal,
                    type = type,
                    rigidBodyIndexA = rigidBodyIndexA,
                    rigidBodyIndexB = rigidBodyIndexB,
                    position = position,
                    rotation = rotation,
                    positionMinimum = positionMinimum,
                    positionMaximum = positionMaximum,
                    rotationMinimum = rotationMinimum,
                    rotationMaximum = rotationMaximum,
                    positionSpring = positionSpring,
                    rotationSpring = rotationSpring,
                )
            }
        }

        private data class MaterialMorphData(
            val materialIndex: Int,
            val morphIndex: Int,
        )

        fun load(buffer: ByteBuffer): LoadResult {
            val header = loadHeader(buffer)
            loadVertices(buffer)
            loadSurfaces(buffer)
            loadTextures(buffer)
            loadMaterials(buffer)
            loadBones(buffer)
            loadMorphTargets(buffer)
            loadDisplayFrames(buffer)
            loadRigidBodies(buffer)
            loadJoints(buffer)

            val modelId = UUID.randomUUID()
            val rootNodes = mutableListOf<Node>()

            fun addBone(index: Int, parentPosition: Vector3fc? = null): Node {
                val bone = bones[index]
                val boneNodeId = NodeId(modelId, index)

                val children = childBoneMap[index]?.map {
                    addBone(it, bone.position)
                } ?: listOf()

                val components = buildList {
                    effectorToIkDataMap[index]?.forEach { data ->
                        add(
                            NodeComponent.IkTargetComponent(
                                ikTarget = IkTarget(
                                    limitRadian = data.limitRadian,
                                    loopCount = data.loopCount,
                                    joints = data.links.map { link ->
                                        IkTarget.IkJoint(
                                            nodeId = NodeId(modelId, link.index),
                                            limit = link.limits?.let {
                                                IkTarget.IkJoint.Limits(
                                                    min = Vector3f(
                                                        it.limitMin.x(),
                                                        -it.limitMax.y(),
                                                        -it.limitMax.z(),
                                                    ),
                                                    max = Vector3f(
                                                        it.limitMax.x(),
                                                        -it.limitMin.y(),
                                                        -it.limitMin.z(),
                                                    ),
                                                )
                                            }
                                        )
                                    },
                                    effectorNodeId = NodeId(modelId, data.effectorIndex),
                                ),
                                transformId = TransformId.IK,
                            )
                        )
                    }
                    sourceToInheritMap[index]?.forEach { data ->
                        add(
                            NodeComponent.InfluenceSourceComponent(
                                influence = Influence(
                                    target = NodeId(modelId, data.targetIndex),
                                    influence = data.influence,
                                    influenceRotation = data.inheritRotation,
                                    influenceTranslation = data.inheritTranslation,
                                    appendLocal = data.inheritLocal,
                                ),
                                transformId = TransformId.INFLUENCE,
                            )
                        )
                    }

                    boneToRigidBodyMap[index]?.forEach { index ->
                        add(
                            NodeComponent.RigidBodyComponent(
                                rigidBodyId = RigidBodyId(modelId, index),
                                rigidBody = rigidBodies[index].let { rigidBody ->
                                    val enableNameBasedOverrides = false
                                    val basePhysicsMode = when (rigidBody.physicsMode) {
                                        PmxRigidBody.PhysicsMode.FOLLOW_BONE -> RigidBody.PhysicsMode.FOLLOW_BONE
                                        PmxRigidBody.PhysicsMode.PHYSICS -> RigidBody.PhysicsMode.PHYSICS
                                        PmxRigidBody.PhysicsMode.PHYSICS_PLUS_BONE -> RigidBody.PhysicsMode.PHYSICS_PLUS_BONE
                                    }

                                    val nameLocal = rigidBody.nameLocal
                                    val adjustedPhysicsMode = if (enableNameBasedOverrides) {
                                        when {
                                            nameLocal.startsWith("Skirt_D_") ->
                                                RigidBody.PhysicsMode.FOLLOW_BONE
                                            nameLocal.startsWith("Ribbon_Braid_") -> basePhysicsMode
                                            nameLocal.startsWith("Ribbon_") ||
                                                nameLocal.startsWith("Pocket Watch_") ||
                                                nameLocal.startsWith("Strap_") ->
                                                RigidBody.PhysicsMode.FOLLOW_BONE
                                            nameLocal.startsWith("Skirt_") &&
                                                basePhysicsMode == RigidBody.PhysicsMode.PHYSICS ->
                                                RigidBody.PhysicsMode.PHYSICS_PLUS_BONE
                                            else -> basePhysicsMode
                                        }
                                    } else {
                                        basePhysicsMode
                                    }

                                    val baseGroup = 1 shl rigidBody.groupId
                                    val collisionMask = rigidBody.nonCollisionGroup and 0xFFFF
                                    val defaultMask = collisionMask
                                    println(
                                        "PHYSDBG RB_GROUP " +
                                            "idx=$index " +
                                            "name=${rigidBody.nameLocal} " +
                                            "groupId=${rigidBody.groupId} " +
                                            "nonColl=${rigidBody.nonCollisionGroup} " +
                                            "baseGroup=$baseGroup " +
                                            "defaultMask=$defaultMask " +
                                            "finalMask=$collisionMask",
                                    )

                                    RigidBody(
                                        name = rigidBody.nameLocal.takeIf(String::isNotBlank),
                                        collisionGroup = baseGroup,
                                        collisionMask = collisionMask,
                                        shape = when (rigidBody.shape) {
                                            PmxRigidBody.ShapeType.SPHERE -> RigidBody.ShapeType.SPHERE
                                            PmxRigidBody.ShapeType.BOX -> RigidBody.ShapeType.BOX
                                            PmxRigidBody.ShapeType.CAPSULE -> RigidBody.ShapeType.CAPSULE
                                        },
                                        shapeSize = rigidBody.shapeSize,
                                        shapePosition = rigidBody.shapePosition,
                                        shapeRotation = rigidBody.shapeRotation,
                                        mass = rigidBody.mass,
                                        moveAttenuation = (rigidBody.moveAttenuation + 0.05f).coerceAtMost(1f),
                                        rotationDamping = (rigidBody.rotationDamping + 0.05f).coerceAtMost(1f),
                                        repulsion = rigidBody.repulsion.coerceAtMost(0.1f),
                                        frictionForce = rigidBody.frictionForce,
                                        physicsMode = adjustedPhysicsMode,
                                    )
                                },
                            )
                        )
                    }
                }

                return Node(
                    name = bone.nameLocal,
                    id = boneNodeId,
                    transform = NodeTransform.Decomposed(
                        translation = Vector3f().set(bone.position).also {
                            if (parentPosition != null) {
                                it.sub(parentPosition)
                            }
                        },
                        rotation = Quaternionf(),
                        scale = Vector3f(1f),
                    ),
                    children = children,
                    components = components,
                )
            }

            rootBones.forEach { index ->
                rootNodes.add(addBone(index))
            }

            var nextNodeIndex = bones.size

            val joints = mutableListOf<NodeId>()
            val inverseBindMatrices = mutableListOf<Matrix4f>()
            val jointHumanoidTags = mutableListOf<HumanoidTag?>()

            for (boneIndex in bones.indices) {
                val bone = bones[boneIndex]
                val nodeId = NodeId(modelId, boneIndex)
                joints.add(nodeId)

                val inverseBindMatrix = Matrix4f().translation(bone.position).invertAffine()
                inverseBindMatrices.add(inverseBindMatrix)

                jointHumanoidTags.add(
                    HumanoidTag.fromPmxJapanese(bone.nameLocal)
                        ?: HumanoidTag.fromPmxEnglish(bone.nameUniversal)
                )
            }

            val skin = Skin(
                name = "PMX skin",
                joints = joints,
                inverseBindMatrices = inverseBindMatrices,
                jointHumanoidTags = jointHumanoidTags,
            )

            val pmxMorphToMaterialMorphIndexMap = mutableMapOf<Int, MutableList<MaterialMorphData>>()
            val materialMorphMap = mutableMapOf<Int, MutableList<Primitive.Attributes.MorphTarget>>()
            for ((morphIndex, pmxTarget) in morphTargets.withIndex()) {
                val materialMorphIndexList = pmxMorphToMaterialMorphIndexMap.getOrPut(morphIndex, ::mutableListOf)
                for ((materialIndex, target) in pmxTarget.data) {
                    val materialMorphList = materialMorphMap.getOrPut(materialIndex, ::mutableListOf)
                    val materialMorphIndex = materialMorphList.size
                    materialMorphList.add(target)
                    materialMorphIndexList.add(MaterialMorphData(materialIndex, materialMorphIndex))
                }
            }

            val materialToMeshIds = mutableMapOf<Int, MeshId>()
            materials.forEachIndexed { materialIndex, materialData ->
                val nodeIndex = nextNodeIndex++
                val nodeId = NodeId(modelId, nodeIndex)
                val meshId = MeshId(modelId, nodeIndex)
                materialToMeshIds[materialIndex] = meshId

                val pmxMaterial = materialData?.material ?: return@forEachIndexed
                val material = Material.Unlit(
                    name = pmxMaterial.nameLocal,
                    baseColor = pmxMaterial.diffuseColor,
                    baseColorTexture = pmxMaterial.textureIndex.takeIf {
                        it >= 0 && it in textures.indices
                    }?.let {
                        textures.getOrNull(it)
                    }?.let {
                        Material.TextureInfo(it)
                    },
                    doubleSided = pmxMaterial.drawingFlags.noCull,
                )

                rootNodes.add(
                    Node(
                        name = "Node for material ${pmxMaterial.nameLocal}",
                        id = nodeId,
                        transform = null,
                        components = buildList {
                            add(
                                NodeComponent.MeshComponent(
                                    mesh = Mesh(
                                        id = meshId,
                                        primitives = listOf(
                                            Primitive(
                                                mode = Primitive.Mode.TRIANGLES,
                                                material = material,
                                                attributes = materialData.vertexAttributes,
                                                indices = Accessor(
                                                    bufferView = materialData.indexBufferView,
                                                    componentType = indexBufferType,
                                                    normalized = false,
                                                    count = pmxMaterial.surfaceCount,
                                                    type = Accessor.AccessorType.SCALAR,
                                                ),
                                                targets = materialMorphMap[materialIndex] ?: listOf(),
                                            )
                                        ),
                                        weights = null,
                                    )
                                )
                            )
                            add(
                                NodeComponent.SkinComponent(
                                    skin = skin,
                                    meshIds = listOf(meshId),
                                )
                            )
                        }
                    )
                )
            }

            val cameraNodeIndex = nextNodeIndex++
            rootNodes.add(
                Node(
                    name = "MMD Camera",
                    id = NodeId(modelId, cameraNodeIndex),
                    components = listOf(
                        NodeComponent.CameraComponent(
                            Camera.MMD(name = "MMD Camera")
                        )
                    )
                )
            )

            val scene = Scene(nodes = rootNodes)

            val pmxIndexToExpressions = mutableMapOf<Int, Expression.Target>()
            return LoadResult(
                metadata = Metadata(
                    title = header.modelNameLocal,
                    titleUniversal = header.modelNameUniversal,
                    comment = header.commentLocal,
                    commentUniversal = header.commentUniversal,
                ),
                model = Model(
                    scenes = listOf(scene),
                    skins = listOf(skin),
                    physicalJoints = this.joints.mapNotNull { joint ->
                        if (joint.rigidBodyIndexA !in rigidBodies.indices) {
                            return@mapNotNull null
                        }
                        if (joint.rigidBodyIndexB !in rigidBodies.indices) {
                            return@mapNotNull null
                        }
                        PhysicalJoint(
                            name = joint.nameLocal.takeIf(String::isNotBlank),
                            type = when (joint.type) {
                                PmxJoint.JointType.SPRING_6DOF -> PhysicalJoint.JointType.SPRING_6DOF
                            },
                            rigidBodyA = RigidBodyId(modelId, joint.rigidBodyIndexA),
                            rigidBodyB = RigidBodyId(modelId, joint.rigidBodyIndexB),
                            position = joint.position,
                            rotation = joint.rotation,
                            positionMin = joint.positionMinimum,
                            positionMax = joint.positionMaximum,
                            rotationMin = joint.rotationMinimum,
                            rotationMax = joint.rotationMaximum,
                            positionSpring = joint.positionSpring,
                            rotationSpring = joint.rotationSpring,
                        )
                    },
                    expressions = buildList {
                        for ((index, target) in morphTargets.withIndex()) {
                            val expression = Expression.Target(
                                name = target.nameLocal ?: target.nameUniversal,
                                tag = target.tag,
                                isBinary = false,
                                bindings = pmxMorphToMaterialMorphIndexMap[index]?.mapNotNull { (materialIndex, targetIndex) ->
                                    Expression.Target.Binding.MeshMorphTarget(
                                        meshId = materialToMeshIds[materialIndex] ?: return@mapNotNull null,
                                        index = targetIndex,
                                        weight = 0f,
                                    )
                                } ?: listOf(),
                            )
                            pmxIndexToExpressions[target.pmxIndex] = expression
                            add(expression)
                        }
                        for (group in morphTargetGroups) {
                            add(
                                Expression.Group(
                                    name = group.nameLocal ?: group.nameUniversal,
                                    tag = group.tag,
                                    targets = group.items.mapNotNull { item ->
                                        val pmxMorphIndex = item.index
                                        val target = pmxIndexToExpressions[pmxMorphIndex] ?: return@mapNotNull null
                                        Expression.Group.TargetItem(
                                            target = target,
                                            influence = item.influence,
                                        )
                                    }
                                )
                            )
                        }
                    },
                    defaultScene = scene,
                ),
                animations = listOf(),
            )
        }
    }

    override fun load(path: Path, context: LoadContext, param: LoadParam) =
        FileChannel.open(path, StandardOpenOption.READ).use { channel ->
            val fileSize = channel.size()
            val buffer = runCatching {
                channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize)
            }.getOrNull() ?: run {
                if (fileSize > 32 * 1024 * 1024) {
                    throw PmxLoadException("PMX model size too large: maximum allowed is 32M, current is $fileSize")
                }
                val fileSize = fileSize.toInt()
                val buffer = ByteBuffer.allocate(fileSize)
                channel.readAll(buffer)
                buffer.flip()
                buffer
            }
            val context = Context(context, param)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            context.load(buffer)
        }
}
