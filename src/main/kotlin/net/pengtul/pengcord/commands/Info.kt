package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.pengtul.pengcord.Utils.Companion.getUptime
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.toComponentNewline
import net.pengtul.pengcord.toComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
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

            val serverInfoText = Component.text()
                .content("§9=======§r§6[Server Info]§r§9=======§r")
                .append(Component.newline())
                .append("§aServer RAM usage: $currentUsedRAM MiB/$maxRAM MiB (${((currentUsedRAM.toDouble() / maxRAM.toDouble()) * 100).roundToLong() / 100.0}%)".toComponent())
                .append("§aServer Uptime (HH:MM:SS): ${getUptime()}".toComponent())
                .append("§aServer TPS: ${Bukkit.getServer().tps}".toComponent())
                .append("§aOnline: ${Bukkit.getOnlinePlayers().joinToString { it.name }}\nout of ${Bukkit.getServer().maxPlayers}".toComponent())
                .append("§9==========================§r".toComponent())
                .build()
            sender.sendMessage(serverInfoText)
            
            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args}\".")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `${this.javaClass.name}` with args \"${args}\".")
            return true
        }
        else {
            Main.discordBot.log(LogType.MCComamndError,"User ${sender.name} ran `info`. Failed due to invalid permissions.")
            Main.serverLogger.info("User ${sender.name} ran `info`. Failed due to invalid permissions.")

            return true
        }
    }
}