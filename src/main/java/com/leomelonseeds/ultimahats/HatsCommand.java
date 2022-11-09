package com.leomelonseeds.ultimahats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.leomelonseeds.ultimahats.inv.HatsMenu;
import com.leomelonseeds.ultimahats.util.ConfigUtils;
import com.leomelonseeds.ultimahats.util.ItemUtils;

public class HatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        String action = args.length > 0 ? args[0] : null;
        
        // Check permission
        if (!checkPermission(sender, action)) {
            sendErrorMsg(sender, "You don't have permission for this action.");
            return true;
        }
        
        // Open main hats GUI
        if (args.length == 0) {
            if (sender instanceof Player) {
                new HatsMenu((Player) sender);
                return true;
            }
        }
        
        // Apply a hat regardless of the hats a player owns
        if (action.equalsIgnoreCase("apply")) {
            if (args.length != 3) {
                sendErrorMsg(sender, "Usage: /hats apply [player] [hat]");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sendErrorMsg(sender, "Target " + args[1] + "not found!");
                return true;
            }
            
            if (!ConfigUtils.getConfigFile("hats.yml").contains(args[2])) {
                sendErrorMsg(sender, "Hat " + args[2] + "not found!");
                return true;
            }
            
            if (!ItemUtils.applyHat(player, args[2])) {
                sendErrorMsg(sender, "Could not apply the hat to the player.");
                return true;
            }
            
            sendSuccessMsg(sender, "Applied hat " + args[2] + " to " + args[1]);
            return true;
        }
        
        // Remove a hat from player, if they are wearing any
        if (action.equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                sendErrorMsg(sender, "Usage: /hats remove [player]");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sendErrorMsg(sender, "Target " + args[1] + "not found!");
                return true;
            }
            
            if (!ItemUtils.removeHat(player)) {
                sendErrorMsg(sender, "Target " + args[1] + " is not wearing a custom hat!");
                return true;
            }
            
            sendSuccessMsg(sender, "Removed " + args[1] + "'s worn hat.");
            return true;
        }
        
        // Apply the item the player is holding in their hand as a hat
        if (action.equalsIgnoreCase("currentitem")) {
            if (!(sender instanceof Player)) {
                sendErrorMsg(sender, "You must be a player!");
                return true;
            }
            
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                sendErrorMsg(sender, "You must be holding an item!");
                return true;
            }
            
            if (UltimaHats.getPlugin().getWearers().isWearing(player)) {
                sendErrorMsg(sender, "Please remove your custom hat before using this command!");
                return true;
            }
            
            ItemUtils.applyItem(player, item);
            player.getInventory().setItemInMainHand(null);
            sendSuccessMsg(sender, "Applied your held item as a hat.");
            return true;
        }
        
        // Reloads the plugin config
        if (action.equalsIgnoreCase("reload")) {
            ConfigUtils.reloadConfigs();
            sendSuccessMsg(sender, "Reloaded all configs!");
            return true;
        }
        
        sendErrorMsg(sender, "Usage: /hats currentitem/apply/remove/reload");
        return true;
    }
    
    // Returns true if sender has permission for this action
    private boolean checkPermission(CommandSender sender, String action) {
        if (action == null) {
            return (sender.hasPermission("ultimahats.hats"));
        }
        return (sender.hasPermission("ultimahats." + action));
    }
    
    /**
     * Send the given user an error message.
     *
     * @param target the user
     * @param msg the error message
     */
    protected void sendErrorMsg(CommandSender target, String msg) {
        target.sendMessage(ChatColor.RED + "Error: " + ChatColor.GRAY + msg);
    }

    /**
     * Send the given user a success message.
     *
     * @param target the user
     * @param msg the error message
     */
    protected void sendSuccessMsg(CommandSender target, String msg) {
        target.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.GRAY + msg);
    }
}
