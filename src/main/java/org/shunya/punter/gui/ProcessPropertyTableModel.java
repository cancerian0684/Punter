package org.shunya.punter.gui;

import it.sauronsoftware.cron4j.Predictor;
import org.apache.commons.beanutils.BeanUtils;
import org.shunya.punter.jpa.ProcessData;
import org.shunya.punter.utils.FieldPropertiesMap;
import org.shunya.server.component.DBService;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ProcessPropertyTableModel extends AbstractTableModel {
	public final List<Integer> width = asList(150,206);
	private static final long serialVersionUID = 1L;
    private final DBService dbService;
    /** Holds the table data in a two dimensional ArrayList datastructure */
	private ArrayList<Object>  data=new ArrayList<Object>();          

	/** Holds the column names */         
	private String [] columnNames = new String [] 
                                  {"<html><b>Property","<html><b>Value"};
	private Class [] columnClasses = new Class[] 
                                   {String.class,String.class};

  public ArrayList<Object> getModelData(){
	  ArrayList<Object>  newdata=new ArrayList<Object>(data); 
	  return newdata;
  }
  public ProcessPropertyTableModel(DBService dbService) {

      this.dbService = dbService;
  }

  /**
   * Overrides AbstractTableModel method. Returns the number of columns in table
   * @return <b>int</b> number of columns in the table.
   */
  public int getColumnCount() {
    return columnNames.length;
  }

  /**
   * Overrides AbstractTableModel method. Returns the number of rows in table
   * @return <b>int</b> number of rows in the table.   
   */
  public int getRowCount() {
    return data.size();
  }

  public String getColumnName(int col){
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    ArrayList<?> colArrayList = (ArrayList<?>) data.get(row);
    return colArrayList.get(col);
  }
  public Class<?> getColumnClass(int col) {
    return columnClasses[col];
  }

  public void setValueAt( Object obj, int row, int col ) {
    ArrayList<Object> colArrayList = (ArrayList<Object>)data.get(row);
    colArrayList.set( col, obj);
    try{
    	ProcessData processData=(ProcessData) colArrayList.get(2);
        FieldPropertiesMap propertiesMap = processData.getInputParams();
        propertiesMap.get((String) colArrayList.get(0)).setValue((String) obj);
        processData.setInputParams(propertiesMap);
    	processData = dbService.saveProcess(processData);
    	BeanUtils.copyProperties(colArrayList.get(2), processData);
    	try{
    	if("scheduleString".equalsIgnoreCase((String)colArrayList.get(0))){
    		Predictor pr = new Predictor((String) obj);
    		 for (int i = 0; i < 4; i++) {
    		        System.out.println(pr.nextMatchingDate()+" ");
    		 }
    		 System.out.println("----------xxx-------------");
    	}
    	}catch (Exception e) {
    		System.err.println("Wrong Format for Schedule");
		}
    }catch (Exception e) {
    	e.printStackTrace();
	}
     super.fireTableDataChanged();
  }
 /* public synchronized ArrayList insertRow( ArrayList newrow ) {
    data.add(0,newrow);
    super.fireTableRowsInserted(0,0);
    return (ArrayList) data.get(0);
  }*/
  
  public synchronized ArrayList<?> insertRow( ArrayList<?> newrow ) {
	    data.add(newrow);
	    super.fireTableRowsInserted(data.size()-1,data.size()-1);
	    return (ArrayList<?>) data.get(data.size()-1);
	  }

  public synchronized void deleteRow(int row) {
    data.remove(row);
    super.fireTableDataChanged();
  }

  
  public synchronized void deleteRows(ArrayList<Object> rows) {
	    data.removeAll(rows);
	    super.fireTableDataChanged();
	  }
  public void deleteAfterSelectedRow(int row){
    // Get the initial size of the table before the deletion has started.
    int size = this.getRowCount();

    // The number of items to be deleted is got by subtracting the
    // selected row + 1 from size. This is done because as each row is deleted
    // the rows next to it are moved up by one space and the number of rows
    // in the JTable decreases. So the technique used here is always deleting
    // the row next to the selected row from the table n times so that all the
    // rows after the selected row are deleted.
    int n = size-(row+1);
    for(int i=1;i<=n;i++){
      data.remove(row+1);
    }
    super.fireTableDataChanged();
  }

  public ArrayList<?> getRow(int row) {
    return (ArrayList<?>) data.get(row);
  }

  public void updateRow( ArrayList<?> updatedRow, int row ) {
    data.set( row, updatedRow);
    super.fireTableDataChanged();
  }
  public synchronized void refreshTable(){
	  for(int i=0;i<getRowCount();i++){
	  for(int j=0;j<getColumnCount();j++){
		  super.fireTableCellUpdated(i, j);
	  }
	  }
  }
  /**
   * Clears the table data.
   */
  public void clearTable() {
    data = new ArrayList<Object>();
    super.fireTableDataChanged();
  }
  public boolean isCellEditable(int row, int col) {
	  if(col==1){
		  return true;
	  }
      return false;
      }
  private void printDebugData() {
      int numRows = getRowCount();
      int numCols = getColumnCount();

      for (int i=0; i < numRows; i++) {
          System.out.print("    row " + i + ":");
          for (int j=0; j < numCols; j++) {
              System.out.print("  " + data.get(i));
          }
          System.out.println();
      }
      System.out.println("--------------------------");
  }
}