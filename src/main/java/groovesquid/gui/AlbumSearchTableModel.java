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
import groovesquid.model.Album;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author Maino
 */
public class AlbumSearchTableModel extends AbstractTableModel {

    private String[] columnNames = { Main.getLocaleString("NAME"), Main.getLocaleString("ARTIST") };
    
    private List<Album> albums = new ArrayList<Album>();

    public AlbumSearchTableModel() {
        
    }
    
    public AlbumSearchTableModel(List<Album> albums) {
        this.albums = albums;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return albums.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        Album album = albums.get(row);

        switch (col) {
            case 0: return album.getName();
            case 1: return album.getArtist().getName();
        }
        return null;
    }
    
    public List<Album> getAlbums() {
        return albums;
    }
        
    public void removeRow(int row) {
        albums.remove(row);
        fireTableStructureChanged();
    }
    
    public void removeRow(Album song) {
        albums.remove(song);
        fireTableStructureChanged();
    }
    
    public void addRow(Album song) {
        albums.add(song);
        fireTableStructureChanged();
    }
}
