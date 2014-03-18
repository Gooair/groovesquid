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

import java.util.List;

/**
 *
 * @author Maino
 */
public class Playlist {
    private String id;
    private String name;
    private String author;
    private String numSongs;
    private List<Song> songs;
    
    public Playlist(Object id, Object name, Object author, Object numSongs) {
        this.id = id.toString();
        this.name = name.toString();
        this.author = author.toString();
        this.numSongs = numSongs.toString();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getNumSongs() {
        int numSongsIndexOf = numSongs.indexOf(".");
        if(numSongsIndexOf > 0) {
            return numSongs.substring(0, numSongsIndexOf);
        }
        return numSongs;
    }
}
