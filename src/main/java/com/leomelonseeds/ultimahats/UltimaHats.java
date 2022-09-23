package com.leomelonseeds.ultimahats;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.leomelonseeds.ultimahats.inv.InventoryManager;

public class UltimaHats extends JavaPlugin {
    
    public static UltimaHats plugin;
    private InventoryManager invs;
    
    @Override
    public void onEnable() {
        
        plugin = this;
        
        invs = new InventoryManager();
        
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public static UltimaHats getPlugin() {
        return plugin;
    }
    
    public void log(String message) {
        Bukkit.getLogger().log(Level.INFO, "[UltimaHats]" + message);
    }
    
    public InventoryManager getInvs() {
        return invs;
    }
}
