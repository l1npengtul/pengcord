package net.pengtul.pengcord.data.schema

import net.pengtul.pengcord.data.interact.ExpiryState
import net.pengtul.pengcord.main.Main
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

object Bans: IdTable<Long>() {
    val banId = long("id").autoIncrement().uniqueIndex()
    val playerUUID = (uuid("playerUUID") references Players.playerUUID)
    val discordUUID = long("discordUUID")
    // Format
    // Minecraft Issuer: M [username-16] [uuid-32]
    // Discord Issuer: D [username-32]#[discriminator-4] [long-20]
    // Unknown: U
    val issuedBy = varchar("issuedBy", 159)
    val isPermanent = bool("isPermanent").default(false)
    val issuedOn = datetime("issuedOn").default(DateTime.now())
    val expiresOn = datetime("expiresOn").default(Main.neverHappenedDateTime)
    val reason = varchar("reason", 256).default("Banned")
    val expiryState = enumeration("expiry", ExpiryState::class).default(ExpiryState.OnGoing)
    override val id: Column<EntityID<Long>> = banId.entityId()
}

data class Ban(
    val banId: Long,
    val playerUUID: UUID,
    val discordUUID: Long,
    val issuedBy: String,
    val isPermanent: Boolean,
    val issuedOn: DateTime,
    val expiresOn: DateTime,
    val reason: String,
    val expiryState: ExpiryState,
)

fun banFromResultRow(resRow: ResultRow): Ban {
    return Ban(
        resRow[Bans.banId],
        resRow[Bans.playerUUID],
        resRow[Bans.discordUUID],
        resRow[Bans.issuedBy],
        resRow[Bans.isPermanent],
        resRow[Bans.issuedOn],
        resRow[Bans.expiresOn],
        resRow[Bans.reason],
        resRow[Bans.expiryState],
    )
}