package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.CharWriter
import com.ubertob.kondor.json.JsonConverter
import com.ubertob.kondor.json.JsonStyle
import com.ubertob.kondor.json.jsonnode.JsonNode
import com.ubertob.kondor.json.jsonnode.NodeKind
import com.ubertob.kondor.json.jsonnode.NodePath
import com.ubertob.kondor.json.parser.TokensStream

fun <T, R, N : JsonNode> JValueClass(
    getter: (T) -> R,
    factory: (R) -> T,
    converter: JsonConverter<R, N>,
) = object : JsonConverter<T, N> {
    override val _nodeType: NodeKind<N>
        get() = converter._nodeType

    override fun fromJsonNode(node: N, path: NodePath) =
        converter.fromJsonNode(node, path).transform(factory)

    override fun fromTokens(tokens: TokensStream, path: NodePath) =
        converter.fromTokens(tokens, path).transform(factory)

    override fun toJsonNode(value: T) =
        converter.toJsonNode(getter(value))

    override fun appendValue(app: CharWriter, style: JsonStyle, offset: Int, value: T) =
        converter.appendValue(app, style, offset, getter(value))
}
