package top.fifthlight.armorstand.state

import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import top.fifthlight.armorstand.util.ModelLoaders
import top.fifthlight.blazerod.model.formats.ModelFileLoaders
import top.fifthlight.blazerod.model.loader.LoadContext
import top.fifthlight.blazerod.render.api.animation.AnimationItem
import top.fifthlight.blazerod.render.api.animation.AnimationItemFactory
import top.fifthlight.blazerod.render.api.animation.AnimationItemInstance
import top.fifthlight.blazerod.render.api.animation.AnimationItemInstanceFactory
import top.fifthlight.blazerod.render.api.resource.RenderScene
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

data class AnimationSet(
    val idle: AnimationItem? = null,
    val walk: AnimationItem? = null,
    val sprint: AnimationItem? = null,
    val sneak: AnimationItem? = null,
    val sneakIdle: AnimationItem? = null,
    val swingRight: AnimationItem? = null,
    val swingLeft: AnimationItem? = null,
    val elytraFly: AnimationItem? = null,
    val swim: AnimationItem? = null,
    val onClimbable: AnimationItem? = null,
    val onClimbableUp: AnimationItem? = null,
    val onClimbableDown: AnimationItem? = null,
    val sleep: AnimationItem? = null,
    val ride: AnimationItem? = null,
    val die: AnimationItem? = null,
    val onHorse: AnimationItem? = null,
    val onPig: AnimationItem? = null,
    val onBoat: AnimationItem? = null,
    val crawl: AnimationItem? = null,
    val crawlIdle: AnimationItem? = null,
    val lieDown: AnimationItem? = null,
    val custom: Map<String, AnimationItem> = emptyMap(),
    val itemActive: Map<ItemActiveKey, AnimationItem> = emptyMap(),
) {
    data class ItemActiveKey(
        val itemName: ResourceLocation,
        val hand: HandSide,
        val actionType: ActionType,
    ) {
        enum class HandSide { LEFT, RIGHT }
        enum class ActionType { USING, SWINGING }
    }

    operator fun plus(other: AnimationSet): AnimationSet {
        if (other == EMPTY) {
            return this
        } else if (this == EMPTY) {
            return other
        }
        return AnimationSet(
            idle = other.idle ?: this.idle,
            walk = other.walk ?: this.walk,
            sprint = other.sprint ?: this.sprint,
            sneak = other.sneak ?: this.sneak,
            swingRight = other.swingRight ?: this.swingRight,
            swingLeft = other.swingLeft ?: this.swingLeft,
            elytraFly = other.elytraFly ?: this.elytraFly,
            swim = other.swim ?: this.swim,
            onClimbable = other.onClimbable ?: this.onClimbable,
            onClimbableUp = other.onClimbableUp ?: this.onClimbableUp,
            onClimbableDown = other.onClimbableDown ?: this.onClimbableDown,
            sleep = other.sleep ?: this.sleep,
            ride = other.ride ?: this.ride,
            die = other.die ?: this.die,
            onHorse = other.onHorse ?: this.onHorse,
            crawl = other.crawl ?: this.crawl,
            lieDown = other.lieDown ?: this.lieDown,
            custom = this.custom + other.custom,
            itemActive = this.itemActive + other.itemActive,
        )
    }

    companion object {
        val EMPTY = AnimationSet()
    }
}

