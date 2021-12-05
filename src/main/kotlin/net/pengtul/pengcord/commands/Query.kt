package net.pengtul.pengcord.commands

import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Query: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            sender.sendMessage("§cExpected 2 arguments. Usage: /query <b|m|w|f> <punishmentId>")
            return false
        }

        if (sender.hasPermission("pengcord.punishments.query")) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val whatToQuery = args[0]
                val punishmentToQuery = args[1].toLong()

                when (whatToQuery.lowercase()) {
                    "filter", "filteralerts", "f" -> {
                        Main.database.queryPlayerFilterAlertById(punishmentToQuery)?.let { filterAlert ->
                            sender.sendMessage("§a=========Filter Alert Query for ID ${filterAlert.filterAlertId}=========")
                            sender.sendMessage("§aFilterAlert ID: ${filterAlert.filterAlertId}")
                            sender.sendMessage("§aTarget UUID ${filterAlert.playerUUID}")
                            sender.sendMessage("§aTarget Discord UUID: ${filterAlert.discordUUID}")
                            sender.sendMessage("§aIssued On: ${filterAlert.issuedOn}")
                            sender.sendMessage("§aMessage Context: ${filterAlert.context}")
                            sender.sendMessage("§aTriggered Words: ${filterAlert.word}")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "warn", "warns", "w" -> {
                        Main.database.queryPlayerWarnById(punishmentToQuery)?.let { warn ->
                            sender.sendMessage("§a=========Warns Query for ID ${warn.warnId}=========")
                            sender.sendMessage("§aWarn ID: ${warn.warnId}")
                            sender.sendMessage("§aTarget UUID ${warn.playerUUID}")
                            sender.sendMessage("§aTarget Discord UUID: ${warn.discordUUID}")
                            sender.sendMessage("§aIssued By: ${warn.issuedBy}")
                            sender.sendMessage("§aIssued On: ${warn.issuedOn}")
                            sender.sendMessage("§aReason: ${warn.reason}")
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "mute", "mutes", "m" -> {
                        Main.database.queryPlayerMuteById(punishmentToQuery)?.let { mute ->
                            sender.sendMessage("§a=========Mutes Query for ID ${mute.muteId}=========")
                            sender.sendMessage("§aMute ID: ${mute.muteId}")
                            sender.sendMessage("§aTarget UUID ${mute.playerUUID}")
                            sender.sendMessage("§aTarget Discord UUID: ${mute.discordUUID}")
                            sender.sendMessage("§aIssued By: ${mute.issuedBy}")
                            sender.sendMessage("§aIssued On: ${mute.issuedOn}")
                            if (mute.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Mute")
                            } else {
                                sender.sendMessage("§aExpires: ${mute.expiresOn}")
                            }
                            sender.sendMessage("§aReason: ${mute.reason}")
                            if (mute.expiryState != ExpiryState.OnGoing) {
                                sender.sendMessage("§aMute Status: §2§l${mute.expiryState}")
                            } else {
                                sender.sendMessage("§aMute Status: §c§l${mute.expiryState}")
                            }
                            return@Runnable
                        }
                        sender.sendMessage("§cQuery failed: Invalid ID!")
                    }
                    "ban", "bans", "b" -> {
                        Main.database.queryPlayerBanById(punishmentToQuery)?.let { ban ->
                            sender.sendMessage("§a=========Bans Query for ID ${ban.banId}=========")
                            sender.sendMessage("§aBan ID: ${ban.banId}")
                            sender.sendMessage("§aTarget UUID ${ban.playerUUID}")
                            sender.sendMessage("§aTarget Discord UUID: ${ban.discordUUID}")
                            sender.sendMessage("§aIssued By: ${ban.issuedBy}")
                            sender.sendMessage("§aIssued On: ${ban.issuedOn}")
                            if (ban.isPermanent) {
                                sender.sendMessage("§aExpires: §c§lNever, Permanent Ban")
                            } else {
                                sender.sendMessage("§aExpires: ${ban.expiresOn}")
                            }
                            sender.sendMessage("§aReason: ${ban.reason}")
                            if (ban.expiryState != ExpiryState.OnGoing) {
                                sender.sendMessage("§aBan Status: §2§l${ban.expiryState}")
                            } else {
                                sender.sendMessage("§aBan Status: §c§l${ban.expiryState}")
                            }
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
            Main.discordBot.log("[pengcord]: User ${sender.name} ran `queryrecord`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `queryrecord`. Failed due to insufficient permissions.")
            return false
        }
    }
}