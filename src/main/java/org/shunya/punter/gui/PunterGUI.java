package org.shunya.punter.gui;

import ch.qos.logback.classic.Level;
import org.shunya.punter.annotations.PunterTask;
import org.shunya.punter.executors.ProcessExecutor;
import org.shunya.punter.executors.PunterJobScheduler;
import org.shunya.punter.jpa.*;
import org.shunya.punter.tasks.Tasks;
import org.shunya.punter.utils.*;
import org.shunya.server.PunterProcessRunMessage;
import org.shunya.server.component.DBService;
import org.shunya.server.component.PunterService;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;
import static org.shunya.punter.utils.FieldPropertiesMap.parseStringMap;

public class PunterGUI extends JPanel implements TaskObserver, Observer {
    public static final String WIN_A = "WIN_A";
    public static final String WIN_1 = "CTRL_ALT_1";
    public static final String WIN_2 = "CTRL_ALT_2";
    public static final String WIN_3 = "CTRL_ALT_3";
    public static final String WIN_4 = "CTRL_ALT_4";
    public static final String WIN_5 = "CTRL_ALT_5";
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
    private int selectedRow = -1;
    private final Timer timer;
    private static Properties taskProps = new Properties();
    private JSplitPane jsp3;
    private JSplitPane jsp;
    private JSplitPane jsp6;
    private JTextArea jTextArea;
    private PunterJobScheduler punterJobScheduler;
    private PunterComponent clipBoardListener;
    private boolean schedulerRunning = false;
    private PunterJobBasket punterJobBasket;
    private GlobalHotKeyListener globalHotKeyListener;
    private final DBService dbService;
    private final PunterService punterService;

