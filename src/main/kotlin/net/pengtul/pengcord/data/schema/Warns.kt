package net.pengtul.pengcord.data.schema

import net.pengtul.pengcord.data.schema.Warns.autoIncrement
import net.pengtul.pengcord.data.schema.Warns.references
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

object Warns: IdTable<Long>() {
    val warnId = long("warnId").autoIncrement().uniqueIndex()
    val playerUUID = (uuid("playerUUID") references Players.playerUUID)
    val discordUUID = (long("discordUUID") references Players.discordUUID)
    // Format
    // Minecraft Issuer: M [username-16] [uuid-32]
    // Discord Issuer: D [username-32]#[discriminator-4] [long-20]
    val issuedBy = varchar("issuedBy", 159)
    val issuedOn = datetime("issuedOn")
    val reason = varchar("reason", 256)
    override val id: Column<EntityID<Long>> = warnId.entityId()
}

data class Warn(
    val warnId: Long,
    val playerUUID: UUID,
    val discordUUID: Long,
    val issuedBy: String,
    val issuedOn: DateTime,
    val reason: String,
)

fun warnFromResultRow(resultRow: ResultRow): Warn {
    return Warn(
        resultRow[Warns.warnId],
        resultRow[Warns.playerUUID],
        resultRow[Warns.discordUUID],
        resultRow[Warns.issuedBy],
        resultRow[Warns.issuedOn],
        resultRow[Warns.reason],
    )
}