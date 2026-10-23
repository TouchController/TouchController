package top.fifthlight.combine.backend.minecraft.item.v1_20_1

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import top.fifthlight.combine.backend.minecraft.identifier.v1_20_1.toCombine
import top.fifthlight.combine.backend.minecraft.identifier.v1_20_1.toMinecraft
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import kotlin.jvm.optionals.getOrNull
import top.fifthlight.combine.item.data.Item as CombineItem

@ActualImpl(CombineItem::class)
data class ItemImpl(
    val inner: Item,
) : CombineItem {
    override val id: Identifier
        get() = BuiltInRegistries.ITEM.getKey(inner).toCombine()

    companion object : CombineItem.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(id: Identifier): CombineItem? {
            val item = BuiltInRegistries.ITEM.getOptional(id.toMinecraft()).getOrNull() ?: return null
            return ItemImpl(item)
        }
    }
}

fun Item.toCombine() = ItemImpl(this)
fun CombineItem.toVanilla() = (this as ItemImpl).inner
