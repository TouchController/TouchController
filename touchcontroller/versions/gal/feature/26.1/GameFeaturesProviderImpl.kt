package top.fifthlight.touchcontroller.gal.feature.v26_1

import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.feature.EntityFeatures
import top.fifthlight.touchcontroller.common.gal.feature.GameFeatures
import top.fifthlight.touchcontroller.common.gal.feature.GameFeaturesProvider

@ActualImpl(GameFeaturesProvider::class)
object GameFeaturesProviderImpl: GameFeaturesProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): GameFeaturesProvider = this

    override val gameFeatures = GameFeatures(
        dualWield = true,
        entity = EntityFeatures(
            haveCamel = true,
            haveLlama = true,
            haveStrider = true,
        )
    )
}
