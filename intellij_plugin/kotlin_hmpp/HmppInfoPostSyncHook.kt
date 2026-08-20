package top.fifthlight.intellij.kotlinhmpp

import org.jetbrains.bazel.sync.ProjectPostSyncHook

/**
 * A post-sync hook to ensure data collected by BEP is correctly cleared to avoid leaks.
 */
class HmppInfoPostSyncHook : ProjectPostSyncHook {
    override suspend fun onPostSync(environment: ProjectPostSyncHook.ProjectPostSyncHookEnvironment) {
        HmppInfoService.getInstance(environment.project).consumeAll(environment.taskId.taskGroupId)
    }
}
