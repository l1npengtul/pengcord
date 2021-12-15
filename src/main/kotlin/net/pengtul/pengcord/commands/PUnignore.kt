package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.Utils
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PUnignore: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.unignore")) {

            Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}`. Failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have permission to run this command!")
            return false
        }
        if (sender !is Player) {
            sender.sendMessage("§cYou must be a player to run this command!")
            return false
        }
        val playerToQuery = Utils.queryDiscordUserFromString(args.getOrNull(0) ?: "")
        if (playerToQuery == null) {
            sender.sendMessage("§cPlease put a valid player!")
            return false
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Utils.unignoreUser(sender.uniqueId, playerToQuery)
            sender.sendMessage("§aYou won't see discord to ingame sync messages from ${playerToQuery}.")
        })
        return true
    }
}