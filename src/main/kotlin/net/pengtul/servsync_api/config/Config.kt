package net.pengtul.servsync_api.config

import net.pengtul.servsync_api.Main

public data class Config(val enablesync: Boolean,val saesang: String?, val discordkey: String?, val cbind: String?, val servbind: String?, val verienable: Boolean, val sqlObj: SQLClass?, val serv: String?, val sys: String?, val servname: String?) {
    var EnableSync: Boolean = enablesync;
    var WorldToTrack: String? = saesang;
    val DiscordBot: String? = discordkey;
    var ServerBindID: String? = servbind;
    var ChannelBindID: String? = cbind;
    val EnableVerify: Boolean = verienable;
    val SQLHandler: SQLClass? = sqlObj;
    val ServerPrefix: String? = serv;
    val ServerName: String? = servname;
    val SysPrefix: String? = sys;
    public fun writeValues(){
        Main.ServerRawConfig
    }
}