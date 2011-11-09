/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bukkit.skaor.bagcraft;

import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/**
 *
 * @author Skaor
 */
public class BagcraftServerEconomy extends ServerListener {
    private Bagcraft plugin;

    public BagcraftServerEconomy(Bagcraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (Methods.hasMethod()) {
            if(Methods.checkDisabled(event.getPlugin())) {
                Methods.reset();
                plugin.method = null;
                System.out.println("[" + plugin.getDescription().getName() + "] Payment method was disabled. No longer accepting payments.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!Methods.hasMethod() && Methods.setMethod(plugin.getServer().getPluginManager())) {
            plugin.method = Methods.getMethod();
            System.out.println("[" + plugin.getDescription().getName() + "] Payment method found (" + Methods.getMethod().getName() + " version: " + Methods.getMethod().getVersion() + ")");
        }
    }
}