package net.pengtul.pengcord.bot.botcmd;

import org.javacord.api.entity.message.Message;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

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



public class CommandHelper {
    public static void deleteAfterSend(String message, int duration, @NotNull final Message msg){
        msg.getChannel()
                .sendMessage(message)
                .thenAccept(sent -> sent.getApi().getThreadPool().getScheduler()
                        .schedule(() -> sent.delete().exceptionally(ExceptionLogger.get()), duration, TimeUnit.SECONDS))
                .exceptionally(ExceptionLogger.get());
    }
}
