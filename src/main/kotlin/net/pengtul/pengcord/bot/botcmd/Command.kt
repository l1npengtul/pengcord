package net.pengtul.pengcord.bot.botcmd

import net.pengtul.pengcord.main.Main
import org.bukkit.*
import org.bukkit.plugin.Plugin
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import java.io.File
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit


class Command {

    companion object {
        fun shutdown(shutdownTimer: Long, plugin: Plugin) {
            for (p in Bukkit.getServer().onlinePlayers) {
                p.sendTitle("§c§lServer Shutdown in ${shutdownTimer / 20}s", "§cPlease reconnect to this server soon!", 10, 70, 20)
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 0L)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 8L)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        p.playSound(Location(p.world, p.location.x, p.location.y, p.location.z), Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F)
                    }, 16L)
                })
            }
            Bukkit.getServer().broadcastMessage("§k--------------------------------------------")
            Bukkit.getServer().broadcastMessage("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds).")
            Bukkit.getServer().broadcastMessage("§c§l§nPlease reconnect after the reboot!")
            Bukkit.getServer().broadcastMessage("§k--------------------------------------------")
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                Bukkit.shutdown()
            }, shutdownTimer)
        }

        fun removePlayerfromDiscord(user: User, message: Message): Boolean{
            var ret = false
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                Main.ServerConfig.usersList!!.remove(user.idAsString)
                CommandHelper.deleteAfterSend("Successfully unverified user ${user.idAsString}", 10, message)
                ret = true
            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${user.idAsString}.", 5, message)
            }
            return ret
        }

        fun removePlayerfromDiscord(user: User, message: Message, doKick: Boolean): Boolean{
            var ret = false
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                if (doKick){
                    Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList!![user.idAsString]))?.kickPlayer("Kicked by operator.")
                }
                Main.ServerConfig.usersList!!.remove(user.idAsString)
                CommandHelper.deleteAfterSend("Successfully unverified user ${user.idAsString}", 10, message)
                ret = true
            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${user.idAsString}.", 5, message)
            }
            return ret
        }

        fun removePlayerfromMinecraft(username: String): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                val uuid = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(username))
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    for (key in Main.ServerConfig.usersList!!.keys) {
                        Main.ServerLogger.info("$uuid, $key")
                        if (Main.ServerConfig.usersList!![key] == uuid && key.isNotBlank()){
                            Main.ServerLogger.info("$uuid, $key")
                            Main.ServerConfig.usersList!!.remove(key)
                            ret = true
                        }
                    }
                })
            }
            return ret
        }

        fun removePlayerfromMinecraft(username: String, doKick: Boolean): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                val uuid = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(username))
                Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                    for (key in Main.ServerConfig.usersList!!.keys) {
                        Main.ServerLogger.info("$uuid, $key")
                        if (Main.ServerConfig.usersList!![key] == uuid && key.isNotBlank()){
                            Main.ServerLogger.info("$uuid, $key")
                            if (doKick){
                                Bukkit.getScheduler().runTask(it, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(uuid))?.kickPlayer("Kicked by operator.")
                                })
                            }
                            Main.ServerConfig.usersList!!.remove(key)
                            ret = true
                        }
                    }
                })
            }
            return ret
        }

        fun banUsingDiscord(user: User, message: Message, days: Int?, reason: String?, userToBan: User){
            var banUntil: Date? = Date()
            if (days != null){
                if (banUntil != null) {
                    if (days <= 0){
                        banUntil = null
                    }
                    else{
                        banUntil.time = banUntil.time + TimeUnit.DAYS.toMillis(days.toLong())
                    }
                }
            }
            else {
                banUntil = null
            }

            val username: String = Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList?.getValue(user.idAsString)!!).username

            if (Main.ServerConfig.usersList?.containsKey(userToBan.idAsString)!!) {
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        username,
                        reason,
                        banUntil,
                        user.discriminatedName
                )
                if (banUntil == null){
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        Bukkit.getScheduler().runTask(it, Runnable {
                            Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Permanent Ban.")
                            if (removePlayerfromMinecraft(username)){
                                Main.discordBot.sendMessageToDiscord("Banned and removed user $username")
                            }
                        })
                    }
                }
                else {
                    Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {
                        Bukkit.getScheduler().runTask(it, Runnable {
                            Bukkit.getServer().getPlayer(UUID.fromString(Main.ServerConfig.usersList?.getValue(userToBan.idAsString)!!))?.kickPlayer("Banned by ${user.discriminatedName} for $reason. Banned until $banUntil")
                            if (removePlayerfromMinecraft(username)){
                                Main.discordBot.sendMessageToDiscord("Banned and removed user $username")
                            }
                        })
                    }
                }
            }
            else {
                CommandHelper.deleteAfterSend("Error: Could not find ${userToBan.idAsString}.", 5, message)
            }
        }

        fun banUsingMinecraft(banUUID: String, reason: String?, days: Int?, banUser: String?): Boolean{
            var ret = false
            Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let {plugin ->
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    for (key in Main.ServerConfig.usersList?.keys!!) {
                        if (Main.ServerConfig.usersList!![key] == banUUID && key.isNotBlank()) {
                            var banUntil: Date? = Date()
                            if (days != null){
                                if (banUntil != null) {
                                    if (days <= 0){
                                        banUntil = null
                                    }
                                    else{
                                        banUntil.time = banUntil.time + TimeUnit.DAYS.toMillis(days.toLong())
                                    }
                                }
                            }
                            else {
                                banUntil = null
                            }

                            val username : String = Main.mojangAPI.getPlayerProfile(banUUID).username

                            Bukkit.getBanList(BanList.Type.NAME).addBan(
                                    username,
                                    reason,
                                    banUntil,
                                    banUser
                            )
                            if (banUntil == null) {
                                Bukkit.getScheduler().runTask(plugin, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(banUUID))?.kickPlayer("Banned by $banUser for $reason. Permanent Ban.")
                                    ret = removePlayerfromMinecraft(username)
                                })

                            } else {
                                Bukkit.getScheduler().runTask(plugin, Runnable {
                                    Bukkit.getServer().getPlayer(UUID.fromString(banUUID))?.kickPlayer("Banned by $banUser for $reason. Banned until $banUntil")
                                    ret = removePlayerfromMinecraft(username)
                                })
                            }
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
                            hash[sender.idAsString] = player.uniqueId.toString()
                            Main.ServerLogger.info("[pengcord]: Registered User with discord id ${sender.idAsString} with UUID ${player.uniqueId}")
                            CommandHelper.deleteAfterSend("Successfully Verified!", 8, message)
                            player.sendTitle("§aYou have been verified!", "§aGLHF!", 10, 70, 20)
                            player.playSound(Location(player.world, player.location.x, player.location.y, player.location.z), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F)
                            if (player.isInvulnerable) {
                                player.isInvulnerable = false
                            }
                            Main.playersToVerify.remove(sender.idAsString)
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
            try {
                val user: User = message.mentionedUsers[0]
                val reason: String? = mArray[2]
                if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                    banUsingDiscord(sender, message, mArray[3].toInt(), reason, user)
                }
            }
            catch (e: Exception){
                val user: User = message.mentionedUsers[0]
                if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                    banUsingDiscord(sender, message, null, null, user)
                }
            }
        }
        else if (message.mentionedUsers.size == 0) {
            try {
                val playerUUID: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(mArray[1]))
                val reason: String? = mArray[2]
                val suc = banUsingMinecraft(playerUUID, reason, mArray[3].toInt(), sender.discriminatedName)
                if (suc){
                    message.channel.sendMessage("Successfully Banned player ${mArray[1]}")
                }
            }
            catch (e: Exception){
                val playerUUID: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(mArray[1]))
                val suc = banUsingMinecraft(playerUUID, "", null, sender.discriminatedName)
                if (suc){
                    message.channel.sendMessage("Successfully Banned player ${mArray[1]}")
                }
            }
        }
    }

    fun stop(msg: String, sender: User, message: Message) {
        if (doesUserHavePermission(sender, message)){
            val mArray: List<String> = msg.split(" ")
            val shutdownTimer = try {
                mArray[1].toLong() * 20L
            } catch (e: Exception) {
                200L
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
        else {
            CommandHelper.deleteAfterSend("You do not have permission to run this command", 5, message)
        }
    }

    fun whoIs(msg: String, sender : User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        if (message.mentionedUsers.size == 1) {
            val user: User = message.mentionedUsers[0]
            if (Main.ServerConfig.usersList?.containsKey(user.idAsString)!!) {
                val player: String = Main.mojangAPI.getPlayerProfile(Main.ServerConfig.usersList!!.getValue(user.idAsString)).username
                val uuid: String = Main.mojangAPI.getUUIDOfUsername(player)
                //Main.downloadSkinUUID(Main.insertDashUUID(uuid))
                Main.ServerLogger.info("aaa")
                Main.ServerLogger.info(Main.mojangAPI.getPlayerProfile(uuid).username)
                Main.ServerLogger.info(player)
                Main.ServerLogger.info(user.discriminatedName)
                Main.ServerLogger.info(user.idAsString)
                Main.ServerLogger.info(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png").absolutePath)
                val embed = EmbedBuilder()
                        .setAuthor("Discord whois lookup")
                        .setTitle("Whois for user ${user.discriminatedName} .")
                        //.setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${Main.insertDashUUID(uuid)}.png"))
                        .addField("Discord UUID:", user.idAsString + " .")
                        .addInlineField("Minecraft Username:", "$player .")
                        .addInlineField("Minecraft UUID:", "${Main.insertDashUUID(uuid)} .")
                //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                message.serverTextChannel.ifPresent {
                    Main.ServerLogger.info("aaa")
                    it.sendMessage(embed)
                }

            }
        }
        else if (message.mentionedUsers.size == 0) {
            val player: String = Main.insertDashUUID(Main.mojangAPI.getUUIDOfUsername(mArray[1]))
            if (Main.ServerConfig.usersList?.containsValue(player)!!) {
                Bukkit.getServer().pluginManager.getPlugin("pengcord")?.let { plugin ->
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                        Main.ServerLogger.info("aaa")
                        for (key in Main.ServerConfig.usersList?.keys!!) {
                            if (Main.ServerConfig.usersList!!.getValue(key) == player && key.isNotBlank()) {
                                Main.ServerLogger.info(key)
                                val user: User = Main.discordBot.discordApi.getUserById(key).join()
                                Main.downloadSkinUUID(Main.insertDashUUID(player))
                                Main.ServerLogger.info(Main.mojangAPI.getPlayerProfile(player).username)
                                Main.ServerLogger.info(player)
                                Main.ServerLogger.info(user.discriminatedName)
                                Main.ServerLogger.info(key)
                                Main.ServerLogger.info(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png").absolutePath)
                                val embed: EmbedBuilder = EmbedBuilder()
                                        .setAuthor("Discord whois lookup")
                                        .setTitle("Whois for user ${Main.mojangAPI.getPlayerProfile(player).username} .")
                                        //.setThumbnail(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player}.png"))
                                        .addField("Minecraft UUID:", "$player .")
                                        .addInlineField("Discord Username: ", "${user.discriminatedName} .")
                                        .addInlineField("Discord UUID:", "$key .")
                                //.addInlineField("LuckPerms Prefix:", getPlayerPrefix(player.uniqueId.toString()))
                                message.serverTextChannel.ifPresent {
                                    Main.ServerLogger.info("aaa")
                                    it.sendMessage(embed)
                                }
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


    fun kick(msg: String, sender: User, message: Message) {
        val mArray: List<String> = msg.split(" ")
        if (doesUserHavePermission(sender, message)){
            if (message.mentionedUsers.size == 1){
                val user: User = message.mentionedUsers[0]

                removePlayerfromDiscord(user, message, true)
                Main.discordBot.sendMessageToDiscord("Successfully Unverified ${user.discriminatedName}")
            }
            else if (message.mentionedUsers.size == 0){
                try {
                    removePlayerfromMinecraft(mArray[1], true)
                    Main.discordBot.sendMessageToDiscord("Successfully Unverified ${mArray[1]}")
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


