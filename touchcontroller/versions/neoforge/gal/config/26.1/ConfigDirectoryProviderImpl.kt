/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.neoforge.v26_1.gal.config

import net.neoforged.fml.loading.FMLPaths
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProvider
import java.nio.file.Path

@ActualImpl(ConfigDirectoryProvider::class)
object ConfigDirectoryProviderImpl : ConfigDirectoryProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ConfigDirectoryProvider = this

    override val configDirectory: Path by lazy {
        FMLPaths.CONFIGDIR.get().resolve(BuildInfo.MOD_ID)
    }
}
