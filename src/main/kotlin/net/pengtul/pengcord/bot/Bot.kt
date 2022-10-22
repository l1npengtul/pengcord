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
import net.pengtul.pengcord.bot.listeners.emoji.DscEmojiEditedListener
import net.pengtul.pengcord.bot.listeners.emoji.DscEmojiRemovedListener
import net.pengtul.pengcord.bot.listeners.emoji.DscEmojiWhitelistChangedListener
import net.pengtul.pengcord.bot.listeners.DscMessageListener
import net.pengtul.pengcord.bot.listeners.DscServerMemberBannedListener
import net.pengtul.pengcord.bot.listeners.DscServerMemberLeaveListener
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.util.Utils.Companion.formatMessage
import org.bukkit.entity.Player
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.permission.PermissionsBuilder
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.UserStatus
import org.javacord.api.entity.webhook.WebhookBuilder
import org.joda.time.DateTime
import kotlin.collections.ArrayList
import net.pengtul.pengcord.bot.listeners.emoji.DscEmojiAddedListener
import kotlin.text.StringBuilder


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
    val regex: Regex = """(§.)""".toRegex()
    lateinit var discordServer: Server
    var mutedRole: Role? = null
    val mentionPlayerRegex = Regex("(@(.+)#\\d{4})")
    val mentionImproperFormatPlayerRegex = Regex("(@(.+))")
    val everyoneMentionRegex = Regex("@everyone")
    val hereMentionRegex = Regex("@here")
    var checkIfServerEmojiUsed = Regex("")
    var serverEmoteRegexMap: HashMap<Regex, String> = HashMap()
    private var lastMessageSentBroadcast = ""
    private var lastMessageSentJoinLeave = ""
    private var lastMessageSentInGame = ""
    private var lastMessageSentAnnouncement = ""

    init {
        this.onSucessfulConnect()
        try {
            webhook = JavacordWebhookClient.withUrl(Main.serverConfig.webhookURL.toString())
            Main.serverConfig.botChatSyncChannel?.let { channelId ->
                if (discordApi.getServerThreadChannelById(channelId).isPresent) {
                    webhook = webhook.onThread(channelId)
                }
            }
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
                    discordApi.getServerThreadChannelById(channelId).ifPresentOrElse({ channel ->
                        val serverRegular = channel.parent.asServerTextChannel().orElseThrow()
                        webhook = JavacordWebhookClient.from(
                            WebhookBuilder(serverRegular)
                                .setName("DSC-SYNC")
                                .create()
                                .join()
                        )
                            .onThread(channel.id)
                    }, {
                        throw DiscordLoginFailException("Invalid Server Sync")
                    })
                })
            }
        }

        Main.serverConfig.webhookURL = webhook.url

        discordApi.addListener(DscMessageListener())
        if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
            discordApi.addListener(DscServerMemberLeaveListener())
            discordApi.addListener(DscServerMemberBannedListener())
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
        commandHandler = JCDiscordCommandHandler(discordApi, Main.serverConfig.botPrefix, bc.toList())
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

        this.discordApi.getRoleById(Main.serverConfig.discordMutedRole).ifPresentOrElse(
            {
                mutedRole = it
            },
            {
                mutedRole = null
            }
        )

        if (Main.serverConfig.enableDiscordCustomEmojiSync) {
            updateDiscordEmojis()
            discordApi.addListener(DscEmojiAddedListener())
            discordApi.addListener(DscEmojiEditedListener())
            discordApi.addListener(DscEmojiRemovedListener())
            discordApi.addListener(DscEmojiWhitelistChangedListener())
        }
    }

    fun updateDiscordEmojis() {
        Main.serverLogger.info("Syncing emojis...")

        val emojiRegexString = StringBuilder()

        this.discordServer.customEmojis.forEach {emoji ->
            if (emojiRegexString.isBlank()) {
                emojiRegexString.append("(:${emoji.name}:)")
            } else {
                emojiRegexString.append("|(:${emoji.name}:)")
            }
            Main.serverLogger.info("Found emoji ${emoji.name}")
            this.serverEmoteRegexMap[Regex("(:${emoji.name}:)")] = "<:${emoji.name}:${emoji.id}>"
        }

        this.checkIfServerEmojiUsed = Regex(emojiRegexString.toString())
    }

    private fun onSucessfulConnect() {
        if (Main.serverConfig.botServer == 0L) {
            val permissions = PermissionsBuilder()
                .setAllowed(PermissionType.CREATE_INSTANT_INVITE)
                .setAllowed(PermissionType.SEND_MESSAGES)
                .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                .setAllowed(PermissionType.MANAGE_WEBHOOKS)
                .setAllowed(PermissionType.EMBED_LINKS)
                .setAllowed(PermissionType.ADD_REACTIONS)
                .setAllowed(PermissionType.MANAGE_CHANNELS)
                .setAllowed(PermissionType.MANAGE_THREADS)

            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                permissions.setAllowed(PermissionType.BAN_MEMBERS)
                permissions.setAllowed(PermissionType.KICK_MEMBERS)
                permissions.setAllowed(PermissionType.MANAGE_ROLES)
            }

            if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                permissions.setAllowed(PermissionType.MANAGE_MESSAGES)
            }

            Main.serverLogger.info("Invite to server using:")
            Main.serverLogger.info(discordApi.createBotInvite(permissions.build()))
            throw DiscordLoginFailException("Failed to get discord server")
        }
        Main.serverConfig.botServer?.let {
            this.discordApi.getServerById(it).ifPresentOrElse (
                {
                    server ->
                    Main.serverLogger.info("Connected to discord server ${server.name}")
                    discordServer = server
                },
                {
                    val permissions = PermissionsBuilder()
                        .setAllowed(PermissionType.CREATE_INSTANT_INVITE)
                        .setAllowed(PermissionType.SEND_MESSAGES)
                        .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                        .setAllowed(PermissionType.READ_MESSAGE_HISTORY)
                        .setAllowed(PermissionType.MANAGE_WEBHOOKS)
                        .setAllowed(PermissionType.EMBED_LINKS)
                        .setAllowed(PermissionType.ADD_REACTIONS)
                        .setAllowed(PermissionType.MANAGE_CHANNELS)
                        .setAllowed(PermissionType.MANAGE_THREADS)

                    if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                        permissions.setAllowed(PermissionType.BAN_MEMBERS)
                        permissions.setAllowed(PermissionType.KICK_MEMBERS)
                        permissions.setAllowed(PermissionType.MANAGE_ROLES)
                    }

                    if (Main.serverConfig.enableLiterallyNineteenEightyFour) {
                        permissions.setAllowed(PermissionType.MANAGE_MESSAGES)
                    }

                    Main.serverLogger.info("Invite to server using:")
                    Main.serverLogger.info(discordApi.createBotInvite(permissions.build()))
                    throw DiscordLoginFailException("Failed to get discord server")
                }
            )
        }
    }

    fun sendMessageToDiscord(message: String){
        if (message == "" || lastMessageSentBroadcast.startsWith(message)) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    lastMessageSentBroadcast = message
                    channel.sendMessage(regex.replace(message,""))
                }
            }
        }
    }

    fun sendMessageToDiscordJoinLeave(message: String){
        if (message == "" || Main.serverConfig.filterJoinLeaveMessage || lastMessageSentJoinLeave.startsWith(message)) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    lastMessageSentJoinLeave = message
                    channel.sendMessage("[GAME]: ${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendMessageToDiscordInGame(message: String){
        if (message == "" || Main.serverConfig.filterInGameMessage || lastMessageSentInGame.startsWith(message)) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    lastMessageSentInGame = message
                    channel.sendMessage("[GAME]: ${regex.replace(message,"")}")
                }
            }
        }
    }

    fun sendMessageToDiscordAnnouncement(message: String){
        if (message == "" || Main.serverConfig.filterAnnouncements || lastMessageSentAnnouncement.startsWith(message)) {
            return
        }

        Main.serverConfig.botChatSyncChannel?.let {
            discordApi.getTextChannelById(it).ifPresent { channel ->
                if (!regex.replace(message,"").startsWith("[DSC]")){
                    lastMessageSentAnnouncement = message
                    channel.sendMessage("[BROADCAST]: ${regex.replace(message,"")}")
                }
            }
        }
    }

//    fun sendEmbedToDiscord(message: EmbedBuilder){
//        Main.serverLogger.info("aaaaa")
//        Main.serverConfig.botChatSyncChannel?.let {
//            discordApi.getTextChannelById(
//                it
//            ).ifPresent { channel ->
//                channel.sendMessage(message)
//            }
//        }
//    }

    fun log(type: LogType, msg: String){
        val message = msg.replace("@", "@#")
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

    fun sendMessagetoWebhook(message: String, usrname: String, player: net.pengtul.pengcord.data.schema.Player){
        val fmt = formatMessage(message)
        if (this::webhook.isInitialized && fmt.isNotBlank() && usrname.isNotBlank()) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val senderUsername = if (usrname.lowercase() != "clyde") usrname else "BlydE"
                val webhookMessage = WebhookMessageBuilder()
                    .setUsername(senderUsername)
                    .setContent(fmt)

                val discordUser = discordApi.getUserById(player.discordUUID).get()
                val effective = discordUser.getEffectiveAvatar(discordServer)
                webhookMessage.setAvatarUrl(effective.url.toString())
                webhook.send(webhookMessage.build())
            })
        }
    }
}

