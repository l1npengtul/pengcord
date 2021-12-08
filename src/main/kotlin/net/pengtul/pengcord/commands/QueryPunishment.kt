package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.pengtul.pengcord.util.Utils.Companion.timeToOrSinceDateTime
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class QueryPunishment: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.sendMessage("§cExpected 2 arguments. Usage: /querypunishment <b|m|w|f> <punishmentId>")
            return false
        }

        if (sender.hasPermission("pengcord.punishments.query")) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val whatToQuery = args[0]
                val punishmentToQuery = args[1].toLong()

                when (whatToQuery.lowercase()) {
                    "filter", "filteralerts", "f" -> {
                        Main.database.queryPlayerFilterAlertById(punishmentToQuery)?.let { filterAlert ->
//                            sender.sendMessage("§a=========Filter Alert Query for ID ${filterAlert.filterAlertId}=========")
//                            sender.sendMessage("§aFilterAlert ID: ${filterAlert.filterAlertId}")
//                            sender.sendMessage("§aTarget UUID ${filterAlert.playerUUID}")
//                            sender.sendMessage("§aTarget Discord UUID: ${filterAlert.discordUUID}")
//                            sender.sendMessage("§aIssued On: ${filterAlert.issuedOn}")
//                            sender.sendMessage("§aMessage Context: ${filterAlert.context}")
//                            sender.sendMessage("§aTriggered Words: ${filterAlert.word}")
                            val alertComponent = Component.text()
                                .content("=========Filter Alert Query for ID ${filterAlert.filterAlertId}=========")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("FilterAlert ID: ${filterAlert.filterAlertId}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${filterAlert.playerUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${filterAlert.playerUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target Discord UUID: ${filterAlert.discordUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${filterAlert.discordUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued On: ${filterAlert.issuedOn}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                timeToOrSinceDateTime(filterAlert.issuedOn).toComponent()
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Message Context: ${filterAlert.context}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Triggered Words: ${filterAlert.word}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .build()
                            sender.sendMessage(alertComponent)
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `querypunishment`.")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "warn", "warns", "w" -> {
                        Main.database.queryPlayerWarnById(punishmentToQuery)?.let { warn ->
//                            sender.sendMessage("§a=========Warns Query for ID ${warn.warnId}=========")
//                            sender.sendMessage("§aWarn ID: ${warn.warnId}")
//                            sender.sendMessage("§aTarget UUID ${warn.playerUUID}")
//                            sender.sendMessage("§aTarget Discord UUID: ${warn.discordUUID}")
//                            sender.sendMessage("§aIssued By: ${warn.issuedBy}")
//                            sender.sendMessage("§aIssued On: ${warn.issuedOn}")
//                            sender.sendMessage("§aReason: ${warn.reason}")
                            val warnComponent = Component.text()
                                .content("=========Warn Query for ID ${warn.warnId}=========")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Warn ID: ${warn.warnId}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${warn.playerUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${warn.playerUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target Discord UUID: ${warn.discordUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${warn.discordUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued By: ${warn.issuedBy}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${warn.issuedBy}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued On: ${warn.issuedOn}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                timeToOrSinceDateTime(warn.issuedOn).toComponent()
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Reason: ${warn.reason}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .build()
                            sender.sendMessage(warnComponent)
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `query`.")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "mute", "mutes", "m" -> {
                        Main.database.queryPlayerMuteById(punishmentToQuery)?.let { mute ->
//                            sender.sendMessage("§a=========Mutes Query for ID ${mute.muteId}=========")
//                            sender.sendMessage("§aMute ID: ${mute.muteId}")
//                            sender.sendMessage("§aTarget UUID ${mute.playerUUID}")
//                            sender.sendMessage("§aTarget Discord UUID: ${mute.discordUUID}")
//                            sender.sendMessage("§aIssued By: ${mute.issuedBy}")
//                            sender.sendMessage("§aIssued On: ${mute.issuedOn}")
//                            if (mute.isPermanent) {
//                                sender.sendMessage("§aExpires: §c§lNever, Permanent Mute")
//                            } else {
//                                sender.sendMessage("§aExpires: ${mute.expiresOn}")
//                            }
//                            sender.sendMessage("§aReason: ${mute.reason}")
//                            if (mute.expiryState != ExpiryState.OnGoing) {
//                                sender.sendMessage("§aMute Status: §2§l${mute.expiryState}")
//                            } else {
//                                sender.sendMessage("§aMute Status: §c§l${mute.expiryState}")
//                            }
                            val muteComponent = Component.text()
                                .content("=========Mute Query for ID ${mute.muteId}=========")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Mute ID: ${mute.muteId}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${mute.playerUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${mute.playerUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target Discord UUID: ${mute.discordUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${mute.discordUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued By: ${mute.issuedBy}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${mute.issuedBy}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued On: ${mute.issuedOn}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                timeToOrSinceDateTime(mute.issuedOn).toComponent()
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Reason: ${mute.reason}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                            if (mute.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Mute")
                                muteComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: Never, Permanent Mute")
                                            .style(Style.style()
                                                .decorate(TextDecoration.BOLD)
                                                .decorate(TextDecoration.ITALIC)
                                                .color(NamedTextColor.RED)
                                                .build()
                                            )
                                            .build()
                                    )
                            } else {
                                muteComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: ${mute.expiresOn}")
                                            .style(Style.style()
                                                .decorate(TextDecoration.BOLD)
                                                .decorate(TextDecoration.ITALIC)
                                                .color(NamedTextColor.RED)
                                                .build()
                                            )
                                            .hoverEvent(
                                                HoverEvent.showText(
                                                    timeToOrSinceDateTime(mute.expiresOn).toComponent()
                                                )
                                            )
                                            .build()
                                    )
                            }
                            muteComponent
                                .append(
                                    Component.text()
                                        .content("Mute Status: ${mute.expiryState}")
                                        .style(Style.style()
                                            .color(NamedTextColor.GREEN)
                                            .build()
                                        )
                                        .build()
                                )
                            sender.sendMessage(muteComponent.build())
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `querypunishment`.")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "ban", "bans", "b" -> {
                        Main.database.queryPlayerBanById(punishmentToQuery)?.let { ban ->
//                            sender.sendMessage("§a=========Bans Query for ID ${ban.banId}=========")
//                            sender.sendMessage("§aBan ID: ${ban.banId}")
//                            sender.sendMessage("§aTarget UUID ${ban.playerUUID}")
//                            sender.sendMessage("§aTarget Discord UUID: ${ban.discordUUID}")
//                            sender.sendMessage("§aIssued By: ${ban.issuedBy}")
//                            sender.sendMessage("§aIssued On: ${ban.issuedOn}")
//                            if (ban.isPermanent) {
//                                sender.sendMessage("§aExpires: §c§lNever, Permanent Ban")
//                            } else {
//                                sender.sendMessage("§aExpires: ${ban.expiresOn}")
//                            }
//                            sender.sendMessage("§aReason: ${ban.reason}")
//                            if (ban.expiryState != ExpiryState.OnGoing) {
//                                sender.sendMessage("§aBan Status: §2§l${ban.expiryState}")
//                            } else {
//                                sender.sendMessage("§aBan Status: §c§l${ban.expiryState}")
//                            }
                            val banComponent = Component.text()
                                .content("=========Ban Query for ID ${ban.banId}=========")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Ban ID: ${ban.banId}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${ban.playerUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${ban.playerUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target Discord UUID: ${ban.discordUUID}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${ban.discordUUID}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued By: ${ban.issuedBy}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                "Click to look up".toComponent()
                                            )
                                        )
                                        .clickEvent(
                                            ClickEvent.clickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                "/pengcord:whois ${ban.issuedBy}"
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Issued On: ${ban.issuedOn}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .hoverEvent(
                                            HoverEvent.showText(
                                                timeToOrSinceDateTime(ban.issuedOn).toComponent()
                                            )
                                        )
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Reason: ${ban.reason}")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                            if (ban.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Ban")
                                banComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: Never, Permanent Ban")
                                            .style(Style.style()
                                                .decorate(TextDecoration.BOLD)
                                                .decorate(TextDecoration.ITALIC)
                                                .color(NamedTextColor.RED)
                                                .build()
                                            )
                                            .build()
                                    )
                            } else {
                                banComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: ${ban.expiresOn}")
                                            .style(Style.style()
                                                .decorate(TextDecoration.BOLD)
                                                .decorate(TextDecoration.ITALIC)
                                                .color(NamedTextColor.RED)
                                                .build()
                                            )
                                            .hoverEvent(
                                                HoverEvent.showText(
                                                    timeToOrSinceDateTime(ban.expiresOn).toComponent()
                                                )
                                            )
                                            .build()
                                    )
                            }
                            banComponent
                                .append(
                                    Component.text()
                                        .content("Ban Status: ${ban.expiryState}")
                                        .style(Style.style()
                                            .color(NamedTextColor.GREEN)
                                            .build()
                                        )
                                        .build()
                                )
                            sender.sendMessage(banComponent.build())
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `querypunishment`.")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    else -> {
                        sender.sendMessage("§cInvalid Query Type!")
                    }
                }
            })
            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name} ran `querypunishment`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `querypunishment`. Failed due to insufficient permissions.")
            return false
        }
    }
}