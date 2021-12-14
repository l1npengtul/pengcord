package net.pengtul.pengcord.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.util.Utils
import net.pengtul.pengcord.util.toComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PIgnoreList: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("pengcord.command.ignorelist")) {
            Main.serverLogger.info("User ${sender.name} ran `${this.javaClass.name}`. Failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have permission to run this command!")
            return false
        }
        if (sender !is Player) {
            sender.sendMessage("§cYou must be a player to run this command!")
            return false
        }
        val playerToQuery = Utils.queryPlayerFromString(args.getOrNull(0) ?: "")
        if (playerToQuery == null) {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val sendComponent = Component.text()
                    .content("Ignores: ")
                    .style(Style.style(NamedTextColor.GREEN))
                Main.database.queryIgnoresBySourcePlayerUUID(sender.uniqueId).forEach { ignore ->
                    Main.database.playerGetByUUID(ignore.target)?.let {
                        sendComponent.append(
                            "${it.currentUsername}(${ignore.ignoreId})"
                                .toComponent()
                                .hoverEvent(HoverEvent.showText("Click to unignore!".toComponent()))
                                .clickEvent(ClickEvent.suggestCommand("/pengcord:unignore ${it.playerUUID}"))
                        )
                    }
                }
                sender.sendMessage(sendComponent.build())
            })
            return true
        } else {
            Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
                val sendComponent = Component.text()
                    .content("Ignores: ")
                    .style(Style.style(NamedTextColor.GREEN))
                Main.database.queryIgnoresBySourcePlayerUUID(playerToQuery.playerUUID).forEach { ignore ->
                    Main.database.playerGetByUUID(ignore.target)?.let {
                        sendComponent.append(
                            "${it.currentUsername}(${ignore.ignoreId})"
                                .toComponent()
                                .hoverEvent(HoverEvent.showText("Click to unignore!".toComponent()))
                                .clickEvent(ClickEvent.suggestCommand("/pengcord:unignore ${it.playerUUID}"))
                        )
                    }
                }
                sender.sendMessage(sendComponent.build())
            })
            return true
        }
    }
}