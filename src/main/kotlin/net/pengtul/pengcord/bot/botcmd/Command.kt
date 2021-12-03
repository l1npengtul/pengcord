package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.bukkit.*
import org.bukkit.plugin.Plugin
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/*
*    This code creates and defines all discord commands
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



class Command {

    companion object {

        fun doesUserHavePermission(user: User, message: Message): Boolean {
            return if (!Main.ServerConfig.adminList.isNullOrEmpty()) {
                var userRoles = ""
                message.server.get().getHighestRole(user).ifPresent { role ->
                    userRoles = role.idAsString
                }
                Main.ServerConfig.adminList!!.contains(userRoles)
            } else {
                message.server.get().isAdmin(user) && Main.ServerConfig.adminNoRole
            }
        }

        fun shutdown(shutdownTimer: Long, plugin: Plugin) {
            for (p in Bukkit.getServer().onlinePlayers) {
                p.sendTitle("§c§lServer Shutdown in ${shutdownTimer / 20}s", "§cPlease reconnect to this server soon!", 10, 70, 20)
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 0L)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 8L)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 16L)
                })
            }
            Bukkit.getServer().broadcastMessage("§k--------------------------------------------")
            Bukkit.getServer().broadcastMessage("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds).")
            Bukkit.getServer().broadcastMessage("§c§l§nPlease reconnect after the reboot!")
            Bukkit.getServer().broadcastMessage("§k--------------------------------------------")

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                Bukkit.shutdown()
            }, shutdownTimer)
        }

        fun removePlayerfromDiscord(user: User, message: Message, doKick: Boolean): Boolean{
            var ret = false
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                if (doKick){
                    Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList!![user.idAsString]))?.kickPlayer("Kicked by operator.")
                }
                Main.ServerConfig.usersList!!.remove(user.idAsString)
                CommandHelper.deleteAfterSend("Successfully unverified user ${user.idAsString}", 10, message)
                ret = true
            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${user.idAsString}.", 5, message)
            }
            return ret
        }

        private fun removePlayerfromMinecraft(username: String): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                val uuid = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(username))
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    for (key in Main.ServerConfig.usersList!!.keys) {
                        Main.ServerLogger.info("$uuid, $key")
                        if (Main.ServerConfig.usersList!![key] == uuid && key.isNotBlank()) {
                            Main.ServerLogger.info("$uuid, $key")
                            Main.ServerConfig.usersList!!.remove(key)
                            ret = true
                        }
                    }
                })
            }
            return ret
        }

        fun removePlayerfromMinecraft(username: String, doKick: Boolean): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                val uuid = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(username))
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    for (key in Main.ServerConfig.usersList!!.keys) {
                        Main.ServerLogger.info("$uuid, $key")
                        if (Main.ServerConfig.usersList!![key] == uuid && key.isNotBlank()) {
                            Main.ServerLogger.info("$uuid, $key")
                            if (doKick) {
                                Bukkit.getScheduler().runTask(it, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(uuid))?.kickPlayer("Kicked by operator.")
                                })
                            }
                            Main.ServerConfig.usersList!!.remove(key)
                            ret = true
                        }
                    }
                })
            }
            return ret
        }

        fun banUsingDiscord(user: User, message: Message, days: Int?, reason: String?, userToBan: User){
            var banUntil: Date? = Date()
            if (days != null){
                if (banUntil != null) {
                    if (days <= 0){
                        banUntil = null
                    }
                    else{
                        banUntil.time = banUntil.time + TimeUnit.DAYS.toMillis(days.toLong())
                    }
                }
            }
            else {
                banUntil = null
            }

            val username: String = Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList?.getValue(user.idAsString)!!).username

            if (Main.ServerConfig.usersList?.containsKey(userToBan.idAsString)!!) {
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        username,
                        reason,
                        banUntil,
                        user.discriminatedName
                )
                if (banUntil == null){
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        Bukkit.getScheduler().runTask(it, Runnable {
                            Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Permanent Ban.")
                            if (removePlayerfromMinecraft(username)) {
                                CommandHelper.deleteAfterSend("Banned and removed user $username", 10, message)
                                Main.discordBot.log("[pengcord]: Attempted/Successful ban of player ${userToBan.discriminatedName} (${userToBan.idAsString}) by user ${user.discriminatedName} (${user.idAsString}).")
                                val embed = EmbedBuilder()
                                        .setAuthor("User Banned")
                                        .setTitle("Ban Report for user banned: ${userToBan.discriminatedName}")
                                        .addInlineField("Banned By:", "${user.discriminatedName} (${user.idAsString})")
                                        .addInlineField("User Banned:", "${userToBan.discriminatedName} (${userToBan.idAsString})")
                                        .addInlineField("Reason:", "$reason .")
                                        .addInlineField("Days:", "Permanent")
                                Main.discordBot.logEmbed(embed)
                            }
                        })
                    }
                }
                else {
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        Bukkit.getScheduler().runTask(it, Runnable {
                            Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Banned until $banUntil")
                            if (removePlayerfromMinecraft(username)) {
                                Main.discordBot.log("[pengcord]: Attempted/Successful ban of player ${userToBan.discriminatedName} (${userToBan.idAsString}) by user ${user.discriminatedName} (${user.idAsString}).")
                                val embed = EmbedBuilder()
                                        .setAuthor("User Banned")
                                        .setTitle("Ban Report for user banned: ${userToBan.discriminatedName}")
                                        .addInlineField("Banned By:", "${user.discriminatedName} (${user.idAsString})")
                                        .addInlineField("User Banned:", "${userToBan.discriminatedName} (${userToBan.idAsString})")
                                        .addInlineField("Reason:", "$reason .")
                                        .addInlineField("Days:", "$banUntil .")
                                Main.discordBot.logEmbed(embed)
                                CommandHelper.deleteAfterSend("Banned and removed user $username", 10, message)
                            }
                        })
                    }
                }
            }
            else {
                Main.discordBot.log("[pengcord]: Attempted ban of player ${userToBan.discriminatedName} (${userToBan.idAsString}) by user ${user.discriminatedName} (${user.idAsString}). User to ban does not exist, command execution failure.")
                CommandHelper.deleteAfterSend("Error: Could not find ${userToBan.idAsString}.", 5, message)
            }
        }

        fun banUsingMinecraft(banUUID: String, reason: String?, days: Int?, banUser: String?): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let { plugin ->
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    for (key in Main.ServerConfig.usersList?.keys!!) {
                        if (Main.ServerConfig.usersList!![key] == banUUID && key.isNotBlank()) {
                            var banUntil: Date? = Date()
                            if (days != null) {
                                if (banUntil != null) {
                                    if (days <= 0) {
                                        banUntil = null
                                    } else {
                                        banUntil.time = banUntil.time + TimeUnit.DAYS.toMillis(days.toLong())
                                    }
                                }
                            } else {
                                banUntil = null
                            }

                            val username: String = Main.mojangAPI.getPlayerProfile(banUUID).username

                            Bukkit.getBanList(BanList.Type.NAME).addBan(
                                    username,
                                    reason,
                                    banUntil,
                                    banUser
                            )
                            if (banUntil == null) {
                                Bukkit.getScheduler().runTask(plugin, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(banUUID))?.kickPlayer("Banned by $banUser for $reason. Permanent Ban.")
                                    ret = removePlayerfromMinecraft(username)
                                })

                            } else {
                                Bukkit.getScheduler().runTask(plugin, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(banUUID))?.kickPlayer("Banned by $banUser for $reason. Banned until $banUntil")
                                    ret = removePlayerfromMinecraft(username)
                                })
                            }
                            ret = true
                        }
                    }
                })
            }
            return true
        }

        fun getUptime(): String {
            val uptime: Long = ManagementFactory.getRuntimeMXBean().uptime
            return (((uptime / 360000.0) * 100000.0).roundToLong() / 100000.0).toString()
        }
    }
}


