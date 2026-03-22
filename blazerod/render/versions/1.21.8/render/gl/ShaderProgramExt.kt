package top.fifthlight.blazerod.render.version_1_21_8.render.gl

import com.mojang.blaze3d.opengl.GlProgram
import com.mojang.blaze3d.opengl.GlShaderModule
import com.mojang.blaze3d.opengl.GlStateManager
import net.minecraft.client.renderer.ShaderManager
import org.lwjgl.opengl.GL20C
import org.slf4j.LoggerFactory

object ShaderProgramExt {
    private val LOGGER = LoggerFactory.getLogger(ShaderProgramExt::class.java)

    @JvmStatic
    @Throws(ShaderManager.CompilationException::class)
    fun create(shaderModule: GlShaderModule, name: String): GlProgram {
        val programId = GlStateManager.glCreateProgram().also { programId ->
            if (programId <= 0) {
                throw ShaderManager.CompilationException("Could not create shader program (returned program ID $programId)")
            }
        }
        GlStateManager.glAttachShader(programId, shaderModule.shaderId)
        GlStateManager.glLinkProgram(programId)
        val linkStatus = GlStateManager.glGetProgrami(programId, GL20C.GL_LINK_STATUS)
        val infoLog = GlStateManager.glGetProgramInfoLog(programId, 32768)
        return if (linkStatus != 0 && "Failed for unknown reason" !in infoLog) {
            if (infoLog.isNotEmpty()) {
                LOGGER.info("Info log when linking program containing CS {}. Log output: {}", shaderModule.id, infoLog)
            }

            GlProgram(programId, name)
        } else {
            throw ShaderManager.CompilationException("Error encountered when linking program containing CS ${shaderModule.id}. Log output: $infoLog")
        }
    }
}
