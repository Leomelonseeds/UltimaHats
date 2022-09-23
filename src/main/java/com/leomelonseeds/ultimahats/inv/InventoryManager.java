package com.leomelonseeds.ultimahats.inv;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class InventoryManager {

    private Map<Player, HatInventory> inventoryCache;
    
    public InventoryManager() {
        inventoryCache = new HashMap<>();
    }
    
    public HatInventory getInventory(Player player) {
        return inventoryCache.get(player);
    }
    
    // Registers and opens an inventory
    public void registerInventory(Player player, HatInventory inv) {
        inv.updateInventory();
        player.openInventory(inv.getInventory());
        inventoryCache.put(player, inv);
    }
    
    public void removePlayer(Player player) {
        inventoryCache.remove(player);
    }
}
