package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

/*   This is the class for verifying people from discord
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

class Verify: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Verifies a user after they have ran the `/verify <discord tag>` command in Minecraft first."
    override val commandName: String
        get() = "verify"
    override val commandUsage: String
        get() = "verify <Minecraft Username>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        Bukkit.getServer().getPlayer(args[0])?.let {
            if (Main.playersToVerify.containsValue(it.uniqueId.toString()) && Main.playersToVerify.containsKey(sender.idAsString)) {
                Main.ServerConfig.usersList?.let { hash ->
                    Bukkit.getServer().getPlayer(args[0])?.let { player ->
                        if (!(hash.containsValue(player.uniqueId.toString()) && hash.containsKey(sender.idAsString))) {
                            hash[sender.idAsString] = player.uniqueId.toString()
                            Main.ServerLogger.info("[pengcord]: Registered User with discord id ${sender.idAsString} with UUID ${player.uniqueId}")
                            Main.discordBot.log("[pengcord]: Registered User with discord id ${sender.idAsString} (username: ${sender.discriminatedName}) with UUID ${player.uniqueId} (username: ${player.displayName})")
                            CommandHelper.deleteAfterSend("Successfully Verified!", 8, message)
                            player.sendTitle("§aYou have been verified!", "§aGLHF!", 10, 70, 20)
                            player.playSound(Location(player.world, player.location.x, player.location.y, player.location.z), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F)
                            if (player.isInvulnerable) {
                                player.isInvulnerable = false
                            }
                            Main.playersToVerify.remove(sender.idAsString)
                            return
                        } else {
                            Main.discordBot.log("[pengcord]: User with discord id ${sender.idAsString} (username: ${sender.discriminatedName}) with UUID ${player.uniqueId} (username: ${player.displayName}) already exists")
                            CommandHelper.deleteAfterSend("You already exist! No need to verify. Please ask for help if this is a mistake.", 8, message)
                            return
                        }
                    }
                    Main.discordBot.log("[pengcord]: User with discord id ${sender.idAsString} (username: ${sender.discriminatedName}) failed to be verified")
                    CommandHelper.deleteAfterSend("You need to provide a valid username and be logged into the server.", 8, message)
                }
            } else {
                Main.discordBot.log("[pengcord]: User with discord id ${sender.idAsString} (username: ${sender.discriminatedName}) failed to be verified")
                CommandHelper.deleteAfterSend("Who are you? Make sure you already did `/verify <discord tag>` in the minecraft server first!", 8, message)
            }
        }
        Main.discordBot.log("[pengcord]: User with discord id ${sender.idAsString} (username: ${sender.discriminatedName}) failed to be verified")
        CommandHelper.deleteAfterSend("Couldn't find you ${args[0]}! Make sure you are logged into the minecraft server!", 5, message)
    }
}