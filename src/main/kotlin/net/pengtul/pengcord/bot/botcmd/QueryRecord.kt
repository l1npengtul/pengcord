package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User

class QueryRecord: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "queryrecord"
    override val commandDescription: String
        get() = "Query punishment history of a player. b -> Bans, m -> Mutes, w -> Warns, f -> Filter Alerts, a -> All"
    override val commandUsage: String
        get() = "queryrecord <b|m|w|f|a> <player>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!doesUserHavePermission(sender, "pengcord.punishments.query")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                Main.discordBot.log(LogType.DSCComamndError, "User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }
        val whatToQuery = args[0]
        val playerToQuery = queryPlayerFromString(args[1])
        if (playerToQuery == null) {
            message.addReaction("❌").thenAccept {
                CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
            }
        }
        when (whatToQuery.lowercase()) {
            "all", "a" -> {
                playerToQuery?.playerUUID?.let { it ->
                    val alerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(it).map { filterAlert ->
                        "${filterAlert.filterAlertId}(${filterAlert.word})"
                    }
                    val alertEmbed = EmbedBuilder()
                        .setAuthor("Filter Alerts Record Report")
                        .setTitle("Filter Alerts Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Alerts: ", alerts.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        alertEmbed.setImage(img)
                    }

                    val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                        "${warn.warnId}(${warn.issuedBy})"
                    }
                    val warnEmbed = EmbedBuilder()
                        .setAuthor("Warn Record Report")
                        .setTitle("Warns Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Warns: ", warns.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        warnEmbed.setImage(img)
                    }

                    val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                    val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val muteEmbed = EmbedBuilder()
                        .setAuthor("Mutes Record Report")
                        .setTitle("Mutes Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Expired Mutes: ", expiredMutes.joinToString())
                        .addInlineField("Pardoned Mutes: ", pardonedMutes.joinToString())
                        .addInlineField("Ongoing Mutes: ", ongoingMutes.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        muteEmbed.setImage(img)
                    }

                    val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                    val expiredBans = bans.filter { ban -> ban.expiryState == ExpiryState.Expired }.map { b ->
                        "${b.banId}(${b.issuedBy})"
                    }
                    val pardonedBans =
                        bans.filter { ban -> ban.expiryState == ExpiryState.Pardoned }.map { b ->
                            "${b.banId}(${b.issuedBy})"
                        }
                    val ongoingBans = bans.filter { ban -> ban.expiryState == ExpiryState.OnGoing }.map { b ->
                        "${b.banId}(${b.issuedBy})"
                    }
                    val banEmbed = EmbedBuilder()
                        .setAuthor("Bans Record Report")
                        .setTitle("Bans Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Expired Bans: ", expiredBans.joinToString())
                        .addInlineField("Pardoned Bans: ", pardonedBans.joinToString())
                        .addInlineField("Ongoing Bans: ", ongoingBans.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        banEmbed.setImage(img)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(alertEmbed)
                        message.reply(warnEmbed)
                        message.reply(muteEmbed)
                        message.reply(banEmbed)
                        Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                        Main.serverLogger.info("User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                    }
                }
            }
            "filter", "filteralerts", "f" -> {
                playerToQuery?.playerUUID?.let { it ->
                    val alerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(it).map { filterAlert ->
                        "${filterAlert.filterAlertId}(${filterAlert.word})"
                    }
                    val alertEmbed = EmbedBuilder()
                        .setAuthor("Filter Alerts Record Report")
                        .setTitle("Filter Alerts Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Alerts: ", alerts.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        alertEmbed.setImage(img)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(alertEmbed).thenAccept {
                            Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                            Main.serverLogger.info("User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                        }
                    }
                }
            }
            "warn", "warns", "w" -> {
                playerToQuery?.playerUUID?.let { it ->
                    val warns = Main.database.queryPlayerWarnsByPlayerMinecraft(it).map { warn ->
                        "${warn.warnId}(${warn.issuedBy})"
                    }
                    val warnEmbed = EmbedBuilder()
                        .setAuthor("Warn Record Report")
                        .setTitle("Warns Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Warns: ", warns.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        warnEmbed.setImage(img)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(warnEmbed).thenAccept {
                            Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                            Main.serverLogger.info("User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                        }
                    }
                }
            }
            "mute", "mutes", "m" -> {
                playerToQuery?.playerUUID?.let {
                    val mutes = Main.database.queryPlayerMutesByPlayerMinecraft(it)
                    val expiredMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val pardonedMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val ongoingMutes = mutes.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                        "${mute.muteId}(${mute.issuedBy})"
                    }
                    val muteEmbed = EmbedBuilder()
                        .setAuthor("Mutes Record Report")
                        .setTitle("Mutes Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Expired Mutes: ", expiredMutes.joinToString())
                        .addInlineField("Pardoned Mutes: ", pardonedMutes.joinToString())
                        .addInlineField("Ongoing Mutes: ", ongoingMutes.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        muteEmbed.setImage(img)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(muteEmbed).thenAccept {
                            Main.discordBot.log(LogType.DSCComamndRan, "User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                            Main.serverLogger.info("User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                        }
                    }
                }
            }
            "ban", "bans", "b" -> {
                playerToQuery?.playerUUID?.let {
                    val bans = Main.database.queryPlayerBansByPlayerMinecraft(it)
                    val expiredBans = bans.filter { mute -> mute.expiryState == ExpiryState.Expired }.map { b ->
                        "${b.banId}(${b.issuedBy})"
                    }
                    val pardonedBans =
                        bans.filter { mute -> mute.expiryState == ExpiryState.Pardoned }.map { mute ->
                            "${mute.banId}(${mute.issuedBy})"
                        }
                    val ongoingBans = bans.filter { mute -> mute.expiryState == ExpiryState.OnGoing }.map { mute ->
                        "${mute.banId}(${mute.issuedBy})"
                    }
                    val banEmbed = EmbedBuilder()
                        .setAuthor("Bans Record Report")
                        .setTitle("Bans Record Report for user ${playerToQuery.currentUsername}")
                        .addInlineField("Expired Bans: ", expiredBans.joinToString())
                        .addInlineField("Pardoned Bans: ", pardonedBans.joinToString())
                        .addInlineField("Ongoing Bans: ", ongoingBans.joinToString())
                    Main.getDownloadedSkinAsFile(it)?.let { img ->
                        banEmbed.setImage(img)
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(banEmbed).thenAccept {
                            Main.discordBot.log(
                                LogType.DSCComamndRan,
                                "User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}."
                            )
                            Main.serverLogger.info("User ${sender.discriminatedName} ran ${this.javaClass.name} with arguments ${args.joinToString()}}.")
                        }
                    }
                }
            }
            else -> {
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Query!", 5, message)
                }
                return
            }
        }
        message.addReaction("❌").thenAccept {
            CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
        }

    }
}