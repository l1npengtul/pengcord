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
import org.joda.time.Duration
import org.joda.time.Period

class WhoIs: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "whois"
    override val commandDescription: String
        get() = "Look up someone by their Discord Username/Discord UUID/Minecraft Username/Minecraft UUID"
    override val commandUsage: String
        get() = "whois <player>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        val toQuery = queryPlayerFromString(args[0])
        if (toQuery == null) {
            message.addReaction("❌").thenAccept {
                CommandHelper.deleteAfterSend("❌: Cannot find player!", 5, message)
            }
            return
        }
        toQuery.let { player ->
            val hasPermission = doesUserHavePermission(sender, "pengcord.command.whois")
            val playerFilterAlerts = Main.database.queryPlayerFilterAlertsByPlayerMinecraft(player.playerUUID)
            val playerMutes = Main.database.queryPlayerMutesByPlayerMinecraft(player.playerUUID)
            val activePlayerMutes = Main.database.queryPlayerMutesByPlayerMinecraft(player.playerUUID).filter {
                it.expiryState == ExpiryState.OnGoing
            }

            val playerWarns = Main.database.queryPlayerWarnsByPlayerMinecraft(player.playerUUID)

            val playerBans = Main.database.queryPlayerBansByPlayerMinecraft(player.playerUUID)
            val activePlayerBans = Main.database.queryPlayerBansByPlayerMinecraft(player.playerUUID).filter {
                it.expiryState == ExpiryState.OnGoing
            }
            Main.database.playerGetCurrentTimePlayed(player = player.playerUUID).onSuccess { time ->
                val userInfoEmbed = EmbedBuilder()
                    .setAuthor("Player Information for Discord User ${sender.discriminatedName}")
                    .setTitle("Player Info:")
                    .addInlineField("Minecraft Username/UUID:", "${player.currentUsername}/${player.playerUUID}")
                    .addInlineField("Discord UUID:", "${player.discordUUID}")
                    .addInlineField("Deaths:", "${player.deaths}")
                    .addInlineField("Time Played:", Main.periodFormatter.print(Period(Duration(time * 1000))))
                if (player.firstJoinDateTime != Main.neverHappenedDateTime) {
                    userInfoEmbed.addInlineField("First Join Date Time:", "${player.firstJoinDateTime}")
                }
                if (player.verifiedDateTime != Main.neverHappenedDateTime) {
                    userInfoEmbed.addInlineField("Latest Verification Date Time:", "${player.verifiedDateTime}")
                }
                if (hasPermission) {
                    userInfoEmbed.addField("# Of Filter Alerts:", "${playerFilterAlerts.count()}")
                    userInfoEmbed.addField("# Of Warns:", "${playerWarns.count()}")
                    userInfoEmbed.addField("Muted Status:", "${player.isMuted}")
                    userInfoEmbed.addField("# Of Mutes:", "${activePlayerMutes.count()} active, ${playerMutes.count()} total")
                    userInfoEmbed.addField("Banned Status:", "${player.isBanned}")
                    userInfoEmbed.addField("# Of Bans:", "${activePlayerBans.count()} active, ${playerBans.count()} total")
                }
                Main.getDownloadedSkinAsFile(player.playerUUID)?.let {
                    userInfoEmbed.setImage(it)
                }
                message.addReaction("✅").thenAccept {
                    message.reply(userInfoEmbed).thenAccept {
                        Main.discordBot.log(LogType.DSCComamndRan,"User ${sender.discriminatedName} ran command `me`.")
                        Main.discordBot.logEmbed(userInfoEmbed)
                        Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran command `me`.")
                    }
                }
            }
        }
    }
}