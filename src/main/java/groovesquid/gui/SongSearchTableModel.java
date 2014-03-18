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
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author Maino
 */
public class SongSearchTableModel extends AbstractTableModel {

    private String[] columnNames = { Main.getLocaleString("SONG"), Main.getLocaleString("ARTIST"), Main.getLocaleString("ALBUM"), Main.getLocaleString("DURATION"), Main.getLocaleString("YEAR") };
    
    private List<Song> songs = new ArrayList<Song>();

    public SongSearchTableModel() {
        
    }
    
    public SongSearchTableModel(List<Song> songs) {
        this.songs = songs;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return songs.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        Song song = songs.get(row);

        switch (col) {
            case 0: return song.getName();
            case 1: return song.getArtist().getName();
            case 2: return song.getAlbum().getName();
            case 3: return song.getReadableDuration();
            case 4: return song.getYear();
        }
        return null;
    }
    
    public List<Song> getSongs() {
        return songs;
    }
        
    public void removeRow(int row) {
        songs.remove(row);
        fireTableDataChanged();
    }
    
    public void removeRow(Song song) {
        songs.remove(song);
        fireTableDataChanged();
    }
    
    public void addRow(Song song) {
        songs.add(song);
        fireTableDataChanged();
    }
    
    public void addRows(List<Song> songs) {
        this.songs.addAll(songs);
        fireTableDataChanged();
    }
    
    public void setRows(List<Song> songs) {
        this.songs = songs;
        fireTableDataChanged();
    }
}