data class FullAnimationSet(
    val idle: AnimationItemInstance,
    val walk: AnimationItemInstance,
    val sprint: AnimationItemInstance,
    val sneak: AnimationItemInstance,
    val sneakIdle: AnimationItemInstance,
    val swingRight: AnimationItemInstance,
    val swingLeft: AnimationItemInstance,
    val elytraFly: AnimationItemInstance,
    val swim: AnimationItemInstance,
    val onClimbable: AnimationItemInstance,
    val onClimbableUp: AnimationItemInstance,
    val onClimbableDown: AnimationItemInstance,
    val sleep: AnimationItemInstance,
    val ride: AnimationItemInstance,
    val die: AnimationItemInstance,
    val onHorse: AnimationItemInstance,
    val onPig: AnimationItemInstance,
    val onBoat: AnimationItemInstance,
    val crawl: AnimationItemInstance,
    val crawlIdle: AnimationItemInstance,
    val custom: Map<String, AnimationItemInstance> = emptyMap(),
    val itemActive: Map<AnimationSet.ItemActiveKey, AnimationItemInstance> = emptyMap(),
) {
    companion object {
        fun from(animationSet: AnimationSet): FullAnimationSet? {
            return animationSet.idle?.let { idle ->
                // @formatter:off
                FullAnimationSet(
                    idle = AnimationItemInstanceFactory.of(idle),
                    walk = AnimationItemInstanceFactory.of(animationSet.walk ?: idle),
                    sprint = AnimationItemInstanceFactory.of(animationSet.sprint ?: animationSet.walk ?: idle),
                    sneak = AnimationItemInstanceFactory.of(animationSet.sneak ?: animationSet.walk ?: idle),
                    sneakIdle = AnimationItemInstanceFactory.of(animationSet.sneakIdle ?: idle),
                    swingRight = AnimationItemInstanceFactory.of(animationSet.swingRight ?: animationSet.swingLeft ?: idle),
                    swingLeft = AnimationItemInstanceFactory.of(animationSet.swingLeft ?: animationSet.swingRight ?: idle),
                    elytraFly = AnimationItemInstanceFactory.of(animationSet.elytraFly ?: animationSet.swim ?: animationSet.walk ?: idle),
                    swim = AnimationItemInstanceFactory.of(animationSet.swim ?: animationSet.walk ?: idle),
                    onClimbable = AnimationItemInstanceFactory.of(animationSet.onClimbable ?: animationSet.onClimbableUp ?: animationSet.onClimbableDown ?: idle),
                    onClimbableUp = AnimationItemInstanceFactory.of(animationSet.onClimbableUp ?: animationSet.onClimbableDown ?: animationSet.onClimbable ?: idle),
                    onClimbableDown = AnimationItemInstanceFactory.of(animationSet.onClimbableDown ?: animationSet.onClimbableUp ?: animationSet.onClimbable ?: idle),
                    sleep = AnimationItemInstanceFactory.of(animationSet.sleep ?: animationSet.lieDown ?: idle),
                    ride = AnimationItemInstanceFactory.of(animationSet.ride ?: animationSet.onHorse ?: idle),
                    die = AnimationItemInstanceFactory.of(animationSet.die ?: animationSet.lieDown ?: animationSet.sleep ?: idle),
                    onHorse = AnimationItemInstanceFactory.of(animationSet.onHorse ?: animationSet.ride ?: idle),
                    onPig = AnimationItemInstanceFactory.of(animationSet.onPig ?: animationSet.ride ?: idle),
                    onBoat = AnimationItemInstanceFactory.of(animationSet.onBoat ?: animationSet.ride ?: idle),
                    crawl = AnimationItemInstanceFactory.of(animationSet.crawl ?: animationSet.sneak ?: idle),
                    crawlIdle = AnimationItemInstanceFactory.of(animationSet.crawlIdle ?: animationSet.crawl ?: animationSet.sleep ?: animationSet.sneak ?: idle),
                    custom = animationSet.custom.mapValues { (_, value) -> AnimationItemInstanceFactory.of(value) },
                    itemActive = animationSet.itemActive.mapValues { (_, value) -> AnimationItemInstanceFactory.of(value) },
                )
                // @formatter:on
            }
        }
    }
}

data object AnimationSetLoader {
    private val logger = LoggerFactory.getLogger(AnimationSetLoader::class.java)

