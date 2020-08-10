package net.pengtul.servsync_api

public data class Player(val uuid: String, val uuid_disc: String, val usediscordauth: Boolean) {
    private var PlayerUUID: String = uuid;
    private var DiscordUUID: String = uuid_disc;
    private var DiscordAuth: Boolean = usediscordauth;
}