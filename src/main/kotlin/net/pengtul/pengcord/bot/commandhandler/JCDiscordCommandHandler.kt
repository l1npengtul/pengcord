package net.pengtul.pengcord.bot.commandhandler

import net.pengtul.pengcord.bot.commandhandler.default.Git
import net.pengtul.pengcord.bot.commandhandler.default.Help
import net.pengtul.pengcord.main.Main
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageBuilder

class JCDiscordCommandHandler(api: DiscordApi, prefix: String, autoHelp: Boolean, boundChannels: List<String>) {
    private var allowedCommandChannels: MutableList<TextChannel> = ArrayList()
    private var commandPrefix = prefix
    private val discordApi: DiscordApi = api
    private var commandMap: HashMap<JCDiscordCommandExecutor, JCDiscordCommandEvent> = HashMap()
    val genHelp = autoHelp
    private var helpMessage = MessageBuilder()

    init {
        Main.serverLogger.info("Added Command Listener")

        for (channel in boundChannels){
            if (discordApi.getTextChannelById(channel).isPresent){
                allowedCommandChannels.add(discordApi.getTextChannelById(channel).get())
            }
            else {
                Main.serverLogger.warning("Channel $channel does not exist!")
            }
        }
        helpMessage.append("```diff")
        helpMessage.appendNewLine()
        addCommand(Git())
    }

    fun addCommand(command: JCDiscordCommandExecutor){
        Main.serverConfig.botCommandChannel?.let { channelId ->
            Main.discordBot.discordApi.getTextChannelById(channelId)?.ifPresent { textChannel ->
                if (!commandMap.containsKey(command)){
                    val commandListener = JCDiscordCommandEvent(commandPrefix, command.commandName, command, allowedCommandChannels)
                    commandMap[command] = commandListener
                    textChannel.addMessageCreateListener(commandListener)
                    Main.serverLogger.info("Added Command ${command.commandName}")
                }
            }
        }
    }

    fun generateHelp(){
        for (cmd in commandMap.keys){
            helpMessage.appendNewLine()
            helpMessage.append("+ Command ${Main.serverConfig.botPrefix}${cmd.commandName}")
            helpMessage.appendNewLine()
            helpMessage.append("- *Usage: ${Main.serverConfig.botPrefix}${cmd.commandUsage}")
            helpMessage.appendNewLine()
            helpMessage.append("- *Description: ${cmd.commandDescription}")
        }
        helpMessage.appendNewLine()
        helpMessage.append("```")
        addCommand(Help(helpMessage))
    }
}