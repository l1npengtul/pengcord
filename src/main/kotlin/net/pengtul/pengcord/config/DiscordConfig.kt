package net.pengtul.pengcord.config

import net.pengtul.pengcord.bot.ChannelTyoe
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.javacord.api.entity.channel.ChannelType
import java.io.File
import java.lang.Exception
import java.util.*

class DiscordConfig(dataFolder: File, fn: String) {
    public lateinit var discordConfig: FileConfiguration;
    public lateinit var discordConfigFile: File;
    private val dirPath: File = dataFolder;
    private val fileName: String = fn;
    
    public fun getFile() {
        if (!this::discordConfigFile.isInitialized){
            if (dirPath.mkdirs()){
                discordConfigFile = File(dirPath.toString() + File.pathSeparator + fileName);
                if (discordConfigFile.exists() && !discordConfigFile.isDirectory){
                    discordConfig = YamlConfiguration.loadConfiguration(File(dirPath, fileName));
                    this.writeDefaultValues();
                }
                else{
                    discordConfigFile.createNewFile();
                    if (discordConfigFile.exists() && !discordConfigFile.isDirectory){
                        discordConfig = YamlConfiguration.loadConfiguration(File(dirPath, fileName));
                        this.writeDefaultValues();
                    }
                }
            }
            else{
                if (discordConfigFile.exists() && !discordConfigFile.isDirectory){
                    discordConfig = YamlConfiguration.loadConfiguration(File(dirPath, fileName));
                }
            }
        }
    }

    private fun writeDefaultValues() {
        if (this::discordConfig.isInitialized) {
            discordConfig.set("guild-lock", "");
            discordConfig.set("channel-lock-sync", "");
            discordConfig.set("command-lock-sync", "");
            discordConfig.set("admin-command-lock-sync", "");
            val roleTempList: List<String> = Arrays.asList("","","");
            discordConfig.set("admin-roles", roleTempList);
            val tempList: List<String> = Arrays.asList(":");
            discordConfig.set("rolelist", tempList);
        }
    }

    public fun getLockChannel(cType: ChannelTyoe): Long{
        return when(cType){
            ChannelTyoe.SyncChannel -> {
                discordConfig.getLong("channel-lock-sync");
            }
            ChannelTyoe.AdminCommandChannel -> {
                discordConfig.getLong("command-lock-sync");
            }
            ChannelTyoe.CommandChannel -> {
                discordConfig.getLong("admin-command-lock-sync");
            }
        }
    }
}