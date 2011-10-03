package com.sapient.punter.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.TrayIcon;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.PlainDocument;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.sapient.punter.jpa.*;
import neoe.ne.EditPanel;

import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.annotations.PunterTask;
import com.sapient.punter.executors.ProcessExecutor;
import com.sapient.punter.executors.ScheduledJobPicker;
import com.sapient.punter.gui.TextAreaEditor.EditorListener;
import com.sapient.punter.tasks.Tasks;
import com.sapient.punter.utils.ConsoleOutputStream;
import com.sapient.punter.utils.InputParamValue;
import com.sapient.punter.utils.OutputParamValue;
import com.sapient.punter.utils.TextAreaFIFO;

public class PunterGUI extends JPanel implements TaskObserver{
    private boolean DEBUG = false;
    private final JTable taskTable;
    private final JTable processPropertyTable;
    private TableRowSorter<ProcessTableModel> sorter;
    private TableSearchFilter tableSearchFilter;
    private final JTable processTable;
    private final JTable inputParamTable;
    private final JTable outputParamTable;
    private final JTable processHistoryTable;
    private final JTable processAlertTable;
    private final JTable processTaskAlertTable;
    private final JTable processTaskHistoryTable;
    private final JTable runningProcessTable;
    private final JTable runningTaskTable;
    private TextAreaFIFO appLogArea;
    private TextAreaFIFO procLogArea;
    private int selectedRow=-1;
    private final Timer timer;
    private DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
    private static Properties taskProps=new Properties();
	private JSplitPane jsp3;
	private JSplitPane jsp;
	private JSplitPane jsp6;
	private JTextArea jTextArea;
    static{
    	try {
			taskProps.load(PunterGUI.class.getClassLoader().getResourceAsStream("resources/tasks.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public PunterGUI() throws Exception {
        super(new GridLayout(1,0));
        tableSearchFilter=new TableSearchFilter();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        final RunningProcessTableModel runningProcessTableModel = new RunningProcessTableModel();
		runningProcessTable=new JTable(runningProcessTableModel);
        runningProcessTable.setShowGrid(false);
        runningProcessTable.setPreferredScrollableViewportSize(new Dimension(370, 160));
        runningProcessTable.setFillsViewportHeight(true);
        runningProcessTable.setAutoCreateRowSorter(true);
        runningProcessTable.setRowHeight(20);
        runningProcessTable.setIntercellSpacing(new Dimension(0, 0));
        runningProcessTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        runningProcessTable.setFont(new Font("Courier New",Font.TRUETYPE_FONT,12));
        JTableHeader header = runningProcessTable.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        if(AppSettings.getInstance().getObject("runningProcessTable")!=null)
			GUIUtils.initilializeTableColumns(runningProcessTable, (int[]) AppSettings.getInstance().getObject("runningProcessTable"));
        else
        	GUIUtils.initilializeTableColumns(runningProcessTable, runningProcessTableModel.width);
        runningProcessTable.getColumn("<html><b>Completed").setCellRenderer(new ProgressRenderer(runningProcessTable));
        runningProcessTable.addKeyListener(new java.awt.event.KeyAdapter(){
         public void keyTyped(KeyEvent e){}
          public void keyPressed(KeyEvent e)        
          {
            doKeyPressed(e);
          }
        });
        ListSelectionModel runningProcessTableSM = runningProcessTable.getSelectionModel();
        runningProcessTableSM.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        		if (lsm.isSelectionEmpty()) {
        		} else {
        			int selectedRow = lsm.getMinSelectionIndex();
        			//allowing sorting and all
        			selectedRow=runningProcessTable.convertRowIndexToModel(selectedRow);
        			ProcessHistory ph=(ProcessHistory) ((RunningProcessTableModel) runningProcessTable.getModel()).getRow(selectedRow).get(0);
        			if(!ph.getRunState().equals(RunState.NEW)){
        			List<TaskHistory> thList = ph.getTaskHistoryList();
        			procLogArea.setDocument(ph.getLogDocument());
        			procLogArea.setEditable(false);
        			((RunningTaskTableModel)runningTaskTable.getModel()).clearTable();
        			for (TaskHistory taskHistory : thList) {
        				final ArrayList<Object> newRequest = new ArrayList<Object>();
	      	          	newRequest.add(taskHistory);
	      	           ((RunningTaskTableModel)runningTaskTable.getModel()).insertRow(newRequest);
					}
        			}else{
        				procLogArea.setDocument(new PlainDocument());
        				((RunningTaskTableModel)runningTaskTable.getModel()).clearTable();
        			}
        		}
        	}
        });
        
        final RunningTaskTableModel runningTaskTableModel = new RunningTaskTableModel();
		runningTaskTable=new JTable(runningTaskTableModel){
	         public boolean editCellAt(int row, int column, java.util.EventObject e) {
	        	 column=convertColumnIndexToModel(column);
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
		            	   RunningTaskTableModel phtm = ((RunningTaskTableModel)runningTaskTable.getModel());
		            	   TaskHistory ph=(TaskHistory) phtm.getRow(runningTaskTable.convertRowIndexToModel(runningTaskTable.getSelectedRow())).get(0);
		            	   EditPanel editor;
		            	   if(ph.getLogs()!=null)
						   try {
								editor = new EditPanel(ph.getLogs());
								editor.openWindow();
						   } catch (Exception e1) {
								e1.printStackTrace();
						   }
		                  return false;
		               } else if (!table.isRowSelected(row)) {
		                  return false;
		               } else {
		                  return super.editCellAt(row, column, e);
		              }
	        		}
	        		return false;
//	    	 	return super.editCellAt(row, column, e);
	         }
        };
        runningTaskTable.setShowGrid(false);
        runningTaskTable.setPreferredScrollableViewportSize(new Dimension(300, 160));
        runningTaskTable.setFillsViewportHeight(true);
        runningTaskTable.setFont(new Font("Courier New",Font.TRUETYPE_FONT,12));
        runningTaskTable.setAutoCreateRowSorter(true);
        runningTaskTable.setShowVerticalLines(false);
        runningTaskTable.setRowHeight(26);
        runningTaskTable.setIntercellSpacing(new Dimension(0, 0));
        runningTaskTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        if(AppSettings.getInstance().getObject("runningTaskTable")!=null)
			GUIUtils.initilializeTableColumns(runningTaskTable, (int[]) AppSettings.getInstance().getObject("runningTaskTable"));
        else
        	GUIUtils.initilializeTableColumns(runningTaskTable, runningTaskTableModel.width);
        header = runningTaskTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        JTabbedPane jtb=new JTabbedPane();
        jtb.addTab("Tasks", new JScrollPane(runningTaskTable));
        procLogArea=new TextAreaFIFO();
        jtb.addTab("Logs", new JScrollPane(procLogArea));
        JSplitPane splitRunningProcessPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(runningProcessTable), jtb);
        splitRunningProcessPane.setDividerSize(0);
