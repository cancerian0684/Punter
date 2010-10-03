package com.sapient.punter.gui;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.sapient.punter.executors.ProcessExecutor;
import com.sapient.punter.executors.ScheduledJobPicker;
import com.sapient.punter.jpa.ProcessDao;
import com.sapient.punter.jpa.ProcessHistory;
import com.sapient.punter.jpa.StaticDaoFacade;
import com.sapient.punter.jpa.TaskDao;
import com.sapient.punter.jpa.TaskHistory;
import com.sapient.punter.tasks.Tasks;

/** 
 * TableRenderDemo is just like TableDemo, except that it
 * explicitly initializes column sizes and it uses a combo box
 * as an editor for the Sport column.
 */
public class PunterGUI extends JPanel implements TaskObserver{
    private boolean DEBUG = false;
    private static JFrame frame;
    private final JTable taskTable;
    private final JTable processPropertyTable;
    private final JTable processTable;
    private final JTable inputParamTable;
    private final JTable outputParamTable;
    private final JTable processHistoryTable;
    private final JTable processTaskHistoryTable;
    private final JTable runningProcessTable;
    private final JTable runningTaskTable;
    private int selectedRow=-1;
    private final Timer timer;
    private static SimpleDateFormat sdf=new SimpleDateFormat("dd, MMM hh:mm:ss");
    public PunterGUI() throws Exception {
        super(new GridLayout(1,0));
        runningProcessTable=new JTable(new RunningProcessTableModel());
        runningProcessTable.setShowGrid(true);
        runningProcessTable.setPreferredScrollableViewportSize(new Dimension(330, 160));
        runningProcessTable.setFillsViewportHeight(true);
        ListSelectionModel runningProcessTableSM = runningProcessTable.getSelectionModel();
        runningProcessTableSM.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        		if (lsm.isSelectionEmpty()) {
        		} else {
        			int selectedRow = lsm.getMinSelectionIndex();
        			System.out.println("Row " + selectedRow + " is now selected.");
        			ProcessHistory ph=(ProcessHistory) ((RunningProcessTableModel) runningProcessTable.getModel()).getRow(selectedRow).get(4);
        			try {
//						ph=StaticDaoFacade.getProcessHistoryById(ph.getId());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
        			List<TaskHistory> thList = ph.getTaskHistoryList();
        			((RunningTaskTableModel)runningTaskTable.getModel()).clearTable();
        			for (TaskHistory taskHistory : thList) {
        				final ArrayList<Object> newRequest = new ArrayList<Object>();
	      	          	newRequest.add(taskHistory.getSequence());
	      	          	newRequest.add(taskHistory.getTask().getName());
	      	          	newRequest.add(taskHistory.getRunState());
	      	          	newRequest.add(taskHistory.getLogs());
	      	          	newRequest.add(taskHistory);
	      	           ((RunningTaskTableModel)runningTaskTable.getModel()).insertRow(newRequest);
					}
        		}
        	}
        });
        
        runningTaskTable=new JTable(new RunningTaskTableModel());
        runningTaskTable.setShowGrid(true);
        runningTaskTable.setPreferredScrollableViewportSize(new Dimension(300, 160));
        runningTaskTable.setFillsViewportHeight(true);
        
        JSplitPane splitRunningProcessPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(runningProcessTable), new JScrollPane(runningTaskTable));
        splitRunningProcessPane.setDividerSize(0);
