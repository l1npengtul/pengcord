package net.pengtul.pengcord.bot.emoji

import net.pengtul.pengcord.main.Main
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeNameEvent
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeNameListener

class DscEmojiEditedEvent: KnownCustomEmojiChangeNameListener {
    override fun onKnownCustomEmojiChangeName(event: KnownCustomEmojiChangeNameEvent?) {
        Main.discordBot.updateDiscordEmojis()
    }
}