//        splitRunningProcessPane.setBorder(new TitledBorder("Process Explorer"));
        ProcessTableModel model=new ProcessTableModel();
        sorter = new TableRowSorter<ProcessTableModel>(model);
        processTable=new JTable(model);/*{
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
        processTable.setRowSorter(sorter);
        processTable.setShowGrid(true);
        processTable.setPreferredScrollableViewportSize(new Dimension(150, 300));
        processTable.setFillsViewportHeight(true);
        processTable.setRowHeight(22);
        processTable.setFont(new Font("Arial",Font.TRUETYPE_FONT,11));
        processTable.setForeground(Color.BLUE);
        processTable.setTableHeader(null);
        processTable.getColumn("<html><b>My Process's").setCellRenderer(new ProcessTableRenderer());
        InputMap imap = processTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    ActionMap amap = processTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(processTable.getSelectedRow() != -1){
					 int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Process('s) ?", "Confirm",
	 					        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 					   if (response == JOptionPane.YES_OPTION) {	 						   
//	                	System.out.println("Delete Key Pressed.");
 						   ProcessTableModel tableModel=(ProcessTableModel) processTable.getModel();
 						   int[] selectedRows = processTable.getSelectedRows();
 						   ArrayList<Object> selectedRowsData = new ArrayList<Object>();
 						   for (int selectedRow : selectedRows) {
 							   ArrayList<?> request=tableModel.getRow(processTable.convertRowIndexToModel(selectedRow));
 							   selectedRowsData.add(request);
 							   ProcessData proc=(ProcessData) request.get(0);
 							   try {
 								   System.err.println("removing process : "+proc.getId());
 								   StaticDaoFacade.getInstance().removeProcess(proc);
 							   } catch (Exception e1) {
 								   e1.printStackTrace();
 							   }
 						   }
 						   tableModel.deleteRows(selectedRowsData);
 						   if(tableModel.getRowCount()>0){
 							   processTable.setRowSelectionInterval(0, 0);
 						   }
 					   }
	                }
			}});
	    processTable.setDragEnabled(true);
	    processTable.setTransferHandler(new TransferHandler(){
	    	public boolean canImport(TransferHandler.TransferSupport support) {
          		 return checkImportPossible(support);
              }
	    	public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                System.err.println("Import possible");
                try{
                Transferable t = support.getTransferable();
                if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                {
             	   System.err.println("Import possible .. file");
                try {
                    java.util.List<File> l =(java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                    JAXBContext context = JAXBContext.newInstance(ProcessData.class);
      		      	Unmarshaller unmarshaller = context.createUnmarshaller();
                    for (File f : l) {
                    	System.err.println(f.getName());
                    	ProcessData procDao = (ProcessData)unmarshaller.unmarshal(new FileReader(f));
    		            procDao.setUsername(AppSettings.getInstance().getUsername());
    		            List<TaskData> tl = procDao.getTaskList();
    		            for (TaskData taskData : tl) {
							taskData.setProcess(procDao);
						}
    		            procDao=StaticDaoFacade.getInstance().createProcess(procDao);
    		            ArrayList<Object> newrow=new ArrayList<Object>();
    		            newrow.add(procDao);
    		            ((ProcessTableModel) processTable.getModel()).insertRow(newrow);
                    	System.err.println("Process added : "+procDao.getName());
                    }
                    return true;
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
               }
               else{
                	System.err.println("Data Flavors not supported yet :");
                	DataFlavor dfs[]=t.getTransferDataFlavors();
                	for(DataFlavor df:dfs){
                		System.err.println(df.getMimeType());
                	}
               }
               }catch(Exception e){
               		e.printStackTrace();
               }
               return false;
            }
	    	 public int getSourceActions(JComponent c) {
                return MOVE;
             }
	    	 protected Transferable createTransferable(JComponent c) {
	    		  final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor};
                  JTable table = (JTable)c;
                  int []selectedRows=table.getSelectedRows();
                  try{
                  JAXBContext context = JAXBContext.newInstance(ProcessData.class);
                  Marshaller marshaller = context.createMarshaller();
                  marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                  File temp=new File("Temp");
             	  temp.mkdir();
             	  temp.deleteOnExit();
             	  final List<File> files=new java.util.ArrayList<File>();
                  for (int i : selectedRows) {
                	  ProcessData procDao=(ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(i)).get(0);
        			  procDao=StaticDaoFacade.getInstance().getProcess(procDao.getId());
        			  File file = new File(temp,procDao.getName()+".xml");
	  		          FileWriter fw = new FileWriter(file);
	  		          marshaller.marshal(procDao,fw);
	  		          fw.close();
	  		          files.add(file);
                  }
                  if(files.size()>0){
 	                 Transferable transferable = new Transferable() {
                      public Object getTransferData(DataFlavor flavor) {
                          if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                              return files;
                          }
                          return null;
                        }
                      public DataFlavor[] getTransferDataFlavors() {
                        return flavors;
                      }
                      public boolean isDataFlavorSupported(
                          DataFlavor flavor) {
                        return flavor.equals(
                            DataFlavor.javaFileListFlavor);
                      }
                    };
                    return transferable;
                    }
                  }catch (Exception e) {
                	  e.printStackTrace();
				}
                return null;
	    	 }
	    	 public void exportDone(JComponent c, Transferable t, int action) {
            }
	    });
        final TaskTableModel taskTableModel = new TaskTableModel();
		taskTable = new JTable(taskTableModel);
        taskTable.setShowGrid(true);
        taskTable.setShowVerticalLines(false);
        taskTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        taskTable.setFillsViewportHeight(true);
        taskTable.setAutoCreateRowSorter(true);
        taskTable.setRowHeight(26);
        taskTable.setIntercellSpacing(new Dimension(0, 0));
        taskTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        header = taskTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }

        header.setPreferredSize(new Dimension(30, 26));
        TableColumn col2 = taskTable.getColumnModel().getColumn(0);
        col2.setCellRenderer(dtcr);
        imap = taskTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    amap = taskTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(taskTable.getSelectedRow() != -1){
//	                	System.out.println("Delete Key Pressed.");
	                	TaskTableModel tableModel=(TaskTableModel) taskTable.getModel();
	                	int[] selectedRows = taskTable.getSelectedRows();
						ArrayList<Object> selectedRowsData = new ArrayList<Object>();
						for (int selectedRow : selectedRows) {
							ArrayList<?> request=tableModel.getRow(taskTable.convertRowIndexToModel(selectedRow));
							selectedRowsData.add(request);
			                TaskData task=(TaskData) request.get(0);
							try {
								System.err.println("removing task : "+task.getId());
								StaticDaoFacade.getInstance().removeTask(task);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						tableModel.deleteRows(selectedRowsData);
	                }
			}});
	    
	    taskTable.setDragEnabled(true);
	    taskTable.setTransferHandler(new TransferHandler(){
	    	public boolean canImport(TransferHandler.TransferSupport support) {
         		 return checkImportPossible(support);
             }
	    	public boolean importData(TransferHandler.TransferSupport support) {
               if (!canImport(support)) {
                   return false;
               }
               System.err.println("Import possible");
               try{
               Transferable t = support.getTransferable();
               if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
               {
            	   System.err.println("Import possible .. file");
               try {
                   java.util.List<File> l =(java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                   JAXBContext context = JAXBContext.newInstance(TaskData.class);
     		      	Unmarshaller unmarshaller = context.createUnmarshaller();
                   for (File f : l) {
                   	System.err.println(f.getName());
                   	JAXBElement<TaskData> root = unmarshaller.unmarshal(new StreamSource(new FileReader(f)),TaskData.class);
                   	TaskData taskData=root.getValue();
                   	ProcessData procDao=(ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                   	taskData.setProcess(procDao);
                   	taskData=StaticDaoFacade.getInstance().createTask(taskData);
                   	if(procDao.getTaskList()==null){
                   		procDao.setTaskList(new ArrayList<TaskData>());
                   	}
                   	procDao.getTaskList().add(taskData);
                   	StaticDaoFacade.getInstance().saveProcess(procDao);
                   	TaskTableModel model=(TaskTableModel) taskTable.getModel();
  	        	    final ArrayList<Object> newRequest = new ArrayList<Object>();
  	              	newRequest.add(taskData);
  	              	model.insertRow(newRequest);
                   }
                   return true;
               } catch (UnsupportedFlavorException e) {
                   return false;
               } catch (IOException e) {
                   return false;
               }
              }
              else{
               	System.err.println("Data Flavors not supported yet :");
               	DataFlavor dfs[]=t.getTransferDataFlavors();
               	for(DataFlavor df:dfs){
               		System.err.println(df.getMimeType());
               	}
              }
              }catch(Exception e){
              		e.printStackTrace();
              }
              return false;
           }
	    	 public int getSourceActions(JComponent c) {
               return MOVE;
            }
	    	 protected Transferable createTransferable(JComponent c) {
	    		  final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor};
                 JTable table = (JTable)c;
                 int []selectedRows=table.getSelectedRows();
                 try{
                 JAXBContext context = JAXBContext.newInstance(TaskData.class);
                 Marshaller marshaller = context.createMarshaller();
                 marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                 File temp=new File("Temp");
            	 temp.mkdir();
            	 temp.deleteOnExit();
            	 final List<File> files=new java.util.ArrayList<File>();
                 for (int i : selectedRows) {
               	  TaskData procDao=(TaskData) ((TaskTableModel) taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(i)).get(0);
       			  File file = new File(temp,procDao.getDescription()+".xml");
	  		          FileWriter fw = new FileWriter(file);
	  		          marshaller.marshal(procDao,fw);
	  		          fw.close();
	  		          files.add(file);
                 }
                 if(files.size()>0){
	                 Transferable transferable = new Transferable() {
                     public Object getTransferData(DataFlavor flavor) {
                         if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                             return files;
                         }
                         return null;
                       }
                     public DataFlavor[] getTransferDataFlavors() {
                       return flavors;
                     }
                     public boolean isDataFlavorSupported(
                         DataFlavor flavor) {
                       return flavor.equals(
                           DataFlavor.javaFileListFlavor);
                     }
                   };
                   return transferable;
                   }
                 }catch (Exception e) {
               	  e.printStackTrace();
				}
               return null;
	    	 }
	    	 public void exportDone(JComponent c, Transferable t, int action) {
           }
	    });
        final JMenuItem addTaskMenu,taskDocsMenu;
		final JPopupMenu popupTask = new JPopupMenu();
		addTaskMenu = new JMenuItem("Add Task");
	    addTaskMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  if(processTable.getSelectedRow()!=-1){
	        	  ArrayList<?> ar = ((ProcessTableModel)processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow()));
	        	  long procId=(Long)((ProcessData)ar.get(0)).getId();
//	        	  System.out.println("Process ID : "+procId);
	        	  try{
	        		  Object[] possibilities =taskProps.keySet().toArray();
	        		  String s = (String)JOptionPane.showInputDialog(
	        				  			  Main.PunterGuiFrame,
	        		                      "Choose the Task\n",
	        		                      "Select Task",
	        		                      JOptionPane.PLAIN_MESSAGE,
	        		                      null,
	        		                      possibilities,
	        		                      possibilities[0]);

	        		  //If a string was returned, say so.
	        		  if ((s != null) && (s.length() > 0)) {
//	        		      System.out.println("Selected Task... " + s + "!");
	        		      Class<?> cls = Class.forName(taskProps.getProperty(s));
	    	        	  HashMap<String, OutputParamValue> outProp = Tasks.listOutputParams((Tasks)cls.newInstance());
	    	        	  HashMap<String, InputParamValue> inProp=Tasks.listInputParams((Tasks)cls.newInstance());
	    	        	  
	    	        	  TaskData task=new TaskData();
	    	        	  task.setInputParams(inProp);
	    	        	  task.setOutputParams(outProp);
	    	        	  task.setName(s);
	    	        	  task.setClassName(taskProps.getProperty(s));
	    	        	  ProcessData p=new ProcessData();
	    	        	  p.setId(procId);
	    	        	  task.setProcess(p);
	    	        	  task=StaticDaoFacade.getInstance().createTask(task);
	    	        	  TaskTableModel model=(TaskTableModel) taskTable.getModel();
	    	        	  final ArrayList<Object> newRequest = new ArrayList<Object>();
	    	              	newRequest.add(task);
	    	              	model.insertRow(newRequest);
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	        	 }
	          }
	    });
	    popupTask.add(addTaskMenu);
	    taskDocsMenu = new JMenuItem("Task Docs");
	    taskDocsMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(taskTable.getSelectedRow()!=-1){
					 try{
					 TaskData td=(TaskData) ((TaskTableModel)taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(taskTable.getSelectedRow())).get(0);
					 Class<?> cls = Class.forName(td.getClassName());
					 Tasks t=(Tasks) cls.newInstance();
					 PunterTask ann = t.getClass().getAnnotation(PunterTask.class);
					 System.err.println("url="+ann.documentation());
					 DocumentationDialog.displayHelp(ann.documentation(), false, Main.PunterGuiFrame);
					 }catch (Exception ee) {
						 ee.printStackTrace();
					}
				 }
			}
		});
	    popupTask.add(taskDocsMenu);
	    taskTable.addMouseListener(new MouseAdapter() {
	          //JPopupMenu popup;
	          public void mousePressed(MouseEvent e) {
	        	  int selRow = taskTable.rowAtPoint(e.getPoint());
//	        	  System.out.println(selRow);
	        	  if(selRow!=-1){
	        		  taskTable.setRowSelectionInterval(selRow, selRow);
	        		  int selRowInModel = taskTable.convertRowIndexToModel(selRow);
	        	  }
	      		  if (SwingUtilities.isRightMouseButton(e)) {
	      			  if(taskTable.getSelectedRow()==-1||selRow==-1){
	      				taskDocsMenu.setEnabled(false);
	      			  }else{
	      				taskDocsMenu.setEnabled(true);
	      			  }
	      			  popupTask.show(e.getComponent(), e.getX(), e.getY());
	      		  	}
	          }
	      });
	    
	    final JMenuItem addProcessMenu, runProcessMenu, exportProcess, importProcess;
		final JPopupMenu popupProcess = new JPopupMenu();
		addProcessMenu = new JMenuItem("Add Process");
		addProcessMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
//	        	  System.out.println("Adding new Process");
	        	  try{
	        		  ProcessData proc=new ProcessData();
	        		  proc.setName("new Process");
	        		  HashMap<String, InputParamValue> inProp = com.sapient.punter.tasks.Process.listInputParams();
    	        	  proc.setInputParams(inProp);
    	        	  proc.setUsername(AppSettings.getInstance().getUsername());
    	        	  proc=StaticDaoFacade.getInstance().createProcess(proc);
	        		  ProcessTableModel model=(ProcessTableModel) processTable.getModel();
	        		  final ArrayList<Object> newRequest = new ArrayList<Object>();
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
//					System.out.println("Timer running");
					int rows=runningTaskTable.getRowCount();
					for(int r=0;r<rows;r++){
						final ArrayList<?> newRequest = ((RunningTaskTableModel)runningTaskTable.getModel()).getRow(runningTaskTable.convertRowIndexToModel(r));
						/*TaskHistory cr=(TaskHistory)newRequest.get(0);
						newRequest.set(0,""+ cr.getSequence());
						newRequest.set(2,""+ cr.getRunState());
						newRequest.set(3, cr.getLogs()!=null?cr.getLogs():"");*/
					}
					((RunningTaskTableModel)runningTaskTable.getModel()).refreshTable();
				}else{
					if(((RunningTaskTableModel)runningTaskTable.getModel()).getRowCount()>0){
					((RunningTaskTableModel)runningTaskTable.getModel()).clearTable();
					procLogArea.setDocument(new PlainDocument());
					}
				}
			}});
		timer.start();
		importProcess = new JMenuItem("Imp Process");
		importProcess.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  try{
	        		  if(processTable.getSelectedRow()!=-1){
	        			  JAXBContext context = JAXBContext.newInstance(ProcessData.class);
	        		      Unmarshaller unmarshaller = context.createUnmarshaller();
	        		      final JFileChooser fc = new JFileChooser();
	        		      fc.setDialogType(JFileChooser.OPEN_DIALOG);
	        		      int returnVal = fc.showOpenDialog(PunterGUI.this);
	        		      if (returnVal == JFileChooser.APPROVE_OPTION) {
	        		            File file = fc.getSelectedFile();
	        		            ProcessData procDao = (ProcessData)unmarshaller.unmarshal(new FileReader(file));
	        		            procDao.setUsername(AppSettings.getInstance().getUsername());
	        		            List<TaskData> tl = procDao.getTaskList();
	        		            for (TaskData taskData : tl) {
									taskData.setProcess(procDao);
								}
	        		            procDao=StaticDaoFacade.getInstance().createProcess(procDao);
	        		            ArrayList<Object> newrow=new ArrayList<Object>();
	        		            newrow.add(procDao);
	        		            ((ProcessTableModel) processTable.getModel()).insertRow(newrow);
	        		        } else {
//	        		            log.append("Open command cancelled by user." + newline);
	        		        }
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
		exportProcess = new JMenuItem("Exp Process");
		exportProcess.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  try{
	        		  if(processTable.getSelectedRow()!=-1){
	        			  ProcessData procDao=(ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
	        			  procDao=StaticDaoFacade.getInstance().getProcess(procDao.getId());
	        			  JAXBContext context = JAXBContext.newInstance(ProcessData.class);
	        		      Marshaller marshaller = context.createMarshaller();
	        		      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        		      
	        		      final JFileChooser fc = new JFileChooser();
	        		      fc.setDialogType(JFileChooser.SAVE_DIALOG);
	        		      fc.setSelectedFile(new File(procDao.getName()+".xml"));
	        		      int returnVal = fc.showSaveDialog(PunterGUI.this);
	        		      if (returnVal == JFileChooser.APPROVE_OPTION) {
	        		            File file = fc.getSelectedFile();
	        		            FileWriter fw = new FileWriter(file);
	        		            marshaller.marshal(procDao,fw);
	        		            fw.close();
	        		            System.out.println("File saved to :"+file.getAbsolutePath());
	        		        } else {
//	        		            log.append("Open command cancelled by user." + newline);
	        		        }
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
		
		runProcessMenu = new JMenuItem("Run Process");
		runProcessMenu.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
//	        	  System.out.println("Running Process");
	        	  try{
	        		  if(processTable.getSelectedRow()!=-1){
	        			  ProcessData procDao=(ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
	        			  procDao=StaticDaoFacade.getInstance().getProcess(procDao.getId());
	        			  createProcess(procDao);
	        		  }
	        	  }catch(Exception ee){
	        		  ee.printStackTrace();
	        	  }
	          }
	    });
		popupProcess.add(addProcessMenu);
		popupProcess.add(runProcessMenu);
		popupProcess.add(exportProcess);
		popupProcess.add(importProcess);
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
        final InputParamTableModel inputParamTableModel = new InputParamTableModel();
		inputParamTable = new JTable(inputParamTableModel){

	         public boolean editCellAt(final int row,final int column, java.util.EventObject e) {
//	        	 column=convertColumnIndexToModel(column);
	    		 	if(isEditing()) {
	    		 		getCellEditor().stopCellEditing();
	    		 	}
	        		if (e instanceof MouseEvent) {
	        			JTable table = (JTable) e.getSource();
	        			MouseEvent mEvent = ((MouseEvent) e);
	        		
	        			if( ((MouseEvent)e).getClickCount() == 1 && this.isRowSelected( row ) ){
	        				return false;
	        			}
		               if (mEvent.getClickCount() == 2&&column==1) {
		            	   final InputParamTableModel iptm = ((InputParamTableModel)inputParamTable.getModel());
		            	   String value= (String) iptm.getValueAt(row, column);
		            	   TextAreaEditor.getInstance(value, new EditorListener() {
							@Override
							public void save(String text) {
								iptm.setValueAt(text, row, column);
							}
		            	   },Main.PunterGuiFrame);
		                  return false;
		               } else if (!table.isRowSelected(row)) {
		                  return false;
		               } else {
		                  return super.editCellAt(row, column, e);
		              }
	        		}
	        		return false;
//	    	 	return super.editCellAt(row, column, e);
	         }
       };
        inputParamTable.setShowGrid(true);
        inputParamTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
        inputParamTable.setFillsViewportHeight(true);
        inputParamTable.getColumn("<html><b>Value").setCellRenderer(new DefaultStringRenderer());
        inputParamTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        outputParamTable = new JTable(new OutputParamTableModel());
        outputParamTable.setShowGrid(true);
        outputParamTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
        outputParamTable.setFillsViewportHeight(true);
        outputParamTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        initColumnSizesOutputParamTable();
        ListSelectionModel rowSM = taskTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent e) {
        		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        		if (lsm.isSelectionEmpty()) {
        		} else {
        			AppSettings.getInstance().setObject("outputParamTable", GUIUtils.getColumnWidth(outputParamTable));
        			AppSettings.getInstance().setObject("inputParamTable", GUIUtils.getColumnWidth(inputParamTable));
        			int selectedRow = lsm.getMinSelectionIndex();
//        			System.out.println("Row " + selectedRow + " is now selected.");
        			TaskData t=(TaskData)((TaskTableModel) taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(selectedRow)).get(0);
        			if(t.getInputParams()!=null){
        			inputParamTable.setModel(new InputParamTableModel(t));
        			inputParamTable.getColumn("<html><b>Value").setCellRenderer(new DefaultStringRenderer());
        			initColumnSizesInputParamTable();
        			}else{        				
        				inputParamTable.setModel(new InputParamTableModel());
        				inputParamTable.getColumn("<html><b>Value").setCellRenderer(new DefaultStringRenderer());
        				initColumnSizesInputParamTable();
        			}
        			if(t.getOutputParams()!=null){
        			outputParamTable.setModel(new OutputParamTableModel(t));
        			initColumnSizesOutputParamTable();
        			}else{
        				outputParamTable.setModel(new OutputParamTableModel());
        				initColumnSizesOutputParamTable();
        			}
        		}
        	}
        });
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(taskTable);
        JTabbedPane tabbedPane = new JTabbedPane();
        //Set up column sizes.
        if(AppSettings.getInstance().getObject("taskTable")!=null)
			GUIUtils.initilializeTableColumns(taskTable, (int[]) AppSettings.getInstance().getObject("taskTable"));
        else
        	GUIUtils.initilializeTableColumns(taskTable, taskTableModel.width);
        if(AppSettings.getInstance().getObject("processTable")!=null)
			GUIUtils.initilializeTableColumns(processTable, (int[]) AppSettings.getInstance().getObject("processTable"));
        else
        	GUIUtils.initilializeTableColumns(processTable, model.width);
        initColumnSizesInputParamTable();
