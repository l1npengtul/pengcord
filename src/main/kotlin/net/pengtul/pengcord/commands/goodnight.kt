package net.pengtul.pengcord.commands

import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.StringBuilder

class goodnight : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("pengcord.command.goodnight")){
            if (args[0] == "force" && sender.hasPermission("pengcord.command.fgoodnight")){
                for(world in Bukkit.getServer().worlds){
                    if (world.time >= 13000){
                        world.time = 1000
                    }
                }
                return true
            }
            else if ((((Main.peopleVoted + 1)/Bukkit.getOnlinePlayers().size.toFloat())* 100) >= 60){
                Main.peopleVoted = Main.peopleVoted + 1
                Bukkit.getServer().broadcastMessage("§6Good Night Sanctuary!")
                for (player in Bukkit.getOnlinePlayers()){
                    player.sendMessage("§aGood Night ${player.displayName}")
                }
                Main.peopleVoted = 0
                return true
            }
            else if (((((Main.peopleVoted + 1)/Bukkit.getOnlinePlayers().size.toFloat())* 100) < 60) && sender is Player){
                Main.peopleVoted = Main.peopleVoted + 1
                Bukkit.getServer().broadcastMessage("${sender.displayName} says goodnight to the Sanctuary.")
                var equalSigns : Int = ((((Main.peopleVoted + 1)/Bukkit.getOnlinePlayers().size.toFloat())* 100)/5).toInt()
                var message = StringBuilder()
                message.append("[")
                for (eq in 0 until 20){
                    if (equalSigns > 1){
                        message.append("=")
                        equalSigns -= 1
                    }
                    else if (equalSigns > 0){
                        message.append(">")
                        equalSigns -= 1
                    }
                    else {
                        message.append(" ")
                    }
                }
                message.append("] ${(((Main.peopleVoted + 1)/Bukkit.getOnlinePlayers().size.toFloat())* 100).toInt()}% way to new day.")
                Bukkit.getServer().broadcastMessage(message.toString())
                return true
            }
            else {
                Main.discordBot.log("User ${sender.name} tried to force a goodnight vote, failed due to invalid permissions.")
                sender.sendMessage("§cYou do not have enough permission to run this command!")
                return true
            }
        }
        else {
            Main.discordBot.log("User ${sender.name} tried to cast a goodnight vote, failed due to invalid permissions.")
            sender.sendMessage("§cYou do not have enough permission to run this command!")
            return true
        }
    }
}