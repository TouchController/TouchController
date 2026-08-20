package top.fifthlight.intellij.kotlinhmpp

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import org.jetbrains.bazel.label.Label
import java.nio.file.Path

/**
 * Collect output files from BEP.
 */
internal class HmppBepCollector(
    private val groupName: String = "intellij-info",
    private val resolveOutputPath: (Path) -> Path,
) {
    private class NamedSet(val files: List<Path>, val children: List<String>)

    private var active = false
    private val namedSets = HashMap<String, NamedSet>()
    private val infoFileSetsByLabel = HashMap<Label, MutableSet<String>>()

    val isActive: Boolean
        get() = active

    fun handleEvent(event: BuildEventStreamProtos.BuildEvent) {
        if (!active) {
            if (event.hasOptionsParsed()) {
                active = true
            } else {
                return
            }
        }

        if (event.id.hasNamedSet()) {
            namedSets[event.id.namedSet.id] = NamedSet(
                files = event.namedSetOfFiles.filesList.map { it.toOutputPath() },
                children = event.namedSetOfFiles.fileSetsList.map { it.id },
            )
        }

        if (event.id.hasTargetCompleted()) {
            Label.parseOrNull(event.id.targetCompleted.label)?.let { label ->
                for (group in event.completed.outputGroupList) {
                    if (group.name == groupName) {
                        infoFileSetsByLabel.getOrPut(label) { mutableSetOf() }
                            .addAll(group.fileSetsList.map { it.id })
                    }
                }
            }
        }
    }

    fun buildFinished(): HmppInfoFiles {
        val filesByLabel = HashMap<Label, Set<Path>>()
        for ((label, setIds) in infoFileSetsByLabel) {
            val files = mutableSetOf<Path>()
            for (id in setIds) {
                val visited = mutableSetOf<String>()
                fun collectNamedSet(id: String) {
                    if (!visited.add(id)) return
                    val set = namedSets[id] ?: return
                    files += set.files
                    for (child in set.children) {
                        collectNamedSet(child)
                    }
                }

                collectNamedSet(id)
            }
            if (files.isNotEmpty()) {
                filesByLabel[label] = files
            }
        }
        namedSets.clear()
        infoFileSetsByLabel.clear()
        active = false
        return filesByLabel
    }

    private fun BuildEventStreamProtos.File.toOutputPath(): Path {
        val relative = if (pathPrefixList.isEmpty()) {
            Path.of(name)
        } else {
            val pathFragments = pathPrefixList.subList(1, pathPrefixCount) + name
            Path.of(pathPrefixList.first(), *pathFragments.toTypedArray())
        }
        return resolveOutputPath(relative)
    }
}
