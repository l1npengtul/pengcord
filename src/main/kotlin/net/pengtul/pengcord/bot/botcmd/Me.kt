package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.Utils
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import org.joda.time.Duration
import org.joda.time.Period

class Me: JCDiscordCommandExecutor {
    override val commandDescription: String
        get() = "Gets Info on yourself (Only if you are verified on server)"
    override val commandName: String
        get() = "me"
    override val commandUsage: String
        get() = "me"

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
                        .addInlineField("Time Played:", Main.periodFormatter.print(Period(Duration(time * 1000))))
                    Main.getDownloadedSkinAsFile(dbPlayer.playerUUID)?.let {
                        userInfoEmbed.setImage(it)
                    }
                    if (dbPlayer.firstJoinDateTime != Main.neverHappenedDateTime) {
                        userInfoEmbed.addInlineField("First Join Date Time:", "${dbPlayer.firstJoinDateTime}")
                    }
                    if (dbPlayer.verifiedDateTime != Main.neverHappenedDateTime) {
                        userInfoEmbed.addInlineField("Latest Verification Date Time:", "${dbPlayer.verifiedDateTime}")
                    }
                    if (Utils.doesUserHavePermission(sender, "pengcord.command.ignorelist")) {
                        val sendComponent = mutableListOf<String>()
                        Main.database.queryIgnoresBySourcePlayerUUID(dbPlayer.playerUUID).forEach { ignore ->
                            Main.database.playerGetByUUID(ignore.target)?.let {
                                sendComponent.add(
                                    "${it.currentUsername}(${ignore.ignoreId})"
                                )
                            }
                        }
                        userInfoEmbed.addField("User Ignores:", sendComponent.toString())
                    }
                    message.addReaction("✅").thenAccept {
                        message.reply(userInfoEmbed).thenAccept {
                            Main.serverLogger.info(LogType.DSCComamndRan,"User ${sender.discriminatedName} ran command `me`.")
                            Main.discordBot.logEmbed(userInfoEmbed)
                        }
                    }
                    return@Runnable
                }
                message.addReaction("❌").thenAccept {
                    
                    CommandHelper.deleteAfterSend("Could not find your played time!", 5, message)
                }
                return@Runnable
            }
            message.addReaction("❌").thenAccept {
                
                CommandHelper.deleteAfterSend("Could not find you! Are you verified on the server?", 5, message)
            }
            return@Runnable
        })
    }
}