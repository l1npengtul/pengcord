package net.pengtul.pengcord

import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.data.interact.UpdateVerify
import net.pengtul.pengcord.data.schema.Ban
import net.pengtul.pengcord.data.schema.Mute
import net.pengtul.pengcord.data.schema.Player
import net.pengtul.pengcord.main.Main
import org.bukkit.*
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import org.joda.time.Duration
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.*

/*
*    This code creates and defines all discord commands
*    Copyright (C) 2020  Lewis Rho
*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */



class Utils {

    companion object {
        fun doesUserHavePermission(user: User): Boolean {
            val botServerId = (Main.serverConfig.botServer ?: return false)
            val server = Main.discordBot.discordApi.getServerById(botServerId) ?: return false
            val mServer = if (server.isPresent) server.orElseThrow() else return false
            var ret = false

            mServer.getMemberById(user.id).ifPresent { serverUser ->
                serverUser.getRoles(mServer).forEach { role ->
                    if (Main.serverConfig.discordAdminRoles.contains(role.id)) {
                        ret = true
                    }
                }
            }

            return ret
        }

        fun queryPlayerFromString(user: String): Player? {
            // Discriminated Name
            if (user.contains("#")) {
                val discordServerId = Main.serverConfig.botServer ?: return null
                val maybeServer = Main.discordBot.discordApi.getServerById(discordServerId) ?: return null
                lateinit var usr: User
                maybeServer.ifPresent { server ->
                    server.getMemberByDiscriminatedNameIgnoreCase(user).ifPresent { u ->
                        usr = u
                    }
                }
                return try {
                    Main.database.playerGetByDiscordUUID(usr.id)
                } catch (_: Exception) {
                    null
                }
            }
            // Minecraft UUID with dash
            else if (user.contains("-")) {
                val noDashUUID = user.replace("-", "")
                return Main.database.playerGetByUUID(UUID.fromString(noDashUUID))
            }
            // Discord or Minecraft UUID
            else if (user.toIntOrNull() != null) {
                return Main.database.playerGetByDiscordUUID(user.toLong()) ?: Main.database.playerGetByUUID(UUID.fromString(user))
            }
            // Minecraft Username
            else {
                return Main.database.playerGetByCurrentName(user)
            }
        }

        fun shutdown(shutdownTimer: Long) {
            val scheduler = Main.scheduler
            val pengcord = Main.pengcord
            val server = Bukkit.getServer()
            server.showTitle(
                Title.title(Component.text("§c§lServer Shutdown in ${shutdownTimer / 20}s"), Component.text("§cPlease reconnect to this server soon!"), Title.DEFAULT_TIMES)
            )
            scheduler.runTaskLater(pengcord, Runnable {
                server.playSound(net.kyori.adventure.sound.Sound.sound(Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F))
            }, 0L)
            scheduler.runTaskLater(pengcord, Runnable {
                server.playSound(net.kyori.adventure.sound.Sound.sound(Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F))
            }, 8L)
            scheduler.runTaskLater(pengcord, Runnable {
                server.playSound(net.kyori.adventure.sound.Sound.sound(Sound.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F))
            }, 16L)

            server.sendMessage(Component.text("§k--------------------------------------------------------------"), MessageType.SYSTEM)
            server.sendMessage(Component.text("§c§l§nThis minecraft server is restarting soon! (${shutdownTimer / 20} seconds)."), MessageType.SYSTEM)
            server.sendMessage(Component.text("§c§l§nPlease reconnect after the reboot!"), MessageType.SYSTEM)
            server.sendMessage(Component.text("§k--------------------------------------------------------------"), MessageType.SYSTEM)

            scheduler.runTaskLaterAsynchronously(pengcord, Runnable {
                Bukkit.shutdown()
            }, shutdownTimer)
        }

        fun unverifyPlayer(player: TypeOfUniqueID) {
            when (player) {
                is TypeOfUniqueID.DiscordTypeOfUniqueID -> {
                    unverifyPlayerDiscord(player.uuid)
                }
                is TypeOfUniqueID.MinecraftTypeOfUniqueID -> {
                    unverifyPlayerMinecraft(player.uuid)
                }
                else -> {
                    // Silently Fail
                }
            }
        }

        private fun unverifyPlayerDiscord(player: Long) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerGetByDiscordUUID(player)?.let { player ->
                    Main.database.playerUpdateVerify(player.playerUUID, UpdateVerify.Unverify)
                    Bukkit.getPlayer(player.playerUUID)?.kick(Component.text("Unverify"))
                    Main.serverLogger.info("Sucessfully unverified player ${player.currentUsername} (${player.playerUUID})")
                    Main.discordBot.log("Sucessfully unverified player ${player.currentUsername} (${player.playerUUID})")
                }
            })
        }

        private fun unverifyPlayerMinecraft(player: UUID) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerGetByUUID(player)?.let { player ->
                    Main.database.playerUpdateVerify(player.playerUUID, UpdateVerify.Unverify)
                    Bukkit.getPlayer(player.playerUUID)?.kick(Component.text("Unverify"))
                    Main.serverLogger.info("Sucessfully unverified player ${player.currentUsername} (${player.playerUUID})")
                    Main.discordBot.log("Sucessfully unverified player ${player.currentUsername} (${player.playerUUID})")
                }
            })
        }

        fun warnPlayer(player: UUID, issuer: TypeOfUniqueID, reason: String) {
            when (issuer) {
                is TypeOfUniqueID.DiscordTypeOfUniqueID -> {
                    Main.discordBot.discordApi.getUserById(issuer.uuid).thenAccept { user ->
                        warnPlayerInDiscord(player, user, reason)
                    }
                }
                is TypeOfUniqueID.MinecraftTypeOfUniqueID -> {
                    warnPlayerInMinecraft(player, issuer.uuid, reason)
                }
                is TypeOfUniqueID.Unknown -> {

                }
            }
        }

        private fun warnPlayerInMinecraft(player: UUID, issuer: UUID, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.addWarnToPlayerMinecraft(player, issuer, reason)
                Main.scheduler.runTask(Main.pengcord, Runnable {
                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                        Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                                warnPlayerDiscord(dbPlayer, issuer.toString(), reason)
                            }
                            Main.discordBot.logEmbed(warnEmbedGenerator(dbPlayer, issuer.toString(), reason))
                        }
                    })
                    Bukkit.getPlayer(player)?.kick(Component.text(reason))
                })
            })
        }

        private fun warnPlayerInDiscord(player: UUID, issuer: User, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.addWarnToPlayerDiscord(player, issuer, reason)
                Main.scheduler.runTask(Main.pengcord, Runnable {
                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                        Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                                warnPlayerDiscord(dbPlayer, issuer.discriminatedName, reason)
                            }
                            Main.discordBot.logEmbed(warnEmbedGenerator(dbPlayer, issuer.toString(), reason))
                        }
                    })
                    Bukkit.getPlayer(player)?.kick(Component.text(reason))
                })
            })
        }

        private fun warnPlayerInUnknown(player: UUID, issuer: String, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.addWarnToPlayerUnknown(player, issuer, reason)
                Main.scheduler.runTask(Main.pengcord, Runnable {
                    Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                        Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                                warnPlayerDiscord(dbPlayer, issuer, reason)
                            }
                            Main.discordBot.logEmbed(warnEmbedGenerator(dbPlayer, issuer, reason))
                        }
                    })
                    Bukkit.getPlayer(player)?.kick(Component.text(reason))
                })
            })
        }

        private fun warnPlayerDiscord(player: Player, issuer: String, reason: String) {
            Main.discordBot.discordApi.getUserById(player.discordUUID).thenAccept { user ->
                user.sendMessage("You were muted by $issuer because of $reason")
            }
        }

        private fun warnEmbedGenerator(player: Player, issuer: String, reason: String): EmbedBuilder {
            val syncedToDiscord = Main.serverConfig.enableCrossMinecraftDiscordModeration
            Main.downloadSkinUUID(player.playerUUID)
            return EmbedBuilder().setAuthor("User Unverified from Server")
                .setTitle("User Warned")
                .addInlineField("Report","Warned player ${player.currentUsername} (UUID: ${player.playerUUID}, Discord ${player.discordUUID}}")
                .addInlineField("Warned By", issuer)
                .addInlineField("Warned User", player.currentUsername)
                .addInlineField("Reason", reason)
                .addInlineField("Synced To Discord", "$syncedToDiscord")
                .setColor(Color.red)
                .setImage(Main.getDownloadedSkinAsFile(player.playerUUID))
        }

        fun mutePlayer(player: Player, issuer: TypeOfUniqueID, until: ExpiryDateTime, reason: String) {
            when (issuer) {
                is TypeOfUniqueID.DiscordTypeOfUniqueID -> {
                    Main.discordBot.discordApi.getUserById(issuer.uuid).thenAccept { user ->
                        mutePlayerInDiscord(player.playerUUID, user, until, reason)
                    }
                }
                is TypeOfUniqueID.MinecraftTypeOfUniqueID -> {
                    mutePlayerInMinecraft(player.playerUUID, issuer.uuid, until, reason)
                }
                is TypeOfUniqueID.Unknown -> {

                }
            }

            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                mutePlayerDiscord(player, reason)
            }
        }

        private fun mutePlayerInMinecraft(player: UUID, issuer: UUID, until: ExpiryDateTime, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                    }
                    Main.database.addMuteToPlayerMinecraft(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnmuteTask(id)
                    }
                }
            })
        }

        private fun mutePlayerInDiscord(player: UUID, issuer: User, until: ExpiryDateTime, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            Main.serverLogger.info("User ${issuer.discriminatedName} (${issuer.id}) banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${issuer.discriminatedName}/${issuer.id}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            Main.serverLogger.info("User ${issuer.discriminatedName} (${issuer.id}) banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${issuer.discriminatedName}/${issuer.id}", until, reason))
                        }
                    }
                    Main.database.addMutePlayerDiscord(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnmuteTask(id)
                    }
                }
            })
        }

        private fun mutePlayerInUnknown(player: UUID, issuer: String, until: ExpiryDateTime, reason: String) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(muteEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                    }
                    Main.database.addMuteToPlayerUnknown(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnmuteTask(id)
                    }
                }
            })
        }

        private fun muteEmbedGenerator(player: Player, issuer: String, until: ExpiryDateTime, reason: String): EmbedBuilder {
            val syncedToDiscord = Main.serverConfig.enableCrossMinecraftDiscordModeration
            Main.downloadSkinUUID(player.playerUUID)
            return EmbedBuilder().setAuthor("User Muted")
                .setTitle("User Muted Report")
                .addInlineField("Report","Mute Report for player ${player.currentUsername} (UUID: ${player.playerUUID}, Discord ${player.discordUUID}}")
                .addInlineField("Muted By", issuer)
                .addInlineField("Muted User", player.currentUsername)
                .addInlineField("Until", "$until")
                .addInlineField("Reason", reason)
                .addInlineField("Synced To Discord", "$syncedToDiscord")
                .setColor(Color.red)
                .setImage(Main.getDownloadedSkinAsFile(player.playerUUID))
        }

        private fun mutePlayerDiscord(player: Player, reason: String) {
            val botServerId = (Main.serverConfig.botServer ?: return)
            val server = Main.discordBot.discordApi.getServerById(botServerId) ?: return
            val mServer = if (server.isPresent) server.orElseThrow() else return
            mServer.getRoleById(Main.serverConfig.discordMutedRole).ifPresent { muteRole ->
                mServer.getMemberById(player.discordUUID).ifPresent { user ->
                    mServer.addRoleToUser(user, muteRole).thenAccept {
                        Main.discordBot.log("[pengcord]: [DISCORD-MUTED]: User ${player.discordUUID} muted due to ban $reason")
                        Main.serverLogger.info("[DISCORD-MUTED]: User ${player.discordUUID} muted due to ban $reason")
                    }
                }
            }
        }

        fun pardonMute(mute: Mute, pardoned: Boolean = false) {
            val expiry = if (pardoned) ExpiryState.Pardoned else ExpiryState.Expired

            // Invalidate Current mute
            Main.database.pardonMute(mute.muteId, pardoned)
            // Check if other mutes exist
            Main.database.playerUpdateCurrentUsername(mute.playerUUID)
            val currentPlayer = Main.database.playerGetByUUID(mute.playerUUID) ?: return
            val otherMutes = Main.database.queryPlayerMutesByPlayerMinecraft(mute.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }
            // There are no other mutes so we can remove player punishments.
            if (otherMutes.isEmpty()) {
                Main.discordBot.log("[pengcord]: [MINECRAFT-UNBAN]: User ${currentPlayer.currentUsername} (${mute.playerUUID}) unmuted due to ban $expiry")
                Main.serverLogger.info("[MINECRAFT-UNBAN]: User ${currentPlayer.currentUsername} (${mute.playerUUID}) unmuted due to ban $expiry")


                // Discord Mute removal
                if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                    val botServerId = (Main.serverConfig.botServer ?: return)
                    val server = Main.discordBot.discordApi.getServerById(botServerId) ?: return
                    val mServer = if (server.isPresent) server.orElseThrow() else return
                    mServer.getRoleById(Main.serverConfig.discordMutedRole).ifPresent { muteRole ->
                        mServer.getMemberById(mute.discordUUID).ifPresent { user ->
                            mServer.removeRoleFromUser(user, muteRole).thenAccept {
                                Main.discordBot.log("[pengcord]: [DISCORD-UNMUTE]: User ${mute.discordUUID} unmuted due to ban $expiry")
                                Main.serverLogger.info("[DISCORD-UNMUTE]: User ${mute.discordUUID} unmuted due to ban $expiry")
                            }
                        }
                    }
                }
            }
        }

        fun banPlayer(player: Player, issuer: TypeOfUniqueID, until: ExpiryDateTime, reason: String) {
            when (issuer) {
                is TypeOfUniqueID.DiscordTypeOfUniqueID -> {
                    Main.discordBot.discordApi.getUserById(issuer.uuid).thenAccept { user ->
                        banPlayerInDiscord(player.playerUUID, user, until, reason)
                    }
                }
                is TypeOfUniqueID.MinecraftTypeOfUniqueID -> {
                    banPlayerInMinecraft(player.playerUUID, issuer.uuid, until, reason)
                }
                is TypeOfUniqueID.Unknown -> {

                }
            }

            if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                banPlayerDiscord(player, reason)
            }
        }

        private fun banPlayerInMinecraft(player: UUID, issuer: UUID, until: ExpiryDateTime, reason: String) {
            val banList = Bukkit.getBanList(BanList.Type.NAME)

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerUpdateVerify(player, UpdateVerify.Unverify)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            banList.addBan(dbPlayer.currentUsername, reason, until.time.toDate(), issuer.toString())
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            banList.addBan(dbPlayer.currentUsername, reason, null, issuer.toString())
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                    }
                    Main.database.addBanToPlayerMinecraft(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnbanTask(id)
                    }
                    Main.scheduler.runTask(Main.pengcord, Runnable {
                        Bukkit.getPlayer(player)?.kick(Component.text(reason))
                    })
                }
            })
        }

        private fun banPlayerInDiscord(player: UUID, issuer: User, until: ExpiryDateTime, reason: String) {
            val banList = Bukkit.getBanList(BanList.Type.NAME)

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerUpdateVerify(player, UpdateVerify.Unverify)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            banList.addBan(dbPlayer.currentUsername, reason, until.time.toDate(), issuer.toString())
                            Main.serverLogger.info("User ${issuer.discriminatedName} (${issuer.id}) banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${issuer.discriminatedName}/${issuer.id}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            banList.addBan(dbPlayer.currentUsername, reason, null, issuer.toString())
                            Main.serverLogger.info("User ${issuer.discriminatedName} (${issuer.id}) banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${issuer.discriminatedName}/${issuer.id}", until, reason))
                        }
                    }
                    Main.database.addBanPlayerDiscord(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnbanTask(id)
                    }
                    Main.scheduler.runTask(Main.pengcord, Runnable {
                        Bukkit.getPlayer(player)?.kick(Component.text(reason))
                    })
                }
            })
        }

        private fun banPlayerInUnknown(player: UUID, issuer: String, until: ExpiryDateTime, reason: String) {
            val banList = Bukkit.getBanList(BanList.Type.NAME)

            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                Main.database.playerUpdateCurrentUsername(player)
                Main.database.playerUpdateVerify(player, UpdateVerify.Unverify)
                Main.database.playerGetByUUID(player)?.let { dbPlayer ->
                    when (until) {
                        is ExpiryDateTime.DateAndTime -> {
                            banList.addBan(dbPlayer.currentUsername, reason, until.time.toDate(), issuer)
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) until ${until.time} for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                        is ExpiryDateTime.Permanent -> {
                            banList.addBan(dbPlayer.currentUsername, reason, null, issuer)
                            Main.serverLogger.info("User $issuer banned player ${dbPlayer.currentUsername} ($player) permanently for $reason")
                            Main.discordBot.logEmbed(banEmbedGenerator(dbPlayer, "${Main.mojangAPI.getPlayerProfile(Main.insertDashUUID(issuer.toString()))}/${issuer}", until, reason))
                        }
                    }
                    Main.database.addBanPlayerUnknown(player, issuer, until, reason).onSuccess { id ->
                        Main.startUnbanTask(id)
                    }
                    Main.scheduler.runTask(Main.pengcord, Runnable {
                        Bukkit.getPlayer(player)?.kick(Component.text(reason))
                    })
                }
            })
        }

        private fun banEmbedGenerator(player: Player, issuer: String, until: ExpiryDateTime, reason: String): EmbedBuilder {
            val syncedToDiscord = Main.serverConfig.enableCrossMinecraftDiscordModeration
            Main.downloadSkinUUID(player.playerUUID)
            return EmbedBuilder().setAuthor("User Banned from Server")
                .setTitle("User Banned")
                .addInlineField("Report","Ban Report for player ${player.currentUsername} (UUID: ${player.playerUUID}, Discord ${player.discordUUID}}")
                .addInlineField("Banned By", issuer)
                .addInlineField("Banned User", player.currentUsername)
                .addInlineField("Until", "$until")
                .addInlineField("Reason", reason)
                .addInlineField("Synced To Discord", "$syncedToDiscord")
                .setColor(Color.red)
                .setImage(Main.getDownloadedSkinAsFile(player.playerUUID))
        }

        private fun banPlayerDiscord(player: Player, reason: String) {
            val botServerId = (Main.serverConfig.botServer ?: return)
            val server = Main.discordBot.discordApi.getServerById(botServerId) ?: return
            val mServer = if (server.isPresent) server.orElseThrow() else return

            mServer.banUser(player.discordUUID, Main.serverConfig.discordBanDeleteMessageDays, reason)
            Main.serverLogger.info("Synced ban for user ${player.currentUsername}. (Discord: ${player.discordUUID})")
            Main.discordBot.log("Synced ban for user ${player.currentUsername}. (Discord: ${player.discordUUID})")
        }

        fun banPardon(ban: Ban, pardoned: Boolean = false) {
            val expiry = if (pardoned) ExpiryState.Pardoned else ExpiryState.Expired

            // Invalidate Current Ban
            Main.database.pardonBan(ban.banId, pardoned)
            // Check if other bans exist
            Main.database.playerUpdateCurrentUsername(ban.playerUUID)
            val currentPlayer = Main.database.playerGetByUUID(ban.playerUUID) ?: return
            val otherBans = Main.database.queryPlayerBansByPlayerMinecraft(ban.playerUUID).filter { it.expiryState == ExpiryState.OnGoing }
            // There are no other bans so we can remove player punishments.
            if (otherBans.isEmpty()) {
                // MC ban removal only matters if they were pardoned.
                if (pardoned) {
                    val banList = Bukkit.getBanList(BanList.Type.NAME)
                    banList.pardon(currentPlayer.currentUsername)
                    Main.discordBot.log("[pengcord]: [MINECRAFT-UNBAN]: User ${currentPlayer.currentUsername} (${ban.playerUUID}) unbanned due to ban $expiry")
                    Main.serverLogger.info("[MINECRAFT-UNBAN]: User ${currentPlayer.currentUsername} (${ban.playerUUID}) unbanned due to ban $expiry")
                }

                // Discord Ban removal
                if (Main.serverConfig.enableCrossMinecraftDiscordModeration) {
                    val botServerId = (Main.serverConfig.botServer ?: return)
                    val server = Main.discordBot.discordApi.getServerById(botServerId) ?: return
                    val mServer = if (server.isPresent) server.orElseThrow() else return
                    mServer.unbanUser(ban.discordUUID).thenAccept {
                        Main.discordBot.log("[pengcord]: [DISCORD-UNBAN]: User ${ban.discordUUID} unbanned due to ban $expiry")
                        Main.serverLogger.info("[DISCORD-UNBAN]: User ${ban.discordUUID} unbanned due to ban $expiry")
                    }
                }
            }
        }

        fun getUptime(): String {
            return Main.periodFormatter.print(Duration(ManagementFactory.getRuntimeMXBean().uptime).toPeriod())
        }
    }
}

fun String.intOrString() = toIntOrNull() ?: this