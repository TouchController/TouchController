package top.fifthlight.combine.backend.minecraft.identifier.v26_2

import net.minecraft.resources.Identifier
import top.fifthlight.combine.data.Identifier as CombineIdentifier

fun CombineIdentifier.toMinecraft(): Identifier = when (this) {
    is CombineIdentifier.Vanilla -> Identifier.withDefaultNamespace(id)
    is CombineIdentifier.Namespaced -> Identifier.fromNamespaceAndPath(namespace, id)
}

fun Identifier.toCombine() = if (this.namespace == "minecraft") {
    CombineIdentifier.Vanilla(path)
} else {
    CombineIdentifier.Namespaced(namespace, path)
}
