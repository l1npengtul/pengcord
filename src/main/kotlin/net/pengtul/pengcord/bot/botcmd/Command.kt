package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.Embed
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.io.File
import java.util.*


class Command {

    companion object {
        fun shutdown(shutdownTimer: Long, plugin: Plugin) {
            for (p in Bukkit.getServer().onlinePlayers) {
                p.sendTitle("Server Shutdown in ${shutdownTimer / 20}s", "Please reconnect to this server soon!", 10, 70, 20)
                p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
            }
            Bukkit.getServer().broadcastMessage("§k------------------------")
            Bukkit.getServer().broadcastMessage("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds).")
            Bukkit.getServer().broadcastMessage("§c§l§nPlease reconnect after the reboot!.")
            Bukkit.getServer().broadcastMessage("§k------------------------")
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                Bukkit.shutdown()
            }, shutdownTimer)
        }

        fun removePlayerfromDiscord(user: User, message: Message){
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                Main.ServerConfig.usersList!!.remove(user.idAsString)
                CommandHelper.deleteAfterSend("Successfully unverified user ${user.idAsString}", 10, message)
            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${user.idAsString}.", 5, message)
            }
        }

        fun removePlayerfromMinecraft(username: String){
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                val uuid = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(username))
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    var pl: OfflinePlayer
                    val plUID: UUID = UUID.fromString(uuid)
                    for (key in Main.ServerConfig.usersList!!.keys) {
                        pl = Bukkit.getOfflinePlayer(UUID.fromString(Main.ServerConfig.usersList!!.get(key)))
                        if (pl.hasPlayedBefore()) {
                            if (pl.uniqueId == plUID) {
                                Main.ServerConfig.usersList!!.remove(key)
                                Main.discordBot.sendMessageToDiscord("Successfully removed ${pl.name} (UUID ${pl.uniqueId}, DSC $key) from the list of verified users.")
                            }
                        }
                    }
                })
            }
        }

        fun banUsingDiscord(user: User, message: Message, days: Int?, reason: String?, userToBan: User){
            var BanUntil: Date? = Date()
            val c = Calendar.getInstance()
            c.time = BanUntil
            if (days != null) {
                c.add(Calendar.DATE, days)
            }

            if (days == 0){
                BanUntil = null
            }

            if (Main.ServerConfig.usersList?.containsKey(userToBan.idAsString)!!) {
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList?.getValue(user.idAsString)!!).username,
                        reason,
                        BanUntil,
                        user.discriminatedName
                )
                if (BanUntil == null){
                    Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Permanent Ban.")
                }
                else {
                    Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Banned for $BanUntil days.")
                }
                Main.ServerConfig.usersList!!.remove(userToBan.idAsString)

            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${userToBan.idAsString}.", 5, message)
            }

        }

        fun banUsingMinecraft(banUUID: String, reason: String?, days: Int, banUser: String?): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    for (key in Main.ServerConfig.usersList?.keys!!) {
                        if (Main.ServerConfig.usersList!![key] == banUUID){
                            var BanUntil: Date? = Date()
                            val c = Calendar.getInstance()
                            c.time = BanUntil
                            c.add(Calendar.DATE, days)

                            if (days >= 0){
                                BanUntil = null
                            }
                            Bukkit.getBanList(BanList.Type.NAME).addBan(
                                    Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList?.getValue(banUUID)!!).username,
                                    reason,
                                    BanUntil,
                                    banUser
                            )
                            if (BanUntil == null){
                                Bukkit.getServer().getPlayer(banUUID)?.kickPlayer("Banned by $banUser for $reason. Permanent Ban.")
                            }
                            else {
                                Bukkit.getServer().getPlayer(banUUID)?.kickPlayer("Banned by $banUser for $reason. Banned for $BanUntil days.")
                            }
                            Main.ServerConfig.usersList!!.remove(key)
                            ret = true
                        }
                    }
                })
            }
            return ret
        }


    }

    fun bindCommand(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        try {
            if (doesUserHavePermission(sender, message)){
                when (mArray[1]) {
                    "sync" -> {
                        Main.ServerConfig.syncChannel = message.channel.idAsString
                        message.server.ifPresent { server ->
                            Main.ServerConfig.serverBind = server.idAsString
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        }
                        Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        CommandHelper.deleteAfterSend("Successfully set this channel as the `sync` channel.", 8, message)
                    }
                    "command" -> {
                        Main.ServerConfig.commandChannel = message.channel.idAsString
                        message.server.ifPresent { server ->
                            Main.ServerConfig.serverBind = server.idAsString
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        }
                        Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        CommandHelper.deleteAfterSend("Successfully set this channel as the `command` channel.", 8, message)
                    }
                    "admin" -> {
                        Main.ServerConfig.adminChannel = message.channel.idAsString
                        message.server.ifPresent { server ->
                            Main.ServerConfig.serverBind = server.idAsString
                            Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        }
                        Main.ServerLogger.info(Main.ServerConfig.serverBind)
                        CommandHelper.deleteAfterSend("Successfully set this channel as the `admin` channel.", 8, message)
                    }
                    else -> {
                        CommandHelper.deleteAfterSend("Usage: ${Main.ServerConfig.botPrefix}bind <sync/command/admin>", 5, message)
                    }
                }
            }
        } catch (e: Exception) {
            CommandHelper.deleteAfterSend("An Exception occurred in your request. ```$e```", 8, message)
        }
        //CommandHelper.deleteAfterSend("",8, message)
    }

    fun verifyCommand(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        Bukkit.getServer().getPlayer(mArray[1])?.let {
            if (Main.playersToVerify.containsValue(it.uniqueId.toString()) && Main.playersToVerify.containsKey(sender.idAsString)) {
                Main.ServerConfig.usersList?.let { hash ->
                    Bukkit.getServer().getPlayer(mArray[1])?.let { player ->
                        if (!(hash.containsValue(player.uniqueId.toString()) && hash.containsKey(sender.idAsString))) {
                            hash.put(sender.idAsString, player.uniqueId.toString())
                            Main.ServerLogger.info("[pengcord]: Registered User with discord id ${sender.idAsString} with UUID ${player.uniqueId}")
                            CommandHelper.deleteAfterSend("Successfully Verified!", 8, message)
                            player.sendTitle("You have been verified!", "GLHF!", 10, 70, 20)
                            player.playSound(Location(player.world, player.location.x, player.location.y, player.location.z), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F)
                            if (player.isInvulnerable) {
                                player.isInvulnerable = false
                            }
                            return
                        } else {
                            CommandHelper.deleteAfterSend("You already exist! No need to verify. Please ask for help if this is a mistake.", 8, message)
                            return
                        }
                    }
                    CommandHelper.deleteAfterSend("You need to provide a valid username and be logged into the server.", 8, message)
                }
            } else {
                CommandHelper.deleteAfterSend("Who are you? Make sure you already did `/verify <discord tag>` in the minecraft server first!", 8, message)
            }
        }
        CommandHelper.deleteAfterSend("Couldn't find you ${mArray[1]}! Make sure you are logged into the minecraft server!", 5, message)
    }

    fun banDiscord(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        if (message.mentionedUsers.size == 1) {
            //
            val user: User = message.mentionedUsers[0]
            val reason: String? = mArray[2]
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                banUsingDiscord(sender, message, mArray[3].toInt(), reason, user)
            }
        }
        else if (message.mentionedUsers.size == 0) {
            val playerUUID: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(mArray[1]))
            val reason: String? = mArray[2]
            banUsingMinecraft(playerUUID, reason, mArray[3].toInt(), sender.discriminatedName)
        }
    }

    fun stop(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        var shutdownTimer: Long = 0;
        try {
            shutdownTimer = mArray[1].toLong() * 20L
        } catch (e: Exception) {
            shutdownTimer = 200L
        }

        message.server?.let { optional ->
            optional.ifPresent {
                if (doesUserHavePermission(sender, message)){
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        shutdown(shutdownTimer, it)
                    }
                }
            }
        }
    }

    fun whoIs(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        if (message.mentionedUsers.size == 1) {
            val user: User = message.mentionedUsers[0]
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                val player: String = Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList!!.getValue(user.idAsString)).username
                val uuid: String = Main.mojangAPI.getUUIDOfUsername(player)
                //val bukkitPlayer: OfflinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(Main.insertDashUUID(uuid)))
                Main.downloadSkinUUID(Main.insertDashUUID(uuid))
                Main.ServerLogger.info("aaa")
                val embed = EmbedBuilder()
                        .setAuthor("Discord whois lookup")
                        .setTitle("Whois for user ${user.discriminatedName}")
                        .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${Main.insertDashUUID(uuid)}.png"))
                        .addField("Discord UUID:", user.idAsString + " ")
                        .addInlineField("Minecraft Username:", "$player ")
                        .addInlineField("Minecraft UUID:", "${Main.insertDashUUID(uuid)} ")
                //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                message.serverTextChannel.ifPresent {
                    it.sendMessage(embed)
                }

            }
        }
        else if (message.mentionedUsers.size == 0) {
            val player: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(mArray[1]))
            if (Main.ServerConfig.usersList?.containsValue(player)!!) {
                Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                    Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                        Main.ServerLogger.info("aaa")
                        for (key in Main.ServerConfig.usersList?.keys!!) {
                            if (Main.ServerConfig.usersList!![key] == player) {
                                val user: User = Main.discordBot.discordApi.getUserById(key).join()
                                var embed: EmbedBuilder = EmbedBuilder()
                                        .setAuthor("Discord whois lookup")
                                        .setTitle("Whois for user ${Main.mojangAPI.getPlayerProfile(player).username}")
                                        .setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png"))
                                        .addField("Minecraft UUID:", "$player ")
                                        .addInlineField("Discord Username:", user.discriminatedName.toString() + " ")
                                        .addInlineField("Discord UUID:", "$key ")
                                //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                                Main.discordBot.sendEmbedToDiscord(embed)
                                return@Runnable
                            }
                        }
                    })
                }
            } else {
                CommandHelper.deleteAfterSend("Player does not exist or has bypass permissions.", 8, message)
            }

        } else {
            CommandHelper.deleteAfterSend("Please enter a valid person to look up! Usage: ${Main.ServerConfig.botPrefix}whois <discord user mention> ", 5, message)
        }
    }


    fun unVerify(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        if (doesUserHavePermission(sender, message)){
            if (message.mentionedUsers.size == 1){
                val user: User = message.mentionedUsers[0]
                removePlayerfromDiscord(user, message)
            }
            else if (message.mentionedUsers.size == 0){
                try {
                    removePlayerfromMinecraft(mArray[1])
                }
                catch (e: Exception){
                    CommandHelper.deleteAfterSend("Could not find user ${mArray[1]}.", 5, message)
                }
            }
        }
    }

    private fun doesUserHavePermission(user: User, message: Message): Boolean {
        return if (!Main.ServerConfig.adminList.isNullOrEmpty()) {
            var userRoles = ""
            message.server.get().getHighestRole(user).ifPresent { role ->
                userRoles = role.idAsString
            }
            Main.ServerConfig.adminList!!.contains(userRoles)
        } else {
            message.server.get().isAdmin(user) && Main.ServerConfig.adminNoRole
        }
    }
}


