package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.Utils
import net.pengtul.pengcord.Utils.Companion.shutdown
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class StopServer: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "stopserver"
    override val commandDescription: String
        get() = "Stop the server."
    override val commandUsage: String
        get() = "stopserver <seconds? = 20>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!Utils.doesUserHavePermission(sender, "pengcord.command.shutdown")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                Main.discordBot.log(LogType.DSCComamndError, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }
        val shutdownTimer = (args[0].toIntOrNull() ?: 20).toLong()
        shutdown(shutdownTimer)
        Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
        Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")


    }
}