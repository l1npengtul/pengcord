package net.pengtul.pengcord.bot.emoji

import net.pengtul.pengcord.main.Main
import org.javacord.api.event.server.emoji.KnownCustomEmojiDeleteEvent
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener

class DscEmojiRemovedEvent: KnownCustomEmojiDeleteListener {
    override fun onKnownCustomEmojiDelete(event: KnownCustomEmojiDeleteEvent?) {
        Main.discordBot.updateDiscordEmojis()
    }
}