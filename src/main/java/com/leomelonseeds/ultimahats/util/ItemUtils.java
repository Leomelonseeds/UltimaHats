package com.leomelonseeds.ultimahats.util;

import org.bukkit.entity.Player;

import com.leomelonseeds.ultimahats.UltimaHats;

public class ItemUtils {
    
    public static void applyHat(Player player) {
        UltimaHats.getPlugin().getSQL().getHat(player.getUniqueId(), result -> {
            if (result == null) {
                return;
            }
            
            String hat = (String) result;
        });
    }

}
