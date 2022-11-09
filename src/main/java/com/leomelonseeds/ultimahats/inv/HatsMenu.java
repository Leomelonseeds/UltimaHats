package com.leomelonseeds.ultimahats.inv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

import net.milkbowl.vault.economy.Economy;

public class HatsMenu implements HatInventory {
    
    private Player player;
    private Inventory inv;
    private int page;
    private int hatsSize;
    private UltimaHats plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration hatsConfig;
    
    public HatsMenu(Player player) {
        this.player = player;
        this.page = 0;
        this.plugin = UltimaHats.getPlugin();
        this.mainConfig = plugin.getConfig();
        this.hatsConfig = ConfigUtils.getConfigFile("hats.yml");
        this.hatsSize = mainConfig.getInt("mainGUI.hatsSize");
        int size = mainConfig.getInt("mainGUI.size");
        String title = mainConfig.getString("mainGUI.title");
        
        inv = Bukkit.createInventory(null, size, ConfigUtils.toComponent(title));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        // Run 1 tick later to properly update everything
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            inv.clear();
            
            // Fill items
            if (mainConfig.getBoolean("mainGUI.fill.enabled")) {
                ItemStack fill = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.fill"));
                int startIndex = mainConfig.getInt("mainGUI.fill.start");
                int endIndex = mainConfig.getInt("mainGUI.fill.end");
                for (int i = startIndex; i <= endIndex; i++) {
                    inv.setItem(i, fill);
                }
            }
            
            // Unequip item
            ItemStack unequip = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.unequip"));
            inv.setItem(mainConfig.getInt("mainGUI.unequip.slot"), unequip);
            
            // Extra items
            for (String key : mainConfig.getConfigurationSection("mainGUI.extra-items").getKeys(false)) {
                String path = "mainGUI.extra-items." + key;
                ItemStack item = ItemUtils.makeItem(mainConfig.getConfigurationSection(path));
                inv.setItem(mainConfig.getInt(path + ".slot"), item);
            }
            
            // Add pagination if too many hats
            Set<String> keys = hatsConfig.getKeys(false);
            List<String> hatKeys = new ArrayList<>(keys);
            int keySize = hatKeys.size();
            double maxPages = Math.ceil((double) keySize / hatsSize);
            
            // Epic pagination
            if (page > 0) {
                ItemStack lastPage = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.last-page"));
                inv.setItem(mainConfig.getInt("mainGUI.last-page.slot"), lastPage);
            }
            
            if (page < maxPages - 1) {
                ItemStack nextPage = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.next-page"));
                inv.setItem(mainConfig.getInt("mainGUI.next-page.slot"), nextPage);
            }
            
            // Finally set all the hat items
            for (int i = page * hatsSize; i < Math.min(keySize, page * hatsSize + hatsSize); i++) {
                ItemStack hat = ItemUtils.makeItem(hatsConfig.getConfigurationSection(hatKeys.get(i)), true, player);
                inv.setItem(i % hatsSize, hat);
            }
        }, 1);
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        if (type != ClickType.LEFT) {
            return;
        }
        
        ItemStack item = inv.getItem(slot);
        if (item == null) {
            return;
        }
        
        // Get the config section of the clicked item
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(new NamespacedKey(UltimaHats.getPlugin(), "hat"),
                PersistentDataType.STRING)) {
            return;
        }
        String clicked = meta.getPersistentDataContainer().get(new NamespacedKey(UltimaHats.getPlugin(), "hat"),
                PersistentDataType.STRING);
        
        // Check page/unequip items
        if (clicked.equals("next-page")) {
            page++;
            updateInventory();
            return;
        }
        
        if (clicked.equals("last-page")) {
            page--;
            updateInventory();
            return;
        }
        
        if (clicked.equals("unequip") && ItemUtils.removeHat(player)) {
            updateInventory();
            return;
        }
        
        // Check through extra items and execute commands
        Set<String> extras = mainConfig.getConfigurationSection("mainGUI.extra-items").getKeys(false);
        if (extras.contains(clicked)) {
            executeCommands(mainConfig.getConfigurationSection("mainGUI.extra-items." + clicked));
            return;
        }
        
        // Check through hats to see if player clicked on a hat
        Set<String> hats = hatsConfig.getKeys(false);
        if (hats.contains(clicked)) {
            // Don't do anything if player has hat selected
            String currentHat = plugin.getSQL().getHat(player.getUniqueId());
            if (currentHat != null && currentHat.equals(clicked)) {
                return;
            }
            
            int requirementStatus = ItemUtils.meetsRequirements(player, hatsConfig.getConfigurationSection(clicked + ".requirements"));
            
            // Select hat if player owns it
            if (requirementStatus == 1 || ItemUtils.purchasedHat(player, clicked)) {
                if (ItemUtils.applyHat(player, clicked)) {
                    executeCommands(hatsConfig.getConfigurationSection(clicked));
                    updateInventory();
                }
                return;
            }
            
            // Hat is buyable - fetch cost and make purchase
            // At this point the plugin already knows an economy plugin exists
            if (requirementStatus == 0) {
                Economy econ = plugin.getEcon();
                double cost = hatsConfig.getDouble(clicked + ".requirements.cost");
                double bal = econ.getBalance(player);
                if (bal < cost) {
                    player.sendMessage(ConfigUtils.getString("not-enough-money", player));
                    return;
                } else {
                    new ConfirmAction("Purchase " + clicked + " for $" + cost, player, this, confirm -> {
                        if (!confirm) {
                            return;
                        }
                        econ.withdrawPlayer(player, cost);
                        plugin.getSQL().saveNewHat(player.getUniqueId(), clicked);
                        String purchase = ConfigUtils.getString("hat-purchased", player);
                        purchase = purchase.replaceAll("%hat%", hatsConfig.getString(clicked + ".name"));
                        purchase = purchase.replaceAll("%cost%", "" + hatsConfig.getDouble(clicked + ".requirements.cost"));
                        player.sendMessage(ConfigUtils.toComponent(purchase));
                    });
                    updateInventory();
                    return;
                }
            }
            
            // Player has not unlocked hat
            if (requirementStatus == -1) {
                player.sendMessage(ConfigUtils.getString("requirements-not-met", player));
                return;
            }
        }
    }
    
    // Execute commands for a config section, if there are any
    private void executeCommands(ConfigurationSection section) {
        if (section.contains("commands")) {
            for (String command : section.getStringList("commands")) {
                command = command.replaceAll("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
