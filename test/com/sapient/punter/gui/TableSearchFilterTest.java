package com.sapient.punter.gui;

import static org.junit.Assert.*;

import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.table.TableRowSorter;

import org.junit.Test;

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
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ProcessTableModel getModel() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getValue(int index) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getValueCount() {
				// TODO Auto-generated method stub
				return 0;
			}		});
	}

}
