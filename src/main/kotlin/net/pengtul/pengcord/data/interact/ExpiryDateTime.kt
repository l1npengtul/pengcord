package net.pengtul.pengcord.data.interact

import org.joda.time.DateTime

sealed class ExpiryDateTime {
    class DateAndTime(val time: DateTime): ExpiryDateTime()
    object Permanent: ExpiryDateTime()

    override fun toString(): String {
        return when (this) {
            is DateAndTime -> {
                this.time.toString()
            }
            is Permanent -> {
                "Permanent"
            }
        }
    }
}