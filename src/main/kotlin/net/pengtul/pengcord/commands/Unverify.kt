package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.unverifyPlayer
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/*
*    The Unverify and Kick command
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



class Unverify: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = if (args.isEmpty()) {
                    Main.database.playerGetByCurrentName(sender.name) ?: return@Runnable
                } else {
                    Main.database.playerGetByCurrentName(args[0]) ?: return@Runnable
                }

                unverifyPlayer(TypeOfUniqueID.MinecraftTypeOfUniqueID(player.playerUUID))
            })
            return true
        }

        else if (sender.hasPermission("pengcord.verify.undo")){
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = if (args.isEmpty()) {
                    Main.database.playerGetByCurrentName(sender.name) ?: return@Runnable
                } else {
                    Main.database.playerGetByCurrentName(args[0]) ?: return@Runnable
                }

                unverifyPlayer(TypeOfUniqueID.MinecraftTypeOfUniqueID(player.playerUUID))
            })
            return true
        }
        else{
            sender.sendMessage("§cYou do not have permission to run this command.")
            Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `unverify`. Failed due to invalid permissions.")
            Main.serverLogger.info("[MC]: User ${sender.name} ran `unverify`. Failed due to invalid permissions.")
            return false
        }
    }
}