@file:Suppress("UnstableApiUsage")

package top.fifthlight.intellij.bazelaspectbcr

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType
import com.intellij.execution.configurations.PtyCommandLine
import org.jetbrains.bazel.bazelrunner.BazelCommandExecutionDescriptor
import org.jetbrains.bazel.bazelrunner.BazelProcessLauncher
import java.nio.file.Path

class BcrAspectLauncher(
    private val workspaceRoot: Path,
    private val parentEnvironment: Map<String, String>,
) : BazelProcessLauncher {
    override fun launchProcess(executionDescriptor: BazelCommandExecutionDescriptor): Process {
        val modified = modifyCommand(executionDescriptor)
        return createProcess(modified)
    }

    private fun modifyCommand(desc: BazelCommandExecutionDescriptor): BazelCommandExecutionDescriptor {
        val extraAspects = AspectArgumentInjector.EP_NAME.extensionList
            .flatMap { it.additionalAspects() }
            .joinToString(", ")

        // Replace extracted aspects with the BCR one
        val newCommand = desc.command.map { arg ->
            if (arg.startsWith("--aspects=")) {
                val aspectContent = arg.removePrefix("--aspects=")
                val aspects = aspectContent.split(',')
                "--aspects=" + aspects.joinToString(separator = ",")  { aspect ->
                    if (aspect.startsWith("//.bazelbsp/")) {
                        "@intellij_aspect//" + aspect.removePrefix("//.bazelbsp/")
                    } else {
                        aspect
                    }
                }
            } else {
                arg
            }
        }.toMutableList()

        // Inject extra aspects if there is any aspects
        val aspectsIndex = newCommand.indexOfFirst { arg -> arg.startsWith("--aspects") }
        if (aspectsIndex != -1 && extraAspects.isNotEmpty()) {
            newCommand.add(aspectsIndex, "--aspects=$extraAspects")
        }
        return desc.copy(command = newCommand)
    }

    private fun createProcess(desc: BazelCommandExecutionDescriptor): Process {
        val ptyTermSize = desc.ptyTermSize
        val commandLine = if (ptyTermSize != null) {
            PtyCommandLine(desc.command)
                .withConsoleMode(true)
                .withInitialColumns(ptyTermSize.columns)
                .withInitialRows(ptyTermSize.rows)
        } else {
            GeneralCommandLine(desc.command)
        }
        commandLine.withParentEnvironmentType(ParentEnvironmentType.NONE)
        commandLine.withEnvironment(mapOf("TERM" to "xterm-256color") + parentEnvironment + desc.environment)
        commandLine.withWorkingDirectory(workspaceRoot)
        commandLine.withRedirectErrorStream(false)
        return commandLine.createProcess()
    }
}
