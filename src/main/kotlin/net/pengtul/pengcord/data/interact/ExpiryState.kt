package net.pengtul.pengcord.data.interact

enum class ExpiryState(val typ: Int) {
    Pardoned(0),
    Expired(1),
    OnGoing(2),
}