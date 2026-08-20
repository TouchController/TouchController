@file:Suppress("UnstableApiUsage")

package top.fifthlight.intellij.kotlinhmpp

import com.intellij.build.events.MessageEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.NlsContexts
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.entities
import org.jetbrains.bazel.label.DependencyLabelKind
import org.jetbrains.bazel.label.Label
import org.jetbrains.bazel.magicmetamodel.formatAsModuleName
import org.jetbrains.bazel.sync.includesKotlin
import org.jetbrains.bazel.sync.workspace.importer.BazelWorkspaceImporter
import org.jetbrains.bazel.sync.workspace.importer.WorkspaceImporterContext
import org.jetbrains.bazel.sync.workspace.importer.WorkspaceImporterPhase
import org.jetbrains.bazel.sync.workspace.importer.WorkspaceImporterResult
import org.jetbrains.bazel.sync.workspace.languages.jvm.KotlinBuildTarget
import org.jetbrains.bazel.sync.workspace.snapshot.WorkspaceSnapshot
import org.jetbrains.bazel.sync.workspace.snapshot.WorkspaceTarget
import org.jetbrains.bazel.sync.workspace.snapshot.WorkspaceTargetKey
import org.jetbrains.bazel.sync.workspace.snapshot.allTargets
import org.jetbrains.bazel.sync.workspace.snapshot.findBuildData
import org.jetbrains.bazel.sync.workspace.snapshot.kind
import org.jetbrains.bazel.workspace.importer.KotlinFacetBuilder
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.idea.workspaceModel.CompilerArgumentsSerializer
import org.jetbrains.kotlin.idea.workspaceModel.kotlinSettings
import org.jetbrains.kotlin.idea.workspaceModel.modifyKotlinSettingsEntity
import top.fifthlight.intellij.kotlinhmpp.proto.HmppInfoProtos
import java.nio.file.Path

/**
 * Importer for HMPP modules.
 *
 * The Bazel plugin has already created modules for kt_common_library, and wired
 * up the dependencies for our libraries. So this importer does:
 *
 * - Add a Kotlin facet to kt_common_library modules, so they are recognized as
 *   Kotlin modules.
 * - Set compiler arguments for modules depending on kt_common_library.
 *
 * All information comes from [HmppInfoService].
 */
class KotlinHmppWorkspaceImporter : BazelWorkspaceImporter.Named {
    companion object {
        private val LOG = logger<KotlinHmppWorkspaceImporter>()
    }

    private data class HmppFragment(
        val name: String,
        val sources: List<Path>,
        val refines: List<WorkspaceTargetKey>,
    )

    private data class HmppCompilerArgument(
        val fragments: List<String>,
        val sources: List<String>,
        val refines: List<String>,
    ) {
        companion object {
            fun build(
                commonTargets: Map<WorkspaceTargetKey, CommonInfo>,
                fragment: HmppFragment,
            ): HmppCompilerArgument {
                val fragments = mutableListOf<String>()
                val sources = mutableListOf<String>()
                val refines = mutableListOf<String>()

                val visitedFragments = mutableSetOf<HmppFragment>()
                fun visit(fragment: HmppFragment) {
                    if (!visitedFragments.add(fragment)) {
                        return
                    }

                    fragments.add(fragment.name)
                    fragment.sources.forEach { sources.add("${fragment.name}:$it") }
                    fragment.refines
                        .mapNotNull { commonTargets[it]?.fragment }
                        .forEach {
                            refines.add("${fragment.name}:${it.name}")
                            visit(it)
                        }
                }
                visit(fragment)
                return HmppCompilerArgument(fragments, sources, refines)
            }
        }

        fun toCommandArgs() = buildList {
            add("-Xmulti-platform")
            add("-Xexpect-actual-classes")
            fragments.forEach { add("-Xfragments=$it") }
            sources.forEach { add("-Xfragment-sources=$it") }
            refines.forEach { add("-Xfragment-refines=$it") }
        }
    }

    private data class CommonInfo(
        val workspaceModuleName: String,
        val fragment: HmppFragment,
    )

    private data class JvmConsumer(
        val key: WorkspaceTargetKey,
        val workspaceModuleName: String,
        val labelTargetName: String,
        val bazelModuleName: String?,
        val sources: List<Path>,
        val directCommonDependencies: List<WorkspaceTargetKey>,
    )

    private data class State(
        val commonTargets: Map<WorkspaceTargetKey, CommonInfo>,
        val jvmConsumers: List<JvmConsumer>,
    )

    private var state: State? = null

    override val importerName: @NlsContexts.ProgressTitle String
        get() = "Kotlin HMPP"

    override suspend fun import(
        context: WorkspaceImporterContext,
        phase: WorkspaceImporterPhase,
        snapshot: WorkspaceSnapshot,
    ): Result<WorkspaceImporterResult> = runCatching {
        when (phase) {
            WorkspaceImporterPhase.Initialize -> onInitialize(context, snapshot)
            is WorkspaceImporterPhase.WorkspaceApply -> onWorkspaceApply(phase.builder)
            else -> WorkspaceImporterResult.Success
        }
    }

