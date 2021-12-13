package net.pengtul.pengcord.bot.botcmd

import org.javacord.api.entity.message.Message
import org.javacord.api.util.logging.ExceptionLogger
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/*
*    A message that delets after send
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
object CommandHelper {
    fun deleteAfterSend(message: String?, duration: Int, msg: Message) {
        msg.reply(message)
            .thenAccept { sent ->
                sent.api.threadPool.scheduler.schedule(
                    {
                        sent.delete().exceptionally(ExceptionLogger.get())
                    },
                    duration.toLong(),
                    TimeUnit.SECONDS
                )
            }.exceptionally (ExceptionLogger.get())
    }
}