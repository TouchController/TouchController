package top.fifthlight.blazerod.model.gltf.test

import top.fifthlight.blazerod.model.gltf.GltfBinaryLoader
import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.model.loader.ThumbnailResult
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.measureTime

class GlbLoadTest {
    private fun loadFilePath(attr: String): Path {
        val property = System.getProperty(attr + "_path")
        return Path.of(RunfileDummy.getRunfiles().rlocation(property))
    }

    @Test
    fun testAlicia() {
        val file = loadFilePath("alicia_solid")
        measureTime {
            GltfBinaryLoader().load(file)
        }.let { duration ->
            println("Alicia load time: $duration")
        }
    }

    @Test
    fun testArmorStand() {
        val file = loadFilePath("armorstand")
        measureTime {
            GltfBinaryLoader().load(file)
        }.let { duration ->
            println("armorstand load time: $duration")
        }
    }

    @Test
    fun testVrmThumbnail() {
        val file = loadFilePath("alicia_solid")
        measureTime {
            val result = GltfBinaryLoader().getThumbnail(file, LoadContext.Empty)
            assertIs<ThumbnailResult.Embed>(result)
            assertEquals(7739784, result.offset)
            assertEquals(138928, result.length)
        }.let { duration ->
            println("Alicia thumbnail time: $duration")
        }
    }

    @Test
    fun testInterpolation() {
        val file = loadFilePath("interpolation_test")
        measureTime {
            GltfBinaryLoader().load(file)
        }.let { duration ->
            println("Interpolation load time: $duration")
        }
    }
}
