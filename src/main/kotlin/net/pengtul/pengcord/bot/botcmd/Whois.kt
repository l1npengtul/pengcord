package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.io.File

/*   This is the class for getting whois data from discord
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

class Whois: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets additional information about this player/user"
    override val commandName: String
        get() = "whois"
    override val commandUsage: String
        get() = "whois <MC Username/Discord Mention>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        Main.ServerLogger.info(args[0])
        if (message.mentionedUsers.size == 1) {
            val user: User = message.mentionedUsers[0]
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                try {
                    val player: String = Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList!!.getValue(user.idAsString)).username
                    val uuid: String = Main.mojangAPI.getUUIDOfUsername(player)
                    Main.downloadSkinUUID(uuid)
                    val playerIco = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png")
                    if (playerIco.exists()){
                        val embed = EmbedBuilder()
                                .setAuthor("Discord whois lookup")
                                .setTitle("Whois for user ${user.discriminatedName}.")
                                .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png"))
                                .addField("Discord UUID:", user.idAsString + "")
                                .addInlineField("Minecraft Username:", "$player ")
                                .addInlineField("Minecraft UUID:", "${Main.insertDashUUID(uuid)} ")
                        message.serverTextChannel.ifPresent {
                            it.sendMessage(embed)
                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`.")
                            Main.discordBot.logEmbed(embed)
                            Main.ServerLogger.info(args[0])
                        }
                    }
                    else {
                        val embed = EmbedBuilder()
                                .setAuthor("Discord whois lookup")
                                .setTitle("Whois for user ${user.discriminatedName} ")
                                .addField("Discord UUID:", user.idAsString + " ")
                                .addInlineField("Minecraft Username:", "$player ")
                                .addInlineField("Minecraft UUID:", "${Main.insertDashUUID(uuid)} ")
                        message.serverTextChannel.ifPresent {
                            it.sendMessage(embed)
                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`.")
                            Main.discordBot.logEmbed(embed)
                            Main.ServerLogger.info(args[0])
                        }
                    }
                }
                catch (e: Exception){
                    Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`. Failed due to invalid user. ")
                    CommandHelper.deleteAfterSend("Player likely does not exist.", 5, message)
                }
            }
        }
        else if (message.mentionedUsers.size == 0) {
            val player: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(args[0]))
            if (Main.ServerConfig.usersList?.containsValue(player)!!) {
                Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let { plugin ->
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                        Main.ServerLogger.info(args[0])
                        for (key in Main.ServerConfig.usersList?.keys!!) {
                            if (Main.ServerConfig.usersList!!.getValue(key) == player && key.isNotBlank()) {
                                try {
                                    Main.ServerLogger.info(key)
                                    val user: User = Main.discordBot.discordApi.getUserById(key).join()
                                    Main.downloadSkinUUID(player)
                                    val playerIco = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png")
                                    if (playerIco.exists()){
                                        val embed: EmbedBuilder = EmbedBuilder()
                                                .setAuthor("Discord whois lookup")
                                                .setTitle("Whois for user ${Main.mojangAPI.getPlayerProfile(player).username} ")
                                                .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png"))
                                                .addField("Minecraft UUID:", "$player ")
                                                .addInlineField("Discord Username: ", "${user.discriminatedName} ")
                                                .addInlineField("Discord UUID:", "$key ")
                                        message.serverTextChannel.ifPresent {
                                            it.sendMessage(embed)
                                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`.")
                                            Main.discordBot.logEmbed(embed)
                                            Main.ServerLogger.info(args[0])
                                        }
                                        return@Runnable
                                    }
                                    else {
                                        val embed: EmbedBuilder = EmbedBuilder()
                                                .setAuthor("Discord whois lookup")
                                                .setTitle("Whois for user ${Main.mojangAPI.getPlayerProfile(player).username} ")
                                                //.setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png"))
                                                .addField("Minecraft UUID:", "$player ")
                                                .addInlineField("Discord Username: ", "${user.discriminatedName} ")
                                                .addInlineField("Discord UUID:", "$key ")
                                        message.serverTextChannel.ifPresent {
                                            it.sendMessage(embed)
                                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`.")
                                            Main.discordBot.logEmbed(embed)
                                            Main.ServerLogger.info(args[0])
                                        }
                                        return@Runnable
                                    }
                                }
                                catch (e: Exception){
                                    Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`. Failed due to invalid user. ")
                                    CommandHelper.deleteAfterSend("Player likely does not exist.", 5, message)
                                }
                            }
                        }
                    })
                }
            } else {
                Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`. Failed due to invalid user. ")
                CommandHelper.deleteAfterSend("Player does not exist or has bypass permissions.", 8, message)
            }

        } else {
            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`. Failed due to invalid user. ")
            CommandHelper.deleteAfterSend("Please enter a valid person to look up! Usage: ${Main.ServerConfig.botPrefix}whois <discord user mention> ", 5, message)
        }
    }
}