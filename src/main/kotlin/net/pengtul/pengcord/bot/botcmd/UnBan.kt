package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.Utils.Companion.banPardon
import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class UnBan: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "unban"
    override val commandDescription: String
        get() = "Unbans/Pardons a player from a ban punishment. Pardon either by ban Id or use all <player> to pardon all bans from a player."
    override val commandUsage: String
        get() = "unban <ban Id/all> <player?>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (!doesUserHavePermission(sender, "pengcord.punishment.ban")) {
            message.addReaction("\uD83D\uDEAB").thenAccept {
                
                Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                CommandHelper.deleteAfterSend("\uD83D\uDEAB: You are not a moderator!", 5, message)
            }
            return
        }

        if (args[0].lowercase() == "all") {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val player = queryPlayerFromString(args[1])
                if (player == null) {
                    message.addReaction("❌").thenAccept {
                        CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
                    }
                    return@Runnable
                }
                player.let {
                    Main.database.queryPlayerBansByPlayerMinecraft(player.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }.forEach { ban ->
                        banPardon(ban, pardoned = true)
                        message.addReaction("✅").thenAccept {
                            CommandHelper.deleteAfterSend("§aLifted ban ${ban.banId} for player UUID: ${ban.playerUUID}/Discord: ${ban.discordUUID}!", 5, message)
                            
                            Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                        }
                    }
                }
            })
        } else {
            val banId = args[0].toLong()
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.queryPlayerBanById(banId)?.let { ban ->
                    banPardon(ban, pardoned = true)
                    message.addReaction("✅").thenAccept {
                        CommandHelper.deleteAfterSend("§aLifted ban ${ban.banId} for player UUID: ${ban.playerUUID}/Discord: ${ban.discordUUID}!", 5, message)
                        
                        Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\".")
                    }
                    return@Runnable
                }
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Failed to find player!", 5, message)
                }
            })
        }
    }
}