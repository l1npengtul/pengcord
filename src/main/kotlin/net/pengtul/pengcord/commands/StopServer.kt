package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/*
*    Stops the server (MC Command)
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




class StopServer: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var ret = false
        val shutdownTimer = try {
            args[0].toLong() * 20L
        } catch (e: Exception) {
            sender.sendMessage("§cPlease enter a valid time!")
            return false
        }

        if (sender.hasPermission("pengcord.command.stopserver")) {
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                net.pengtul.pengcord.bot.botcmd.Command.shutdown(shutdownTimer, it)
                if (sender is Player){
                    Main.discordBot.log("[pengcord]: User ${sender.uniqueId} (${sender.name}) ran `stopserver`.")
                }
                else {
                    Main.discordBot.log("[pengcord]: Console ran command `stopserver`.")
                }
                ret = true
            }
        }
        else {
            if (sender is Player){
                Main.discordBot.log("[pengcord]: User ${sender.uniqueId} (${sender.name}) ran `stopserver`. Failed due to invalid permission.")
            }
            else {
                Main.discordBot.log("[pengcord]: Console ran command `stopserver`. Failed due to invalid permission.")
            }
        }
        return ret
    }
}