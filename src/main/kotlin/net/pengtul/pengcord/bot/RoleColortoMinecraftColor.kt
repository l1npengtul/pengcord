package net.pengtul.pengcord.bot

import net.pengtul.pengcord.main.Main
import org.javacord.api.entity.user.User
import java.awt.Color
import java.util.*

public class RoleColortoMinecraftColor {
    companion object{
        public fun getDiscordtoMinecraft(user: User, serverId: String): String{
            var returnValue: String = "§7";
            Main.discordBot.discordApi.getServerById(serverId).ifPresent { server ->
                var userRoleColor: Optional<Color> = user.getRoleColor(server);
                if (userRoleColor.isPresent){
                    when(userRoleColor.get()) {
                        Color.DARK_GRAY -> {
                            returnValue = "§8";
                        }
                        Color.darkGray -> {
                            returnValue = "§8";
                        }
                        Color.GRAY -> {
                            returnValue = "§7";
                        }
                        Color.gray -> {
                            returnValue = "§7";
                        }
                        Color.LIGHT_GRAY -> {
                            returnValue = "§7";
                        }
                        Color.lightGray -> {
                            returnValue = "§7";
                        }
                        Color.BLACK  -> {
                            returnValue = "§0";
                        }
                        Color.black -> {
                            returnValue = "§0";
                        }
                        Color.BLUE -> {
                            returnValue = "§9";
                        }
                        Color.blue -> {
                            returnValue = "§9";
                        }
                        Color.GREEN -> {
                            returnValue = "§a"
                        }
                        Color.green -> {
                            returnValue = "§a"
                        }
                        Color.CYAN -> {
                            returnValue = "§a"
                        }
                        Color.cyan -> {
                            returnValue = "§a"
                        }
                        Color.RED -> {
                            returnValue = "§b"
                        }
                        Color.red -> {
                            returnValue = "§b"
                        }
                        Color.MAGENTA -> {
                            returnValue = "§5"
                        }
                        Color.magenta -> {
                            returnValue = "§5"
                        }
                        Color.ORANGE -> {
                            returnValue = "§6"
                        }
                        Color.orange -> {
                            returnValue = "§6"
                        }
                        Color.pink -> {
                            returnValue = "§d"
                        }
                        Color.PINK -> {
                            returnValue = "§d"
                        }
                        Color.YELLOW -> {
                            returnValue = "§e"
                        }
                        Color.yellow -> {
                            returnValue = "§e"
                        }
                        Color.WHITE -> {
                            returnValue = "§f"
                        }
                        Color.white -> {
                            returnValue = "§f"
                        }
                        else -> {
                            returnValue = "§7";
                        }
                    }
                }
                else {
                    returnValue = "§7";
                }
            }

            return returnValue
        }
    }
}