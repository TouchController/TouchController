package top.fifthlight.combine.backend.minecraft.identifier.v1_20_1

import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.core.data.Identifier as CombineIdentifier

fun CombineIdentifier.toMinecraft(): ResourceLocation = when (this) {
    is CombineIdentifier.Vanilla -> ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, id)
    is CombineIdentifier.Namespaced -> ResourceLocation(namespace, id)
}

fun ResourceLocation.toCombine() = if (this.namespace == "minecraft") {
    CombineIdentifier.Vanilla(path)
} else {
    CombineIdentifier.Namespaced(namespace, path)
}
