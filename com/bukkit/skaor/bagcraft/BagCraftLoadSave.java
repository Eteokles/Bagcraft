package com.bukkit.skaor.bagcraft;

import com.nijikokun.register.payment.Method.MethodAccount;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Skaor
 */
public class BagCraftLoadSave {

    private final Bagcraft plugin;
    private final String PLAYER_FILE = "bagcraft.xml";

    public BagCraftLoadSave(Bagcraft instance) {
        this.plugin = instance;
    }

    public String saveBag(Player player, File m_folder, String numero) {
        if (limitVerification(Integer.parseInt(numero), player)) {
            Document bagcraftplayer = plugin.bagcraftLoadXml();
            Element racine = bagcraftplayer.getRootElement();
            if (verificationFile(player, bagcraftplayer)) {
                Element playerElement = racine.getChild(player.getName());
                Element bagElement = playerElement.getChild("bag-"+numero);
                if (player.getTargetBlock(null, 0).getType() == Material.CHEST) {
                    if (plugin.configuration_g.getBoolean("use-iConomy") == true) {
                        if (bagElement.getAttributeValue("status").equals("[CLOSE]")) {
                            return ChatColor.YELLOW + "BagCraft : You need to buy this bag before use it.";
                        } else {
                            return executeSaveBag(player,bagElement,bagcraftplayer);
                        }
                    } else {
                        return executeSaveBag(player,bagElement,bagcraftplayer);
                    }
                } else {
                    return ChatColor.YELLOW + "BagCraft : Please target a simple or double chest.";
                }
            } else {
                return ChatColor.RED + "BagCraft : User can't be verified.";
            }
        } else {
            return ChatColor.RED + "Bagcraft : You are limited to " + plugin.playerControl.playerNbrSlot(player.getName()) + " bag(s)";
        }

    }
    
    private String executeSaveBag(Player player, Element bagElement, Document bagcraftplayer) {
        if (bagElement.getAttributeValue("value").equals("[EMPTY]")) {
            Chest coffre1 = (Chest)player.getTargetBlock(null, 0).getState();
            Chest coffre2 = coffreExist(coffre1);
            String contenus = "";
            for (int i=0; i < 27; i++) {
                contenus += Integer.toString(coffre1.getInventory().getItem(i).getTypeId());
                contenus += ",";
                contenus += Integer.toString(coffre1.getInventory().getItem(i).getAmount());
                contenus += ",";
                contenus += Short.toString(coffre1.getInventory().getItem(i).getDurability());
                coffre1.getInventory().clear(i);
                if (i < 26) {
                    contenus += "|";
                }
            }
            if (coffre2 != null) {
                for (int i=0; i < 27; i++) {
                    if (i == 0) {
                        contenus += "|";
                    }
                    contenus += Integer.toString(coffre2.getInventory().getItem(i).getTypeId());
                    contenus += ",";
                    contenus += Integer.toString(coffre2.getInventory().getItem(i).getAmount());
                    contenus += ",";
                    contenus += Short.toString(coffre2.getInventory().getItem(i).getDurability());
                    coffre2.getInventory().clear(i);
                    if (i < 26) {
                        contenus += "|";
                    }
                }
            }
            bagElement.setAttribute("value", contenus);
            try {
                plugin.bagcraftSaveXml(bagcraftplayer);
                return ChatColor.GREEN + "BagCraft : The bag has been saved";
            } catch (Exception ex) {
                return ChatColor.RED + "BagCraft : Error saving file";
            }
        } else {
            return ChatColor.YELLOW + "BagCraft : This bag is not empty.";
        }
    }

