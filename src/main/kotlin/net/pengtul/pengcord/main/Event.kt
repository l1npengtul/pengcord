package net.pengtul.pengcord.main

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.util.toComponent
import net.pengtul.pengcord.util.toStr
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
            if (Main.checkIfPlayerVerifiedCache(event.uniqueId)) {
                Main.serverLogger.info("[pengcord]: User ${event.name} (${event.uniqueId}) connected, is verified. Adding to cache.")
            }
        })
    }

    // Player Leave/Join
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent){
        event.joinMessage()?.let {
            if (Main.serverConfig.enableSync){
                Main.discordBot.sendMessageToDiscordJoinLeave(it.toStr().replace("§e",""))
                Main.downloadSkin(event.player)
                Main.discordBot.log(LogType.PlayerJoin, "${it.toStr()} (user ${event.player.uniqueId}).")
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
                Main.discordBot.sendMessageToDiscordJoinLeave(it.toStr().replace("§e", ""))
                Main.discordBot.log(LogType.PlayerLeave, "${it.toStr()} (user ${event.player.uniqueId }).")
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
                Main.discordBot.log(LogType.PlayerLeave, "${it.toStr()} for reason ${event.reason()} (user ${event.player.uniqueId }).")
                Main.discordBot.sendMessageToDiscordJoinLeave("${it.toStr().replace("§e", "")}. Reason: ${event.reason().toStr().replace("§e", "")}")
            }

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateTimePlayed(event.player.uniqueId)
            })
        }
    }

    // Chat Events
    @EventHandler
    fun onPlayerChatEvent(event: AsyncChatEvent) {
        if (event.isAsynchronous) {
            val message = event.message().toStr().replace("@", "@_")
            if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(event.player.uniqueId))) {
                Main.discordBot.log(LogType.Verification, "<${event.player.name}> Attempted to say \" $message \"" +
                        "\nbut user is not verified!")
                Main.serverLogger.info(
                    "[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" $message \"" +
                            "\nbut user is not verified!"
                )
                event.isCancelled = true
                return
            }

            if (Main.database.checkIfPlayerMuted(event.player.uniqueId)) {
                Main.discordBot.log(LogType.PlayerMuted, "<${event.player.name}> Attempted to say \" $message \"" +
                        "\nbut user is muted!")
                Main.serverLogger.info("[MC-EVENT-PLAYERCHAT]: <${event.player.name}> Attempted to say \" $message \"" +
                        "\nbut user is muted!" )
                event.isCancelled = true
                return
            }

            if (Main.serverConfig.enableSync) {
                if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                    if (!Main.discordBot.chatFilterRegex.containsMatchIn(message.lowercase())){
                        var prefix = Main.vaultChatApi.getPlayerPrefix(event.player)
                        prefix = if (prefix.isEmpty()) {
                            ""
                        } else {
                            "[$prefix] "
                        }

                        Main.discordBot.sendMessagetoWebhook(
                            message,
                            "$prefix${event.player.name}",
                            event.player
                        )
                        Main.discordBot.log(LogType.PlayerChat, "<${event.player.name}> $message")
                    }
                    else if (Main.serverConfig.bannedWords.isNotEmpty()){
                        event.player.sendMessage(Main.serverConfig.filteredMessage)
                        Main.discordBot.log(LogType.ChatFilter, "User ${event.player.name} (${event.player.uniqueId }) tripped chat filter with message $message")
                        val matchedWords = Main.discordBot.chatFilterRegex.findAll(message.lowercase()).map {
                            it.value
                        }.joinToString()
                        Main.database.addFilterAlertToPlayer(player = event.player.uniqueId, w = matchedWords, message).onFailure { exception ->
                            Main.serverLogger.warning("[ChatFilter] [SQLError]: Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                            Main.discordBot.log(LogType.GenericError, "Failed to add filter alert to ${event.player.name} (${event.player.uniqueId }) due to error: $exception")
                        }
                        event.isCancelled = true
                    }
                } else {
                    var prefix = Main.vaultChatApi.getPlayerPrefix(event.player)
                    prefix = if (prefix.isEmpty()) {
                        ""
                    } else {
                        "[$prefix] "
                    }
                    Main.discordBot.sendMessagetoWebhook(
                        message,
                        "$prefix${event.player.name}",
                        event.player
                    )
                    Main.discordBot.log(LogType.PlayerChat, "<${event.player.name}> $message")
                }
            }
        }
    }

    @EventHandler
    fun onBroadcastChatEvent(event: BroadcastMessageEvent){
        Main.serverLogger.info("broadcast ${event.message().toStr()}")
        if (Main.serverConfig.enableSync){
            if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                if (!Main.discordBot.chatFilterRegex.containsMatchIn(event.message().toStr()) && Main.serverConfig.bannedWords.isNotEmpty()){
                    Main.discordBot.log(LogType.Announcement, "${event.message().toStr()}.")
                    Main.discordBot.sendMessageToDiscordAnnouncement(event.message().toStr())
                }
                else if (Main.serverConfig.bannedWords.isNotEmpty()) {
                    Main.discordBot.log(LogType.Announcement, "Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message().toStr()}.")
                    Main.serverLogger.info("[ChatFilter]: Message of unknown origin (Message Broadcast) tripped chat filter with message ${event.message().toStr()}.")
                    event.isCancelled = true
                }
            } else {
                Main.discordBot.log(LogType.Announcement, "${event.message().toStr()}.")
                Main.discordBot.sendMessageToDiscordAnnouncement(event.message().toStr())
            }
        }
    }

    // Death Messages
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent){
        val death = event.deathMessage()
        if(Main.serverConfig.enableSync && death != null) {
            Main.discordBot.log(LogType.InGameStuff, "${death.toStr()}.")
            Main.discordBot.sendMessageToDiscordInGame(death.toStr())
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.database.playerDied(event.player.uniqueId)
        })
    }

    // Prevent unregistered players from moving
    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent){
        if (Main.serverConfig.enableVerify){
            if (!Main.checkIfPlayerVerifiedCache(event.player.uniqueId) && !Main.checkIfPlayerPendingVerification(event.player.uniqueId)) {
                val unverifiedComponent = Component.text()
                    .content("You are not verified! Do `/verify <discord tag>` to start!")
                    .append(Component.newline())
                    .append("/verify clyde#0000 (replace clyde#0000 with your own discord username and tag)".toComponent())
                    .hoverEvent(
                        HoverEvent.showText("Click for command".toComponent())
                    )
                    .clickEvent(
                        ClickEvent.suggestCommand("/pengcord:verify ")
                    )
                    .build()
                event.player.sendMessage(unverifiedComponent)
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
            if (!Main.checkIfPlayerVerifiedCache(event.player.uniqueId) && !Main.checkIfPlayerPendingVerification(event.player.uniqueId)) {
                val unverifiedComponent = Component.text()
                    .content("You are not verified! Do `/verify <discord tag>` to start!")
                    .append(Component.newline())
                    .append("/verify clyde#0000 (replace clyde#0000 with your own discord username and tag)".toComponent())
                    .hoverEvent(
                        HoverEvent.showText("Click for command".toComponent())
                    )
                    .clickEvent(
                        ClickEvent.suggestCommand("/pengcord:verify ")
                    )
                    .build()
                event.player.sendMessage(unverifiedComponent)
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
                Main.discordBot.sendMessageToDiscordInGame(msg.toStr())
            }
        }
    }
}