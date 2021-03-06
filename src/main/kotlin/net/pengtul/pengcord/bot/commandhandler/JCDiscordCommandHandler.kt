package net.pengtul.pengcord.bot.commandhandler

import net.pengtul.pengcord.bot.commandhandler.default.Git
import net.pengtul.pengcord.bot.commandhandler.default.Help
import net.pengtul.pengcord.bot.commandhandler.exceptions.CommandAlreadyExistsException
import net.pengtul.pengcord.main.Main
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.listener.message.MessageCreateListener

public class JCDiscordCommandHandler(api: DiscordApi, prefix: String, autoHelp: Boolean, boundChannels: List<String>) {
    private var allowedCommandChannels: MutableList<TextChannel> = ArrayList()
    var commandPrefix = prefix
    private val discordApi: DiscordApi = api
    var commandMap: HashMap<JCDiscordCommandExecutor, JCDiscordCommandEvent> = HashMap()
    val genHelp = autoHelp
    var helpMessage = MessageBuilder()

    init {
        Main.ServerLogger.info("Added Command Listener")

        for (channel in boundChannels){
            if (discordApi.getTextChannelById(channel).isPresent){
                allowedCommandChannels.add(discordApi.getTextChannelById(channel).get())
            }
            else {
                Main.ServerLogger.warning("Channel $channel does not exist!")
            }
        }
        helpMessage.append("```diff")
        helpMessage.appendNewLine()
        addCommand(Git())
    }

    public fun addCommand(command: JCDiscordCommandExecutor){
        if (!commandMap.containsKey(command)){
            val commandListener = JCDiscordCommandEvent(commandPrefix, command.commandName, command, allowedCommandChannels)
            commandMap[command] = commandListener
            discordApi.addMessageCreateListener(commandListener)
            Main.ServerLogger.info("Added Command ${command.commandName}")
        }
    }

    public fun generateHelp(){
        for (cmd in commandMap.keys){
            helpMessage.appendNewLine()
            helpMessage.append("+ Command ${Main.ServerConfig.botPrefix}${cmd.commandName}")
            helpMessage.appendNewLine()
            helpMessage.append("- *Usage: ${Main.ServerConfig.botPrefix}${cmd.commandUsage}")
            helpMessage.appendNewLine()
            helpMessage.append("- *Description: ${cmd.commandDescription}")
        }
        helpMessage.appendNewLine()
        helpMessage.append("```")
        addCommand(Help(helpMessage))
    }
}