    public String loadBag(Player player, File m_folder, String numero) {
        if (limitVerification(Integer.parseInt(numero), player)) {
            try {
                Document bagcraftplayer = plugin.bagcraftLoadXml();
                Element racine = bagcraftplayer.getRootElement();
                if (verificationFile(player, bagcraftplayer)) {
                    Element playerElement = racine.getChild(player.getName());
                    Element bagElement = playerElement.getChild("bag-"+numero);
                    if (player.getTargetBlock(null, 0).getType() == Material.CHEST) {
                        if (plugin.configuration_g.getBoolean("use-iConomy") == true) {
                            if (bagElement.getAttributeValue("status").equals("[CLOSE]")) {
                                return ChatColor.YELLOW + "BagCraft : You need to buy this bag before use it.";
                            } else {
                                return executeLoadBag(player,bagElement,bagcraftplayer);
                            }
                        } else {
                            return executeLoadBag(player,bagElement,bagcraftplayer);
                        }
                    } else {
                        return ChatColor.YELLOW + "BagCraft : Please target a simple or double chest.";
                    }
                } else {
                    return ChatColor.RED + "BagCraft : User can't be verified.";
                }
            } catch (Exception ex) {
                return ChatColor.RED + "BagCraft : Error at reading saving file.";
            }
        } else {
            return ChatColor.RED + "Bagcraft : You are limited to " + plugin.playerControl.playerNbrSlot(player.getName()) + " bag(s)";
        }
    }
    
    private String executeLoadBag(Player player, Element bagElement, Document bagcraftplayer){
        Chest coffre1 = (Chest)player.getTargetBlock(null, 0).getState();
        Chest coffre2 = coffreExist(coffre1);
        if (verifContent(coffre1) && verifContent(coffre2)) {
            String contenu = bagElement.getAttributeValue("value");
            if (contenu.equals("[EMPTY]")) {
                return ChatColor.YELLOW + "BagCraft : The bag is empty.";
            } else {
                String delimiter = "\\|";
                String[] objets = contenu.split(delimiter);
                if (objets.length == 54 && coffre2 == null) {
                    return ChatColor.YELLOW + "BagCraft : This bag contains a double chest, You can't put in a simple chest.";
                } else {
                    for (int i=0; i < 27; i++) {
                        String objettype = objets[i].split(",")[0];
                        String nombre = objets[i].split(",")[1];
                        String durabilite = objets[i].split(",")[2];
                        if (!objettype.equals("0")) {
                            int objetint = Integer.parseInt(objettype);
                            int nombreint = Integer.parseInt(nombre);
                            short durability = Short.parseShort(durabilite);
                            ItemStack objet = new ItemStack(objetint,nombreint,durability);
                            coffre1.getInventory().setItem(i, objet);
                        }
                    }
                    if (objets.length == 54 && coffre2 != null) {
                        for (int i=27; i < 54; i++) {
                            String objettype = objets[i].split(",")[0];
                            String nombre = objets[i].split(",")[1];
                            String durabilite = objets[i].split(",")[2];
                            if (!objettype.equals("0")) {
                                int objetint = Integer.parseInt(objettype);
                                int nombreint = Integer.parseInt(nombre);
                                short durability = Short.parseShort(durabilite);
                                ItemStack objet = new ItemStack(objetint,nombreint,durability);
                                coffre2.getInventory().setItem((i-27), objet);
                            }
                        }
                    }
                }
                bagElement.setAttribute("value", "[EMPTY]");
                try {
                    plugin.bagcraftSaveXml(bagcraftplayer);
                    return ChatColor.GREEN + "BagCraft : The bag has been loaded.";
                } catch (Exception ex) {
                    return ChatColor.RED + "BagCraft : Error unspecified.";
                }
            }
        } else {
            return ChatColor.YELLOW + "BagCraft : The chest need to be empty to load a bag.";
        }
    }

