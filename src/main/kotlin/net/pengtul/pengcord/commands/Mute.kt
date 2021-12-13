package net.pengtul.pengcord.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.pengtul.pengcord.util.Utils.Companion.mutePlayer
import net.pengtul.pengcord.util.Utils.Companion.parseTimeFromString
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Mute: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() != 3) {
            sender.sendMessage("§cExpected 3 arguments. Usage: /mute <player> <reason> <time/perm>")
            return false
        }
        if (sender.hasPermission("pengcord.punishments.mute")) {
            val playerToBan = args[0]
            val reason = args[1]
            val until = parseTimeFromString(args[2]) ?: ExpiryDateTime.Permanent

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                queryPlayerFromString(playerToBan)?.let { player ->
                    Main.database.playerGetByCurrentName(sender.name)?.let { senderPlayer ->
                        Bukkit.getPlayer(senderPlayer.playerUUID)?.let { bukkitPlayer ->
                            if (bukkitPlayer.hasPermission("pengcord.punishments.mute") || bukkitPlayer.isOp) {
                                sender.sendMessage("§cCannot ban another moderator!")
                                
                                Main.serverLogger.info("User ${sender.name} ran `pban`. Failed due to attempt mute other moderator.")
                                return@Runnable
                            }
                            mutePlayer(player, TypeOfUniqueID.MinecraftTypeOfUniqueID(senderPlayer.playerUUID), until, reason)
                            sender.sendMessage("Muted Player!".toComponent().color(NamedTextColor.GREEN))
                            
                            Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                        return@Runnable
                    }
                    // else
                    mutePlayer(player, TypeOfUniqueID.Unknown(sender.name), until, reason)
                    sender.sendMessage("Muted Player!".toComponent().color(NamedTextColor.GREEN))
                    
                    Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                }
            })

            return true
        } else {
            
            Main.serverLogger.info("User ${sender.name} ran `mute`. Failed due to insufficient permissions.")
            return false
        }
    }
}