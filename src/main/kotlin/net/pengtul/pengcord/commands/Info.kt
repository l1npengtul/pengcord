package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.pengtul.pengcord.util.Utils.Companion.getUptime
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.util.toComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.math.roundToInt
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
                .content("§9=======§r§6[Server Info]§r§9=======§r\n")
                .append("§aServer RAM usage: $currentUsedRAM MiB/$maxRAM MiB (${((currentUsedRAM.toDouble() / maxRAM.toDouble()) * 100).roundToLong()}%)\n".toComponent())
                .append("§aServer Uptime (HH:MM:SS): ${getUptime()}\n".toComponent())
                .append("§aServer TPS [1M, 5M, 15M]: ${Bukkit.getTPS().map { (it * 100).roundToInt() / 100.0 }.joinToString()}\n".toComponent())
                .append("§aOnline: ${Bukkit.getOnlinePlayers().joinToString { it.name }}\nout of ${Bukkit.getServer().maxPlayers}\n".toComponent())

            if (!Main.serverConfig.discordServerLink.isNullOrBlank()) {
                serverInfoText.append("Discord Server: ${Main.serverConfig.discordServerLink}".toComponent(
                    HoverEvent.showText("Click to join!".toComponent()),
                    ClickEvent.openUrl(Main.serverConfig.discordServerLink.toString())
                ))
            }
            serverInfoText.append("§9==========================§r".toComponent())
            sender.sendMessage(serverInfoText)

            Main.serverLogger.info(LogType.MCComamndRan,"User ${sender.name} ran `${this.javaClass.name}` with args \"${args}\".")
            return true
        }
        else {
            
            Main.serverLogger.info(LogType.MCComamndError,"User ${sender.name} ran `info`. Failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have permission to run this command!")
            return false
        }
    }
}