//        setUpSportColumn(taskTable, taskTable.getColumnModel().getColumn(2));
        JSplitPane jsp2=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(inputParamTable),new JScrollPane(outputParamTable));
        jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane,jsp2);
        
        jsp.setDividerLocation(0.4);
        if(AppSettings.getInstance().getObject("jspLocation")!=null)
        	jsp.setDividerLocation(((Integer)AppSettings.getInstance().getObject("jspLocation")));
        //Add the scroll pane to this panel.
        processTable.setAutoscrolls(true);
        ListSelectionModel listSelectionModel = processTable.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if(e.getValueIsAdjusting()){
//                	System.err.println("Mouse is adjusting..");
                } else if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    try{
                    	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
	                    pthtmodel.clearTable();
	                    ArrayList<?> ar = ((ProcessTableModel)processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow()));
	      	        	long procId=(Long)((ProcessData)ar.get(0)).getId();
//	      	        	System.out.println("PID= "+procId);
	      	        	//populate processHistory
	      	        	ProcessHistoryTableModel phtmodel=(ProcessHistoryTableModel) processHistoryTable.getModel();
	      	            List<ProcessHistory> phl = StaticDaoFacade.getInstance().getSortedProcessHistoryListForProcessId(procId);
	      	            phtmodel.clearTable();
		      	        for (ProcessHistory ph : phl) {
		      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
		      	          	newRequest.add(ph);
		      	          	phtmodel.insertRow(newRequest);
	      	  		}
	      	        if(phtmodel.getRowCount()>0){
	      	        	processHistoryTable.setRowSelectionInterval(0, 0);
	      	        }
      	        	//populate task table
                    List<TaskData> taskList=StaticDaoFacade.getInstance().getProcessTasksById(procId);
                    ProcessData process = StaticDaoFacade.getInstance().getProcess(procId);
                    TaskTableModel model=(TaskTableModel) taskTable.getModel();
                    model.clearTable();
                    AppSettings.getInstance().setObject("inputParamTable", GUIUtils.getColumnWidth(inputParamTable));
                    AppSettings.getInstance().setObject("outputParamTable", GUIUtils.getColumnWidth(outputParamTable));
                    for(TaskData task:taskList){
                    	final ArrayList<Object> newRequest = new ArrayList<Object>();
                    	newRequest.add(task);
                    	model.insertRow(newRequest);
                    }
                    if(taskTable.getModel().getRowCount()>0){
                    	taskTable.setRowSelectionInterval(0, 0);
                    }else{
                    	inputParamTable.setModel(new InputParamTableModel());
                    	outputParamTable.setModel(new OutputParamTableModel());
                    	initColumnSizesInputParamTable();
                    	initColumnSizesOutputParamTable();
                    }
                    
                    //populate process properties
                    ProcessPropertyTableModel pptmodel=(ProcessPropertyTableModel) processPropertyTable.getModel();
                    pptmodel.clearTable();
                    HashMap<String, InputParamValue> props = process.getInputParams();
                    for(Object key : props.keySet()) {  
                		InputParamValue value = props.get(key);    
//                		System.out.println(key + " = " + value.getValue());
                		final ArrayList<Object> newRequest = new ArrayList<Object>();
                    	newRequest.add(key);
                    	newRequest.add(value.getValue());
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
        processHistoryTable.setRowHeight(22);
        processHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        header = processHistoryTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        processHistoryTable.getColumn("<html><b>Run ID").setCellRenderer(new ProcessHistoryTableRenderer());
        
        final ProcessTaskHistoryTableModel processTaskHistoryTableModel = new ProcessTaskHistoryTableModel();
		processTaskHistoryTable=new JTable(processTaskHistoryTableModel){
	         public boolean editCellAt(int row, int column, java.util.EventObject e) {
	        	 column=convertColumnIndexToModel(column);
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
		            	   ProcessTaskHistoryTableModel phtm = ((ProcessTaskHistoryTableModel)processTaskHistoryTable.getModel());
		            	   TaskHistory ph=(TaskHistory) phtm.getRow(processTaskHistoryTable.convertRowIndexToModel(processTaskHistoryTable.getSelectedRow())).get(0);
		            	   EditPanel editor;
		            	   if(ph.getLogs()!=null)
						   try {
								editor = new EditPanel(ph.getLogs());
								editor.openWindow();
						   } catch (Exception e1) {
								e1.printStackTrace();
						   }
		                  return false;
		               } else if (!table.isRowSelected(row)) {
		                  return false;
		               } else {
		                  return super.editCellAt(row, column, e);
		              }
	        		}
	        		return false;
//	    	 	return super.editCellAt(row, column, e);
	         }
        };
        processTaskHistoryTable.setShowGrid(true);
        processTaskHistoryTable.setRowHeight(26);
        processTaskHistoryTable.setShowVerticalLines(false);
        processTaskHistoryTable.setPreferredScrollableViewportSize(new Dimension(300, 300));
        processTaskHistoryTable.setFillsViewportHeight(true);
        processTaskHistoryTable.setAutoCreateRowSorter(true);
        processTaskHistoryTable.getTableHeader().setReorderingAllowed(false);
        processTaskHistoryTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        JScrollPane processHistoryPane = new JScrollPane(processHistoryTable);
        JScrollPane processTaskHistoryPane = new JScrollPane(processTaskHistoryTable);
        JSplitPane jsp4=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,processHistoryPane,processTaskHistoryPane);
        jsp4.setDividerSize(0);
        tabbedPane.addTab("Process History", null, jsp4,"Tasks Run History for selected Process");
        if(AppSettings.getInstance().getObject("processTaskHistoryTable")!=null)
			GUIUtils.initilializeTableColumns(processTaskHistoryTable, (int[]) AppSettings.getInstance().getObject("processTaskHistoryTable"));
        else
        	GUIUtils.initilializeTableColumns(processTaskHistoryTable, processTaskHistoryTableModel.width);
        Runtime.getRuntime().addShutdownHook(new Thread(){
        	@Override
        	public void run() {
        		super.run();
        		//saving states of all the Tables.
        		System.out.println("Divider Location :"+jsp3.getDividerLocation()+" - "+jsp3.getDividerSize());
        		AppSettings.getInstance().setObject("processTaskAlertTable", GUIUtils.getColumnWidth(processTaskAlertTable));
        		AppSettings.getInstance().setObject("runningProcessTable", GUIUtils.getColumnWidth(runningProcessTable));
        		AppSettings.getInstance().setObject("processTaskHistoryTable", GUIUtils.getColumnWidth(processTaskHistoryTable));
        		AppSettings.getInstance().setObject("runningTaskTable", GUIUtils.getColumnWidth(runningTaskTable));
        		AppSettings.getInstance().setObject("taskTable", GUIUtils.getColumnWidth(taskTable));
        		AppSettings.getInstance().setObject("processTable", GUIUtils.getColumnWidth(processTable));
        		AppSettings.getInstance().setObject("outputParamTable", GUIUtils.getColumnWidth(outputParamTable));
        		AppSettings.getInstance().setObject("inputParamTable", GUIUtils.getColumnWidth(inputParamTable));
        		AppSettings.getInstance().setObject("processPropertyTable", GUIUtils.getColumnWidth(processPropertyTable));
        		AppSettings.getInstance().setObject("jsp3Location", jsp3.getDividerLocation());
        		AppSettings.getInstance().setObject("jsp6Location", jsp6.getDividerLocation());
        		AppSettings.getInstance().setObject("jspLocation", jsp.getDividerLocation());
				AppSettings.getInstance().setObject("appProperties", jTextArea.getText());
        	}
        });
        final ProcessPropertyTableModel processPropertyTableModel = new ProcessPropertyTableModel();
		processPropertyTable=new JTable(processPropertyTableModel);
        processPropertyTable.setShowGrid(true);
        processPropertyTable.setPreferredScrollableViewportSize(new Dimension(400, 300));
        processPropertyTable.setFillsViewportHeight(true);
        processPropertyTable.getTableHeader().setReorderingAllowed(false);
        processPropertyTable.setIntercellSpacing(new Dimension(0,0));
        processPropertyTable.getColumn("<html><b>Property").setMaxWidth(200);
        processPropertyTable.getColumn("<html><b>Property").setPreferredWidth(150);
        processPropertyTable.getColumn("<html><b>Value").setCellRenderer(new ProcessPropertyTableRenderer());
        if(AppSettings.getInstance().getObject("processPropertyTable")!=null)
			GUIUtils.initilializeTableColumns(processPropertyTable, (int[]) AppSettings.getInstance().getObject("processPropertyTable"));
        else
        	GUIUtils.initilializeTableColumns(processPropertyTable, processPropertyTableModel.width);
        JScrollPane processPropertyPane = new JScrollPane(processPropertyTable);
        tabbedPane.addTab("Process Property", null, processPropertyPane,"Properties for selected Process");
        
        // processAlertTable
        processAlertTable=new JTable(new ProcessAlertTableModel());
