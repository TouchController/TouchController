@file:Suppress("UnstableApiUsage")
package top.fifthlight.intellij.kotlinhmpp

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import org.jetbrains.bazel.server.bep.BepEventHandler
import org.jetbrains.bazel.server.bep.BepEventHandlerContext
import org.jetbrains.bazel.server.bep.BepEventHandlerProvider

/**
 * Collect aspect outputs via BEP, and feed them into [HmppInfoService].
 */
class BepEventHandlerProvider : BepEventHandlerProvider {
    override fun create(context: BepEventHandlerContext): BepEventHandler {
        val service = HmppInfoService.getInstance(context.project)
        val collector = HmppBepCollector(
            resolveOutputPath = { context.bazelPathsResolver.resolveOutput(it) }
        )
        return object : BepEventHandler {
            override fun handleEvent(event: BuildEventStreamProtos.BuildEvent): Boolean {
                if (event.hasFinished()) {
                    if (collector.isActive) {
                        service.publish(context.parentId.taskGroupId, collector.buildFinished())
                    }
                } else {
                    collector.handleEvent(event)
                }
                return false
            }
        }
    }
}
