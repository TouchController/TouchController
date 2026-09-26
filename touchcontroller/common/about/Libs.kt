/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.about

data class Developer(
    val name: String? = null,
)

data class Library(
    val uniqueId: String,
    val name: String,
    val artifactVersion: String? = null,
    val description: String? = null,
    val developers: List<Developer> = listOf(),
    val licenses: List<String> = listOf(),
    val website: String? = null,
)

data class License(
    val content: String? = null,
    val name: String,
    val url: String? = null,
)

data class Libs(
    val libraries: List<Library> = listOf(),
    val licenses: Map<String, License> = mapOf(),
)
