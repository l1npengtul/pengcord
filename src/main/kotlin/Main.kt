import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

public class Main() : JavaPlugin(), Listener, CommandExecutor{
    companion object {
        public val ServerLogger : Logger = Bukkit.getLogger();
    }
    init {
    }

    override fun onEnable() {
        ServerLogger.info {
            "ServerSync API Sucessfully Started!"
        }
    }
}