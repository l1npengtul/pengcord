package net.pengtul.pengcord.bot.commandhandler

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

public interface JCDiscordCommandExecutor {
    val commandName: String;
    val commandDescription: String;
    val commandUsage: String;
    fun executeCommand(msg: String, sender: User, message: Message, args: List<String>)
}