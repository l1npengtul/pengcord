package net.pengtul.pengcord.data.schema

import net.pengtul.pengcord.main.Main
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

object Players: Table() {
    val playerUUID = uuid("playerUUID").uniqueIndex()
    val currentUsername = varchar("currentUsername", 17).default("")
    val discordUUID = long("discordUUID")
    val firstJoinDateTime = datetime("firstJoinDateTime").default(Main.neverHappenedDateTime)
    val verifiedDateTime = datetime("verifiedDateTime").default(Main.neverHappenedDateTime)
    val secondsPlayed = long("hoursPlayed").default(0)
    val isBanned = bool("isBanned").default(false)
    val isMuted = bool("isMuted").default(false)
    val deaths = long("deaths").default(0)

    override val primaryKey = PrimaryKey(playerUUID, name = "PlayerUUID")
}

data class Player(
    val playerUUID: UUID,
    val currentUsername: String,
    val discordUUID: Long,
    val firstJoinDateTime: DateTime,
    val verifiedDateTime: DateTime,
    val secondsPlayed: Long,
    val isBanned: Boolean,
    val isMuted: Boolean,
    val deaths: Long,
)

fun playerFromResultRow(resRow: ResultRow): Player {
    return Player(
        resRow[Players.playerUUID],
        resRow[Players.currentUsername],
        resRow[Players.discordUUID],
        resRow[Players.firstJoinDateTime],
        resRow[Players.verifiedDateTime],
        resRow[Players.secondsPlayed],
        resRow[Players.isBanned],
        resRow[Players.isMuted],
        resRow[Players.deaths],
    )
}