@file:Suppress("UnstableApiUsage")
package top.fifthlight.intellij.kotlinhmpp

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.bazel.label.Label
import org.jetbrains.bsp.protocol.TaskGroupId
import org.jetbrains.bsp.protocol.TaskId
import java.nio.file.Path

/**
 * Label string -> set of `*.intellij-info.txt` file.
 */
typealias HmppInfoFiles = Map<Label, Set<Path>>

/**
 * Store HmppInfoFiles for further read.
 *
 * The Bazel plugins hard-coded the schema of `*.intellij-info.txt` file,
 * so we need a way to parse these files with **our** schema.
 */
@Service(Service.Level.PROJECT)
class HmppInfoService {
    private var datasets: MutableMap<TaskGroupId, HmppInfoFiles> = mutableMapOf()

    fun publish(taskGroupId: TaskGroupId, files: HmppInfoFiles) = synchronized(this) {
        datasets[taskGroupId] = datasets[taskGroupId]?.let { it + files } ?: files
    }

    fun consumeAll(taskGroupId: TaskGroupId): HmppInfoFiles? = synchronized(this) {
        datasets.remove(taskGroupId)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): HmppInfoService = project.getService(HmppInfoService::class.java)
    }
}
