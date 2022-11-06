package com.leomelonseeds.ultimahats.util;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.db.Callback;

public class ItemUtils {
    
    /**
     * Applies the player's saved hat
     * 
     * @param player
     */
    public static void applyHat(Player player) {
        String result = UltimaHats.getPlugin().getSQL().getHat(player.getUniqueId());
        if (result == null) {
            return;
        }
        
        String hat = result;
        applyHat(player, hat);
    }
    
    /**
     * Applies a hat to player
     * 
     * @param player
     * @param hat
     */
    public static void applyHat(Player player, String hat) {
        
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
     * Make an item from the specified configuration section.
     * Extra args used for constructing the GUI items.
     * 
     * @param section
     * @param gui
     * @param player
     * @return
     */
    public static ItemStack makeItem(ConfigurationSection section, boolean gui, Player player, Callback callback) {
        // Check if material and item exist
        Material material = Material.getMaterial(section.getString("item"));
        String name = section.getString("name");
        if (material == null || name == null) {
            return null;
        }
        
        // Check if playerhead/banner conditions met
        if ((material == Material.PLAYER_HEAD && !section.contains("value")) ||
                material.toString().contains("BANNER") && !section.contains("patterns")) {
            return null;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ConfigUtils.toComponent(name));
        
        // Set lore if exists
        List<String> lore = section.getStringList("lore");
        if (gui) {
            ConfigurationSection strings = UltimaHats.getPlugin().getConfig().getConfigurationSection("lore"); 
        }
        return null;
    }
    
    // Make skull from base64 string
    private ItemStack makeSkull(String base64EncodedString) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64EncodedString));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

}
