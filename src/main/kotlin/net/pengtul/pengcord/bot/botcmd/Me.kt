package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User

class Me: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets Info on yourself (Only if you are verified on server)"
    override val commandName: String
        get() = "info"
    override val commandUsage: String
        get() = "info"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
            Main.database.playerGetByDiscordUUID(sender.id)?.let { dbPlayer ->
                Main.database.playerGetCurrentTimePlayed(player = dbPlayer.playerUUID).onSuccess { time ->
                    val userInfoEmbed = EmbedBuilder()
                        .setAuthor("Player Information for Discord User ${sender.discriminatedName}")
                        .setTitle("Player Info:")
                        .addInlineField("Minecraft Username/UUID:", "${dbPlayer.currentUsername}/${dbPlayer.playerUUID}")
                        .addInlineField("Discord UUID:", "${dbPlayer.discordUUID}")
                        .addInlineField("Banned Status:", "${dbPlayer.isBanned}")
                        .addInlineField("Muted Status:", "${dbPlayer.isMuted}")
                        .addInlineField("Deaths:", "${dbPlayer.deaths}")
                    if (dbPlayer.firstJoinDateTime != Main.neverHappenedDateTime) {
                        userInfoEmbed.addInlineField("First Join Date Time:", "${dbPlayer.firstJoinDateTime}")
                    }
                    if (dbPlayer.verifiedDateTime != Main.neverHappenedDateTime) {
                        userInfoEmbed.addInlineField("Latest Verification Date Time:", "${dbPlayer.verifiedDateTime}")
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(userInfoEmbed).thenAccept {
                            Main.discordBot.log(LogType.DSCComamndRan,"User ${sender.discriminatedName} ran command `me`.")
                            Main.discordBot.logEmbed(userInfoEmbed)
                            Main.serverLogger.info("[pengcord]: User ${sender.discriminatedName} ran command `me`.")
                        }
                    }
                }
                message.addReaction("❌").thenAccept {
                    Main.discordBot.log(LogType.DSCComamndError,"Could not get user played time.")
                    CommandHelper.deleteAfterSend("Could not find your played time!", 5, message)
                }
            }
            message.addReaction("❌").thenAccept {
                Main.discordBot.log(LogType.DSCComamndError,"Invalid User.")
                CommandHelper.deleteAfterSend("Could not find you! Are you verified on the server?", 5, message)
            }
        })
    }
}