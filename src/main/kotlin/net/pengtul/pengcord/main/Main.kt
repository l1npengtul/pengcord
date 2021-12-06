package net.pengtul.pengcord.main

/*
*    The Main Class for Pengcord
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

// If you're reviewing this, or have to read this
// I'm sorry.

import net.milkbowl.vault.chat.Chat
import net.pengtul.pengcord.Utils.Companion.banPardon
import net.pengtul.pengcord.Utils.Companion.pardonMute
import net.pengtul.pengcord.bot.Bot
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.commands.*
import net.pengtul.pengcord.data.ServerConfig
import net.pengtul.pengcord.data.UserSQL
import net.pengtul.pengcord.data.interact.ExpiryState
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.shanerx.mojang.Mojang
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.*
import java.util.logging.Level
import javax.imageio.ImageIO
import kotlin.collections.HashMap

typealias MinecraftId = UUID

class Main : JavaPlugin(), Listener, CommandExecutor{
    companion object {
        val serverLogger =  Bukkit.getLogger()
        lateinit var database: UserSQL
        lateinit var serverRawConfig: FileConfiguration
        lateinit var serverConfig: ServerConfig
        lateinit var discordFolder: File
        lateinit var discordBot: Bot
        var mojangAPI: Mojang = Mojang().connect()
        lateinit var pengcord: Plugin
        val scheduler: BukkitScheduler = Bukkit.getScheduler()
        val neverHappenedDateTime: DateTime = DateTime(0)
        var playersAwaitingVerification: HashMap<Int, MinecraftId> = HashMap()
        var playersCurrentJoinTime: HashMap<MinecraftId, DateTime> = HashMap()
        val periodFormatter: PeriodFormatter = PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendSeconds()
            .toFormatter()
        lateinit var vaultApi: Plugin
        lateinit var vaultChatApi: Chat
        private var salty: String = UUID.randomUUID().toString()

        fun downloadSkin(usr: Player){
            val usrUUID: String = usr.uniqueId.toString()
            scheduler.runTaskAsynchronously(pengcord, Runnable {
                try {
                    val newPngFile = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${usrUUID}.png")
                    newPngFile.mkdirs()
                    val image: BufferedImage = ImageIO.read(URL("https://minotar.net/helm/${usrUUID}/100.png"))
                    ImageIO.write(image, "png", newPngFile)
                } catch (e: Exception) {
                    println("Exception $e")
                }
            })

        }

        fun downloadSkinUUID(uuid: UUID){
            scheduler.runTaskAsynchronously(pengcord, Runnable {
                try {
                    val newPngFile = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${uuid}.png")
                    newPngFile.mkdirs()
                    val image: BufferedImage = ImageIO.read(URL("https://minotar.net/helm/${uuid}/100.png"))
                    ImageIO.write(image, "png", newPngFile)
                } catch (e: Exception) {
                    println("Exception $e")
                }
            })
        }

        fun getDownloadedSkinAsFile(uuid: UUID): File? {
            val pngFile = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${uuid}.png")
            if (!pngFile.isFile) {
                return null
            }
            return pngFile
        }

        fun uuidToString(uuid: UUID): String {
            return insertDashUUID(uuid.toString())
        }

        fun insertDashUUID(uuid: String): String {
            return if (!uuid.contains('-')) {
                var sb = StringBuffer(uuid)
                sb.insert(8, "-")
                sb = StringBuffer(sb.toString())
                sb.insert(13, "-")
                sb = StringBuffer(sb.toString())
                sb.insert(18, "-")
                sb = StringBuffer(sb.toString())
                sb.insert(23, "-")
                sb.toString()
            } else {
                uuid
            }
        }

        fun generateUniqueKey(): Int {
            val rightNow = DateTime.now().toInstant().millis.toString()
            if (rightNow.last() == '6' || rightNow.last() == '6') {
                this.salty = UUID.randomUUID().toString()
            }
            return (salty+rightNow).hashCode()
        }

        fun startUnmuteTask(muteId: Long) {
            scheduler.runTaskAsynchronously(pengcord, Runnable {
                val mute = database.queryPlayerMuteById(muteId) ?: return@Runnable
                if (mute.isPermanent) {
                    return@Runnable
                } else {
                    if (mute.expiresOn.isBeforeNow) {
                        pardonMute(mute, false)
                    } else {
                        scheduler.runTaskLaterAsynchronously(pengcord, Runnable {
                            pardonMute(mute, false)
                        }, Duration(DateTime.now().toInstant(), mute.expiresOn.toInstant()).toStandardSeconds().seconds * 20L)
                    }

                }
            })
        }

        fun startUnbanTask(banId: Long) {
            scheduler.runTaskAsynchronously(pengcord, Runnable {
                val ban = database.queryPlayerBanById(banId) ?: return@Runnable
                if (ban.isPermanent) {
                    return@Runnable
                } else {
                    if (ban.expiresOn.isBeforeNow) {
                        banPardon(ban, false)
                    } else {
                        scheduler.runTaskLaterAsynchronously(pengcord, Runnable {
                            banPardon(ban, false)
                        }, Duration(DateTime.now().toInstant(), ban.expiresOn.toInstant()).toStandardSeconds().seconds * 20L)
                    }

                }
            })
        }
    }

    override fun onEnable() {
        // Save the default config if it doesn't exist
        this.saveDefaultConfig()

        // Register the Events
        Bukkit.getPluginManager().registerEvents(Event(), this)


        val pl = Bukkit.getServer().pluginManager.getPlugin("pengcord")
        pengcord = pl!!

        vaultApi = Bukkit.getServer().pluginManager.getPlugin("Vault")!!
        val rsp = Bukkit.getServer().servicesManager.getRegistration(Chat::class.java)
        vaultChatApi = rsp!!.provider


        // Read the `config.yml` and set a dataclass
        val cfgfile = this.config
        serverRawConfig = cfgfile

        val cfg = ServerConfig.new(serverRawConfig)
        cfg.onFailure {
            this.logger.log(Level.SEVERE, "Failed to load server configuration:")
            this.logger.log(Level.SEVERE, it.toString())
            this.logger.log(Level.SEVERE, "Exiting pengcord!!!")
            this.pluginLoader.disablePlugin(this)
        }

        // Initialize SQL
        database = UserSQL(this.dataFolder)

        discordFolder = dataFolder
        try{
            discordBot = Bot()
        }
        catch (e: Exception){
            serverLogger.severe("Exception ${e}. Disabling plugin!")
            this.pluginLoader.disablePlugin(this)
        }

        discordBot.sendMessageToDiscord("Server Started!")
        // General Commands
        this.getCommand("stopserver")?.setExecutor(StopServer())
        this.getCommand("info")?.setExecutor(Info())
        this.getCommand("git")?.setExecutor(Git())
        if (serverConfig.enableSync) {
            this.getCommand("reply")?.setExecutor(Reply())
        }
        // Verify
        this.getCommand("verify")?.setExecutor(Verify())
        this.getCommand("unverify")?.setExecutor(Unverify())
        // Query Player
        this.getCommand("me")?.setExecutor(Me())
        this.getCommand("whois")?.setExecutor(WhoIs())
        // Punishments
        this.getCommand("warn")?.setExecutor(Warn())
        this.getCommand("mute")?.setExecutor(Mute())
        this.getCommand("unmute")?.setExecutor(UnMute())
        this.getCommand("pban")?.setExecutor(PBan())
        this.getCommand("punban")?.setExecutor(PUnban())
        this.getCommand("queryrecords")?.setExecutor(QueryRecord())
        this.getCommand("query")?.setExecutor(Query())

        // Start tasks to unban/unmute players
        database.queryPlayerBansByStatus(ExpiryState.OnGoing).filter { !it.isPermanent }.forEach {ban ->
            startUnbanTask(ban.banId)
        }
        database.queryPlayerMutesByStatus(ExpiryState.OnGoing).filter { !it.isPermanent }.forEach {mute ->
            startUnbanTask(mute.muteId)
        }

        discordBot.log(LogType.ServerStartup, "Server Startup and Plugin Initialization successful.")
        serverLogger.info {
            "[Pengcord] Sucessfully Started!"
        }
    }

    override fun onDisable() {
        database.close()
        discordBot.sendMessageToDiscord("Server Shutting Down! See you soon!")
        discordBot.log(LogType.ServerShutdown, "Server Shutdown initiated.")
        discordBot.webhook.delete().join()
        discordBot.discordApi.disconnect()
        serverConfig.saveToConfigFile(this.config)
        this.saveConfig()
    }
}
