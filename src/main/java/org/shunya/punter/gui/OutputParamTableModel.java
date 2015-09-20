package org.shunya.punter.gui;

import org.apache.commons.beanutils.BeanUtils;
import org.shunya.punter.jpa.TaskData;
import org.shunya.punter.utils.FieldProperties;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.server.component.DBService;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBException;
import java.util.List;

import static java.util.Arrays.asList;

class OutputParamTableModel extends AbstractTableModel {
    private final DBService dbService;
    private String[] columnNames = {"<html><b>Property",
            "<html><b>Value"};

    public OutputParamTableModel(DBService dbService) {
        this.dbService = dbService;
    }

    public OutputParamTableModel(TaskData t, DBService dbService) throws JAXBException {
        this.dbService = dbService;
        FieldPropertiesMap prop = t.getOutputParamsAsObject();
        int size = prop.keySet().size();
        this.data = new Object[size][3];
        int i = 0;
        for (String key : prop.keySet()) {
            FieldProperties value = prop.get(key);
//        		System.out.println(key + " = " + value.getValue());
            this.data[i][0] = key;
            this.data[i][1] = value.getValue();
            this.data[i][2] = t;
            i++;
        }
    }

    private Object[][] data = {};

    public final static List<Integer> width = asList(60, 126);

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
        if (true) {
            try {
                TaskData taskData = (TaskData) data[row][2];
                FieldPropertiesMap propertiesMap = taskData.getOutputParamsAsObject();
                FieldProperties opv = propertiesMap.get((String) data[row][0]);
                opv.setValue((String) value);
                taskData.setOutputParamsAsObject(propertiesMap);
                dbService.saveTask(taskData);
                BeanUtils.copyProperties(data[row][2], taskData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("New value of data:");
            printDebugData();
        }
    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print("  " + data[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}