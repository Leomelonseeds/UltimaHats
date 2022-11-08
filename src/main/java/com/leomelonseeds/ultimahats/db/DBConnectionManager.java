package com.leomelonseeds.ultimahats.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnectionManager {

    private final UltimaHats plugin;
    private HikariDataSource dataSource;


    public DBConnectionManager(UltimaHats plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    // Sets up either mysql or sqlite
    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        
        String type = plugin.getConfig().getString("storage-method");
        if (type.equalsIgnoreCase("mysql")) {
            config.setJdbcUrl("jdbc:mysql://" + plugin.getConfig().getString("mysql.host")
                    + ":" + plugin.getConfig().getInt("mysql.port")
                    + "/" + plugin.getConfig().getString("mysql.database"));
            config.setUsername(plugin.getConfig().getString("mysql.user"));
            config.setPassword(plugin.getConfig().getString("mysql.password"));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(10);
        } else if (type.equalsIgnoreCase("sql")) {
            File db = new File(plugin.getDataFolder(), "data.db");
            if (!db.exists()) {
                try {
                    db.createNewFile();
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to create the SQL data file! Disabling plugin...");
                    Bukkit.getServer().getPluginManager().disablePlugin(plugin);
                }
            }
            config.setPoolName("UltimaHatsSQLitePool");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:plugins/UltimaHats/data.db");
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Database must be one of 'SQL' or 'MySQL'! Disabling plugin...");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
