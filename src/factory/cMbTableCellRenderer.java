package factory;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * JSA
 * Thomas Jefferson National Accelerator Facility
 * *
 * This software was developed under a United States
 * Government license, described in the NOTICE file
 * included as part of this distribution.
 * *
 * Copyright (c)
 *
 * @author gurjyan
 */
public class cMbTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        if(value!=null){
            Color c;
            if (row % 2 == 0 && !table.isCellSelected(row, column)) {
                c=new Color(240,240,240);
                cell.setBackground(c);
            }
            else {
                cell.setBackground(Color.white);
                c = Color.white;
            }

            if (value instanceof String ) {
                String sever = (String) value;
                if(sever.contains("WARN")  || sever.contains("warn")){
                    cell.setBackground(Color.YELLOW);
                } else if (sever.contains("ERROR")  || sever.contains("error")){
                    cell.setBackground(Color.RED);
                } else if (sever.equalsIgnoreCase("SEVERE")){
                    cell.setBackground(Color.RED);
                } else {
                    cell.setBackground(c);
                }
            }
        }
        return cell;
    }
}


