package com.leomelonseeds.ultimahats.listener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

public class JoinListener implements Listener {
    
    UltimaHats plugin;
    
    public JoinListener(UltimaHats plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getSQL().createPlayer(player.getUniqueId(), result -> {});
        
        // Apply player's saved hat, if there is one
        // If none was found, try removing existing hats instead
        if (!ItemUtils.applyHat(player)) {
            ItemUtils.removeHat(player);
        }
        
        /* REMOVED TO PREVENT DATA WIPES FROM USER ERROR
        // Check player's owned hats and remove those that don't exist
        // to save on storage space and improve tidyness
        String result = plugin.getSQL().getOwnedHats(player.getUniqueId());
        if (result == null) {
            return;
        }
        String hats = result;
        List<String> owned = Arrays.asList(hats.split(","));
        Set<String> available = ConfigUtils.getConfigFile("hats.yml").getKeys(false);
        for (String s : new ArrayList<>(owned)) {
            if (!available.contains(s)) {
                owned.remove(s);
            }
        }
        String cleaned = String.join(",", owned);
        plugin.getSQL().saveOwnedHats(player.getUniqueId(), cleaned);
        */
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        plugin.getWearers().removeWearer(player);
    }
    
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        World from = e.getFrom();
        World to = player.getWorld();
        
        // Do nothing if both worlds are disabled or both are enabled
        boolean isFrom = ConfigUtils.isHatWorld(from);
        boolean isTo = ConfigUtils.isHatWorld(to);
        if (isFrom == isTo) {
            return;
        }
        
        if (!isTo) {
            ItemUtils.removeHat(player);
        } else {
            ItemUtils.applyHat(player);
        }
    }
}
