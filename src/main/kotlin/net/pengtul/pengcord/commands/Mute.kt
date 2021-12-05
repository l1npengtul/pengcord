package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.banPlayer
import net.pengtul.pengcord.Utils.Companion.mutePlayer
import net.pengtul.pengcord.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.joda.time.DateTime

class Mute: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() != 3) {
            sender.sendMessage("§cExpected 3 arguments. Usage: /mute <player> <reason> <days>")
            return false
        }
        if (sender.hasPermission("pengcord.punishments.mute")) {
            val playerToBan = args[0]
            val reason = args[1]
            val days = args[2].toIntOrNull() ?: 0
            val until = if (days == 0) ExpiryDateTime.Permanent else ExpiryDateTime.DateAndTime(DateTime.now().plusDays(days))

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                queryPlayerFromString(playerToBan)?.let { player ->
                    Main.database.playerGetByCurrentName(sender.name)?.let { senderPlayer ->
                        banPlayer(player, TypeOfUniqueID.MinecraftTypeOfUniqueID(senderPlayer.playerUUID), until, reason)
                        return@Runnable
                    }
                    // else
                    banPlayer(player, TypeOfUniqueID.Unknown(sender.name), until, reason)
                }
            })

            return true
        } else {
            Main.discordBot.log("[pengcord]: User ${sender.name} ran `pban`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `pban`. Failed due to insufficient permissions.")
            return false
        }
    }
}