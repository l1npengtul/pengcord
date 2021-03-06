package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.lang.StringBuilder

/*   This is the class for getting server misc info
*    Copyright (C) 2020-2021  l1npengtul Rho
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
        val playerList = StringBuilder()
        var playerOnline = 0
        for (player in Bukkit.getOnlinePlayers()){
            playerList.append("${Main.discordBot.regex.replace(player.playerListName, "")}, ")
            playerOnline++
        }
        if (Bukkit.getOnlinePlayers().isEmpty()){
            playerList.append("N/A")
        }
        val currentUsedRAM : Long = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
        val maxRAM : Long = Runtime.getRuntime().totalMemory() /  1048576L
        val ramUsedPercent = Math.round((currentUsedRAM.toFloat()  / maxRAM.toFloat()) * 100)
        val tpsString = StringBuilder()
        for (tps in Bukkit.getTPS()){
            tpsString.append("${Math.round(tps * 100.0F)/100.0F}, ")
        }
        val embed = EmbedBuilder()
                .setAuthor("Server Info")
                .setTitle("Server TPS, Server Playerlist, Server RAM usage, Server Uptime")
                .addInlineField("Server TPS 1M, 5M, 15M", "$tpsString")
                .addInlineField("Server RAM Usage", "$currentUsedRAM MiB / $maxRAM MiB ($ramUsedPercent%)")
                .addInlineField("Server Uptime(HH:MM:SS): ", "${Command.getUptime()} ")
                .addField("Users Online ($playerOnline)", "$playerList")

        message.serverTextChannel.ifPresent {
            it.sendMessage(embed)
            Main.discordBot.log("[pengcord]: User ${sender.idAsString} (${sender.discriminatedName}) ran command `$commandName`.")
            Main.discordBot.logEmbed(embed)
        }

    }
}