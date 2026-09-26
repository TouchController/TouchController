/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.about.resources

import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.about.AboutInfo
import top.fifthlight.touchcontroller.common.about.AboutInfoProvider
import top.fifthlight.touchcontroller.common.about.serialization.JLibs
import java.io.InputStream

@ActualImpl(AboutInfoProvider::class)
object ResourcesAboutInfoProvider : AboutInfoProvider {
    @ActualConstructor
    @JvmStatic
    fun of() = ResourcesAboutInfoProvider

    private fun getResourceAsStream(name: String): InputStream? = this.javaClass.classLoader.getResourceAsStream(name)
    private fun readResource(name: String): String? = getResourceAsStream(name)?.reader()?.use { it.readText() }

    override val aboutInfo: AboutInfo by lazy {
        val modLicense = readResource("LICENSE_${BuildInfo.MOD_NAME}")
        val librariesJson = readResource("aboutlibraries.json")
        val libraries = librariesJson?.let { librariesJson ->
            JLibs.fromJson(librariesJson).orThrow()
        }
        AboutInfo(
            modLicense = modLicense,
            libraries = libraries,
        )
    }
}
