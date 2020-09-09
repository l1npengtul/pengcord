package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.io.File
import java.util.*

class Command {
    fun bindCommand(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ")
        try {
            if (!Main.ServerConfig.adminList.isNullOrEmpty()){
                var userRoles = ""
                message.server.get().getHighestRole(sender).ifPresent { role ->
                    userRoles = role.idAsString
                }
                if(Main.ServerConfig.adminList!!.contains(userRoles)){
                    when (mArray[1]) {
                        "sync" -> {
                            Main.ServerConfig.syncChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `sync` channel.", 8, message)
                        }
                        "command" -> {
                            Main.ServerConfig.commandChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `command` channel.", 8, message)
                        }
                        "admin" -> {
                            Main.ServerConfig.adminChannel = message.channel.idAsString
                            Main.ServerConfig.serverBind = message.server.ifPresent { }.toString()
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `admin` channel.", 8, message)
                        }
                        else -> {
                            CommandHelper.deleteAfterSend("Usage: ${Main.ServerConfig.botPrefix}bind <sync/command/admin>", 5, message)
                        }
                    }
                }
                else if (message.server.get().isAdmin(sender) && Main.ServerConfig.adminNoRole) {
                    when (mArray[1]) {
                        "sync" -> {
                            Main.ServerConfig.syncChannel = message.channel.idAsString
                            message.server.ifPresent { server -> Main.ServerConfig.serverBind = server.idAsString }
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `sync` channel.", 8, message)
                        }
                        "command" -> {
                            Main.ServerConfig.commandChannel = message.channel.idAsString
                            message.server.ifPresent { server -> Main.ServerConfig.serverBind = server.idAsString }
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `command` channel.", 8, message)
                        }
                        "admin" -> {
                            Main.ServerConfig.adminChannel = message.channel.idAsString
                            message.server.ifPresent { server -> Main.ServerConfig.serverBind = server.idAsString }
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                            CommandHelper.deleteAfterSend("Successfully set this channel as the `admin` channel.", 8, message)
                        }
                        else -> {
                            CommandHelper.deleteAfterSend("Usage: ${Main.ServerConfig.botPrefix}bind <sync/command/admin>", 5, message)
                        }
                    }
                }
                else {
                    CommandHelper.deleteAfterSend("Inadequate Permissions to execute this command.", 5, message)
                }
            }
        }
        catch (e: Exception){
            CommandHelper.deleteAfterSend("An Exception occurred in your request. ```$e```", 8, message)
        }
        //CommandHelper.deleteAfterSend("",8, message)
    }

    fun verifyCommand(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ")
        Bukkit.getServer().getPlayer(mArray[1])?.let{
            if (Main.playersToVerify.containsValue(it.uniqueId.toString()) && Main.playersToVerify.containsKey(sender.idAsString)){
                Main.ServerConfig.usersList?.let {
                    Bukkit.getServer().getPlayer(mArray[1])?.let { player ->
                        if (!it.containsValue(player.uniqueId.toString()) && !it.containsKey(sender.idAsString)){
                            it.put(sender.idAsString, player.uniqueId.toString())
                            Main.ServerLogger.info("[pengcord]: Registered User with discord id ${sender.idAsString} with UUID ${player.uniqueId}")
                            CommandHelper.deleteAfterSend("Successfully Verified!", 8, message)
                            if(player.isInvulnerable){
                                player.isInvulnerable = false
                            }
                            return
                        }
                        else {
                            CommandHelper.deleteAfterSend("You already exist! No need to verify. Please ask for help if this is a mistake.", 8, message)
                            return
                        }
                    }
                    CommandHelper.deleteAfterSend("You need to provide a valid username and be logged into the server.", 8, message)
                }
            }
            else {
                CommandHelper.deleteAfterSend("Who are you? Make sure you already did `/verify <discord tag>` in the minecraft server first!", 8, message)
            }
        }
        CommandHelper.deleteAfterSend("Couldn't find you ${mArray[1]}! Make sure you are logged into the minecraft server!", 5, message)
    }

    fun ban(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
    }

