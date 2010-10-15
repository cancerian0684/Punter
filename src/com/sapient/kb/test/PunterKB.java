package com.sapient.kb.test;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.sapient.kb.jpa.Document;
import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.gui.Main;

public class PunterKB extends JPanel{
	private static JFrame frame;
	private JTextField filterText;
	private JTable table;
	
	private static StaticDaoFacade docService;
	{
		docService.getDocList("");
	}
	
	public PunterKB() {
		 setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		 List<String> dataList=new ArrayList<String>();
		 dataList.add("Munish");
		 dataList.add("Manu");
		 dataList.add("banyal");
		 dataList.add("Parvesh");
		 filterText=new JTextField(20);
		 filterText.setFont(new Font("Arial",Font.TRUETYPE_FONT,12));
		 filterText.setMaximumSize(new Dimension(1200, 30));
		 filterText.setPreferredSize(new Dimension(200, 30));
		 filterText.getDocument().addDocumentListener(
	                new DocumentListener() {
	                    public void changedUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
	                    public void insertUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
						/**
						 * 
						 */
						private void updateSearchResult() {
							DocumentTableModel ttm=((DocumentTableModel)table.getModel());
	                    	ttm.clearTable();
	                    	List<Document> docs = docService.getDocList(filterText.getText());
	                    	for (Document doc : docs) {
								ArrayList<Document> docList=new ArrayList<Document>();
								docList.add(doc);
								ttm.insertRow(docList);
							}
	                    	 /*int vColIndex = 1;
		               		 int margin = 0;
		               		 packColumn(table, vColIndex, margin);*/
						}
	                    public void removeUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
	                });
		 table=new JTable(new DocumentTableModel()){
         public boolean editCellAt(int row, int column, java.util.EventObject e) {
    	 column=convertColumnIndexToModel(column);
    	 if(column==1){
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
                      	Document doc=(Document) ((DocumentTableModel)table.getModel()).getRow(table.getSelectedRow()).get(0);
         				TestEditor.showEditor(docService.getDocument(doc),docService,Main.main);
         				docService.updateAccessCounter(doc);
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

		 };
//		 table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		 table.setShowGrid(false);
		 table.setPreferredScrollableViewportSize(new Dimension(800, 600));
		 table.setFillsViewportHeight(true);
		 table.setAutoCreateRowSorter(true);
		 table.setRowHeight(60);
		 table.setIntercellSpacing(new Dimension(0, 0));
		 table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 table.setFont(new Font("Arial",Font.TRUETYPE_FONT,11));
		 TableCellRenderer dcr = table.getDefaultRenderer(String.class);
		 if(dcr instanceof JLabel){
	        	((JLabel)dcr).setVerticalTextPosition(SwingConstants.TOP);
	        	((JLabel)dcr).setBorder(new EmptyBorder(0, 0, 0, 0));
	         }
         JTableHeader header = table.getTableHeader();
         TableCellRenderer headerRenderer = header.getDefaultRenderer();
         if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
         }
         header.setPreferredSize(new Dimension(30, 20));
         TableColumn column = null;
         for (int i = 0; i < 3; i++) {
             column = table.getColumnModel().getColumn(i);
             if (i == 1) {
                 column.setPreferredWidth(1000); //third column is bigger
             } else if(i==0){
                 column.setPreferredWidth(100);
             }
             else{
            	 column.setPreferredWidth(100);
             }
         }
         
         add(filterText);
         add(new JScrollPane(table));
         final JMenuItem addProcessMenu,openDocMenu;
 		 final JPopupMenu popupProcess = new JPopupMenu();
 		 addProcessMenu = new JMenuItem("Add Document");
 		 addProcessMenu.addActionListener(new ActionListener() {
 	          public void actionPerformed(ActionEvent e) {
 	        	  System.out.println("Adding Document");
 	        	  Document doc=docService.createDocument();
 	        	  TestEditor.showEditor(doc,docService,Main.main);
 	          }
 	    });
 		popupProcess.add(addProcessMenu);
 		openDocMenu = new JMenuItem("Open Document");
 		openDocMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Opening Document");
 				if(table.getSelectedRow()>=0){
 				Document doc=(Document) ((DocumentTableModel)table.getModel()).getRow(table.getSelectedRow()).get(0);
 				TestEditor.showEditor(docService.getDocument(doc),docService,Main.main);
 				docService.updateAccessCounter(doc);
 				}
 			}
 		});
 		popupProcess.add(openDocMenu);
 		table.addMouseListener(new MouseAdapter() {
 	          //JPopupMenu popup;
 	          public void mousePressed(MouseEvent e) {
 				if (table.getSelectedRowCount() > 1) {
 					if (SwingUtilities.isRightMouseButton(e)) {
 						table.clearSelection();
 						popupProcess.show(e.getComponent(), e.getX(), e.getY());
 					}
 				}
 				 else{
 		        	  int selectedRow = table.rowAtPoint(e.getPoint());
 		        		  if (SwingUtilities.isRightMouseButton(e)) {
 		        			  if(selectedRow!=-1){
	 		        			  table.setRowSelectionInterval(selectedRow, selectedRow);
	 		        			  selectedRow=table.convertRowIndexToModel(selectedRow);
	 		        			  openDocMenu.setEnabled(true);
 		        			  }else{
 		        				  openDocMenu.setEnabled(false);
 		        			  }
 		        			  popupProcess.show(e.getComponent(),e.getX(), e.getY());
 		        		 }
 		        	  }
 			}
 	      });
	}
	public void packColumns(JTable table, int margin) {
	    for (int c=0; c<table.getColumnCount(); c++) {
	        packColumn(table, c, 2);
	    }
	}

	// Sets the preferred width of the visible column specified by vColIndex. The column
	// will be just wide enough to show the column head and the widest cell in the column.
	// margin pixels are added to the left and right
	// (resulting in an additional width of 2*margin pixels).
	public void packColumn(JTable table, int vColIndex, int margin) {
	    TableModel model = table.getModel();
	    DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
	    TableColumn col = colModel.getColumn(vColIndex);
	    int width = 0;

	    // Get width of column header
	    TableCellRenderer renderer = col.getHeaderRenderer();
	    if (renderer == null) {
	        renderer = table.getTableHeader().getDefaultRenderer();
	    }
	    Component comp = renderer.getTableCellRendererComponent(
	        table, col.getHeaderValue(), false, false, 0, 0);
	    width = comp.getPreferredSize().width;

	    // Get maximum width of column data
	    for (int r=0; r<table.getRowCount(); r++) {
	        renderer = table.getCellRenderer(r, vColIndex);
	        comp = renderer.getTableCellRendererComponent(
	            table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
	        width = Math.max(width, comp.getPreferredSize().width);
	    }

	    // Add margin
	    width += 2*margin;

	    // Set the width
	    col.setPreferredWidth(width);
	}

	 private static void createAndShowGUI() throws Exception {
	        //Create and set up the window.
	        frame = new JFrame("Punter KB");
	        JFrame.setDefaultLookAndFeelDecorated(true);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setLocationRelativeTo(null);

	        //Create and set up the content pane.
	        PunterKB newContentPane = new PunterKB();
	        newContentPane.setOpaque(true); //content panes must be opaque
	        frame.setContentPane(newContentPane);
	        
	        frame.pack();
	        frame.setVisible(true);
	 }
	 public static void main(String[] args) {
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