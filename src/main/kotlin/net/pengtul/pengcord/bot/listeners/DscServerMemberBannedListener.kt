package net.pengtul.pengcord.bot.listeners

import net.pengtul.pengcord.util.Utils.Companion.banPlayer
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.data.interact.UpdateVerify
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.LogType
import org.javacord.api.event.server.member.ServerMemberBanEvent
import org.javacord.api.listener.server.member.ServerMemberBanListener

class DscServerMemberBannedListener: ServerMemberBanListener {
    override fun onServerMemberBan(event: ServerMemberBanEvent?) {
        event?.let { serverMemberBanEvent ->
            if (serverMemberBanEvent.server.equals(Main.discordBot.discordServer)) {
                serverMemberBanEvent.requestBan().thenAccept { ban ->
                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                        Main.database.playerGetByDiscordUUID(ban.user.id)?.let { player ->
                            Main.database.playerUpdateVerify(player.playerUUID, UpdateVerify.Unverify)
                            val banReason = ban.reason.orElse("")
                            val lastPlayerBan = Main.database.queryPlayerBansByPlayerDiscord(player.discordUUID).lastOrNull() ?: return@Runnable
                            if (lastPlayerBan.reason != banReason) {
                                banPlayer(player, TypeOfUniqueID.Unknown("Discord Ban, Check Server Audit Log"), ExpiryDateTime.Permanent, banReason)
                            }
                            Main.serverLogger.info(LogType.PlayerBanned, "Synced Discord ban for player ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}/${ban.user.discriminatedName})")
                        }
                    })
                }
            }
        }
    }
}