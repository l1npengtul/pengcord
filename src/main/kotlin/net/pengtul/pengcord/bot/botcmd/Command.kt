package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User
import java.lang.Exception

public class Command(){
    public fun bindCommand(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ");
        try {
            if(message.server.get().isAdmin(sender)){
                when (mArray[1]) {
                    "sync" -> {
                        Main.ServerConfig.syncChannel = message.channel.idAsString;
                        Main.ServerConfig.ServerBind = message.server.ifPresent { server -> server }.toString();
                        message.channel.sendMessage("Sucessfully set this channel as the `sync` channel.");

                    }
                    "command" -> {
                        Main.ServerConfig.commandChannel = message.channel.idAsString;
                        Main.ServerConfig.ServerBind = message.server.ifPresent { server -> server }.toString();
                        message.channel.sendMessage("Sucessfully set this channel as the `command` channel.");
                    }
                    "admin" -> {
                        Main.ServerConfig.adminChannel = message.channel.idAsString;
                        Main.ServerConfig.ServerBind = message.server.ifPresent { server -> server }.toString();
                        message.channel.sendMessage("Sucessfully set this channel as the `admin` channel.");
                    }
                    else -> {
                        message.channel.sendMessage("Usage: <\"sync\"/\"command\"/\"admin\">");
                    }
                }
            }
        }
        catch (e: Exception){
            message.channel.sendMessage("An Exception occured in your request. ```${e.toString()}```");
        }
        message.channel.sendMessage("Usage: <\"sync\"/\"command\"/\"admin\">");
    }
}