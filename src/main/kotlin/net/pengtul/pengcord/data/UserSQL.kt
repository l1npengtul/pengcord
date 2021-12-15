package net.pengtul.pengcord.data

import com.comphenix.protocol.PacketType
import net.pengtul.pengcord.data.interact.ExpiryDateTime
import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.data.interact.TypeOfUniqueID
import net.pengtul.pengcord.data.interact.UpdateVerify
import net.pengtul.pengcord.data.schema.*
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.main.Main.Companion.uuidToString
import org.javacord.api.entity.user.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.Duration
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

// WARNING: Every one of these must be done **inside** of a AsyncRun in bukkit!
class UserSQL {
    private val database: Database = Database.connect(url = "jdbc:h2:./plugins/pengcord/pengcordstore;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    init {
        transaction(database) {
            val databaseDir = File(Main.pengcord.dataFolder.path+File.pathSeparator+"pengcordstore.mv.db")
            Main.serverLogger.info(Main.pengcord.dataFolder.path)
            if (databaseDir.isFile) {
                SchemaUtils.createMissingTablesAndColumns(Players, Warns, Mutes, Bans, FilterAlerts, Ignores)
            } else {
                SchemaUtils.create(Players, Warns, Mutes, Bans, FilterAlerts, Ignores)
            }
        }
    }

    fun playerNew(uuid: UUID) {
        val username = Main.mojangAPI.getPlayerProfile(uuidToString(uuid)).username
        transaction(this.database) {
            // Check if player exists, if they do return early
            val checkExistanceQuery = Players.select { Players.playerUUID eq uuid }
            if (checkExistanceQuery.empty()) {
                Main.serverLogger.info("Attempt add $username (${uuid}) to table. Creating user.")
                Players.insert {
                    it[playerUUID] = uuid
                    it[currentUsername] = username
                    it[firstJoinDateTime] = DateTime.now()
                }
            } else {
                Main.serverLogger.info("Attempt add $username (${uuid}) to table. Already exists.")
                Players.update ({Players.playerUUID eq uuid}) {
                    it[currentUsername] = username
                }
            }
        }
    }

    fun queryAllPlayers(): List<Player> {
        return transaction(this.database) {
            return@transaction Players.selectAll().map {
                playerFromResultRow(it)
            }
        }
    }

    fun playerIsVerified(uuid: TypeOfUniqueID): Boolean {
        return transaction(this.database) {
            return@transaction when(uuid) {
                is TypeOfUniqueID.DiscordTypeOfUniqueID -> {
                    try {
                        Players.select { Players.discordUUID eq uuid.uuid }.single()[Players.verifiedDateTime] != Main.neverHappenedDateTime
                    } catch (_: Exception) {
                        false
                    }
                }
                is TypeOfUniqueID.MinecraftTypeOfUniqueID -> {
                    try {
                        Players.select { Players.playerUUID eq uuid.uuid }.single()[Players.verifiedDateTime] != Main.neverHappenedDateTime
                    } catch (_: Exception) {
                        false
                    }
                }
                is TypeOfUniqueID.Unknown -> {
                    // Silently Fail
                    false
                }
            }
        }
    }

    fun playerUpdateVerify(uuid: UUID, status: UpdateVerify) {
        transaction(this.database) {
            when (status) {
                is UpdateVerify.NewlyVerified -> {
                    Players.update ({Players.playerUUID eq uuid}) {
                        it[discordUUID] = status.discordUUID
                        it[verifiedDateTime] = DateTime.now()
                    }
                }
                is UpdateVerify.Unverify -> {
                    Players.update ({Players.playerUUID eq uuid}) {
                        it[verifiedDateTime] = Main.neverHappenedDateTime
                    }
                }
            }
        }
    }

    fun playerUpdateCurrentUsername(uuid: UUID) {
        transaction(this.database) {
            val username = Main.mojangAPI.getPlayerProfile(uuidToString(uuid)).username

            Players.update ({Players.playerUUID eq uuid}) {
                it[currentUsername] = username
            }
        }
    }

