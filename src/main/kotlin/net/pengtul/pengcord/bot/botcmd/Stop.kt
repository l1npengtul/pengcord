package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.botcmd.Command.Companion.doesUserHavePermission
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

/*   This is the class for shutting down the server from discord
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

class Stop: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Stops the server."
    override val commandName: String
        get() = "stop"
    override val commandUsage: String
        get() = "stop <time: seconds>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (doesUserHavePermission(sender, message)){
            val mArray: List<String> = msg.split(" ")
            val shutdownTimer = try {
                mArray[1].toLong() * 20L
            } catch (e: Exception) {
                200L
            }

            message.server?.let { optional ->
                optional.ifPresent {
                    if (doesUserHavePermission(sender, message)){
                        Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran a shutdown command.")
                            Command.shutdown(shutdownTimer, it)
                        }
                    }
                }
            }
        }
        else {
            CommandHelper.deleteAfterSend("You do not have permission to run this command", 5, message)
            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) tried to shutdown the server but has inadequate permission to do so.")
        }
    }
}