    static {
        try {
            taskProps.load(PunterGUI.class.getClassLoader().getResourceAsStream("resources/tasks.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PunterGUI(final DBService dbService, PunterService punterService) throws Exception {
        super(new GridLayout(1, 0));
        this.dbService = dbService;
        this.punterService = punterService;
        clipBoardListener = new ClipBoardListener(dbService, punterService);
        dbService.setClipBoardListener((ClipBoardListener) clipBoardListener);
        tableSearchFilter = new TableSearchFilter();
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        final RunningProcessTableModel runningProcessTableModel = new RunningProcessTableModel();
        runningProcessTable = new JTable(runningProcessTableModel);
        runningProcessTable.setShowGrid(false);
        runningProcessTable.setPreferredScrollableViewportSize(new Dimension(370, 160));
        runningProcessTable.setFillsViewportHeight(true);
        runningProcessTable.setAutoCreateRowSorter(true);
        runningProcessTable.setRowHeight(20);
        runningProcessTable.setIntercellSpacing(new Dimension(0, 0));
        runningProcessTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        runningProcessTable.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 12));
        JTableHeader header = runningProcessTable.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        if (AppSettings.getInstance().getObject("runningProcessTable") != null)
            GUIUtils.initilializeTableColumns(runningProcessTable, (List) AppSettings.getInstance().getObject("runningProcessTable"));
        else
            GUIUtils.initilializeTableColumns(runningProcessTable, (List) runningProcessTableModel.width);
        runningProcessTable.getColumn("<html><b>Completed").setCellRenderer(new ProgressRenderer(runningProcessTable));
        runningProcessTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                doOnKeyPressed(e);
            }
        });
        ListSelectionModel runningProcessTableSM = runningProcessTable.getSelectionModel();
        runningProcessTableSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int firstSelectedRow = lsm.getMinSelectionIndex();
                    //allowing sorting and all
                    firstSelectedRow = runningProcessTable.convertRowIndexToModel(firstSelectedRow);
                    ProcessHistory ph = (ProcessHistory) ((RunningProcessTableModel) runningProcessTable.getModel()).getRow(firstSelectedRow).get(0);
                    if (!ph.getRunState().equals(RunState.NEW)) {
                        List<TaskHistory> thList = ph.getTaskHistoryList();
                        ((RunningTaskTableModel) runningTaskTable.getModel()).clearTable();
                        for (TaskHistory taskHistory : thList) {
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(taskHistory);
                            ((RunningTaskTableModel) runningTaskTable.getModel()).insertRow(newRequest);
                        }
                    } else {
                        ((RunningTaskTableModel) runningTaskTable.getModel()).clearTable();
                    }
                }
            }
        });

        final RunningTaskTableModel runningTaskTableModel = new RunningTaskTableModel();
        runningTaskTable = new JTable(runningTaskTableModel) {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                column = convertColumnIndexToModel(column);
                if (isEditing()) {
                    getCellEditor().stopCellEditing();
                }
                if (e instanceof MouseEvent) {
                    JTable table = (JTable) e.getSource();
                    MouseEvent mEvent = ((MouseEvent) e);

                    if (((MouseEvent) e).getClickCount() == 1 && this.isRowSelected(row)) {
                        return false;
                    }
                    if (mEvent.getClickCount() == 2 && runningTaskTable.getSelectedRow() != -1) {
                        RunningTaskTableModel phtm = ((RunningTaskTableModel) runningTaskTable.getModel());
                        TaskHistory taskHistory = (TaskHistory) phtm.getRow(runningTaskTable.convertRowIndexToModel(runningTaskTable.getSelectedRow())).get(0);
                        if (taskHistory.getLogs() != null) {
                            //Task has completed
                            try {
                                LogWindow logWindow = new LogWindow(10000, taskHistory.getTask().getName());
                                logWindow.log(taskHistory.getLogs(), Level.ALL);
                                logWindow.showLog();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            final Tasks task = taskHistory.getTasks();
                            if (task != null && task.getHosts() != null && !task.getHosts().isEmpty()) {
                                try {
                                    LogWindow logWindow = new LogWindow(10000, taskHistory.getTask().getName());
                                    logWindow.log(task.getRemoteLog(taskHistory.getId()), Level.ALL);
                                    logWindow.showLog();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else if (task != null) {
                                //Task is currently running
                                task.showLog();
                            }
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
        runningTaskTable.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 12));
        runningTaskTable.setAutoCreateRowSorter(true);
        runningTaskTable.setShowVerticalLines(false);
        runningTaskTable.setRowHeight(26);
        runningTaskTable.setIntercellSpacing(new Dimension(0, 0));
        runningTaskTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        runningTaskTable.getColumn("<html><b>Completed").setCellRenderer(new ProgressRenderer(runningTaskTable));
        if (AppSettings.getInstance().getObject("runningTaskTable") != null)
            GUIUtils.initilializeTableColumns(runningTaskTable, (List) AppSettings.getInstance().getObject("runningTaskTable"));
        else
            GUIUtils.initilializeTableColumns(runningTaskTable, runningTaskTableModel.width);
        header = runningTaskTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        JTabbedPane jtb = new JTabbedPane();
        jtb.addTab("Tasks", new JScrollPane(runningTaskTable));
        JSplitPane splitRunningProcessPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(runningProcessTable), jtb);
        splitRunningProcessPane.setDividerSize(1);
//        splitRunningProcessPane.setBorder(new TitledBorder("Process Explorer"));
        ProcessTableModel model = new ProcessTableModel(dbService);
        sorter = new TableRowSorter<ProcessTableModel>(model);
        processTable = new JTable(model);/*{
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
        processTable.setPreferredScrollableViewportSize(new Dimension(250, 300));
        processTable.setFillsViewportHeight(true);
        processTable.setRowHeight(25);
        processTable.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 11));
        processTable.setForeground(Color.BLUE);
        processTable.setTableHeader(null);
        processTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        processTable.getColumn("<html><b>My Process's").setCellRenderer(new ProcessTableRenderer());
        InputMap imap = processTable.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
        ActionMap amap = processTable.getActionMap();
        amap.put("table.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processTable.getSelectedRow() != -1) {
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Process('s) ?", "Confirm",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
//	                	System.out.println("Delete Key Pressed.");
                        ProcessTableModel tableModel = (ProcessTableModel) processTable.getModel();
                        int[] selectedRows = processTable.getSelectedRows();
                        ArrayList<Object> selectedRowsData = new ArrayList<Object>();
                        for (int selectedRow : selectedRows) {
                            ArrayList<?> request = tableModel.getRow(processTable.convertRowIndexToModel(selectedRow));
                            selectedRowsData.add(request);
                            ProcessData proc = (ProcessData) request.get(0);
                            try {
                                System.err.println("removing process : " + proc.getId());
                                dbService.removeProcess(proc);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        tableModel.deleteRows(selectedRowsData);
                        if (processTable.getRowCount() > 0) {
                            processTable.setRowSelectionInterval(0, 0);
                        }
                    }
                }
            }
        });
        processTable.setDragEnabled(true);
        processTable.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                return checkImportPossible(support);
            }

            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                System.err.println("Import possible");
                try {
                    Transferable t = support.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        System.err.println("Import possible .. file");
                        try {
                            java.util.List<File> l = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                            JAXBContext context = JAXBContext.newInstance(ProcessData.class);
                            Unmarshaller unmarshaller = context.createUnmarshaller();
                            for (File f : l) {
                                System.err.println(f.getName());
                                ProcessData processData = (ProcessData) unmarshaller.unmarshal(new FileReader(f));
                                processData.setUsername(AppSettings.getInstance().getUsername());
                                List<TaskData> tl = processData.getTaskList();
                                for (TaskData taskData : tl) {
                                    taskData.setProcess(processData);
                                }
                                processData = dbService.create(processData);
                                ArrayList<Object> newrow = new ArrayList<Object>();
                                newrow.add(processData);
                                ((ProcessTableModel) processTable.getModel()).insertRow(newrow);
                                System.err.println("Process added : " + processData.getName());
                            }
                            return true;
                        } catch (UnsupportedFlavorException e) {
                            return false;
                        } catch (IOException e) {
                            return false;
                        }
                    } else {
                        System.err.println("Data Flavors not supported yet :");
                        DataFlavor dfs[] = t.getTransferDataFlavors();
                        for (DataFlavor df : dfs) {
                            System.err.println(df.getMimeType());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor};
                JTable table = (JTable) c;
                int[] selectedRows = table.getSelectedRows();
                try {
                    JAXBContext context = JAXBContext.newInstance(ProcessData.class);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    File temp = new File("Temp");
                    temp.mkdir();
                    temp.deleteOnExit();
                    final List<File> files = new java.util.ArrayList<File>();
                    for (int i : selectedRows) {
                        ProcessData procDao = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(i)).get(0);
                        procDao = dbService.getProcess(procDao.getId());
                        File file = new File(temp, procDao.getName() + ".xml");
                        FileWriter fw = new FileWriter(file);
                        marshaller.marshal(procDao, fw);
                        fw.close();
                        files.add(file);
                    }
                    if (files.size() > 0) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            public void exportDone(JComponent c, Transferable t, int action) {
            }
        });
        final TaskTableModel taskTableModel = new TaskTableModel(dbService);
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
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }

        header.setPreferredSize(new Dimension(30, 26));
        taskTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);
        imap = taskTable.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
        amap = taskTable.getActionMap();
        amap.put("table.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (taskTable.getSelectedRow() != -1) {
//	                	System.out.println("Delete Key Pressed.");
                    TaskTableModel tableModel = (TaskTableModel) taskTable.getModel();
                    int[] selectedRows = taskTable.getSelectedRows();
                    ArrayList<Object> selectedRowsData = new ArrayList<>();
                    for (int selectedRow : selectedRows) {
                        ArrayList<?> request = tableModel.getRow(taskTable.convertRowIndexToModel(selectedRow));
                        TaskData task = (TaskData) request.get(0);
                        try {
                            System.err.println("removing task : " + task.getId());
                            dbService.removeTask(task);
                            selectedRowsData.add(request);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    tableModel.deleteRows(selectedRowsData);
                }
            }
        });

        taskTable.setDragEnabled(true);
        taskTable.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                return checkImportPossible(support);
            }

            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                System.err.println("Import possible");
                try {
                    Transferable t = support.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        System.err.println("Import possible .. file");
                        try {
                            java.util.List<File> l = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                            JAXBContext context = JAXBContext.newInstance(TaskData.class);
                            Unmarshaller unmarshaller = context.createUnmarshaller();
                            for (File f : l) {
                                System.err.println(f.getName());
                                JAXBElement<TaskData> root = unmarshaller.unmarshal(new StreamSource(new FileReader(f)), TaskData.class);
                                TaskData taskData = root.getValue();
                                ProcessData procDao = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                                taskData.setProcess(procDao);
                                taskData = dbService.create(taskData);
                                if (procDao.getTaskList() == null) {
                                    procDao.setTaskList(new ArrayList<TaskData>());
                                }
                                procDao.getTaskList().add(taskData);
                                dbService.saveProcess(procDao);
                                TaskTableModel model = (TaskTableModel) taskTable.getModel();
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
                    } else {
                        System.err.println("Data Flavors not supported yet :");
                        DataFlavor dfs[] = t.getTransferDataFlavors();
                        for (DataFlavor df : dfs) {
                            System.err.println(df.getMimeType());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor};
                JTable table = (JTable) c;
                int[] selectedRows = table.getSelectedRows();
                try {
                    JAXBContext context = JAXBContext.newInstance(TaskData.class);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    File temp = new File("Temp");
                    temp.mkdir();
                    temp.deleteOnExit();
                    final List<File> files = new java.util.ArrayList<File>();
                    for (int i : selectedRows) {
                        TaskData procDao = (TaskData) ((TaskTableModel) taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(i)).get(0);
                        File file = new File(temp, procDao.getDescription() + ".xml");
                        FileWriter fw = new FileWriter(file);
                        marshaller.marshal(procDao, fw);
                        fw.close();
                        files.add(file);
                    }
                    if (files.size() > 0) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            public void exportDone(JComponent c, Transferable t, int action) {
            }
        });
        final JMenuItem addTaskMenu, taskDocsMenu;
        final JPopupMenu popupTask = new JPopupMenu();
        addTaskMenu = new JMenuItem("Add Task");
        addTaskMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (processTable.getSelectedRow() != -1) {
                    ArrayList<?> ar = ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow()));
                    long procId = (Long) ((ProcessData) ar.get(0)).getId();
//	        	  System.out.println("Process ID : "+procId);
                    try {
                        Object[] possibilities = taskProps.keySet().toArray();
                        String s = (String) JOptionPane.showInputDialog(
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
                            FieldPropertiesMap outProp = Tasks.listOutputParams((Tasks) cls.newInstance());
                            FieldPropertiesMap inProp = Tasks.listInputParams((Tasks) cls.newInstance());

                            TaskData task = new TaskData();
                            task.setInputParamsAsObject(inProp);
                            task.setOutputParamsAsObject(outProp);
                            task.setName(s);
                            task.setFailOver(false);
                            task.setClassName(taskProps.getProperty(s));
                            ProcessData p = new ProcessData();
                            p.setId(procId);
                            task.setProcess(p);
                            task = dbService.create(task);
                            TaskTableModel model = (TaskTableModel) taskTable.getModel();
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(task);
                            model.insertRow(newRequest);
                        }
                    } catch (Exception ee) {
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
                if (taskTable.getSelectedRow() != -1) {
                    try {
                        TaskData td = (TaskData) ((TaskTableModel) taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(taskTable.getSelectedRow())).get(0);
                        Class<?> cls = Class.forName(td.getClassName());
                        Tasks t = (Tasks) cls.newInstance();
                        PunterTask ann = t.getClass().getAnnotation(PunterTask.class);
                        System.err.println("url=" + ann.documentation());
                        DocumentationDialog.displayHelp(ann.documentation(), false, Main.PunterGuiFrame);
                    } catch (Exception ee) {
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
                if (selRow != -1) {
                    taskTable.setRowSelectionInterval(selRow, selRow);
                    int selRowInModel = taskTable.convertRowIndexToModel(selRow);
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (taskTable.getSelectedRow() == -1 || selRow == -1) {
                        taskDocsMenu.setEnabled(false);
                    } else {
                        taskDocsMenu.setEnabled(true);
                    }
                    popupTask.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        final JMenuItem addProcessMenu, runProcessMenu, exportProcess, importProcess, processURL;
        final JPopupMenu popupProcess = new JPopupMenu();
        addProcessMenu = new JMenuItem("Add Process");
        addProcessMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//	        	  System.out.println("Adding new Process");
                try {
                    ProcessData proc = new ProcessData();
                    proc.setName("new Process");
                    FieldPropertiesMap inProp = org.shunya.punter.tasks.Process.listInputParams();
                    proc.setInputParams(inProp);
                    proc.setUsername(AppSettings.getInstance().getUsername());
                    proc = dbService.create(proc);
                    ProcessTableModel model = (ProcessTableModel) processTable.getModel();
                    final ArrayList<Object> newRequest = new ArrayList<Object>();
                    newRequest.add(proc);
                    model.insertRow(newRequest);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });
        timer = new Timer(1000, e -> {
            int row = runningProcessTable.getSelectedRow();
            if (row != -1) {
                ((RunningTaskTableModel) runningTaskTable.getModel()).refreshTable();
            } else {
                if (runningTaskTable.getModel().getRowCount() > 0) {
                    ((RunningTaskTableModel) runningTaskTable.getModel()).clearTable();
                }
            }
        });
        timer.start();
        importProcess = new JMenuItem("Imp Process");
        importProcess.addActionListener(e -> {
            try {
                if (processTable.getSelectedRow() != -1) {
                    JAXBContext context = JAXBContext.newInstance(ProcessData.class);
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    final JFileChooser fc = new JFileChooser();
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    int returnVal = fc.showOpenDialog(PunterGUI.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        ProcessData procDao = (ProcessData) unmarshaller.unmarshal(new FileReader(file));
                        procDao.setUsername(AppSettings.getInstance().getUsername());
                        List<TaskData> tl = procDao.getTaskList();
                        for (TaskData taskData : tl) {
                            taskData.setProcess(procDao);
                        }
                        procDao = dbService.create(procDao);
                        ArrayList<Object> newrow = new ArrayList<Object>();
                        newrow.add(procDao);
                        ((ProcessTableModel) processTable.getModel()).insertRow(newrow);
                    } else {
//	        		            log.append("Open command cancelled by user." + newline);
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });
        exportProcess = new JMenuItem("Exp Process");
        exportProcess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (processTable.getSelectedRow() != -1) {
                        ProcessData procDao = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                        procDao = dbService.getProcess(procDao.getId());
                        JAXBContext context = JAXBContext.newInstance(ProcessData.class);
                        Marshaller marshaller = context.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                        final JFileChooser fc = new JFileChooser();
                        fc.setDialogType(JFileChooser.SAVE_DIALOG);
                        fc.setSelectedFile(new File(procDao.getName() + ".xml"));
                        int returnVal = fc.showSaveDialog(PunterGUI.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            FileWriter fw = new FileWriter(file);
                            marshaller.marshal(procDao, fw);
                            fw.close();
                            System.out.println("File saved to :" + file.getAbsolutePath());
                        } else {
//	        		            log.append("Open command cancelled by user." + newline);
                        }
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });


        processURL = new JMenuItem("Copy URL");
        processURL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//	        	  System.out.println("Running Process");
                try {
                    if (processTable.getSelectedRow() != -1) {
                        ProcessData procDao = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                        String procId = "" + procDao.getId();
                        String server = dbService.getServerHostAddress().getHostName();
                        String port = "" + dbService.getWebServerPort();
                        String client = dbService.getLocalHostAddress().getHostName();
                        String url = "http://" + server + ":" + port + "/process/" + client + "/" + procId;
                        StringSelection stringSelection = new StringSelection(url);
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        toolkit.getSystemClipboard().setContents(stringSelection, null);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });

        runProcessMenu = new JMenuItem("Run Process");
        runProcessMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//	        	  System.out.println("Running Process");
                try {
                    if (processTable.getSelectedRow() != -1) {
                        ProcessData procDao = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                        PunterProcessRunMessage punterProcessRunMessage = new PunterProcessRunMessage();
                        punterProcessRunMessage.setProcessId(procDao.getId());
                        punterJobBasket.addJobToBasket(punterProcessRunMessage);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });
        popupProcess.add(addProcessMenu);
        popupProcess.add(runProcessMenu);
        popupProcess.add(exportProcess);
        popupProcess.add(importProcess);
        popupProcess.add(processURL);
        processTable.addMouseListener(new MouseAdapter() {
            //JPopupMenu popup;
            public void mousePressed(MouseEvent e) {
                if (processTable.getSelectedRowCount() > 1) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        selectedRow = -1;
                        popupProcess.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    selectedRow = processTable.rowAtPoint(e.getPoint());
                    if (selectedRow != -1) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            processTable.setRowSelectionInterval(selectedRow, selectedRow);
                            selectedRow = processTable.convertRowIndexToModel(selectedRow);
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
                    } else {
                        popupProcess.show(e.getComponent(),
                                e.getX(), e.getY());
                    }
                }
            }
        });
        final InputParamTableModel inputParamTableModel = new InputParamTableModel(dbService);
        inputParamTable = new JTable(inputParamTableModel) {

            public boolean editCellAt(final int row, final int column, java.util.EventObject e) {
//	        	 column=convertColumnIndexToModel(column);
                if (isEditing()) {
                    getCellEditor().stopCellEditing();
                }
                if (e instanceof MouseEvent) {
                    JTable table = (JTable) e.getSource();
                    MouseEvent mEvent = ((MouseEvent) e);

                    if (((MouseEvent) e).getClickCount() == 1 && this.isRowSelected(row)) {
                        return false;
                    }
                    if (mEvent.getClickCount() == 2 && column == 1) {
                        final InputParamTableModel iptm = ((InputParamTableModel) inputParamTable.getModel());
                        String value = (String) iptm.getValueAt(row, column);
                        TextAreaEditor.getInstance(value, text -> iptm.setValueAt(text, row, column), Main.PunterGuiFrame);
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

        outputParamTable = new JTable(new OutputParamTableModel(dbService));
        outputParamTable.setShowGrid(true);
        outputParamTable.setPreferredScrollableViewportSize(new Dimension(250, 150));
        outputParamTable.setFillsViewportHeight(true);
        outputParamTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        initColumnSizesOutputParamTable();
        ListSelectionModel rowSM = taskTable.getSelectionModel();
        rowSM.addListSelectionListener(e -> {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                AppSettings.getInstance().setObject("outputParamTable", GUIUtils.getColumnWidth(outputParamTable));
                AppSettings.getInstance().setObject("inputParamTable", GUIUtils.getColumnWidth(inputParamTable));
                int selectedRow1 = lsm.getMinSelectionIndex();
//        			System.out.println("Row " + selectedRow + " is now selected.");
                TaskData t = (TaskData) ((TaskTableModel) taskTable.getModel()).getRow(taskTable.convertRowIndexToModel(selectedRow1)).get(0);
                try {
                    if (t.getInputParamsAsObject() != null) {
                        inputParamTable.setModel(new InputParamTableModel(t, dbService));
                        inputParamTable.getColumn("<html><b>Value").setCellRenderer(new DefaultStringRenderer());
                        initColumnSizesInputParamTable();
                    } else {
                        inputParamTable.setModel(new InputParamTableModel(t, dbService));
                        inputParamTable.getColumn("<html><b>Value").setCellRenderer(new DefaultStringRenderer());
                        initColumnSizesInputParamTable();
                    }
                } catch (JAXBException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                try {
                    if (t.getOutputParamsAsObject() != null) {
                        outputParamTable.setModel(new OutputParamTableModel(t, dbService));
                        initColumnSizesOutputParamTable();
                    } else {
                        outputParamTable.setModel(new OutputParamTableModel(dbService));
                        initColumnSizesOutputParamTable();
                    }
                } catch (JAXBException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(taskTable);
        JTabbedPane tabbedPane = new JTabbedPane();
        //Set up column sizes.
        if (AppSettings.getInstance().getObject("taskTable") != null)
            GUIUtils.initilializeTableColumns(taskTable, (List) AppSettings.getInstance().getObject("taskTable"));
        else
            GUIUtils.initilializeTableColumns(taskTable, taskTableModel.width);
        if (AppSettings.getInstance().getObject("processTable") != null)
            GUIUtils.initilializeTableColumns(processTable, (List) AppSettings.getInstance().getObject("processTable"));
        else
            GUIUtils.initilializeTableColumns(processTable, model.width);
        initColumnSizesInputParamTable();
//        setUpSportColumn(taskTable, taskTable.getColumnModel().getColumn(2));
        JSplitPane jsp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(inputParamTable), new JScrollPane(outputParamTable));
        jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, jsp2);

        jsp.setDividerLocation(0.4);
        if (AppSettings.getInstance().getObject("jspLocation") != null)
            jsp.setDividerLocation(((Integer) AppSettings.getInstance().getObject("jspLocation")));
        //Add the scroll pane to this panel.
        processTable.setAutoscrolls(true);
        ListSelectionModel listSelectionModel = processTable.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (e.getValueIsAdjusting()) {
//                	System.err.println("Mouse is adjusting..");
                } else if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    try {
                        ProcessTaskHistoryTableModel pthtmodel = (ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
                        pthtmodel.clearTable();
                        ArrayList<?> ar = ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow()));
                        long procId = (Long) ((ProcessData) ar.get(0)).getId();
//	      	        	System.out.println("PID= "+procId);
                        //populate processHistory
                        ProcessHistoryTableModel phtmodel = (ProcessHistoryTableModel) processHistoryTable.getModel();
                        List<ProcessHistory> phl = dbService.getSortedProcessHistoryListForProcessId(procId);
                        phtmodel.clearTable();
                        for (ProcessHistory ph : phl) {
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(ph);
                            phtmodel.insertRow(newRequest);
                        }
                        if (phtmodel.getRowCount() > 0) {
                            processHistoryTable.setRowSelectionInterval(0, 0);
                        }
                        //populate task table
                        List<TaskData> taskList = dbService.getProcessTasksById(procId);
                        taskList = taskList.stream().filter(task -> task.isActive() == AppSettings.getInstance().isShowActiveTasks() || task.isActive() == true).collect(toList());
                        ProcessData process = dbService.getProcess(procId);
                        TaskTableModel model = (TaskTableModel) taskTable.getModel();
                        model.clearTable();
                        AppSettings.getInstance().setObject("inputParamTable", GUIUtils.getColumnWidth(inputParamTable));
                        AppSettings.getInstance().setObject("outputParamTable", GUIUtils.getColumnWidth(outputParamTable));
                        for (TaskData task : taskList) {
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(task);
                            model.insertRow(newRequest);
                        }
                        if (taskTable.getModel().getRowCount() > 0) {
                            taskTable.setRowSelectionInterval(0, 0);
                        } else {
                            inputParamTable.setModel(new InputParamTableModel(dbService));
                            outputParamTable.setModel(new OutputParamTableModel(dbService));
                            initColumnSizesInputParamTable();
                            initColumnSizesOutputParamTable();
                        }

                        //populate process properties
                        ProcessPropertyTableModel pptmodel = (ProcessPropertyTableModel) processPropertyTable.getModel();
                        pptmodel.clearTable();
                        FieldPropertiesMap props = process.getInputParams();
                        for (String key : props.keySet()) {
                            FieldProperties value = props.get(key);
//                		System.out.println(key + " = " + value.getValue());
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(key);
                            newRequest.add(value.getValue());
                            newRequest.add(process);
                            pptmodel.insertRow(newRequest);
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
        JScrollPane listPane = new JScrollPane(processTable);
        jsp.setDividerSize(1);
        tabbedPane.addTab("Process Tasks", null, (jsp), "Tasks List for selected Process");
        processHistoryTable = new JTable(new ProcessHistoryTableModel());
        processHistoryTable.setShowGrid(true);
        processHistoryTable.setPreferredScrollableViewportSize(new Dimension(200, 300));
        processHistoryTable.setFillsViewportHeight(true);
        processHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processHistoryTable.setRowHeight(22);
        processHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        header = processHistoryTable.getTableHeader();
        headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 20));
        processHistoryTable.getColumn("<html><b>Run ID").setCellRenderer(new ProcessHistoryTableRenderer());

        final ProcessTaskHistoryTableModel processTaskHistoryTableModel = new ProcessTaskHistoryTableModel();
        processTaskHistoryTable = new JTable(processTaskHistoryTableModel) {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                column = convertColumnIndexToModel(column);
                if (isEditing()) {
                    getCellEditor().stopCellEditing();
                }
                if (e instanceof MouseEvent) {
                    JTable table = (JTable) e.getSource();
                    MouseEvent mEvent = ((MouseEvent) e);

                    if (((MouseEvent) e).getClickCount() == 1 && this.isRowSelected(row)) {
                        return false;
                    }
                    if (mEvent.getClickCount() == 2) {
                        ProcessTaskHistoryTableModel phtm = ((ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel());
                        TaskHistory ph = (TaskHistory) phtm.getRow(processTaskHistoryTable.convertRowIndexToModel(processTaskHistoryTable.getSelectedRow())).get(0);
                        if (ph.getLogs() != null)
                            try {
                                LogWindow logWindow = new LogWindow(10000, ph.getTask().getName());
                                logWindow.log(ph.getLogs(), Level.ALL);
                                logWindow.showLog();
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
        JSplitPane jsp4 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processHistoryPane, processTaskHistoryPane);
        jsp4.setDividerSize(1);
        tabbedPane.addTab("Process History", null, jsp4, "Tasks Run History for selected Process");
        if (AppSettings.getInstance().getObject("processTaskHistoryTable") != null)
            GUIUtils.initilializeTableColumns(processTaskHistoryTable, (List) AppSettings.getInstance().getObject("processTaskHistoryTable"));
        else
            GUIUtils.initilializeTableColumns(processTaskHistoryTable, processTaskHistoryTableModel.width);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                //saving states of all the Tables.
                stopPunterJobScheduler();
                System.out.println("Divider Location :" + jsp3.getDividerLocation() + " - " + jsp3.getDividerSize());
                AppSettings.getInstance().setObject("processTaskAlertTable", GUIUtils.getColumnWidth(processTaskAlertTable));
                AppSettings.getInstance().setObject("processAlertTable", GUIUtils.getColumnWidth(processAlertTable));
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
        final ProcessPropertyTableModel processPropertyTableModel = new ProcessPropertyTableModel(dbService);
        processPropertyTable = new JTable(processPropertyTableModel);
        processPropertyTable.setShowGrid(true);
        processPropertyTable.setPreferredScrollableViewportSize(new Dimension(400, 300));
        processPropertyTable.setFillsViewportHeight(true);
        processPropertyTable.getTableHeader().setReorderingAllowed(false);
        processPropertyTable.setIntercellSpacing(new Dimension(0, 0));
        processPropertyTable.getColumn("<html><b>Property").setMaxWidth(200);
        processPropertyTable.getColumn("<html><b>Property").setPreferredWidth(150);
        processPropertyTable.getColumn("<html><b>Value").setCellRenderer(new ProcessPropertyTableRenderer());
        if (AppSettings.getInstance().getObject("processPropertyTable") != null)
            GUIUtils.initilializeTableColumns(processPropertyTable, (List) AppSettings.getInstance().getObject("processPropertyTable"));
        else
            GUIUtils.initilializeTableColumns(processPropertyTable, processPropertyTableModel.width);
        JScrollPane processPropertyPane = new JScrollPane(processPropertyTable);
        tabbedPane.addTab("Process Property", null, processPropertyPane, "Properties for selected Process");

        // processAlertTable
        processAlertTable = new JTable(new ProcessAlertTableModel(dbService));
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
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        processAlertTable.getTableHeader().setReorderingAllowed(false);
        processAlertTable.getColumn("<html><b>Run ID").setPreferredWidth(250);
        processAlertTable.getColumn("<html><b>Clear").setPreferredWidth(50);
        processAlertTable.getColumn("<html><b>Run ID").setCellRenderer(new ProcessAlertTableRenderer());
        if (AppSettings.getInstance().getObject("processAlertTable") != null)
            GUIUtils.initilializeTableColumns(processAlertTable, (List) AppSettings.getInstance().getObject("processAlertTable"));
        JScrollPane processAlertPane = new JScrollPane(processAlertTable);

        processTaskAlertTable = new JTable(new ProcessTaskHistoryTableModel()) {

            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                column = convertColumnIndexToModel(column);
                if (isEditing()) {
                    getCellEditor().stopCellEditing();
                }
                if (e instanceof MouseEvent) {
                    JTable table = (JTable) e.getSource();
                    MouseEvent mEvent = ((MouseEvent) e);

                    if (((MouseEvent) e).getClickCount() == 1 && this.isRowSelected(row)) {
                        return false;
                    }
                    if (mEvent.getClickCount() == 2) {
                        ProcessTaskHistoryTableModel phtm = ((ProcessTaskHistoryTableModel) processTaskAlertTable.getModel());
                        TaskHistory ph = (TaskHistory) phtm.getRow(processTaskAlertTable.convertRowIndexToModel(processTaskAlertTable.getSelectedRow())).get(0);
                        if (ph.getLogs() != null)
                            try {
                                LogWindow logWindow = new LogWindow(10000, ph.getTask().getName());
                                logWindow.log(ph.getLogs(), Level.ALL);
                                logWindow.showLog();
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
        if (AppSettings.getInstance().getObject("processTaskAlertTable") != null)
            GUIUtils.initilializeTableColumns(processTaskAlertTable, (List) AppSettings.getInstance().getObject("processTaskAlertTable"));
        JScrollPane processTaskALertPane = new JScrollPane(processTaskAlertTable);
        jsp6 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processAlertPane, processTaskALertPane);
        jsp6.setDividerSize(1);
        if (AppSettings.getInstance().getObject("jsp6Location") != null)
            jsp6.setDividerLocation(((Integer) AppSettings.getInstance().getObject("jsp6Location")));
        tabbedPane.addTab("My Alerts", null, jsp6, "My Workflow Alerts");
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JTabbedPane pane = (JTabbedPane) evt.getSource();
                int sel = pane.getSelectedIndex();
                if (sel == 3) {
                    ((ProcessTaskHistoryTableModel) processTaskAlertTable.getModel()).clearTable();
                    ProcessAlertTableModel phtmodel = (ProcessAlertTableModel) processAlertTable.getModel();
                    List<ProcessHistory> phl = dbService.getMySortedProcessHistoryList(
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
                        AppSettings.getInstance().setObject(AppConstants.APP_PROPERTIES, string);
                        AppSettings.getInstance().setObject(AppConstants.APP_PROPERTIES_MAP, properties);
                        try {
                            int id = Integer.parseInt(properties.getProperty(WIN_A, "0"));
                            globalHotKeyListener.setTaskIdToRun(id);
                        } catch (Exception e) {
                        }
                        // System.err.println("Properties Loaded to the System.");
                    } catch (IOException e) {
                        System.err.println("Error Loading app properties into the system.");
                        e.printStackTrace();
                    }
                }
            }
        });
        ListSelectionModel PATSelectionModel = processAlertTable.getSelectionModel();
        PATSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (e.getValueIsAdjusting()) {
//                	System.err.println("Mouse is adjusting..");
                } else if (lsm.isSelectionEmpty()) {
//                	System.out.println("PTHL Empty --No Row is now selected.");
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
//                    System.out.println("PTHL -- Row " + selectedRow + " is now selected.");
                    try {
                        ArrayList<?> ar = ((ProcessAlertTableModel) processAlertTable.getModel()).getRow(processAlertTable.convertRowIndexToModel(processAlertTable.getSelectedRow()));
                        long phId = (Long) ((ProcessHistory) ar.get(0)).getId();
                        ProcessHistory ph = dbService.getProcessHistoryById(phId);
                        //populate ProcessTaskHistory
                        ProcessTaskHistoryTableModel pthtmodel = (ProcessTaskHistoryTableModel) processTaskAlertTable.getModel();
                        List<TaskHistory> pthl = ph.getTaskHistoryList();
                        Collections.sort(pthl, new TaskHistorySeqComparator());
                        pthtmodel.clearTable();
                        for (TaskHistory th : pthl) {
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(th);
                            pthtmodel.insertRow(newRequest);
                        }
                        if (pthtmodel.getRowCount() > 0) {
                            processTaskAlertTable.setRowSelectionInterval(0, 0);
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
        appLogArea = new TextAreaFIFO(5000);
        appLogArea.setEditable(false);
        tabbedPane.addTab("App Logs", null, new JScrollPane(appLogArea), "Application Wide Logging");
        jTextArea = new JTextArea();
        jTextArea.setText((String) AppSettings.getInstance().getObject("appProperties"));
        tabbedPane.addTab("App Props", null, new JScrollPane(jTextArea), "Application Wide Properties");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        final JTextField searchTextField = new JTextField("");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        panel.add(searchTextField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        panel.add(listPane, c);

        searchTextField.getDocument().addDocumentListener(getDocumentListener(searchTextField));
        jsp3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, tabbedPane);
        jsp3.setDividerSize(1);
        if (AppSettings.getInstance().getObject("jsp3Location") != null)
            jsp3.setDividerLocation(((Integer) AppSettings.getInstance().getObject("jsp3Location")));
        JSplitPane jsp5 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp3, splitRunningProcessPane);
        jsp5.setDividerSize(1);
        add(jsp5);
//        add(jsp3);
//        tabbedPane.addTab("Running Tasks", null, splitRunningProcessPane, "Running Tasks in Punter");

        ListSelectionModel PHTSelectionModel = processHistoryTable.getSelectionModel();
        PHTSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (e.getValueIsAdjusting()) {
//                	System.err.println("Mouse is adjusting..");
                } else if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    try {
                        ArrayList<?> ar = ((ProcessHistoryTableModel) processHistoryTable.getModel()).getRow(processHistoryTable.convertRowIndexToModel(processHistoryTable.getSelectedRow()));
                        long phId = (Long) ((ProcessHistory) ar.get(0)).getId();
                        ProcessHistory ph = dbService.getProcessHistoryById(phId);
                        //populate ProcessTaskHistory
                        ProcessTaskHistoryTableModel pthtmodel = (ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
                        List<TaskHistory> pthl = ph.getTaskHistoryList();
                        Collections.sort(pthl, new TaskHistorySeqComparator());
                        pthtmodel.clearTable();
                        for (TaskHistory th : pthl) {
                            final ArrayList<Object> newRequest = new ArrayList<Object>();
                            newRequest.add(th);
                            pthtmodel.insertRow(newRequest);
                        }
                        if (pthtmodel.getRowCount() > 0) {
                            processTaskHistoryTable.setRowSelectionInterval(0, 0);
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
        ProcessTableModel tmpModel = (ProcessTableModel) processTable.getModel();
        List<ProcessData> pl = dbService.getProcessList(AppSettings.getInstance().getUsername());
        for (ProcessData p : pl) {
            final ArrayList<Object> newRequest = new ArrayList<Object>();
            newRequest.add(p);
            tmpModel.insertRow(newRequest);
        }
        punterJobBasket = PunterJobBasket.getInstance();
        punterJobBasket.addObserver(this);
        if (processTable.getModel().getRowCount() > 0) {
            processTable.setRowSelectionInterval(0, 0);
        }
        if (AppSettings.getInstance().isSchedulerEnabled()) {
            startPunterJobScheduler();
        }
        setErrAndOutStreamToLogDocument();
    }

    public boolean isClipboardListenerRunning() {
        if (clipBoardListener != null) {
            return clipBoardListener.isStarted();
        }
        return false;
    }

    public void startClipBoardListener() {
        if (clipBoardListener == null || !clipBoardListener.isStarted()) {
            synchronized (this) {
                clipBoardListener.startComponent();
            }
        }
    }

    public void stopClipBoardListener() {
        if (clipBoardListener != null && clipBoardListener.isStarted()) {
            synchronized (this) {
                clipBoardListener.stopComponent();
            }
        }
    }

    public void startPunterJobScheduler() {
        if (!schedulerRunning) {
            synchronized (this) {
                punterJobScheduler = new PunterJobScheduler(dbService);
                punterJobScheduler.start();
                schedulerRunning = true;
            }
        }
    }

    public void stopPunterJobScheduler() {
        if (schedulerRunning) {
            punterJobScheduler.stop();
        }
        schedulerRunning = false;
    }

    private void setErrAndOutStreamToLogDocument() {
        System.setOut(new PrintStream(new ConsoleOutputStream(appLogArea.getDocument(), System.out), true));
        System.setErr(new PrintStream(new ConsoleOutputStream(appLogArea.getDocument(), System.err), true));
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

    public void createAndRunProcess(final PunterProcessRunMessage processRunMessage) throws Exception {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Future<Map> future = createAndRunProcessSync(processRunMessage);
                try {
                    processRunMessage.setResults(future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    processRunMessage.markDone();
                }
            }
        };
        thread.setName("CreateAndRunProcess");
        thread.start();
    }

    @Override
    public Future<Map> createAndRunProcessSync(PunterProcessRunMessage processRunMessage) {
        try {
            final ProcessData processData = dbService.getProcess(processRunMessage.getProcessId());
            final ProcessHistory processHistory = ProcessHistoryBuilder.build(processData, dbService);
            final ProcessHistory ph1 = dbService.create(processHistory);
            final ArrayList<Object> newRequest = new ArrayList<>();
            newRequest.add(ph1);
            final org.shunya.punter.tasks.Process process = org.shunya.punter.tasks.Process.getProcess(dbService, processData.getInputParams(), ph1, parseStringMap(processRunMessage.getParams()));
            process.setTaskObservable(this);
            SwingUtilities.invokeAndWait(() -> {
                try {
                    if (processTable.getSelectedRow() != -1) {
                        ProcessData pd = (ProcessData) ((ProcessTableModel) processTable.getModel()).getRow(processTable.convertRowIndexToModel(processTable.getSelectedRow())).get(0);
                        if (pd.getId() == processData.getId()) {
                            ((ProcessHistoryTableModel) processHistoryTable.getModel()).insertRowAtBeginning(newRequest);
                            processHistoryTable.setRowSelectionInterval(0, 0);
                        }
                    }
                    // Adding row to running process table model
                    final RunningProcessTableModel rptm = (RunningProcessTableModel) runningProcessTable.getModel();
                    ArrayList<Object> newRequest1 = new ArrayList<>();
                    newRequest1.add(ph1);
                    rptm.insertRowAtBeginning(newRequest1);
                    process.addObserver(new ProcessObserver() {
                        @Override
                        public void update(ProcessHistory ph) {
                            ((ProcessHistoryTableModel) processHistoryTable.getModel()).refreshTable();
                            rptm.refreshTable();
                        }

                        @Override
                        public void processCompleted() {
                            if (rptm.getRowCount() > 0 && runningProcessTable.getSelectedRow() == -1) {
                                runningProcessTable.setRowSelectionInterval(0, 0);
                            }
                            if (ph1.getRunStatus().equals(RunStatus.SUCCESS))
                                Main.displayMsg("" + ph1.getName() + " Success", TrayIcon.MessageType.INFO);
                            else
                                Main.displayMsg("" + ph1.getName() + " Failed", TrayIcon.MessageType.WARNING);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return runProcess(process);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Future<Map> runProcess(org.shunya.punter.tasks.Process process) {
        return ProcessExecutor.getInstance().submitProcess(process);
    }

    @Override
    public void saveTaskHistory(final TaskHistory taskHistory) {
        try {
            dbService.saveTaskHistory(taskHistory);
            if (processHistoryTable.getSelectedRow() != -1) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    try {
                        ArrayList<?> ar = ((ProcessHistoryTableModel) processHistoryTable.getModel()).getRow(processHistoryTable.convertRowIndexToModel(processHistoryTable.getSelectedRow()));
                        long pidTable = ((ProcessHistory) ar.get(0)).getId();
                        long pid = taskHistory.getProcessHistory().getId();
                        if (pid == pidTable) {
                            ProcessHistory ph = dbService.getProcessHistoryById(pid);
                            //populate ProcessTaskHistory
                            ProcessTaskHistoryTableModel pthtmodel = (ProcessTaskHistoryTableModel) processTaskHistoryTable.getModel();
                            List<TaskHistory> pthl = ph.getTaskHistoryList();
                            pthtmodel.clearTable();
                            for (TaskHistory th : pthl) {
                                final ArrayList<Object> newRequest = new ArrayList<>();
                                newRequest.add(th);
                                pthtmodel.insertRow(newRequest);
                            }
                            if (pthtmodel.getRowCount() > 0) {
                                processTaskHistoryTable.setRowSelectionInterval(0, 0);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(TaskHistory taskHistory) {
        ((RunningTaskTableModel) runningTaskTable.getModel()).refreshTable();
    }

    private void doOnKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                if (runningProcessTable.getSelectedRow() != -1) {
                    int[] userSelectedRows = runningProcessTable.getSelectedRows();
                    List<Long> rowsToDelete = new ArrayList<Long>();
                    for (int row : userSelectedRows) {
                        int rowModelIndex = runningProcessTable.convertRowIndexToModel(row);
                        ArrayList process = ((RunningProcessTableModel) runningProcessTable.getModel()).getRow(rowModelIndex);
                        if (((ProcessHistory) process.get(0)).getRunState().equals(RunState.COMPLETED))
                            rowsToDelete.add(((ProcessHistory) process.get(0)).getId());
                    }
                    ((RunningProcessTableModel) runningProcessTable.getModel()).deleteRowNumbers(rowsToDelete);
                    break;
                }
            default:
                break;
        }
    }


    private void initColumnSizesOutputParamTable() {
        if (AppSettings.getInstance().getObject("outputParamTable") != null)
            GUIUtils.initilializeTableColumns(outputParamTable, (List) AppSettings.getInstance().getObject("outputParamTable"));
        else
            GUIUtils.initilializeTableColumns(outputParamTable, (List) OutputParamTableModel.width);
    }

    private void initColumnSizesInputParamTable() {
        if (AppSettings.getInstance().getObject("inputParamTable") != null)
            GUIUtils.initilializeTableColumns(inputParamTable, (List) AppSettings.getInstance().getObject("inputParamTable"));
        else
            GUIUtils.initilializeTableColumns(inputParamTable, (List) InputParamTableModel.width);
    }

    public void setUpSportColumn(JTable table, TableColumn sportColumn) {
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
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        sportColumn.setCellRenderer(renderer);
    }

    private boolean checkImportPossible(TransferHandler.TransferSupport support) {
        try {
            if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

            } else {
                DataFlavor[] dfs = support.getDataFlavors();
                System.out.println("\n\nTotal MimeTypes Supported by this operation :" + dfs.length);
                for (DataFlavor df : dfs) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            PunterProcessRunMessage processRunMessage = (PunterProcessRunMessage) arg;
            createAndRunProcess(processRunMessage);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public GlobalHotKeyListener getGlobalHotKeyListener() {
        return globalHotKeyListener;
    }

    public void setGlobalHotKeyListener(GlobalHotKeyListener globalHotKeyListener) {
        this.globalHotKeyListener = globalHotKeyListener;
    }

    static class DefaultStringRenderer extends DefaultTableCellRenderer {
        public DefaultStringRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            try {
                TaskData taskData = (TaskData) table.getModel().getValueAt(row, 2);
                FieldProperties fieldProperties = taskData.getInputParamsAsObject().get((String) table.getModel().getValueAt(row, 0));
                setToolTipText(fieldProperties.getDescription());
            } catch (JAXBException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        public void setValue(Object value) {
            setText((value == null || value.toString().isEmpty()) ? "---" : value.toString());
        }
    }

    static class ProcessPropertyTableRenderer extends DefaultTableCellRenderer {

        public ProcessPropertyTableRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            ProcessData processData = (ProcessData) ((ProcessPropertyTableModel) table.getModel()).getValueAt(row, 2);
            FieldProperties fieldProperties = null;
            try {
                fieldProperties = (FieldProperties) processData.getInputParams().get((String) table.getModel().getValueAt(row, 0));
                setToolTipText(fieldProperties.getDescription());
            } catch (JAXBException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        public void setValue(Object value) {
            setText((value.toString().isEmpty()) ? "---" : value.toString());
        }
    }

    static class ProcessHistoryTableRenderer extends DefaultTableCellRenderer {
        private static final Color successColor = new Color(51, 153, 51);
        private static final Color failureColor = new Color(153, 51, 0);

        public ProcessHistoryTableRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            ProcessHistory ph = (ProcessHistory) ((ProcessHistoryTableModel) table.getModel()).getRow(table.convertRowIndexToModel(row)).get(0);
            if (ph.getRunStatus().equals(RunStatus.FAILURE)) {
                setBackground(failureColor);
                setForeground(Color.WHITE);
            } else if (ph.getRunStatus().equals(RunStatus.SUCCESS)) {
                setBackground(successColor);
                setForeground(Color.WHITE);
            } else if (ph.getRunStatus().equals(RunStatus.NOT_RUN)) {
                setBackground(Color.GRAY);
                setForeground(Color.BLACK);
            } else {
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

        public ProcessAlertTableRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            ProcessHistory ph = (ProcessHistory) ((ProcessAlertTableModel) table.getModel()).getRow(table.convertRowIndexToModel(row)).get(0);
            if (ph.getRunStatus().equals(RunStatus.FAILURE)) {
                setBackground(failureColor);
                setForeground(Color.WHITE);
            } else if (ph.getRunStatus().equals(RunStatus.SUCCESS)) {
                setBackground(successColor);
                setForeground(Color.WHITE);
            } else if (ph.getRunStatus().equals(RunStatus.NOT_RUN)) {
                setBackground(Color.GRAY);
                setForeground(Color.BLACK);
            } else {
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
            this.theTable = theTable;
            this.setStringPainted(true);
            //  this.setIndeterminate(true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            int val = Integer.parseInt(value.toString());
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
            try {
                //   theTable.repaint();
            } catch (Exception e) {
                System.out.println("1111");
            }
        }
    }

    static class ProcessTableRenderer extends DefaultTableCellRenderer {
        private static final Color successColor = new Color(51, 153, 51);
        private static final Color failureColor = new Color(153, 51, 0);

        public ProcessTableRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            if (value != null) {
                if (value.toString().toLowerCase().contains("prod")) {
                    setForeground(Color.red);
                } else
                    setForeground(Color.blue);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
        }

        public void setValue(Object value) {
            setText((value == null || value.toString().isEmpty()) ? "---" : value.toString());
        }
    }
}