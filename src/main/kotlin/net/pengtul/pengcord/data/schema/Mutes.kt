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

object Mutes: IdTable<Long>() {
    val muteId = long("muteId").autoIncrement().uniqueIndex()
    val playerUUID = (uuid("playerUUID") references Players.playerUUID)
    val discordUUID = (long("discordUUID") references Players.discordUUID)
    // Format
    // Minecraft Issuer: M [username-16] [uuid-32]
    // Discord Issuer: D [username-32]#[discriminator-4] [long-20]
    val issuedBy = varchar("issuedBy", 159)
    val isPermanent = bool("isPermanent").default(false)
    val issuedOn = datetime("issuedOn").default(DateTime.now())
    val expiresOn = datetime("expiresOn").default(Main.neverHappenedDateTime)
    val reason = varchar("reason", 256).default("Muted")
    val expiryState = enumeration("expiry", ExpiryState::class).default(ExpiryState.OnGoing)

    override val id: Column<EntityID<Long>> = muteId.entityId()
}

data class Mute(
    val muteId: Long,
    val playerUUID: UUID,
    val discordUUID: Long,
    val issuedBy: String,
    val isPermanent: Boolean,
    val issuedOn: DateTime,
    val expiresOn: DateTime,
    val reason: String,
    val expiryState: ExpiryState,
)

fun muteFromResultRow(resRow: ResultRow): Mute {
    return Mute(
        resRow[Mutes.muteId],
        resRow[Mutes.playerUUID],
        resRow[Mutes.discordUUID],
        resRow[Mutes.issuedBy],
        resRow[Mutes.isPermanent],
        resRow[Mutes.issuedOn],
        resRow[Mutes.expiresOn],
        resRow[Mutes.reason],
        resRow[Mutes.expiryState],
    )
}