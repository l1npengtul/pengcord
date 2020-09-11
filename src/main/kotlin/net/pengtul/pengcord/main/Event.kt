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
            Main.discordBot.sendMessageToDiscord("${event.joinMessage?.replace("§e","")}")
            Main.downloadSkin(event.player)
            if (event.player.hasPermission("pengcord.verify.bypass")){
                Main.ServerConfig.usersList?.put("", event.player.uniqueId.toString())
                event.player.sendMessage("§aCongratulations! You have super bypass powers!")
            }
        }
    }
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.quitMessage}");
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${event.quitMessage?.replace("§e", "")}")
        }
    }
    @EventHandler
    fun onPlayerKickEvent(event: PlayerKickEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${event.leaveMessage.replace("§e", "")}. Reason: ${event.reason.replace("§e", "")}")
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncPlayerChatEvent){
        if(Main.ServerConfig.enableSync) {
            if (!Main.discordBot.chatFilterRegex.matches(event.message.toLowerCase())){
                Main.discordBot.sendMessagetoWebhook(event.message, event.player.displayName, null, event.player)
            }
            else {
                event.player.sendMessage(Main.ServerConfig.bannedWordMessage!!)
            }
        }
    }
    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (Main.ServerConfig.enableSync){
            if (!Main.discordBot.chatFilterRegex.matches(event.message.toLowerCase())){
                Main.discordBot.sendMessageToDiscord(event.message)
            }
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${event.deathMessage}")
        }
    }

    // Prevent unregistered players from moving
    @EventHandler
    public fun onPlayerMoveEvent(event: PlayerMoveEvent){
        if (Main.ServerConfig.verienable){
            if (!Main.ServerConfig.usersList?.containsValue(event.player.uniqueId.toString())!!){
                event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                if (!event.player.isInvulnerable){
                    event.player.isInvulnerable = true
                }
                event.isCancelled = true;
            }
        }
    }
}