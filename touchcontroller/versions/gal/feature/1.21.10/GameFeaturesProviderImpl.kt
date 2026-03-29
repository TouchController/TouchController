/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.feature.v1_21_10

import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.feature.GameFeatures
import top.fifthlight.touchcontroller.common.gal.feature.GameFeaturesProvider

@ActualImpl(GameFeaturesProvider::class)
object GameFeaturesProviderImpl: GameFeaturesProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): GameFeaturesProvider = this

    override val gameFeatures = GameFeatures(
        dualWield = true,
    )
}
