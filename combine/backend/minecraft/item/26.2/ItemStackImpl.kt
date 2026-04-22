package top.fifthlight.combine.backend.minecraft.item.v26_2

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import top.fifthlight.combine.backend.minecraft.identifier.v26_2.toCombine
import top.fifthlight.combine.backend.minecraft.identifier.v26_2.toMinecraft
import top.fifthlight.combine.backend.minecraft.text.v26_2.TextImpl
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import kotlin.jvm.optionals.getOrNull
import top.fifthlight.combine.item.data.ItemStack as CombineItemStack

@ActualImpl(CombineItemStack::class)
data class ItemStackImpl(
    val inner: ItemStack,
) : CombineItemStack {
    override val amount: Int
        get() = inner.count

    override val id: Identifier
        get() = BuiltInRegistries.ITEM.getKey(inner.item).toCombine()

    override val item: Item
        get() = ItemImpl(inner.item)

    override val isEmpty: Boolean
        get() = inner.isEmpty

    override val name: Text
        get() = TextImpl(inner.itemName)

    override fun withAmount(amount: Int) = ItemStackImpl(inner.copyWithCount(amount))

    companion object : CombineItemStack.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(
            item: Item,
            amount: Int,
        ): CombineItemStack {
            val minecraftItem = (item as ItemImpl).inner
            val stack = ItemStack(minecraftItem, amount)
            return ItemStackImpl(stack)
        }

        @ActualConstructor
        @JvmStatic
        override fun create(
            id: Identifier,
            amount: Int,
        ): CombineItemStack? {
            val item = BuiltInRegistries.ITEM.getOptional(id.toMinecraft()).getOrNull() ?: return null
            val stack = ItemStack(item, amount)
            return ItemStackImpl(stack)
        }
    }
}

fun ItemStack.toCombine() = ItemStackImpl(this)
fun CombineItemStack.toVanilla() = (this as ItemStackImpl).inner
