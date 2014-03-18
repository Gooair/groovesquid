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
import com.google.gson.Gson;
import groovesquid.util.Utils;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * @author Maino
 */
public class UpdateCheckThread extends Thread {
    private final static Logger log = Logger.getLogger(Main.class.getName());
    private static Gson gson = new Gson();
    private static String updateFile = "http://groovesquid.com/updatecheck.php";
    
    public UpdateCheckThread() {
        
    }
    
    @Override
    public void run() {
        UpdateCheck updateCheck = gson.fromJson(getFile(updateFile), UpdateCheck.class);
        if(updateCheck.getClients() != null)
            Grooveshark.setClients(updateCheck.getClients());
        
        openAdPopup(updateCheck.getAds());
        
        if(Utils.compareVersions(updateCheck.getVersion(), Main.getVersion()) > 0) {
            if(JOptionPane.showConfirmDialog(null, "New version (v" + updateCheck.getVersion() + ") is available! Do you want to download the new version (recommended)?", "New version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                try {
                    Desktop.getDesktop().browse(java.net.URI.create("http://groovesquid.com/#download"));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static String getFile(String url) {
        String responseContent = null;
        HttpEntity httpEntity = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            httpGet.setHeader(HTTP.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
            
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpEntity = httpResponse.getEntity();
            
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                httpEntity.writeTo(baos);
            } else {
                throw new RuntimeException(url);
            }

            responseContent = baos.toString("UTF-8");
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        return responseContent;
    }
    
    private void openAdPopup(String[] ads) {
        if(ads.length > 0 && new UrlValidator(new String[] {"http", "https"}).isValid(ads[0])) {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(ads[0]));
            } catch (IOException ex) {
                Logger.getLogger(UpdateCheckThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private void fetchAdBanner(String[] ads) {
        List<String> matches = new ArrayList<String>();
        HttpClient httpClient = HttpClientBuilder.create().build();

        for(int i = 0; i < 10; i++) {
            HttpGet httpGet = null;
            HttpEntity httpEntity = null;
            try {
                String url = ads[0] + "&try=" + i;
                System.out.println(url);
                httpGet = new HttpGet(url);
                httpGet.setHeader(HTTP.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
                httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
               
                HttpResponse httpResponse = httpClient.execute(httpGet);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                httpEntity = httpResponse.getEntity();

                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    httpEntity.writeTo(baos);
                } else {
                    throw new RuntimeException("Failed getting ads");
                }
                String responseContent = baos.toString("UTF-8");

                Pattern pattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
                Matcher matcher = pattern.matcher(responseContent);
                while (matcher.find()) {
                    matches.add(responseContent.substring(matcher.start(0), matcher.end(0)));
                }
                
            } catch (Exception ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    EntityUtils.consume(httpEntity);
                    httpGet.releaseConnection();
                } catch (IOException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println(matches.get(1));
            // no flash ads
            if(!matches.get(1).contains(".swf")) {
                break;
            } else {
                try {
                    httpGet = new HttpGet(matches.get(1));
                    httpGet.setHeader(HTTP.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    httpEntity = httpResponse.getEntity();

                    StatusLine statusLine = httpResponse.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        continue;
                    } else {
                        throw new RuntimeException("Failed getting ads");
                    }
                    
                } catch (Exception ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
        
        final String result;
        if(!matches.get(1).contains(".swf")) {
            result = "<html><body style=\"margin:0;padding:0;text-align:center;\"><a href=\"" + matches.get(0) + "\"><img border=\"0\" src=\"" + matches.get(1)  + "\"></a></body></html>";
        } else {
            result = "<html><body style=\"margin:0;padding:0;text-align:center;\">" + ads[1] + "</body></html>";
        }
        
        final JEditorPane adPane = new JEditorPane();
        adPane.setText(result);
        adPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                        // refresh
                        adPane.setText(result);
                    } catch (Exception ex) {
                        Logger.getLogger(UpdateCheckThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }
    
    public class UpdateCheck {
        private String version;
        private Clients clients;
        private String[] ads;
        
        public String getVersion() {
            return version;
        }
        
        public Clients getClients() {
            return clients;
        }

        private String[] getAds() {
            return ads;
        }
    }
}
