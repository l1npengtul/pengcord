package net.pengtul.pengcord.main

import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.Server;
import org.bukkit.event.player.*
import org.bukkit.plugin.Plugin

public class Event : Listener{
    // Player Leave/Join
    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.joinMessage}");
        Main.discordBot.sendMessageToDiscord("[Server]: ${event.joinMessage?.replace("§e","")}");
        Main.downloadSkin(event.player,null);
    }
    @EventHandler
    public fun onPlayerLeave(event: PlayerQuitEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.quitMessage}");
        Main.discordBot.sendMessageToDiscord("[Server]: ${event.quitMessage?.replace("§e","")}");
    }
    @EventHandler
    public fun onPlayerKickEvent(event: PlayerKickEvent){
        Main.discordBot.sendMessageToDiscord("[Server]: ${event.leaveMessage.replace("§e","")}. Reason: ${event.reason.replace("§e","")}")
    }

    // Chat Events
    @EventHandler
    public fun onPlayerChatEvent(event: AsyncPlayerChatEvent){
        Main.discordBot.sendMessagetoWebhook(event.message,event.player.displayName, null, event.player);
    }
    @EventHandler
    public fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (!event.message.startsWith("§7[DSC]")){
            Main.discordBot.sendMessageToDiscord("Server: ${event.message}");
        }
    }

    // Death Messages
    @EventHandler
    public fun onPlayerDeathEvent(event: PlayerDeathEvent){
        Main.discordBot.sendMessageToDiscord("Server: ${event.deathMessage}");
    }
}