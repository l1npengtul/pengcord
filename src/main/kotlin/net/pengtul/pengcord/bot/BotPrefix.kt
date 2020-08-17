package net.pengtul.pengcord.bot

import net.kautler.command.api.prefix.PrefixProvider
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message

class BotPrefix: PrefixProvider<Message> {
    public override fun getCommandPrefix(msg: Message?): String {
        val cPrefix =  Main.ServerConfig.BotPrefix.toString();
        if (cPrefix == ""){
            Main.ServerLogger.warning("Discord Prefix not set or null! Will parse every message! Will cause lag on server!");
        }
        return cPrefix
    }
}