    private boolean verificationFile(Player player, Document docBagcraft) {
        Document bagcraftplayer = docBagcraft;
        Element racine = bagcraftplayer.getRootElement();
        Element playerElement = racine.getChild(player.getName());
        int limit = plugin.playerControl.playerNbrSlot(player.getName());
        
        if (playerElement == null) {
            playerElement = new Element(player.getName());
            Element bagElement = new Element("bag-1");
            if (plugin.configuration_g.getBoolean("bag.first-bag-free") == true) {
                bagElement.setAttribute("buyed", "[FALSE]");
                bagElement.setAttribute("status", "[OPEN]");//Ajout de l'attribut empty au sac
            } else {
                bagElement.setAttribute("buyed", "[FALSE]");
                bagElement.setAttribute("status", "[CLOSE]");
            }
            bagElement.setAttribute("value", "[EMPTY]");
            playerElement.addContent(bagElement);
            for (int i = 2; i <= limit; i++) {
                Element bag = new Element("bag-"+Integer.toString(i));//Creation de l'element sac 1
                bag.setAttribute("status", "[CLOSE]");
                bag.setAttribute("buyed", "[FALSE]");
                bag.setAttribute("value", "[EMPTY]");//Ajout de l'attribut empty au sac
                playerElement.addContent(bag);
            }
            racine.addContent(playerElement);
            try {
                return plugin.bagcraftSaveXml(bagcraftplayer);
            } catch (TransformerException ex) {
                Logger.getLogger(BagCraftLoadSave.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            return true;
        }
    }

    private Chest coffreExist(Chest coffre) {
        if (coffre.getBlock().getRelative(BlockFace.NORTH, 1).getType() == Material.CHEST) {
            return (Chest)coffre.getBlock().getRelative(BlockFace.NORTH, 1).getState();
        } else if (coffre.getBlock().getRelative(BlockFace.SOUTH, 1).getType() == Material.CHEST) {
            return (Chest)coffre.getBlock().getRelative(BlockFace.SOUTH, 1).getState();
        } else if (coffre.getBlock().getRelative(BlockFace.EAST, 1).getType() == Material.CHEST) {
            return (Chest)coffre.getBlock().getRelative(BlockFace.EAST, 1).getState();
        } else if (coffre.getBlock().getRelative(BlockFace.WEST, 1).getType() == Material.CHEST) {
            return (Chest)coffre.getBlock().getRelative(BlockFace.WEST, 1).getState();
        } else {
            return null;
        }
    }

    private Boolean verifContent(Chest coffre) {
        if (coffre != null) {
            for (int i=0; i < 27; i++) {
                if (coffre.getInventory().getItem(i).getTypeId() != 0) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
    
    private Boolean limitVerification(int bagPosition, Player player) {
        int limit = plugin.playerControl.playerNbrSlot(player.getName());
        if(limit >= bagPosition) {
            return true;
        } else {
            return false;
        }
    }
    
    /*Fonction d'achat de sac lie a iConomy*/
    public void buyBag(Player player) {
        Document bagcraftplayer = plugin.bagcraftLoadXml();
        Element racine = bagcraftplayer.getRootElement();
        
        Element playerElement = racine.getChild(player.getName());
        int limitbag = plugin.playerControl.playerNbrSlot(player.getName());
        Boolean bagfind = false;
        int i = 0;
        MethodAccount playerAccount = plugin.method.getAccount(player.getName());
        while (bagfind == false) {
            i++;
            Element bagElement = playerElement.getChild("bag-"+i);
            if (bagElement.getAttributeValue("buyed").equals("[FALSE]")){
                bagfind = true;
                Double bagPrice = plugin.playerControl.bagPrice(i);
                if (playerAccount.hasEnough(bagPrice)) {
                    playerAccount.subtract(bagPrice);
                    bagElement.setAttribute("buyed", "[TRUE]");
                    bagElement.setAttribute("status", "[EMPTY]");
                    try {
                        plugin.bagcraftSaveXml(bagcraftplayer);
                        player.sendMessage(ChatColor.GREEN + "BagCraft : New bag bought at slot "+Integer.toString(i)+".");
                    } catch (TransformerException ex) {
                        player.sendMessage(ChatColor.RED + "BagCraft : Error when saving new bag .");
                    }               
                } else {
                    player.sendMessage(ChatColor.YELLOW + "BagCraft : You need more money to buy a bag. Currently : "+ Double.toString(playerAccount.balance()) +", Needed :"+Double.toString(bagPrice)+".");
                }
            } else {
                if (i == limitbag) {
                   bagfind = true;
                   player.sendMessage(ChatColor.GREEN + "BagCraft : No new slot available. You've reached your limit of " + limitbag +" bags.");
                }
            }
        }
    }
    
}
