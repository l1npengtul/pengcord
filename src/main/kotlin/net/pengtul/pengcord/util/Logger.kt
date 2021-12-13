package net.pengtul.pengcord.util

import net.pengtul.pengcord.bot.Bot
import net.pengtul.pengcord.data.ServerConfig
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import java.util.logging.Logger

class Logger(plugin: JavaPlugin, config: ServerConfig): SqlLogger {
    private val bukkitLogger: Logger
    private var discordBotLogger: Bot?
    private val discordSync: Boolean
    private val logSql: Boolean

    init {
        bukkitLogger = plugin.logger
        discordBotLogger = null
        discordSync = config.enableSync
        logSql = config.logSql
    }

    fun initDiscordBot(bot: Bot) {
        this.discordBotLogger = bot
    }

    override fun log(context: StatementContext, transaction: Transaction) {
        if (logSql) {
            val message = "[Exposed SQL]: ${context.statement} ${context.expandArgs(transaction)}"
            this.info(message)
            if (discordSync) {
                discordBotLogger?.log(LogType.SQLLog, message)
            }
        }
    }

    fun info(log: String) {
        bukkitLogger.info(log)
        if (discordSync) {
            discordBotLogger?.log(LogType.Generic, "[INFO]: $log")
        }
    }

    fun info(type: LogType, log: String) {
        bukkitLogger.info("${type.log}$log")
        if (discordSync) {
            discordBotLogger?.log(type, "[INFO]: $log")
        }
    }

    fun warn(log: String) {
        bukkitLogger.warning(log)
        if (discordSync) {
            discordBotLogger?.log(LogType.GenericWarning, "[WARN]: $log")
        }
    }

    fun warn(type: LogType, log: String) {
        bukkitLogger.warning("${type.log}$log")
        if (discordSync) {
            discordBotLogger?.log(type, "[WARN]: $log")
        }
    }

    fun severe(log: String) {
        bukkitLogger.severe(log)
        if (discordSync) {
            discordBotLogger?.log(LogType.GenericError, "[ERROR]: $log")
        }
    }

    fun severe(type: LogType, log: String) {
        bukkitLogger.severe("${type.log}$log")
        if (discordSync) {
            discordBotLogger?.log(type, "[ERROR]: $log")
        }
    }
}