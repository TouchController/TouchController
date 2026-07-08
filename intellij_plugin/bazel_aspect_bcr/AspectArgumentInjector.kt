package top.fifthlight.intellij.bazelaspectbcr

import com.intellij.openapi.extensions.ExtensionPointName

interface AspectArgumentInjector {
    fun additionalAspects(): List<String>

    companion object {
        @JvmField
        val EP_NAME = ExtensionPointName.create<AspectArgumentInjector>("top.fifthlight.bazelaspectbcr.aspectArgumentInjector")
    }
}
