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


/**
 *
 * @author Maino
 */
public class Services {
    private static final DownloadService downloadService = new DownloadService();
    private static final PlayService playService = new PlayService(downloadService);
    //private static final AirPlayService airPlayService = new AirPlayService();

    public static DownloadService getDownloadService() {
        return downloadService;
    }

    public static PlayService getPlayService() {
        return playService;
    }
    
    public static SearchService getSearchService() {
        return new SearchService();
    }
    
    /*public static AirPlayService getAirPlayService() {
        return airPlayService;
    }*/
}
