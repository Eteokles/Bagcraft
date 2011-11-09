/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bukkit.skaor.bagcraft;

import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.jdom.Document;
import org.jdom.Element;


/**
 *
 * @author famille
 */
public class BagcraftPlayerControl {

    private final Bagcraft plugin;
    private final String PLAYER_FILE = "bagcraft.xml";
    private final String CONFIG_FILE = "config.yml";

    public BagcraftPlayerControl(Bagcraft instance) {
        this.plugin = instance;
    }
    
    public int playerNbrSlot (String playerName) {
        int slotlimit;
        slotlimit = plugin.configuration_g.getInt(playerName+".slot-limit");
        if (slotlimit == 0) {
            slotlimit = plugin.configuration_g.getInt("bag.slot-limit");
        }
        return slotlimit;
    }
    
    public Double bagPrice (Integer bagPosition) {
        Integer bagPrice = plugin.configuration_g.getInt("bag.slot-price");
        Integer multiple = plugin.configuration_g.getInt("bag.price-multiplicator");
        Integer result = bagPrice;
        for (int i= 2; i <= bagPosition; i++) {
            result *= multiple ;
        }
        return result.doubleValue();
    }
    
    /*public void configVerification () {
        Document bagcraftplayer = plugin.bagcraftLoadXml();
        Element racine = bagcraftplayer.getRootElement();
        if (racine.getChildren().isEmpty() == false) {
            if (firstBagFreeVerif(bagcraftplayer)) {
                if (verifSlotBag(bagcraftplayer)) {
                    bagcraftplayer.normalizeDocument();
                    try {
                        plugin.bagcraftSaveXml(bagcraftplayer);
                    } catch (TransformerException ex) {
                        Logger.getLogger(BagcraftPlayerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }*/
    
    private Boolean firstBagFreeVerif (Document bagcraftplayer) {
        Element racine = bagcraftplayer.getRootElement();   //Recuperation du Node racine
        
        List tempNodeListPlayer = racine.getChildren();
        Iterator i = tempNodeListPlayer.iterator();
        
        while(i.hasNext()) {
            Element selectedPlayer = (Element)i.next();
            Element bag1 = selectedPlayer.getChild("bag-1");
            Element bag2 = selectedPlayer.getChild("bag-2");
            if (plugin.configuration_g.getBoolean("bag.first-bag-free") == false) {
                if (bag1.getAttributeValue("buyed").equals("[FALSE]") && bag2.getAttributeValue("buyed").equals("[FALSE]") && bag1.getAttributeValue("value").equals("[EMPTY]")) {
                    bag1.setAttribute("status", "[CLOSE]");
                } else {
                    bag1.setAttribute("status", "[OPEN]");
                    bag1.setAttribute("buyed", "[TRUE]");
                }
            } else {
                bag1.setAttribute("status", "[OPEN]");
            }
        }
        return true;
    }
    
    private Boolean verifSlotBag (Document bagcraftplayer) {
        Element racine = bagcraftplayer.getRootElement();
        
        List tempNodeListPlayer = racine.getChildren();
        Iterator i = tempNodeListPlayer.iterator();
        
        while(i.hasNext()) {
            int slotlimit;
        
            Element selectedPlayer = (Element)i.next();
            List bagList = selectedPlayer.getChildren();
            
            slotlimit = plugin.configuration_g.getInt(selectedPlayer.getName()+".slot-limit");
            if (slotlimit == -1) {
                slotlimit = plugin.configuration_g.getInt("bag.slot-limit");
            }
            
            if (slotlimit < bagList.size()) {
                int startBag = bagList.size() - 1;
                int rmvBag = bagList.size() - slotlimit;
                for (int nbr = rmvBag; nbr > 0; nbr--) {
                    Element bag = (Element) bagList.get(startBag);
                    selectedPlayer.removeChild(bag.getName());
                    startBag --;
                }
            } else if (slotlimit > bagList.size()) {
                for (int nbr = bagList.size() + 1; nbr <= slotlimit; nbr++) {
                    Element bag = new Element("bag-"+Integer.toString(nbr));
                    bag.setAttribute("status", "[CLOSE]");
                    bag.setAttribute("buyed", "[FALSE]");
                    bag.setAttribute("value", "[EMPTY]");//Ajout de l'attribut empty au sac
                    selectedPlayer.addContent(bag);
                }
            }
            
        }
        
        return true;
    }
    
    public String setConfiguration (String command, String value) {
        if (command.equals("first-bag-free")) {
            if (value.equals("true") || value.equals("false")) {
                plugin.configuration_g.set("bag."+command, Boolean.parseBoolean(value));
                plugin.saveYamls();
                //configVerification();
                return ChatColor.BLUE + "BagCraft : first-bag-free set to "+value+".";
            } else {
                return ChatColor.RED + "BagCraft : first-bag-free need true or false argument.";
            }
        } else if (command.equals("slot-limit")) {
            if (stringIsInt(value)) {
                plugin.configuration_g.set("bag."+command, Integer.parseInt(value));
                plugin.saveYamls();
                //configVerification();
                return ChatColor.BLUE + "BagCraft : slot-limit set to "+value+".";
            } else {
                return ChatColor.BLUE + "BagCraft : slot-limit need an integer argument";
            }
        } else if (command.equals("use-iConomy")) {
            if (value.equals("true") || value.equals("false")) {
                plugin.configuration_g.set(command, Boolean.parseBoolean(value));
                plugin.saveYamls();
                return ChatColor.BLUE + "BagCraft : use-iConomy set to "+value+".";
            } else {
                return ChatColor.RED + "BagCraft : use-iConomy need true or false argument.";
            }
        } else {
            return ChatColor.RED + "Bagcraft : command unknow.";
        }
    }
    
    private Boolean stringIsInt (String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException err) {
            return false;
        }
    }
}