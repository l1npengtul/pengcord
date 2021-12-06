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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.toComponent
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import java.awt.Color

class DscMessageEvent: MessageCreateListener {
    override fun onMessageCreate(event: MessageCreateEvent?) {
        val message: Message? = event?.message
        message?.let { msg ->
            if (!(msg.author.isWebhook || msg.author.isBotUser || msg.author.isYourself) && Main.serverConfig.enableSync && !msg.content.startsWith("mc!")){
                if (msg.channel.idAsString == Main.serverConfig.botChatSyncChannel.toString()){
                    try{
                        if (Main.discordBot.chatFilterRegex.containsMatchIn(msg.readableContent)
                                && Main.serverConfig.enableLiterallyNineteenEightyFour) {
                            msg.delete().thenAccept {
                                Main.serverLogger.info("Removed Message: ${msg.content} / ${msg.readableContent}")
                                Main.serverLogger.info("from user ${msg.author.idAsString} / ${msg.author.discriminatedName}")
                                Main.discordBot.log(LogType.ChatFilter, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) tripped the word filter with message `${msg.content}` / `${msg.readableContent}`.")
                                msg.userAuthor.ifPresent { user ->
                                    user.sendMessage("You cannot say that.")
                                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                                        Main.database.playerGetByDiscordUUID(user.id)?.let { player ->
                                            val matchedWords = Main.discordBot.chatFilterRegex.findAll(msg.content).joinToString()
                                            Main.database.addFilterAlertToPlayer(player.playerUUID, matchedWords, msg.content)
                                        }
                                    })
                                }
                                return@thenAccept
                            }
                        }
                        else {
                            val finalComponent = "".toComponent(
                                HoverEvent.showText("Click to reply!".toComponent()),
                                ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pengcord:reply ${msg.id} ")
                            )

                            val senderColor = msg.author.roleColor.orElse(Color.gray)
                            val dscMessageHeader = "[DSC]".toComponent()
                            dscMessageHeader.style(Style.style(TextColor.fromHexString("151515")))
                            finalComponent.append(dscMessageHeader)

                            val sender = msg.author.displayName.toComponent()
                            sender.style(Style.style(TextColor.color(senderColor.red, senderColor.green, senderColor.blue)))
                            finalComponent.append(sender)

                            val messageContent = EmojiParser.parseToAliases(msg.readableContent).toComponent()
                            messageContent.style(Style.style(TextColor.color(255,255,255)))
                            finalComponent.append(messageContent)


                            for (attachment in msg.attachments){
                                if (attachment.isSpoiler){
                                    Main.discordBot.log(LogType.PlayerChat, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `SPOILER: ${attachment.url}`.")
                                    finalComponent.append(Component.newline())
                                    finalComponent.append(dscMessageHeader)
                                    val attachmentComponent = Component.text(" [SPOILER]: "+attachment.url.toString())
                                    attachmentComponent.style(Style.style(TextColor.fromHexString("151515")))
                                    finalComponent.append(attachmentComponent)
                                }
                                else {
                                    Main.discordBot.log(LogType.PlayerChat, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `${attachment.url}`.")
                                    finalComponent.append(Component.newline())
                                    finalComponent.append(dscMessageHeader)
                                    val attachmentComponent = Component.text(attachment.url.toString())
                                    attachmentComponent.style(Style.style(TextColor.fromHexString("151515")))
                                    finalComponent.append(attachmentComponent)
                                }
                            }

                            Bukkit.broadcast(finalComponent)
                        }
                    }
                    catch (e: Exception){
                        Main.discordBot.log(LogType.GenericError, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) message sync failed due to exception ${e}.")
                        Main.serverLogger.severe("Failed to broadcast message! Exception $e")
                        Main.serverLogger.severe(e.stackTrace.toString())
                    }
                }
            }
        }
    }
}