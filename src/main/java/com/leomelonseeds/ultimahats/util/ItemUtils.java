package com.leomelonseeds.ultimahats.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.leomelonseeds.ultimahats.UltimaHats;

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
     * Returns true if the player has bought this hat
     * 
     * @param player
     * @param hat
     * @return
     */
    public static boolean purchasedHat(Player player, String hat) {
        return false;
    }
    
    /**
     * Checks if a player meets the requirements for a hat
     * 
     * @param player
     * @param hat
     * @return 1 if player meets all requirements and there is no cost,
     * 0 if player meets all requirements and there is a cost,
     * -1 if player does not meet all requirements.
     */
    public static int meetsRequirements(Player player, ConfigurationSection requirements) {
        if (requirements == null) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Make an item from the specified configuration section.
     * Used for making the physical item to wear
     * 
     * @param section
     * @return
     */
    public static ItemStack makeItem(ConfigurationSection section) {
        return makeItem(section, false, null);
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
    public static ItemStack makeItem(ConfigurationSection section, boolean gui, Player player) {
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
            // If player has selected the hat, "selected"
            // If player owns the hat (i.e. he must've also met all requirements), "click to select"
            // If player meets all requirements and also does not have a cost, "click to select"
            // If player meets all requirements and does have a cost, "click to buy"
            // Otherwise, "locked"
            List<String> extraLore;
            ConfigurationSection strings = UltimaHats.getPlugin().getConfig().getConfigurationSection("lore");
            String guiHat = section.getName();
            String currentHat = UltimaHats.getPlugin().getSQL().getHat(player.getUniqueId());
            int requirementStatus = meetsRequirements(player, section.getConfigurationSection("requirements"));
            if (currentHat != null && guiHat.equals(currentHat)) {
                extraLore = strings.getStringList("selected");
            } else if (purchasedHat(player, currentHat) || requirementStatus == 1) {
                extraLore = strings.getStringList("unlocked");
            } else if (requirementStatus == 0) {
                extraLore = new ArrayList<>();
                int cost = section.getInt("requirements.cost");
                for (String s : strings.getStringList("buyable")) {
                    s = s.replaceAll("%cost%", cost + "");
                    extraLore.add(s);
                }
            } else {
                extraLore = strings.getStringList("locked");
            }
            lore.addAll(extraLore);
        }
        meta.lore(ConfigUtils.toComponent(lore));
        
        // Add skull data if necessary
        if (material == Material.PLAYER_HEAD) {
            String base64EncodedString = section.getString("value");
            SkullMeta skullmeta = (SkullMeta) meta;
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", base64EncodedString));
            skullmeta.setPlayerProfile(profile);
        }
        
        // Add banner patterns if necessary
        if (material.toString().contains("BANNER")) {
            BannerMeta bannerMeta = (BannerMeta) meta;
            String patterns = section.getString("patterns");
            Pattern regex = Pattern.compile("Pattern:(\\w+),Color:(\\d+)");
            Matcher matcher = regex.matcher(patterns);
            while (matcher.find()) {
                String bannerPattern = matcher.group(1);
                String color = matcher.group(2);
                byte dyeColor = Byte.parseByte(color);
                bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.getByDyeData(dyeColor), 
                        PatternType.getByIdentifier(bannerPattern)));
            }
        }
        
        // Add glow if exists
        if (section.contains("glow") && section.getBoolean("glow")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
