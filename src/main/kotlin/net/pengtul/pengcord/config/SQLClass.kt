package net.pengtul.servsync_api.config

import java.sql.*
import net.pengtul.pengcord.main.Main
import net.pengtul.pengcord.main.Player

public class SQLClass (val drive: String?, val ip: String?, val db: String?, val usr: String?, val pswd: String?){

    // SQL Vars
    private lateinit var Driver: SQLDriver;
    private lateinit var SQLIp: String;
    private lateinit var SQLDatabase: String;
    private lateinit var SQLUsername: String;
    private lateinit var SQLPassword: String;
    private var unNull:  Int = 0;

    // DB Object
    private lateinit var connection: Connection;
    private lateinit var SqlStatement: Statement;


    init {
        // get the sql driver type
        try {
            drive?.let {
                this.Driver = SQLDriver.valueOf(drive);
                unNull++;
            }
            ip?.let {
                this.SQLIp = ip;
                unNull++;
            }
            db?.let {
                this.SQLDatabase = db;
                unNull++;
            }
            usr?.let {
                this.SQLUsername = usr;
                unNull++;
            }
            pswd?.let {
                this.SQLPassword = pswd;
                unNull++;
            }
            if (unNull == 5){
                Main.ServerLogger.info("Attempting connection to Database...");

            }
            else{
                Main.ServerLogger.warning("Skipping Database Connection...");
            }
        } catch (e: IllegalArgumentException){

        }
    }

    public fun attemptOpenDB(){
        if(connection != null && !connection.isClosed()){
            return;
        }
        synchronized(this){
            if(connection != null && !connection.isClosed()){
                return;
            }
            when(Driver){
                SQLDriver.MYSQL -> {
                    connection = DriverManager.getConnection("jdbc:mysql://" + this.SQLIp + "/" + this.SQLDatabase, this.SQLUsername, this.SQLPassword);
                    if (connection != null){
                        Main.ServerLogger.info("Sucessfully Connected to MySQL!");
                        SqlStatement = connection.createStatement();
                    }
                    else{
                        Main.ServerLogger.severe("Could not connect to MYSQL!");

                    }
                }
                SQLDriver.POSTGRESQL -> {
                    connection = DriverManager.getConnection("jdbc:postgresql://" + this.SQLIp + "/" + this.SQLDatabase, this.SQLUsername, this.SQLPassword);
                    if (connection != null){
                        Main.ServerLogger.info("Sucessfully Connected to PostgreSQL!");
                        SqlStatement = connection.createStatement();
                    }
                    else{
                        Main.ServerLogger.severe("Could not connect to PostgreSQL!");
                    }
                }
                else -> {
                    Main.ServerLogger.severe("Could Not Connect to Database!");
                }
            }
        }
    }

    public fun getPlayerSQLData(uuid: String): Player?{
        if(connection != null){
            var queryResult: ResultSet = this.SqlStatement.executeQuery("SELECT * FROM Players WHERE UUID = $uuid;");
            while (queryResult.next()){
                return Player(queryResult.getString("UUID"), queryResult.getString("DISCORD_UUID"), queryResult.getBoolean("DISCORD_AUTH_USE"));
            }
        }
        else{
            return null;
        }
        return null;
    }

    public fun setPlayerSQLData(player: Player){
        SqlStatement.executeUpdate("INSERT INTO Players (UUID, DISCORD_UUID, DISCORD_AUTH_USE) VALUES (${player.uuid},${player.uuid_disc},${player.usediscordauth})");
    }

    public fun getConnectionHandler(): Connection{
        return this.connection;
    }
}