package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.Utils.Companion.doesUserHavePermission
import net.pengtul.pengcord.Utils.Companion.unverifyPlayer
import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
        if (args.isEmpty()) {
            Main.database.playerGetByDiscordUUID(sender.id)?.let { player ->
                Bukkit.getPlayer(player.playerUUID)?.let { bukkitPlayer ->
                    if (bukkitPlayer.isOp || bukkitPlayer.hasPermission("pengcord.verify.undoself")) {
                        unverifyPlayer(TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id))
                        Main.removePlayerFromVerifiedCache(bukkitPlayer.uniqueId)
                    }
                }
            }
        } else if (doesUserHavePermission(sender, "pengcord.verify.undoself") && sender is Player) {
            unverifyPlayer(TypeOfUniqueID.DiscordTypeOfUniqueID(sender.id))
            Main.removePlayerFromVerifiedCache(sender.uniqueId)
        }
    }
}