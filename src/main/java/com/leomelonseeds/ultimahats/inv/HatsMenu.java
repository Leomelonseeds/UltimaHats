package com.leomelonseeds.ultimahats.inv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

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
        ItemStack unequip = ItemUtils.makeItem(mainConfig.getConfigurationSection("unequip"));
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
            ItemStack nextPage = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.next-page"));
            inv.setItem(mainConfig.getInt("mainGUI.next-page.slot"), nextPage);
        }
        
        if (page < maxPages - 1) {
            ItemStack lastPage = ItemUtils.makeItem(mainConfig.getConfigurationSection("mainGUI.last-page"));
            inv.setItem(mainConfig.getInt("mainGUI.last-page.slot"), lastPage);
        }
        
        // Finally set all the hat items
        for (int i = page * hatsSize; i < Math.min(keySize, page * hatsSize + hatsSize); i++) {
            ItemStack hat = ItemUtils.makeItem(hatsConfig.getConfigurationSection(hatKeys.get(i)), true, player);
            inv.setItem(i % hatsSize, hat);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