//        splitRunningProcessPane.setBorder(new TitledBorder("Process Explorer"));
        processTable=new JTable(new ProcessTableModel());/*{
    	         public boolean editCellAt(int row, int column, java.util.EventObject e) {
    	        	 column=convertColumnIndexToModel(column);
    	        	 if(column==0||column==5){
    	        		 	if(isEditing()) {
    	        		 		getCellEditor().stopCellEditing();
    	        		 	}
    	            		if (e instanceof MouseEvent) {
    	            			JTable table = (JTable) e.getSource();
    	            			MouseEvent mEvent = ((MouseEvent) e);
    	            		
    	            			if( ((MouseEvent)e).getClickCount() == 1 && this.isRowSelected( row ) ){
    	            				return false;
    	            			}
		    	               if (mEvent.getClickCount() == 2) {
		    	            	   if(column==0){
		    	                      	int selectedRow=table.getSelectedRow();
		    	                      	 ArrayList<Object> rowData = tableModel.getRow(selectedRow);
		                           	     ObservableWorker worker=(ObservableWorker)rowData.get(13);
		                           	     InfoDisplayDialog crd=InfoDisplayDialog.getInfoDialog();
		    	                      	 List<String> oldData=worker.getLogList();
		    	                      	 for(String data:oldData){
		    	                      		crd.addLog(data);
		    	                      	 }
		    	                      	 worker.addLogObserver(crd);
		    	                      	 crd.addObserver(worker);
		    	                       }
		    	            	   else{
		    	            		   table.setRowSelectionInterval(selectedRow, selectedRow);
		    	            	   }
		    	                  return false;
		    	               } else if (!table.isRowSelected(row)) {
		    	                  return false;
		    	               } else {
		    	                  return super.editCellAt(row, column, e);
		    	              }
    	        	 }
    	            return false;
    	        	 }
    	        	 return super.editCellAt(row, column, e);
    	         }
    	   
        };*/
        
        processTable.setShowGrid(true);
        processTable.setPreferredScrollableViewportSize(new Dimension(100, 300));
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
			                ProcessDao proc=(ProcessDao) request.get(1);
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
        taskTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        taskTable.setFillsViewportHeight(true);
        taskTable.setAutoCreateRowSorter(true);
        taskTable.setRowHeight(26);
        taskTable.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader header = taskTable.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 26));
        imap = taskTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    amap = taskTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(taskTable.getSelectedRow() != -1){
	                	System.out.println("Delete Key Pressed.");
	                	TaskTableModel tableModel=(TaskTableModel) taskTable.getModel();
	                	int[] selectedRows = taskTable.getSelectedRows();
						ArrayList<Object> selectedRowsData = new ArrayList<Object>();
						for (int selectedRow : selectedRows) {
							ArrayList request=tableModel.getRow(selectedRow);
							selectedRowsData.add(request);
			                TaskDao task=(TaskDao) request.get(5);
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
	        	  long l=(Long)((ProcessDao)ar.get(1)).getId();
	        	  System.out.println(l);
	        	  try{
	        		  String taskPackage="com.sapient.punter.tasks.";
	        		  Object[] possibilities = {"EchoTask", "DBExportTask", "TestTask"};
	        		  String s = (String)JOptionPane.showInputDialog(
	        				  			  frame,
	        		                      "Choose the Task\n",
	        		                      "Select Task",
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
	    	        	  TaskDao task=new TaskDao();
	    	        	  task.setInputParams(inProp);
	    	        	  task.setOutputParams(outProp);
	    	        	  task.setName(s);
	    	        	  task.setClassName(taskPackage+s);
	    	        	  ProcessDao p=new ProcessDao();
	    	        	  p.setId(l);
	    	        	  task.setProcess(p);
	    	        	  StaticDaoFacade.createTask(task);
	    	        	  TaskTableModel model=(TaskTableModel) taskTable.getModel();
	    	        	  final ArrayList<Object> newRequest = new ArrayList<Object>();
	    	              	newRequest.add(task.getId());
	    	              	newRequest.add(task.getName());
	    	              	newRequest.add(task.getSequence());
	    	              	newRequest.add(task.getDescription());
	    	              	newRequest.add(task.getAuthor());
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
	        		  ProcessDao proc=new ProcessDao();
	        		  proc.setName("new Process");
	        		  List<String> inputParams = com.sapient.punter.tasks.Process.listInputParams();
	        		  Properties inProp=new Properties();
    	        	  for (String key : inputParams) {
    					System.err.println(key);
    					inProp.setProperty(key, "");
    	        	  }
    	        	  proc.setInputParams(inProp);
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
		timer=new Timer(1000,new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int row=runningProcessTable.getSelectedRow();
				if(row!=-1){
					System.out.println("Timer running");
					int rows=runningTaskTable.getRowCount();
					for(int r=0;r<rows;r++){
						final ArrayList<Object> newRequest = ((RunningTaskTableModel)runningTaskTable.getModel()).getRow(r);
						TaskHistory cr=(TaskHistory)newRequest.get(4);
						newRequest.set(0,""+ cr.getSequence());
						newRequest.set(2,""+ cr.getRunState());
						newRequest.set(3, cr.getLogs()!=null?cr.getLogs():"");
					}
					((RunningTaskTableModel)runningTaskTable.getModel()).refreshTable();
				}else{
					if(((RunningTaskTableModel)runningTaskTable.getModel()).getRowCount()>0)
					((RunningTaskTableModel)runningTaskTable.getModel()).clearTable();
				}
			}});
		timer.start();
		runProcessMenu = new JMenuItem("Run Process");
		runProcessMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  System.out.println("Running Process");
	        	  try{
	        		  if(processTable.getSelectedRow()!=-1){
	        			  final ProcessDao procDao=(ProcessDao) ((ProcessTableModel) processTable.getModel()).getRow(processTable.getSelectedRow()).get(1);
	        			  createProcess(procDao);
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
				if (processTable.getSelectedRowCount() > 1) {
					if (SwingUtilities.isRightMouseButton(e)) {
						selectedRow = -1;
						popupProcess.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				 else{
		        	  selectedRow = processTable.rowAtPoint(e.getPoint());
		        	  if (selectedRow != -1) {
		        		  if (SwingUtilities.isRightMouseButton(e)) {
		        			  processTable.setRowSelectionInterval(selectedRow, selectedRow);
		        			  selectedRow=processTable.convertRowIndexToModel(selectedRow);
		        			 // ArrayList<Object> row = tableModel.getRow(selectedRow);
		        			  /*Task t=(Task)row.get(10);
		                	  if(t!=null){
		                		ProcessState s=t.getState();
		        			  if(s==ProcessState.RUNNING){
		        				  startMenu.setEnabled(false); 
		        				  stopMenu.setEnabled(true); 
		        			  }
		        			  else{
		        				  startMenu.setEnabled(true);
		        				  stopMenu.setEnabled(false); 
		        			  }
		                	  }else{
		                		 startMenu.setEnabled(false); 
		       				 	 stopMenu.setEnabled(false); 
		                	  }
		                	   */
		        			  popupProcess.show(e.getComponent(), 
		                              e.getX(), e.getY());
		        		 }
		        	  }else {
		        		  popupProcess.show(e.getComponent(), 
	                              e.getX(), e.getY());
		        	  }
		        	  }
			}
	      });
        inputParamTable = new JTable(new ParamTableModel());
        inputParamTable.setShowGrid(true);
        inputParamTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
        inputParamTable.setFillsViewportHeight(true);
        
        outputParamTable = new JTable(new ParamTableModel());
        outputParamTable.setShowGrid(true);
        outputParamTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
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
        			TaskDao t=(TaskDao) taskTable.getModel().getValueAt(selectedRow, 5);
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
        JTabbedPane tabbedPane = new JTabbedPane();
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
                if(e.getValueIsAdjusting()){
                	System.err.println("Mouse is adjusting..");
                }else if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    System.out.println("Row " + selectedRow + " is now selected.");
                    try{
                    	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
	                    pthtmodel.clearTable();
	                    ArrayList ar = ((ProcessTableModel)processTable.getModel()).getRow(processTable.getSelectedRow());
	      	        	long l=(Long)((ProcessDao)ar.get(1)).getId();
	      	        	System.out.println("PID= "+l);
	      	        	//populate processHistory
	      	        	ProcessHistoryTableModel phtmodel=(ProcessHistoryTableModel) processHistoryTable.getModel();
	      	            List<ProcessHistory> phl = StaticDaoFacade.getSortedProcessHistoryListForProcessId(l);
	      	            phtmodel.clearTable();
		      	        for (ProcessHistory ph : phl) {
		      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
		      	          	newRequest.add(""+ph.getId()+"  [ "+sdf.format(ph.getStartTime())+" ]");
		      	          	newRequest.add(ph);
		      	          	phtmodel.insertRow(newRequest);
		      	        /*List<TaskHistory> thl = ph.getTaskHistoryList();
		      	        for(TaskHistory th:thl){
		      	        	System.out.println(th.getId());
		      	        }*/
	      	  		}
	      	        if(phtmodel.getRowCount()>0){
	      	        	processHistoryTable.setRowSelectionInterval(0, 0);
	      	        }
      	        	//populate task table
                    List<TaskDao> taskList=StaticDaoFacade.getProcessTasksById(l);
                    ProcessDao process = StaticDaoFacade.getProcess(l);
                    TaskTableModel model=(TaskTableModel) taskTable.getModel();
                    model.clearTable();
                    for(TaskDao t:taskList){
                    	final ArrayList<Object> newRequest = new ArrayList<Object>();
                    	newRequest.add(t.getId());
                    	newRequest.add(t.getName());
                    	newRequest.add(t.getSequence());
                    	newRequest.add(t.getDescription());
                    	newRequest.add(t.getAuthor());
                    	newRequest.add(t);
                    	newRequest.add(process);
                    	model.insertRow(newRequest);
                    }
                    if(taskTable.getModel().getRowCount()>0){
                    taskTable.setRowSelectionInterval(0, 0);
                    }else{
                    	inputParamTable.setModel(new ParamTableModel());
                    	outputParamTable.setModel(new ParamTableModel());
                    	initColumnSizes1(inputParamTable);
                    	initColumnSizes1(outputParamTable);
                    }
                    
                    //populate process properties
                    ProcessPropertyTableModel pptmodel=(ProcessPropertyTableModel) processPropertyTable.getModel();
                    pptmodel.clearTable();
                    Properties props = process.getInputParams();
                    for(Object key : props.keySet()) {  
                		Object value = props.get(key);    
                		System.out.println(key + " = " + value);
                		final ArrayList<Object> newRequest = new ArrayList<Object>();
                    	newRequest.add(key);
                    	newRequest.add(value);
                    	newRequest.add(process);
                    	pptmodel.insertRow(newRequest);
                    }
                    }catch (Exception ee) {
                    	ee.printStackTrace();
					}
                }
            }});
        JScrollPane listPane = new JScrollPane(processTable);
        jsp.setDividerSize(1);
        tabbedPane.addTab("Process Tasks", null, (jsp),"Tasks List for selected Process");
        processHistoryTable=new JTable(new ProcessHistoryTableModel());
        processHistoryTable.setShowGrid(true);
        processHistoryTable.setPreferredScrollableViewportSize(new Dimension(200, 300));
        processHistoryTable.setFillsViewportHeight(true);
        processHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTaskHistoryTable=new JTable(new ProcessTaskHistoryTableModel());
        processTaskHistoryTable.setShowGrid(true);
        processTaskHistoryTable.setPreferredScrollableViewportSize(new Dimension(300, 300));
        processTaskHistoryTable.setFillsViewportHeight(true);
        processTaskHistoryTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane processHistoryPane = new JScrollPane(processHistoryTable);
        JScrollPane processTaskHistoryPane = new JScrollPane(processTaskHistoryTable);
        JSplitPane jsp4=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,processHistoryPane,processTaskHistoryPane);
        jsp4.setDividerSize(0);
        tabbedPane.addTab("Process History", null, jsp4,"Tasks Run History for selected Process");
        initColumnSizes4(processTaskHistoryTable);
        
        processPropertyTable=new JTable(new ProcessPropertyTableModel());
        initColumnSizes5(processPropertyTable);
        processPropertyTable.setShowGrid(true);
        processPropertyTable.setPreferredScrollableViewportSize(new Dimension(400, 300));
        processPropertyTable.setFillsViewportHeight(true);
        processPropertyTable.getTableHeader().setReorderingAllowed(false);
        processPropertyTable.setIntercellSpacing(new Dimension(0,0));
        processPropertyTable.getColumn("<html><b>Property").setMaxWidth(200);
        processPropertyTable.getColumn("<html><b>Property").setPreferredWidth(150);
        JScrollPane processPropertyPane = new JScrollPane(processPropertyTable);
        tabbedPane.addTab("Process Property", null, processPropertyPane,"Properties for selected Process");
        
        JSplitPane jsp3=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,listPane,tabbedPane);
        jsp3.setDividerSize(1);
        JSplitPane jsp5=new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp3,splitRunningProcessPane);
        jsp5.setDividerSize(1);
        add(jsp5);
        
        ListSelectionModel PHTSelectionModel = processHistoryTable.getSelectionModel();
        PHTSelectionModel.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if(e.getValueIsAdjusting()){
                	System.err.println("Mouse is adjusting..");
                }
                if (lsm.isSelectionEmpty()) {
                	System.out.println("PTHL Empty --No Row is now selected.");
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    System.out.println("PTHL -- Row " + selectedRow + " is now selected.");
                    try{
                    ArrayList ar = ((ProcessHistoryTableModel)processHistoryTable.getModel()).getRow(processHistoryTable.getSelectedRow());
      	        	long l=(Long)((ProcessHistory)ar.get(1)).getId();
      	        	System.out.println("PHID= "+l);
      	        	ProcessHistory ph = StaticDaoFacade.getProcessHistoryById(l);
      	        	//populate ProcessTaskHistory
      	        	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
      	            List<TaskHistory> pthl = ph.getTaskHistoryList();
      	            pthtmodel.clearTable();
	      	          for (TaskHistory th : pthl) {
	      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
	      	          	newRequest.add(th.getSequence());
	      	          	newRequest.add(th.getTask().getName());
	      	          	newRequest.add(th.getRunState());
	      	          	newRequest.add(th.getLogs());
	      	          	pthtmodel.insertRow(newRequest);
	      	  		}
	      	        if(pthtmodel.getRowCount()>0){
	      	        	processTaskHistoryTable.setRowSelectionInterval(0, 0);
	      	          }
                    }catch (Exception ee) {
                    	ee.printStackTrace();
					}
                }
            }});
        ProcessTableModel model=(ProcessTableModel) processTable.getModel();
        List<ProcessDao> pl = StaticDaoFacade.getProcessList();
        for (ProcessDao p : pl) {
        	final ArrayList<Object> newRequest = new ArrayList<Object>();
        	newRequest.add(p.getName());
        	newRequest.add(p);
        	model.insertRow(newRequest);
		}
        if(processTable.getModel().getRowCount()>0)
            processTable.setRowSelectionInterval(0, 0);
         
        //create scheduled job picker
        ScheduledJobPicker sjp= new ScheduledJobPicker();
        sjp.setGuiReference(this);
    }
    public void createProcess(final ProcessDao procDao) throws Exception{
		  System.out.println(procDao.getId()+" == "+procDao.getName());
		  List<TaskDao> ptl = StaticDaoFacade.getSortedTasksByProcessId(procDao.getId());
		  
		  final List<TaskHistory> thList=new ArrayList<TaskHistory>(10);
		  final ProcessHistory ph=new ProcessHistory();
		  ph.setName("Test-1");
		  ph.setStartTime(new Date());
		  ph.setProcess(procDao);
		  ph.setTaskHistoryList(thList);
		  for (TaskDao taskDao : ptl) {
			System.out.println("Task -"+taskDao.getId());
			TaskHistory th = new TaskHistory();
			th.setTask(taskDao);
			th.setProcessHistory(ph);
			th.setSequence(taskDao.getSequence());
			thList.add(th);
		  	}
		  
		  Thread t=new Thread(){
			@Override
			public void run() {
			try{
    		  final ProcessHistory ph1 = StaticDaoFacade.createProcessHistory(ph);
    		  final ArrayList<Object> newRequest = new ArrayList<Object>();
  	          newRequest.add(""+ph1.getId()+"  [ "+sdf.format(ph1.getStartTime())+" ]");
  	          newRequest.add(ph1);
  	          javax.swing.SwingUtilities.invokeLater(new Runnable() {
  	        	public void run() {
            	try{
            		if(processTable.getSelectedRow()!=-1){
            		ProcessDao pd=(ProcessDao) ((ProcessTableModel)processTable.getModel()).getRow(processTable.getSelectedRow()).get(1);
            		if(pd.getId()==procDao.getId()){
            			((ProcessHistoryTableModel)processHistoryTable.getModel()).insertRowAtBeginning(newRequest);
            			processHistoryTable.setRowSelectionInterval(0, 0);
            		}
            		}
                    }catch (Exception e) {
                    	e.printStackTrace();
            		}
            	}
  	      	  });
  	          final com.sapient.punter.tasks.Process process=com.sapient.punter.tasks.Process.getProcess(procDao.getInputParams(),ph1);
    		  process.setTaskObservable(PunterGUI.this);
    		  // Adding row to running process table model
    		  final RunningProcessTableModel rptm=(RunningProcessTableModel) runningProcessTable.getModel();
    		  ArrayList<Object> newRequest1 = new ArrayList<Object>();
  	          newRequest1.add(procDao.getName());
  	          newRequest1.add("");
  	          newRequest1.add(ph1.getRunState());
  	          newRequest1.add(ph1.getStartTime());
  	          newRequest1.add(ph1);
  	          final ArrayList rptmRow = rptm.insertRow(newRequest1);
			  process.addObserver(new ProcessObserver() {
					 
					@Override
					public void update(ProcessHistory ph) {
//						System.err.println("updating table model");
						newRequest.set(0, ""+ph.getId()+"  [ "+sdf.format(ph.getStartTime())+" ]");
						((ProcessHistoryTableModel)processHistoryTable.getModel()).refreshTable();
						rptmRow.set(1, ph.getId());
						rptmRow.set(2, ph.getRunState());
						rptm.refreshTable();
					}

					@Override
					public void processCompleted() {
						rptm.deleteRow(rptmRow);
						if(rptm.getRowCount()>0&&runningProcessTable.getSelectedRow()==-1){
							runningProcessTable.setRowSelectionInterval(0, 0);
						}
//						rptm.refreshTable();
					}
				});
				runProcess(process);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}  
		  };
		  t.start();
    }
    public void runProcess(com.sapient.punter.tasks.Process process){
    	ProcessExecutor.getInstance().submitProcess(process);
    }
    
    @Override
	public void saveTaskHistory(final TaskHistory taskHistory) {
    	try {
    		System.err.println("Creating task history");
			StaticDaoFacade.saveTaskHistory(taskHistory);
			if(processHistoryTable.getSelectedRow()!=-1){
			 javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	try{
		            	ArrayList ar = ((ProcessHistoryTableModel)processHistoryTable.getModel()).getRow(processHistoryTable.getSelectedRow());
		            	long pidTable=(Long)((ProcessHistory)ar.get(1)).getId();
		            	long pid=taskHistory.getProcessHistory().getId();
		            	System.out.println(""+pid+" == "+pidTable);
		            	if(pid==pidTable){
		            		ProcessHistory ph = StaticDaoFacade.getProcessHistoryById(pid);
		      	        	//populate ProcessTaskHistory
		      	        	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
		      	            List<TaskHistory> pthl = ph.getTaskHistoryList();
		      	            pthtmodel.clearTable();
			      	          for (TaskHistory th : pthl) {
			      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
			      	          	newRequest.add(th.getSequence());
			      	          	newRequest.add(th.getTask().getName());
			      	          	newRequest.add(th.getRunState());
			      	          	newRequest.add(th.getLogs());
			      	          	newRequest.add(th);
			      	          	pthtmodel.insertRow(newRequest);
			      	  		}
			      	        if(pthtmodel.getRowCount()>0){
			      	        	processTaskHistoryTable.setRowSelectionInterval(0, 0);
			      	          }
		            	}
		            	}
		            catch (Exception e) {
		            	e.printStackTrace();
					}
		            }
		        });
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
    
    private void initColumnSizes5(JTable table) {
    	ProcessPropertyTableModel model = (ProcessPropertyTableModel)table.getModel();
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
    
    private void initColumnSizes4(JTable table) {
    	ProcessTaskHistoryTableModel model = (ProcessTaskHistoryTableModel)table.getModel();
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
