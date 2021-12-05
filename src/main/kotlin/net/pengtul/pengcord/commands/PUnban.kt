package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils.Companion.banPardon
import net.pengtul.pengcord.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.data.schema.Player
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PUnban: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size <= 2) {
            sender.sendMessage("§cExpected 2 arguments. Usage: /punban <ban Id/add> <player?>")
            return false
        }

        if (sender.hasPermission("pengcord.punishments.ban")) {
            // If all
            if (args[0].lowercase() == "all") {
                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                    val player = queryPlayerFromString(args[1])
                    if (player == null) {
                        sender.sendMessage("§cInvalid player!")
                        return@Runnable
                    }
                    player.let {
                        Main.database.queryPlayerBansByPlayerMinecraft(player.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }.forEach { ban ->
                            banPardon(ban, pardoned = true)
                            sender.sendMessage("§aLifted ban ${ban.banId} for player ${it.currentUsername}(UUID: ${it.playerUUID}/Discord: ${it.discordUUID})!")
                        }
                    }
                })
            } else {
                val banId = args[0].toLong()
                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                    Main.database.queryPlayerBanById(banId)?.let { ban ->
                        banPardon(ban, pardoned = true)
                        sender.sendMessage("§aLifted ban ${ban.banId} for player UUID: ${ban.playerUUID}/Discord: ${ban.discordUUID}!")
                    }
                })
            }
            return true
        } else {
            Main.discordBot.log("[pengcord]: User ${sender.name} ran `punban`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name} ran `punban`. Failed due to insufficient permissions.")
            return false
        }
    }
}