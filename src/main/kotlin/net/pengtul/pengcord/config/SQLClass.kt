package net.pengtul.pengcord.config

/*
*    SQL DB Connection object
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


import java.sql.*
import net.pengtul.pengcord.main.Main
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File

// NOTE: Currently Unused.

class SQLClass {
    private var dbConnection: Connection
    private var dbStatement: Statement
    private var currentPlugin: Plugin = Bukkit.getServer().pluginManager.getPlugin("pengcord")!!

    init {
        // get plugin
        // establish connection to DB
        Class.forName("org.h2.Driver")
        dbConnection = DriverManager.getConnection("jdbc:h2:plugins${File.separator}pengcord${File.separator}", "pengcord", "")
        dbStatement = dbConnection.createStatement()

        // Create table if it doesnt exist
        val sql: String = "CREATE TABLE IF NOT EXISTS player_data (\n" +
                "PlayerUUID VARCHAR(255)," +
                "PlayerDiscordUUID BIGINT(255)," +
                "PlayerVerified BIT," +
                "PlayerJoinDate BIGINT(255)," +
                "PlayerRole VARCHAR(255)," +
                "\n);"

        try {
            val createTable: PreparedStatement = dbConnection.prepareStatement(sql)
            createTable.executeUpdate()
        }
        catch (e: SQLException){
            Main.ServerLogger.severe("Could not create SQL table!")
            Bukkit.getPluginManager().getPlugin("pengcord")?.let {
                Bukkit.getServer().pluginManager.disablePlugin(it)
            }

        }
    }

    fun updatePlayerVerified(uuid: String){
        currentPlugin.let {
            Bukkit.getScheduler().runTaskAsynchronously(it, Runnable {
                val stmt: PreparedStatement = dbConnection.prepareStatement("SELECT PlayerVerified FROM player_data WHERE PlayerUUID='${uuid}'")
                val resultSet: ResultSet = stmt.executeQuery()
                if(resultSet.next()){
                    if (resultSet.getBoolean("PlayerVerified")){
                        //Main.verifiedPlayerList.add(uuid)
                    }
                }
            })
        }
    }

    fun onPlayerVerified(uuid: String, discordId: String, rank: String){
        Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
            val stmt: PreparedStatement = dbConnection.prepareStatement("INSERT INTO player_data ON DUPLICATE KEY UPDATE VALUES" +
                    "('${uuid}', ${discordId.toLong()}, 1, ${System.currentTimeMillis() / 1000L}), '${rank}'")
            stmt.executeUpdate()
        })
    }

    fun isPlayerVerified(uuid: String): Boolean{
        var rt = false
        Bukkit.getScheduler().runTaskAsynchronously(currentPlugin, Runnable {
            val stmt: PreparedStatement = dbConnection.prepareStatement("SELECT PlayerVerified FROM player_data WHERE PlayerUUID='${uuid}'")
            val resultSet: ResultSet = stmt.executeQuery()
            if (resultSet.next()){
                rt = resultSet.getBoolean("PlayerVerified")
            }
        })
        return rt
    }
}
