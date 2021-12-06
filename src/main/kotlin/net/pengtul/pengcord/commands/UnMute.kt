package net.pengtul.pengcord.commands

import net.pengtul.pengcord.Utils
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class UnMute: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size <= 2) {
            sender.sendMessage("§cExpected 2 arguments. Usage: /unmute <ban Id/add> <player?>")
            return false
        }

        if (sender.hasPermission("pengcord.punishments.mute")) {
            // If all
            if (args[0].lowercase() == "all") {
                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                    val player = Utils.queryPlayerFromString(args[1])
                    if (player == null) {
                        sender.sendMessage("§cInvalid player!")

                        Main.discordBot.log(LogType.MCComamndError, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid args.")
                        Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid args.")
                        return@Runnable
                    }
                    player.let {
                        Main.database.queryPlayerMutesByPlayerMinecraft(player.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }.forEach { mute ->
                            Utils.pardonMute(mute, pardoned = true)
                            sender.sendMessage("§aLifted mute ${mute.muteId} for player ${it.currentUsername}(UUID: ${it.playerUUID}/Discord: ${it.discordUUID})!")
                            Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                })
            } else {
                val muteId = args[0].toLong()
                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                    Main.database.queryPlayerMuteById(muteId)?.let { mute ->
                        Utils.pardonMute(mute, pardoned = true)
                        sender.sendMessage("§aLifted mute ${mute.muteId} for player UUID: ${mute.playerUUID}/Discord: ${mute.discordUUID}!")
                        Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                })
            }
            return true
        } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name()} ran `unmute`. Failed due to insufficient permissions.")
            Main.serverLogger.info("[pengcord]: User ${sender.name()} ran `unmute`. Failed due to insufficient permissions.")
            return false
        }
    }
}