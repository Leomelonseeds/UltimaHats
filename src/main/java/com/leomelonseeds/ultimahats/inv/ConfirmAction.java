package com.leomelonseeds.ultimahats.inv;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.leomelonseeds.ultimahats.util.ConfigUtils;

public class ConfirmAction implements HatInventory {
    
    private Inventory inv;
    private ConfirmCallback callback;
    private HatInventory hatinv;
    private Player player;
    
    public ConfirmAction(String action, Player player, HatInventory hatinv, ConfirmCallback callback) {
        this.player = player;
        this.callback = callback;
        this.hatinv = hatinv;
        
        inv = Bukkit.createInventory(null, 27, ConfigUtils.toComponent("Confirm - " + action));
        manager.registerInventory(player, this);
    }

    @Override
    public void updateInventory() {
        // galaxy brain inventory filling
        for (int i = 0; i < 27; i++) {
            int mod = i % 9;
            Material material;
            String name = "";
            if (0 <= mod && mod <= 3) {
                material = Material.EMERALD_BLOCK;
                name = "&aConfirm";
            } else if (5 <= mod && mod <= 8) {
                material = Material.REDSTONE_BLOCK;
                name = "&cCancel";
            } else {
                material = Material.IRON_BARS;
            }
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(ConfigUtils.toComponent(name));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }

    @Override
    public void registerClick(int slot, ClickType type) {
        Material material = inv.getItem(slot).getType();
        
        if (material == Material.IRON_BARS) {
            return;
        }
        
        manager.registerInventory(player, hatinv);
        
        if (material == Material.EMERALD_BLOCK) {
            callback.onConfirm(true);
        } else if (material == Material.REDSTONE_BLOCK) {
            callback.onConfirm(false);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

}
