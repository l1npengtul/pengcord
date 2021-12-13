package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.util.Utils.Companion.mutePlayer
import net.pengtul.pengcord.util.Utils.Companion.parseTimeFromString
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Mute: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Mute Other Player"
    override val commandName: String
        get() = "mute"
    override val commandUsage: String
        get() = "mute <player> <reason> <time/perm>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!doesUserHavePermission(sender, "pengcord.punishment.mute")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }

        if (args.size != 3) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: Improper arguments!", 5, message)
            }
            return
        }

        val toQuery = if (message.mentionedUsers.size == 1) {
            message.mentionedUsers[0].id.toString()
        } else {
            args[0]
        }


        val reason = args[1]
        val time = parseTimeFromString(args[2])
        if (time == null) {
            message.addReaction("❌").thenAccept {
                CommandHelper.deleteAfterSend("Invaid Time Frame! Timeframes are <amount><unit> such as 10d (10 days) or \"perm\" for permanent mute!", 5, message)
            }
            return
        }
        queryPlayerFromString(toQuery)?.let { player ->
            Main.discordBot.discordServer.getMemberById(player.discordUUID)?.ifPresent {
                if (!doesUserHavePermission(it, "pengcord.punishment.mute")) {
                    mutePlayer(player, TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id), time, reason)
                    message.addReaction("✅").thenAccept {
                        Main.serverLogger.info("${sender.discriminatedName}(${sender.id}) sucessfully muted ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) from discord.")
                        
                    }
                } else {
                    message.addReaction("❌").thenAccept {
                        CommandHelper.deleteAfterSend("❌: Cannot mute another moderator!", 5, message)
                        Main.serverLogger.info("Attempt by ${sender.discriminatedName}(${sender.id}) to mute ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) but failed due to target being another moderator.")
                        
                    }
                }
                return@ifPresent
            }
        }
        message.addReaction("❌").thenAccept {
            CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
        }

    }
}