//        initColumnSizes5(processPropertyTable);
        
        processAlertTable.setShowGrid(true);
        processAlertTable.setShowVerticalLines(false);
        processAlertTable.setPreferredScrollableViewportSize(new Dimension(350, 200));
        processAlertTable.setFillsViewportHeight(true);
        processAlertTable.setAutoCreateRowSorter(true);
        processAlertTable.setRowHeight(26);
        processAlertTable.setIntercellSpacing(new Dimension(0, 0));
        processAlertTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        processAlertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        header = processAlertTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        processAlertTable.getTableHeader().setReorderingAllowed(false);
        processAlertTable.getColumn("<html><b>Run ID").setPreferredWidth(250);
        processAlertTable.getColumn("<html><b>Clear Alert").setPreferredWidth(100);
        processAlertTable.getColumn("<html><b>Run ID").setCellRenderer(new ProcessAlertTableRenderer());
        JScrollPane processAlertPane = new JScrollPane(processAlertTable);
        
        processTaskAlertTable=new JTable(new ProcessTaskHistoryTableModel()){

	         public boolean editCellAt(int row, int column, java.util.EventObject e) {
	        	 column=convertColumnIndexToModel(column);
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
		            	   ProcessTaskHistoryTableModel phtm = ((ProcessTaskHistoryTableModel)processTaskAlertTable.getModel());
		            	   TaskHistory ph=(TaskHistory) phtm.getRow(processTaskAlertTable.convertRowIndexToModel(processTaskAlertTable.getSelectedRow())).get(0);
		            	   EditPanel editor;
		            	   if(ph.getLogs()!=null)
						   try {
								editor = new EditPanel(ph.getLogs());
								editor.openWindow();
						   } catch (Exception e1) {
								e1.printStackTrace();
						   }
		                  return false;
		               } else if (!table.isRowSelected(row)) {
		                  return false;
		               } else {
		                  return super.editCellAt(row, column, e);
		              }
	        		}
	        		return false;
