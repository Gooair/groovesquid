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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Maino
 */
public class TableHeaderCellRenderer implements TableCellRenderer {

    private final TableCellRenderer wrappedRenderer;
    private final JLabel label;
    private Color color;
    
    public TableHeaderCellRenderer(TableCellRenderer wrappedRenderer, Color color) {
        if (!(wrappedRenderer instanceof JLabel)) {
            throw new IllegalArgumentException("The supplied renderer must inherit from JLabel");
        }
        this.wrappedRenderer = wrappedRenderer;
        this.label = (JLabel) wrappedRenderer;
        this.color = color;
    }  

    @Override  
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        wrappedRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(label.getPreferredSize().width, 30));
        label.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
        if(column > 0) {
            label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, new Color(204, 204, 204)), BorderFactory.createMatteBorder(0, 1, 0, 0, color)), BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        } else {
            label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, new Color(204, 204, 204)), BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        }
        label.setHorizontalTextPosition(10);

        return label;
    }
}
