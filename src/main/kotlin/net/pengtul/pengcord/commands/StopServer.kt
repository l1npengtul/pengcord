package net.pengtul.pengcord.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StopServer: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var ret = false
        val shutdownTimer = try {
            args[0].toLong() * 20L
        } catch (e: Exception) {
            200L
        }

        if (sender.hasPermission("pengcord.command.stopserver")) {
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                net.pengtul.pengcord.bot.botcmd.Command.shutdown(shutdownTimer, it)
                ret = true
            }
        }
        return ret
    }
}