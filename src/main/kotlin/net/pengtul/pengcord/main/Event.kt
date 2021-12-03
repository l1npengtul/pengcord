package net.pengtul.pengcord.main

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.event.player.*
import java.util.*

/*
*    Minecraft Event Handler
*    Copyright (C) 2020  Lewis Rho
*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


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
            Main.discordBot.log("[pengcord]: [MC-EVENT-JOIN]: ${event.joinMessage} (user ${event.player.uniqueId }).")
        }
    }
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent){
        //Main.ServerLogger.info("[${Main.ServerConfig.ServerName}]: ${event.quitMessage}");
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.sendMessageToDiscord("${event.quitMessage?.replace("§e", "")}")
            Main.discordBot.log("[pengcord]: [MC-EVENT-QUIT]: ${event.quitMessage} (user ${event.player.uniqueId }).")
        }
    }
    @EventHandler
    fun onPlayerKickEvent(event: PlayerKickEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.log("[pengcord]: [MC-EVENT-KICK]: ${event.leaveMessage} for reason ${event.reason} (user ${event.player.uniqueId }).")
            Main.discordBot.sendMessageToDiscord("${event.leaveMessage.replace("§e", "")}. Reason: ${event.reason.replace("§e", "")}")
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncPlayerChatEvent){
        if(Main.ServerConfig.enableSync) {
            if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message.lowercase(Locale.getDefault()))){
                Main.discordBot.sendMessagetoWebhook(event.message, event.player.displayName, null, event.player)
                Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERCHAT]: <${event.player.name}> ${event.message}")
            }
            else {
                event.player.sendMessage(Main.ServerConfig.bannedWordMessage!!)
                Main.discordBot.log("[pengcord]: [ChatFilter]: User ${event.player.name} (${event.player.uniqueId }) tripped chat filter with message ${event.message}")
                event.isCancelled = true
            }
        }
    }
    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (Main.ServerConfig.enableSync){
            if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message)){
                Main.discordBot.log("[pengcord]: [MC-EVENT-BROADCAST]: ${event.message}.")
                Main.discordBot.sendMessageToDiscord(event.message)
                event.isCancelled = false
            }
            else {
                Main.discordBot.log("[pengcord]: [ChatFilter]: Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message}.")
                event.isCancelled = true
            }
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        if(Main.ServerConfig.enableSync) {
            Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERDEATH]: ${event.deathMessage}.")
            Main.discordBot.sendMessageToDiscord("${event.deathMessage}")
        }
    }

    // Prevent unregistered players from moving
    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent){
        if (Main.ServerConfig.verienable){
            if (!Main.ServerConfig.usersList?.containsValue(event.player.uniqueId.toString())!!){
                event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                if (!event.player.isInvulnerable) {
                    event.player.isInvulnerable = true
                }
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent){
        if (Main.ServerConfig.verienable){
            if (!Main.ServerConfig.usersList?.containsValue(event.player.uniqueId.toString())!!){
                event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                if (!event.player.isInvulnerable) {
                    event.player.isInvulnerable = true
                }
                event.isCancelled = true
            }
        }
    }
}