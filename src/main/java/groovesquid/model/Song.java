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

import groovesquid.Main;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author Maino
 */
public class Song {
    private String id;
    private String name;
    private Artist artist;
    private Album album;
    private Double duration;
    private int bitrate;
    private String year;
    private Long trackNum;
    
    public Song(Object id, Object name, Object artistId, Object artistName, Object albumId, Object albumName, Object duration, Object year, Object trackNum) {
        this.id = id.toString();
        this.name = name.toString().trim();
        this.artist = new Artist(artistId, artistName);
        this.album = new Album(albumId.toString(), albumName.toString(), artist);
        if(duration != null) {
            this.duration = Double.valueOf(duration.toString());
        } else {
            this.duration = 0.0D;
        }
        this.bitrate = 0;
        if(year != null) {
            this.year = year.toString();
        } else {
            this.year = "";
        }
        if(trackNum != null) {
            this.trackNum = Long.valueOf(trackNum.toString());
        } else {
            this.trackNum = null;
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Artist getArtist() {
        return artist;
    }
    
    public Album getAlbum() {
        return album;
    }
    
    public String getReadableDuration() {
        int durationIndexOf = duration.toString().indexOf(".");
        int tempDuration = 0;
        if(durationIndexOf > 0) {
            tempDuration = Integer.valueOf(duration.toString().substring(0, durationIndexOf));
        }
        if (tempDuration > 0) {
            //hours = totalSecs / 3600;
            int minutes = (tempDuration % 3600) / 60;
            int seconds = tempDuration % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return "";
        }
    }
    
    public Double getDuration() {
        return duration;
    }
    
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getBitrate() {
        return bitrate + " kBit/s";
    }
    
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
    
    public String getYear() {
        if(!year.isEmpty()) {
            return year;
        } else {
            return "";
        }
    }
    
    public String getFileName() {
        String badFileName = artist.getName() + " - " + name + ".mp3";
        
        final int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
        Arrays.sort(illegalChars);

        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int)badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char)c);
            }
        }
        return Main.getConfig().getDownloadDirectory() + File.separator + cleanName.toString();
    }
    
    public Long getTrackNum() {
        return trackNum;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Song");
        sb.append("{songID=").append(id);
        sb.append(", songName='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
