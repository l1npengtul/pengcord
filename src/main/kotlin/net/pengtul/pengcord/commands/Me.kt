package net.pengtul.pengcord.commands

import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.joda.time.Duration
import org.joda.time.Period

class Me: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.me")) {
            sender.sendMessage("§c§nInvalid Permissions!")
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name()} ran `me`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `me`. Failed due to insufficient permissions.")
        }
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Bukkit.getServer().getPlayer(sender.name)?.let { player ->
                Main.database.playerGetByUUID(player.uniqueId)?.let { dbPlayer ->
                    Main.database.playerGetCurrentTimePlayed(player = dbPlayer.playerUUID).onSuccess { time ->
                        sender.sendMessage("§6§nWhoIs Lookup for Discord User ${args[1]}")
                        sender.sendMessage("§aDiscord UUID: ${dbPlayer.discordUUID}")
                        sender.sendMessage("§aMinecraft Username: ${dbPlayer.currentUsername}")
                        sender.sendMessage("§aMinecraft UUID: ${dbPlayer.playerUUID}")
                        if (dbPlayer.firstJoinDateTime != Main.neverHappenedDateTime) {
                            sender.sendMessage("§aFirst Join Date Time: ${dbPlayer.firstJoinDateTime}")
                        }
                        if (dbPlayer.verifiedDateTime != Main.neverHappenedDateTime) {
                            sender.sendMessage("§aLatest Verification Date Time: ${dbPlayer.verifiedDateTime}")
                        }
                        sender.sendMessage("§aTime Played: ${Main.periodFormatter.print(Period(Duration(time * 1000)))}")
                        sender.sendMessage("§aMuted Status: ${dbPlayer.isMuted}")
                        sender.sendMessage("§aDeaths: ${dbPlayer.deaths}")

                        Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                }
            }
            sender.sendMessage("§c§nMinecraft User ${args[1]} not found!")
            return@Runnable
        })
        return true
    }
}