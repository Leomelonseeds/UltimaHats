package com.leomelonseeds.ultimahats.wearer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.leomelonseeds.ultimahats.UltimaHats;
import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

public class AnimatedWearer extends Wearer {
    
    BukkitTask nextBlock;
    List<ItemStack> items;
    List<Integer> frameTimes;

    public AnimatedWearer(Player player, String hat) {
        super(player, hat);
        items = new ArrayList<>();
        frameTimes = new ArrayList<>();
    }
    
    /**
     * Initializes the animated hat
     * 
     * @return true if it was successfully initialized
     */
    @Override
    public boolean initializeHat() {
        ConfigurationSection frames = ConfigUtils.getConfigFile("hats.yml").getConfigurationSection(hat + ".frames");
        for (String key : frames.getKeys(false)) {
            ItemStack item = ItemUtils.makeItem(frames.getConfigurationSection(key));
            int frameTime = frames.getInt(key + ".time");
            if (item == null || frameTime == 0) {
                return false;
            }
            items.add(item);
            frameTimes.add(frameTime);
        }
        startAnimation(0);
        return true;
    }
    
    // Starts the hat animation
    private void startAnimation(int index) {
        ItemStack item = items.get(index);
        int time = frameTimes.get(index);
        ItemUtils.applyItem(player, item);
        nextBlock = new BukkitRunnable() {
            @Override
            public void run() {
                startAnimation(index >= frameTimes.size() - 1 ? 0 : index + 1);
            }
        }.runTaskLater(UltimaHats.getPlugin(), time);
    }
    
    /**
     * Stops the hat from running
     */
    public void cancel() {
        nextBlock.cancel();
    }
}
