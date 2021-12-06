package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.bot.commandhandler.JCDiscordCommandExecutor
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.data.interact.UpdateVerify
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.toComponent
import org.bukkit.Bukkit
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

class Verify: JCDiscordCommandExecutor {
    override val commandName: String
        get() = "verify"
    override val commandDescription: String
        get() = "Verifies a player (yourself)"
    override val commandUsage: String
        get() = "verify <minecraft username> <secret key>"

    override fun executeCommand(msg: String, sender: User, message: Message, args: List<String>) {
        val playerId = Bukkit.getPlayer(args[0])
        if (playerId == null) {
            message.addReaction("❌").thenAccept {
                CommandHelper.deleteAfterSend("Invalid Player!", 5, message)
            }
        }
        // check to see if player already verified
        playerId?.let { bukkitPlayer ->
            if (Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(bukkitPlayer.uniqueId))) {
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Player Already Verified!", 5, message)
                }
            }

            val inputKey = args[1].toIntOrNull()
            if (inputKey == null) {
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Invalid Key!", 5, message)
                }
            }

            if (Main.playersAwaitingVerification[inputKey] == bukkitPlayer.uniqueId) {
                Main.database.playerUpdateVerify(bukkitPlayer.uniqueId, UpdateVerify.NewlyVerified(sender.id))
                message.addReaction("\uD83C\uDF89").thenAccept {
                    sender.sendMessage("Welcome to the Server ${bukkitPlayer.name}(${sender.getDisplayName(Main.discordBot.discordServer)})! We hope you enjoy your stay!")
                    Main.pengcord.server.broadcast("§aWelcome ${sender.getDisplayName(Main.discordBot.discordServer)}(${bukkitPlayer.name}) to the server!".toComponent())
                }
            } else {
                message.addReaction("❌").thenAccept {
                    CommandHelper.deleteAfterSend("Make sure you join the server and ran /verify <discord name> first!", 10, message)
                }
            }

        }
    }
}