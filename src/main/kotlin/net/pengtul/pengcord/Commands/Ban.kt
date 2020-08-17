package net.pengtul.pengcord.Commands

import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

public class Ban {
    /*override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if (args[2] != null){
            var playerToBan: String? = Bukkit.getServer().getPlayer(args[0])?.uniqueId.toString();
            if (playerToBan != null){
                if (args[2] == null){

                }
                else {
                    var date: Date = Date();
                    var dateToBanUntil: Date = Date();
                    var localDateToWork: LocalDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    when(args[2].last().toLowerCase()){
                        'h' -> {
                            var hours: Long = args[2].dropLast(1).toLong();
                            localDateToWork.plusHours(hours);
                            dateToBanUntil = Date.from(localDateToWork.atZone(ZoneId.systemDefault()).toInstant());
                            Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(playerToBan, )
                        }
                        'd' -> {

                        }
                        'm' -> {

                        }
                        'y' -> {

                        }
                    }
                }
            }
            else if (args.get(0).contains("@")){
                sender.sendMessage("§cYou cannot use selectors with this command!");
            }
            else{
                sender.sendMessage("Could not find player ${args.get(0)}.")
            }
        }
        return false
    }*/
}