package net.pengtul.pengcord.bot

/*
*   Discord bot initialization
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


import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.pengtul.pengcord.bot.botcmd.*
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandHandler
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.intent.Intent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.webhook.Webhook
import org.javacord.api.entity.webhook.WebhookBuilder
import org.javacord.api.entity.webhook.WebhookUpdater
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import javax.swing.Icon


class Bot {
    var discordApi: DiscordApi
    var webhookInit: Boolean
    lateinit var webhook: Webhook
    lateinit var webhookUpdater: WebhookUpdater
    lateinit var webhookSender: WebhookClient
    var commandHandler: JCDiscordCommandHandler
    var chatFilterRegex: Regex
    val regex: Regex = """(§.)""".toRegex()


    init {
        discordApi = DiscordApiBuilder()
                .setToken(Main.ServerConfig.discordApiKey)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login()
                .exceptionally {
                    throw DiscordLoginFailException("Failed to log into discord!")
                }
                .join()

        if (!Main.ServerConfig.webhookID.isNullOrEmpty() && !Main.ServerConfig.webhookToken.isNullOrEmpty()){
            val whid = Main.ServerConfig.webhookID.toString()
            val whtk = Main.ServerConfig.webhookToken.toString()
            webhookSender = WebhookClient.withId(whid.toLong(), whtk)
        }
        else {
            discordApi.getServerTextChannelById(Main.ServerConfig.syncChannel).ifPresent {
                WebhookBuilder(it).setName("DSC-SYNC").create().thenAccept {
                    webhook = it
                    Main.ServerConfig.webhookID = webhook.idAsString
                    Main.ServerConfig.webhookToken = webhook.token.get()
                    val whid = Main.ServerConfig.webhookID.toString()
                    val whtk = Main.ServerConfig.webhookToken.toString()
                    webhookSender = WebhookClient.withId(whid.toLong(), whtk)
                }
            }
        }


        webhookInit = this::webhook.isInitialized && this::webhookSender.isInitialized && this::webhookUpdater.isInitialized
        this.onSucessfulConnect()
        discordApi.addListener(DscMessageEvent())

        if (Main.ServerConfig.bannedWordsEnable){
            chatFilterRegex = if (!Main.ServerConfig.bannedWords.isNullOrEmpty()){
                val regexString = StringBuilder()
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
        val bc : MutableList<String> = ArrayList()
        //bc.add(Main.ServerConfig.syncChannel!!)
        //bc.add(Main.ServerConfig.syncChannel!!)
        bc.add(Main.ServerConfig.commandChannel!!)
        bc.add(Main.ServerConfig.adminChannel!!)
        commandHandler = JCDiscordCommandHandler(discordApi, Main.ServerConfig.botPrefix!!, true, bc.toList())
        commandHandler.addCommand(BanFromDiscord())
        commandHandler.addCommand(Info())
        commandHandler.addCommand(Kick())
        commandHandler.addCommand(Stop())
        commandHandler.addCommand(Verify())
        commandHandler.addCommand(Whois())
        commandHandler.generateHelp()
    }

    private fun onSucessfulConnect() {
        Main.ServerLogger.info("[pengcord]: Successfully connected to Discord! Invite to server using:")
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

    fun log(message: String){
        discordApi.getTextChannelById(Main.ServerConfig.adminChannel).ifPresent {channel ->
            channel.sendMessage("[${System.currentTimeMillis() / 1000L}]: ${regex.replace(message, "")}")
        }
    }

    fun logEmbed(embed: EmbedBuilder){
        discordApi.getTextChannelById(Main.ServerConfig.adminChannel).ifPresent {channel ->
            embed.addField("Time Log:", "UnixTime: ${System.currentTimeMillis() / 1000L}.")
            channel.sendMessage(embed)
        }
    }

    fun sendMessagetoWebhook(message: String, usrname: String, pfp: String?, player: org.bukkit.entity.Player){
        val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord")
        Main.ServerLogger.info("async task")
        currentPlugin?.let {
            Main.ServerLogger.info("async task")
            Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
                Main.ServerLogger.info("async task")
                val msg: String = message
                if (usrname.toLowerCase().equals("clyde")){
                    webhookUpdater = webhookUpdater.setName("cly de")
                }
                else {
                    val wbhkmsg: WebhookMessageBuilder = WebhookMessageBuilder()
                    wbhkmsg.setUsername(regex.replace(usrname, ""))
                    wbhkmsg.setAvatarUrl(Main.getSkinURL(player))
                    wbhkmsg.setContent(msg)
                    webhookSender.send(wbhkmsg.build()).thenAccept {
                    }
                }
            })
        }
    }
}

