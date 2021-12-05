package net.pengtul.pengcord.data.interact

import java.util.*

sealed class TypeOfUniqueID {
    class MinecraftTypeOfUniqueID(val uuid: UUID) : TypeOfUniqueID()
    class DiscordTypeOfUniqueID(val uuid: Long) : TypeOfUniqueID()
    class Unknown(val string: String) : TypeOfUniqueID()
}
