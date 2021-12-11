package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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
            Main.serverLogger.info("Got valid verification request... Attempting verify MC Player ${sender.uniqueId} with discord tag ${args[0]}")
            Main.discordBot.discordServer.getMemberByDiscriminatedName(args[0]).ifPresentOrElse ({ serverMember ->
                Main.serverLogger.info("User ${serverMember.discriminatedName} found with user ID ${serverMember.id}")
                Main.serverConfig.botCommandChannel.forEach { cmdChannnelId ->
                    Main.discordBot.discordServer.getChannelById(cmdChannnelId).ifPresent { commandChannel ->
                        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                            if (!Main.playersAwaitingVerification.containsValue(sender.uniqueId) && !Main.database.playerIsVerified(
                                    TypeOfUniqueID.MinecraftTypeOfUniqueID(sender.uniqueId)
                                )
                            ) {
                                val secretKey = Main.generateUniqueKey()
                                Main.playersAwaitingVerification[secretKey] = sender.uniqueId

                                val message = Component.text()
                                    .content("§aPlease type `${Main.serverConfig.botPrefix}verify ${sender.name} $secretKey` in `${Main.discordBot.discordServer.name}/#${commandChannel.name}` to finish your verification! Note that if you made a mistake, log out and log back in.\n§r§c§lDO NOT SHARE THIS.")
                                    .hoverEvent(
                                        HoverEvent.hoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Component.text("Click me to copy command!", NamedTextColor.GREEN)
                                        )
                                    )
                                    .clickEvent(
                                        ClickEvent.clickEvent(
                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                            "${Main.serverConfig.botPrefix}verify ${sender.name} $secretKey"
                                        )
                                    )
                                    .build()

                                sender.sendMessage(message)

                                Main.discordBot.log(
                                    LogType.MCComamndRan,
                                    "User ${sender.name} ran `verify` with arguments ${serverMember.idAsString} (${serverMember.discriminatedName}) and secret key ${secretKey}. Successful, awaiting verification."
                                )
                                return@Runnable
                            } else {
                                Main.discordBot.log(
                                    LogType.MCComamndError,
                                    "User ${sender.name} ran `verify` with arguments ${serverMember.idAsString} (${serverMember.discriminatedName}). Already exists."
                                )
                                sender.sendMessage("§cERROR: You already exist! If you forgot your key, relog to generate a new one!")
                            }
                        })
                    }
                }
            }, {
                sender.sendMessage("§cERROR: User ${args[0]} not found!")
            })
            return true
         } else {
            Main.discordBot.log(LogType.MCComamndError, "User ${sender.name} ran `verify` with arguments. Failed due to error.")
            Main.serverLogger.info("User ${sender.name} ran `verify` with arguments. Failed due to error.")
            return false
        }
    }
}