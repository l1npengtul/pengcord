package net.pengtul.pengcord.bot.commandhandler.default

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Git: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets the git repository for this discord bot/PaperMC plugin"
    override val commandName: String
        get() = "git"
    override val commandUsage: String
        get() = "git"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        sender.sendMessage("The git repository is available at https://gitlab.com/l1npengtul/pengcord. Please read the `LICENSE` and `README.md`")
        Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`")
    }
}