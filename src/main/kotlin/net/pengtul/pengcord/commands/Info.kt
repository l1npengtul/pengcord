package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.getUptime
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.roundToLong

/*   This is the class for getting server info from minecraft
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

class Info: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("pengcord.command.info")){
            val currentUsedRAM : Long = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
            val maxRAM : Long = Runtime.getRuntime().totalMemory() /  1048576L
            sender.sendMessage("§9-------§r§6[Server Info]§r§9-------§r")
            sender.sendMessage("§aServer RAM usage: $currentUsedRAM §r§4MiB/$maxRAM MiB (${((currentUsedRAM.toDouble() / maxRAM.toDouble()) * 100).roundToLong() / 100.0}%)")
            sender.sendMessage("§aServer Uptime (HH:MM:SS): ${getUptime()}")
            sender.sendMessage("§aServer TPS: ${Bukkit.getServer().tps}")
            sender.sendMessage("§aOnline: ${Bukkit.getOnlinePlayers()}/${Bukkit.getServer().maxPlayers}")
            sender.sendMessage("§9-----------------------------------§r")
            if (sender is Player){
                Main.discordBot.log("[pengcord]: User ${sender.uniqueId} (${sender.name}) ran `info`.")
            }
            else {
                Main.discordBot.log("[pengcord]: Console ran command `info`.")
            }
            return true
        }
        else {
            if (sender is Player){
                Main.discordBot.log("[pengcord]: User ${sender.uniqueId} (${sender.name}) ran `info`. Failed due to invalid permissions.")
            }
            else {
                Main.discordBot.log("[pengcord]: Console ran command `info`. Failed due to invalid permissions.")
            }
            return true
        }
    }
}