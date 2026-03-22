package top.fifthlight.armorstand.ui.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.model.ViewModel
import top.fifthlight.armorstand.ui.screen.ArmorStandScreen

fun <T : ArmorStandScreen<T, M>, M : ViewModel> ArmorStandScreen<T, M>.textField(
    placeHolder: Component? = null,
    width: Int = this.width,
    height: Int = 20,
    text: Flow<String>,
    maxChars: Int? = null,
    onChanged: (String) -> Unit,
) = EditBox(font, width, height, Component.empty()).apply {
    placeHolder?.let {
        setHint(placeHolder)
    }
    setMaxLength(maxChars ?: Int.MAX_VALUE)
    setResponder {
        onChanged(it)
    }
    scope.launch {
        text.collect { newText ->
            if (newText != this@apply.value) {
                this@apply.value = newText
            }
        }
    }
}
