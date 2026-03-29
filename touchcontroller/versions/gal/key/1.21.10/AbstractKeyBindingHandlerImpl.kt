/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.key.v1_21_10

import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.network.chat.Component
import top.fifthlight.combine.backend.minecraft.text.v1_21_10.TextImpl
import top.fifthlight.combine.data.Text
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingState
import top.fifthlight.touchcontroller.extension.v1_21_10.ClickableKeyBinding

private fun KeyMapping.click() {
    val clickableKeyBinding = this as ClickableKeyBinding
    clickableKeyBinding.`touchController$click`()
}

private fun KeyMapping.getClickCount(): Int {
    val clickableKeyBinding = this as ClickableKeyBinding
    return clickableKeyBinding.`touchController$getClickCount`()
}

class KeyBindingStateImpl(
    val keyBinding: KeyMapping,
) : KeyBindingState() {
    override val id: String = keyBinding.name

    override val name: Text = TextImpl(Component.translatable(keyBinding.name))

    override val categoryId: String
        get() = keyBinding.category.toString()

    override val categoryName: Text
        get() = TextImpl(keyBinding.category.label())

    override fun click() {
        super.click()
        keyBinding.click()
    }

    override fun haveClickCount(): Boolean = keyBinding.getClickCount() > 0
}

abstract class AbstractKeyBindingHandlerImpl : KeyBindingHandler {
    protected val client: Minecraft = Minecraft.getInstance()
    protected val options: Options = client.options
    private val state = mutableMapOf<KeyMapping, KeyBindingStateImpl>()

    abstract fun getKeyBinding(name: String): KeyMapping?
    abstract fun getAllKeyBinding(): Map<String, KeyMapping>

    private fun DefaultKeyBindingType.toMinecraft() = when (this) {
        DefaultKeyBindingType.ATTACK -> options.keyAttack
        DefaultKeyBindingType.USE -> options.keyUse
        DefaultKeyBindingType.INVENTORY -> options.keyInventory
        DefaultKeyBindingType.SWAP_HANDS -> options.keySwapOffhand
        DefaultKeyBindingType.SNEAK -> options.keyShift
        DefaultKeyBindingType.SPRINT -> options.keySprint
        DefaultKeyBindingType.JUMP -> options.keyJump
        DefaultKeyBindingType.PLAYER_LIST -> options.keyPlayerList
        DefaultKeyBindingType.LEFT -> options.keyLeft
        DefaultKeyBindingType.RIGHT -> options.keyRight
        DefaultKeyBindingType.UP -> options.keyUp
        DefaultKeyBindingType.DOWN -> options.keyDown
    }

    fun isDown(key: KeyMapping) = state[key]?.let { it.clicked || it.locked } == true

    private fun getState(key: KeyMapping) = state.getOrPut(key) {
        KeyBindingStateImpl(key)
    }

    override fun getState(type: DefaultKeyBindingType): KeyBindingState {
        return getState(type.toMinecraft())
    }

    override fun getState(id: String): KeyBindingState? = getKeyBinding(id)?.let(::getState)

    override fun getAllStates(): Map<String, KeyBindingState> =
        getAllKeyBinding().mapValues { (_, value) -> getState(value) }

    override fun getExistingStates(): Collection<KeyBindingState> = state.values
}
