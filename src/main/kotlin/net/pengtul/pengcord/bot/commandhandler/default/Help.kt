package net.pengtul.pengcord.bot.commandhandler.default

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.user.User

class Help(helpmsg: MessageBuilder) : JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Help Command, gets a list of all commands"
    override val commandName: String
        get() = "help"
    override val commandUsage: String
        get() = "help"
    private var content = helpmsg
    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        Bukkit.getPluginManager().getPlugin("pengcord")?.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                content.send(sender).join()
                Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran `$commandName`.")
            })
        }
    }
}