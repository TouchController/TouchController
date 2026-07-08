@file:Suppress("UnstableApiUsage")

package top.fifthlight.intellij.bazelaspectbcr

import org.jetbrains.bazel.bazelrunner.BazelProcessLauncher
import org.jetbrains.bazel.bazelrunner.BazelProcessLauncherProvider
import java.nio.file.Path

class BcrAspectLauncherProvider : BazelProcessLauncherProvider {
    override fun createBazelProcessLauncher(
        workspaceRoot: Path,
        parentEnvironment: Map<String, String>,
    ): BazelProcessLauncher = BcrAspectLauncher(workspaceRoot, parentEnvironment)
}
