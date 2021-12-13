package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.util.Utils.Companion.warnPlayer
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Warn: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "warn"
    override val commandDescription: String
        get() = "Warns a player, and logs it. If they are in the server they are also kicked."
    override val commandUsage: String
        get() = "warn <player> <reason>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!doesUserHavePermission(sender, "pengcord.punishments.warn")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }
        queryPlayerFromString(args[0])?.let { player ->
            if (doesUserHavePermission(player.playerUUID, "pengcord.punishments.warn")) {
                message.addReaction("\uD83D\uDEAB").thenAccept {
                    
                    Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                    CommandHelper.deleteAfterSend("\uD83D\uDEAB: Cannot do that to another moderator!", 5, message)
                }
                return
            }

            warnPlayer(player.playerUUID, TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id), args[1])
            message.addReaction("✅").thenAccept {
                Main.serverLogger.info("${sender.discriminatedName}(${sender.id}) sucessfully warned ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}) from discord.")
                
            }
        }
    }
}