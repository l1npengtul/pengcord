package net.pengtul.servsync_api.config

import net.pengtul.pengcord.main.Main

public data class Config(val enablesync: Boolean, val saesang: String?, val discordkey: String?, val verienable: Boolean, val sqlObj: SQLClass?, val serv: String?, val sys: String?, val servname: String?, val bpf: String?, var bind_sync: String?, val bind_cmd: String?, val bind_admin: String?, val servtrak: String?) {
    var EnableSync: Boolean = enablesync;
    var WorldToTrack: String? = saesang;
    val DiscordBot: String? = discordkey;
    val EnableVerify: Boolean = verienable;
    val SQLHandler: SQLClass? = sqlObj;
    val ServerPrefix: String? = serv;
    val ServerName: String? = servname;
    val SysPrefix: String? = sys;
    val BotPrefix: String? = bpf;
    var syncChannel: String? = bind_sync;
    var commandChannel: String? = bind_cmd;
    var adminChannel: String? = bind_admin;
    var ServerBind: String? = servtrak;
    public fun writeValues(){

        Main.ServerRawConfig.set("enable-sync", this.EnableSync);
        Main.ServerRawConfig.set("world-to-track", this.WorldToTrack);
        Main.ServerRawConfig.set("enable-verify", this.EnableVerify);
        Main.ServerRawConfig.set("bot-prefix", this.BotPrefix);
        Main.ServerRawConfig.set("bot-sync-channel", this.syncChannel);
        Main.ServerRawConfig.set("bot-command-channel", this.commandChannel);
        Main.ServerRawConfig.set("bot-admin-channel", this.adminChannel);
        Main.ServerRawConfig.set("bot-server", this.ServerBind);
    }
}