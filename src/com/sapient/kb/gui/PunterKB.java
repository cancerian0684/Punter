package com.sapient.kb.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
	private JTextField searchTextField;
	private JTable searchResultTable;
	private JComboBox categoryComboBox;
	private JToggleButton toggleButton = new JToggleButton("Spcl. Txt");

	private Object[] categories = {"/all","/all/aisdb","/all/aisdb/article","/all/aisdb/query","/all/aisdb/dev",
										"/all/daisy","/all/daisy/article","/all/daisy/query","/all/daisy/dev",
										"/all/todo","/all/idea","/all/article","/all/personal","/all/misc"};
	private static StaticDaoFacade docService;
	{
		docService.getDocList("","",false);
	}
	
	public PunterKB() {
		 setLayout(new GridBagLayout());
		 GridBagConstraints c = new GridBagConstraints();

		 List<String> dataList=new ArrayList<String>();
		 dataList.add("Munish");
		 dataList.add("Manu");
		 dataList.add("banyal");
		 dataList.add("Parvesh");
		 searchTextField=new JTextField(20);
		 searchTextField.setFont(new Font("Arial",Font.TRUETYPE_FONT,12));
//		 filterText.setMaximumSize(new Dimension(1200, 30));
//		 filterText.setPreferredSize(new Dimension(800, 30));
		 searchTextField.getDocument().addDocumentListener(
	                new DocumentListener() {
	                    public void changedUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
	                    public void insertUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
	                    public void removeUpdate(DocumentEvent e) {
	                    	updateSearchResult();
	                    }
	                });
		 /* searchTextField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent ke) {
				System.err.println("kp");
				updateSearchResult();
//				if (ke.getKeyCode() == KeyEvent.VK_ENTER)
			}

			public void keyReleased(KeyEvent arg0) {
				System.err.println("kr");
			}

			public void keyTyped(KeyEvent arg0) {
				System.err.println("kt");
//				updateSearchResult();
			}
		});*/
		 searchResultTable=new JTable(new DocumentTableModel()){
         public boolean editCellAt(int row, int column, java.util.EventObject e) {
    	 column=convertColumnIndexToModel(column);
    	 if(column==1||column==2){
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
	            	   Document luceneDoc=(Document) ((DocumentTableModel)table.getModel()).getRow(table.convertRowIndexToModel(table.getSelectedRow())).get(0);
	            	   docService.updateAccessCounter(luceneDoc);
	            	   Document doc=docService.getDocument(luceneDoc);
	            	   if(column==1){
         				DocumentEditor.showEditor(doc,docService,Main.main);
	            	   	}
	            	   String category = doc.getCategory();
	            	   if(column ==2){
            	    	String s = (String)JOptionPane.showInputDialog(
		        	    	                    Main.main,
		        	    	                    "Select the Category:",
		        	    	                    "Choose Category",
		        	    	                    JOptionPane.PLAIN_MESSAGE,
		        	    	                    null,
		        	    	                    categories,
		        	    	                    category);
            	    	 if ((s != null) && (s.length() > 0)&&(!s.equals(category))) {
            	    		System.err.println("updating category.");
            	    		doc.setCategory(s);
            	    		luceneDoc.setCategory(s);
            	    		StaticDaoFacade.saveDocument(doc);
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
    	 	}
    	 	return super.editCellAt(row, column, e);
         }

		 };
//		 table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		 searchResultTable.setShowGrid(false);
//		 table.setPreferredScrollableViewportSize(new Dimension(800, 700));
//		 table.setMinimumSize(new Dimension(600, 600));
		 searchResultTable.setFillsViewportHeight(true);
		 searchResultTable.setAutoCreateRowSorter(true);
		 searchResultTable.setRowHeight(60);
		 searchResultTable.setIntercellSpacing(new Dimension(0, 0));
		 searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 searchResultTable.setFont(new Font("Arial",Font.TRUETYPE_FONT,11));
		 TableCellRenderer dcr = searchResultTable.getDefaultRenderer(String.class);
		 if(dcr instanceof JLabel){
	        	((JLabel)dcr).setVerticalTextPosition(SwingConstants.TOP);
	        	((JLabel)dcr).setBorder(new EmptyBorder(0, 0, 0, 0));
	         }
         JTableHeader header = searchResultTable.getTableHeader();
         TableCellRenderer headerRenderer = header.getDefaultRenderer();
         if(headerRenderer instanceof JLabel){
        	((JLabel)headerRenderer).setHorizontalAlignment(JLabel.CENTER);
         }
         header.setPreferredSize(new Dimension(30, 20));
         TableColumn column = null;
         for (int i = 0; i < 3; i++) {
             column = searchResultTable.getColumnModel().getColumn(i);
             if (i == 1) {
                 column.setPreferredWidth(1000); //third column is bigger
             } else if(i==0){
                 column.setPreferredWidth(100);
             }
             else{
            	 column.setPreferredWidth(100);
             }
         }
         categoryComboBox = new JComboBox(categories);
         categoryComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			     updateSearchResult();
			}
		});
         toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSearchResult();
			}
		});
        
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.8;
         c.gridx = 0;
         c.gridy = 0;
         add(searchTextField,c);
         
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.05;
         c.gridx = 1;
         c.gridy = 0;
         add(toggleButton,c);
         
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0.2;
         c.gridx = 2;
         c.gridy = 0;
         add(categoryComboBox, c);
         
         c.fill = GridBagConstraints.BOTH;
         c.ipady = 0;      //make this component tall
         c.weightx = 0.0;
         c.weighty = 0.9;
         c.gridwidth = 3;
         c.gridx = 0;
         c.gridy = 1;
         add(new JScrollPane(searchResultTable), c);

         final JMenuItem addProcessMenu,openDocMenu,deleteDocMenu,docTagsMenu,reindexDocsMenu;
 		 final JPopupMenu popupProcess = new JPopupMenu();
 		 addProcessMenu = new JMenuItem("Add");
 		 addProcessMenu.addActionListener(new ActionListener() {
 	          public void actionPerformed(ActionEvent e) {
 	        	  System.out.println("Adding Document");
 	        	  Document doc=docService.createDocument();
 	        	  DocumentEditor.showEditor(doc,docService,Main.main);
 	          }
 	    });
 		popupProcess.add(addProcessMenu);
 		openDocMenu = new JMenuItem("Open");
 		openDocMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Opening Document");
 				if(searchResultTable.getSelectedRow()>=0){
 				Document doc=(Document) ((DocumentTableModel)searchResultTable.getModel()).getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
 				DocumentEditor.showEditor(docService.getDocument(doc),docService,Main.main);
 				docService.updateAccessCounter(doc);
 				}
 			}
 		});
 		popupProcess.add(openDocMenu);
 		deleteDocMenu = new JMenuItem("Delete");
 		deleteDocMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Delete Document");
 				if(searchResultTable.getSelectedRow()>=0){
 					DocumentTableModel dtm = (DocumentTableModel)searchResultTable.getModel();
	 				Document doc=(Document)dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
	 				docService.deleteDocument(doc);
	 				dtm.deleteRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow()));
 				}
 			}
 		});
 		popupProcess.add(deleteDocMenu);
 		
 		docTagsMenu = new JMenuItem("Tags");
 		docTagsMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Document Tags");
 				if(searchResultTable.getSelectedRow()>=0){
 					DocumentTableModel dtm = (DocumentTableModel)searchResultTable.getModel();
	 				Document doc=(Document)dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
	 				TagDialog.getInstance(doc, docService);
 				}
 			}
 		});
 		popupProcess.add(docTagsMenu);
 		
 		reindexDocsMenu = new JMenuItem("Rebuild Indexes");
 		reindexDocsMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clearing old index");
 				LuceneIndexDao.getInstance().deleteIndex();
 				System.out.println("Rebuilding Index");
 				PunterKB.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
 				docService.rebuildIndex();
 				PunterKB.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 			}
 		});
 		popupProcess.add(reindexDocsMenu);
 		searchResultTable.addMouseListener(new MouseAdapter() {
 	          //JPopupMenu popup;
 	          public void mousePressed(MouseEvent e) {
 				if (searchResultTable.getSelectedRowCount() > 1) {
 					if (SwingUtilities.isRightMouseButton(e)) {
 						searchResultTable.clearSelection();
 						popupProcess.show(e.getComponent(), e.getX(), e.getY());
 					}
 				}
 				 else{
 		        	  int selectedRow = searchResultTable.rowAtPoint(e.getPoint());
 		        		  if (SwingUtilities.isRightMouseButton(e)) {
 		        			  if(selectedRow!=-1){
	 		        			  searchResultTable.setRowSelectionInterval(selectedRow, selectedRow);
	 		        			  selectedRow=searchResultTable.convertRowIndexToModel(selectedRow);
	 		        			  openDocMenu.setEnabled(true);
	 		        			  deleteDocMenu.setEnabled(true);
	 		        			  docTagsMenu.setEnabled(true);
 		        			  }else{
 		        				  openDocMenu.setEnabled(false);
 		        				  deleteDocMenu.setEnabled(false);
 		        				  docTagsMenu.setEnabled(false);
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
	private void updateSearchResult() {
		DocumentTableModel ttm=((DocumentTableModel)searchResultTable.getModel());
    	ttm.clearTable();
    	List<Document> docs = docService.getDocList(searchTextField.getText(),categoryComboBox.getSelectedItem().toString(),toggleButton.isSelected());
    	for (Document doc : docs) {
			ArrayList<Document> docList=new ArrayList<Document>();
			docList.add(doc);
			ttm.insertRow(docList);
		}
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