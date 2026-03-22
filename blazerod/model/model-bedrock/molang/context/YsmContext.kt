package top.fifthlight.blazerod.model.bedrock.molang.context

import team.unnamed.mocha.runtime.value.ObjectProperty
import team.unnamed.mocha.runtime.value.ObjectValue
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.bedrock.molang.binding.booleanProperty
import top.fifthlight.blazerod.model.bedrock.molang.binding.floatProperty
import top.fifthlight.blazerod.model.bedrock.molang.binding.intProperty
import top.fifthlight.blazerod.model.bedrock.molang.binding.stringProperty

object YsmContext : ObjectValue {
    private val entries = CaseInsensitiveStringHashMap<ObjectProperty>()
    override fun getProperty(name: String) = entries[name]

    init {
        // Player
        entries["head_yaw"] = floatProperty(AnimationContext.Property.PlayerHeadXRotation)
        entries["head_pitch"] = floatProperty(AnimationContext.Property.PlayerHeadYRotation)
        entries["person_view"] = intProperty(AnimationContext.Property.PlayerPersonView)
        entries["is_passenger"] = booleanProperty(AnimationContext.Property.EntityIsRiding)
        entries["is_sleep"] = booleanProperty(AnimationContext.Property.PlayerIsSleeping)
        entries["is_sneak"] = booleanProperty(AnimationContext.Property.PlayerIsSneaking)
        entries["food_level"] = intProperty(AnimationContext.Property.PlayerFoodLevel)

        // World
        entries["dimension_name"] = stringProperty(AnimationContext.Property.WorldDimension)
        entries["weather"] = intProperty(AnimationContext.Property.WorldWeather)

        // Game
        entries["fps"] = intProperty(AnimationContext.Property.GameFps)
    }
}
