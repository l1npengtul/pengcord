package net.pengtul.pengcord.data.schema

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

object Ignores: IdTable<Long>() {
    val ignoreId = long("id").autoIncrement().uniqueIndex()
    val sourcePlayerUUID = (uuid("sourcePlayerUUID") references Players.playerUUID)
    val targetPlayerUUID = (long("targetPlayerUUID") references Players.discordUUID)

    override val id: Column<EntityID<Long>> = ignoreId.entityId()
}

data class Ignore(
    val ignoreId: Long,
    val source: UUID,
    val target: Long,
)

fun ignoreFromResultRow(resrow: ResultRow): Ignore {
    return Ignore(
        resrow[Ignores.ignoreId],
        resrow[Ignores.sourcePlayerUUID],
        resrow[Ignores.targetPlayerUUID],
    )
}