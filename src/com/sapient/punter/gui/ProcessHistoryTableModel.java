package com.sapient.punter.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.sapient.punter.jpa.ProcessHistory;
 
public class ProcessHistoryTableModel extends AbstractTableModel {
	public final Object[] longValues = {"Kathy123sdljflsdfl"};
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd, MMM hh:mm:ss aa");
	/** Holds the table data in a two dimensional ArrayList datastructure */
	private ArrayList<Object>  data=new ArrayList<Object>();          

	/** Holds the column names */         
	private String [] columnNames = new String [] 
                                  {"<html><b>Run ID"};
	private Class [] columnClasses = new Class[] 
                                   {String.class};
  
  /**
   * Constructor: Initializes the table structure, including number of columns
   * and column headings. Also initializes table data with default values.
   * @param <b>columns[] </b> array of column titles.
   * @param <b>defaultv </b> array of default value objects, for each column.
   * @param <b>rows </b> number of rows initially.
   */
  public ArrayList<Object> getModelData(){
	  ArrayList<Object>  newdata=new ArrayList<Object>(data); 
	  return newdata;
  }
  public ProcessHistoryTableModel() {
	  
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

  /**
   * Overrides AbstractTableModel method. 
   * @param <b>column number </b> column number
   * @return <b>String </b> column name.
   */
  public String getColumnName(int col){
    return columnNames[col];
  }

  /**
   * Overrides AbstractTableModel method.
   * @param <b>rows </b> row number
   * @param <b>rows </b> column number   
   * @return <b>Object</b> the value at the specified cell.
   */
  public Object getValueAt(int row, int col) {
    ArrayList colArrayList = (ArrayList) data.get(row);
    ProcessHistory ph=(ProcessHistory) colArrayList.get(0);
    return ""+ph.getId()+"  [ "+sdf.format(ph.getStartTime())+" ]"+" "+ph.getRunStatus().toString().charAt(0);
  }

  /**
   * Overrides AbstractTableModel method. Returns the class for the
   * specified column.
   * @param <b>col </b> column number  
   * @return <b> Class </b> the class for the specified column.  
   */
  public Class getColumnClass(int col) {
    return columnClasses[col];
  }

  /**
   * Overrides AbstractTableModel method. Sets the value at the specified cell
   * to object.
   * @param <b>Object </b> 
   * @param <b>row </b> row number   
   * @param <b>column </b> column number   
   * @return <b> Class </b> the class for the specified column.    
   */
  public void setValueAt( Object obj, int row, int col ) {
    ArrayList colArrayList = (ArrayList)data.get(row);
//    colArrayList.set( col, obj);
               	
     super.fireTableDataChanged();
  }

  /**
   * Adds a new row to the table.
   * @param <b>ArrayList </b> new row data
   */
  public synchronized ArrayList insertRowAtBeginning( ArrayList newrow ) {
    data.add(0,newrow);
    super.fireTableRowsInserted(0,0);
    return (ArrayList) data.get(0);
  }
  
  public synchronized ArrayList insertRow( ArrayList newrow ) {
	    data.add(newrow);
	    super.fireTableRowsInserted(data.size()-1,data.size()-1);
	    return (ArrayList) data.get(data.size()-1);
	  }

  /**
   * Deletes the specified row from the table.
   * @param <b>row </b> row number   
   */
  public synchronized void deleteRow(int row) {
    data.remove(row);
    super.fireTableDataChanged();
  }

  
  public synchronized void deleteRows(ArrayList<Object> rows) {
	    data.removeAll(rows);
	    super.fireTableDataChanged();
	  }
  /**
   *  Delete all the rows existing after the selected row from the JTable
   * @param <b>row </b> row number 
   */
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
  /**
   * Returns the values at the specified row as a ArrayList.
   * @param <b>row </b> row number
   */
  public ArrayList getRow(int row) {
    return (ArrayList) data.get(row);
  }

  /**
   * Updates the specified row. It replaces the row ArrayList at the specified
   * row with the new ArrayList.
   * @param <b>ArrayList </b> row data
   * @param <b>row </b> row number   
   */
  public void updateRow( ArrayList updatedRow, int row ) {
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
    data = new ArrayList();
    super.fireTableDataChanged();
  }
  public boolean isCellEditable(int row, int col) {  
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