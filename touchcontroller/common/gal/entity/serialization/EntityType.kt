/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.gal.entity.serialization

import com.ubertob.kondor.json.JStringRepresentable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider

object JEntityType : JStringRepresentable<EntityType>() {
    private val types = EntityTypeProvider.allTypes.associateBy { it.identifier }

    override val cons: (String) -> EntityType = { string ->
        types[Identifier(string)] ?: error("Bad entity type: $string")
    }

    override val render: (EntityType) -> String = { it.identifier.toString() }
}
