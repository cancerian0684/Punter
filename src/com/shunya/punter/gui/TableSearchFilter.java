package com.shunya.punter.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

public class TableSearchFilter {
	public void applyFilter(TableRowSorter<ProcessTableModel> sorter, String text) {
        sorter.setStringConverter(getTableStringConverter());
        RowFilter<ProcessTableModel, Object> rf = getRowFilter(text);
        if(rf==null) return;
        sorter.setRowFilter(rf);
    }

	private TableStringConverter getTableStringConverter() {
		return new TableStringConverter() {
			@Override
			public String toString(TableModel model, int row, int column) {
				String tmp=(String) model.getValueAt(row, column);
				return  tmp.toLowerCase();
			}
		};
	}

	public RowFilter<ProcessTableModel, Object> getRowFilter(String text) {
		RowFilter<ProcessTableModel, Object> rf = null;
        try {
            rf = RowFilter.regexFilter(text.toLowerCase(), 0);
            String[] tokens = text.toLowerCase().split("[\\s;,]");
            List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>(tokens.length);
            for (int i = 0; i < tokens.length; i++) {
				String string = tokens[i];
				filters.add(RowFilter.regexFilter(string));
			}
            rf = RowFilter.andFilter(filters);
        } catch (java.util.regex.PatternSyntaxException e) {
            return  null;
        }
		return rf;
	}
}
