package net.pengtul.pengcord.bot

/*
*   Discord bot initialization
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


import club.minnced.discord.webhook.WebhookClient
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.Embed
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import org.javacord.api.entity.webhook.Webhook
import org.javacord.api.entity.webhook.WebhookBuilder
import org.javacord.api.entity.webhook.WebhookUpdater
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder


class Bot {
    var discordApi: DiscordApi
    var webhookInit: Boolean
    lateinit var webhook: Webhook
    lateinit var webhookUpdater: WebhookUpdater
    lateinit var webhookSender: WebhookClient
    var chatFilterRegex: Regex
    val regex: Regex = """(§.)""".toRegex()


    companion object {
        public fun unverify(discordUUID: String){
            Main.ServerConfig.usersList?.remove(discordUUID)
        }
    }

    init {
        discordApi = DiscordApiBuilder()
                .setToken(Main.ServerConfig.discordApiKey)
                .login()
                .exceptionally {
                    throw DiscordLoginFailException("Failed to log into discord!")
                }
                .join()


        discordApi.getServerTextChannelById(Main.ServerConfig.syncChannel).ifPresent { serverTextChannel: ServerTextChannel ->
            this.webhook = WebhookBuilder(serverTextChannel)
                    .setName("DSC-SYNC")
                    .create()
                    .join()
            this.webhookUpdater = webhook.createUpdater()
            webhook.token.ifPresent { s: String ->
                this.webhookSender = WebhookClient.withId(webhook.id,s)
            }
        }
        webhookInit = this::webhook.isInitialized && this::webhookSender.isInitialized && this::webhookUpdater.isInitialized
        this.onSucessfulConnect()
        discordApi.addListener(DscMessageEvent())

        if (Main.ServerConfig.bannedWordsEnable){
            chatFilterRegex = if (!Main.ServerConfig.bannedWords.isNullOrEmpty()){
                var regexString = StringBuilder()
                for (word in Main.ServerConfig.bannedWords!!){
                    regexString.append("($word)|")
                }
                regexString.deleteAt(regexString.length-1) // Drop the last |
                Regex(regexString.toString(), RegexOption.IGNORE_CASE)
            } else {
                Regex("(?!)", RegexOption.IGNORE_CASE)
            }
        }
        else {
            chatFilterRegex = Regex("(?!)", RegexOption.IGNORE_CASE)
        }
    }

    private fun onSucessfulConnect() {
        Main.ServerLogger.info("Sucessfully connected to Discord! Invite to server using:")
        Main.ServerLogger.info(discordApi.createBotInvite())
    }

    fun sendMessageToDiscord(message: String){
        discordApi.getTextChannelById(Main.ServerConfig.syncChannel).ifPresent { channel ->
            if (!regex.replace(message,"").startsWith("[DSC]")){
                channel.sendMessage("${Main.ServerConfig.serverPrefix}${regex.replace(message,"")}")
            }
        }
    }

    fun sendEmbedToDiscord(message: EmbedBuilder){
        Main.ServerLogger.info("aaaaa")
        discordApi.getTextChannelById(Main.ServerConfig.syncChannel).ifPresent { channel ->
            Main.ServerLogger.info("aaaaaa")
            channel.sendMessage(message)
        }
    }


    fun sendMessagetoWebhook(message: String, usrname: String, pfp: String?, player: org.bukkit.entity.Player){
        if(webhookInit){
            val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord")
            currentPlugin?.let {
                Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
                    var msg: String = message
                    if (usrname.toLowerCase().equals("clyde")){
                        webhookUpdater = webhookUpdater.setName("cly de")
                    }
                    else {
                        webhookUpdater = webhookUpdater.setName(regex.replace(usrname, ""))
                    }
                    try {
                        webhookUpdater.setAvatar(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player.uniqueId}.png"))
                    }
                    catch (e: Exception){
                        Main.ServerLogger.severe("Failed to get PlayerIcon for player ${player.displayName}: Exception $e")
                    }
                    webhook = webhookUpdater.update().join()

                    this.webhookSender.send(msg)
                })
            }
        }
    }
}

