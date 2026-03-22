package top.fifthlight.blazerod.model.gltf

import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.LoadParam
import top.fifthlight.blazerod.model.loader.LoadResult
import top.fifthlight.blazerod.model.loader.ModelFileLoader
import java.nio.ByteBuffer
import java.nio.file.Path
import kotlin.io.path.readText

class GltfTextLoader : ModelFileLoader {
    override val extensions = mapOf(
        "gltf" to setOf(
            ModelFileLoader.Ability.MODEL,
            ModelFileLoader.Ability.EMBED_ANIMATION,
        ),
    )

    // There is no reliable way to probe a glTF file
    override val probeLength: Int? = null
    override fun probe(buffer: ByteBuffer) = false

    override fun load(path: Path, context: LoadContext, param: LoadParam): LoadResult {
        val json = path.readText()
        return GltfLoader(
            buffer = null,
            filePath = path,
            context = context,
            param = LoadParam(),
        ).load(json)
    }
}
