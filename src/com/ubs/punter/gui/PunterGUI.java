/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package com.ubs.punter.gui;

/*
 * TableRenderDemo.java requires no other files.
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.ubs.punter.Tasks;
import com.ubs.punter.jpa.Process;
import com.ubs.punter.jpa.StaticDaoFacade;
import com.ubs.punter.jpa.Task;

/** 
 * TableRenderDemo is just like TableDemo, except that it
 * explicitly initializes column sizes and it uses a combo box
 * as an editor for the Sport column.
 */
public class PunterGUI extends JPanel {
    private boolean DEBUG = false;
    private static JFrame frame;
    private final JTable taskTable;
    private final JTable processTable;
    public PunterGUI() throws Exception {
        super(new GridLayout(1,0));
        processTable=new JTable(new ProcessTableModel());
        ProcessTableModel model=(ProcessTableModel) processTable.getModel();
        List<Process> pl = StaticDaoFacade.getProcessList();
        for (Process p : pl) {
        	final ArrayList<Object> newRequest = new ArrayList<Object>();
        	newRequest.add(p.getName());
        	newRequest.add(p);
        	model.insertRow(newRequest);
		}
        processTable.setShowGrid(true);
        processTable.setPreferredScrollableViewportSize(new Dimension(100, 270));
        processTable.setFillsViewportHeight(true);
        processTable.setRowHeight(20);
        InputMap imap = processTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    ActionMap amap = processTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(processTable.getSelectedRow() != -1){
	                	System.out.println("Delete Key Pressed.");
	                	ProcessTableModel tableModel=(ProcessTableModel) processTable.getModel();
	                	int[] selectedRows = processTable.getSelectedRows();
						ArrayList<Object> selectedRowsData = new ArrayList<Object>();
						for (int selectedRow : selectedRows) {
							ArrayList request=tableModel.getRow(selectedRow);
							selectedRowsData.add(request);
			                Process proc=(Process) request.get(1);
							try {
								System.err.println("removing process : "+proc.getId());
								StaticDaoFacade.removeProcess(proc);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						tableModel.deleteRows(selectedRowsData);
						if(tableModel.getRowCount()>0){
							processTable.setRowSelectionInterval(0, 0);
						}
	                }
			}});
	    
