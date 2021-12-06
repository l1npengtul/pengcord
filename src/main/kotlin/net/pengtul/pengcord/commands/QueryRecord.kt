package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
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
                                "§r§5${filterAlert.filterAlertId}§r§a(${filterAlert.word})"
                            }
                            sender.sendMessage("§a=========Filter Alert Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a${alerts.joinToString()}")

                            val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                                "§r§5${warn.warnId}§r§a(${warn.issuedBy})"
                            }
                            sender.sendMessage("§a=========Warns Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a${warns.joinToString()}")

                            val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                            val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            sender.sendMessage("§a=========Mutes Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a====EXPIRED====")
                            sender.sendMessage("§a${expiredMutes.joinToString()}")
                            sender.sendMessage("§a====PARDONED====")
                            sender.sendMessage("§a${pardonedMutes.joinToString()}")
                            sender.sendMessage("§a====ONGOING====")
                            sender.sendMessage("§a${ongoingMutes.joinToString()}")

                            val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                            val expiredBans = bans.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            val pardonedBans = bans.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            val ongoingBans = bans.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            sender.sendMessage("§a=========Bans Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a====EXPIRED====")
                            sender.sendMessage("§a${expiredBans.joinToString()}")
                            sender.sendMessage("§a====PARDONED====")
                            sender.sendMessage("§a${pardonedBans.joinToString()}")
                            sender.sendMessage("§a====ONGOING====")
                            sender.sendMessage("§a${ongoingBans.joinToString()}")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran ${this.javaClass.name}.")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran ${this.javaClass.name}.")

                        }
                    }
                    "filter", "filteralerts", "f" -> {
                        playerToQuery?.playerUUID?.let {
                            val alerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(it).map { filterAlert ->
                                "§r§5${filterAlert.filterAlertId}§r§a(${filterAlert.word})"
                            }
                            sender.sendMessage("§a=========Filter Alert Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a${alerts.joinToString()}")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran ${this.javaClass.name}.")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran ${this.javaClass.name}.")
                        }
                    }
                    "warn", "warns", "w" -> {
                        playerToQuery?.playerUUID?.let {
                            val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                                "§r§5${warn.warnId}§r§a(${warn.issuedBy})"
                            }
                            sender.sendMessage("§a=========Warns Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a${warns.joinToString()}")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    "mute", "mutes", "m" -> {
                        playerToQuery?.playerUUID?.let {
                            val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                            val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                                "§r§5${mute.muteId}§r§a(${mute.issuedBy})"
                            }
                            sender.sendMessage("§a=========Mutes Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a====EXPIRED====")
                            sender.sendMessage("§a${expiredMutes.joinToString()}")
                            sender.sendMessage("§a====PARDONED====")
                            sender.sendMessage("§a${pardonedMutes.joinToString()}")
                            sender.sendMessage("§a====ONGOING====")
                            sender.sendMessage("§a${ongoingMutes.joinToString()}")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    "ban", "bans", "b" -> {
                        playerToQuery?.playerUUID?.let {
                            val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                            val expiredBans = bans.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            val pardonedBans = bans.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            val ongoingBans = bans.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { ban ->
                                "§r§5${ban.banId}§r§a(${ban.issuedBy})"
                            }
                            sender.sendMessage("§a=========Bans Query for player ${playerToQuery.currentUsername}=========")
                            sender.sendMessage("§a====EXPIRED====")
                            sender.sendMessage("§a${expiredBans.joinToString()}")
                            sender.sendMessage("§a====PARDONED====")
                            sender.sendMessage("§a${pardonedBans.joinToString()}")
                            sender.sendMessage("§a====ONGOING====")
                            sender.sendMessage("§a${ongoingBans.joinToString()}")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                    else -> {
                        sender.sendMessage("§cInvalid Query Type!")
                    }
                }
            })
            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name()} ran `queryrecord`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `queryrecord`. Failed due to insufficient permissions.")
            return false
        }
    }
}