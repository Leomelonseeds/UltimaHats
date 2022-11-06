package com.leomelonseeds.ultimahats.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.leomelonseeds.ultimahats.UltimaHats;

public class ItemUtils {
    
    /**
     * Applies a hat to a player
     * 
     * @param player
     */
    public static void applyHat(Player player) {
        UltimaHats.getPlugin().getSQL().getHat(player.getUniqueId(), result -> {
            if (result == null) {
                return;
            }
            
            String hat = (String) result;
        });
    }
    
    /**
     * Removes player's currently equipped hat, if there is one
     * 
     * @param player
     */
    public static void removeHat(Player player) {
        
    }
    
    /**
     * Checks the necessary conditions to see if a player owns a hat
     * 
     * @param player
     * @param hat
     * @return
     */
    public static boolean ownsHat(Player player, String hat) {
        return false;
    }
    
    /**
     * Make a skull from a base 64 string
     * 
     * @param base64EncodedString
     * @return
     */
    public ItemStack makeSkull(String base64EncodedString) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64EncodedString));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

}
