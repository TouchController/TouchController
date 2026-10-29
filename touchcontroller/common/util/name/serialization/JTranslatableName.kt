/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.util.name.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.*
import com.ubertob.kondor.json.parser.KondorSeparator
import com.ubertob.kondor.json.parser.Separator
import com.ubertob.kondor.json.parser.TokensPath
import com.ubertob.kondor.json.parser.TokensStream
import com.ubertob.kondor.outcome.asSuccess
import top.fifthlight.touchcontroller.common.serialization.JIdentifier
import top.fifthlight.touchcontroller.common.serialization.sealedByName
import top.fifthlight.touchcontroller.common.util.name.TranslatableName

object JTranslatable : JObj<TranslatableName.Translatable>() {
    val identifier by str(JIdentifier, TranslatableName.Translatable::identifier)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = TranslatableName.Translatable(+identifier)
}

object JTranslatableString : JObj<TranslatableName.TranslatableString>() {
    val identifier by str(TranslatableName.TranslatableString::identifier)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = TranslatableName.TranslatableString(+identifier)
}

object JLiteral : JObj<TranslatableName.Literal>() {
    val string by str(TranslatableName.Literal::string)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = TranslatableName.Literal(+string)
}

val JTranslatableNameSealed = sealedByName {
    "translatable" encodes subtype<TranslatableName.Translatable>(JTranslatable)
    "translatableString" encodes subtype<TranslatableName.TranslatableString>(JTranslatableString)
    "literal" encodes subtype<TranslatableName.Literal>(JLiteral)
}

object JTranslatableName : JsonConverter<TranslatableName, JsonNode> {
    @Suppress("UNCHECKED_CAST")
    override val _nodeType: NodeKind<JsonNode> = ObjectNode as NodeKind<JsonNode>

    override fun fromJsonNode(node: JsonNode, path: NodePath): JsonOutcome<TranslatableName> =
        when (node) {
            is JsonNodeString -> TranslatableName.Literal(node.text).asSuccess()
            else -> JTranslatableNameSealed.fromJsonNode(node as JsonNodeObject, path)
        }

    override fun fromTokens(tokens: TokensStream, path: NodePath): JsonOutcome<TranslatableName> {
        val first = if (tokens.hasNext()) tokens.peek() else null
        return if (first is Separator && first.sep != KondorSeparator.OpeningCurly) {
            StringNode.parse(TokensPath(tokens, path)).transform { TranslatableName.Literal(it.text) }
        } else {
            JTranslatableNameSealed.fromTokens(tokens, path)
        }
    }

    override fun toJsonNode(value: TranslatableName) = JTranslatableNameSealed.toJsonNode(value)

    override fun appendValue(app: CharWriter, style: JsonStyle, offset: Int, value: TranslatableName) =
        JTranslatableNameSealed.appendValue(app, style, offset, value)

    override fun schema() = JTranslatableNameSealed.schema()
}
