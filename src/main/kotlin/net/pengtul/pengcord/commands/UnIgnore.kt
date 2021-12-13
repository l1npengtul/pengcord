package net.pengtul.pengcord.commands

import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.Utils
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnIgnore: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.ignore")) {
            
            Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}`. Failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have permission to run this command!")
            return false
        }
        if (sender !is Player) {
            sender.sendMessage("§cYou must be a player to run this command!")
            return false
        }
        val playerToUnignore = Utils.queryPlayerFromString(args.getOrNull(0) ?: "")
        if (playerToUnignore == null) {
            sender.sendMessage("§cPlease put a valid player!")
            return false
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.database.removeIgnoreOfPlayers(sender.uniqueId, playerToUnignore.playerUUID)
            sender.sendMessage("§aYou will now see discord to ingame messages from ${playerToUnignore.currentUsername}.")
        })
        return true
    }
}