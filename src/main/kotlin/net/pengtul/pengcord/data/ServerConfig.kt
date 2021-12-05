package net.pengtul.pengcord.data

import net.pengtul.pengcord.error.DiscordLoginFailException
import org.bukkit.configuration.file.FileConfiguration
import java.lang.Exception

class ServerConfig(rawConfig: FileConfiguration) {
    // Verify related settings
    val enableVerify: Boolean
    val discordServerLink: String?
    val minecraftServerIp: String?

    // User-editable discord settings
    var enableSync: Boolean
    val botToken: String
    val botPrefix: String
    val botBioText: String
    val botPlayingStatus: String
    val filterJoinLeaveMessage: Boolean
    val filterInGameMessage: Boolean
    val filterAnnouncements: Boolean
    val serverMessagePrefix: String
    var discordAdminRoles: List<Long>
    var botChatSyncChannel: Long?
    var botCommandChannel: Long?
    var botLoggingChannel: Long?
    var botServer: Long?

    // Bot-Managed Discord Settings
    var webhookId: Long?
    var webhookToken: Long?

    // Word Filter Settings
    val enableLiterallyNineteenEightyFour: Boolean
    var bannedWords: List<String>
    val filteredMessage: String

    // Moderation Settings
    val enableCrossMinecraftDiscordModeration: Boolean
    val discordMutedRole: Long
    val discordBanDeleteMessageDays: Int

    init {
        enableVerify = rawConfig.getBoolean("enable-verify")
        discordServerLink = rawConfig.getString("discord-server-link")
        minecraftServerIp = rawConfig.getString("minecraft-server-ip")

        enableSync = rawConfig.getBoolean("enable-sync")
        val token = rawConfig.getString("bot-token")
        botToken = if (!token.isNullOrBlank()) token.toString() else throw DiscordLoginFailException("Token cannot be null or blank!")
        botPrefix = rawConfig.getString("bot-prefix") ?: "mc!"
        filterJoinLeaveMessage = rawConfig.getBoolean("filter-join-leave-message")
        filterInGameMessage = rawConfig.getBoolean("filter-in-game-message")
        filterAnnouncements = rawConfig.getBoolean("filter-announcements")
        serverMessagePrefix = "${rawConfig.getString("server-message-prefix") ?: "[Server]:"} "
        discordAdminRoles = rawConfig.getList("discord-admin-roles")?.mapNotNull { it?.toString()?.toLong() } ?: emptyList()
        botChatSyncChannel = rawConfig.getLong("bot-chat-sync-channel")
        botCommandChannel = rawConfig.getLong("bot-command-channel")
        botLoggingChannel = rawConfig.getLong("bot-logging-channel")
        botServer = rawConfig.getLong("bot-server")
        botBioText = rawConfig.getString("bot-bio-text") ?:  "A link between worlds..."
        botPlayingStatus = rawConfig.getString("bot-playing-status") ?: "Vanguard of the Minecraft Server"

        webhookId = rawConfig.getLong("webhook-id")
        webhookToken = rawConfig.getLong("webhook-token")

        enableLiterallyNineteenEightyFour = rawConfig.getBoolean("enable-literally-nineteen-eighty-four")
        val banWords = rawConfig.getStringList("banned-words")
        banWords.forEach { word ->
            if (word.length > 50) {
                throw IllegalArgumentException("One word is greater than 50 character!")
            }
        }
        bannedWords = banWords
        filteredMessage = rawConfig.getString("filtered-message") ?: "§c§oSorry, but you are not allowed to say that as it violates our rules."

        enableCrossMinecraftDiscordModeration = rawConfig.getBoolean("enable-bot-moderation-features")
        discordMutedRole = rawConfig.getLong("discord-muted-role")
        var days = rawConfig.getInt("discord-onban-delete-message-days")
        if (days > 7) {
            days = 7
        } else if (days < 0) {
            days = 0
        }
        discordBanDeleteMessageDays = days
    }

    companion object {
        fun new(rawConfig: FileConfiguration): Result<ServerConfig> {
            return try {
                Result.success(ServerConfig(rawConfig))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun saveToConfigFile(rawConfig: FileConfiguration) {
        // Dont save enableSync to make it temporary
        rawConfig.set("discord-admin-roles", discordAdminRoles)
        rawConfig.set("bot-chat-sync-channel", botChatSyncChannel)
        rawConfig.set("bot-command-channel", botCommandChannel)
        rawConfig.set("bot-logging-channel", botLoggingChannel)
        rawConfig.set("bot-server", botServer)
        rawConfig.set("webhook-id", webhookId)
        rawConfig.set("webhook-token", webhookToken)
        rawConfig.set("banned-words", this.bannedWords)
    }
}
