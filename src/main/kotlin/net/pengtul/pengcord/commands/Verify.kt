package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.javacord.api.entity.user.User
import java.lang.Exception
import java.lang.StringBuilder

/*
*    The Verify command, prepares the user to be verified on discord
*    Copyright (C) 2020  Lewis Rho
*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */



class Verify: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && Main.serverConfig.enableVerify && sender.hasPermission("pengcord.verify.command")){
            val argument = StringBuilder()
            for (arg in args){
                if (arg.startsWith("#")){
                    argument.append(arg)
                }
                else if (argument.isNotBlank()){
                    argument.append(" $arg")
                }
                else {
                    argument.append(arg)
                }
            }

            Main.serverLogger.info("Got valid verification request... Attempting verify MC Player ${sender.uniqueId} with discord tag $argument")
            Main.serverConfig.botServer?.let { serverId ->
                Main.discordBot.discordApi.getServerById(serverId).ifPresent { discordServer ->
                    discordServer.getMemberByDiscriminatedNameIgnoreCase(argument.toString()).ifPresent { serverMember ->
                        Main.serverLogger.info("User ${serverMember.discriminatedName} found with user ID ${serverMember.id}")
                        Main.serverConfig.botCommandChannel?.let { cmdChannnelId ->
                            discordServer.getChannelById(cmdChannnelId).ifPresent { commandChannel ->
                                if (!Main.playersAwaitingVerification.containsKey(serverMember.id) && !Main.playersAwaitingVerification.containsValue(sender.uniqueId)) {
                                    Main.playersAwaitingVerification[serverMember.id] = sender.uniqueId
                                    sender.sendMessage("§aPlease type `${Main.serverConfig.botPrefix}verify ${sender.name}` in ${discordServer.name}/#${commandChannel.name} to finish your verification!")
                                    Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `verify` with arguments ${serverMember.idAsString} (${serverMember.discriminatedName}). Successful, awaiting verification.")
                                }
                                else {
                                    Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `verify` with arguments ${serverMember.idAsString} (${serverMember.discriminatedName}). Already exists.")
                                    sender.sendMessage("§cERROR: You already exist! Type `${Main.serverConfig.botPrefix}verify ${sender.name}` in ${discordServer.name}/#${commandChannel.name} to finish your verification!")
                                }
                            }
                        }
                    }
                }
            }
            return true
         } else {
            Main.discordBot.log("[pengcord]: [MC]: User ${sender.name} ran `verify` with arguments. Failed due to error.")
            return false
        }
    }
}