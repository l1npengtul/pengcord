package net.pengtul.pengcord.bot.commandhandler

import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

class JCDiscordCommandEvent(pre: String, commandToTrack: String, execute: JCDiscordCommandExecutor, binds: List<TextChannel>) : MessageCreateListener {
    val prefix = pre
    val cmd = commandToTrack
    val exec = execute
    val boundChannels = binds
    override fun onMessageCreate(event: MessageCreateEvent?) {
        event?.let { msgevent ->
            if (msgevent.messageContent.startsWith("$prefix$cmd") && boundChannels.contains(msgevent.channel)){
                msgevent.messageAuthor.asUser().ifPresent { auth ->
                    val processedArgs: MutableList<String> = ArrayList()
                    for(arg in msgevent.messageContent.removePrefix("$prefix$cmd").split(" ")){
                        if (arg.isNotBlank() && arg.isNotEmpty()){
                            processedArgs.add(arg)
                        }
                    }

                    Main.ServerLogger.info(msgevent.messageContent.removePrefix("$prefix$cmd").split(" ").toString())
                    exec.executeCommand(
                            msg = msgevent.messageContent,
                            sender = auth,
                            message = msgevent.message,
                            args = processedArgs.toList()
                    )
                }
            }
        }
    }
}