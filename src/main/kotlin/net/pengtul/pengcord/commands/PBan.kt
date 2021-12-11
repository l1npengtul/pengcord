package net.pengtul.pengcord.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.util.Utils.Companion.parseTimeFromString
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PBan: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() != 3) {
            sender.sendMessage("§cExpected 3 arguments. Usage: /pban <player> <reason> <time/perm>")
            return false
        }
        if (sender.hasPermission("pengcord.punishments.ban")) {
            val playerToMute = args[0]
            val reason = args[1]
            val until = parseTimeFromString(args[2]) ?: ExpiryDateTime.Permanent

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Utils.queryPlayerFromString(playerToMute)?.let { player ->
                    Main.database.playerGetByCurrentName(sender.name)?.let { senderPlayer ->
                        Utils.banPlayer(
                            player,
                            TypeOfUniqueID.MinecraftTypeOfUniqueID(senderPlayer.playerUUID),
                            until,
                            reason
                        )
                        sender.sendMessage("Banned Player!".toComponent().color(NamedTextColor.GREEN))
                        return@Runnable
                    }
                    // else
                    Utils.banPlayer(player, TypeOfUniqueID.Unknown(sender.name), until, reason)
                    sender.sendMessage("Banned Player!".toComponent().color(NamedTextColor.GREEN))
                    Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                }
            })

            return true
        } else {
            Main.discordBot.log(LogType.DSCComamndError, "User ${sender.name} ran `pban`. Failed due to insufficient permissions.")
            Main.serverLogger.info("User ${sender.name} ran `pban`. Failed due to insufficient permissions.")
            return false
        }
    }
}