    fun playerGetByDiscordUUID(uuid: Long): Player? {
        return transaction(this.database) {
            try {
                val resultrow = Players.select { Players.discordUUID eq uuid }.single()
                return@transaction playerFromResultRow(resultrow)
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun playerGetByUUID(uuid: UUID): Player? {
        return transaction(this.database) {
            try {
                return@transaction playerFromResultRow(Players.select { Players.playerUUID eq uuid }.single())
            } catch (e: Exception) {
                return@transaction null
            }
        }
    }

    fun playerUpdateTimePlayed(player: UUID) {
        transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { dbPlayer ->
                Main.playersCurrentJoinTime[player]?.let { joinTime ->
                    val joinToNow = Duration(joinTime.millis, DateTime.now().millis).plus(dbPlayer.secondsPlayed * 1000).toPeriod().seconds.toLong()
                    val upUntilNow = try {
                        Players.select {Players.playerUUID eq player}.single()[Players.secondsPlayed]
                    } catch (_: Exception) {
                        0
                    }
                    Players.update ({Players.playerUUID eq player}) {
                        it[secondsPlayed] = joinToNow + upUntilNow
                    }
                    Main.playersCurrentJoinTime[player] = DateTime.now()
                }
            }
        }
    }

    fun playerGetCurrentTimePlayed(player: UUID): Result<Long> {
        this.playerUpdateTimePlayed(player)
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { dbPlayer ->
                return@transaction Result.success(dbPlayer.secondsPlayed)
            }
            return@transaction Result.failure(IllegalArgumentException("Player not found!"))
        }
    }

    fun playerGetByCurrentName(player: String): Player? {
        return transaction(this.database) {
            try {
                return@transaction playerFromResultRow(Players.select {Players.currentUsername eq player}.single())
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun playerDied(player: UUID) {
        transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { dbPlayer ->
                Players.update ({Players.playerUUID eq player}) {
                    it[deaths] = dbPlayer.deaths + 1
                }
            }
        }
    }

    fun playerGetDeaths(player: UUID): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { dbPlayer ->
                return@transaction Result.success(dbPlayer.deaths)
            }
            return@transaction Result.failure(IllegalArgumentException("Player not found!"))
        }
    }

    fun queryPlayerWarns(): List<Warn> {
        return transaction(this.database) {
            val warns = mutableListOf<Warn>()
            Warns.selectAll().forEach { row ->
                warns.add(
                    warnFromResultRow(row)
                )
            }
            return@transaction warns
        }
    }

    fun queryPlayerWarnsByPlayerMinecraft(player: UUID): List<Warn> {
        return transaction(this.database) {
            val warns = mutableListOf<Warn>()
            Warns.select { Warns.playerUUID eq player }.forEach { row ->
                warns.add(
                    warnFromResultRow(row)
                )
            }
            return@transaction warns
        }
    }

    fun queryPlayerWarnsByPlayerDiscord(user: Long): List<Warn> {
        return transaction(this.database) {
            val warns = mutableListOf<Warn>()
            Warns.select { Warns.discordUUID eq user }.forEach { row ->
                warns.add(
                    warnFromResultRow(row)
                )
            }
            return@transaction warns
        }
    }

    fun queryPlayerWarnById(warn: Long): Warn? {
        return transaction(this.database) {
            try {
                return@transaction warnFromResultRow(Warns.select { Warns.warnId eq warn }.single())
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun addWarnToPlayerMinecraft(player: UUID, issuer: UUID, r: String): Result<Unit> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                val issuerUsername = Main.mojangAPI.getPlayerProfile(uuidToString(issuer))
                var rsn = r
                if (rsn.length > 256) {
                    rsn = rsn.substring(256)
                }

                Warns.insert {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "M ${issuerUsername.username} ${issuerUsername.uuid}"
                    it[issuedOn] = DateTime.now()
                    it[reason] = rsn
                }
                return@transaction Result.success(Unit)
            }
            return@transaction Result.failure(IllegalArgumentException("Failed to find user to add warn to!"))
        }
    }

    fun addWarnToPlayerDiscord(player: UUID, issuer: User, r: String): Result<Unit> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                var rsn = r
                if (rsn.length > 256) {
                    rsn = rsn.substring(256)
                }

                Warns.insert {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "D ${issuer.discriminatedName} ${issuer.id}"
                    it[issuedOn] = DateTime.now()
                    it[reason] = rsn
                }
                return@transaction Result.success(Unit)
            }
            return@transaction Result.failure(IllegalArgumentException("Failed to find user to add warn to!"))
        }
    }

    fun addWarnToPlayerUnknown(player: UUID, issuer: String, r: String): Result<Unit> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                var rsn = r
                if (rsn.length > 256) {
                    rsn = rsn.substring(256)
                }

                Warns.insert {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "U $issuer"
                    it[issuedOn] = DateTime.now()
                    it[reason] = rsn
                }
                return@transaction Result.success(Unit)
            }
            return@transaction Result.failure(IllegalArgumentException("Failed to find user to add warn to!"))
        }
    }

    fun queryPlayerFilterAlerts(): List<FilterAlert> {
        return transaction(this.database) {
            val filterAlerts = mutableListOf<FilterAlert>()
            FilterAlerts.selectAll().forEach { row ->
                filterAlerts.add(
                    filterAlertFromResultRow(row)
                )
            }

            return@transaction filterAlerts
        }
    }

    fun queryPlayerFilterAlertsByPlayerMinecraft(player: UUID): List<FilterAlert> {
        return transaction(this.database) {
            val filterAlerts = mutableListOf<FilterAlert>()
            FilterAlerts.select { FilterAlerts.playerUUID eq player }.forEach { row ->
                filterAlerts.add(
                    filterAlertFromResultRow(row)
                )
            }

            return@transaction filterAlerts
        }
    }

    fun queryPlayerFilterAlertsByPlayerDiscord(user: Long): List<FilterAlert> {
        return transaction(this.database) {
            val filterAlerts = mutableListOf<FilterAlert>()
            FilterAlerts.select { FilterAlerts.discordUUID eq user }.forEach { row ->
                filterAlerts.add(
                    filterAlertFromResultRow(row)
                )
            }

            return@transaction filterAlerts
        }
    }

    fun queryPlayerFilterAlertById(id: Long): FilterAlert? {
        return transaction(this.database) {
            try {
                return@transaction filterAlertFromResultRow(FilterAlerts.select { FilterAlerts.filterAlertId eq id }.single())
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun addFilterAlertToPlayer(player: UUID, w: String, c: String): Result<Unit> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                var ctx = c
                if (ctx.length > 256) {
                    ctx = ctx.substring(256)
                }

                var wd = w
                if (wd.length > 50) {
                    wd = wd.substring(50)
                }

                FilterAlerts.insert {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedOn] = DateTime.now()
                    it[context] = ctx
                    it[word] = wd
                }

                return@transaction Result.success(Unit)
            }
            return@transaction Result.failure(IllegalArgumentException("Player Not Found"))
        }
    }

    fun checkIfPlayerMuted(uuid: UUID): Boolean {
        return transaction(this.database) {
            try {
                return@transaction Players.select { Players.playerUUID eq uuid }.single()[Players.isMuted]
            } catch (_: Exception) {
                false
            }
        }
    }

    fun queryPlayerMutes(): List<Mute> {
        return transaction(this.database) {
            val mutes = mutableListOf<Mute>()
            Mutes.selectAll().forEach { row ->
                mutes.add(
                    muteFromResultRow(row)
                )
            }

            return@transaction mutes
        }
    }

    fun queryPlayerMuteById(id: Long): Mute? {
        return transaction(this.database) {
            try {
                return@transaction muteFromResultRow(Mutes.select { Mutes.muteId eq id }.single())
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun queryPlayerMutesByPlayerMinecraft(player: UUID): List<Mute> {
        return transaction(this.database) {
            val mutes = mutableListOf<Mute>()
            Mutes.select { Mutes.playerUUID eq player }.forEach { row ->
                mutes.add(muteFromResultRow(row))
            }

            return@transaction mutes
        }
    }

    fun queryPlayerMutesByPlayerDiscord(player: Long): List<Mute> {
        return transaction(this.database) {
            val mutes = mutableListOf<Mute>()
            Mutes.select { Mutes.discordUUID eq player }.forEach { row ->
                mutes.add(muteFromResultRow(row))
            }

            return@transaction mutes
        }
    }

    fun queryPlayerMutesByStatus(status: ExpiryState): List<Mute> {
        return transaction(this.database) {
            val mutes = mutableListOf<Mute>()
            Mutes.select { Mutes.expiryState eq status }.forEach { row ->
                mutes.add(muteFromResultRow(row))
            }

            return@transaction mutes
        }
    }

    fun addMuteToPlayerMinecraft(player: UUID, issuer: UUID, expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                val issuerUsername = Main.mojangAPI.getPlayerProfile(uuidToString(issuer))
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Mutes.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "M ${issuerUsername.username} ${issuerUsername.uuid}"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isMuted] = true
                }

                return@transaction Result.success(id.value)
            }
            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun addMutePlayerDiscord(player: UUID, issuer: User, expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Mutes.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "D ${issuer.discriminatedName} ${issuer.id}"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isMuted] = true
                }

                return@transaction Result.success(id.value)
            }

            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun addMuteToPlayerUnknown(player: UUID, issuer: String, expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Mutes.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "U $issuer"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isMuted] = true
                }

                return@transaction Result.success(id.value)
            }
            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun pardonMute(mute: Long, pardon: Boolean = false) {
        transaction(this.database) {
            val expiry = if (pardon) ExpiryState.Pardoned else ExpiryState.Expired

            val thisMute = this@UserSQL.queryPlayerMuteById(mute) ?: return@transaction

            // concurrency check
            if (thisMute.expiryState == ExpiryState.OnGoing) {
                Mutes.update ({Mutes.muteId eq mute}) {
                    it[expiryState] = expiry
                }
            }
            // update muted status
            val mutes = Mutes.select { Mutes.playerUUID eq thisMute.playerUUID }.count()
            if (mutes == 0L) {
                Players.update ({ Players.playerUUID eq thisMute.playerUUID }) {
                    it[isMuted] = false
                }
            }
        }
    }


    fun checkIfPlayerBanned(uuid: UUID): Boolean {
        return transaction(this.database) {
            try {
                return@transaction Players.select { Players.playerUUID eq uuid }.single()[Players.isBanned]
            } catch (_: Exception) {
                false
            }
        }
    }


    fun queryPlayerBans(): List<Ban> {
        return transaction(this.database) {
            val bans = mutableListOf<Ban>()
            Bans.selectAll().forEach { row ->
                bans.add(
                    banFromResultRow(row)
                )
            }

            return@transaction bans
        }
    }

    fun queryPlayerBanById(id: Long): Ban? {
        return transaction(this.database) {
            try {
                return@transaction banFromResultRow(Bans.select { Bans.banId eq id }.single())
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun queryPlayerBansByPlayerMinecraft(player: UUID): List<Ban> {
        return transaction(this.database) {
            val bans = mutableListOf<Ban>()
            Bans.select { Bans.playerUUID eq player }.forEach { row ->
                bans.add(banFromResultRow(row))
            }

            return@transaction bans
        }
    }

    fun queryPlayerBansByPlayerDiscord(player: Long): List<Ban> {
        return transaction(this.database) {
            val bans = mutableListOf<Ban>()
            Mutes.select { Bans.discordUUID eq player }.forEach { row ->
                bans.add(banFromResultRow(row))
            }

            return@transaction bans
        }
    }

    fun queryPlayerBansByStatus(status: ExpiryState): List<Ban> {
        return transaction(this.database) {
            val bans = mutableListOf<Ban>()
            Bans.select { Bans.expiryState eq status }.forEach { row ->
                bans.add(banFromResultRow(row))
            }

            return@transaction bans
        }
    }

    fun addBanToPlayerMinecraft(player: UUID, issuer: UUID, expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                val issuerUsername = Main.mojangAPI.getPlayerProfile(uuidToString(issuer))
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Bans.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "M ${issuerUsername.username} ${issuerUsername.uuid}"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isBanned] = true
                }

                return@transaction Result.success(id.value)
            }
            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun addBanPlayerDiscord(player: UUID, issuer: User, expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Bans.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "D ${issuer.discriminatedName} ${issuer.id}"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isBanned] = true
                }

                return@transaction Result.success(id.value)
            }

            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun addBanPlayerUnknown(player: UUID, issuer: String,  expiryDateTime: ExpiryDateTime, rsn: String): Result<Long> {
        return transaction(this.database) {
            this@UserSQL.playerGetByUUID(player)?.let { player ->
                if (rsn.length > 256) {
                    return@transaction Result.failure(IllegalArgumentException("reason message too long!"))
                }

                val id = Bans.insertAndGetId {
                    it[playerUUID] = player.playerUUID
                    it[discordUUID] = player.discordUUID
                    it[issuedBy] = "U $issuer"
                    it[issuedOn] = DateTime.now()
                    when (expiryDateTime) {
                        is ExpiryDateTime.DateAndTime -> {
                            it[isPermanent] = false
                            // Kotlin is giving me an array access error
                            // so we have to do this idiocy
                            it[expiresOn] = expiryDateTime.time
                        }
                        is ExpiryDateTime.Permanent -> {
                            it[isPermanent] = true
                            it[expiresOn] = Main.neverHappenedDateTime
                        }
                    }
                    it[reason] = rsn
                    it[expiryState] = ExpiryState.OnGoing
                }

                Players.update ({Players.playerUUID eq player.playerUUID}) {
                    it[isBanned] = true
                }

                return@transaction Result.success(id.value)
            }

            return@transaction Result.failure(IllegalArgumentException("Player not found (perhaps not verified?)!"))
        }
    }

    fun pardonBan(ban: Long, pardon: Boolean = false) {
        transaction(this.database) {
            val expiry = if (pardon) ExpiryState.Pardoned else ExpiryState.Expired


            val thisBan = this@UserSQL.queryPlayerBanById(ban) ?: return@transaction

            if (thisBan.expiryState == ExpiryState.OnGoing) {
                Bans.update ({Bans.banId eq ban}) {
                    it[Bans.expiryState] = expiry
                }
            }

            // update muted status
            val bans = Mutes.select { Mutes.playerUUID eq thisBan.playerUUID }.count()
            if (bans == 0L) {
                Players.update ({ Players.playerUUID eq thisBan.playerUUID }) {
                    it[isBanned] = false
                }
            }
        }
    }

    fun queryIgnoresBySourcePlayerUUID(uuid: UUID): List<Ignore> {
        return transaction(this.database) {
            val ignores = mutableListOf<Ignore>()
            Ignores.select {Ignores.sourcePlayerUUID eq uuid}.forEach {
                ignores.add(ignoreFromResultRow(it))
            }
            return@transaction ignores
        }
    }

    fun queryIgnoresBySourceDiscordUUID(uuid: Long): List<Ignore> {
        return transaction(this.database) {
            val ignores = mutableListOf<Ignore>()
            val player = this@UserSQL.playerGetByDiscordUUID(uuid) ?: return@transaction ignores
            Ignores.select {Ignores.sourcePlayerUUID eq player.playerUUID}.forEach {
                ignores.add(ignoreFromResultRow(it))
            }
            return@transaction ignores
        }
    }

    fun ignoreById(ignoreId: Long): Ignore? {
        return transaction(this.database) {
            try {
                Ignores.select {Ignores.ignoreId eq ignoreId}.singleOrNull()?.let {
                    return@transaction ignoreFromResultRow(it)
                }
                return@transaction null
            } catch (_: Exception) {
                return@transaction null
            }
        }
    }

    fun addIgnore(source: UUID, destination: Long) {
        transaction(this.database) {
            val sourceIgnores = this@UserSQL.queryIgnoresBySourcePlayerUUID(source).filter {
                it.target == destination
            }
            if (sourceIgnores.isEmpty()) {
                Ignores.insert {
                    it[sourcePlayerUUID] = source
                    it[targetPlayerUUID] = destination
                }
            }
        }
    }

    fun removeIgnore(ignoreId: Long) {
        val ignore = this.ignoreById(ignoreId)
        ignore?.let {
            transaction(this.database) {
                Ignores.deleteWhere { Ignores.ignoreId eq ignoreId }
            }
        }
    }

    fun removeIgnoreOfPlayers(source: UUID, target: Long) {
        val ignoresOfPlayer = this.queryIgnoresBySourcePlayerUUID(source).filter {
            it.target == target
        }
        transaction(this.database) {
            ignoresOfPlayer.forEach { ig ->
                Ignores.deleteWhere { Ignores.ignoreId eq ig.ignoreId }
            }
        }
    }

    fun close() {
        TransactionManager.closeAndUnregister(this.database)
    }
}