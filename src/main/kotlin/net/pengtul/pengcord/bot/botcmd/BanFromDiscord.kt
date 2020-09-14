package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.awt.Color

/*   This is the class for banning from Discord
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

class BanFromDiscord: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Bans a player using discord. Note that this does not support banning players from the discord server itself, just only discord -> Minecraft Server. Note that the <reason> CANNOT contain spaces."
    override val commandName: String
        get() = "pban"
    override val commandUsage: String
        get() = "pban <minecraft username/discord mention> <reason> <time: days>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (Command.doesUserHavePermission(sender, message)){
            if (message.mentionedUsers.size == 1) {
                try {
                    val user: User = message.mentionedUsers[0]
                    val reason: String? = args[1]
                    if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                        Command.banUsingDiscord(sender, message, args[2].toInt(), reason, user)
                    }
                }
                catch (e: Exception){
                    val user: User = message.mentionedUsers[0]
                    if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                        Command.banUsingDiscord(sender, message, null, null, user)
                    }
                }
            }
            else if (message.mentionedUsers.size == 0) {
                try {
                    val playerUUID: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(args[0]))
                    val reason: String? = args[1]
                    val suc = Command.banUsingMinecraft(playerUUID, reason, args[2].toInt(), sender.discriminatedName)
                    Main.discordBot.log("[pengcord]: Attempted/Successful ban of player ${args[0]} ($playerUUID) by user ${sender.discriminatedName} (${sender.idAsString})")
                    if (suc){
                        CommandHelper.deleteAfterSend("Successfully Banned player ${args[0]}", 10, message)
                        val embed = EmbedBuilder()
                                .setAuthor("User Banned")
                                .setTitle("Ban Report for user banned: ${args[0]}")
                                .addInlineField("Banned By:", "${sender.discriminatedName} (${sender.idAsString})")
                                .addInlineField("User Banned:", "${args[0]} ($playerUUID)")
                                .addInlineField("Reason:", "$reason .")
                                .addInlineField("Days:", "${args[2].toInt()} .")
                        Main.discordBot.logEmbed(embed)
                    }
                }
                catch (e: Exception){
                    val playerUUID: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(args[0]))
                    val suc = Command.banUsingMinecraft(playerUUID, "", null, sender.discriminatedName)
                    Main.discordBot.log("[pengcord]: Attempted/Successful ban of player ${args[0]} ($playerUUID) by user ${sender.discriminatedName} (${sender.idAsString})")
                    if (suc){
                        CommandHelper.deleteAfterSend("Successfully Banned player ${args[0]}", 10, message)
                        val embed = EmbedBuilder()
                                .setAuthor("User Banned")
                                .setTitle("Ban Report for user banned: ${args[0]}")
                                .addInlineField("Banned By:", "${sender.discriminatedName} (${sender.idAsString})")
                                .addInlineField("User Banned:", "${args[0]} ($playerUUID)")
                                .addInlineField("Reason:", "null")
                                .addInlineField("Days:", "Permanent")
                        Main.discordBot.logEmbed(embed)
                    }
                }
            }
        }
        else {
            Main.discordBot.log("User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`. Failed due to inadequate permissions.")
            CommandHelper.deleteAfterSend("[pengcord]: You do not have permission to run this command.", 5, message)
        }
    }
}