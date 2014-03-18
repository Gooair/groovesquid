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

package groovesquid.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 *
 * @author Maino
 */
public class Language {
    
    private Locale locale;
    private ResourceBundle resourceBundle;
    private String languageName, localeString;
    
    public Language(String localeString) {
        String parts[] = localeString.split("_", -1);
        
        if (parts.length == 1) locale = new Locale(parts[0]);
        else if (parts.length == 2) locale = new Locale(parts[0], parts[1]);
        else locale = new Locale(parts[0], parts[1], parts[2]);
        
        resourceBundle = ResourceBundle.getBundle("groovesquid.properties.locale", locale, new UTF8Control());
        languageName = resourceBundle.getString("LANGUAGE_NAME");
        this.localeString = localeString;
    }
    
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    public String getLanguageName() {
        return languageName;
    }
    
    public String getLocaleString() {
        return localeString;
    }
}

class UTF8Control extends Control {
    @Override
    public ResourceBundle newBundle
        (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException
    {
        // The below is a copy of the default implementation.
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try {
                // Only this line is changed to make it to read properties files as UTF-8.
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }
}
