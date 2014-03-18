/*
 * Copyright (C) 2013 Maino
 * 
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
 * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
 * California, 94105, USA.
 * 
 */

package groovesquid;

import com.google.gson.Gson;
import groovesquid.gui.About;
import groovesquid.gui.GUI;
import groovesquid.gui.Settings;
import groovesquid.model.Clients;
import groovesquid.model.Language;
import groovesquid.util.Utils;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Maino
 */
public class Main {
    
    private final static Logger log = Logger.getLogger(Main.class.getName());
    
    private static GUI gui;
    private static Settings settings;
    private static About about;
      
    private static String version = "0.6.0";
    private static Clients clients = new Clients(new Clients.Client("htmlshark", "20130520", "nuggetsOfBaller"), new Clients.Client("jsqueue", "20130520", "chickenFingers"));
    private static Gson gson = new Gson();
    private static File configDir;
    private static Config config;
    private static Map<String, Language> languages;

    public static void main(String[] args) {
        
        System.setSecurityManager(null);
        
        log.info("Groovesquid v" + version + " running on " + System.getProperty("java.vm.name") + " " + System.getProperty("java.runtime.version") + " (" + System.getProperty("java.vm.vendor") + ") in " + System.getProperty("java.home"));
        
        // show gui
        
        // apple os x
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Groovesquid");
        // antialising
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        // flackering bg fix
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.noddraw", "true");
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
 
        // load languages
        languages = loadLanguages();

        // Load config
        config = loadConfig();
        
        // GUI
        try {
            gui = (GUI) config.getGuiClass().newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        settings = new Settings();
        about = new About();

        // Update Checker
        new UpdateCheckThread().start();

        // Services
        
        
        // init grooveshark (every 25min)
        new InitThread().start();

    }
    
    public static GUI getGui() {
        return Main.gui;
    }
    
    public static void resetGui() {
        gui.dispose();
        try {
            gui = (GUI) config.getGuiClass().newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        gui.initDone();
        about.dispose();
        about = new About();
        settings.dispose();
        settings = new Settings();
    }

    public static Config loadConfig() {
        configDir = new File(Utils.dataDirectory() + File.separator + ".groovesquid");
        if(!configDir.exists()) {
            configDir.mkdir();
        }
        
        File oldConfigFile = new File("config.json");
        File configFile = new File(configDir + File.separator + "config.json");
        
        if(oldConfigFile.exists() && !configFile.exists()) {
            try {
                FileUtils.copyFile(oldConfigFile, configFile);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            oldConfigFile.delete();
        }

        if(configFile.exists()) {
            try {
                Config tempConfig = gson.fromJson(FileUtils.readFileToString(configFile), Config.class);
                if(tempConfig != null) {
                    return tempConfig;
                }
            } catch (Exception ex) {
                configFile.delete();
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Config();
    }
    
    public static void saveConfig() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                File configFile = new File(configDir + File.separator + "config.json");
                try {
                    FileUtils.writeStringToFile(configFile, gson.toJson(config));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };  
        worker.execute();
    }
        
    public static String getLocaleString(String localeName) {
        Language currentLanguage;
        if(languages.containsKey(config.getLocale())) {
            currentLanguage = languages.get(config.getLocale());
        } else {
            // default language
            currentLanguage = languages.get("en_US");
        }
        if(currentLanguage.getResourceBundle().containsKey(localeName)) {
            return currentLanguage.getResourceBundle().getString(localeName);
        } else if(languages.get("en_US").getResourceBundle().containsKey(localeName)) {
            return languages.get("en_US").getResourceBundle().getString(localeName);
        } else {
            return localeName;
        }
    }

    private static Map<String, Language> loadLanguages() {
        Map<String, Language> hm = new LinkedHashMap<String, Language>();
        
        try {
            hm.put("en_US", new Language("en_US"));
            hm.put("de_DE", new Language("de_DE"));
            hm.put("fr_FR", new Language("fr_FR"));
            hm.put("es_ES", new Language("es_ES"));
            hm.put("it_IT", new Language("it_IT"));
            hm.put("tr_TR", new Language("tr_TR"));
            hm.put("se_SE", new Language("se_SE"));
            hm.put("ru_RU", new Language("ru_RU"));
            hm.put("pl_PL", new Language("pl_PL"));
            hm.put("nl_BE", new Language("nl_BE"));
            hm.put("sr_Latn_RS", new Language("sr_Latn_RS"));
            hm.put("sr_RS", new Language("sr_RS"));
            hm.put("pt_PT", new Language("pt_PT"));
            hm.put("pt_BR", new Language("pt_BR"));
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        return hm;
    }
    
    public static synchronized Map<String, Language> getLanguages() {
        return languages;
    }
    
    public static int getLanguageIndex() {
        int i = 0;
        for(String localeString : languages.keySet()) {
            if(Main.getConfig().getLocale().equals(localeString)) {
                return i;
            }
            i++;
        }
        return 0;
    }
    
    public static synchronized Config getConfig() {
        return config;
    }

    public static String getVersion() {
        return version;
    }
    
    public static Clients getClients() {
        return clients;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static About getAbout() {
        return about;
    }

}
