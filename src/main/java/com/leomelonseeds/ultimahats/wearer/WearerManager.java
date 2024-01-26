package com.leomelonseeds.ultimahats.wearer;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class WearerManager {
    
    Set<Wearer> wearers;
    
    public WearerManager() {
        wearers = new HashSet<>();
    }

    /**
     * Adds a wearer, overriding duplicate players
     * 
     * @param w
     */
    public void addWearer(Wearer wearer) {
        removeWearer(wearer.getPlayer());
        wearers.add(wearer);
    }
    
    /**
     * Removes a wearer from the list
     * 
     * @param player
     */
    public void removeWearer(Player player) {
        for (Wearer w : new HashSet<>(wearers)) {
            if (!w.getPlayer().equals(player)) {
                continue;
            }
            
            if (w instanceof AnimatedWearer) {
                ((AnimatedWearer) w).cancel();
            }
            wearers.remove(w);
            player.getInventory().setHelmet(null);
            break;
        }
    }
    
    /**
     * Checks if a player is wearing any hat
     * 
     * @param player
     * @return
     */
    public boolean isWearing(Player player) {
        for (Wearer w : wearers) {
            if (w.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }
    
    public Set<Wearer> getWearers() {
        return wearers;
    }
}
