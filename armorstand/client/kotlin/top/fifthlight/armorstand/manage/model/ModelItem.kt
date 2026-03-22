package top.fifthlight.armorstand.manage.model

import net.minecraft.resources.ResourceLocation
import top.fifthlight.armorstand.util.ModelHash
import java.nio.file.Path
import kotlin.io.path.extension

data class ModelItem(
    val path: Path,
    val name: String,
    val lastChanged: Long,
    val hash: ModelHash,
    var favorite: Boolean = false,
) {
    val type by lazy { Type.of(path) }

    enum class Type(
        val icon: ResourceLocation,
        val extensions: Set<String> = setOf(),
        val markerFiles: Set<String> = setOf(),
    ) {
        GLTF(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_gltf"),
            extensions = setOf("gltf", "glb"),
        ),
        VRM(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_vrm"),
            extensions = setOf("vrm"),
        ),
        PMX(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_pmx"),
            extensions = setOf("pmx"),
        ),
        PMD(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_pmd"),
            extensions = setOf("pmd"),
        ),
        FBX(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_fbx"),
            extensions = setOf("fbx"),
        ),
        OBJ(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_obj"),
            extensions = setOf("obj"),
        ),
        JSON(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_json"),
            markerFiles = setOf("ysm.json", "model.json"),
        ),
        UNKNOWN(
            icon = ResourceLocation.fromNamespaceAndPath("armorstand", "thumbnail_unknown"),
            extensions = setOf(),
        );

        companion object {
            private val extensionToTypeMap = buildMap {
                for (type in Type.entries) {
                    for (extension in type.extensions) {
                        put(extension, type)
                    }
                }
            }

            private val markerFileToTypeMap = buildMap {
                for (type in Type.entries) {
                    for (markerFile in type.markerFiles) {
                        put(markerFile, type)
                    }
                }
            }

            fun of(path: Path): Type {
                val fileName = path.fileName.toString().lowercase()
                markerFileToTypeMap[fileName]?.let { return it }
                val extension = path.extension
                extensionToTypeMap[extension]?.let { return it }
                return UNKNOWN
            }
        }
    }
}
