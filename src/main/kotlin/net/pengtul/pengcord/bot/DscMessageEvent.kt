package net.pengtul.pengcord.bot

import net.pengtul.pengcord.bot.botcmd.Command
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.lang.Exception

public class DscMessageEvent: MessageCreateListener {
    private var command: Command = Command();
    public override fun onMessageCreate(event: MessageCreateEvent?) {
        val msg: Message? = event?.message;
        msg?.let {
            if (msg.content.startsWith(Main.ServerConfig.BotPrefix.toString())){
                Main.ServerLogger.info("Inc CMD");
                var messageAsList: List<String> = msg.content.split(" ");
                when (messageAsList[0]){
                    "${Main.ServerConfig.BotPrefix}bind" -> {
                        if(msg.author.asUser().isPresent){
                            command.bindCommand(msg.content.toString(), msg.author.asUser().get(), msg);
                        }
                    }
                }
            }
            else {
                if (!(msg.author.isWebhook || msg.author.isBotUser || msg.author.isYourself)){
                    if (msg.channel.idAsString.equals(Main.ServerConfig.syncChannel.toString())){
                        //Main.ServerLogger.info("§7[DSC]${msg.author.displayName}: ${msg.content}");
                        try{
                            Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: ${msg.content}");
                        }
                        catch (e: Exception){

                        }
                    }
                }
            }
        }
    }
}