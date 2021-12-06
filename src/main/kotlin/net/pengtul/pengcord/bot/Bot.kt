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
import net.pengtul.pengcord.bot.botcmd.*
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandHandler
import net.pengtul.pengcord.data.ServerConfig
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.webhook.Webhook
import org.javacord.api.entity.webhook.WebhookBuilder
import org.javacord.api.entity.webhook.WebhookUpdater
import org.joda.time.DateTime
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class Bot {
    var discordApi: DiscordApi = DiscordApiBuilder()
            .setToken(Main.serverConfig.botToken)
            .login()
            .exceptionally {
                throw DiscordLoginFailException("Failed to log into discord!")
            }
            .join()
    private var webhookInit: Boolean
    lateinit var webhook: Webhook
    private lateinit var webhookUpdater: WebhookUpdater
    private lateinit var webhookSender: WebhookClient
    var commandHandler: JCDiscordCommandHandler
    var chatFilterRegex: Regex
    private val regex: Regex = """(§.)""".toRegex()
    lateinit var discordServer: Server

    init {
        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getServerTextChannelById(
                it
            ).ifPresent { serverTextChannel: ServerTextChannel ->
                this.webhook = WebhookBuilder(serverTextChannel)
                    .setName("DSC-SYNC")
                    .create()
                    .join()
                this.webhookUpdater = webhook.createUpdater()
                ServerConfig
            }
        }
        webhookInit = this::webhook.isInitialized && this::webhookSender.isInitialized && this::webhookUpdater.isInitialized
        this.onSucessfulConnect()
        discordApi.addListener(DscMessageEvent())
        if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
            discordApi.addListener(DscServerMemberLeaveEvent())
            discordApi.addListener(DscServerMemberBannedEvent())
        }

        chatFilterRegex = if (Main.serverConfig.enableLiterallyNineteenEightyFour){
            if (Main.serverConfig.bannedWords.isNotEmpty()){
                val regexString = StringBuilder()
                for (word in Main.serverConfig.bannedWords){
                    regexString.append("($word)|")
                }
                regexString.deleteAt(regexString.length-1) // Drop the last |
                Regex(regexString.toString(), RegexOption.IGNORE_CASE)
            } else {
                Regex("(?!)", RegexOption.IGNORE_CASE)
            }
        } else {
            Regex("(?!)", RegexOption.IGNORE_CASE)
        }
        val bc : MutableList<String> = ArrayList()
        bc.add(Main.serverConfig.botChatSyncChannel!!.toString())
        bc.add(Main.serverConfig.botLoggingChannel!!.toString())
        commandHandler = JCDiscordCommandHandler(discordApi, Main.serverConfig.botPrefix, true, bc.toList())
        // General Commands
        commandHandler.addCommand(Info())
        commandHandler.addCommand(StopServer())
        commandHandler.addCommand(WhoIs())
        commandHandler.addCommand(Me())
        // Users
        commandHandler.addCommand(Verify())
        commandHandler.addCommand(Unverify())
        // Punishments
        commandHandler.addCommand(Warn())
        commandHandler.addCommand(Ban())
        commandHandler.addCommand(UnBan())
        commandHandler.addCommand(Mute())
        commandHandler.addCommand(UnMute())
        commandHandler.addCommand(Query())
        commandHandler.addCommand(QueryRecord())
        commandHandler.generateHelp()
    }

    private fun onSucessfulConnect() {
        val botServerId = Main.serverConfig.botServer!!
        val server = Main.discordBot.discordApi.getServerById(botServerId)!!
        discordServer = if (server.isPresent) server.orElseThrow() else throw DiscordLoginFailException("Failed to get discord server")
        Main.serverLogger.info("[pengcord]: Successfully connected to Discord! Invite to server using:")
        Main.serverLogger.info(discordApi.createBotInvite())
    }

    fun sendMessageToDiscord(message: String){
        if (message == "") {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    channel.sendMessage("${Main.serverConfig.botPrefix}${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendMessageToDiscordJoinLeave(message: String){
        if (message == "" || Main.serverConfig.filterJoinLeaveMessage) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    channel.sendMessage("[GAME]: ${Main.serverConfig.botPrefix}${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendMessageToDiscordInGame(message: String){
        if (message == "" || Main.serverConfig.filterInGameMessage) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    channel.sendMessage("[GAME]: ${Main.serverConfig.botPrefix}${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendMessageToDiscordAnnouncement(message: String){
        if (message == "" || Main.serverConfig.filterAnnouncements) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    channel.sendMessage("[BROADCAST]: ${Main.serverConfig.botPrefix}${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendEmbedToDiscord(message: EmbedBuilder){
        Main.serverLogger.info("aaaaa")
        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(
                it
            ).ifPresent { channel ->
                channel.sendMessage(message)
            }
        }
    }

    fun log(type: LogType, message: String){
        Main.serverConfig.botLoggingChannel?.let {
            when (type) {
                LogType.Announcement -> {
                    if (!Main.serverConfig.filterAnnouncements) {
                        discordApi.getTextChannelById(it).ifPresent { channel ->
                            channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                        }
                    }
                }
                LogType.InGameStuff -> {
                    if (!Main.serverConfig.filterInGameMessage) {
                        discordApi.getTextChannelById(it).ifPresent { channel ->
                            channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                        }
                    }
                }
                LogType.PlayerJoin -> {
                    if (!Main.serverConfig.filterJoinLeaveMessage) {
                        discordApi.getTextChannelById(it).ifPresent { channel ->
                            channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                        }
                    }
                }
                LogType.PlayerLeave -> {
                    if (!Main.serverConfig.filterJoinLeaveMessage) {
                        discordApi.getTextChannelById(it).ifPresent { channel ->
                            channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                        }
                    }
                }
                LogType.PlayerDie -> {
                    if (!Main.serverConfig.filterInGameMessage) {
                        discordApi.getTextChannelById(it).ifPresent { channel ->
                            channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                        }
                    }
                }
                else -> {
                    discordApi.getTextChannelById(it).ifPresent { channel ->
                        channel.sendMessage("[${DateTime.now()}]: [pengcord]: ${type}${regex.replace(message, "")}")
                    }
                }
            }
        }
    }

    fun logEmbed(embed: EmbedBuilder){
        Main.serverConfig.botLoggingChannel?.let {
            discordApi.getTextChannelById(
                it
            ).ifPresent {channel ->
                embed.addField("Time Log:", "UnixTime: ${DateTime.now()}.")
                channel.sendMessage(embed)
            }
        }
    }

    fun sendMessagetoWebhook(message: String, usrname: String, player: Player){
        if(webhookInit){
            val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord")
            currentPlugin?.let {
                Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
                    val msg: String = message
                    webhookUpdater = if (usrname.lowercase(Locale.getDefault()) == "clyde"){
                        webhookUpdater.setName("cly de")
                    } else {
                        webhookUpdater.setName(usrname)
                    }
                    try {
                        webhookUpdater.setAvatar(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player.uniqueId}.png"))
                    }
                    catch (e: Exception){
                        Main.serverLogger.severe("Failed to get PlayerIcon for player ${player.name}: Exception $e")
                    }
                    webhook = webhookUpdater.update().join()

                    this.webhookSender.send(msg)
                })
            }
        }
    }
}

