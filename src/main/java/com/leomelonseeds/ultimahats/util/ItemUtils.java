package com.leomelonseeds.ultimahats.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.wearer.AnimatedWearer;
import com.leomelonseeds.ultimahats.wearer.Wearer;
import com.leomelonseeds.ultimahats.wearer.WearerManager;

import me.clip.placeholderapi.PlaceholderAPI;

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
        // Make sure section exists if loading from db
        ConfigurationSection section = ConfigUtils.getConfigFile("hats.yml").getConfigurationSection(hat);
        if (section == null) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to find " + player.getName() + "'s saved hat " + hat);
            return;
        }
        
        // Check if player is already wearing a thing
        if (!UltimaHats.getPlugin().getConfig().getBoolean("force-remove-helmets")) {
            player.sendMessage(ConfigUtils.getString("armor-equipped"));
            return;
        }
        
        // Initialize wearer
        Wearer wearer = new Wearer(player, hat);
        if (section.contains("frames")) {
            wearer = new AnimatedWearer(player, hat);
        }
        
        // Check if the item can be made
        if (!wearer.initializeHat()) {
            Bukkit.getLogger().log(Level.WARNING, "The hat '" + hat + "' could not be initialized (incorrect config?)");
            return;
        }
        
        // If all checks passed, add to wearer list
        UltimaHats.getPlugin().getWearers().addWearer(wearer);
        String msg = ConfigUtils.getString("hat-selected");
        msg = msg.replaceAll("%hat%", section.getString("name"));
        player.sendMessage(ConfigUtils.toComponent(msg));
    }
    
    /**
     * Applies an item to the player. If an item is already on the
     * player's head, it is either placed into inventory or dropped.
     * 
     * @param player
     * @param item
     */
    public static void applyItem(Player player, ItemStack item) {
        if (player.getInventory().getHelmet().getType() != Material.AIR) {
            ItemStack helmet = player.getInventory().getHelmet();
            HashMap<Integer, ItemStack> extra = player.getInventory().addItem(helmet);
            if (!extra.isEmpty()) {
                player.getWorld().dropItem(player.getLocation(), helmet);
            }
            player.sendMessage(ConfigUtils.getString("armor-removed"));
        }
        player.getInventory().setHelmet(item);
    }
    
    /**
     * Removes player's currently equipped hat, if there is one
     * 
     * @param player
     * @return true if a hat was removed
     */
    public static boolean removeHat(Player player) {
        WearerManager wm = UltimaHats.getPlugin().getWearers();
        if (!wm.isWearing(player)) {
            return false;
        }
        player.getInventory().setHelmet(null);
        player.sendMessage(ConfigUtils.getString("hat-unequipped"));
        return true;
    }
    
    /**
     * Returns true if the player has bought this hat
     * 
     * @param player
     * @param hat
     * @return
     */
    public static boolean purchasedHat(Player player, String hat) {
        String owned = UltimaHats.getPlugin().getSQL().getOwnedHats(player.getUniqueId());
        List<String> hats = Arrays.asList(owned.split(","));
        if (hats.contains(hat)) {
            return true;
        }
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
        
        // Check hat requirements
        if (requirements.contains("hats")) {
            List<String> hats = requirements.getStringList("hats");
            for (String h : hats) {
                if (!ownsHat(player, h)) {
                    return -1;
                }
            }
        }
        
        // Check permission requirements
        if (requirements.contains("permissions")) {
            List<String> permissions = requirements.getStringList("permissions");
            for (String p : permissions) {
                if (!player.hasPermission(p)) {
                    return -1;
                }
            }
        }
        
        // Check placeholder requirements
        if (requirements.contains("placeholders") && UltimaHats.getPlugin().hasPAPI()) {
            List<String> placeholders = requirements.getStringList("placeholders");
            for (String p : placeholders) {
                // Set placeholders from PAPI
                String toCompare = PlaceholderAPI.setPlaceholders(player, p);
                int comparatorIndex = -1;
                String comparator = null;
                
                // Find first occurence of comparator
                String[] comparators = {">=", "<=", "=", "<", ">"};
                for (String c : comparators) {
                    if (toCompare.contains(c)) {
                        comparatorIndex = toCompare.indexOf(c);
                        comparator = c;
                        break;
                    }
                }
                
                // Error if not parseable
                if (comparatorIndex == -1 || comparator == null || comparatorIndex + comparator.length() > toCompare.length()) {
                    Bukkit.getLogger().log(Level.WARNING, "The requirement '" + p + "' is incorrectly defined!");
                    continue;
                }
                
                String left = toCompare.substring(0, comparatorIndex);
                String right = toCompare.substring(comparatorIndex + comparator.length());
                boolean success = false;
                try {
                    // Check if can be parsed into numbers
                    double leftNum = Double.parseDouble(left);
                    double rightNum = Double.parseDouble(right);
                    switch (comparator) {
                    case ">=":
                        success = leftNum >= rightNum;
                        break;
                    case "<=":
                        success = leftNum <= rightNum;
                        break;
                    case "=":
                        success = leftNum == rightNum;
                        break;
                    case "<":
                        success = leftNum < rightNum;
                        break;
                    case ">":
                        success = leftNum > rightNum;
                        break;
                    }
                } catch (NumberFormatException e) {
                    // If not, Compare as strings instead
                    if (!comparator.equals("=")) {
                        Bukkit.getLogger().log(Level.WARNING, "The requirement '" + p + "' is incorrectly defined!");
                        continue;
                    }
                    success = left.equals(right);
                }
                
                if (!success) {
                    return -1;
                }
            }
        }
        
        // Check cost requirements. Don't need to check if player owns hat, since
        // if they do, they already met the requirements.
        if (requirements.contains("cost") && UltimaHats.getPlugin().hasEconomy()) {
            return 0;
        }
        return 1;
    }
    
    /**
     * Check if a player can select a hat
     * 
     * @param player
     * @param hat
     * @return
     */
    private static boolean ownsHat(Player player, String hat) {
        FileConfiguration hatsConfig = ConfigUtils.getConfigFile("hats.yml");
        ConfigurationSection reqs = hatsConfig.getConfigurationSection(hat + ".requirements");
        return purchasedHat(player, hat) || meetsRequirements(player, reqs) == 1;
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
        String guiHat = section.getName();
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
        
        // Add persistent data for this item for GUI use
        meta.getPersistentDataContainer().set(new NamespacedKey(UltimaHats.getPlugin(), "hat"),
                PersistentDataType.STRING, guiHat);
        
        item.setItemMeta(meta);
        return item;
    }
}
