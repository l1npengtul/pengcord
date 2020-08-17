package net.pengtul.pengcord.main

import net.pengtul.pengcord.bot.Bot
import net.pengtul.pengcord.bot.DscMessageEvent
import net.pengtul.pengcord.config.DiscordConfig
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import net.pengtul.servsync_api.config.*;
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.javacord.api.DiscordApi
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.net.URL
import javax.imageio.ImageIO

public class Main(): JavaPlugin(), Listener, CommandExecutor{
    companion object {
        public val ServerLogger: Logger = Bukkit.getLogger();
        public lateinit var ServerConfig: Config;
        public lateinit var ServerRawConfig: FileConfiguration;
        //public lateinit var SqlDealer: SQLClass;
        //public lateinit var discordApi: DiscordApi;
        public lateinit var dFolder: File;
        public lateinit var discordBot: Bot;

        public fun downloadSkin(usr: Player, type: String?){
            val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord");
            val usrUUID: String = usr.uniqueId.toString();
            currentPlugin?.let {
                Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
                    try{
                        val newPngFile: File = File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${usrUUID}.png");
                        newPngFile.mkdirs();
                        val image: BufferedImage = ImageIO.read(URL("https://minotar.net/helm/${usrUUID}/100.png"));
                        ImageIO.write(image,"png",newPngFile);
                    }
                    catch (e: Exception){
                        println("Exception ${e.toString()}");
                    }
                })
            }
        }
    }
    init {
        // do nothing
    }



    override fun onEnable() {
        // Save the default config if it doesn't exist
        this.saveDefaultConfig();

        // Register the Events
        Bukkit.getPluginManager().registerEvents(Event(),this);



        // Read the `config.yml` and set a dataclass
        val cfgfile = this.config;
        ServerRawConfig = cfgfile;
        ServerConfig = Config(
                    cfgfile.getBoolean("enable-sync"),
                    cfgfile.getString("world-to-track"),
                    cfgfile.getString("client-token"),
                    cfgfile.getBoolean("enable-verify"),
                    SQLClass(
                            cfgfile.getString("sql-driver"),
                            cfgfile.getString("sql-ip"),
                            cfgfile.getString("sql-database"),
                            cfgfile.getString("sql-username"),
                            cfgfile.getString("sql-password")
                    ),
                    cfgfile.getString("server-sys"),
                    cfgfile.getString("server-name"),
                    cfgfile.getString("system-sys"),
                    cfgfile.getString("bot-prefix"),
                    cfgfile.getString("bot-sync-channel"),
                    cfgfile.getString("bot-command-channel"),
                    cfgfile.getString("bot-admin-channel"),
                    cfgfile.getString("bot-server")
        );

        dFolder = dataFolder;
        try{
            discordBot = Bot();
        }
        catch (e: Exception){
            Main.ServerLogger.severe("Exception ${e}. Disabling plugin!");
            this.pluginLoader.disablePlugin(this);
        }

        // Log to console for startup message
        ServerLogger.info {
            "[Pengcord] Sucessfully Started!"
        }
    }

    override fun onDisable() {
        //SqlDealer.getConnectionHandler().close();
        discordBot.sendMessageToDiscord("Server Shutdown Event!");
        discordBot.discordApi.disconnect();
        ServerConfig.writeValues();
        this.saveConfig();
    }
}