        taskTable = new JTable(new TaskTableModel());
        taskTable.setShowGrid(true);
        taskTable.setShowVerticalLines(false);
        taskTable.setPreferredScrollableViewportSize(new Dimension(400, 270));
        taskTable.setFillsViewportHeight(true);
        imap = taskTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    amap = taskTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				 if(taskTable.getSelectedRow() != -1){
	                	System.out.println("Delete Key Pressed.");
	                	TaskTableModel tableModel=(TaskTableModel) taskTable.getModel();
	                	int[] selectedRows = taskTable.getSelectedRows();
						ArrayList<Object> selectedRowsData = new ArrayList<Object>();
						for (int selectedRow : selectedRows) {
							ArrayList request=tableModel.getRow(selectedRow);
							selectedRowsData.add(request);
			                Task task=(Task) request.get(5);
							try {
								System.err.println("removing task : "+task.getId());
								StaticDaoFacade.removeTask(task);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						tableModel.deleteRows(selectedRowsData);
	                }
			}});
        final JMenuItem addTaskMenu;
		final JPopupMenu popupTask = new JPopupMenu();
		addTaskMenu = new JMenuItem("Add Task");
	    addTaskMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  ArrayList ar = ((ProcessTableModel)processTable.getModel()).getRow(processTable.getSelectedRow());
	        	  long l=(Long)((Process)ar.get(1)).getId();
	        	  System.out.println(l);
	        	  try{
	        		  String taskPackage="com.ubs.punter.tasks.";
	        		  Object[] possibilities = {"EchoTask", "GrepTask", "TestTask"};
	        		  String s = (String)JOptionPane.showInputDialog(
	        				  			  frame,
	        		                      "Choose the Task\n",
	        		                      "Tasks Dialog",
	        		                      JOptionPane.PLAIN_MESSAGE,
	        		                      null,
	        		                      possibilities,
	        		                      "EchoTask");

	        		  //If a string was returned, say so.
	        		  if ((s != null) && (s.length() > 0)) {
	        		      System.out.println("Selected Task... " + s + "!");
	        		      Class<?> cls = Class.forName(taskPackage+s);
	    	        	  List<String> inParams = Tasks.listInputParams((Tasks)cls.newInstance());
	    	        	  List<String> outParams = Tasks.listOutputParams((Tasks)cls.newInstance());
	    	        	  Properties inProp=new Properties();
	    	        	  for (String key : inParams) {
	    					System.err.println(key);
	    					inProp.setProperty(key, "");
	    	        	  }
	    	        	  Properties outProp=new Properties();
	    	        	  for (String key : outParams) {
	    					System.err.println(key);
	    					outProp.setProperty(key, "");
	    	        	  }
	    	        	  Task task=new Task();
	    	        	  task.setInputParams(inProp);
	    	        	  task.setOutputParams(outProp);
	    	        	  task.setName(taskPackage+s);
	    	        	  Process p=new Process();
	    	        	  p.setId(l);
	    	        	  task.setProcess(p);
	    	        	  StaticDaoFacade.createTask(task);
	    	        	  TaskTableModel model=(TaskTableModel) taskTable.getModel();
	    	        	  final ArrayList<Object> newRequest = new ArrayList<Object>();
	    	              	newRequest.add(task.getId());
	    	              	newRequest.add(task.getName());
	    	              	newRequest.add(task.getSequence());
	    	              	newRequest.add(task.getId());
	    	              	newRequest.add("munishc");
	    	              	newRequest.add(task);
	    	              	newRequest.add(p);
	    	              	model.insertRow(newRequest);
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
	    popupTask.add(addTaskMenu);
	    taskTable.addMouseListener(new MouseAdapter() {
	          //JPopupMenu popup;
	          public void mousePressed(MouseEvent e) {
      		  if (SwingUtilities.isRightMouseButton(e)) {
      			  popupTask.show(e.getComponent(), 
                            e.getX(), e.getY());
      		 }
	          }
	      });
	    
	    final JMenuItem addProcessMenu, runProcessMenu;
		final JPopupMenu popupProcess = new JPopupMenu();
		addProcessMenu = new JMenuItem("Add Process");
		addProcessMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  System.out.println("Adding new Process");
	        	  try{
	        		  Process proc=new Process();
	        		  proc.setName("new Process");
	        		  StaticDaoFacade.createProcess(proc);
	        		  ProcessTableModel model=(ProcessTableModel) processTable.getModel();
	        		  final ArrayList<Object> newRequest = new ArrayList<Object>();
		              	newRequest.add(proc.getName());
		              	newRequest.add(proc);
		              	model.insertRow(newRequest);
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
		runProcessMenu = new JMenuItem("Run Process");
		runProcessMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  System.out.println("Adding new Process");
	        	  try{
	        		  if(processTable.getSelectedRow()!=-1){
	        		  ProcessTableModel model=(ProcessTableModel) processTable.getModel();
	        		  ArrayList<Object> currRow=model.getRow(processTable.getSelectedRow());
	        		  Process p=(Process) currRow.get(1);
	        		  System.out.println(p.getId()+" == "+p.getName());
	        		  List<Task> ptl = StaticDaoFacade.getProcessTasksById(p.getId());
	        		  com.ubs.punter.Process process=new com.ubs.punter.Process();
	        		  for (Task task : ptl) {
						System.out.println(task.getId());
						Tasks t=Tasks.getTask(task.getName(), task.getInputParams(), task.getOutputParams());
						process.addTask(t);
	        		  	}
	        		  process.execute();
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
		popupProcess.add(addProcessMenu);
		popupProcess.add(runProcessMenu);
	    processTable.addMouseListener(new MouseAdapter() {
	          //JPopupMenu popup;
	          public void mousePressed(MouseEvent e) {
      		  if (SwingUtilities.isRightMouseButton(e)) {
      			popupProcess.show(e.getComponent(), 
                            e.getX(), e.getY());
      		 }
	          }
	      });
        final JTable inputParamTable = new JTable(new ParamTableModel());
        inputParamTable.setShowGrid(true);
        inputParamTable.setPreferredScrollableViewportSize(new Dimension(300, 150));
        inputParamTable.setFillsViewportHeight(true);
        
        final JTable outputParamTable = new JTable(new ParamTableModel());
        outputParamTable.setShowGrid(true);
        outputParamTable.setPreferredScrollableViewportSize(new Dimension(300, 120));
        outputParamTable.setFillsViewportHeight(true);
        initColumnSizes1(outputParamTable);
        ListSelectionModel rowSM = taskTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        		if (lsm.isSelectionEmpty()) {
        		} else {
        			int selectedRow = lsm.getMinSelectionIndex();
        			System.out.println("Row " + selectedRow + " is now selected.");
        			Object[][] data = {
        				    {"name", "Munish Chandel"},
        				    {"name", "Rohit Banyal"},
        				    {"Sue", "Black"},
        			        };
        			Task t=(Task) taskTable.getModel().getValueAt(selectedRow, 5);
        			if(t.getInputParams()!=null){
        			inputParamTable.setModel(new ParamTableModel(t,true));
        			initColumnSizes1(inputParamTable);
        			}else{        				
        				inputParamTable.setModel(new ParamTableModel());
        				initColumnSizes1(inputParamTable);
        			}
        			if(t.getOutputParams()!=null){
        			outputParamTable.setModel(new ParamTableModel(t,false));
        			initColumnSizes1(outputParamTable);
        			}else{
        				outputParamTable.setModel(new ParamTableModel());
        				initColumnSizes1(outputParamTable);
        			}
        		}
        	}
        });
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(taskTable);

        //Set up column sizes.
        initColumnSizes(taskTable);
        initColumnSizes1(inputParamTable);
        initColumnSizes2(processTable);
        //Fiddle with the Sport column's cell editors/renderers.
//        setUpSportColumn(taskTable, taskTable.getColumnModel().getColumn(2));
        JSplitPane jsp2=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(inputParamTable),new JScrollPane(outputParamTable));
        JSplitPane jsp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane,jsp2);
        jsp.setDividerLocation(0.4);
        //Add the scroll pane to this panel.
        processTable.setAutoscrolls(true);
        ListSelectionModel listSelectionModel = processTable.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    System.out.println("Row " + selectedRow + " is now selected.");
                    try{
//                    	long l=(Long) jList.getModel().getElementAt(selectedRow);
                    	ArrayList ar = ((ProcessTableModel)processTable.getModel()).getRow(processTable.getSelectedRow());
      	        	  long l=(Long)((Process)ar.get(1)).getId();
                    List<Task> taskList=StaticDaoFacade.getProcessTasksById(l);
                    Process process = StaticDaoFacade.getProcess(l);
                    TaskTableModel model=(TaskTableModel) taskTable.getModel();
                    model.clearTable();
                    for(Task t:taskList){
                    	final ArrayList<Object> newRequest = new ArrayList<Object>();
                    	newRequest.add(t.getId());
                    	newRequest.add(t.getName());
                    	newRequest.add(t.getSequence());
                    	newRequest.add(t.getId());
                    	newRequest.add("munishc");
                    	newRequest.add(t);
                    	newRequest.add(process);
                    	model.insertRow(newRequest);
                    }
                    if(taskTable.getModel().getRowCount()>0)
                    taskTable.setRowSelectionInterval(0, 0);
                    }catch (Exception ee) {
                    	ee.printStackTrace();
					}
                }
            }});
        JScrollPane listPane = new JScrollPane(processTable);
        jsp.setDividerSize(1);
        JSplitPane jsp3=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,listPane,jsp);
        jsp3.setDividerSize(1);
//        jsp3.setDividerLocation(01);
        add(jsp3);
    }

    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table) {
    	TaskTableModel model = (TaskTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 4; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, longValues[i],
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    private void initColumnSizes2(JTable table) {
    	ProcessTableModel model = (ProcessTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 1; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, longValues[i],
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    private void initColumnSizes1(JTable table) {
    	ParamTableModel model = (ParamTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
            table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 2; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 table, longValues[i],
                                 false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public void setUpSportColumn(JTable table,
                                 TableColumn sportColumn) {
        //Set up the editor for the sport cells.
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("Snowboarding");
        comboBox.addItem("Rowing");
        comboBox.addItem("Knitting");
        comboBox.addItem("Speed reading");
        comboBox.addItem("Pool");
        comboBox.addItem("None of the above");
        sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        sportColumn.setCellRenderer(renderer);
    }

   

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @throws Exception 
     */
    private static void createAndShowGUI() throws Exception {
        //Create and set up the window.
        frame = new JFrame("My Punter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        PunterGUI newContentPane = new PunterGUI();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try{
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    createAndShowGUI();
                    }catch (Exception e) {
                    	e.printStackTrace();
            		}
            }
        });
    }
}
