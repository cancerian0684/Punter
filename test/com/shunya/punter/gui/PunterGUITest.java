package com.sapient.punter.gui;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;

import org.junit.Test;

public class PunterGUITest {

	@Test
	public void newFilter() throws Exception {
		 RowFilter<ProcessTableModel, Object> rf = null;
	        try {
	            String text="test munish";
				rf = RowFilter.regexFilter(text.toLowerCase(), 0);
	            String[] tokens = text.toLowerCase().split("[\\s;,]");
	            List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>(tokens.length);
	            for (int i = 0; i < tokens.length; i++) {
					String string = tokens[i];
					filters.add(RowFilter.regexFilter(string));
				}
	            rf = RowFilter.orFilter(filters);
	        } catch (java.util.regex.PatternSyntaxException e) {
	            return;
	        }
	}
}
