package com.leomelonseeds.ultimahats;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.leomelonseeds.ultimahats.db.SQLManager;
import com.leomelonseeds.ultimahats.inv.InventoryManager;
import com.leomelonseeds.ultimahats.listener.InventoryListener;
import com.leomelonseeds.ultimahats.listener.JoinListener;
import com.leomelonseeds.ultimahats.wearer.WearerManager;

import net.milkbowl.vault.economy.Economy;

public class UltimaHats extends JavaPlugin {
    
    public static UltimaHats plugin;
    private InventoryManager invs;
    private SQLManager sqlManager;
    private WearerManager wearerManager;
    private Economy econ;
    
    @Override
    public void onEnable() {
        log("Enabling UltimaHats...");
        plugin = this;
        invs = new InventoryManager();
        wearerManager = new WearerManager();
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        getCommand("ultimahats").setExecutor(new HatsCommand());
        
        log("Saving config files");
        saveDefaultConfig();
        saveIfNotPresent("hats.yml");
        log("Config files saved!");
        
        log("Initializing database...");
        setupDatabase();
        log("Database setup complete.");
        
        log("Hooking into Vault...");
        setupVault();
        
        log("UltimaHats ready to wear!");
    }
    
    @Override
    public void onDisable() {
        log("Closing database...");
        sqlManager.close();
        log("Have a nice day!");
    }
    
    /**
     * Checks if plugin has an economy provider
     * 
     * @return
     */
    public boolean hasEconomy() {
        return econ != null;
    }
    
    /**
     * Checks if PlaceholderAPI is there
     * 
     * @return
     */
    public boolean hasPAPI() {
        return Bukkit.getPluginManager().getPlugin("PlacehoderAPI") != null;
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
     * Setup the economy manager
     */
    private void setupVault() {
        RegisteredServiceProvider<Economy> rspE = getServer().getServicesManager().getRegistration(Economy.class);
        if (rspE == null) {
            getLogger().log(Level.WARNING, "Vault not found! Cost requirements will not work");
            return;
        }
        econ = rspE.getProvider();
        log("Economy setup complete.");
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
    
    /**
     * Use this only if hasEconomy returned true
     * 
     * @return
     */
    public Economy getEcon() {
        return econ;
    }
    
    public WearerManager getWearers() {
        return wearerManager;
    }
}
