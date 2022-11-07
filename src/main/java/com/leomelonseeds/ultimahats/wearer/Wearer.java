package com.leomelonseeds.ultimahats.wearer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

public class Wearer {
    
    protected Player player;
    protected String hat;
    
    public Wearer(Player player, String hat) {
        this.player = player;
        this.hat = hat;
    }
    
    /**
     * Initializes the hat
     * 
     * @return true if it was successfully initialized
     */
    public boolean initializeHat() {
        ItemStack item = ItemUtils.makeItem(ConfigUtils.getConfigFile("hats.yml").getConfigurationSection(hat));
        if (item == null) {
            return false;
        }
        ItemUtils.applyItem(player, item);
        return true;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getHat() {
        return hat;
    }
}
