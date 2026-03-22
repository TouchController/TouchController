package top.fifthlight.blazerod.render.version_1_21_8.runtime

import org.joml.Matrix4f
import org.joml.Matrix4fc
import top.fifthlight.blazerod.render.common.BlazeRod
import top.fifthlight.blazerod.render.api.resource.RenderTask
import top.fifthlight.blazerod.render.common.runtime.data.LocalMatricesBuffer
import top.fifthlight.blazerod.render.common.runtime.data.MorphTargetBuffer
import top.fifthlight.blazerod.render.common.runtime.data.RenderSkinBuffer
import top.fifthlight.blazerod.render.common.util.cowbuffer.CowBuffer

class RenderTaskImpl(
    val instance: ModelInstanceImpl,
    val light: Int,
    val overlay: Int,
    modelMatrix: Matrix4fc,
    val localMatricesBuffer: CowBuffer<LocalMatricesBuffer>,
    val skinBuffer: List<CowBuffer<RenderSkinBuffer>>,
    val morphTargetBuffer: List<CowBuffer<MorphTargetBuffer>>,
) : RenderTask {
    val modelMatrix: Matrix4f = Matrix4f(modelMatrix)
    private var released: Boolean = false

    init {
        instance.increaseReferenceCount()
        localMatricesBuffer.increaseReferenceCount()
        skinBuffer.forEach { it.increaseReferenceCount() }
        morphTargetBuffer.forEach { it.increaseReferenceCount() }
    }

    override fun release() {
        if (released) {
            return
        }
        released = true
        instance.decreaseReferenceCount()
        localMatricesBuffer.decreaseReferenceCount()
        skinBuffer.forEach { it.decreaseReferenceCount() }
        morphTargetBuffer.forEach { it.decreaseReferenceCount() }
    }
}

class TaskMap : AutoCloseable {
    private var closed = false
    private val tasks = mutableMapOf<RenderSceneImpl, MutableList<RenderTaskImpl>>()

    private fun checkNotClosed() = check(!closed) { "TaskMap is closed" }

    fun addTask(task: RenderTaskImpl) {
        checkNotClosed()
        tasks.getOrPut(task.instance.scene) { mutableListOf() }.add(task)
    }

    fun executeTasks(executor: (RenderSceneImpl, List<RenderTaskImpl>) -> Unit) {
        checkNotClosed()
        for ((scene, tasks) in tasks) {
            if (tasks.size > BlazeRod.INSTANCE_SIZE) {
                for (chunk in tasks.chunked(BlazeRod.INSTANCE_SIZE)) {
                    executor(scene, chunk)
                }
            } else {
                executor(scene, tasks)
            }
            for (task in tasks) {
                task.release()
            }
        }
        tasks.clear()
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        for ((_, tasks) in tasks) {
            for (task in tasks) {
                task.release()
            }
        }
        tasks.clear()
    }
}
