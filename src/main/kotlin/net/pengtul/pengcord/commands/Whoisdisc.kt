package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.lang.StringBuilder

class Whoisdisc: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var ret = false
        if (sender.hasPermission("pengcord.command.whois")){
            val argument = StringBuilder()
            for (arg in args){
                if (arg.startsWith("#")){
                    argument.append(arg)
                }
                else if (!argument.isBlank()){
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
                            ret = true
                        }
                    }
                })
            }
        }
        return ret
    }
}