package net.pengtul.pengcord.bot

/*
*    Event that fires and processes incoming messages from Discord
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


import com.vdurmont.emoji.EmojiParser
import net.pengtul.pengcord.bot.botcmd.Command
import net.pengtul.pengcord.bot.botcmd.CommandHelper
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.lang.Exception

class DscMessageEvent: MessageCreateListener {
    override fun onMessageCreate(event: MessageCreateEvent?) {
        val msg: Message? = event?.message
        msg?.let {
            if (!(msg.author.isWebhook || msg.author.isBotUser || msg.author.isYourself) && Main.ServerConfig.enableSync && !msg.content.startsWith("mc!")){
                if (msg.channel.idAsString == Main.ServerConfig.syncChannel.toString()){
                    try{
                        if (Main.discordBot.chatFilterRegex.containsMatchIn(msg.readableContent)
                                && Main.ServerConfig.bannedWordsEnable
                                && Main.ServerConfig.bannedWordDiscord) {

                            msg.delete().thenAccept {
                                Main.ServerLogger.info("Removed Message: ${msg.content} / ${msg.readableContent}")
                                Main.ServerLogger.info("from user ${msg.author.idAsString} / ${msg.author.discriminatedName}")
                                Main.discordBot.log("[pengcord]: [ChatFilter]: User ${msg.author.idAsString} (${msg.author.discriminatedName}) tripped the word filter with message `${msg.content}` / `${msg.readableContent}`.")
                                var msgBuilder = MessageBuilder()
                                msg.userAuthor.ifPresent {
                                    msgBuilder.append(it.mentionTag)
                                }
                                msgBuilder.append(", ${Main.ServerConfig.bannedWordMessage?.let { it1 -> Main.discordBot.regex.replace(it1, "") }}")
                                msgBuilder.append(".")
                                CommandHelper.deleteAfterSend(msgBuilder.toString(), 8, msg)
                                return@thenAccept
                            }
                        }
                        else{
                            val message = "§7[DSC]${msg.author.displayName}: ${EmojiParser.parseToAliases(msg.readableContent)}"
                            if (EmojiParser.parseToAliases(msg.readableContent).isNotBlank()){
                                Main.discordBot.log("[pengcord]: User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `$message`.")
                                Bukkit.getServer().broadcastMessage(message)
                            }
                            for (attachment in msg.attachments){
                                if (attachment.isSpoiler){
                                    Main.discordBot.log("[pengcord]: User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `SPOILER: ${attachment.url}`.")
                                    Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: SPOILER: ${attachment.url}")
                                }
                                else {
                                    Main.discordBot.log("[pengcord]: User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `${attachment.url}`.")
                                    Bukkit.getServer().broadcastMessage("§7[DSC]${msg.author.displayName}: ${attachment.url}")
                                }
                            }
                        }
                    }
                    catch (e: Exception){
                        Main.discordBot.log("[pengcord]: User ${msg.author.idAsString} (${msg.author.discriminatedName}) message sync failed due to exception ${e}.")
                        Main.ServerLogger.severe("Failed to broadcast message! Exception $e")
                        Main.ServerLogger.severe(e.stackTrace.toString())
                    }
                }
            }
        }
    }
}