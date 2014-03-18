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

import groovesquid.model.Clients;
import groovesquid.model.Track;
import groovesquid.service.FilenameSchemeParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Maino
 */
public class Config {
    
    public static enum DownloadComplete {
        DO_NOTHING, OPEN_FILE, OPEN_DIRECTORY;
        public static String[] names() {
            DownloadComplete[] states = values();
            String[] names = new String[states.length];

            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name();
            }

            return names;
        }
    }
    
    public static enum FileExists {
        DO_NOTHING, OVERWRITE, RENAME;
        public static String[] names() {
            FileExists[] states = values();
            String[] names = new String[states.length];

            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name();
            }

            return names;
        }
    }
    
    private String version, originalVersion;
    private List<Track> downloads, originalDownloads;
    private String downloadDirectory, originalDownloadDirectory;
    private Clients clients, originalClients;
    private int maxParallelDownloads, originalMaxParallelDownloads;
    private String fileNameScheme, originalFileNameScheme;
    private boolean autocompleteEnabled, originalAutocompleteEnabled;
    private int downloadComplete, originalDownloadComplete;
    private int fileExists, originalFileExists;
    private String locale, originalLocale;
    private String guiClass, originalGuiClass;
    private String proxyHost, originalProxyHost;
    private Integer proxyPort, originalProxyPort;

    public Config() {
        originalVersion = Main.getVersion();
        originalClients = Main.getClients();
        originalDownloads = new ArrayList<Track>();
        originalDownloadDirectory = System.getProperty("user.home");
        originalMaxParallelDownloads = 10;
        originalFileNameScheme = FilenameSchemeParser.DEFAULT_FILENAME_SCHEME;
        originalAutocompleteEnabled = false;
        originalDownloadComplete = DownloadComplete.DO_NOTHING.ordinal();
        originalFileExists = FileExists.RENAME.ordinal();
        originalLocale = Locale.getDefault().toString();
        originalGuiClass = "groovesquid.gui.style.Flat";
        originalProxyHost = null;
        originalProxyPort = null;
        resetSettings();
    }
    
    public final void resetSettings() {
        version = originalVersion;
        clients = originalClients;
        downloads = originalDownloads;
        downloadDirectory = originalDownloadDirectory;
        maxParallelDownloads = originalMaxParallelDownloads;
        fileNameScheme = originalFileNameScheme;
        autocompleteEnabled = originalAutocompleteEnabled;
        downloadComplete = originalDownloadComplete;
        locale = originalLocale;
        guiClass = originalGuiClass;
        proxyHost = originalProxyHost;
        proxyPort = originalProxyPort;
    }
    
    public synchronized List<Track> getDownloads() {
        return downloads;
    }

    public synchronized void setDownloads(List<Track> downloads) {
        this.downloads = downloads;
        Main.saveConfig();
    }
    
    public synchronized String getDownloadDirectory() {
        return downloadDirectory;
    }
    
    public synchronized void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        Main.saveConfig();
    }

    public synchronized Clients getClients() {
        return clients;
    }
    
    public synchronized void setClients(Clients clients) {
        this.clients = clients;
        Main.saveConfig();
    }
    
    public synchronized int getMaxParallelDownloads() {
        return maxParallelDownloads;
    }
    
    public synchronized void setMaxParallelDownloads(int maxParallelDownloads) {
        this.maxParallelDownloads = maxParallelDownloads;
        Main.saveConfig();
    }
    
    public synchronized void setFileNameScheme(String fileNameScheme) {
        this.fileNameScheme = fileNameScheme;
        Main.saveConfig();
    }
    
    public synchronized String getFileNameScheme() {
        return fileNameScheme;
    }

    public boolean getAutocompleteEnabled() {
        return autocompleteEnabled;
    }

    public void setAutocompleteEnabled(boolean autocompleteEnabled) {
        this.autocompleteEnabled = autocompleteEnabled;
    }
    
    public int getDownloadComplete() {
        return downloadComplete;
    }
    
    public int getFileExists() {
        return fileExists;
    }
    
    public void setDownloadComplete(int downloadComplete) {
        this.downloadComplete = downloadComplete;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
        Main.saveConfig();
    }
    
    public Class getGuiClass() {
        try {
            return Class.forName(guiClass);
        } catch (ClassNotFoundException ex) {
            try {
                return Class.forName("groovesquid.gui.style.Flat");
            } catch (ClassNotFoundException ex1) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String getProxyHost() {
        return proxyHost;
    }
    
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    
    public Integer getProxyPort() {
        return proxyPort;
    }
    
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }
}
