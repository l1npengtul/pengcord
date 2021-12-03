package net.pengtul.pengcord.data

data class ServerConfig(
    // User-editable discord settings
    val enable_sync: Boolean,
    val bot_token: String?,
    val bot_prefix: String?,
    val filter_join_leave_message: Boolean,
    val filter_ingame_message: Boolean,
    val filter_annoncements: Boolean,
    val server_message_prefix: String?,
    var server_admin_roles: List<String>,
    var bot_server: String?,
    var bot_chatsync: String?,
    var bot_command: String?,
    var bot_logs: String?,
    // Bot-Managed Discord Settings
    var webhook_id: String?,
    var webhook_token: String?,
    // Word Filter Settings
    val enable_literally_nineteeneightyfour: Boolean,
    val enable_literally_nineteeneightyfour_on_discord: Boolean,
    var banned_words: List<String>,
    val word_filtered_message: String?

)