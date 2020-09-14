package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Unverify: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("pengcord.verify.undo")){
            if (net.pengtul.pengcord.bot.botcmd.Command.removePlayerfromMinecraft(args[0], true)){
                sender.sendMessage("§aSuccessfully Unverified ${args[0]}")
                return true
            }
            else {
                sender.sendMessage("§cFailed to unverify ${args[0]}")
                return false
            }
        }
        else{
            sender.sendMessage("§cYou do not have permission to run this command.")
            return false
        }
    }
}