@file:Suppress("UnusedReceiverParameter")

package top.fifthlight.blazerod.model.bedrock.molang.binding

import org.joml.Vector3d
import team.unnamed.mocha.runtime.value.*
import team.unnamed.mocha.runtime.value.Function
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.util.*

internal inline fun <E, T : Value> ObjectValue.property(crossinline getter: (E) -> T) =
    EntityObjectProperty<E> { getter(it) }

internal fun ObjectValue.vector3dProperty(property: AnimationContext.Property<Vector3d>) = object : ObjectProperty {
    override fun value(): Value = Function<AnimationContext> { ctx, args ->
        val context = ctx.entity()
        val index = args.next().eval()?.asNumber?.toInt() ?: 0
        val value = context.getProperty(property) ?: return@Function NumberValue.zero()
        when (index) {
            0 -> NumberValue.of(value.x)
            1 -> NumberValue.of(value.y)
            2 -> NumberValue.of(value.z)
            else -> NumberValue.zero()
        }
    }

    override fun constant() = false
}

internal val ONE_VALUE = NumberValue.of(1.0)

internal inline fun <E> ObjectValue.booleanProperty(crossinline getter: (E) -> Boolean): ObjectProperty =
    property<E, NumberValue> { entity -> if (getter(entity)) ONE_VALUE else NumberValue.zero() }

internal inline fun <E> ObjectValue.numberProperty(crossinline getter: (E) -> Number?): ObjectProperty =
    property<E, NumberValue> { entity -> getter(entity)?.toDouble()?.let { NumberValue.of(it) } ?: NumberValue.zero() }

internal val EMPTY_STRING = StringValue.of("")

internal inline fun <E> ObjectValue.stringProperty(crossinline getter: (E) -> String?): ObjectProperty =
    property<E, StringValue> { entity -> getter(entity)?.let { StringValue.of(it) } ?: EMPTY_STRING }

internal fun ObjectValue.intProperty(property: AnimationContext.Property<IntWrapper>) =
    numberProperty<AnimationContext> { it.getProperty(property)?.value?.toDouble() }

internal fun ObjectValue.longProperty(property: AnimationContext.Property<LongWrapper>) =
    numberProperty<AnimationContext> { it.getProperty(property)?.value?.toDouble() }

internal fun ObjectValue.doubleProperty(property: AnimationContext.Property<DoubleWrapper>) =
    numberProperty<AnimationContext> { it.getProperty(property)?.value }

internal fun ObjectValue.floatProperty(property: AnimationContext.Property<FloatWrapper>) =
    numberProperty<AnimationContext> { it.getProperty(property)?.value?.toDouble() }

internal fun ObjectValue.booleanProperty(property: AnimationContext.Property<BooleanWrapper>) =
    booleanProperty<AnimationContext> { it.getProperty(property)?.value ?: false }

internal fun ObjectValue.stringProperty(property: AnimationContext.Property<String>) =
    stringProperty<AnimationContext> { it.getProperty(property) }
