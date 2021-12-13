package net.pengtul.pengcord.bot.listeners.emoji

import net.pengtul.pengcord.main.Main
import org.javacord.api.event.server.emoji.KnownCustomEmojiDeleteEvent
import org.javacord.api.listener.server.emoji.KnownCustomEmojiDeleteListener

class DscEmojiRemovedListener: KnownCustomEmojiDeleteListener {
    override fun onKnownCustomEmojiDelete(event: KnownCustomEmojiDeleteEvent?) {
        Main.discordBot.updateDiscordEmojis()
    }
}