package net.pengtul.pengcord.config

/*
*   Class the carries all the config info
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


import net.pengtul.pengcord.main.Main

data class Config(val es: Boolean, val saesang: String?, val discordkey: String?, val verienable: Boolean, val serv: String?,
                  val sys: String?, val servname: String?, val bpf: String?, var bind_sync: String?, val bind_cmd: String?, val bind_admin: String?,
                  val servtrak: String?, val webid: String?, val webtok: String?, val adnr: Boolean, val admins: List<String>?,
                  val bwenable: Boolean, val bw: List<String>?, val bwmsg: String?, val bwdisc: Boolean) {
    var enableSync: Boolean = es
    var worldToTrack: String? = saesang
    val discordApiKey: String? = discordkey
    val enableVerify: Boolean = verienable
    val serverPrefix: String? = serv
    val serverName: String? = serv
    val botPrefix: String? = bpf
    var syncChannel: String? = bind_sync
    var commandChannel: String? = bind_cmd
    var adminChannel: String? = bind_admin
    var serverBind: String? = servtrak
    var adminList: List<String>? = admins
    var adminNoRole: Boolean = adnr
    var bannedWords: List<String>? = bw
    var bannedWordsEnable: Boolean = bwenable
    var bannedWordMessage: String? = bwmsg
    var bannedWordDiscord: Boolean = bwdisc
    var usersList: HashMap<String, String>? = getListOfUsers();

    fun writeValues(){
        Main.ServerRawConfig.set("enable-sync", this.enableSync)
        Main.ServerRawConfig.set("world-to-track", this.worldToTrack)
        Main.ServerRawConfig.set("enable-verify", this.enableVerify)
        Main.ServerRawConfig.set("bot-prefix", this.botPrefix)
        Main.ServerRawConfig.set("bot-sync-channel", this.syncChannel)
        Main.ServerRawConfig.set("bot-command-channel", this.commandChannel)
        Main.ServerRawConfig.set("bot-admin-channel", this.adminChannel)
        Main.ServerRawConfig.set("bot-server", this.serverBind)
        Main.ServerRawConfig.set("webhook-id", this.serverBind)
        Main.ServerRawConfig.set("webhook-token", this.serverBind)
        Main.ServerRawConfig.set("server-admin-roles", this.adminList)

        this.saveUsersList()
    }

    fun getListOfUsers(): HashMap<String, String>{
        var map: HashMap<String, String> = java.util.HashMap<String, String>();
        for(key in Main.ServerRawConfig.getConfigurationSection("verified-users")?.getKeys(false)!!) {
            Main.ServerRawConfig.getString("verified-users.$key")?.let {
                map.put(key, it)
            }
        }
        return map;
    }

    fun saveUsersList() {
        for (msg in this.usersList?.keys!!){
            if (!msg.isBlank()){
                Main.ServerRawConfig.set("verified-users.$msg", this.usersList!![msg])
            }
        }
    }
}