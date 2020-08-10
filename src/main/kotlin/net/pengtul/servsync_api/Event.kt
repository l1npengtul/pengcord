package net.pengtul.servsync_api

import org.bukkit.Bukkit.getServer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.Server;

public class Event : Listener{
    // Player Leave/Join
    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent){
        Main.ServerLogger.info("[Server]: " + event.joinMessage);
    }
    @EventHandler
    public fun onPlayerLeave(event: PlayerQuitEvent){
        Main.ServerLogger.info("[Server]: " + event.quitMessage);
    }

    // Chat Events
    @EventHandler
    public fun onChatEvent(event: AsyncPlayerChatEvent){
        // TODO: Check if message sent is not private, i.e. check if getRecipients() is equal to all the players on the server
        if (event.recipients.size == getServer().onlinePlayers.size){
            Main.ServerLogger.info("[" + event.player + "]: " + event.message);
        }
    }
    @EventHandler
    public fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (event.recipients.size == getServer().onlinePlayers.size){
            Main.ServerLogger.info(event.message);
        }
    }

    // Death Messages
    @EventHandler
    public fun onPlayerDeathEvent(event: PlayerDeathEvent){
        Main.ServerLogger.info(event.deathMessage);
    }
}