package com.shunya.punter.gui;

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBException;

import com.shunya.punter.utils.FieldPropertiesMap;
import org.apache.commons.beanutils.BeanUtils;

import com.shunya.kb.jpa.StaticDaoFacade;
import com.shunya.punter.jpa.TaskData;
import com.shunya.punter.utils.FieldProperties;
 class InputParamTableModel extends AbstractTableModel {
        private String[] columnNames = {"<html><b>Property",
                                        "<html><b>Value"};
        private boolean inputParam;
        public InputParamTableModel() {
		}
        public InputParamTableModel(Object[][] data) {
        	this.data=data;
		}
        public InputParamTableModel(TaskData t) throws JAXBException {
        	FieldPropertiesMap prop;
        	prop=t.getInputParams();
        	int size=prop.keySet().size();
        	this.data=new Object[size][3];
        	int i=0;
        	for(String key : prop.keySet()) {
        		FieldProperties value = (FieldProperties) prop.get(key);
//        		System.out.println(key + " = " + value.getValue());
        		this.data[i][0]=key;
        		this.data[i][1]=value.getValue();
        		this.data[i][2]=t;
        		i++;
        		}
		}
        private Object[][] data = {        };

		public static int[] width= {60,126};

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
        public void setValueAt(final Object value,final int row,final int col) {
        	data[row][col] = value;
            try {
                TaskData taskData=(TaskData) data[row][2];
                FieldPropertiesMap propertiesMap = taskData.getInputParams();
                FieldProperties fieldProperties= propertiesMap.get((String) data[row][0]);
                fieldProperties.setValue((String) value);
                taskData.setInputParams(propertiesMap);
                taskData=StaticDaoFacade.getInstance().saveTask(taskData);
				BeanUtils.copyProperties(data[row][2], taskData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			fireTableCellUpdated(row, col);
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