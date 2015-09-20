package org.shunya.punter.gui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUIUtils {
    public static List<Integer> getColumnWidth(JTable table) {
        List<Integer> columnWidth = new ArrayList(table.getColumnModel().getColumnCount());
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            columnWidth.add(table.getColumnModel().getColumn(i).getPreferredWidth());
        }
        return columnWidth;
    }

    public static void initilializeTableColumns(JTable table, List<Integer> size) {
        TableCellRenderer dcr = table.getDefaultRenderer(Integer.class);
        if (dcr instanceof JLabel) {
            ((JLabel) dcr).setHorizontalAlignment(SwingConstants.CENTER);
        }
        table.setDefaultRenderer(Integer.class, dcr);
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            TableCellRenderer headerRenderer = header.getDefaultRenderer();
            if (headerRenderer instanceof JLabel) {
                ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
            }
            header.setPreferredSize(new Dimension(30, 25));
        }
        for (int i = 0; i < size.size(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(size.get(i));
        }
    }
}
