package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.joda.time.Duration
import org.joda.time.Period

class WhoIs: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (args.size != 1 || args.isEmpty()) {
            sender.sendMessage("Command usage: /whois [Minecraft Username/Minecraft UUID/Discord Discriminated Username/Discord UUID Long]")
            return false
        }

        if (sender.hasPermission("pengcord.command.whois")){
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                queryPlayerFromString(args[0])?.let { dbPlayer ->
                    val playerMutes = Main.database.queryPlayerMutesByPlayerMinecraft(dbPlayer.playerUUID)
                    val activePlayerMutes = Main.database.queryPlayerMutesByPlayerMinecraft(dbPlayer.playerUUID).filter {
                        it.expiryState == ExpiryState.OnGoing
                    }
                    val playerWarns = Main.database.queryPlayerWarnsByPlayerMinecraft(dbPlayer.playerUUID)
                    val playerBans = Main.database.queryPlayerBansByPlayerMinecraft(dbPlayer.playerUUID)
                    val activePlayerBans = Main.database.queryPlayerBansByPlayerMinecraft(dbPlayer.playerUUID).filter {
                        it.expiryState == ExpiryState.OnGoing
                    }
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
                        if (sender.hasPermission("pengcord.punishments.query")) {
                            sender.sendMessage("§a# Of Warns: ${playerWarns.count()}")
                            sender.sendMessage("§aBanned Status: ${dbPlayer.isBanned}")
                            sender.sendMessage("§a# Of Bans: ${activePlayerBans.count()} active, ${playerBans.count()} total")
                            sender.sendMessage("§aMuted Status: ${dbPlayer.isMuted}")
                            sender.sendMessage("§a# Of Mutes: ${activePlayerMutes.count()} active, ${playerMutes.count()} total")
                        }
                        sender.sendMessage("§aDeaths: ${dbPlayer.deaths}")
                        Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `whois` with ${args[0]}.")
                        Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `whois` with ${args[0]}.")
                        return@Runnable
                    }
                }
                sender.sendMessage("§cPlayer Not Found.")
            })
            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name()} ran `whois` with argument ${args[0]}. Failed due to invalid permission.")
            Main.serverLogger.info("User ${sender.name()} ran `whois` with argument ${args[0]}. Failed due to invalid permission.")
            sender.sendMessage("§cYou do not have permission to run this command.")
            return false
        }
    }
}