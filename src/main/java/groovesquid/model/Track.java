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

import groovesquid.util.Utils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Maino
 */
public class Track {

    public static enum Status {
        QUEUED, INITIALIZING, DOWNLOADING, FINISHED, CANCELLED, ERROR;

        public boolean isSuccessful() {
            return this == FINISHED;
        }

        public boolean isFinished() {
            return this == FINISHED || this == CANCELLED || this == ERROR;
        }
        
        public boolean isDownloading() {
            return this == DOWNLOADING;
        }
    }
    
    private final Song song;
    private transient Store store;
    private long totalBytes;
    private long downloadedBytes;
    private Status status;
    private Date date;
    private String path;
    private Long startDownloadTime;
    private Long stopDownloadTime;
    
    public Track(Song song, Store store) {
        this.song = song;
        this.store = store;
        this.totalBytes = 0;
        this.downloadedBytes = 0;
        this.status = Status.QUEUED;
        this.date = new Date();
        this.path = store.getDescription();
        this.startDownloadTime = 0L;
        this.stopDownloadTime = 0L;
    }
    
    public Song getSong() {
        return song;
    }
    
    public Store getStore() {
        return store;
    }
    
    public void setStore(Store store) {
        this.store = store;
    }
    
    public int getProgress() {
        return (int)(totalBytes > 0 ? (downloadedBytes * 100.0) / totalBytes : 0.0);
    }
    
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (!this.status.isFinished()) {
            this.status = status;
        }
    }
    
    public long getTotalBytes() {
        return totalBytes;
    }
    
    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }
    
    public void updateDownloadedBytes() {
        downloadedBytes++;
    }
    
    public void incDownloadedBytes(long increment) {
        this.downloadedBytes += increment;
    }
    
    public String getDownloadedSize() {
        return Utils.humanReadableByteCount(downloadedBytes, true);
    }
    
    public String getSize() {
        return Utils.humanReadableByteCount(totalBytes, true);
    }
    
    public String getPath() {
        return path;
    }
    
    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }
    
    public long getStartDownloadTime() {
        return startDownloadTime;
    }
    
    public void setStartDownloadTime(long startDownloadTime) {
        this.startDownloadTime = startDownloadTime;
    }
    
    public long getStopDownloadTime() {
        return stopDownloadTime;
    }
    
    public void setStopDownloadTime(long stopDownloadTime) {
        this.stopDownloadTime = stopDownloadTime;
    }
    
    public Long getDownloadDuration() {
        if (startDownloadTime == null)
            return null;
        else if (stopDownloadTime != null)
            return stopDownloadTime - startDownloadTime;
        else
            return System.currentTimeMillis() - startDownloadTime;
    }

    public Double getDownloadRate() {
        Long downloadDuration = getDownloadDuration();
        if (downloadDuration != null && downloadDuration > 0 && downloadedBytes > 0)
            return (double) downloadedBytes / downloadDuration * 1000.0;
        else
            return 0.0D;
    }
    
    /*private void updateTableCell() {
        SwingUtilities.invokeLater(new Runnable(){public void run(){
            ((DownloadTableModel)Main.getGui().getDownloadTable().getModel()).fireTableCellUpdated(song, 5);
        }});
    }*/
}
