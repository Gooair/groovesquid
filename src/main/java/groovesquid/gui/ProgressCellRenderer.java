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

import groovesquid.model.Track;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
    public ProgressCellRenderer() {
        super(0, 100);
        setValue(0);
        setString("");
        setStringPainted(true);
        setOpaque(false);
    }

    @Override
    public boolean isDisplayable() { 
        return true; 
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text = "";
        Track track = ((DownloadTableModel)table.getModel()).getSongDownloads().get(row);
        
        if(track != null) {
            Double downloadRate = track.getDownloadRate();
            if(track.getStatus() != null) {
                switch (track.getStatus()) {
                    case INITIALIZING:
                        text = "initializing...";
                        break;
                    case QUEUED:
                        text = "waiting...";
                        break;
                    case DOWNLOADING:
                        if (downloadRate != null) {
                            text = String.format("%1.0f%%, %d of %d kB, %1.0f kB/s",
                                    track.getProgress() * 1.0,
                                    track.getDownloadedBytes() / 1024,
                                    track.getTotalBytes() / 1024,
                                    downloadRate / 1024);
                        } else {
                            text = String.format("%1.0f%%, %d kB",
                                    track.getProgress() * 1.0,
                                    track.getTotalBytes() / 1024);
                        }
                        break;
                    case FINISHED:
                        downloadRate = track.getDownloadRate();
                        if (downloadRate != null) {
                            text = String.format("%d kB, %1.0f kB/s",
                                    track.getDownloadedBytes() / 1024,
                                    downloadRate / 1024);
                        } else {
                            text = String.format("%d kB",
                                    track.getDownloadedBytes() / 1024);
                        }
                        break;
                    case CANCELLED:
                        text = "cancelled";
                        break;
                    case ERROR:
                        text = "Error";
                        value = 100;
                        break;
                }
            }
        }
        
        this.setValue((Integer)value);
        this.setString(text);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            if (row % 2 == 0) {
                setBackground(new Color(242,242,242));
            } else {
                setBackground(new Color(230,230,230));
            }
            setBackground(table.getBackground());
        }
        
        setBorderPainted(false);
        setForeground(new Color(243,156,18));
        setFont(table.getFont());
        
        return this;
    }
}