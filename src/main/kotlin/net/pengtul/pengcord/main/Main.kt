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

import net.luckperms.api.LuckPerms
import net.pengtul.pengcord.bot.Bot
import net.pengtul.pengcord.commands.Verify
import net.pengtul.pengcord.config.Config
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.logging.Logger
import javax.imageio.ImageIO

class Main : JavaPlugin(), Listener, CommandExecutor{
    companion object {
        val ServerLogger: Logger = Bukkit.getLogger()
        lateinit var ServerConfig: Config
        lateinit var ServerRawConfig: FileConfiguration

        //public lateinit var SqlDealer: SQLClass;
        //public lateinit var discordApi: DiscordApi;
        lateinit var dFolder: File
        lateinit var discordBot: Bot
        lateinit var luckPerms: LuckPerms
        var playersToVerify = HashMap<String, String>()
        var doSyncDiscord: Boolean = true
        // lateinit var sqlClass: SQLClass

        fun downloadSkin(usr: Player){
            val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord")
            val usrUUID: String = usr.uniqueId.toString()
            currentPlugin?.let {
                Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
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
        }


    }



    override fun onEnable() {
        // Save the default config if it doesn't exist
        this.saveDefaultConfig()

        // Register the Events
        Bukkit.getPluginManager().registerEvents(Event(), this)

        // Read the `config.yml` and set a dataclass
        val cfgfile = this.config
        ServerRawConfig = cfgfile

        ServerConfig = Config(
                es = cfgfile.getBoolean("enable-sync"),
                saesang = cfgfile.getString("world-to-track"),
                discordkey = cfgfile.getString("client-token"),
                verienable = cfgfile.getBoolean("enable-verify"),
                serv = cfgfile.getString("server-sys"),
                sys = cfgfile.getString("system-sys"),
                servname = cfgfile.getString("server-name"),
                bpf = cfgfile.getString("bot-prefix"),
                bind_sync = cfgfile.getString("bot-sync-channel"),
                bind_cmd = cfgfile.getString("bot-command-channel"),
                bind_admin = cfgfile.getString("bot-admin-channel"),
                servtrak = cfgfile.getString("bot-server"),
                webid = cfgfile.getString("webhook-id"),
                webtok = cfgfile.getString("webhook-token"),
                admins = cfgfile.getStringList("server-admin-roles"),
                adnr = cfgfile.getBoolean("server-non-adminrole-admin"),
                bwenable = cfgfile.getBoolean("banned-words-enabled"),
                bw = cfgfile.getStringList("banned-words"),
                bwdisc = cfgfile.getBoolean("banned-word-discord"),
                bwmsg = cfgfile.getString("banned-word-message")
        )

        dFolder = dataFolder
        try{
            discordBot = Bot()
        }
        catch (e: Exception){
            ServerLogger.severe("Exception ${e}. Disabling plugin!")
            this.pluginLoader.disablePlugin(this)
        }

        // Log to console for startup message
        ServerLogger.info {
            "[Pengcord] Sucessfully Started!"
        }

        discordBot.sendMessageToDiscord("Server Started!")
        this.getCommand("verify")?.setExecutor(Verify())

        //sqlClass = SQLClass();

        // Load LuckPerms
        val provider: RegisteredServiceProvider<LuckPerms> = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)!!
        luckPerms = provider.provider
    }

    override fun onDisable() {
        //SqlDealer.getConnectionHandler().close();
        discordBot.sendMessageToDiscord("Server Shutdown Event!")
        discordBot.webhook.delete().join()
        discordBot.discordApi.disconnect()
        ServerConfig.writeValues()
        this.saveConfig()
    }
}