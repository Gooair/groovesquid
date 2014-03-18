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
import groovesquid.model.Playlist;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author Maino
 */
public class PlaylistSearchTableModel extends AbstractTableModel {

    private String[] columnNames = { Main.getLocaleString("NAME"), Main.getLocaleString("AUTHOR"), Main.getLocaleString("SONGS") };
    
    private List<Playlist> playlists = new ArrayList<Playlist>();

    public PlaylistSearchTableModel() {
        
    }
    
    public PlaylistSearchTableModel(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return playlists.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        Playlist playlist = playlists.get(row);

        switch (col) {
            case 0: return playlist.getName();
            case 1: return playlist.getAuthor();
            case 2: return playlist.getNumSongs();
        }
        return null;
    }
    
    public List<Playlist> getPlaylists() {
        return playlists;
    }
        
    public void removeRow(int row) {
        playlists.remove(row);
        fireTableStructureChanged();
    }
    
    public void removeRow(Playlist song) {
        playlists.remove(song);
        fireTableStructureChanged();
    }
    
    public void addRow(Playlist song) {
        playlists.add(song);
        fireTableStructureChanged();
    }
}
