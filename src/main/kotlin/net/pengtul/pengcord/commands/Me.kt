package net.pengtul.pengcord.commands

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.pengtul.pengcord.util.Utils.Companion.timeToOrSinceDateTime
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.joda.time.Duration
import org.joda.time.Period

class Me: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.me")) {
            sender.sendMessage("§c§nInvalid Permissions!")
            
            Main.serverLogger.info("User ${sender.name} ran `me`. Failed due to insufficient permissions.")
        }

        if (sender !is Player) {
            sender.sendMessage("§c§nYou must be a player to run this command!")
        } else {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.queryAllPlayers().forEach {
                    Main.serverLogger.info(it.currentUsername)
                }
                Main.database.playerGetByUUID(sender.uniqueId)?.let { dbPlayer ->
                    Main.database.playerGetCurrentTimePlayed(player = dbPlayer.playerUUID).onSuccess { time ->
                        sender.sendMessage("§6§nWhoIs Lookup for User ${sender.name}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, sender.name)
                        ))

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
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Main.periodFormatter.print(Period(Duration(time * 1000))))
                        ))

                        sender.sendMessage("§aMuted Status: ${dbPlayer.isMuted}".toComponent(
                            HoverEvent.showText("Click to query!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:queryrecord m ${dbPlayer.playerUUID}")
                        ))

                        sender.sendMessage("§aDeaths: ${dbPlayer.deaths}".toComponent(
                            HoverEvent.showText("Click to copy!".toComponent()),
                            ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dbPlayer.deaths.toString())
                        ))

                        
                        Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}` with args \"${sender.name}\".")
                        return@Runnable
                    }
                }
                sender.sendMessage("§c§nMinecraft User ${sender.name} not found!")
                return@Runnable
            })
        }


        return true
    }
}