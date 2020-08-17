package net.pengtul.pengcord.bot

import club.minnced.discord.webhook.WebhookClient
import net.pengtul.pengcord.config.DiscordConfig
import net.pengtul.pengcord.error.DiscordLoginFailException
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.main.Player
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.webhook.Webhook
import org.javacord.api.entity.webhook.WebhookBuilder
import org.javacord.api.entity.webhook.WebhookUpdater
import java.io.File
import java.lang.Exception


public class Bot {
    public var discordApi: DiscordApi;
    public var webhookInit: Boolean;
    public lateinit var webhook: Webhook;
    public lateinit var webhookUpdater: WebhookUpdater;
    public lateinit var webhookSender: WebhookClient;
    companion object{
        public lateinit var config: DiscordConfig;
    }
    init {
        discordApi = DiscordApiBuilder()
                .setToken(Main.ServerConfig.DiscordBot)
                .login()
                .exceptionally {
                    throw DiscordLoginFailException("Failed to log into discord!");
                }
                .join();


        discordApi.getServerTextChannelById(Main.ServerConfig.syncChannel).ifPresent { serverTextChannel: ServerTextChannel ->
            this.webhook = WebhookBuilder(serverTextChannel)
                    .setName("DSC-SYNC")
                    .create()
                    .join()
            this.webhookUpdater = webhook.createUpdater();
            webhook.token.ifPresent { s: String ->
                this.webhookSender = WebhookClient.withId(webhook.id,s);
            }
        };
        webhookInit = this::webhook.isInitialized && this::webhookSender.isInitialized && this::webhookUpdater.isInitialized
        this.onSucessfulConnect();
        discordApi.addListener(DscMessageEvent());
    }



    public fun disconnectApi() {
        this.discordApi.disconnect();
    }

    public fun onSucessfulConnect() {
        Main.ServerLogger.info("Sucessfully connected to Discord! Invite to server using:");
        Main.ServerLogger.info(discordApi.createBotInvite())
    }

    public fun sendMessageToDiscord(message: String){
        discordApi.getTextChannelById(Main.ServerConfig.syncChannel).ifPresent { channel ->
            channel.sendMessage(message)
        };
    }

    public fun sendMessagetoWebhook(message: String, usrname: String, pfp: String?, player: org.bukkit.entity.Player){
        if(webhookInit){
            val currentPlugin: Plugin? = Bukkit.getServer().pluginManager.getPlugin("pengcord");
            currentPlugin?.let {
                Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
                    var msg: String = message;
                    if (!usrname.toLowerCase().equals("clyde")){
                        webhookUpdater = webhookUpdater.setName(usrname);
                    }
                    else if (usrname.equals(webhook.name.get())){

                    }
                    else{
                        webhookUpdater = webhookUpdater.setName("DSC-SYNC");
                        msg = "<${usrname}>" + message;
                    }
                    try {
                        webhookUpdater.setAvatar(File("plugins${File.separator}pengcord${File.separator}playerico${File.separator}${player.uniqueId}.png"));
                    }
                    catch (e: Exception){
                        Main.ServerLogger.severe("Failed to get PlayerIcon for player ${player.displayName}: Exception $e");
                    }
                    webhook = webhookUpdater.update().join();
                    this.webhookSender.send(msg);
                })
            }
        }
    }
}

