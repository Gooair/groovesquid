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

import groovesquid.model.Country;
import com.google.gson.Gson;
import groovesquid.util.Utils;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author Maino
 */
public class InitThread extends Thread {
    
    private final static Logger log = Logger.getLogger(Main.class.getName());
    private CountDownLatch latch = new CountDownLatch(1);
    
    public CountDownLatch getLatch() {
        return latch;
    }

    public InitThread() {
        log.info("Initializing...");
    }
    
    @Override
    public void run() {
        Gson gson = new Gson();
        
        String session = null;
        try {
            session = gson.fromJson(Grooveshark.sendRequest("initiateSession", null), Response.class).getResult();
        } catch (Exception ex) {
            Main.getGui().showError(Main.getLocaleString("ERROR_INITIATE_SESSION"));
            return;
        }
        Grooveshark.setSession(session);
        
        String commtoken = gson.fromJson(Grooveshark.sendRequest("getCommunicationToken", new HashMap(){{
            put("secretKey", Utils.md5(Grooveshark.getSession()));
        }}), Response.class).getResult();
        Grooveshark.setCommtoken(commtoken);
        // commtoken expires after 25 minutes
        Grooveshark.setTokenExpires(new Date().getTime() + ((1000 * 60) * 25));
        
        Country country = gson.fromJson(Grooveshark.sendRequest("getCountry", null), CountryResponse.class).getResult();
        if(country != null)
            Grooveshark.setCountry(country);
        
        SwingUtilities.invokeLater(new Runnable(){public void run(){
            Main.getGui().initDone();
        }});
        
        latch.countDown();
    }

    class Response {
        private HashMap<String, Object> header;
        private String result;
        private HashMap<String, Object> fault;

        public Response(HashMap<String, Object> header, String result) {
            this.header = header;
            this.result = result;
        }

        public HashMap getHeader() {
            return this.header;
        }

        public String getResult() {
            return this.result;
        }

        public HashMap<String, Object> getFault() {
            return this.fault;
        }
    }
    
    class CountryResponse {
        private HashMap<String, Object> header;
        private Country result;
        private HashMap<String, Object> fault;

        public CountryResponse(HashMap<String, Object> header, Country result) {
            this.header = header;
            this.result = result;
        }

        public HashMap getHeader() {
            return this.header;
        }

        public Country getResult() {
            return this.result;
        }

        public HashMap<String, Object> getFault() {
            return this.fault;
        }
    }
}

