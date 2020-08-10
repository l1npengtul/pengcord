package net.pengtul.servsync_api

import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import net.pengtul.servsync_api.config.*;
import org.bukkit.configuration.file.FileConfiguration

public class Main(): JavaPlugin(), Listener, CommandExecutor{
    companion object {
        public val ServerLogger: Logger = Bukkit.getLogger();
        public lateinit var ServerConfig: Config;
        public lateinit var ServerRawConfig: FileConfiguration;
        public lateinit var SqlDealer: SQLClass;
    }
    init {
    }



    override fun onEnable() {
        // Save the default config if it doesnt exist
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
                    cfgfile.getString("bot-channel-bind"),
                    cfgfile.getString("bot-server-bind"),
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
                    cfgfile.getString("system-sys")
        );


        // Log to console for startup message
        ServerLogger.info {
            "ServerSync API Sucessfully Started!"
        }
    }

    override fun onDisable() {
        SqlDealer.getConnectionHandler().close();
    }
}