package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User
import sun.rmi.runtime.Log
import java.lang.Exception

class Command {
    fun bindCommand(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ")
        try {
            if (!Main.ServerConfig.adminList.isNullOrEmpty()){
                var userRoles = ""
                message.server.get().getHighestRole(sender).ifPresent { role ->
                    userRoles = role.idAsString
                }
                if(Main.ServerConfig.adminList!!.contains(userRoles)){
                    when (mArray[1]) {
                        "sync" -> {
                            Main.ServerConfig.syncChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `sync` channel.",8, message)
                        }
                        "command" -> {
                            Main.ServerConfig.commandChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `command` channel.",8, message)
                        }
                        "admin" -> {
                            Main.ServerConfig.adminChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `admin` channel.",8, message)
                        }
                        else -> {
                            CommandHelper.deleteAfterSend("Usage: ${Main.ServerConfig.botPrefix}bind <sync/command/admin>",5, message)
                        }
                    }
                }
                else if (message.server.get().isAdmin(sender)) {
                    when (mArray[1]) {
                        "sync" -> {
                            Main.ServerConfig.syncChannel = message.channel.idAsString
                            message.server.ifPresent{server -> Main.ServerConfig.serverBind = server.idAsString}
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `sync` channel.", 8, message)
                        }
                        "command" -> {
                            Main.ServerConfig.commandChannel = message.channel.idAsString
                            message.server.ifPresent{server -> Main.ServerConfig.serverBind = server.idAsString}
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `command` channel.", 8, message)
                        }
                        "admin" -> {
                            Main.ServerConfig.adminChannel = message.channel.idAsString
                            message.server.ifPresent{server -> Main.ServerConfig.serverBind = server.idAsString}
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `admin` channel.", 8, message)
                        }
                        else -> {
                            CommandHelper.deleteAfterSend("Usage: ${Main.ServerConfig.botPrefix}bind <sync/command/admin>", 5, message)
                        }
                    }
                }
                else {
                    CommandHelper.deleteAfterSend("Inadequate Permissions to execute this command.", 5, message)
                }
            }
        }
        catch (e: Exception){
            CommandHelper.deleteAfterSend("An Exception occurred in your request. ```$e```",8, message)
        }
        //CommandHelper.deleteAfterSend("",8, message)
    }

    fun verifyCommand(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ")
        Bukkit.getServer().getPlayer(mArray[1])?.let{
            if (Main.playersToVerify.containsValue(it.uniqueId.toString()) && Main.playersToVerify.containsKey(sender.idAsString)){
                Main.ServerConfig.usersList?.let {
                    Bukkit.getServer().getPlayer(mArray[1])?.let { player ->
                        if (!it.containsValue(player.uniqueId.toString()) && !it.containsKey(sender.idAsString)){
                            it.put(sender.idAsString, player.uniqueId.toString())
                            Main.ServerLogger.info("[pengcord]: Registered User with discord id ${sender.idAsString} with UUID ${player.uniqueId}")
                            CommandHelper.deleteAfterSend("Successfully Verified!",8, message)
                            if(player.isInvulnerable){
                                player.isInvulnerable = false
                            }
                            return
                        }
                        else {
                            CommandHelper.deleteAfterSend("You already exist! No need to verify. Please ask for help if this is a mistake.",8, message)
                            return
                        }
                    }
                    CommandHelper.deleteAfterSend("You need to provide a valid username and be logged into the server.", 8, message)
                }
            }
            else {
                CommandHelper.deleteAfterSend("Who are you? Make sure you already did `/verify <discord tag>` in the minecraft server first!",8, message)
            }
        }
        CommandHelper.deleteAfterSend("Couldn't find you ${mArray[1]}! Make sure you are logged into the minecraft server!",5, message)
    }

    fun ban(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
    }

    fun stop(msg: String, sender: User, message: Message) {

    }
}

