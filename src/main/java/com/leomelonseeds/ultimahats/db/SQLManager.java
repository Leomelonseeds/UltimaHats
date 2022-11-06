package com.leomelonseeds.ultimahats.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import com.leomelonseeds.ultimahats.UltimaHats;

public class SQLManager {

    private final DBConnectionManager conn;
    private UltimaHats plugin;
    private Logger logger;
    private BukkitScheduler scheduler;
    
    public SQLManager(UltimaHats plugin) {
        this.plugin = plugin;
        logger = Bukkit.getLogger();
        scheduler = Bukkit.getScheduler();
        conn = new DBConnectionManager(plugin);
    }
    
    /**
     * Checks whether the connection to the database has been established
    *
    * @return whether the execution was successful
    */
    public boolean testConnection() {
        try {
            logger.log(Level.INFO, "Testing SQL connection...");
            conn.getConnection();
            logger.log(Level.INFO, "Connection established!");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "The SQL connection failed! Check your config.");
            return false;
        }
        return true;
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        conn.closePool();
    }
   
    /**
     * Initial setup of SQL tables
     */
    public void setupTables() {
        scheduler.runTaskAsynchronously(plugin, () -> {
            // Player table:
            // Stores a UUID as the primary key
            // "hat" is the player's currently selected hat, which can be null
            // "purchased" is a json string list of the hats a player has purchased
            try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS ultimahats_players(
                            uuid CHAR(36) NOT NULL,
                            hat TEXT,
                            purchased TEXT,
                            PRIMARY KEY (uuid)
                    );
                    """
            )) {
                stmt.execute();
                logger.log(Level.INFO, "[UltimaHats] Setup the players table!");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to setup the players table!");
            }
        });
    }
    
    /**
     * Create an entry for a new player
     *
     * @param uuid
     */
    public void createPlayer(UUID uuid, Callback callback) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                    "INSERT IGNORE INTO ultimahats_players(uuid) VALUE(?);"
            )) {
                stmt.setString(1, uuid.toString());
                stmt.execute();
                scheduler.runTask(plugin, () -> callback.onQueryDone(null));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to create a new entry for " + Bukkit.getPlayer(uuid).getName());
            }
        });
    }
    
    /**
     * Gets a player's current hat from the database
     *
     * @param uuid
     * @param callback
     */
    public String getHat(UUID uuid) {
        try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                "SELECT hat FROM ultimahats_players WHERE uuid = ?;"
        )) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();
            String hat = null;
            if (resultSet.next()) {
                hat = resultSet.getString("hat");
            }
            return hat;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get hat for " + Bukkit.getPlayer(uuid).getName());
        }
        return null;
    }
    
    /**
     * Save a players hat
     *
     * @param uuid
     */
    public void savePlayerHat(UUID uuid, String hat) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                    "UPDATE ultimahats_players SET hat = ? WHERE uuid = ?;"
            )) {
                stmt.setString(1, hat);
                stmt.setString(2, uuid.toString());
                stmt.execute();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to set hat for " + Bukkit.getPlayer(uuid).getName());
            }
        });
    }
    
    /**
     * Gets a player's owned hats from the database
     *
     * @param uuid
     * @param callback
     */
    public String getOwnedHats(UUID uuid) {
        try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                "SELECT purchased FROM ultimahats_players WHERE uuid = ?;"
        )) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();
            String hats = null;
            if (resultSet.next()) {
                hats = resultSet.getString("purchased");
            }
            return hats; 
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get hat for " + Bukkit.getPlayer(uuid).getName());
        }
        return null;
    }
    
    /**
     * Add a new owned hat to player's database
     *
     * @param uuid
     */
    public void saveNewHat(UUID uuid, String newHat) {
        String result = getOwnedHats(uuid);
        String toSave = newHat;
        if (result != null) {
            toSave = result + "," + newHat;
        }
        saveOwnedHats(uuid, toSave);
    }
    
    /**
     * Save a new set of owned hats for a player
     *
     * @param uuid
     */
    public void saveOwnedHats(UUID uuid, String hats) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try (Connection c = conn.getConnection(); PreparedStatement stmt = c.prepareStatement(
                    "UPDATE ultimahats_players SET purchased = ? WHERE uuid = ?;"
            )) {
                stmt.setString(1, hats);
                stmt.setString(2, uuid.toString());
                stmt.execute();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to set purchased hats for " + Bukkit.getPlayer(uuid).getName());
            }
        });
    }
}
