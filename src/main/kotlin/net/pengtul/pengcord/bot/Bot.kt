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


import club.minnced.discord.webhook.external.JavacordWebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.pengtul.pengcord.bot.botcmd.*
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandHandler
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import org.bukkit.entity.Player
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.permission.PermissionsBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.UserStatus
import org.javacord.api.entity.webhook.WebhookBuilder
import org.joda.time.DateTime
import java.lang.StringBuilder
import kotlin.collections.ArrayList


class Bot {
    var discordApi: DiscordApi = DiscordApiBuilder()
            .setToken(Main.serverConfig.botToken)
            .setAllIntents()
            .login()
            .exceptionally {
                throw DiscordLoginFailException("Failed to log into discord!")
            }
            .join()
    private lateinit var webhook: JavacordWebhookClient
    var commandHandler: JCDiscordCommandHandler
    var chatFilterRegex: Regex
    private val regex: Regex = """(§.)""".toRegex()
    lateinit var discordServer: Server

    init {
        this.onSucessfulConnect()

        try {
            webhook = JavacordWebhookClient.withUrl(Main.serverConfig.webhookURL.toString())
        } catch (_: Exception) {
            Main.serverConfig.botChatSyncChannel?.let { channelId ->
                discordApi.getServerTextChannelById(channelId).ifPresentOrElse({ channel ->
                webhook = JavacordWebhookClient.from(
                    WebhookBuilder(channel)
                        .setName("DSC-SYNC")
                        .create()
                        .join()
                )
                }, {
                    throw DiscordLoginFailException("Sync Channel Cannot be null!")
                })
            }
        }

        Main.serverConfig.webhookURL = webhook.url

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
                Regex("", RegexOption.LITERAL)
            }
        } else {
            Regex("", RegexOption.LITERAL)
        }

        this.discordApi.updateStatus(
            UserStatus.ONLINE
        )
        this.discordApi.updateActivity(ActivityType.PLAYING, Main.serverConfig.botPlayingStatus)

        val bc : MutableList<String> = ArrayList()
        bc.add(Main.serverConfig.botChatSyncChannel.toString())
        bc.add(Main.serverConfig.botLoggingChannel.toString())
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
        if (Main.serverConfig.botServer == 0L) {
            val permissions = PermissionsBuilder()
                .setAllowed(PermissionType.CREATE_INSTANT_INVITE)
                .setAllowed(PermissionType.SEND_MESSAGES)
                .setAllowed(PermissionType.READ_MESSAGES)
                .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                .setAllowed(PermissionType.MANAGE_WEBHOOKS)
                .setAllowed(PermissionType.EMBED_LINKS)
                .setAllowed(PermissionType.ADD_REACTIONS)

            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                permissions.setAllowed(PermissionType.BAN_MEMBERS)
                permissions.setAllowed(PermissionType.KICK_MEMBERS)
                permissions.setAllowed(PermissionType.MANAGE_ROLES)
            }

            if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                permissions.setAllowed(PermissionType.MANAGE_MESSAGES)
            }

            Main.serverLogger.info("[pengcord]: Invite to server using:")
            Main.serverLogger.info(discordApi.createBotInvite(permissions.build()))
            throw DiscordLoginFailException("Failed to get discord server")
        }
        Main.serverConfig.botServer?.let {
            this.discordApi.getServerById(it).ifPresentOrElse (
                {
                    server ->
                    Main.serverLogger.info("[pengcord]: Connected to discord server ${server.name}")
                    discordServer = server
                },
                {
                    val permissions = PermissionsBuilder()
                        .setAllowed(PermissionType.CREATE_INSTANT_INVITE)
                        .setAllowed(PermissionType.SEND_MESSAGES)
                        .setAllowed(PermissionType.READ_MESSAGES)
                        .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                        .setAllowed(PermissionType.MANAGE_WEBHOOKS)
                        .setAllowed(PermissionType.EMBED_LINKS)
                        .setAllowed(PermissionType.ADD_REACTIONS)

                    if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                        permissions.setAllowed(PermissionType.BAN_MEMBERS)
                        permissions.setAllowed(PermissionType.KICK_MEMBERS)
                        permissions.setAllowed(PermissionType.MANAGE_ROLES)
                    }

                    if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                        permissions.setAllowed(PermissionType.MANAGE_MESSAGES)
                    }

                    Main.serverLogger.info("[pengcord]: Invite to server using:")
                    Main.serverLogger.info(discordApi.createBotInvite(permissions.build()))
                    throw DiscordLoginFailException("Failed to get discord server")
                }
            )
        }
    }

    fun sendMessageToDiscord(message: String){
        if (message == "") {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    channel.sendMessage(regex.replace(message,""))
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
                    channel.sendMessage("[GAME]: ${regex.replace(message,"")}")
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
                    channel.sendMessage("[GAME]: ${regex.replace(message,"")}")
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
                    channel.sendMessage("[BROADCAST]: ${regex.replace(message,"")}")
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
        Main.serverLogger.info(message)
        if (this::webhook.isInitialized && message.isNotBlank() && usrname.isNotBlank()) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val senderUsername = if (usrname.lowercase() != "clyde") usrname else "BlydE"
                val webhookMessage = WebhookMessageBuilder()
                    .setUsername(senderUsername)
                    .setAvatarUrl(Main.getDownloadSkinURL(player.uniqueId))
                    .setContent(message)
                    .build()
                webhook.send(webhookMessage)
            })
        }
    }

    fun cleanUpWebhook() {
        this.webhook.close()
    }
}

