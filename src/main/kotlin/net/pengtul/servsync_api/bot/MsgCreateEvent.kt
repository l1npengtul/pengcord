package net.pengtul.servsync_api.bot

import net.pengtul.servsync_api.Main
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener

class MsgCreateEvent: MessageCreateListener {
    public override fun onMessageCreate(event: MessageCreateEvent?) {
        if (event != null){
            if(Main.ServerConfig.servbind == null){
                return; // Do nothing, probably Javacord lying to us
            }
            else {

            }
        }
    }
}