package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.lang.StringBuilder

/*
*    /whois <Discord Tag> command
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



class Whoisdisc: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var ret = false
        if (sender.hasPermission("pengcord.command.whois")){
            val argument = StringBuilder()
            for (arg in args){
                if (arg.startsWith("#")){
                    argument.append(arg)
                }
                else if (argument.isNotBlank()){
                    argument.append(" $arg")
                }
                else {
                    argument.append(arg)
                }
            }
            Bukkit.getPluginManager().getPlugin("pengcord")?.let {pl ->
                Bukkit.getScheduler().runTaskAsynchronously(pl, Runnable {
                    Main.discordBot.discordApi.getServerById(Main.ServerConfig.serverBind!!).ifPresent{server ->
                        server.getMemberByDiscriminatedNameIgnoreCase(argument.toString()).ifPresent { user ->
                            val minecraftUUID = Main.ServerConfig.usersList?.get(user.idAsString)
                            val minecraftUsername = Main.mojangAPI.getPlayerProfile(minecraftUUID).username
                            sender.sendMessage("§6whois for $argument")
                            sender.sendMessage("§aMinecraft UUID: $minecraftUUID")
                            sender.sendMessage("§aMinecraft Name: $minecraftUsername")
                            sender.sendMessage("§9Discord UUID: ${user.idAsString}")
                            sender.sendMessage("§9Discord Name: ${user.discriminatedName}")
                            Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `whoisdisc` with argument ${argument}.")
                            ret = true
                        }
                    }
                })
            }
        }
        else {
            Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `whoisdisc`. Failed due to inadequate permissions.")
        }
        return ret
    }
}