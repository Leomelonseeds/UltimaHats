package com.leomelonseeds.ultimahats.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.leomelonseeds.ultimahats.UltimaHats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ConfigUtils {

    // Map of open cached config files
    private static Map<String, FileConfiguration> configCache = new HashMap<>();
    
    /**
     * Get config file from default folder
     * 
     * @param configName
     * @return
     */
    public static FileConfiguration getConfigFile(String configName) {
        // Check for config file in cache
        if (configCache.containsKey(configName)) {
            return configCache.get(configName);
        }

        File file = new File(UltimaHats.getPlugin().getDataFolder().toString(), configName);
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        configCache.put(configName, config);
        return config;
    }
    
    /**
     * Reloads all loaded configs, closing all GUIs
     */
    public static void reloadConfigs() {
        UltimaHats.getPlugin().reloadConfig();
        UltimaHats.getPlugin().getInvs().stopAll();
        configCache.clear();
    }

    /**
     * Get a string from the config's "strings" section
     * Plays an associated sound too, if there is any
     * 
     * @param player
     * @param string
     * @return the parsed string
     */
    public static String getString(String string, Player player) {
        String prefix = UltimaHats.getPlugin().getConfig().getString("strings.prefix");
        String msg = UltimaHats.getPlugin().getConfig().getString("strings." + string);
        sendConfigSound(string, player);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }
    
    /**
     * Send a sound to the player.
     *
     * @param path the key of the sounds in "sounds"
     * @param player the player to send sound to
     */
    public static void sendConfigSound(String path, Player player) {
        ConfigurationSection soundConfig = UltimaHats.getPlugin().getConfig().getConfigurationSection("sounds");

        if (!soundConfig.contains(path)) {
            return;
        }

        Sound sound = Sound.valueOf(soundConfig.getString(path + ".sound"));
        float volume = (float) soundConfig.getDouble(path + ".volume");
        float pitch = (float) soundConfig.getDouble(path + ".pitch");

        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }
    
    /**
     * Get a line, translate it to a component.
     * 
     * @param line
     * @return
     */
    public static Component toComponent(String line) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', line));
    }
    
    /**
     * Get lines to translate to components
     * 
     * @param line
     * @return
     */
    public static List<Component> toComponent(List<String> lines) {
        List<Component> result = new ArrayList<>();
        for (String s : lines) {
            result.add(toComponent(s));
        }
        return result;
    }

    /**
     * Component to plain text!
     * 
     * @param component
     * @return
     */
    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
