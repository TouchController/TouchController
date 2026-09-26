/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.assets.serialization

import top.fifthlight.touchcontroller.common.assets.TextureItems
import top.fifthlight.touchcontroller.common.assets.TextureSets
import top.fifthlight.touchcontroller.common.util.registry.serialization.registryConverter

val JTextureSet = registryConverter(TextureSets.registry) { TextureSets.fallback }

val JTextureItem = registryConverter(TextureItems.registry) { TextureItems.unknown }
