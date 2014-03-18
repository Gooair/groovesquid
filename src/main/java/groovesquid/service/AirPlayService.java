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

package groovesquid.service;

/*import com.jameslow.AirPlay;
import com.jameslow.AirPlay.Service;*/
import groovesquid.Main;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.apache.commons.collections.iterators.ArrayIterator;

/**
 *
 * @author Maino
 */
public class AirPlayService {
    /*
    private Service[] services;
    private AirPlay airPlay;
    
    public AirPlayService() {
        searchServices();
        
    }

    private void searchServices() {
        new Thread(new SearchServicesTask()).start();
    }

    private class SearchServicesTask implements Runnable {
        public void run() {
            try {
                services = AirPlay.search();
            } catch (IOException ex) {
                Logger.getLogger(AirPlayService.class.getName()).log(Level.SEVERE, null, ex);
            }
            SwingUtilities.invokeLater(new Runnable(){public void run(){
                for (int i = 0; i < services.length; i++) {
                    final int x = i;
                    final Service service = services[i];
                    Main.getGui().getAirPlayPopupMenu().add(new JCheckBoxMenuItem(new AbstractAction(service.name) {
                        public void actionPerformed(ActionEvent evt) {
                            //Main.getGui().getAirPlayPopupMenu().getSelectionModel().setSelectedIndex(x);
                            if(((JCheckBoxMenuItem)evt.getSource()).isSelected()) {
                                connect(service);
                                play();
                            } else {
                                disconnect();
                            }
                        }
                    }));
                    System.out.println(service.hostname + ":" + service.port);
                }
            }});
        }
    }
    
    public Service[] getServices() {
        return services;
    }
    
    public void connect(int index) {
        airPlay = new AirPlay(services[index]);
    }
    
    public void connect(Service service) {
        airPlay = new AirPlay(service);
    }
    
    public void disconnect() {
        if(airPlay != null) {
            airPlay.stop();
            airPlay = null;
        }
    }
    
    public void play() {
        try {
            airPlay.desktop();
        } catch (AWTException ex) {
            Logger.getLogger(AirPlayService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AirPlayService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
}
