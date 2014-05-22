package org.shunya.punter.gui;

import org.junit.Test;

import javax.swing.*;
import javax.swing.RowFilter.Entry;

import static org.junit.Assert.fail;

public class TableSearchFilterTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testApplyFilter() throws Exception {
		TableSearchFilter tableSearchFilter=new TableSearchFilter();
		RowFilter<ProcessTableModel, Object> rf = tableSearchFilter.getRowFilter("test munish chandel");
		rf.include(new Entry<ProcessTableModel, Object>() {

			@Override
			public Object getIdentifier() {
				return null;
			}

			@Override
			public ProcessTableModel getModel() {
				return null;
			}

			@Override
			public Object getValue(int index) {
				return null;
			}

			@Override
			public int getValueCount() {
				return 0;
			}		});
	}

}
