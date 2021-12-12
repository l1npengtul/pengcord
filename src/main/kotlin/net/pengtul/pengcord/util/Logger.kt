package net.pengtul.pengcord.util

import net.pengtul.pengcord.bot.Bot
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import java.util.logging.Logger

class Logger(discordBot: Bot, plugin: JavaPlugin): SqlLogger {
    private val bukkitLogger: Logger
    private val discordBotLogger: Bot
    private val discordSync: Boolean
    private val logSql: Boolean

    init {
        bukkitLogger = plugin.logger
        discordBotLogger = discordBot
        discordSync = false
        logSql = false
    }

    override fun log(context: StatementContext, transaction: Transaction) {
        if (logSql) {
            this.info()
        }
    }

    fun info(log: String) {
        bukkitLogger.info(log)
        if
    }
}