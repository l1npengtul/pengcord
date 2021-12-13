package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User

class Query: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "query"
    override val commandDescription: String
        get() = "Query a specific punishment. b -> Bans, m -> Mutes, w -> Warns, f -> Filter Alerts"
    override val commandUsage: String
        get() = "query <b|m|w|f> <punishmentId>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!Utils.doesUserHavePermission(sender, "pengcord.punishments.query")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }
        val whatToQuery = args[0]
        val punishmentToQuery = args[1].toLong()

        when (whatToQuery.lowercase()) {
            "filter", "filteralerts", "f" -> {
                Main.database.queryPlayerFilterAlertById(punishmentToQuery)?.let { filterAlert ->
                    val embedBuilder = EmbedBuilder()
                        .setAuthor("Filter Alert Query")
                        .setTitle("Filter Alert Query for filterAlertId${filterAlert.filterAlertId}")
                        .addInlineField("ID:", "${filterAlert.filterAlertId}")
                        .addInlineField("Target UUID:", "${filterAlert.playerUUID}")
                        .addInlineField("Target Discord UUID:", "${filterAlert.discordUUID}")
                        .addInlineField("Issued On:", "${filterAlert.issuedOn}")
                        .addInlineField("Message Context:", filterAlert.context)
                        .addInlineField("Words:", filterAlert.word)
                    Main.getDownloadedSkinAsFile(filterAlert.playerUUID)?.let {
                        embedBuilder.setImage(it)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(embedBuilder)
                        
                        Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
                }
            }
            "warn", "warns", "w" -> {
                Main.database.queryPlayerWarnById(punishmentToQuery)?.let { warn ->
                    val embedBuilder = EmbedBuilder()
                        .setAuthor("Warn Query")
                        .setTitle("Warn Query for warnId${warn.warnId}")
                        .addInlineField("ID:", "${warn.warnId}")
                        .addInlineField("Target UUID:", "${warn.playerUUID}")
                        .addInlineField("Target Discord UUID:", "${warn.discordUUID}")
                        .addInlineField("Issued On:", "${warn.issuedOn}")
                        .addInlineField("Issued By:", warn.issuedBy)
                        .addInlineField("Reason:", warn.reason)
                    Main.getDownloadedSkinAsFile(warn.playerUUID)?.let {
                        embedBuilder.setImage(it)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(embedBuilder)
                        
                        Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
                }
            }
            "mute", "mutes", "m" -> {
                Main.database.queryPlayerMuteById(punishmentToQuery)?.let { mute ->
                    val embedBuilder = EmbedBuilder()
                        .setAuthor("Mute Query")
                        .setTitle("Mute Query for muteId${mute.muteId}")
                        .addInlineField("ID:", "${mute.muteId}")
                        .addInlineField("Target UUID:", "${mute.playerUUID}")
                        .addInlineField("Target Discord UUID:", "${mute.discordUUID}")
                        .addInlineField("Issued On:", "${mute.issuedOn}")
                        .addInlineField("Issued By:", mute.issuedBy)
                        .addInlineField("Reason:", mute.reason)
                    Main.getDownloadedSkinAsFile(mute.playerUUID)?.let {
                        embedBuilder.setImage(it)
                    }
                    if (mute.isPermanent) {
                        embedBuilder.addInlineField("Expires:", "Never, Permanent Mute")
                    } else {
                        embedBuilder.addInlineField("Expires:", "${mute.expiresOn}")
                    }
                    embedBuilder.addField("Current State:", "${mute.expiryState}")

                    message.addReaction("✅").thenAccept {
                        message.reply(embedBuilder)
                        
                        Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
                }
            }
            "ban", "bans", "b" -> {
                Main.database.queryPlayerBanById(punishmentToQuery)?.let { ban ->
                    val embedBuilder = EmbedBuilder()
                        .setAuthor("Ban Query")
                        .setTitle("Ban Query for banId${ban.banId}")
                        .addInlineField("ID:", "${ban.banId}")
                        .addInlineField("Target UUID:", "${ban.playerUUID}")
                        .addInlineField("Target Discord UUID:", "${ban.discordUUID}")
                        .addInlineField("Issued On:", "${ban.issuedOn}")
                        .addInlineField("Issued By:", ban.issuedBy)
                        .addInlineField("Reason:", ban.reason)
                    Main.getDownloadedSkinAsFile(ban.playerUUID)?.let {
                        embedBuilder.setImage(it)
                    }
                    if (ban.isPermanent) {
                        embedBuilder.addInlineField("Expires:", "Never, Permanent Mute")
                    } else {
                        embedBuilder.addInlineField("Expires:", "${ban.expiresOn}")
                    }
                    embedBuilder.addField("Current State:", "${ban.expiryState}")

                    message.addReaction("✅").thenAccept {
                        message.reply(embedBuilder)
                        
                        Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
                }
                return
            }
            else -> {
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Query Type!", 5, message)
                }
                return
            }
        }
    }
}