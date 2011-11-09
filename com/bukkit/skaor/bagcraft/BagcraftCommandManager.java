package com.bukkit.skaor.bagcraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Nicolas Girot
 */

public class BagcraftCommandManager {
    private final Bagcraft plugin;
    
    public BagcraftCommandManager(Bagcraft instance) {
        this.plugin = instance;
    }
    
    public boolean userCommand(Player player, String[] args) {
        if (args[0].compareToIgnoreCase("LimitBag") == 0) {
            player.sendMessage(ChatColor.YELLOW + "Bagcraft : You are limited to " + plugin.playerControl.playerNbrSlot(player.getName()));
            return true;
        } 
        else if(args[0].compareToIgnoreCase("BuyBag") == 0) {
            if (plugin.configuration_g.getBoolean("use-iConomy") == true) {
                plugin.loadSave.buyBag(player);
            } else {
                player.sendMessage(ChatColor.RED + "Bagcraft : Command disabled. Economy function isn't activated." );
            }
            return true;
        } 
        else if (args[0].compareToIgnoreCase("Save") == 0) {
            if (Integer.parseInt(args[1]) > 0) {
                player.sendMessage(plugin.loadSave.saveBag(player,plugin.m_folder,args[1]));
            }
            return true;
        } 
        else if (args[0].compareToIgnoreCase("Load") == 0 ) {
            if (Integer.parseInt(args[1]) > 0) {
                player.sendMessage(plugin.loadSave.loadBag(player,plugin.m_folder,args[1]));
            }
            return true;
        }
        
        /***********OPERATOR COMMAND************/
        else if (args[0].compareToIgnoreCase("Set") == 0) {
            if (player.isOp()) {
                if (args.length == 3) {
                    player.sendMessage(plugin.playerControl.setConfiguration(args[1], args[2]));
                    return true;
                } else {
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.RED + "BagCraft : You are not allowed to use this command.");
                return true;
            }
        }
        else {
            return false;
        }
    }
}
