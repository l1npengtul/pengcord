package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils.Companion.getUptime
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/*   This is the class for getting server misc info
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

class Info: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets server info on TPS, RAM usage, and online players."
    override val commandName: String
        get() = "info"
    override val commandUsage: String
        get() = "info"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        val playerList = Bukkit.getOnlinePlayers()
        val currentUsedRAM : Long = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
        val maxRAM : Long = Runtime.getRuntime().totalMemory() /  1048576L
        val embed = EmbedBuilder()
                .setAuthor("Server Info")
                .setTitle("Server TPS, Server Playerlist, Server RAM usage, Server Uptime")
                .addInlineField("Server TPS 1M, 5M, 15M:", Bukkit.getTPS().map { (it * 100).roundToInt() / 100.0 }.joinToString())
                .addInlineField("Server RAM Usage:", "$currentUsedRAM MiB / $maxRAM MiB (${((currentUsedRAM.toDouble() / maxRAM.toDouble()) * 100).roundToLong()}%)")
                .addInlineField("Server Uptime (HH:MM:SS): ", getUptime())
                .addField("Users Online (${playerList.size}/${Bukkit.getMaxPlayers()}):",
                    playerList.joinToString { it.name })

        if (!Main.serverConfig.discordServerLink.isNullOrBlank()) {
            embed.addField("Server IP:", Main.serverConfig.discordServerLink)
        }

        message.reply(embed).thenAccept {
            
            Main.discordBot.logEmbed(embed)
        }
    }
}