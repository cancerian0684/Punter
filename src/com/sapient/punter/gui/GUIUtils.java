package com.sapient.punter.gui;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GUIUtils {
	public static int [] getColumnWidth(JTable table){
		int []columnWidth=new int [table.getColumnModel().getColumnCount()];
		for(int i=0;i<columnWidth.length;i++){
			columnWidth[i]= table.getColumnModel().getColumn(i).getPreferredWidth();
		}
		return columnWidth;
	}
	
	public static void initilializeTableColumns(JTable table, int size[]) {
		TableCellRenderer dcr = table.getDefaultRenderer(Integer.class);
		if(dcr instanceof JLabel){
	        	((JLabel)dcr).setHorizontalAlignment(SwingConstants.CENTER);
	    }
		table.setDefaultRenderer(Integer.class, dcr);
        JTableHeader header = table.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
       	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 25));
        TableColumn column = null;
        for (int i = 0; i < size.length; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(size[i]);
        }
	}
}
