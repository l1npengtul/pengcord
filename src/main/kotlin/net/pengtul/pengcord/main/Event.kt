package net.pengtul.pengcord.main

import io.papermc.paper.event.player.AsyncChatEvent
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.toComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.event.player.*
import org.joda.time.DateTime
import kotlin.collections.HashMap

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
    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (Main.serverConfig.enableLiterallyNineteenEightyFour && Main.serverConfig.bannedWords.isNotEmpty()) {
            if (Main.discordBot.chatFilterRegex.containsMatchIn(event.name.lowercase()) &&  Main.serverConfig.bannedWords.isNotEmpty()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Please change your minecraft username!".toComponent())
            } else {
                event.allow()
            }
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.insertIntoVerifiedCache(event.uniqueId)
        })
    }

    // Player Leave/Join
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent){
        event.joinMessage()?.let {
            if (Main.serverConfig.enableSync){
                Main.discordBot.sendMessageToDiscordJoinLeave(it.toString().replace("§e",""))
                Main.downloadSkin(event.player)
                Main.discordBot.log(LogType.PlayerJoin, "$it (user ${event.player.uniqueId }).")
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
                Main.discordBot.log(LogType.PlayerLeave, "$it (user ${event.player.uniqueId }).")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateTimePlayed(event.player.uniqueId)
                Main.removePlayerFromVerifiedCache(event.player.uniqueId)
            })
        }
    }

    @EventHandler
    fun onPlayerKickEvent(event: PlayerKickEvent){
        Main.playersAwaitingVerification = Main.playersAwaitingVerification.filterValues { v ->
            v != event.player.uniqueId
        } as HashMap<Int, MinecraftId>
        event.leaveMessage().let {
            if(Main.serverConfig.enableSync) {
                Main.discordBot.log(LogType.PlayerLeave, "$it for reason ${event.reason()} (user ${event.player.uniqueId }).")
                Main.discordBot.sendMessageToDiscordJoinLeave("${it.toString().replace("§e", "")}. Reason: ${event.reason().toString().replace("§e", "")}")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateTimePlayed(event.player.uniqueId)
            })
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncChatEvent) {
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(event.player.uniqueId))) {
                Main.discordBot.log(LogType.Verification, "<${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is not verified!")
                Main.serverLogger.info(
                    "[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                            "\nbut user is not verified!"
                )
                event.isCancelled = true
                return@Runnable
            }

            if (Main.database.checkIfPlayerMuted(event.player.uniqueId)) {
                Main.discordBot.log(LogType.PlayerMuted, "<${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is muted!")
                Main.serverLogger.info("[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" ${event.message()} \"" +
                        "\nbut user is muted!" )
                event.isCancelled = true
                return@Runnable
            }

            if (Main.serverConfig.enableSync) {
                if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                    if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message().toString().lowercase())){
                        val prefix = Main.vaultChatApi.getPlayerPrefix(event.player)
                        Main.discordBot.sendMessagetoWebhook(
                            event.message().toString(),
                            "[${prefix}] ${event.player.displayName()}",
                            event.player
                        )
                        Main.discordBot.log(LogType.PlayerChat, "<${event.player.name}> ${event.message()}")
                    }
                    else if (Main.serverConfig.bannedWords.isNotEmpty()){
                        event.player.sendMessage(Main.serverConfig.filteredMessage)
                        Main.discordBot.log(LogType.ChatFilter, "User ${event.player.name} (${event.player.uniqueId }) tripped chat filter with message ${event.message()}")
                        val matchedWords = Main.discordBot.chatFilterRegex.findAll(event.message().toString().lowercase()).joinToString()
                        Main.database.addFilterAlertToPlayer(player = event.player.uniqueId, w = matchedWords, event.message().toString()).onFailure { exception ->
                            Main.serverLogger.warning("[ChatFilter] [SQLError]: Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                            Main.discordBot.log(LogType.GenericError, "Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                        }
                        event.isCancelled = true
                    }
                } else {
                    val prefix = Main.vaultChatApi.getPlayerPrefix(event.player)
                    Main.discordBot.sendMessagetoWebhook(
                        event.message().toString(),
                        "[${prefix}] ${event.player.displayName()}",
                        event.player
                    )
                    Main.discordBot.log(LogType.PlayerChat, "<${event.player.name}> ${event.message()}")
                }
            }
        })
    }

    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        if (Main.serverConfig.enableSync){
            if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message().toString())){
                    Main.discordBot.log(LogType.Announcement, "${event.message()}.")
                    Main.discordBot.sendMessageToDiscordAnnouncement(event.message().toString())
                    event.isCancelled = false
                }
                else if (Main.serverConfig.bannedWords.isNotEmpty()) {
                    Main.discordBot.log(LogType.Announcement, "Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message()}.")
                    Main.serverLogger.info("[ChatFilter]: Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message()}.")
                    event.isCancelled = true
                }
            } else {
                Main.discordBot.log(LogType.Announcement, "${event.message()}.")
                Main.discordBot.sendMessageToDiscordAnnouncement(event.message().toString())
                event.isCancelled = false

            }
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        if(Main.serverConfig.enableSync) {
            Main.discordBot.log(LogType.InGameStuff, "${event.deathMessage()}.")
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
            if (Main.checkIfPlayerVerifiedCache(event.player.uniqueId)) {
                event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                if (!event.player.isInvulnerable) {
                    event.player.isInvulnerable = true
                }
                if (!event.player.isInvisible) {
                    event.player.isInvisible = true
                }
                event.isCancelled = true
            } else {
                if (event.player.isInvulnerable) {
                    event.player.isInvulnerable = false
                }
                if (event.player.isInvisible) {
                    event.player.isInvisible = false
                }
            }
        }
    }

    // Prevent unregistered players from interacting
    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent){
        if (Main.serverConfig.enableVerify){
            if (Main.checkIfPlayerVerifiedCache(event.player.uniqueId)) {
                event.player.sendMessage("§cYou are not verified! Do `/verify <discord tag>` to start!")
                event.player.sendMessage("§ce.g. /verify clyde#0000 (replace clyde#0000 with your own discord username and tag)")
                if (!event.player.isInvulnerable) {
                    event.player.isInvulnerable = true
                }
                if (!event.player.isInvisible) {
                    event.player.isInvisible = true
                }
                event.isCancelled = true
            } else {
                if (event.player.isInvulnerable) {
                    event.player.isInvulnerable = false
                }
                if (event.player.isInvisible) {
                    event.player.isInvisible = false
                }
            }
        }
    }

    @EventHandler
    fun onPlayerAdvancementDoneEvent(event: PlayerAdvancementDoneEvent) {
        if (Main.serverConfig.enableSync) {
            event.message()?.let { msg ->
                Main.discordBot.sendMessageToDiscordInGame(msg.toString())
            }
        }
    }
}