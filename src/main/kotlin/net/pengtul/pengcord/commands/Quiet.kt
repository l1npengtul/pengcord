package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Quiet: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && sender.hasPermission("pengcord.silent.chat")) {
            Main.silentSet.add(sender.uniqueId)
            sender.sendMessage("§aYour chats will no longer be synced!")
            return true
        } else {
            sender.sendMessage("§cInadequate Permission!")
            return false
        }
    }
}