    fun stop(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        var shutdownTimer: Long = 0;
        try{
            shutdownTimer = mArray[1].toLong() * 20L
        }
        catch (e: Exception){

        }

        message.server?.let {
            it.ifPresent { server ->
                server.getHighestRole(sender).ifPresent { role ->
                    if (Main.ServerConfig.adminList?.contains(role.idAsString)!!){
                        Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                            for (p in Bukkit.getServer().onlinePlayers){
                                p.sendTitle("Server Shutdown in ${shutdownTimer / 20}s", "Please reconnect to this server soon!", 10, 70, 20)
                                p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                            }
                            Bukkit.getServer().broadcastMessage("§k------------------------")
                            Bukkit.getServer().broadcastMessage("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds).")
                            Bukkit.getServer().broadcastMessage("§c§l§nPlease reconnect after the reboot!.")
                            Bukkit.getServer().broadcastMessage("§k------------------------")
                            Bukkit.getScheduler().runTaskLaterAsynchronously(it, Runnable {
                                Bukkit.shutdown()
                            }, shutdownTimer)
                        }
                    }
                    else if (server.isAdmin(sender) && Main.ServerConfig.adminNoRole){
                        Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                            for (p in Bukkit.getServer().onlinePlayers){
                                p.sendTitle("Server Shutdown in ${shutdownTimer / 20}s", "Please reconnect to this server soon!", 10, 70, 20)
                                p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                            }
                            Bukkit.getServer().broadcastMessage("§k------------------------")
                            Bukkit.getServer().broadcastMessage("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds).")
                            Bukkit.getServer().broadcastMessage("§c§l§nPlease reconnect after the reboot!.")
                            Bukkit.getServer().broadcastMessage("§k------------------------")
                            Bukkit.getScheduler().runTaskLaterAsynchronously(it, Runnable {
                                Bukkit.shutdown()
                            }, shutdownTimer)
                        }
                    }
                }
            }
        }
    }

    fun whoIs(msg: String, sender: User, message: Message){
        val mArray: List<String> = msg.split(" ")
        if (message.mentionedUsers.size == 1){
            val user: User = message.mentionedUsers[0]
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!){
                val player: Player? = Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList!![user.idAsString]))
                if (player != null){
                    var embed = EmbedBuilder()
                            .setAuthor("Discord whois lookup")
                            .setTitle("Whois for user ${user.discriminatedName}")
                            .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player.uniqueId}.png"))
                            .addField("Discord UUID:", user.idAsString)
                            .addInlineField("Minecraft Username:", player.name)
                            .addInlineField("Minecraft UUID:", player.uniqueId.toString())
                            //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                    message.serverTextChannel.ifPresent {
                        it.sendMessage(embed)
                    }
                }
                else {
                    CommandHelper.deleteAfterSend("Player does not exist or has bypass permissions.", 8, message)
                }
            }
        }
        else if (message.mentionedUsers.size == 0){
            val player: Player? = Bukkit.getServer().getPlayer(mArray[1])
            if (player != null) {
                if (Main.ServerConfig.usersList?.containsValue(player.uniqueId.toString())!!){
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                            for (key in Main.ServerConfig.usersList?.keys!!) {
                                if (Main.ServerConfig.usersList!![key] == player.uniqueId.toString()) {
                                    val user: User = Main.discordBot.discordApi.getUserById(key).join()
                                    var embed = EmbedBuilder()
                                            .setAuthor("Discord whois lookup")
                                            .setTitle("Whois for user ${player.name}")
                                            .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player.uniqueId}.png"))
                                            .addField("Minecraft UUID:", player.uniqueId.toString())
                                            .addInlineField("Discord Username:", user.discriminatedName.toString())
                                            .addInlineField("Discord UUID:", key)
                                            //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                                    message.serverTextChannel.ifPresent {
                                        it.sendMessage(embed)
                                    }
                                    return@Runnable
                                }
                            }
                        })
                    }
                }
                else {
                    CommandHelper.deleteAfterSend("Player does not exist or has bypass permissions.", 8, message)
                }
            }
        }
        else {
            CommandHelper.deleteAfterSend("Please enter a valid person to look up! Usage: ${Main.ServerConfig.botPrefix}whois <discord user mention> ", 5, message)
        }
    }
    fun getPlayerPrefix(uuid: String): String? {
        var user = Main.luckPerms.userManager.getUser(uuid)
        user?.let {
            var context = Main.luckPerms.contextManager.getContext(it)
            var meta = it.cachedData.metaData
            return meta.prefix
        }
        return null
    }
}

