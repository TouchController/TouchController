package top.fifthlight.intellij.kotlinhmpp

import top.fifthlight.intellij.bazelaspectbcr.AspectArgumentInjector

/**
 * Inject out aspect into aspect lists
 */
class AspectInjector : AspectArgumentInjector {
    override fun additionalAspects(): List<String> = listOf(Consts.KOTLIN_COMMON_INFO_ASPECT)
}
