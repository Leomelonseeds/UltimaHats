package com.leomelonseeds.ultimahats.listener;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimahats.UltimaHats;

public class DeathListener implements Listener {

    private UltimaHats plugin;
    
    public DeathListener(UltimaHats plugin) {
        this.plugin = plugin;
    }
    
    // Remove item drop if dead
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Ignore if player not wearing anything
        Player player = e.getPlayer();
        if (plugin.getSQL().getHat(player.getUniqueId()) == null) {
            return;
        }
        
        // Ignore if keep inventory enabled
        if (e.getKeepInventory()) {
            return;
        }
        
        // Retain helmet on death
        ItemStack helmet = player.getInventory().getHelmet();
        List<ItemStack> drops = e.getDrops();
        drops.remove(helmet);
        e.getItemsToKeep().add(helmet);
    }

}
