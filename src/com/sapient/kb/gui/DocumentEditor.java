package com.sapient.kb.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.OptimisticLockException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.EkitCoreSpell;
import com.sapient.kb.jpa.Attachment;
import com.sapient.kb.jpa.Document;
import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.gui.AppSettings;
import com.sapient.punter.gui.Main;
import com.sapient.punter.gui.PunterGUI;

public class DocumentEditor extends JDialog{
	protected JTextField textField;
	private JSplitPane jsp;
	private EkitCore ekitCore;
	private Document doc;
	private StaticDaoFacade docService;
	private JTable attachmentTable;
	private String currentMD5;
	private boolean editable=false;
	private boolean everEdited=false;
	private static BufferedImage idleImage;
	static {
		 try {
			idleImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_discnt.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	public static void showEditor(Document doc,StaticDaoFacade docService,JFrame parent){
		DocumentEditor testEditor=new DocumentEditor(parent,doc,docService);
		testEditor.ekitCore.setDocumentText(new String(doc.getContent()));
		testEditor.textField.setText(doc.getTitle());
		testEditor.setTitle(doc.getId()+"-"+ doc.getTitle().substring(0, doc.getTitle().length()>20?20:doc.getTitle().length())+" ... [ "+doc.getAccessCount()+" .. "+doc.getDateAccessed()+" ]");
		testEditor.pack();
		testEditor.setVisible(true);
//		testEditor.ekitCore.requestFocus();
	}
	public DocumentEditor(JFrame parent,final Document ldoc,StaticDaoFacade docServic) {
		super(parent,false);
		setAlwaysOnTop(false);
		setIconImage(idleImage);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.doc=ldoc;
		this.docService=docServic;
//	    this.ekitCore = new EkitCoreSpell(false, str1, str2, str3, null, localURL, bool1, bool2, bool3, bool4, str4, str5, bool5, bool6, true, bool8, (bool8) ? "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|FN|SP|UC|UM|SP|SR|*|BL|IT|UD|SP|SK|SU|SB|SP|AL|AC|AR|AJ|SP|UL|OL|SP|LK|*|ST|SP|FO" : "NW|NS|OP|SV|PR|SP|CT|CP|PS|SP|UN|RE|SP|BL|IT|UD|SP|FN|SP|UC|SP|LK|SP|SR|SP|ST", bool9);
	    this.ekitCore = new EkitCoreSpell(false);
	    setJMenuBar(this.ekitCore.getMenuBar());
	    this.ekitCore.setEnterKeyIsBreak(true);
	    this.ekitCore.setFrame(Main.KBFrame);
	    this.ekitCore.getTextPane().setEditable(false);
	    this.ekitCore.getTextPane().addMouseListener(new DocumentEditMousListener());
	    KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK , false);
	    this.ekitCore.unregisterKeyboardAction(keystroke);
	    textField = new JTextField(20);
		GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(textField, c);
		JTabbedPane jtp=new JTabbedPane();
		attachmentTable=new JTable(new AttachmentTableModel()){
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
		            	   AttachmentTableModel atm = ((AttachmentTableModel)attachmentTable.getModel());
		            	   Attachment attch=(Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow())).get(0);
		            	   if(Desktop.isDesktopSupported()){
		                   		File temp=new File("Temp");
		                   		temp.mkdir();
		                   		File nf=new File(temp,"A"+attch.getId()+""+attch.getExt());
		                   		System.out.println("Opening up the file.."+nf.getName());
		                   		try {
		                   			if(!nf.exists()){
		                   			FileOutputStream fos = new FileOutputStream(nf);
		                   			fos.write(attch.getContent());
		                   			fos.close();
		                   			nf.deleteOnExit();
		                   			}
									Desktop.getDesktop().open(nf);
								} catch (IOException e1) {
									e1.printStackTrace();
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
		attachmentTable.setShowGrid(false);
		attachmentTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
		attachmentTable.setFillsViewportHeight(true);
		attachmentTable.setAutoCreateRowSorter(true);
		attachmentTable.setRowHeight(20);
		attachmentTable.setIntercellSpacing(new Dimension(0, 0));
		attachmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attachmentTable.setFont(new Font("Courier New",Font.TRUETYPE_FONT,11));
		attachmentTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		attachmentTable.setDragEnabled(true);
		InputMap imap = attachmentTable.getInputMap(JComponent.WHEN_FOCUSED);
	    imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
	    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false), "table.copy");
	    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false), "table.paste");
	    ActionMap amap = attachmentTable.getActionMap();
	    amap.put("table.delete", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				 if(attachmentTable.getSelectedRow() != -1){
	                	System.out.println("Delete Key Pressed.");
	                	int[] selectedRows = attachmentTable.getSelectedRows();
						for (int selectedRow : selectedRows) {
							AttachmentTableModel atm = ((AttachmentTableModel)attachmentTable.getModel());
			            	Attachment attch=(Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow())).get(0);
			            	try {
								docService.deleteAttachment(attch);
							} catch (RemoteException e1) {
								e1.printStackTrace();
							}
			            	atm.deleteRow(selectedRow);
						}
	                }
			}});
		AttachmentTableModel atm = ((AttachmentTableModel)attachmentTable.getModel());
		Collection<Attachment> attchmts = doc.getAttachments();
		if(attchmts!=null)
		for (Attachment attachment : attchmts) {
			ArrayList<Object> newRow= new ArrayList<Object>();
			newRow.add(attachment);
			atm.insertRow(newRow);
		}
		attachmentTable.setTransferHandler(new TransferHandler(){
       	 public boolean canImport(TransferHandler.TransferSupport support) {
       		 try{
                if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)||support.isDataFlavorSupported(new DataFlavor("text/rtf; class=java.io.InputStream"))||support.isDataFlavorSupported(new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16"))) {
                
                }else{
               	 DataFlavor []dfs=support.getDataFlavors();
               	 System.out.println("\n\nTotal MimeTypes Supported by this operation :"+dfs.length);
               	 for(DataFlavor df:dfs){
               		 System.out.println(df.getMimeType());
               	 }
               	 return false;
                }
                boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;
                if (!copySupported) {
                    return false;
                }
                support.setDropAction(COPY);
                return true;
       		 }catch(Exception e){
       			 e.printStackTrace();
       			 return false;
       		 }
           }

           private void displayDropLocation(final String string) {
               SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       JOptionPane.showMessageDialog(null, string);
                   }
               });
           }

           @SuppressWarnings("unchecked")
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
                   for (File f : l) {
                   	System.err.println(f.getName());
                   	Attachment attchment=new Attachment();
                   	attchment.setTitle(f.getName());
                   	attchment.setContent(getBytesFromFile(f));
                   	attchment.setDateCreated(new Date());
                   	attchment.setDocument(doc);
                   	attchment.setExt(getExtension(f));
                   	attchment.setLength(f.length());
                   	attchment=docService.saveAttachment(attchment);

                   	AttachmentTableModel atm = ((AttachmentTableModel)attachmentTable.getModel());
        			ArrayList<Object> newRow= new ArrayList<Object>();
        			newRow.add(attchment);
        			atm.insertRow(newRow);
                   }
                   return true;
               } catch (UnsupportedFlavorException e) {
                   return false;
               } catch (IOException e) {
                   return false;
               }
               }
              else if(t.isDataFlavorSupported(new DataFlavor("text/rtf; class=java.io.InputStream")))
               {
            	   System.err.println("Import possible .. rtf");
               try {
               	   DataFlavor []dfs={new DataFlavor("text/rtf; class=java.io.InputStream")};
                //	df.s
                   InputStream in =(InputStream) t.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
                //  System.out.println(out);
                   File f=File.createTempFile("test",".doc");
                   FileOutputStream fo=new FileOutputStream(f);
                   copy(in, fo);
                //  fo.write(out.getBytes());
                   fo.close();
//                  fileListerWorker.getFileListQueue().add(f);
                   System.out.println(f.getAbsolutePath());
                  /*for (File f : l) {
                   	fileListerWorker.getFileListQueue().add(f);
                   	System.err.println(f.getName());
                   }*/
                   return true;
               } catch (UnsupportedFlavorException e) {
               	System.out.println("UnsupportedFlavorException");
                 //  return false;
               } catch (IOException e) {
               	System.out.println("IOException");
                 //  return false;
               } catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
               }
               else if(t.isDataFlavorSupported(new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16")))
               {
            	   System.err.println("Import possible .. html");
               try {
               	 DataFlavor []dfs={new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16")};
                   InputStream in =(InputStream) t.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
                   File f=File.createTempFile("test",".html");
                   FileOutputStream fo=new FileOutputStream(f);
                   copy(in, fo);
                   fo.close();
                   System.out.println(f.getAbsolutePath());
                   return true;
               } catch (UnsupportedFlavorException e) {
               	System.out.println("UnsupportedFlavorException");
               } catch (IOException e) {
               	System.out.println("IOException");
               } catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
               }else{
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
           	   final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor/*,uriListFlavor*/};
               JTable table = (JTable)c;
               int []selectedRows=table.getSelectedRows();
               final List<File> files=new java.util.ArrayList<File>();
               for(int selectedRow:selectedRows){
            	AttachmentTableModel atm = ((AttachmentTableModel)attachmentTable.getModel());
           	   	Attachment attch=(Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(selectedRow)).get(0);
           	   	File temp=new File("Temp");
        		temp.mkdir();
        		File nf=new File(temp,attch.getTitle());
        		if(!nf.exists()){
        			try{
           			FileOutputStream fos = new FileOutputStream(nf);
           			fos.write(attch.getContent());
           			fos.close();
           			nf.deleteOnExit();
        			}catch (Exception e) {
						e.printStackTrace();
					}
        		}
        		files.add(nf);
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
           //    displayDropLocation("Operation not completed Yet.");
               return null;
           }
       
           public void exportDone(JComponent c, Transferable t, int action) {
				if (action == MOVE) {/*
					JTable table = (JTable) c;
					int[] selectedRows = table.getSelectedRows();
					ArrayList<Object> selectedRowsData = new ArrayList<Object>();
					for (int selectedRow : selectedRows) {
						ArrayList request=tableModel.getRow(selectedRow);
		                ConversionRequest requestRef=(ConversionRequest) request.get(4);
						if (requestRef.getStatus().equals(ConversionRequest.Status.SUCCESS)||requestRef.getStatus().equals(ConversionRequest.Status.FAILURE)) {
							selectedRowsData.add(tableModel.getRow(selectedRow));
						}
					}
					tableModel.deleteRows(selectedRowsData);
               */}
           }
	     });
		jtp.addTab("Attachments", new JScrollPane(attachmentTable));
		jtp.setPreferredSize(new Dimension(200, 100));
		ekitCore.setPreferredSize(new Dimension(500,600));
		jsp=new JSplitPane(JSplitPane.VERTICAL_SPLIT,ekitCore,jtp);
		jsp.setDividerSize(5);
		jsp.setDividerLocation(1.0);
		c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        getContentPane().add(jsp, c);
        setPreferredSize(AppSettings.getInstance().getDocumentEditorLastDim());
        setLocation(AppSettings.getInstance().getDocumentEditorLocation());
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {
		    	if(isDocumentModified()){
		    		System.err.println("document is modified..");
		    		Object[] options = {"Yes, please",
                    "Do Not !"};
					int n = JOptionPane.showOptionDialog(DocumentEditor.this,
					    "Do you want to save the contents ?",
					    "Unsaved changes .. ",
					    JOptionPane.YES_NO_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,     //do not use a custom Icon
					    options,  //the titles of buttons
					    options[0]); //default button title
					if(n==JOptionPane.YES_OPTION){
						try{
							saveDocument();
						}catch (OptimisticLockException e) {
							JOptionPane.showMessageDialog(DocumentEditor.this, "Document updated in parallel.", "update conflict", JOptionPane.ERROR_MESSAGE);
							return;
						}catch (Exception e) {
							JOptionPane.showMessageDialog(DocumentEditor.this, "Error saving the document."+e.getMessage(), "Error saving!", JOptionPane.ERROR_MESSAGE);
							return;
						}
						doc=null;
						docService=null;
					}
		    	}
		        AppSettings.getInstance().setDocumentEditorLocation(getLocationOnScreen());
		        AppSettings.getInstance().setDocumentEditorLastDim(DocumentEditor.this.getSize());
				DocumentEditor.this.doc=null;
				DocumentEditor.this.docService=null;
				DocumentEditor.this.currentMD5=null;
				DocumentEditor.this.ekitCore=null;
				DocumentEditor.this.attachmentTable=null;
				DocumentEditor.this.jsp=null;
				DocumentEditor.this.textField=null;
				dispose(); 
		    }
		});

		 ActionListener actionListener = new ActionListener() {
	     public void actionPerformed(ActionEvent actionEvent) {
				if (isDocumentModified()) {
					try {
						saveDocument();
					} catch (OptimisticLockException e) {
						JOptionPane.showMessageDialog(DocumentEditor.this,
								"Document updated in parallel.",
								"update conflict", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(DocumentEditor.this,
								"Error saving the document." + e.getMessage(),
								"Error saving!", JOptionPane.ERROR_MESSAGE);
					}
				}
	     }
		 };
		 keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK , false);
		 textField.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	public void saveDocument() throws OptimisticLockException,Exception{
		System.out.println("saving document..  "+textField.getText());
        doc.setTitle(textField.getText());
        doc.setContent(ekitCore.getDocumentText().getBytes()); 
        doc.setMd5(currentMD5);
        doc.setDateUpdated(new Date());
    	try {
    		Document tmpDoc = docService.saveDocument(doc);
    		/*BeanUtilsBean.setInstance(new BeanUtilsBean2());
    		ConvertUtils.deregister();*/
    		DateLocaleConverter converter = new DateLocaleConverter();
    		converter.setLenient(true);
    		ConvertUtils.register(converter, java.util.Date.class);
    		BeanUtils.copyProperties(doc, tmpDoc);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw e;
		}catch(OptimisticLockException ole){
			throw ole;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public boolean isDocumentModified(){
		if((!everEdited)&&doc.getTitle().equals(textField.getText()))
			return false;
		currentMD5=getMD5(ekitCore.getDocumentText());
		return !(currentMD5.equals(doc.getMd5())&&doc.getTitle().equals(textField.getText()));
	}
	static void copy(InputStream is, OutputStream os) throws IOException {
		byte buffer[] = new byte[8192];
		int bytesRead;
		while ((bytesRead = is.read(buffer)) != -1) {
			System.out.println(bytesRead);
			os.write(buffer, 0, bytesRead);
		}
		os.flush();
		os.close();
	}
	public static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);
	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    byte[] bytes = new byte[(int)length];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }
	    is.close();
	    return bytes;
	}

