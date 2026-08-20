package top.fifthlight.intellij.expectactual

import com.intellij.openapi.util.IconLoader

abstract class AbstractAssets protected constructor() {
    protected fun loadIcon(path: String) = IconLoader.getIcon(path, this::class.java)
}
