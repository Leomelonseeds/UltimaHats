package com.leomelonseeds.ultimahats.inv;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import com.leomelonseeds.ultimahats.UltimaHats;

public interface HatInventory {
    InventoryManager manager = UltimaHats.getPlugin().getInvs();
    
    public void updateInventory();
    
    public void registerClick(int slot, ClickType type);
    
    public Inventory getInventory();
}
