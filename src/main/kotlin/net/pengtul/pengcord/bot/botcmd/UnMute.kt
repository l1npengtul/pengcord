package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.util.Utils.Companion.pardonMute
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class UnMute: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "unmute"
    override val commandDescription: String
        get() = "Unmutes/Pardons a player from a mute punishment. Pardon either by mute Id or use all <player> to pardon all mutes from a player."
    override val commandUsage: String
        get() = "unmute <mute Id/all> <player?>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!Utils.doesUserHavePermission(sender, "pengcord.punishment.mute")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                Main.discordBot.log(LogType.DSCComamndError, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }
        if (args[0].lowercase() == "all") {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = Utils.queryPlayerFromString(args[1])
                if (player == null) {
                    message.addReaction("❌").thenAccept {
                        CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
                    }
                    return@Runnable
                }
                player.let {
                    Main.database.queryPlayerMutesByPlayerMinecraft(player.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }.forEach { mutes ->
                        pardonMute(mutes, pardoned = true)
                        message.addReaction("✅").thenAccept {
                            CommandHelper.deleteAfterSend("§aLifted mute ${mutes.muteId} for player UUID: ${mutes.playerUUID}/Discord: ${mutes.discordUUID}!", 5, message)
                            Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                            Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                }
            })
        } else {
            val muteId = args[0].toLong()
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.queryPlayerMuteById(muteId)?.let { mute ->
                    pardonMute(mute, pardoned = true)
                    message.addReaction("✅").thenAccept {
                        CommandHelper.deleteAfterSend("§aLifted mute ${mute.muteId} for player UUID: ${mute.playerUUID}/Discord: ${mute.discordUUID}!", 5, message)
                        Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return@Runnable
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
                }
            })
        }
    }
}