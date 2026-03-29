/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.gameconfig.v1_21_10

import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance
import net.minecraft.client.Options
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.gameconfig.GameConfigEditor
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KProperty

@ActualImpl(GameConfigEditor::class)
object GameConfigEditorImpl : GameConfigEditor {
    @JvmStatic
    @ActualConstructor
    fun of(): GameConfigEditor = this

    private val pendingCallbackLock = ReentrantLock()
    private var pendingCallbacks: MutableList<GameConfigEditor.Callback>? = mutableListOf()

    operator fun <T : Any> OptionInstance<T>.getValue(thisRef: Any?, property: KProperty<*>): T = this.get()
    operator fun <T : Any> OptionInstance<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = this.set(value)

    private class EditorImpl(val options: Options) : GameConfigEditor.Editor {
        override var autoJump: Boolean by options.autoJump()
    }

    fun executePendingCallback() {
        pendingCallbackLock.withLock {
            val callbacks = pendingCallbacks ?: return
            pendingCallbacks = null
            with(EditorImpl(Minecraft.getInstance().options)) {
                if (callbacks.isNotEmpty()) {
                    callbacks.forEach { callback ->
                        callback.invoke(this)
                    }
                    options.save()
                }
            }
        }
    }

    override fun submit(callback: GameConfigEditor.Callback) {
        pendingCallbackLock.withLock {
            pendingCallbacks?.add(callback) ?: run {
                with(EditorImpl(Minecraft.getInstance().options)) {
                    callback.invoke(this)
                    options.save()
                }
            }
        }
    }
}