//	    	 	return super.editCellAt(row, column, e);
	         }
        };
        processTaskAlertTable.setShowGrid(true);
        processTaskAlertTable.setShowVerticalLines(false);
        processTaskAlertTable.setRowHeight(26);
        processTaskAlertTable.setPreferredScrollableViewportSize(new Dimension(350, 300));
        processTaskAlertTable.setFillsViewportHeight(true);
        processTaskAlertTable.setAutoCreateRowSorter(true);
        processTaskAlertTable.getTableHeader().setReorderingAllowed(false);
        processTaskAlertTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        if(AppSettings.getInstance().getObject("processTaskAlertTable")!=null)
			GUIUtils.initilializeTableColumns(processTaskAlertTable, (int[]) AppSettings.getInstance().getObject("processTaskAlertTable"));
        JScrollPane processTaskALertPane = new JScrollPane(processTaskAlertTable);
        jsp6 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,processAlertPane,processTaskALertPane);
        jsp6.setDividerSize(1);
        if(AppSettings.getInstance().getObject("jsp6Location")!=null)
        	jsp6.setDividerLocation(((Integer)AppSettings.getInstance().getObject("jsp6Location")));
        tabbedPane.addTab("My Alerts", null, jsp6,"My Workflow Alerts");
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();
				int sel = pane.getSelectedIndex();
				if (sel == 3) {
					((ProcessTaskHistoryTableModel) processTaskAlertTable.getModel()).clearTable();
					ProcessAlertTableModel phtmodel = (ProcessAlertTableModel) processAlertTable.getModel();
					List<ProcessHistory> phl = StaticDaoFacade.getInstance().getMySortedProcessHistoryList(
							AppSettings.getInstance().getUsername());
					phtmodel.clearTable();
					for (ProcessHistory ph : phl) {
						final ArrayList<Object> newRequest = new ArrayList<Object>();
						newRequest.add(ph);
						phtmodel.insertRow(newRequest);
					}
					if (phtmodel.getRowCount() > 0) {
						processAlertTable.setRowSelectionInterval(0, 0);
					}
				} else if (sel == 5) {
					// System.out.println("Selected 5");
					Object props = AppSettings.getInstance().getObject("appProperties");
					if (props != null)
						jTextArea.setText((String) props);
				} else {
					// leave the prop tab
					String string = jTextArea.getText();
					Properties properties = new Properties();
					try {
						properties.load(new ByteArrayInputStream(string.getBytes()));
						AppSettings.getInstance().setSessionMap((Map) properties);
						AppSettings.getInstance().setObject("appProperties", string);
						// System.err.println("Properties Loaded to the System.");
					} catch (IOException e) {
						System.err.println("Error Loading properties into the system.");
						e.printStackTrace();
					}
				}
			}
		});
        ListSelectionModel PATSelectionModel = processAlertTable.getSelectionModel();
        PATSelectionModel.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if(e.getValueIsAdjusting()){
//                	System.err.println("Mouse is adjusting..");
                }
                else if (lsm.isSelectionEmpty()) {
//                	System.out.println("PTHL Empty --No Row is now selected.");
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
//                    System.out.println("PTHL -- Row " + selectedRow + " is now selected.");
                    try{
                    ArrayList<?> ar = ((ProcessAlertTableModel)processAlertTable.getModel()).getRow(processAlertTable.convertRowIndexToModel(processAlertTable.getSelectedRow()));
      	        	long phId=(Long)((ProcessHistory)ar.get(0)).getId();
      	        	ProcessHistory ph = StaticDaoFacade.getInstance().getProcessHistoryById(phId);
      	        	//populate ProcessTaskHistory
      	        	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskAlertTable.getModel();
      	            List<TaskHistory> pthl = ph.getTaskHistoryList();
      	            Collections.sort(pthl, new TaskHistorySeqComparator());
      	            pthtmodel.clearTable();
	      	          for (TaskHistory th : pthl) {
	      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
	      	          	newRequest.add(th);
	      	          	pthtmodel.insertRow(newRequest);
	      	  		}
	      	        if(pthtmodel.getRowCount()>0){
	      	        	processTaskAlertTable.setRowSelectionInterval(0, 0);
	      	          }
                    }catch (Exception ee) {
                    	ee.printStackTrace();
					}
                }
            }});
        appLogArea=new TextAreaFIFO();
        appLogArea.setEditable(false);
        tabbedPane.addTab("App Logs", null, new JScrollPane(appLogArea),"Application Wide Logging");
		jTextArea = new JTextArea();
		jTextArea.setText((String) AppSettings.getInstance().getObject("appProperties"));
		tabbedPane.addTab("App Props", null, new JScrollPane(jTextArea), "Application Wide Properties");
        JPanel panel=new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        final JTextField searchTextField=new JTextField("");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        panel.add(searchTextField, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty=1.0;
        c.gridx = 0;
        c.gridy = 1;
        panel.add(listPane,c);
        
		searchTextField.getDocument().addDocumentListener(getDocumentListener(searchTextField));
        jsp3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panel,tabbedPane);
        jsp3.setDividerSize(1);
        if(AppSettings.getInstance().getObject("jsp3Location")!=null)
        	jsp3.setDividerLocation(((Integer)AppSettings.getInstance().getObject("jsp3Location")));
        JSplitPane jsp5=new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp3,splitRunningProcessPane);
        jsp5.setDividerSize(1);
        add(jsp5);
        
        ListSelectionModel PHTSelectionModel = processHistoryTable.getSelectionModel();
        PHTSelectionModel.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if(e.getValueIsAdjusting()){
//                	System.err.println("Mouse is adjusting..");
                }
                else if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    try{
                    ArrayList<?> ar = ((ProcessHistoryTableModel)processHistoryTable.getModel()).getRow(processHistoryTable.convertRowIndexToModel(processHistoryTable.getSelectedRow()));
      	        	long phId=(Long)((ProcessHistory)ar.get(0)).getId();
      	        	ProcessHistory ph = StaticDaoFacade.getInstance().getProcessHistoryById(phId);
      	        	//populate ProcessTaskHistory
      	        	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
      	            List<TaskHistory> pthl = ph.getTaskHistoryList();
      	            Collections.sort(pthl, new TaskHistorySeqComparator());
      	            pthtmodel.clearTable();
	      	          for (TaskHistory th : pthl) {
	      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
	      	          	newRequest.add(th);
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
        ProcessTableModel tmpModel=(ProcessTableModel) processTable.getModel();
        List<ProcessData> pl = StaticDaoFacade.getInstance().getProcessList(AppSettings.getInstance().getUsername());
        for (ProcessData p : pl) {
        	final ArrayList<Object> newRequest = new ArrayList<Object>();
        	newRequest.add(p);
        	tmpModel.insertRow(newRequest);
		}
        if(processTable.getModel().getRowCount()>0)
            processTable.setRowSelectionInterval(0, 0);
         
        //create scheduled job picker
        ScheduledJobPicker sjp= new ScheduledJobPicker();
        sjp.setGuiReference(this);
        
        setErrAndOutStreamToLogDocument();
    }

	private void setErrAndOutStreamToLogDocument() {
		System.setOut( new PrintStream(new ConsoleOutputStream (appLogArea.getDocument(), System.out), true));
		System.setErr( new PrintStream(new ConsoleOutputStream (appLogArea.getDocument(), System.err), true));
	}

	private DocumentListener getDocumentListener(final JTextField searchTextField) {
		return new DocumentListener() {
		    public void changedUpdate(DocumentEvent e) {
		    	tableSearchFilter.applyFilter(sorter, searchTextField.getText());
		    }
		    public void insertUpdate(DocumentEvent e) {
		    	tableSearchFilter.applyFilter(sorter, searchTextField.getText());
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	tableSearchFilter.applyFilter(sorter, searchTextField.getText());
		    }
		};
	}
    
    public void createProcess(final ProcessData processData) throws Exception{
        final ProcessHistory processHistory = ProcessHistoryBuilder.build(processData);
		Thread thread=new Thread(){
			@Override
			public void run() {
			try{
    		  final ProcessHistory ph1 = StaticDaoFacade.getInstance().createProcessHistory(processHistory);
    		  final ArrayList<Object> newRequest = new ArrayList<Object>();
  	          newRequest.add(ph1);
				
  	          javax.swing.SwingUtilities.invokeLater(new Runnable() {
  	        	public void run() {
            	try{
            		if(processTable.getSelectedRow()!=-1){
            		ProcessData pd=(ProcessData) ((ProcessTableModel)processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
            		if(pd.getId()==processData.getId()){
            			((ProcessHistoryTableModel)processHistoryTable.getModel()).insertRowAtBeginning(newRequest);
            			processHistoryTable.setRowSelectionInterval(0, 0);
            		}
            		}
            		final com.sapient.punter.tasks.Process process=com.sapient.punter.tasks.Process.getProcess(processData.getInputParams(),ph1);
            		process.setTaskObservable(PunterGUI.this);
            		// Adding row to running process table model
            		final RunningProcessTableModel rptm=(RunningProcessTableModel) runningProcessTable.getModel();
            		ArrayList<Object> newRequest1 = new ArrayList<Object>();
            		newRequest1.add(ph1);
            		rptm.insertRowAtBeginning(newRequest1);
            		process.addObserver(new ProcessObserver() {
            			@Override
            			public void update(ProcessHistory ph) {
            				((ProcessHistoryTableModel)processHistoryTable.getModel()).refreshTable();
            				rptm.refreshTable();
            			}
            			@Override
            			public void processCompleted() {
            				if(rptm.getRowCount()>0&&runningProcessTable.getSelectedRow()==-1){
            					runningProcessTable.setRowSelectionInterval(0, 0);
            				}
            				if(ph1.getRunStatus().equals(RunStatus.SUCCESS))
            					Main.displayMsg(""+ph1.getName()+" Success",TrayIcon.MessageType.INFO);
            				else
            					Main.displayMsg(""+ph1.getName()+" Failed",TrayIcon.MessageType.WARNING);
            			}
            		});
            		runProcess(process);
                    }catch (Exception e) {
                    	e.printStackTrace();
            		}
            	}
  	      	  });
			}catch (Exception e) {
				e.printStackTrace();
			}
			}  
		  };
		  thread.start();
    }
    public void runProcess(com.sapient.punter.tasks.Process process){
    	ProcessExecutor.getInstance().submitProcess(process);
    }
    
    @Override
	public void saveTaskHistory(final TaskHistory taskHistory) {
    	try {
    		StaticDaoFacade.getInstance().saveTaskHistory(taskHistory);
			if(processHistoryTable.getSelectedRow()!=-1){
			 javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	try{
		            	ArrayList<?> ar = ((ProcessHistoryTableModel)processHistoryTable.getModel()).getRow(processHistoryTable.convertRowIndexToModel(processHistoryTable.getSelectedRow()));
		            	long pidTable=(Long)((ProcessHistory)ar.get(0)).getId();
		            	long pid=taskHistory.getProcessHistory().getId();
		            	if(pid==pidTable){
		            		ProcessHistory ph = StaticDaoFacade.getInstance().getProcessHistoryById(pid);
		      	        	//populate ProcessTaskHistory
		      	        	ProcessTaskHistoryTableModel pthtmodel=(ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
		      	            List<TaskHistory> pthl = ph.getTaskHistoryList();
		      	            pthtmodel.clearTable();
			      	          for (TaskHistory th : pthl) {
			      	          	final ArrayList<Object> newRequest = new ArrayList<Object>();
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
    
    private void doKeyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_DELETE:
                if(runningProcessTable.getSelectedRow() != -1){
                		ArrayList row=((RunningProcessTableModel)runningProcessTable.getModel()).getRow(runningProcessTable.convertRowIndexToModel(runningProcessTable.getSelectedRow()));
                		if(((ProcessHistory)row.get(0)).getRunState().equals(RunState.COMPLETED))
                		((RunningProcessTableModel)runningProcessTable.getModel()).deleteRow(runningProcessTable.getSelectedRow());
                    break;
                }
            default:
                break;
        }
    }
    
    
    private void initColumnSizesOutputParamTable() {
    	if(AppSettings.getInstance().getObject("outputParamTable")!=null)
			GUIUtils.initilializeTableColumns(outputParamTable, (int[]) AppSettings.getInstance().getObject("outputParamTable"));
        else
        	GUIUtils.initilializeTableColumns(outputParamTable, OutputParamTableModel.width);
    }
    
    private void initColumnSizesInputParamTable() {
    	if(AppSettings.getInstance().getObject("inputParamTable")!=null)
			GUIUtils.initilializeTableColumns(inputParamTable, (int[]) AppSettings.getInstance().getObject("inputParamTable"));
        else
        	GUIUtils.initilializeTableColumns(inputParamTable, InputParamTableModel.width);
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

    private boolean checkImportPossible(TransferHandler.TransferSupport support) {
		try{
		   if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		   
		   }else{
		  	 DataFlavor []dfs=support.getDataFlavors();
		  	 System.out.println("\n\nTotal MimeTypes Supported by this operation :"+dfs.length);
		  	 for(DataFlavor df:dfs){
		  		 System.out.println(df.getMimeType());
		  	 }
		  	 return false;
		   }
		   boolean copySupported = (TransferHandler.COPY & support.getSourceDropActions()) == TransferHandler.COPY;
		   if (!copySupported) {
		       return false;
		   }
		   support.setDropAction(TransferHandler.COPY);
		   return true;
		 }catch(Exception e){
			 e.printStackTrace();
			 return false;
		 }
	}

	static class DefaultStringRenderer extends DefaultTableCellRenderer {
    	
    	public DefaultStringRenderer() { super(); }
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus, int row,
    			int column) {
    		TaskData td=(TaskData) ((InputParamTableModel)table.getModel()).getValueAt(row, 2);
    		InputParamValue value1 = (InputParamValue) td.getInputParams().get(table.getModel().getValueAt(row, 0));
    		String tooltip="";
    		if(value1.getInputParam()!=null)
    		tooltip=value1.getInputParam().description();
    		if(!tooltip.isEmpty())
    			setToolTipText(tooltip);
    		else
    			setToolTipText(null);
    		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    				row, column);
    	}
    	public void setValue(Object value) {
    	    setText((value.toString().isEmpty()) ? "---" : value.toString());
    	}
    }
    
    static class ProcessPropertyTableRenderer extends DefaultTableCellRenderer {
    	
    	public ProcessPropertyTableRenderer() { super(); }
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus, int row,
    			int column) {
    		ProcessData td=(ProcessData)((ProcessPropertyTableModel)table.getModel()).getValueAt(row, 2);
    		InputParamValue value1 = (InputParamValue) td.getInputParams().get(table.getModel().getValueAt(row, 0));
    		String tooltip="";
    		if(value1.getInputParam()!=null)
    		tooltip=value1.getInputParam().description();
    		if(!tooltip.isEmpty())
    			setToolTipText(tooltip);
    		else
    			setToolTipText(null);
    		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    	}
    	public void setValue(Object value) {
    	    setText((value.toString().isEmpty()) ? "---" : value.toString());
    	}
    }
	static class ProcessHistoryTableRenderer extends DefaultTableCellRenderer {
		private static final Color successColor = new Color(51, 153, 51);
		private static final Color failureColor = new Color(153, 51, 0);
	
	public ProcessHistoryTableRenderer() { super(); }
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		ProcessHistory ph = (ProcessHistory) ((ProcessHistoryTableModel)table.getModel()).getRow(table.convertRowIndexToModel(row)).get(0);
		if(ph.getRunStatus().equals(RunStatus.FAILURE)){
			setBackground(failureColor);
			setForeground(Color.WHITE);
		}else if(ph.getRunStatus().equals(RunStatus.SUCCESS)){
			setBackground(successColor);
			setForeground(Color.WHITE);
		}else if(ph.getRunStatus().equals(RunStatus.NOT_RUN)){
			setBackground(Color.GRAY);
			setForeground(Color.BLACK);
		}else{
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		}
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
	public void setValue(Object value) {
	    setText((value.toString().isEmpty()) ? "---" : value.toString());
	}
    }
	static class ProcessAlertTableRenderer extends DefaultTableCellRenderer {
		private static final Color successColor = new Color(51, 153, 51);
		private static final Color failureColor = new Color(153, 51, 0);
	
	public ProcessAlertTableRenderer() { super(); }
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		ProcessHistory ph = (ProcessHistory) ((ProcessAlertTableModel)table.getModel()).getRow(table.convertRowIndexToModel(row)).get(0);
		if(ph.getRunStatus().equals(RunStatus.FAILURE)){
			setBackground(failureColor);
			setForeground(Color.WHITE);
		}else if(ph.getRunStatus().equals(RunStatus.SUCCESS)){
			setBackground(successColor);
			setForeground(Color.WHITE);
		}else if(ph.getRunStatus().equals(RunStatus.NOT_RUN)){
			setBackground(Color.GRAY);
			setForeground(Color.BLACK);
		}else{
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		}
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
	public void setValue(Object value) {
	    setText((value.toString().isEmpty()) ? "---" : value.toString());
	}
    }
	static class ProgressRenderer extends JProgressBar implements TableCellRenderer {
		private JTable theTable;
        public ProgressRenderer(JTable theTable) {
            super();
            this.theTable=theTable;
            this.setStringPainted(true);
            //  this.setIndeterminate(true);
        }

        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value, 
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) {
            int val=Integer.parseInt(value.toString());
            this.setValue(val);
            return this;
        }

        public boolean isDisplayable() {
            // This does the trick. It makes sure animation is always performed 
            return true;
        }

        public void repaint() {
            // If you have access to the table you can force repaint like this. 
            //Otherwize, you could trigger repaint in a timer at some interval 
           try{
         //   theTable.repaint();
           }catch(Exception e){
               System.out.println("1111");
           }
        }
    }
	static class ProcessTableRenderer extends DefaultTableCellRenderer {
		private static final Color successColor = new Color(51, 153, 51);
		private static final Color failureColor = new Color(153, 51, 0);
	
	public ProcessTableRenderer() { super(); }
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if(value!=null){
			if(value.toString().toLowerCase().contains("prod")){
				setForeground(Color.red);
			}
			else
				setForeground(Color.blue);
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
	public void setValue(Object value) {
	    setText((value.toString().isEmpty()) ? "---" : value.toString());
	}
    }
}