    private fun onInitialize(context: WorkspaceImporterContext, snapshot: WorkspaceSnapshot): WorkspaceImporterResult {
        val hmppInfoPaths = HmppInfoService.getInstance(context.project).consumeAll(context.taskId.taskGroupId) ?: run {
            context.taskConsole.addDiagnosticMessage(
                taskId = context.taskId,
                message = "Unable to fetch HMPP info from service.",
                severity = MessageEvent.Kind.ERROR,
            )
            return WorkspaceImporterResult.Abort
        }
        val hmppInfos = mutableMapOf<Label, HmppInfoProtos.TargetIdeInfo>()
        for ((label, paths) in hmppInfoPaths) {
            if (label in hmppInfos) {
                continue
            }

            for (path in paths) {
                val info = HmppInfoParser.parse(path) ?: continue
                if (!info.hasKotlinCommonTargetInfo()) {
                    continue
                }

                val realLabel = Label.parse(info.key.label)
                if (realLabel in hmppInfos) {
                    break
                }

                hmppInfos[realLabel] = info
            }
        }

        val commonByKey = LinkedHashMap<WorkspaceTargetKey, CommonInfo>()
        val jvmConsumers = ArrayList<JvmConsumer>()
        for (target in snapshot.allTargets) {
            val dependencyCommonLibraryTargets = target.rawBuildTarget.dependencies.filter {
                it.kind == DependencyLabelKind.COMPILE || it.kind == DependencyLabelKind.EXPORTED_COMPILE_TIME
            }.filter {
                snapshot.targetGraph.findTargetByKey(
                    it.targetKey,
                    strict = true
                )?.rawBuildTarget?.kind == TargetKindProvider.ktCommonKind
            }.takeIf { it.isNotEmpty() }

            val sources = target.rawBuildTarget.let { target ->
                target.sources.getFiles() + target.generatedSources.getFiles()
            }.toList()

            if (target.kind == Consts.KT_COMMON_LIBRARY_KIND) {
                val hmppInfo = hmppInfos[target.targetKey.label] ?: continue
                commonByKey[target.targetKey] = CommonInfo(
                    workspaceModuleName = target.targetKey.label.formatAsModuleName(snapshot.repoMapping),
                    fragment = HmppFragment(
                        name = hmppInfo.kotlinCommonTargetInfo.fragmentName,
                        sources = sources,
                        refines = dependencyCommonLibraryTargets?.map { it.targetKey } ?: emptyList()
                    ),
                )
            } else if (target.rawBuildTarget.kind.includesKotlin()) {
                val kotlinBuildTarget = target.findBuildData<KotlinBuildTarget>()
                jvmConsumers += JvmConsumer(
                    key = target.targetKey,
                    workspaceModuleName = target.targetKey.label.formatAsModuleName(snapshot.repoMapping),
                    labelTargetName = target.targetKey.label.targetName,
                    bazelModuleName = kotlinBuildTarget?.moduleName,
                    sources = sources,
                    directCommonDependencies = dependencyCommonLibraryTargets?.map { it.targetKey } ?: emptyList(),
                )
            }
        }
        if (commonByKey.isEmpty()) {
            state = null
            return WorkspaceImporterResult.Abort
        }

        state = State(commonByKey, jvmConsumers)
        return WorkspaceImporterResult.Success
    }

    private fun onWorkspaceApply(builder: MutableEntityStorage): WorkspaceImporterResult {
        val state = this.state ?: return WorkspaceImporterResult.Success
        val modulesByName = builder.entities<ModuleEntity>().associateBy { it.name }

        // For common modules: add Kotlin facet
        val commonModuleNames = mutableSetOf<String>()
        for ((workspaceModuleName, fragment) in state.commonTargets.values) {
            if (!commonModuleNames.add(workspaceModuleName)) {
                continue
            }

            val module = builder.resolve(ModuleId(workspaceModuleName)) ?: continue
            if (module.kotlinSettings.isNotEmpty()) continue
            KotlinFacetBuilder.write(
                kotlinBuildTarget = KotlinBuildTarget(
                    languageVersion = null,
                    apiVersion = null,
                    kotlincOptions = HmppCompilerArgument.build(state.commonTargets, fragment).toCommandArgs(),
                    associates = emptyList(),
                    moduleName = fragment.name,
                ),
                isTestModule = false,
                associates = emptySet(),
                parentModuleEntity = module,
                storage = builder,
            )
        }

        // patch the existing JVM facet's compiler arguments with fragment data
        for ((key, workspaceModuleName, labelTargetName, bazelModuleName, sources, directCommonDependencies) in state.jvmConsumers) {
            val module = modulesByName[workspaceModuleName]
            if (module == null) {
                LOG.warn("No module $workspaceModuleName for target $key; skipping fragment arguments")
                continue
            }
            val facet = module.kotlinSettings.firstOrNull()
            if (facet == null) {
                LOG.warn("No Kotlin facet on module $workspaceModuleName; skipping fragment arguments")
                continue
            }
            val rootName = bazelModuleName?.takeIf { it.isNotBlank() } ?: labelTargetName
            val args = HmppCompilerArgument.build(state.commonTargets, HmppFragment(
                name = rootName,
                sources = sources,
                refines = directCommonDependencies,
            ))
            runCatching {
                CompilerArgumentsSerializer.deserializeFromString(facet.compilerArguments)
            }.getOrNull()?.apply {
                multiPlatform = true
                expectActualClasses = true
                fragments = args.fragments.toTypedArray()
                fragmentSources = args.sources.toTypedArray()
                fragmentRefines = args.refines.toTypedArray()
            }?.let(CompilerArgumentsSerializer::serializeToString)?.let {
                builder.modifyKotlinSettingsEntity(facet) {
                    compilerArguments = it
                }
            }
        }

        return WorkspaceImporterResult.Success
    }
}

