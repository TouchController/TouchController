package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JArray
import com.ubertob.kondor.json.JConverter
import com.ubertob.kondor.json.JsonError
import com.ubertob.kondor.json.JsonOutcome
import com.ubertob.kondor.json.jsonnode.*
import com.ubertob.kondor.outcome.Outcome
import com.ubertob.kondor.outcome.asSuccess
import com.ubertob.kondor.outcome.traverseIndexed
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

fun <T : Any> JPersistentList(
    converter: JConverter<T>,
    ignoreFailedItem: Boolean = false,
) = object : JArray<T, PersistentList<T>> {
    override val converter = converter
    override val _nodeType = ArrayNode

    private fun <T : Any> mapFromArray(
        node: JsonNodeArray,
        f: (Int, JsonNode) -> JsonOutcome<T?>,
    ): JsonOutcome<Iterable<T?>> = if (ignoreFailedItem) {
        node.elements.mapIndexedNotNull { i, e -> f(i, e).orNull() }.asSuccess()
    } else {
        node.elements.traverseIndexed(f)
    }

    override fun fromJsonNode(node: JsonNodeArray, path: NodePath): Outcome<JsonError, PersistentList<T>> =
        mapFromArray(node) { i, e -> converter.fromJsonNodeBase(e, NodePathSegment("[$i]", path)) }
            .transform { convertToCollection(it) }

    override fun convertToCollection(iterable: Iterable<T?>) = iterable.filterNotNull().toPersistentList()

    override fun convertFromCollection(collection: PersistentList<T>): Iterable<T?> = collection
}
