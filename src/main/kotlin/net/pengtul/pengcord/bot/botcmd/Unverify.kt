package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.util.LogType
import net.pengtul.pengcord.util.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.util.Utils.Companion.unverifyPlayer
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.Utils.Companion.queryPlayerFromString
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Unverify: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "unverify"
    override val commandDescription: String
        get() = "Unverifies a player."
    override val commandUsage: String
        get() = "unveify <player?>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        if (args.isNotEmpty() && doesUserHavePermission(sender, "pengcord.verify.undo")) {
//            Main.database.playerGetByDiscordUUID(sender.id)?.let { player ->
//                Bukkit.getPlayer(player.playerUUID)?.let { bukkitPlayer ->
//                    if (bukkitPlayer.isOp || bukkitPlayer.hasPermission("pengcord.verify.undoself")) {
//                        unverifyPlayer(TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id))
//                        Main.removePlayerFromVerifiedCache(bukkitPlayer.uniqueId)
//                    }
//                }
//            }
            val playerToUnverify = queryPlayerFromString(args[0])
            if (playerToUnverify != null && !doesUserHavePermission(playerToUnverify.playerUUID, "pengcord.verify.undo")) {
                playerToUnverify.let {
                    unverifyPlayer(TypeOfUniqueID.MinecraftTypeOfUniqueID(it.playerUUID))
                    Main.removePlayerFromVerifiedCache(it.playerUUID)
                    return
                }
            } else {
                message.addReaction("\uD83D\uDEAB").thenAccept {
                    
                    Main.serverLogger.info("User ${sender.discriminatedName} ran `${this.javaClass.name}` with args \"${args[0]}\". Failed due to invalid permissions.")
                    CommandHelper.deleteAfterSend("\uD83D\uDEAB: Cannot mute another moderator!", 5, message)
                }
            }

        } else if (doesUserHavePermission(sender, "pengcord.verify.undoself")) {
            unverifyPlayer(TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id))
            Main.database.playerGetByDiscordUUID(sender.id)?.let {
                Main.removePlayerFromVerifiedCache(it.playerUUID)
            }
        }
    }
}