package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PIgnore: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.ignore")) {
            Main.serverLogger.info(LogType.MCComamndError,"User ${sender.name} ran `${this.javaClass.name}`. Failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have permission to run this command!")
            return false
        }
        if (sender !is Player) {
            sender.sendMessage("§cYou must be a player to run this command!")
            return false
        }
        val playerToQuery = queryPlayerFromString(args.getOrNull(0) ?: "")
        if (playerToQuery == null) {
            sender.sendMessage("§cPlease put a valid player!")
            return false
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.database.addIgnore(sender.uniqueId, playerToQuery.playerUUID)
            sender.sendMessage("§aYou won't see discord to ingame sync messages from ${playerToQuery.currentUsername}.")
        })
        return true
    }
}