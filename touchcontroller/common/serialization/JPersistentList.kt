package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JArray
import com.ubertob.kondor.json.JConverter
import com.ubertob.kondor.json.jsonnode.ArrayNode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

fun <T : Any> JPersistentList(converter: JConverter<T>) = object : JArray<T, PersistentList<T>> {
    override val converter = converter
    override val _nodeType = ArrayNode

    override fun convertToCollection(iterable: Iterable<T?>) = iterable.filterNotNull().toPersistentList()

    override fun convertFromCollection(collection: PersistentList<T>): Iterable<T?> = collection
}
