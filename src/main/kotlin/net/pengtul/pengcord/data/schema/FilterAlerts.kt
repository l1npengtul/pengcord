package net.pengtul.pengcord.data.schema

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

object FilterAlerts: IdTable<Long>() {
    val filterAlertId = long("filterAlertId").autoIncrement().uniqueIndex()
    val playerUUID = (uuid("playerUUID") references Players.playerUUID)
    val discordUUID = (long("discordUUID") references Players.discordUUID)
    val issuedOn = datetime("issuedOn")
    val context = varchar("context", 256)
    val word = varchar("word", 50)

    override val id: Column<EntityID<Long>> = filterAlertId.entityId()
}

data class FilterAlert(
    val filterAlertId: Long,
    val playerUUID: UUID,
    val discordUUID: Long,
    val issuedOn: DateTime,
    val context: String,
    val word: String,
)

fun filterAlertFromResultRow(
    resultRow: ResultRow
): FilterAlert {
    return FilterAlert(
        resultRow[FilterAlerts.filterAlertId],
        resultRow[FilterAlerts.playerUUID],
        resultRow[FilterAlerts.discordUUID],
        resultRow[FilterAlerts.issuedOn],
        resultRow[FilterAlerts.context],
        resultRow[FilterAlerts.word],
    )
}