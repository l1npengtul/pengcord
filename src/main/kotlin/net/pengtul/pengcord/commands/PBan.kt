package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.joda.time.DateTime

class PBan: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() != 3) {
            sender.sendMessage("§cExpected 3 arguments. Usage: /pban <player> <reason> <days>")
            return false
        }
        if (sender.hasPermission("pengcord.punishments.ban")) {
            val playerToMute = args[0]
            val reason = args[1]
            val days = args[2].toIntOrNull() ?: 0
            val until = if (days == 0) ExpiryDateTime.Permanent else ExpiryDateTime.DateAndTime(DateTime.now().plusDays(days))

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Utils.queryPlayerFromString(playerToMute)?.let { player ->
                    Main.database.playerGetByCurrentName(sender.name)?.let { senderPlayer ->
                        Utils.mutePlayer(
                            player,
                            TypeOfUniqueID.MinecraftTypeOfUniqueID(senderPlayer.playerUUID),
                            until,
                            reason
                        )
                        return@Runnable
                    }
                    // else
                    Utils.mutePlayer(player, TypeOfUniqueID.Unknown(sender.name), until, reason)
                    Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    Main.serverLogger.info("[pengcord]: User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                }
            })

            return true
        } else {
            Main.discordBot.log(LogType.DSCComamndError, "User ${sender.name()} ran `pban`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `pban`. Failed due to insufficient permissions.")
            return false
        }
    }
}