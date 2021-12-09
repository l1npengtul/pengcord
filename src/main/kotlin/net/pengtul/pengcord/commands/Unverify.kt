package net.pengtul.pengcord.commands

import net.pengtul.pengcord.util.Utils.Companion.unverifyPlayer
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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
        if (sender is Player && args.isEmpty() && sender.hasPermission("pengcord.verify.undoself")) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = Main.database.playerGetByCurrentName(sender.name) ?: return@Runnable

                unverifyPlayer(TypeOfUniqueID.MinecraftTypeOfUniqueID(player.playerUUID))
                Main.removePlayerFromVerifiedCache(player.playerUUID)
                Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
            })
            return true
        } else if (sender.hasPermission("pengcord.verify.undo") && args.isNotEmpty()) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = Main.database.playerGetByCurrentName(args[0]) ?: return@Runnable

                unverifyPlayer(TypeOfUniqueID.MinecraftTypeOfUniqueID(player.playerUUID))
                Main.removePlayerFromVerifiedCache(player.playerUUID)
                Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
            })
            return true
        } else {
            sender.sendMessage("§cInvalid arguments!")
            return false
        }
    }
}
