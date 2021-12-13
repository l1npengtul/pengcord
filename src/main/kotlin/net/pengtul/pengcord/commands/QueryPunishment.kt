package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.pengtul.pengcord.util.Utils.Companion.timeToOrSinceDateTime
import net.pengtul.pengcord.util.LogType
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
                            val alertComponent = Component.text()
                                .content("=========Filter Alert Query for ID ${filterAlert.filterAlertId}=========\n")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("FilterAlert ID: ${filterAlert.filterAlertId}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${filterAlert.playerUUID}\n")
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
                                        .content("Target Discord UUID: ${filterAlert.discordUUID}\n")
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
                                        .content("Issued On: ${filterAlert.issuedOn}\n")
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
                                        .content("Message Context: ${filterAlert.context}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Triggered Words: ${filterAlert.word}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .build()
                            sender.sendMessage(alertComponent)
                            
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "warn", "warns", "w" -> {
                        Main.database.queryPlayerWarnById(punishmentToQuery)?.let { warn ->
                            val warnComponent = Component.text()
                                .content("=========Warn Query for ID ${warn.warnId}=========\n")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Warn ID: ${warn.warnId}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${warn.playerUUID}\n")
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
                                        .content("Target Discord UUID: ${warn.discordUUID}\n")
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
                                        .content("Issued By: ${warn.issuedBy}\n")
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
                                        .content("Issued On: ${warn.issuedOn}\n")
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
                                        .content("Reason: ${warn.reason}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .build()
                            sender.sendMessage(warnComponent)
                            
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "mute", "mutes", "m" -> {
                        Main.database.queryPlayerMuteById(punishmentToQuery)?.let { mute ->
                            val muteComponent = Component.text()
                                .content("=========Mute Query for ID ${mute.muteId}=========\n")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Mute ID: ${mute.muteId}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${mute.playerUUID}\n")
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
                                        .content("Target Discord UUID: ${mute.discordUUID}\n")
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
                                        .content("Issued By: ${mute.issuedBy}\n")
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
                                        .content("Issued On: ${mute.issuedOn}\n")
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
                                        .content("Reason: ${mute.reason}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                            if (mute.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Mute\n")
                                muteComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: Never, Permanent Mute\n")
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
                                            .content("Expires: ${mute.expiresOn}\n")
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
                                        .content("Mute Status: ${mute.expiryState}\n")
                                        .style(Style.style()
                                            .color(NamedTextColor.GREEN)
                                            .build()
                                        )
                                        .build()
                                )
                            sender.sendMessage(muteComponent.build())
                            
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "ban", "bans", "b" -> {
                        Main.database.queryPlayerBanById(punishmentToQuery)?.let { ban ->
                            val banComponent = Component.text()
                                .content("=========Ban Query for ID ${ban.banId}=========\n")
                                .style(Style.style(NamedTextColor.GREEN))
                                .append(
                                    Component.text()
                                        .content("Ban ID: ${ban.banId}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                                .append(
                                    Component.text()
                                        .content("Target UUID ${ban.playerUUID}\n")
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
                                        .content("Target Discord UUID: ${ban.discordUUID}\n")
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
                                        .content("Issued By: ${ban.issuedBy}\n")
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
                                        .content("Issued On: ${ban.issuedOn}\n")
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
                                        .content("Reason: ${ban.reason}\n")
                                        .style(Style.style(NamedTextColor.GREEN))
                                        .build()
                                )
                            if (ban.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Ban\n")
                                banComponent
                                    .append(
                                        Component.text()
                                            .content("Expires: Never, Permanent Ban\n")
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
                                            .content("Expires: ${ban.expiresOn}\n")
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
                                        .content("Ban Status: ${ban.expiryState}\n")
                                        .style(Style.style()
                                            .color(NamedTextColor.GREEN)
                                            .build()
                                        )
                                        .build()
                                )
                            sender.sendMessage(banComponent.build())
                            
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
            
            Main.serverLogger.info("User ${sender.name} ran `querypunishment`. Failed due to insufficient permissions.")
            return false
        }
    }
}