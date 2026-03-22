package top.fifthlight.touchcontroller.gal.chat.v26_1

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.client.Minecraft
import net.minecraft.util.StringUtil
import org.apache.commons.lang3.StringUtils
import top.fifthlight.combine.backend.minecraft.text.v26_1.TextImpl
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessage
import top.fifthlight.touchcontroller.common.gal.chat.ChatMessageProvider
import top.fifthlight.touchcontroller.extension.v26_1.ChatComponentWithMessages

@ActualImpl(ChatMessageProvider::class)
object ChatMessageProviderImpl : ChatMessageProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ChatMessageProvider = this

    private val client = Minecraft.getInstance()

    override fun getMessages(): PersistentList<ChatMessage> =
        (client.gui.chat as ChatComponentWithMessages).`touchcontroller$getMessages`()
            .reversed()
            .map { ChatMessage(message = TextImpl(it.content())) }
            .toPersistentList()

    override fun sendMessage(message: String) {
        val message = StringUtil.trimChatMessage(StringUtils.normalizeSpace(message.trim()))
        if (!message.isEmpty()) {
            client.gui.chat.addRecentChat(message);
            if (message.startsWith("/")) {
                client.player?.connection?.sendCommand(message.substring(1))
            } else {
                client.player?.connection?.sendChat(message)
            }
        }
    }
}
