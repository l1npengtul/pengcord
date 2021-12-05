package net.pengtul.pengcord.data.interact

import java.util.*

sealed class UpdateVerify {
    class NewlyVerified(val discordUUID: Long): UpdateVerify()
    object Unverify : UpdateVerify()
}
