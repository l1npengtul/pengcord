package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.pengtul.pengcord.bot.LogType
import net.pengtul.pengcord.main.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Git: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§aThe git repository is available at https://github.com/l1npengtul/pengcord. Please read the `LICENSE` and `README.md`")
        } else {
            // clickable message
            val clickable = Component.text("§aThe git repository is available at https://github.com/l1npengtul/pengcord. Please read the `LICENSE` and `README.md`")
            clickable.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.OPEN_URL,
                "https://github.com/l1npengtul/pengcord"
            ))
            clickable.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.text("Go to the Pengcord Github!")
                )
            )
            sender.sendMessage(clickable)
        }
        Main.discordBot.log(LogType.MCComamndRan, "User ${sender.name} ran command `${this.javaClass.name}`")
        return true
    }
}