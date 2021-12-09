package net.pengtul.pengcord.commands

import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Warn: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.count() != 2) {
            sender.sendMessage("§cExpected 3 arguments. Usage: /warn <player> <reason>")
            return false
        }
        if (sender.hasPermission("pengcord.punishments.warn")) {
            val playerToMute = args[0]
            val reason = args[1]

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Utils.queryPlayerFromString(playerToMute)?.let { player ->
                    Main.database.playerGetByCurrentName(sender.name)?.let { senderPlayer ->
                        Utils.warnPlayer(
                            player.playerUUID,
                            TypeOfUniqueID.MinecraftTypeOfUniqueID(senderPlayer.playerUUID),
                            reason,
                        )
                        return@Runnable
                    }
                    // else
                    Utils.warnPlayer(player.playerUUID, TypeOfUniqueID.Unknown(sender.name), reason)

                    Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran `warn` with ${args[0]}.")
                    Main.serverLogger.info("User ${sender.name} ran `warn` ${args[0]}.")
                }
            })

            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name} ran `warn`. Failed due to insufficient permissions.")
            Main.serverLogger.info("User ${sender.name} ran `warn`. Failed due to insufficient permissions.")
            return false
        }
    }
}