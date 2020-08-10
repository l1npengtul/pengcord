package net.pengtul.servsync_api.bot

import net.pengtul.servsync_api.Main
import org.javacord.api.event.server.ServerJoinEvent
import org.javacord.api.listener.server.ServerJoinListener

class ServJoinEvent : ServerJoinListener{
    public override fun onServerJoin(event: ServerJoinEvent?) {
        if(event != null){
            if (Main.ServerConfig.ServerBindID.equals(""+event.server.id) || Main.ServerConfig.ServerBindID.equals("") || Main.ServerConfig.ServerBindID == null){
                Main.ServerConfig.ServerBindID = ""+event.server.id;
            }
            else{
                event.server.leave(); // Leave server if its not the same as the one in `config.yml`
            }
        }
    }
}