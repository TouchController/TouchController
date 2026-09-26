/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.about.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.about.Developer
import top.fifthlight.touchcontroller.common.about.Library
import top.fifthlight.touchcontroller.common.about.Libs
import top.fifthlight.touchcontroller.common.about.License

object JDeveloper : JObj<Developer>() {
    val name by JFieldMaybe(Developer::name, JString)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Developer(
        name = +name,
    )
}

object JLicense : JObj<License>() {
    val content by JFieldMaybe(License::content, JString)
    val name by str(License::name)
    val url by JFieldMaybe(License::url, JString)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = License(
        content = +content,
        name = +name,
        url = +url,
    )
}

object JLibrary : JObj<Library>() {
    val uniqueId by str(Library::uniqueId)
    val name by str(Library::name)
    val artifactVersion by JFieldMaybe(Library::artifactVersion, JString)
    val description by JFieldMaybe(Library::description, JString)
    val developers by JFieldMaybe(Library::developers, JList(JDeveloper))
    val licenses by JFieldMaybe(Library::licenses, JList(JString))
    val website by JFieldMaybe(Library::website, JString)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Library(
        uniqueId = +uniqueId,
        name = +name,
        artifactVersion = +artifactVersion,
        description = +description,
        developers = (+developers).orEmpty(),
        licenses = (+licenses).orEmpty(),
        website = +website,
    )
}

object JLibs : JObj<Libs>() {
    val libraries by JFieldMaybe(Libs::libraries, JList(JLibrary))
    val licenses by JFieldMaybe(Libs::licenses, JMap(JLicense))

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Libs(
        libraries = (+libraries).orEmpty(),
        licenses = (+licenses).orEmpty(),
    )
}
