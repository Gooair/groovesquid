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
import groovesquid.model.Artist;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author Maino
 */
public class ArtistSearchTableModel extends AbstractTableModel {

    private String[] columnNames = { Main.getLocaleString("NAME") };
    
    private List<Artist> artists = new ArrayList<Artist>();

    public ArtistSearchTableModel() {
        
    }
    
    public ArtistSearchTableModel(List<Artist> artists) {
        this.artists = artists;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return artists.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        Artist artist = artists.get(row);

        switch (col) {
            case 0: return artist.getName();
        }
        return null;
    }
    
    public List<Artist> getArtists() {
        return artists;
    }
        
    public void removeRow(int row) {
        artists.remove(row);
        fireTableStructureChanged();
    }
    
    public void removeRow(Artist song) {
        artists.remove(song);
        fireTableStructureChanged();
    }
    
    public void addRow(Artist song) {
        artists.add(song);
        fireTableStructureChanged();
    }
}
