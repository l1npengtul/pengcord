package net.pengtul.pengcord.commands

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.util.Utils.Companion.timeToOrSinceDateTime
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
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
                    val playerFilterAlerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(dbPlayer.playerUUID)
                    val playerBans = Main.database.queryPlayerBansByPlayerMinecraft(dbPlayer.playerUUID)
                    val activePlayerBans = Main.database.queryPlayerBansByPlayerMinecraft(dbPlayer.playerUUID).filter {
                        it.expiryState == ExpiryState.OnGoing
                    }
                    Main.database.playerGetCurrentTimePlayed(player = dbPlayer.playerUUID).onSuccess { time ->
                        sender.sendMessage("§6§nWhoIs Lookup for User ${dbPlayer.currentUsername}")

                        sender.sendMessage("§aDiscord UUID: ${dbPlayer.discordUUID}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.discordUUID.toString())
                        ))

                        sender.sendMessage("§aMinecraft Username: ${dbPlayer.currentUsername}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.currentUsername)
                        ))

                        sender.sendMessage("§aMinecraft UUID: ${dbPlayer.playerUUID}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.playerUUID.toString())
                        ))

                        if (dbPlayer.firstJoinDateTime != Main.neverHappenedDateTime) {
                            sender.sendMessage("§aFirst Join Date Time: ${dbPlayer.firstJoinDateTime}".toComponent(
                                HoverEvent.showText(timeToOrSinceDateTime(dbPlayer.firstJoinDateTime).toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.firstJoinDateTime.toString())
                            ))
                        }

                        if (dbPlayer.verifiedDateTime != Main.neverHappenedDateTime) {
                            sender.sendMessage("§aLatest Verification Date Time: ${dbPlayer.verifiedDateTime}".toComponent(
                                HoverEvent.showText(timeToOrSinceDateTime(dbPlayer.verifiedDateTime).toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.verifiedDateTime.toString())
                            ))
                        }

                        sender.sendMessage("§aTime Played (HH:MM:SS): ${Main.periodFormatter.print(Period(Duration(time * 1000)))}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Main.periodFormatter.print(Period(Duration(time * 1000))).toString())
                        ))

                        if (sender.hasPermission("pengcord.punishments.query")) {
                            sender.sendMessage("§a# Of Filter Alerts: ${playerFilterAlerts.count()}".toComponent(
                                HoverEvent.showText("Click to query!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:queryrecord f ${dbPlayer.playerUUID}")
                            ))

                            sender.sendMessage("§a# Of Warns: ${playerWarns.count()}".toComponent(
                                HoverEvent.showText("Click to query!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:queryrecord w ${dbPlayer.playerUUID}")
                            ))


                            sender.sendMessage("§aBanned Status: ${dbPlayer.isBanned}".toComponent(
                                HoverEvent.showText("Click to query!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:queryrecord b ${dbPlayer.playerUUID}")
                            ))

                            sender.sendMessage("§a# Of Bans: ${activePlayerBans.count()} active, ${playerBans.count()} total".toComponent(
                                HoverEvent.showText("Click to copy!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "${activePlayerBans.count()} active, ${playerBans.count()} total")
                            ))

                            sender.sendMessage("§aMuted Status: ${dbPlayer.isMuted}".toComponent(
                                HoverEvent.showText("Click to query!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:queryrecord m ${dbPlayer.playerUUID}")
                            ))

                            sender.sendMessage("§a# Of Mutes: ${activePlayerMutes.count()} active, ${playerMutes.count()} total".toComponent(
                                HoverEvent.showText("Click to copy!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "${activePlayerMutes.count()} active, ${playerMutes.count()} total")
                            ))
                        }

                        sender.sendMessage("§aDeaths: ${dbPlayer.deaths}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.deaths.toString())
                        ))

                        
                        Main.serverLogger.info("User ${sender.name} ran `whois` with ${args[0]}.")
                        return@Runnable
                    }
                }
                sender.sendMessage("§cPlayer Not Found.")
            })
            return true
        } else {
            
            Main.serverLogger.info("User ${sender.name} ran `whois` with argument ${args[0]}. Failed due to invalid permission.")
            sender.sendMessage("§cYou do not have permission to run this command.")
            return false
        }
    }
}