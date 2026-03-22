package top.fifthlight.armorstand.util

import kotlinx.coroutines.launch
import top.fifthlight.armorstand.ArmorStand
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.config.GlobalConfig
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.blazerod.render.version_1_21_8.api.render.Renderer
import top.fifthlight.blazerod.render.version_1_21_8.api.render.RendererTypeHolder
import top.fifthlight.blazerod.render.version_1_21_8.api.render.ScheduledRenderer

object RendererManager : AutoCloseable {
    init {
        ArmorStand.instance.scope.launch {
            ConfigHolder.config.collect { newConfig ->
                if (closed) {
                    return@collect
                }
                if (newConfig.renderer.type != currentRenderer.type) {
                    changeRenderer(newConfig.renderer.type)
                }
            }
        }
    }

    private var closed = false

    private fun requireNotClosed() = check(!closed) { "RendererManager is closed" }

    private fun getConfigRendererType() = when (ConfigHolder.config.value.renderer) {
        GlobalConfig.RendererKey.VERTEX_SHADER_TRANSFORM -> RendererTypeHolder.vertexShaderTransform
        GlobalConfig.RendererKey.CPU_TRANSFORM -> RendererTypeHolder.cpuTransform
        GlobalConfig.RendererKey.COMPUTE_SHADER_TRANSFORM -> RendererTypeHolder.computeShaderTransform
    }

    private var _currentRenderer: Renderer<*, *>? = null
    private var currentRendererSupportScheduling = false
    val currentRenderer: Renderer<*, *>
        get() {
            requireNotClosed()
            return _currentRenderer ?: changeRenderer(getConfigRendererType())
        }
    val currentRendererScheduled: ScheduledRenderer<*, *>?
        get() {
            requireNotClosed()
            return if (currentRendererSupportScheduling) {
                currentRenderer as ScheduledRenderer<*, *>
            } else {
                null
            }
        }

    private fun changeRenderer(type: Renderer.Type<*, *>): Renderer<*, *> {
        requireNotClosed()
        val renderer = type.create()
        if (_currentRenderer != null) {
            ModelInstanceManager.cleanAll()
        }
        _currentRenderer = renderer
        currentRendererSupportScheduling = renderer.type.supportScheduling
        return renderer
    }

    fun rotate() {
        _currentRenderer?.rotate()
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        _currentRenderer?.close()
    }
}
