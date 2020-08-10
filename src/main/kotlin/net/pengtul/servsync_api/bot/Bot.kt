package net.pengtul.servsync_api.bot

import net.pengtul.servsync_api.Main
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

class Bot {
    private var discordApi: DiscordApi;
    init {
        discordApi = DiscordApiBuilder().setToken(Main.ServerConfig.DiscordBot).login().join();
        discordApi.addListener(MsgCreateEvent());
        discordApi.setMessageCacheSize(10,60*30); // Store a maximum of 10 messages/channel for a maximum of 30 minutes.
    }
}