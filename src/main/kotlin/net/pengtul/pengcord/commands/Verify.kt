package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.javacord.api.entity.user.User
import java.lang.Exception
import java.lang.StringBuilder

class Verify: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && Main.ServerConfig.enableVerify){
            var argument = StringBuilder()
            for (arg in args){
                if (arg.startsWith("#")){
                    argument.append(arg)
                }
                else if (!argument.isBlank()){
                    argument.append(" $arg")
                }
                else {
                    argument.append(arg)
                }
            }


            lateinit var discUser: User
            Main.ServerLogger.info("here")
            Main.ServerConfig.serverBind?.let {
                Main.discordBot.discordApi.getServerById(Main.ServerConfig.serverBind).ifPresent { server ->
                    Main.ServerLogger.info(argument.toString())
                    server.getMemberByDiscriminatedNameIgnoreCase(argument.toString()).ifPresent { user ->
                        discUser = user
                        Main.ServerLogger.info(discUser.idAsString)
                        Main.ServerConfig.commandChannel?.let { _ ->
                            try{
                                Main.discordBot.discordApi.getServerById(Main.ServerConfig.serverBind).get().getChannelById(Main.ServerConfig.commandChannel).ifPresent {
                                    if (!Main.playersToVerify.containsKey(discUser.idAsString) && !Main.playersToVerify.containsValue(sender.uniqueId.toString())){
                                        Main.playersToVerify[discUser.idAsString] = sender.uniqueId.toString()
                                        sender.sendMessage("§aPlease type `${Main.ServerConfig.botPrefix}verify ${sender.name}` in #${it.name} to finish your verification!")
                                    }
                                    else {
                                        sender.sendMessage("§cERROR: You already exist! Type `${Main.ServerConfig.botPrefix}verify ${sender.name}` in #${it.name} to finish your verification!")
                                    }
                                }
                            }
                            catch (e: Exception){
                                sender.sendMessage("§aAn exception occurred in your request. ERR: $e")
                                Main.ServerLogger.severe("An exception occurred in your request verifying ${user.discriminatedName}. ERR: $e")
                                Main.ServerLogger.severe(e.stackTrace.toString())
                                Main.ServerLogger.severe("Did you set command channel by using `${Main.ServerConfig.botPrefix}bind command` in the proper channel?")
                            }
                        }
                    }
                }
            }
            return true
        }
        return false
    }
}