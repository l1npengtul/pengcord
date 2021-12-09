package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.util.Utils.Companion.banPlayer
import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Ban: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Ban Other Player"
    override val commandName: String
        get() = "ban"
    override val commandUsage: String
        get() = "ban <player> <reason> <time/perm>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!doesUserHavePermission(sender, "pengcord.punishment.ban")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                Main.discordBot.log(LogType.DSCComamndError, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }

        val toQuery = if (message.mentionedUsers.size == 1) {
            message.mentionedUsers[0].id.toString()
        } else {
            args[0]
        }
        val reason = args[1]
        val time = Utils.parseTimeFromString(args[2])
        if (time == null) {
            message.addReaction("❌").thenAccept {
                CommandHelper.deleteAfterSend("Invaid Time Frame! Timeframes are <amount><unit> such as 10d (10 days) or \"perm\" for permanent ban!", 5, message)
            }
            return
        }
        Utils.queryPlayerFromString(toQuery)?.let { player ->
            Main.discordBot.discordServer.getMemberById(player.discordUUID)?.ifPresent {
                if (!doesUserHavePermission(it, "pengcord.punishment.ban")) {
                    banPlayer(player, TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id), time, reason)
                    message.addReaction("✅").thenAccept {
                        Main.serverLogger.info("${sender.discriminatedName}(${sender.id}) sucessfully banned ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) from discord.")
                        Main.discordBot.log(LogType.DSCComamndRan, "${sender.discriminatedName}(${sender.id}) sucessfully banned ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) from discord.")
                    }
                } else {
                    message.addReaction("❌").thenAccept {
                        CommandHelper.deleteAfterSend("❌: Cannot ban another moderator!", 5, message)
                        Main.serverLogger.info("Attempt by ${sender.discriminatedName}(${sender.id}) to ban ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) but failed due to target being another moderator.")
                        Main.discordBot.log(LogType.DSCComamndError,"Attempt by ${sender.discriminatedName}(${sender.id}) to ban ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) but failed due to target being another moderator.")
                    }
                }
                return@ifPresent
            }
        }
        message.addReaction("❌").thenAccept {
            CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
        }
        return
    }
}