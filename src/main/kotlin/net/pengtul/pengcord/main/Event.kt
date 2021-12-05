package net.pengtul.pengcord.main

import io.papermc.paper.event.player.AsyncChatEvent
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.event.player.*
import org.joda.time.DateTime
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
        event.joinMessage()?.let {
            if (Main.serverConfig.enableSync){
                Main.discordBot.sendMessageToDiscord(it.toString().replace("§e",""))
                Main.downloadSkin(event.player)
                Main.discordBot.log("[pengcord]: [MC-EVENT-JOIN]: $it (user ${event.player.uniqueId }).")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerNew(event.player.uniqueId)
                Main.database.playerUpdateCurrentUsername(event.player.uniqueId)
                Main.playersCurrentJoinTime[event.player.uniqueId] = DateTime.now()
            })
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent){
        event.quitMessage()?.let {
            if(Main.serverConfig.enableSync) {
                Main.discordBot.sendMessageToDiscordJoinLeave(it.toString().replace("§e", ""))
                Main.discordBot.log("[pengcord]: [MC-EVENT-QUIT]: $it (user ${event.player.uniqueId }).")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateTimePlayed(event.player.uniqueId)
            })
        }
    }
    @EventHandler
    fun onPlayerKickEvent(event: PlayerKickEvent){
        event.leaveMessage().let {
            if(Main.serverConfig.enableSync) {
                Main.discordBot.log("[pengcord]: [MC-EVENT-KICK]: $it for reason ${event.reason()} (user ${event.player.uniqueId }).")
                Main.discordBot.sendMessageToDiscordJoinLeave("${it.toString().replace("§e", "")}. Reason: ${event.reason().toString().replace("§e", "")}")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateTimePlayed(event.player.uniqueId)
            })
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncChatEvent){
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            // check if player verified
            if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(
                    event.player.uniqueId
            ))) {
                Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is not verified!")
                Main.serverLogger.info(
                    "[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                            "\nbut user is not verified!"
                )
                event.isCancelled = true
                return@Runnable
            }

            if (Main.database.checkIfPlayerMuted(event.player.uniqueId)) {
                Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is muted!")
                Main.serverLogger.info("[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is muted!" )
                event.isCancelled = true
                return@Runnable
            }

            if (Main.serverConfig.enableSync) {
                if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message().toString().lowercase(Locale.getDefault()))){
                    Main.discordBot.sendMessagetoWebhook(event.message().toString(), event.player.displayName().toString(), null, event.player)
                    Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERCHAT]: <${event.player.name}> ${event.message()}")
                }
                else {
                    event.player.sendMessage(Main.serverConfig.filteredMessage)
                    Main.discordBot.log("[pengcord]: [ChatFilter]: User ${event.player.name} (${event.player.uniqueId }) tripped chat filter with message ${event.message()}")
                    val matchedWords = Main.discordBot.chatFilterRegex.findAll(event.message().toString()).joinToString()
                    Main.database.addFilterAlertToPlayer(player = event.player.uniqueId, w = matchedWords, event.message().toString()).onFailure { exception ->
                        Main.serverLogger.warning("[ChatFilter] [SQLError]: Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                        Main.discordBot.log("[pengcord]: [ChatFilter] [SQLError]: Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                    }
                    event.isCancelled = true
                }
            }
        })


    }
    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (Main.serverConfig.enableSync){
            if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message().toString())){
                Main.discordBot.log("[pengcord]: [MC-EVENT-BROADCAST]: ${event.message()}.")
                Main.discordBot.sendMessageToDiscordAnnouncement(event.message().toString())
                event.isCancelled = false
            }
            else {
                Main.discordBot.log("[pengcord]: [ChatFilter]: Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message()}.")
                Main.serverLogger.info("[ChatFilter]: Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message()}.")
                event.isCancelled = true
            }
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        if(Main.serverConfig.enableSync) {
            Main.discordBot.log("[pengcord]: [MC-EVENT-PLAYERDEATH]: ${event.deathMessage()}.")
            Main.discordBot.sendMessageToDiscordInGame("${event.deathMessage()}")
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.database.playerDied(event.player.uniqueId)
        })
    }

    // Prevent unregistered players from moving
    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent){
        if (Main.serverConfig.enableVerify){
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(event.player.uniqueId))) {
                    event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                    event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                }
                if (!event.player.isInvulnerable || !event.player.isInvisible) {
                    event.player.isInvulnerable = true
                    event.player.isInvisible = true
                }

                event.isCancelled = true
            })
        }
    }

    // Prevent unregistered players from interacting
    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent){
        if (Main.serverConfig.enableVerify){
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(event.player.uniqueId))) {
                    event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                    event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                }
                if (!event.player.isInvulnerable || !event.player.isInvisible) {
                    event.player.isInvulnerable = true
                    event.player.isInvisible = true
                }

                event.isCancelled = true
            })
        }
    }
}