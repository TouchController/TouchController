package top.fifthlight.touchcontroller.common.gal.key

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.mergetools.api.ExpectFactory
import java.awt.event.KeyEvent
import kotlin.uuid.Uuid

@Serializable
enum class DefaultKeyBindingType {
    @SerialName("attack")
    ATTACK,

    @SerialName("use")
    USE,

    @SerialName("inventory")
    INVENTORY,

    @SerialName("swap_hands")
    SWAP_HANDS,

    @SerialName("sneak")
    SNEAK,

    @SerialName("sprint")
    SPRINT,

    @SerialName("jump")
    JUMP,

    @SerialName("player_list")
    PLAYER_LIST,

    @SerialName("left")
    LEFT,

    @SerialName("right")
    RIGHT,

    @SerialName("up")
    UP,

    @SerialName("down")
    DOWN,
}

interface KeyBindingEventsHandler {
    fun onKeyDown(state: KeyBindingState)

    @ExpectFactory
    interface Factory {
        fun of(): KeyBindingEventsHandler
    }

    companion object: KeyBindingEventsHandler by KeyBindingEventsHandlerFactory.of()
}

abstract class KeyBindingState {
    abstract val id: String
    abstract val name: Text
    abstract val categoryId: String
    abstract val categoryName: Text

    // Click for once. You probably don't want to use this as it only increases press count, without actually pressing
    // the button. If it causes problems, use clicked = true instead.
    open fun click() {
        KeyBindingEventsHandler.onKeyDown(this)
    }

    abstract fun haveClickCount(): Boolean

    private var passedClientTick = false
    private var wasClicked: Boolean = false

    fun renderTick(renderTick: Int) {
        lockedUuids.values.removeIf { it < renderTick - 1 }
        if (passedClientTick) {
            wasClicked = clicked || locked
            clicked = false
            passedClientTick = false
        }
    }

    fun clientTick(clientTick: Int) {
        passedClientTick = true
    }

    // Click for one tick (client tick). It will be reset every tick.
    var clicked: Boolean = false
        set(value) {
            if (!locked && !wasClicked && !field && value) {
                click()
            }
            field = value
        }

    protected val lockedUuids = mutableMapOf<Uuid, Int>()

    val locked: Boolean
        get() = lockedUuids.isNotEmpty()

    fun addLock(uuid: Uuid, renderTick: Int) {
        if (!clicked && !locked && !wasClicked) {
            click()
        }
        lockedUuids[uuid] = renderTick
    }

    fun clearLock() {
        lockedUuids.clear()
    }

    fun clearLock(uuid: Uuid) {
        lockedUuids.remove(uuid)
    }

    fun getLock(uuid: Uuid) = lockedUuids[uuid] != null

    fun refreshLock(uuid: Uuid, renderTick: Int) {
        val lastTick = lockedUuids[uuid]
        if (lastTick != null) {
            lockedUuids[uuid] = renderTick
        }
    }

    companion object Empty : KeyBindingState() {
        private val textFactory = TextFactoryFactory.of()
        override val id: String = "empty"
        override val name: Text
            get() = textFactory.empty()
        override val categoryId: String = "empty"
        override val categoryName: Text
            get() = textFactory.empty()

        override fun haveClickCount() = false
    }
}

interface KeyBindingHandler {
    fun renderTick(renderTick: Int) {
        for (state in getExistingStates()) {
            state.renderTick(renderTick)
        }
    }

    fun clientTick(clientTick: Int) {
        for (state in getExistingStates()) {
            state.clientTick(clientTick)
        }
    }

    fun getState(type: DefaultKeyBindingType): KeyBindingState
    fun getState(id: String): KeyBindingState?
    fun getAllStates(): Map<String, KeyBindingState>
    fun getExistingStates(): Collection<KeyBindingState>

    fun mapDefaultType(type: DefaultKeyBindingType) = getState(type).id

    companion object Empty : KeyBindingHandler {
        override fun getState(type: DefaultKeyBindingType) = KeyBindingState.Empty
        override fun getState(id: String): KeyBindingState? = null
        override fun getAllStates(): Map<String, KeyBindingState> = mapOf()
        override fun getExistingStates(): Collection<KeyBindingState> = listOf()
    }

    @ExpectFactory
    interface Factory {
        fun of(): KeyBindingHandler
    }
}
