package net.pengtul.pengcord.main

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.event.player.*

class Event : Listener{
    // Player Leave/Join
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.joinMessage}");
        if (Main.ServerConfig.enableSync){
            Main.discordBot.sendMessageToDiscord("${Main.ServerConfig.serverPrefix}: ${event.joinMessage?.replace("§e","")}")
            Main.downloadSkin(event.player)
        }
    }
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.quitMessage}");
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${Main.ServerConfig.serverPrefix}: ${event.quitMessage?.replace("§e", "")}")
        }
    }
    @EventHandler
    fun onPlayerKickEvent(event: PlayerKickEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${Main.ServerConfig.serverPrefix}: ${event.leaveMessage.replace("§e", "")}. Reason: ${event.reason.replace("§e", "")}")
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncPlayerChatEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessagetoWebhook(event.message, event.player.displayName, null, event.player)
        }
    }
    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (!event.message.startsWith("§7[DSC]") && Main.ServerConfig.enableSync){
            Main.discordBot.sendMessageToDiscord("${Main.ServerConfig.serverPrefix}: ${event.message}")
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${Main.ServerConfig.serverPrefix}: ${event.deathMessage}")
        }
    }

    // Prevent unregistered players from moving
    @EventHandler
    public fun onPlayerMoveEvent(event: PlayerMoveEvent){
        if (Main.ServerConfig.verienable){
            if (!Main.ServerConfig.usersList?.containsValue(event.player.uniqueId.toString())!!){
                event.player.sendMessage("§cYou are not verified! Do `/verify ${event.player.name}` to start!")
                if (!event.player.isInvulnerable){
                    event.player.isInvulnerable = true
                }
                event.isCancelled = true;
            }
        }
    }
}