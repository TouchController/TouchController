/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.entitypicker

import androidx.compose.runtime.*
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text.Companion.translatable
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.minHeight
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.widget.EditText
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.entity.EntityItemProvider
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.entity.EntityTypeProvider
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState

@Composable
fun EntityPicker(
    modifier: Modifier = Modifier,
    onEntityChosen: (EntityType) -> Unit,
) {
    val inGame = GameState.inGame
    var searchText by remember { mutableStateOf("") }
    val allEntityTypes = remember(inGame) {
        EntityTypeProvider.allTypes.map { type ->
            val icon = if (inGame) {
                EntityItemProvider.getEntityIconItem(type)
            } else {
                null
            }
            Pair(icon, type)
        }
    }
    val showingEntities by remember(allEntityTypes) {
        derivedStateOf {
            if (searchText.isEmpty()) {
                allEntityTypes
            } else {
                allEntityTypes.filter { (_, type) ->
                    type.name.string.contains(searchText, ignoreCase = true)
                }.toPersistentList()
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(8)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        EditText(
            modifier = Modifier.fillMaxWidth(),
            placeholder = translatable(Texts.SCREEN_ENTITY_TYPE_LIST_SEARCH_PLACEHOLDER),
            value = searchText,
            onValueChanged = { searchText = it },
        )

        Column(
            modifier = Modifier
                .verticalScroll()
                .weight(1f),
        ) {
            for ((icon, entityType) in showingEntities) {
                ListButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEntityChosen(entityType) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minHeight(16),
                        horizontalArrangement = Arrangement.spacedBy(4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (icon != null) {
                            Item(item = icon)
                        }
                        Text(
                            modifier = Modifier
                                .weight(1f),
                            text = entityType.name,
                        )
                    }
                }
            }
        }
    }
}