    fun load(
        scene: RenderScene,
        animations: List<AnimationItem>?,
        directory: Path,
    ): AnimationSet {
        var idle: AnimationItem? = null
        var walk: AnimationItem? = null
        var sprint: AnimationItem? = null
        var sneak: AnimationItem? = null
        var sneakIdle: AnimationItem? = null
        var swingRight: AnimationItem? = null
        var swingLeft: AnimationItem? = null
        var elytraFly: AnimationItem? = null
        var swim: AnimationItem? = null
        var onClimbable: AnimationItem? = null
        var onClimbableUp: AnimationItem? = null
        var onClimbableDown: AnimationItem? = null
        var sleep: AnimationItem? = null
        var ride: AnimationItem? = null
        var die: AnimationItem? = null
        var onHorse: AnimationItem? = null
        var onPig: AnimationItem? = null
        var onBoat: AnimationItem? = null
        var crawl: AnimationItem? = null
        var crawlIdle: AnimationItem? = null
        val custom: MutableMap<String, AnimationItem> = mutableMapOf()
        val itemActive: MutableMap<AnimationSet.ItemActiveKey, AnimationItem> = mutableMapOf()

        val files = try {
            if (directory.isDirectory()) {
                directory.listDirectoryEntries()
            } else {
                null
            }
        } catch (ex: Exception) {
            logger.warn("Failed to list animation directory: $directory", ex)
            null
        }

        // Try external file
        if (files != null) {
            for (file in files) {
                val name = file.nameWithoutExtension
                val extension = file.extension
                if (extension !in ModelLoaders.animationExtensions) {
                    continue
                }

                fun load() = try {
                    val result = ModelFileLoaders.probeAndLoad(file, LoadContext.File(directory))
                    val animation = result?.animations?.firstOrNull() ?: return null
                    AnimationItemFactory.load(scene, animation)
                } catch (ex: Exception) {
                    logger.warn("Failed to load animation file: $file", ex)
                    null
                }

                when (name.lowercase()) {
                    "idle" -> load()?.let { idle = it }
                    "walk" -> load()?.let { walk = it }
                    "sprint" -> load()?.let { sprint = it }
                    "sneak" -> load()?.let { sneak = it }
                    "sneakidle" -> load()?.let { sneakIdle = it }
                    "swingright" -> load()?.let { swingRight = it }
                    "swingleft" -> load()?.let { swingLeft = it }
                    "elytraFly" -> load()?.let { elytraFly = it }
                    "swim" -> load()?.let { swim = it }
                    "onclimbable" -> load()?.let { onClimbable = it }
                    "onclimbableup" -> load()?.let { onClimbableUp = it }
                    "onclimbabledown" -> load()?.let { onClimbableDown = it }
                    "sleep" -> load()?.let { sleep = it }
                    "ride" -> load()?.let { ride = it }
                    "die" -> load()?.let { die = it }
                    "onhorse" -> load()?.let { onHorse = it }
                    "onpig" -> load()?.let { onPig = it }
                    "onboat" -> load()?.let { onBoat = it }
                    "crawl" -> load()?.let { crawl = it }
                    "crawlidle", "liedown" -> load()?.let { crawlIdle = it }
                    else -> when {
                        name.startsWith("custom") -> {
                            val name = name.substringAfter("custom")
                            load()?.let { custom[name] = it }
                        }

                        name.startsWith("itemActive") -> {
                            val name = name.substringAfter("itemActive")
                            val parts = name.split("_")
                            if (parts.size != 3) {
                                continue
                            }
                            val (itemName, hand, action) = parts
                            val item = try {
                                ResourceLocation.fromNamespaceAndPath("minecraft", itemName.lowercase())
                            } catch (ex: Exception) {
                                logger.warn("Bad item name: $itemName", ex)
                                continue
                            }
                            val handSide = when (hand.lowercase()) {
                                "left" -> AnimationSet.ItemActiveKey.HandSide.LEFT
                                "right" -> AnimationSet.ItemActiveKey.HandSide.RIGHT
                                else -> continue
                            }
                            val actionType = when (action.lowercase()) {
                                "using" -> AnimationSet.ItemActiveKey.ActionType.USING
                                "swinging" -> AnimationSet.ItemActiveKey.ActionType.SWINGING
                                else -> continue
                            }
                            val animation = load() ?: continue
                            itemActive[AnimationSet.ItemActiveKey(item, handSide, actionType)] = animation
                        }
                    }
                }
            }
        }

        // Try embed
        if (animations != null) {
            for (animation in animations) {
                when (animation.name?.lowercase()) {
                    "idle" -> idle = animation
                    "walk" -> walk = animation
                    "run", "sprint" -> sprint = animation
                    "sneak" -> sneak = animation
                    "sneaking" -> sneakIdle = animation
                    "use_mainhand", "swingright" -> swingRight = animation
                    "use_offhand", "swingleft" -> swingLeft = animation
                    "elytra_fly", "elytrafly" -> elytraFly = animation
                    "swim" -> swim = animation
                    "ladder_stillness", "onclimbable" -> onClimbable = animation
                    "ladder_up", "onclimbableup" -> onClimbableUp = animation
                    "ladder_down", "onclimbabledown" -> onClimbableDown = animation
                    "sleep" -> sleep = animation
                    "ride" -> ride = animation
                    "ride_pig", "onpig" -> onPig = animation
                    "boat", "onboat" -> onBoat = animation
                    "climb", "crawl" -> crawl = animation
                    "climbing", "crawlidle", "lieDown" -> crawlIdle = animation
                    "death", "die", "dead" -> die = animation
                }
            }
        }

        if (idle == null) {
            return AnimationSet.EMPTY
        }

        return AnimationSet(
            idle = idle,
            walk = walk,
            sprint = sprint,
            sneak = sneak,
            sneakIdle = sneakIdle,
            swingRight = swingRight,
            swingLeft = swingLeft,
            elytraFly = elytraFly,
            swim = swim,
            onClimbable = onClimbable,
            onClimbableUp = onClimbableUp,
            onClimbableDown = onClimbableDown,
            sleep = sleep,
            ride = ride,
            die = die,
            onHorse = onHorse,
            onPig = onPig,
            onBoat = onBoat,
            crawl = crawl,
            crawlIdle = crawlIdle,
        )
    }
}
