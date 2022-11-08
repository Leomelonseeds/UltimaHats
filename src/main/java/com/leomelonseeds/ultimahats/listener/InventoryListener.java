package com.leomelonseeds.ultimahats.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.inv.HatInventory;
import com.leomelonseeds.ultimahats.inv.InventoryManager;

public class InventoryListener implements Listener {
    
    private UltimaHats plugin;
    
    public InventoryListener(UltimaHats plugin) {
        this.plugin = plugin;
    }
    
    /** Handle clicking of custom GUIs */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null) {
            return;
        }

        // Don't allow applying/taking off hats if custom one worn
        Player player = (Player) event.getWhoClicked();
        if (inv instanceof PlayerInventory) {
            if (event.getSlot() == 39 && plugin.getWearers().isWearing(player)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Checks for custom GUIs
        InventoryManager manager = plugin.getInvs();
        if (!(manager.getInventory(player) instanceof HatInventory)) {
            return;
        }
        
        if (inv.equals(event.getView().getBottomInventory()) && event.getClick().isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        
        if (!inv.equals(event.getView().getTopInventory())){
            return; 
        }
        
        event.setCancelled(true);
        manager.getInventory(player).registerClick(event.getSlot(), event.getClick());
    }
    
    /** Unregister custom inventories when they are closed. */
    @EventHandler
    public void unregisterCustomInventories(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        
        // Unregister
        InventoryManager manager = plugin.getInvs();
        if (manager.getInventory(player) instanceof HatInventory) {
            manager.removePlayer(player);
        }
    }
}
