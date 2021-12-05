package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Me: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets Info on yourself (Only if you are verified on server)"
    override val commandName: String
        get() = "info"
    override val commandUsage: String
        get() = "info"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {

    }
}