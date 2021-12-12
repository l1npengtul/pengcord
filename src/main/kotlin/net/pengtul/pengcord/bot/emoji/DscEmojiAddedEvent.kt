package kotlin.net.pengtul.pengcord.bot.emoji

import net.pengtul.pengcord.main.Main
import org.javacord.api.event.server.emoji.KnownCustomEmojiCreateEvent
import org.javacord.api.listener.server.emoji.KnownCustomEmojiCreateListener


class DscEmojiAddedEvent: KnownCustomEmojiCreateListener {
    override fun onKnownCustomEmojiCreate(event: KnownCustomEmojiCreateEvent?) {
        Main.discordBot.updateDiscordEmojis()
    }
}