@file:Suppress("UnstableApiUsage")
package top.fifthlight.intellij.kotlinhmpp

import org.jetbrains.bazel.commons.RuleType
import org.jetbrains.bazel.commons.TargetKind
import org.jetbrains.bazel.sync.JavaLanguageClass
import org.jetbrains.bazel.sync.workspace.targetKind.TargetKindProvider

class TargetKindProvider: TargetKindProvider {
    internal companion object {
        val ktCommonKind = TargetKind(
            kind = Consts.KT_COMMON_LIBRARY_KIND,
            languageClasses = setOf(JavaLanguageClass.KOTLIN),
            ruleType = RuleType.LIBRARY,
        )
    }

    override val targetKinds: Set<TargetKind> = setOf(ktCommonKind)
}
