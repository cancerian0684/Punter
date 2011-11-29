package com.sapient.punter.gui;

import java.util.Map;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.jpa.TaskData;
import com.sapient.punter.utils.OutputParamValue;
 class OutputParamTableModel extends AbstractTableModel {
        private String[] columnNames = {"<html><b>Property",
                                        "<html><b>Value"};
        public OutputParamTableModel() {
			// TODO Auto-generated constructor stub
		}
        public OutputParamTableModel(Object[][] data) {
        	this.data=data;
		}
        public OutputParamTableModel(TaskData t) {
        	Map<String, OutputParamValue> prop = t.getOutputParams();
        	int size=prop.size();
        	this.data=new Object[size][3];
        	int i=0;
        	for(Object key : prop.keySet()) {  
        		OutputParamValue value = prop.get(key);    
//        		System.out.println(key + " = " + value.getValue());
        		this.data[i][0]=key;
        		this.data[i][1]=value.getValue();
        		this.data[i][2]=t;
        		i++;
        		}
		}
        private Object[][] data = {        };

        public final static int[] width = {60,126};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (true) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);
            TaskData t=(TaskData) data[row][2];
            OutputParamValue opv=t.getOutputParams().get((String)data[row][0]);
            opv.setValue((String)value);
            try {
            	StaticDaoFacade.getInstance().saveTask(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
            if (true) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }