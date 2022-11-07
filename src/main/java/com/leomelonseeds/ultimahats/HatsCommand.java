package com.leomelonseeds.ultimahats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            // Open the GUI
        }
        
        String action = args[0];
        
        if (action.equalsIgnoreCase("apply")) {
            
        }
        
        sendErrorMsg(sender, "Usage: /hats currentitem/apply/remove/reload");
        return true;
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
