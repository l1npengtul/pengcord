package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

/*   This is the class for kicking people from discord
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

class Kick: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Kicks and Unverifies user"
    override val commandName: String
        get() = "pkick"
    override val commandUsage: String
        get() = "pkick <MC Username/Discord Mention>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (Command.doesUserHavePermission(sender, message)){
            if (message.mentionedUsers.size == 1){
                val user: User = message.mentionedUsers[0]

                Bukkit.getPluginManager().getPlugin("pengcord")?.let{plugin ->
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        Command.removePlayerfromDiscord(user, message, true)
                        Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`. Kicked ${user.discriminatedName}.")
                        Main.discordBot.sendMessageToDiscord("Successfully Unverified ${user.discriminatedName}")
                    })
                }
            }
            else if (message.mentionedUsers.size == 0){
                try {
                    Bukkit.getPluginManager().getPlugin("pengcord")?.let{plugin ->
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            Command.removePlayerfromMinecraft(args[0], true)
                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`. Kicked ${args[0]}.")
                            Main.discordBot.sendMessageToDiscord("Successfully Unverified ${args[0]}")
                        })
                    }

                }
                catch (e: Exception){
                    Bukkit.getPluginManager().getPlugin("pengcord")?.let{plugin ->
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`. Failed due to invalid user.")
                            CommandHelper.deleteAfterSend("Could not find user ${args[0]}.", 5, message)
                        })
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