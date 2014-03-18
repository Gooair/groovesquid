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
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Maino
 */
public class StripedTable extends JTable {
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if (isCellSelected(rowIndex, vColIndex)) {
            c.setBackground(getSelectionBackground());
        } else {
            if (rowIndex % 2 == 0) {
                c.setBackground(new Color(242,242,242));
            } else {
                c.setBackground(new Color(230,230,230));
            }
        }
        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        return c;
    }
}
