package net.pengtul.pengcord.commands
//
//import net.pengtul.pengcord.util.LogType
//import net.pengtul.pengcord.data.interact.TypeOfUniqueID
//import net.pengtul.pengcord.main.Main
//import org.bukkit.command.Command
//import org.bukkit.command.CommandExecutor
//import org.bukkit.command.CommandSender
//import org.bukkit.entity.Player
//
//class Reply: CommandExecutor {
//    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        if (!sender.hasPermission("pengcord.command.reply") || sender !is Player) {
//            sender.sendMessage("§cInvalid Permissions!")
//            return false
//        }
//
//        val id = args[0].toLongOrNull()
//
//        if (id == null) {
//            sender.sendMessage("Invalid Id!")
//            return false
//        }
//
//        id.let {
//            val content = args[1]
//            if (content.isNotBlank()) {
//                // check if muted
//                Main.scheduler.runTaskAsynchronously(Main.pengcord, Runnable {
//                    if (!Main.database.playerIsVerified(TypeOfUniqueID.MinecraftTypeOfUniqueID(sender.uniqueId))) {
//                        return@Runnable
//                    }
//                    if (Main.database.checkIfPlayerMuted(sender.uniqueId)) {
//                        Main.discordBot.log(
//                            LogType.PlayerMuted, "<${sender.name}> Attempted to say \" $content \"" +
//                                "\nbut user is muted!")
//                        Main.serverLogger.info("[MC-EVENT-PLAYERCHAT]: <${sender.name}> Attempted to say \" $content \"" +
//                                "\nbut user is muted!" )
//                        return@Runnable
//                    }
//                    if (Main.discordBot.chatFilterRegex.containsMatchIn(content.lowercase())) {
//                        sender.sendMessage(Main.serverConfig.filteredMessage)
//                        Main.discordBot.log(
//                            LogType.ChatFilter,
//                            "User ${sender.name} (${sender.uniqueId}) tripped chat filter with message $content"
//                        )
//                        val matchedWords =
//                            Main.discordBot.chatFilterRegex.findAll(content.lowercase()).joinToString()
//                        Main.database.addFilterAlertToPlayer(
//                            player = sender.uniqueId,
//                            w = matchedWords,
//                            content
//                        ).onFailure { exception ->
//                            Main.serverLogger.warning("[ChatFilter] [SQLError]: Failed to add filter alert to ${sender.name} (${sender.uniqueId}) due to error: $exception")
//                            Main.discordBot.log(
//                                LogType.GenericError,
//                                "Failed to add filter alert to ${sender.name} (${sender.uniqueId}) due to error: $exception"
//                            )
//                        }
//                    } else {
//                        Main.database.playerGetByUUID(sender.uniqueId)?.let { player ->
//                            Main.discordBot.sendMessagetoWebhook(con)
//                        }
//                    }
//                })
//                return true
//            } else {
//                return false
//            }
//        }
//    }
//}