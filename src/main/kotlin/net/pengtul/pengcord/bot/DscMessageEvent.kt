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
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.toComponent
import net.pengtul.pengcord.toTextColor
import org.bukkit.Bukkit
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
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
                        if ((Main.discordBot.chatFilterRegex.containsMatchIn(msg.readableContent) || Main.discordBot.chatFilterRegex.containsMatchIn(
                                msg.attachments.joinToString { "${it.fileName}(${it.url}" }))
                                && Main.serverConfig.enableLiterallyNineteenEightyFour) {
                            msg.delete().thenAccept {
                                Main.serverLogger.info("Removed Message: ${msg.content} / ${msg.readableContent}")
                                Main.serverLogger.info("from user ${msg.author.idAsString} / ${msg.author.discriminatedName}")
                                Main.discordBot.log(LogType.ChatFilter, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) tripped the word filter with message `${msg.content}` / `${msg.readableContent}`.")
                                msg.userAuthor.ifPresent { user ->
                                    user.sendMessage("You cannot say that.")
                                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                                        Main.database.playerGetByDiscordUUID(user.id)?.let { player ->
                                            val matchedWords = Main.discordBot.chatFilterRegex.findAll(msg.content).map {
                                                it.value
                                            }.joinToString()
                                            Main.database.addFilterAlertToPlayer(player.playerUUID, matchedWords, msg.content)
                                        }
                                    })
                                }
                                return@thenAccept
                            }
                        }
                        else {
                            // TODO: Parse Markdown
                            val finalComponent = Component.text()
                            val textToSend = EmojiParser.parseToAliases(msg.readableContent)
                            if (textToSend.isNotBlank()) {
                                var textSendComponent = Component.text()
                                Main.markdownParser.parse(textToSend).

                                finalComponent
                                    .content("[DSC] ")
                                    .style(Style.style(NamedTextColor.DARK_GRAY))
                                    .append(
                                        Component.text("${msg.author.displayName}: ")
                                            .style(Style.style(msg.author.roleColor.orElse(Color.DARK_GRAY).toTextColor())))
                                    .append(
                                        Component.text(EmojiParser.parseToAliases(msg.readableContent))
                                            .style(Style.style(NamedTextColor.DARK_GRAY))
                                    )
                            }

                            for (attachment in msg.attachments){

                                val initialContent = if (attachment.isSpoiler){
                                    Main.discordBot.log(LogType.PlayerChat, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `SPOILER: ${attachment.url}`.")
                                    finalComponent.append(Component.newline())
                                    "[DSC] [SPOILER] "
                                } else {
                                    Main.discordBot.log(LogType.PlayerChat, "User ${msg.author.idAsString} (${msg.author.discriminatedName}) sync message `${attachment.url}`.")
                                    "[DSC]"
                                }
                                val attachmentComponent = Component.text()
                                    .content(initialContent)
                                    .style(Style.style()
                                        .color(NamedTextColor.DARK_GRAY)
                                        .build())
                                    .append(
                                        Component.text("${msg.author.displayName}: ")
                                            .style(Style.style()
                                                .color(msg.author.roleColor.orElse(Color.DARK_GRAY).toTextColor())
                                                .build()))
                                    .append(
                                        Component.text(attachment.fileName)
                                            .style(Style.style()
                                                .decorate(TextDecoration.UNDERLINED)
                                                .color(NamedTextColor.DARK_GRAY)
                                                .build()))
                                    .hoverEvent(
                                        HoverEvent.showText("Click to open".toComponent())
                                    )
                                    .clickEvent(
                                        ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, attachment.url.toString())
                                    )
                                finalComponent.append(attachmentComponent.build())
                            }
                            Bukkit.broadcast(finalComponent.build())
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