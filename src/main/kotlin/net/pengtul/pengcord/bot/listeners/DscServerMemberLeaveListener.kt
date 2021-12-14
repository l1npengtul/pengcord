package net.pengtul.pengcord.bot.listeners

import net.pengtul.pengcord.data.interact.UpdateVerify
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.LogType
import org.javacord.api.event.server.member.ServerMemberLeaveEvent
import org.javacord.api.listener.server.member.ServerMemberLeaveListener

class DscServerMemberLeaveListener: ServerMemberLeaveListener {
    override fun onServerMemberLeave(event: ServerMemberLeaveEvent?) {
        if (event != null) {
            if (event.server.equals(Main.discordBot.discordServer)) {
                val userId = event.user.id
                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                    Main.database.playerGetByDiscordUUID(userId)?.let { player ->
                        Main.database.playerUpdateVerify(player.playerUUID, UpdateVerify.Unverify)
                        
                        Main.serverLogger.info(LogType.Verification, "Synced Discord Leave for player ${player.currentUsername}(${player.playerUUID}/${player.discordUUID}/${event.user.discriminatedName})")
                    }
                })
            }
        }
    }
}