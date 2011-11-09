package com.bukkit.skaor.bagcraft;

import com.nijikokun.register.payment.Method;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 *
 * @author Skaor
 */
public class Bagcraft extends JavaPlugin {
    private final String PLAYER_FILE = "bagcraft.xml";
    private final String CONFIG_FILE = "config.yml";
    
    public YamlConfiguration configuration_g;
    private File configuration_g_File;
    
    private File m_folder;

    //public Configuration configBagcraft;
    public Method method;
    
    public BagcraftPlayerControl playerControl = new BagcraftPlayerControl(this);
    public BagCraftLoadSave loadSave = new BagCraftLoadSave(this);

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, new BagcraftServerEconomy(this), Priority.Low, this);
        this.getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, new BagcraftServerEconomy(this), Priority.Low, this);
        
        m_folder = getDataFolder();
        if (!m_folder.exists())
        {
          System.out.print("BagCraft: No folder.");
          m_folder.mkdir();
          System.out.println("BagCraft: folder created.");
        }
        /*Creation du fichier joueurs*/
        File playerbdd = new File(m_folder.getAbsolutePath() + File.separator + PLAYER_FILE);
        /*Creation du fichier de configuration du Plugin*/
        
        if (!playerbdd.exists()) {
            System.out.print("BagCraft : No player file...");
            try {
                
                Element root = new Element("players");
                Document document = new Document(root); 
                
                bagcraftSaveXml(document);
                System.out.println("BagCraft : Player file has been created.");
                
            } catch (Exception e) {
                System.out.println("Error at the creation of player file.");
            }
        }
        
        configuration_g_File = new File(getDataFolder(), CONFIG_FILE);
        configuration_g = new YamlConfiguration();
        
        configFileFirstLoad();
        
        //playerControl.configVerification();
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    
    private void configFileFirstLoad() {
        if(!configuration_g_File.exists()){                        // checks if the yaml does not exists
            configuration_g.set("use-iConomy", false);
            configuration_g.set("bag.first-bag-free", false);
            configuration_g.set("bag.price-multiplicator", 1);
            configuration_g.set("bag.slot-price", 200);
            configuration_g.set("bag.slot-limit", 9);
            configuration_g.set("playername.slot-limit", 9);
            System.out.println("Bagraft : sauvegarde du fichier de configuration.");
            saveYamls();
            //configuration_g_File.getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            //copy(getResource("config.yml"), configuration_g_File); // copies the yaml from your jar to the folder /plugin/<pluginName>
        } else {
            loadYamls();
        }
    }

    public void onDisable() {
        System.out.println( "Bagcraft is disabled!" );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length < 1 )
        {
          return false;
        } else {
            Player player = (Player)sender; //On recupere le joueur qui a lancer la commande.
            if (player.hasPermission("bagcraft.usebagcraft")) {
                if (args.length == 3) {
                    if (args[0].compareToIgnoreCase("Set") == 0) {
                        player.sendMessage(playerControl.setConfiguration(args[1], args[2]));
                    }  
                } else if (args.length == 2) {
                    if (args[0].compareToIgnoreCase("Save") == 0 && Integer.parseInt(args[1]) > 0 ) {
                        player.sendMessage(loadSave.saveBag(player,m_folder,args[1]));
                    } else if (args[0].compareToIgnoreCase("Load") == 0 && Integer.parseInt(args[1]) > 0 ) {
                        player.sendMessage(loadSave.loadBag(player,m_folder,args[1]));
                    } else {
                        return false;
                    }
                } else if (args.length == 1) { 
                    if (args[0].compareToIgnoreCase("LimitBag") == 0) {
                        player.sendMessage(ChatColor.YELLOW + "Bagcraft : You are limited to " + playerControl.playerNbrSlot(player.getName()));
                    } else if(args[0].compareToIgnoreCase("BuyBag") == 0) {
                        if (configuration_g.getBoolean("use-iConomy") == true) {
                            loadSave.buyBag(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "Bagcraft : Command disabled. iConomy isn't activated." );
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.RED + "BagCraft : You are not allowed to use this plugin.");
            }
        }
        return true;
    }
    
    public File getFolder() {
        return m_folder;
    }
    
    public Boolean bagcraftSaveXml (Document document) throws TransformerException {
        try {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(document, new FileOutputStream(new File(m_folder.getAbsolutePath() + File.separator + PLAYER_FILE)));
            return true;
        } catch (IOException e) {
            System.out.println("BagCraft: error at save player file.");
            return false;
        }
        
    }
    
    public Document bagcraftLoadXml() {
        SAXBuilder sxb = new SAXBuilder();
        try {
           Document document = sxb.build(new File(m_folder.getAbsolutePath() + File.separator + PLAYER_FILE));
           return document;
        }
        catch(Exception e){
            return null;
        }
    }
    
    public void loadYamls() {
        try {
            configuration_g.load(configuration_g_File);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveYamls() {
        try {
            configuration_g.save(configuration_g_File);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}