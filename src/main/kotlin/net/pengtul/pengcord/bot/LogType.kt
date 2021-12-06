package net.pengtul.pengcord.bot

enum class LogType(val log: String) {
    MCComamndError("[MC-COMMAND-ERROR]: "),
    MCComamndRan("[MC-COMMAND-RAN]: "),
    DSCComamndError("[DSC-COMMAND-ERROR]: "),
    DSCComamndRan("[DSC-COMMAND-RAN]: "),
    PlayerChat("[PLAYERCHAT-SYNC]: "),
    ChatFilter("[CHATFILTER]: "),
    Verification("[VERIFY]: "),
    ServerStartup("[SERVER-START]"),
    ServerShutdown("[SERVER-SHUTDOWN]"),
    Announcement("[SERVER]: "),
    InGameStuff("[SERVER]: "),
    GenericError("[ERROR]: "),
    PlayerWarned("[PL-WARNED]: "),
    PlayerMuted("[PL-MUTED]: "),
    PlayerUnMuted("[PL-UNMUTED]: "),
    PlayerBanned("[PL-BANNED]: "),
    PlayerUnBanned("[PL-UNBANNED]: "),
    PlayerJoin("[PL-JOIN]"),
    PlayerLeave("[PL-LEAVE]"),
    PlayerDie("[PL-DIE]"),
}