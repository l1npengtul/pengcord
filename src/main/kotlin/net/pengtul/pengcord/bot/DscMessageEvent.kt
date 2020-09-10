package net.pengtul.pengcord.bot

/*
*    Event that fires and processes incoming messages from Discord
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


import com.vdurmont.emoji.EmojiParser
import net.pengtul.pengcord.bot.botcmd.Command
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.lang.Exception

public class DscMessageEvent: MessageCreateListener {
    private var command: Command = Command();
    public override fun onMessageCreate(event: MessageCreateEvent?) {
        val msg: Message? = event?.message;
        msg?.let {
            if (msg.content.startsWith(Main.ServerConfig.botPrefix.toString())){
                Main.ServerLogger.info("Inc CMD: In text channel ${msg.channel.idAsString} by user ${msg.author.idAsString}");
                val messageAsList: List<String> = msg.content.split(" ");
                when (messageAsList[0]){
                    "${Main.ServerConfig.botPrefix}bind" -> {
                        if(msg.author.asUser().isPresent){
                            command.bindCommand(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                    "${Main.ServerConfig.botPrefix}verify" -> {
                        if(msg.author.asUser().isPresent){
                            command.verifyCommand(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                    "${Main.ServerConfig.botPrefix}whois" -> {
                        if(msg.author.asUser().isPresent){
                            command.whoIs(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                    "${Main.ServerConfig.botPrefix}stop" -> {
                        if(msg.author.asUser().isPresent){
                            command.stop(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                    "${Main.ServerConfig.botPrefix}unverify" -> {
                        if(msg.author.asUser().isPresent){
                            command.unVerify(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                    "${Main.ServerConfig.botPrefix}pban" -> {
                        if(msg.author.asUser().isPresent){
                            command.banDiscord(msg.content.toString(), msg.author.asUser().get(), msg)
                        }
                    }
                }
            }
            else {
                if (!(msg.author.isWebhook || msg.author.isBotUser || msg.author.isYourself) && Main.doSyncDiscord){
                    if (msg.channel.idAsString.equals(Main.ServerConfig.syncChannel.toString())){
                        try{
                            Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: ${EmojiParser.parseToAliases(msg.readableContent)}");
                            for (attachment in msg.attachments){
                                if (attachment.isSpoiler){
                                    Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: SPOILER IMG: ${attachment.proxyUrl}");
                                }
                                else {
                                    Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: ${attachment.proxyUrl}");
                                }
                            }
                        }
                        catch (e: Exception){
                            Main.ServerLogger.severe("Failed to broadcast message! Exception $e")
                            Main.ServerLogger.severe(e.stackTrace.toString())
                        }
                    }
                }
            }
        }
    }
}