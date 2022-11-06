package com.leomelonseeds.ultimahats;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.leomelonseeds.ultimahats.db.SQLManager;
import com.leomelonseeds.ultimahats.inv.InventoryManager;

public class UltimaHats extends JavaPlugin {
    
    public static UltimaHats plugin;
    private InventoryManager invs;
    private SQLManager sqlManager;
    
    @Override
    public void onEnable() {
        
        log("Enabling UltimaHats...");
        plugin = this;
        invs = new InventoryManager();
        
        log("Saving config files");
        saveDefaultConfig();
        saveIfNotPresent("hats.yml");
        log("Config files saved!");
        
        log("Initializing database...");
        setupDatabase();
        log("Database setup complete.");
        
        
        
    }
    
    private void setupDatabase() {
        sqlManager = new SQLManager(this);
        log("Testing SQL connection...");
        if (!sqlManager.testConnection()) {
            log("The plugin cannot function without the database. Shutting down now...");
            this.getPluginLoader().disablePlugin(this);
        }
        log("SQL connection test complete.");

        log("Setting up default tables...");
        sqlManager.setupTables();
    }
    
    /**
     * Save a resource if it is not present
    *
    * @param resourcePath the path to the resource
    */
   private void saveIfNotPresent(String resourcePath) {
       File file = new File(getDataFolder(), resourcePath);
       if (!file.exists()) {
           saveResource(resourcePath, false);
       }
   }
    
    @Override
    public void onDisable() {
        log("Closing database...");
        sqlManager.close();
        log("Have a nice day!");
    }
    
    public static UltimaHats getPlugin() {
        return plugin;
    }
    
    public void log(String message) {
        Bukkit.getLogger().log(Level.INFO, "[UltimaHats] " + message);
    }
    
    public SQLManager getSQL() {
        return sqlManager;
    }
    
    public InventoryManager getInvs() {
        return invs;
    }
}
