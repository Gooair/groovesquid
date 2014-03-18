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

package groovesquid.gui;

import groovesquid.Main;
import groovesquid.model.Song;
import groovesquid.model.Track;
import groovesquid.service.Services;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Maino
 */
public class DownloadTableModel extends AbstractTableModel {
    
    private String[] columnNames = { Main.getLocaleString("SONG"), Main.getLocaleString("ARTIST"), Main.getLocaleString("ALBUM"), Main.getLocaleString("PATH"), Main.getLocaleString("DATE"), Main.getLocaleString("PROGRESS") };

    private List<Track> songDownloads = new ArrayList<Track>();

    public DownloadTableModel() {
        
    }
    
    public DownloadTableModel(List<Song> songs) {
        for (Song song : songs) { 
            Track track = Services.getDownloadService().download(song);
            songDownloads.add(track);
            fireTableDataChanged();
        }
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return songDownloads == null ? 0 : songDownloads.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Track track = songDownloads.get(rowIndex);
        switch (columnIndex) {
            case 0: return track.getSong().getName();
            case 1: return track.getSong().getArtist().getName();
            case 2: return track.getSong().getAlbum().getName();
            case 3: return track.getPath();
            case 4: return track.getDate();
            case 5: return track.getProgress();
        }
        return null;
    }
    
    public void setValueAt(Object obj, Track songDownload, int col) {
        int index = songDownloads.indexOf(songDownload);
        setValueAt(obj, col, index);
        fireTableCellUpdated(index, col);
        updateSongDownloads();
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        if(songDownloads.size() > 0 && getRowCount() > 0) {
            return getValueAt(0, column).getClass();
        } else {
            return Object.class;
        }
    }
    
    public List<Track> getSongDownloads() {
        return songDownloads;
    }
    
    public void setSongDownloads(List<Track> songDownloads) {
        this.songDownloads = songDownloads;
        fireTableDataChanged();
        updateSongDownloads();
    }
    
    public Track getSongDownload(Song song) {
        for(Track songDownload : songDownloads) {
            if(songDownload.getSong().equals(song)) {
                return songDownload;
            }
        }
        return null;
    }
    
    public int getSongDownloadIndex(Song song) {
        for(Track songDownload : songDownloads) {
            if(songDownload.getSong().equals(song)) {
                return songDownloads.indexOf(songDownload);
            }
        }
        return -1;
    }
        
    public void removeRow(int row) {
        songDownloads.remove(row);
        //fireTableDataChanged();
        fireTableRowsDeleted(row, row);
        updateSongDownloads();
    }
    
    public void removeRow(Track songDownload) {
        songDownloads.remove(songDownload);
        //fireTableDataChanged();
        updateSongDownloads();
    }
    
    public void addRow(Track songDownload) {
        songDownloads.add(songDownload);
        fireTableDataChanged();
        updateSongDownloads();
    }

    public void addRow(int row, Track songDownload) {
        songDownloads.add(row, songDownload);
        fireTableDataChanged();
        updateSongDownloads();
    }
    
    public void updateSongDownloads() {
        Main.getConfig().setDownloads(songDownloads);
    }

    public void fireTableCellUpdated(Song song, int col) {
        fireTableCellUpdated(getSongDownloadIndex(song), col);
    }

}
