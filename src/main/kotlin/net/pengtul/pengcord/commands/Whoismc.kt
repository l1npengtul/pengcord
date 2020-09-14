package net.pengtul.pengcord.commands


import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/*
*    /whois <MC Username>
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



class Whoismc : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var ret = false
        if (sender.hasPermission("pengcord.command.whois")){
            val player: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(args[0]))
            if (Main.ServerConfig.usersList?.containsValue(player)!!) {
                Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                    Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                        for (key in Main.ServerConfig.usersList?.keys!!) {
                            if (Main.ServerConfig.usersList!![key] == player && key.isNotBlank()) {
                                sender.sendMessage("§6whois for ${args[0]}")
                                sender.sendMessage("§aMinecraft UUID: $player")
                                sender.sendMessage("§aMinecraft Name: ${args[0]}")
                                sender.sendMessage("§9Discord UUID: $key")
                                sender.sendMessage("§9Discord Name: ${Main.discordBot.discordApi.getUserById(key).join().discriminatedName}")
                                ret = true
                            }
                        }
                    })
                }
            } else {
                sender.sendMessage("§cI don't know who is ${args[0]}...")
            }
        }
        else {
            sender.sendMessage("§cYou do not have permission to run this command.")
        }
        return ret
    }
}