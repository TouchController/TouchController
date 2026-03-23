package top.fifthlight.combine.backend.minecraft.textbuilder.v26_1

import net.minecraft.network.chat.Component
import top.fifthlight.combine.backend.minecraft.text.v26_1.TextImpl
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.TextBuilder
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.combine.data.Text as CombineText

@ActualImpl(TextFactory::class)
object TextFactoryImpl : TextFactory {
    @ActualConstructor
    @JvmStatic
    fun of() = this

    override fun build(block: TextBuilder.() -> Unit) = TextBuilderImpl().apply(block).build()

    override fun literal(string: String) = TextImpl(Component.literal(string))

    private fun transformIdentifier(identifier: Identifier) = when (identifier) {
        is Identifier.Namespaced -> "${identifier.namespace}.${identifier.id}"
        is Identifier.Vanilla -> identifier.id
    }

    override fun of(identifier: Identifier) = TextImpl(Component.translatable(transformIdentifier(identifier)))

    override fun of(id: String) = TextImpl(Component.translatable(id))

    override fun empty() = TextImpl.EMPTY

    // Why Kotlin infer it as Array<Any>? It should be inferred as Array<Any?>
    @Suppress("UNCHECKED_CAST")
    override fun format(identifier: Identifier, vararg arguments: Any?) =
        TextImpl(Component.translatable(transformIdentifier(identifier), *(arguments as Array<out Any>)))

    @Suppress("UNCHECKED_CAST")
    override fun format(id: String, vararg arguments: Any?): CombineText =
        TextImpl(Component.translatable(id, *(arguments as Array<out Any>)))

    override fun toNative(text: CombineText): Any = (text as TextImpl).inner
}
