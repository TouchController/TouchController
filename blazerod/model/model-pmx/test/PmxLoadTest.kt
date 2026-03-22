package top.fifthlight.blazerod.model.pmx.test

import top.fifthlight.blazerod.model.pmx.PmxLoader
import java.nio.file.Path
import kotlin.test.Test
import kotlin.time.measureTime

class PmxLoadTest {
    private fun loadFilePath(attr: String): Path {
        val property = System.getProperty(attr + "_path")
        return Path.of(RunfileDummy.getRunfiles().rlocation(property))
    }

    @Test
    fun testAliciaSolid() {
        val file = loadFilePath("alicia_solid")
        measureTime {
            PmxLoader().load(file)
        }.let { duration ->
            println("Alicia solid load time: $duration")
        }
    }

    @Test
    fun testAliciaBlade() {
        val file = loadFilePath("alicia_blade")
        measureTime {
            PmxLoader().load(file)
        }.let { duration ->
            println("Alicia blade load time: $duration")
        }
    }
}
