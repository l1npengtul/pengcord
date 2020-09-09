package net.pengtul.pengcord.bot.botcmd;

import org.javacord.api.entity.message.Message;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class CommandHelper {
    public static void deleteAfterSend(String message, int duration, @NotNull final Message msg){
        msg.getChannel()
                .sendMessage(message)
                .thenAccept(sent -> sent.getApi().getThreadPool().getScheduler()
                        .schedule(() -> sent.delete().exceptionally(ExceptionLogger.get()), duration, TimeUnit.SECONDS))
                .exceptionally(ExceptionLogger.get());
    }
}
