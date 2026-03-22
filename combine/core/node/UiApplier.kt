package top.fifthlight.combine.node

import androidx.compose.runtime.AbstractApplier

class UiApplier(rootNode: LayoutNode) : AbstractApplier<LayoutNode>(rootNode) {
    override fun insertTopDown(index: Int, instance: LayoutNode) = Unit

    override fun insertBottomUp(index: Int, instance: LayoutNode) {
        current.children.add(index, instance)
        check(instance.parent == null) {
            "$instance must not have a parent when being inserted."
        }
        instance.parent = current
    }

    override fun remove(index: Int, count: Int) {
        current.children.remove(index, count)
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.children.move(from, to, count)
    }

    override fun onClear() {
        current.children.clear()
    }
}
