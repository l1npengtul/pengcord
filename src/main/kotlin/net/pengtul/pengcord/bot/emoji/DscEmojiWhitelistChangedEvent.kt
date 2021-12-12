package net.pengtul.pengcord.bot.emoji

import net.pengtul.pengcord.main.Main
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEvent
import org.javacord.api.listener.server.emoji.KnownCustomEmojiChangeWhitelistedRolesListener

class DscEmojiWhitelistChangedEvent: KnownCustomEmojiChangeWhitelistedRolesListener{
    override fun onKnownCustomEmojiChangeWhitelistedRoles(event: KnownCustomEmojiChangeWhitelistedRolesEvent?) {
        Main.discordBot.updateDiscordEmojis()
    }
}