public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
	//1. Create the frame.
	JFrame frame = new JFrame("FrameDemo");
	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	//2. Optional: What happens when the frame closes?
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	Document doc=new Document();
	doc.setId(1L);
	try {
		doc=StaticDaoFacade.getInstance().getDocument(doc);
	} catch (RemoteException e) {
		e.printStackTrace();
	}
	DocumentEditor.showEditor(doc, StaticDaoFacade.getInstance(), frame);
	/*net.sf.memoranda.ui.htmleditor.HTMLEditor editor=new HTMLEditor();
	editor.editor.setAntiAlias(true);
//	editor.initEditor();
	editor.editToolbar.setFloatable(true);
	// editor.editor.enableInputMethods(false);
	// editor.editor.getInputContext().selectInputMethod(Locale.getDefault());
	titleField.addKeyListener(new KeyListener() {

		public void keyPressed(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				editor.editor.requestFocus();
		}

		public void keyReleased(KeyEvent arg0) {
		}

		public void keyTyped(KeyEvent arg0) {
		}
	});
	//HTMLFileExport
//	editor.editor.setEditable(false);
	editor.editToolbar.setVisible(true);
	frame.getContentPane().add(editor, BorderLayout.CENTER);
//	com.hexidec.ekit.EkitCore ec=new EkitCore();
//	frame.getContentPane().add(ec, BorderLayout.CENTER);
	//4. Size the frame.
	frame.setPreferredSize(new Dimension(500, 500));
	frame.pack();

	//5. Show it.
	frame.setVisible(true);
	while(true)
	try {
		TimeUnit.SECONDS.sleep(5);
		System.err.println(editor.document.getText(0, editor.document.getLength()));
		System.err.println(editor.getContent());
	} catch (BadLocationException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}*/

}
public static String RTFFileExport(javax.swing.text.Document doc) {
    RTFEditorKit kit = new RTFEditorKit();   
    DefaultEditorKit dek=new DefaultEditorKit();
    try {        
    	ByteArrayOutputStream baos=new ByteArrayOutputStream();
    	dek.write(baos, (DefaultStyledDocument)doc, 0, doc.getLength());
        return baos.toString();
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }
    return null;
}
public static String getExtension(File f) {
	String extension = "";
	int dotPos = f.getName().lastIndexOf(".");
	if(dotPos!=-1)
		extension = f.getName().substring(dotPos);
	return extension;
}
private class DocumentEditMousListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if(e.getClickCount()==2){
        	if(editable){
        		editable=false;
        		setTitle(doc.getId()+"-"+ doc.getTitle().substring(0, doc.getTitle().length()>20?20:doc.getTitle().length())+" ... [ "+doc.getAccessCount()+" .. "+doc.getDateAccessed()+" ]");
        		ekitCore.getTextPane().setEditable(editable);
        	}
        	else{
        		editable=true;
        		everEdited=true;
        		setTitle(doc.getId()+"-"+ doc.getTitle().substring(0, doc.getTitle().length()>20?20:doc.getTitle().length())+" ... [ "+doc.getAccessCount()+" .. "+doc.getDateAccessed()+" ]..editing");
        		ekitCore.getTextPane().setEditable(editable);
        		ekitCore.getTextPane().getCaret().setVisible(true);
        	}
        }
    }
}
}
