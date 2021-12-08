package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class QueryRecord: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.sendMessage("§cExpected 2 arguments. Usage: /queryrecord <b|m|w|f|a> <player>")
            return false
        }

        if (sender.hasPermission("pengcord.punishments.query")) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val whatToQuery = args[0]
                val playerToQuery = queryPlayerFromString(args[1])
                if (playerToQuery == null) {
                    sender.sendMessage("§cInvalid Player!")
                }
                when (whatToQuery.lowercase()) {
                    "all", "a" -> {
                        playerToQuery?.playerUUID?.let {
                            val alerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(it).map { filterAlert ->
                                "§r§5${filterAlert.filterAlertId}§r§a(${filterAlert.word})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${filterAlert.filterAlertId}")
                                )
                            }
                            if (alerts.isNotEmpty()) {
                                sender.sendMessage(
                                    Component.text()
                                        .content("=====Filter Alert Query for player ${playerToQuery.currentUsername}=====")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .append(*alerts.toTypedArray())
                                        .build()
                                )
                            } else {
                                sender.sendMessage("§aNo filter alerts for player ${playerToQuery.currentUsername}")
                            }

                            val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                                "§r§5${warn.warnId}§r§a(${warn.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${warn.warnId}")
                                )
                            }
                            if (warns.isNotEmpty()) {
                                sender.sendMessage(
                                    Component.text()
                                        .content("=====Warns Query for player ${playerToQuery.currentUsername}=====")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .append(*warns.toTypedArray())
                                        .build()
                                )
                            } else {
                                sender.sendMessage("§aNo warns for player ${playerToQuery.currentUsername}")
                            }

                            val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                            val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }
                            val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }
                            val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }

                            if (mutes.isNotEmpty()) {
                                val muteComponent = Component.text()
                                    .content("=====Mutes Query for player ${playerToQuery.currentUsername}=====")
                                    .style(Style.style(NamedTextColor.GREEN))
                                    .append(
                                        Component.text("====EXPIRED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*expiredMutes.toTypedArray())
                                    .append(
                                        Component.text("====PARDONED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*pardonedMutes.toTypedArray())
                                    .append(
                                        Component.text("====ONGOING====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*ongoingMutes.toTypedArray())
                                    .build()
                                sender.sendMessage(muteComponent)
                            } else {
                                sender.sendMessage("§aNo mutes for player ${playerToQuery.currentUsername}")
                            }

                            val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                            val expiredBans = bans.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }
                            val pardonedBans = bans.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }
                            val ongoingBans = bans.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }

                            if (bans.isNotEmpty()) {
                                val bansComponent = Component.text()
                                    .content("=====Bans Query for player ${playerToQuery.currentUsername}=====")
                                    .style(Style.style(NamedTextColor.GREEN))
                                    .append(
                                        Component.text("====EXPIRED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*expiredBans.toTypedArray())
                                    .append(
                                        Component.text("====PARDONED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*pardonedBans.toTypedArray())
                                    .append(
                                        Component.text("====ONGOING====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*ongoingBans.toTypedArray())
                                    .build()
                                sender.sendMessage(bansComponent)
                            } else {
                                sender.sendMessage("§aNo bans for player ${playerToQuery.currentUsername}")
                            }

                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran ${this.javaClass.name}.")
                            Main.serverLogger.info("[pengcord]: User ${sender.name} ran ${this.javaClass.name}.")

                        }
                    }
                    "filter", "filteralerts", "f" -> {
                        playerToQuery?.playerUUID?.let {
                            val alerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(it).map { filterAlert ->
                                "§r§5${filterAlert.filterAlertId}§r§a(${filterAlert.word})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${filterAlert.filterAlertId}")
                                )
                            }
                            if (alerts.isNotEmpty()) {
                                sender.sendMessage(
                                    Component.text()
                                        .content("=====Filter Alert Query for player ${playerToQuery.currentUsername}=====")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .append(*alerts.toTypedArray())
                                        .build()
                                )
                            } else {
                                sender.sendMessage("§aNo filter alerts for player ${playerToQuery.currentUsername}")
                            }
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran ${this.javaClass.name}.")
                            Main.serverLogger.info("[pengcord]: User ${sender.name} ran ${this.javaClass.name}.")
                        }
                    }
                    "warn", "warns", "w" -> {
                        playerToQuery?.playerUUID?.let {
                            val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                                "§r§5${warn.warnId}§r§a(${warn.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${warn.warnId}")
                                )
                            }
                            if (warns.isNotEmpty()) {
                                sender.sendMessage(
                                    Component.text()
                                        .content("=====Warns Query for player ${playerToQuery.currentUsername}=====")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .append(*warns.toTypedArray())
                                        .build()
                                )
                            } else {
                                sender.sendMessage("§aNo warns for player ${playerToQuery.currentUsername}")
                            }
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    "mute", "mutes", "m" -> {
                        playerToQuery?.playerUUID?.let {
                            val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                            val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }
                            val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }
                            val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${mute.muteId}")
                                )
                            }

                            if (mutes.isNotEmpty()) {
                                val muteComponent = Component.text()
                                    .content("=====Mutes Query for player ${playerToQuery.currentUsername}=====")
                                    .style(Style.style(NamedTextColor.GREEN))
                                    .append(
                                        Component.text("====EXPIRED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*expiredMutes.toTypedArray())
                                    .append(
                                        Component.text("====PARDONED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*pardonedMutes.toTypedArray())
                                    .append(
                                        Component.text("====ONGOING====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*ongoingMutes.toTypedArray())
                                    .build()
                                sender.sendMessage(muteComponent)
                            } else {
                                sender.sendMessage("§aNo mutes for player ${playerToQuery.currentUsername}")
                            }
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    "ban", "bans", "b" -> {
                        playerToQuery?.playerUUID?.let {

                            val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                            val expiredBans = bans.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }
                            val pardonedBans = bans.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }
                            val ongoingBans = bans.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})".toComponent(
                                    HoverEvent.showText("Click to query!".toComponent()),
                                    ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/pengcord:querypunishment $whatToQuery ${ban.banId}")
                                )
                            }

                            if (bans.isNotEmpty()) {
                                val bansComponent = Component.text()
                                    .content("=====Bans Query for player ${playerToQuery.currentUsername}=====")
                                    .style(Style.style(NamedTextColor.GREEN))
                                    .append(
                                        Component.text("====EXPIRED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*expiredBans.toTypedArray())
                                    .append(
                                        Component.text("====PARDONED====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*pardonedBans.toTypedArray())
                                    .append(
                                        Component.text("====ONGOING====")
                                            .style(Style.style(NamedTextColor.GREEN))
                                    )
                                    .append(*ongoingBans.toTypedArray())
                                    .build()
                                sender.sendMessage(bansComponent)
                            } else {
                                sender.sendMessage("§aNo bans for player ${playerToQuery.currentUsername}")
                            }

                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    else -> {
                        sender.sendMessage("§cInvalid Query Type!")
                    }
                }
            })
            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name} ran `queryrecord`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `queryrecord`. Failed due to insufficient permissions.")
            return false
        }
    }
}