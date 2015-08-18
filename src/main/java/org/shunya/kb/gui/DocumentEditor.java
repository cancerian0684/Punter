package org.shunya.kb.gui;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.hibernate.StaleStateException;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.shunya.kb.model.Attachment;
import org.shunya.kb.model.Document;
import org.shunya.kb.utils.TextCompletionHandler;
import org.shunya.kb.utils.WordService;
import org.shunya.punter.gui.AppSettings;
import org.shunya.punter.gui.GUIUtils;
import org.shunya.punter.gui.PunterGUI;
import org.shunya.server.component.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.persistence.OptimisticLockException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DocumentEditor extends JFrame {
    private final Logger logger = LoggerFactory.getLogger(DocumentEditor.class);
    private final MyUndoableEditListener undoableEditListener = new MyUndoableEditListener();
    protected JTextField textField;
    private JTextPane jTextPane;
    private JTextArea jTextPaneForEditing;
    private Document doc;
    private DBService dbService;
    private JTable attachmentTable;
    private String currentMD5;
    private boolean mayBeEdited = false;
    private static BufferedImage idleImage;

    //undo helpers
    protected UndoAction undoAction;
    protected RedoAction redoAction;
    protected UndoManager undo = new UndoManager();

    static {
        try {
            idleImage = ImageIO.read(PunterGUI.class.getResource("/images/punter_discnt.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final PegDownProcessor markdown4jProcessor = new PegDownProcessor(Extensions.ALL);
//    private final Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();

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

    public static void showEditor(Document doc, DBService docService, WordService wordService) {
        DocumentEditor editor = new DocumentEditor(doc, docService, wordService);
        try {
            editor.setTitle(doc.getId() + "-" + doc.getTitle().substring(0, doc.getTitle().length() > 40 ? 40 : doc.getTitle().length()));
            editor.textField.setText(doc.getTitle());
            editor.jTextPane.setText(editor.markdown4jProcessor.markdownToHtml(new String(doc.getContent(), "UTF-8")));
            editor.jTextPane.setCaretPosition(0);
        } catch (IOException e) {
            e.printStackTrace();
            editor.jTextPane.setText("unable to parse the markdown syntax");
        }
        editor.pack();
        editor.setVisible(true);
        editor.jTextPane.requestFocus();
    }

    protected void addBindings() {
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        undo.setLimit(500);
        jTextPaneForEditing.getDocument().addUndoableEditListener(undoableEditListener);

        InputMap inputMap = jTextPaneForEditing.getInputMap();

        //Ctrl-z for undo
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
        inputMap.put(key, undoAction);

        //Ctrl-y for redo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
        inputMap.put(key, redoAction);
    }

    public DocumentEditor(final Document ldoc, DBService docServic, WordService wordService) {
        setAlwaysOnTop(false);
        setIconImage(idleImage);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.doc = ldoc;
        this.dbService = docServic;
        jTextPane = new JTextPane();
        jTextPane.setContentType("text/html");
        jTextPane.setMargin(new Insets(5, 5, 5, 5));
        jTextPane.setEditable(false);
        final HTMLEditorKit kit = new HTMLEditorKit();
        jTextPane.setEditorKit(kit);
        jTextPane.setContentType(kit.getContentType());
        jTextPane.getCaret().setVisible(false);
        setHtmlDocFont();

        jTextPaneForEditing = new JTextArea();
        jTextPaneForEditing.setMargin(new Insets(5, 5, 5, 5));
        jTextPaneForEditing.setWrapStyleWord(true);
        jTextPaneForEditing.setLineWrap(true);
        jTextPaneForEditing.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, AppSettings.getInstance().getEditorEditSize()));
        jTextPaneForEditing.setText(new String(ldoc.getContent()));

        new TextCompletionHandler(jTextPaneForEditing, dbService, wordService);

        addBindings();

      /* try {
            File cssfile = new File("src/resources/punter.css");
//            URL cssfileUrl = this.getClass().getResource("src/resources/punter.css");
            StyleSheet styleSheet = kit.getStyleSheet();
            styleSheet.importStyleSheet(cssfile.toURI().toURL());
//            styleSheet.importStyleSheet(cssfileUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/

//        jTextPane.addMouseListener(new DocumentEditMousListener());


        textField = new JTextField(20);
        textField.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 12));
        textField.setPreferredSize(new Dimension(textField.getWidth(), 30));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(textField, c);
        final JTabbedPane jtp = new JTabbedPane();
        jtp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
//                System.out.println("Tab: " + jtp.getSelectedIndex());
                if (jtp.getSelectedIndex() == 0) {
                    setTitle(doc.getId() + "-" + doc.getTitle().substring(0, doc.getTitle().length() > 40 ? 40 : doc.getTitle().length()));
                    jTextPane.setText(markdown4jProcessor.markdownToHtml(jTextPaneForEditing.getText()));
                } else if (1 == jtp.getSelectedIndex()) {
                    mayBeEdited = true;
                    setTitle(doc.getId() + "-" + doc.getTitle().substring(0, doc.getTitle().length() > 40 ? 40 : doc.getTitle().length()) + "...editing");
                }
            }
        });

        attachmentTable = new JTable(new AttachmentTableModel()) {
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
                        AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
                        Attachment attch = (Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow())).get(0);
                        if (Desktop.isDesktopSupported()) {
                            File temp = new File("Temp");
                            temp.mkdir();
                            File nf = new File(temp, "A_" + attch.getId() + "" + attch.getExt());
                            System.out.println("Opening up the file.." + nf.getName());
                            try {
                                if (!nf.exists()) {
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
        attachmentTable.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 11));
        attachmentTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        attachmentTable.setDragEnabled(true);
        TableCellRenderer dcr = attachmentTable.getDefaultRenderer(Long.class);
        if (dcr instanceof JLabel) {
            ((JLabel) dcr).setHorizontalAlignment(JLabel.LEFT);
        }
        GUIUtils.initilializeTableColumns(attachmentTable, AttachmentTableModel.longValues);
        InputMap imap = attachmentTable.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke("DELETE"), "table.delete");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false), "table.copy");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false), "table.paste");
        ActionMap amap = attachmentTable.getActionMap();
        amap.put("table.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (attachmentTable.getSelectedRow() != -1) {
                    System.out.println("Delete Key Pressed.");
                    int[] selectedRows = attachmentTable.getSelectedRows();
                    for (int selectedRow : selectedRows) {
                        AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
                        Attachment attch = (Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(attachmentTable.getSelectedRow())).get(0);
                        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + attch.getId() + " ?", "Confirm",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.YES_OPTION) {
                            try {
                                dbService.deleteAttachment(attch.getId());
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }
                            atm.deleteRow(selectedRow);
                        }
                    }
                }
            }
        });
        AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
        Collection<Attachment> attchmts = doc.getAttachments();
        if (attchmts != null)
            for (Attachment attachment : attchmts) {
                ArrayList<Object> newRow = new ArrayList<>();
                newRow.add(attachment);
                atm.insertRow(newRow);
            }
        attachmentTable.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                try {
                    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || support.isDataFlavorSupported(new DataFlavor("text/rtf; class=java.io.InputStream")) || support.isDataFlavorSupported(new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16"))) {

                    } else {
                        DataFlavor[] dfs = support.getDataFlavors();
                        System.out.println("\n\nTotal MimeTypes Supported by this operation :" + dfs.length);
                        for (DataFlavor df : dfs) {
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
                } catch (Exception e) {
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
                try {
                    Transferable t = support.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        System.err.println("Import possible .. file");
                        try {
                            java.util.List<File> l = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                            for (File f : l) {
                                System.err.println(f.getName());
                                Attachment attchment = new Attachment();
                                attchment.setTitle(f.getName());
                                attchment.setContent(getBytesFromFile(f));
                                attchment.setDateCreated(new Date());
                                attchment.setDocument(doc);
                                attchment.setExt(getExtension(f));
                                attchment.setLength(f.length());
                                attchment = dbService.saveAttachment(attchment);

                                AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
                                ArrayList<Object> newRow = new ArrayList<Object>();
                                newRow.add(attchment);
                                atm.insertRow(newRow);
                            }
                            return true;
                        } catch (UnsupportedFlavorException e) {
                            return false;
                        } catch (IOException e) {
                            return false;
                        }
                    } else if (t.isDataFlavorSupported(new DataFlavor("text/rtf; class=java.io.InputStream"))) {
                        System.err.println("Import possible .. rtf");
                        try {
                            DataFlavor[] dfs = {new DataFlavor("text/rtf; class=java.io.InputStream")};
                            //	df.s
                            InputStream in = (InputStream) t.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
                            //  System.out.println(out);
                            File f = File.createTempFile("test", ".doc");
                            FileOutputStream fo = new FileOutputStream(f);
                            copy(in, fo);
                            //  fo.write(out.getBytes());
                            fo.close();
                            final String newTitle = JOptionPane.showInputDialog(DocumentEditor.this, "Document Title - ", "test doc");
                            if (newTitle != null) {
                                Attachment attchment = new Attachment();

                                attchment.setTitle(newTitle);
                                attchment.setContent(getBytesFromFile(f));
                                attchment.setDateCreated(new Date());
                                attchment.setDocument(doc);
                                attchment.setExt(getExtension(f));
                                attchment.setLength(f.length());
                                attchment = dbService.saveAttachment(attchment);

                                AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
                                ArrayList<Object> newRow = new ArrayList<>();
                                newRow.add(attchment);
                                atm.insertRow(newRow);
                            }
                            f.delete();
                            System.out.println(f.getAbsolutePath());
                            return true;
                        } catch (UnsupportedFlavorException e) {
                            System.out.println("UnsupportedFlavorException");
                        } catch (IOException e) {
                            System.out.println("IOException");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (t.isDataFlavorSupported(new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16"))) {
                        System.err.println("Import possible .. html");
                        try {
                            DataFlavor[] dfs = {new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16")};
                            InputStream in = (InputStream) t.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
                            File f = File.createTempFile("test", ".html");
                            FileOutputStream fo = new FileOutputStream(f);
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
                final DataFlavor flavors[] = {DataFlavor.javaFileListFlavor/*,uriListFlavor*/};
                JTable table = (JTable) c;
                int[] selectedRows = table.getSelectedRows();
                final List<File> files = new java.util.ArrayList<File>();
                for (int selectedRow : selectedRows) {
                    AttachmentTableModel atm = ((AttachmentTableModel) attachmentTable.getModel());
                    Attachment attch = (Attachment) atm.getRow(attachmentTable.convertRowIndexToModel(selectedRow)).get(0);
                    File temp = new File("Temp");
                    temp.mkdir();
                    File nf = new File(temp, attch.getTitle() + attch.getExt());
                    if (!nf.exists()) {
                        try {
                            FileOutputStream fos = new FileOutputStream(nf);
                            fos.write(attch.getContent());
                            fos.close();
                            nf.deleteOnExit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    files.add(nf);
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
               */
                }
            }
        });
        jtp.addTab("HTML View", new JScrollPane(jTextPane));
        jtp.addTab("Editor", new JScrollPane(jTextPaneForEditing));
        jtp.addTab("Attachments", new JScrollPane(attachmentTable));
        jtp.setPreferredSize(new Dimension(200, 100));
        jTextPane.setPreferredSize(new Dimension(500, 600));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        getContentPane().add(jtp, c);
        setPreferredSize(AppSettings.getInstance().getDocumentEditorLastDim());
        setLocation(AppSettings.getInstance().getDocumentEditorLocation());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (isDocumentModified()) {
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
                    if (n == JOptionPane.YES_OPTION) {
                        try {
                            saveDocument();
                        } catch (OptimisticLockException e) {
                            JOptionPane.showMessageDialog(DocumentEditor.this, "Document updated in parallel.", "update conflict", JOptionPane.ERROR_MESSAGE);
                            return;
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(DocumentEditor.this, "Error saving the document." + e.getMessage(), "Error saving!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        doc = null;
                        dbService = null;
                    }
                }
                AppSettings.getInstance().setDocumentEditorLocation(getLocationOnScreen());
                AppSettings.getInstance().setDocumentEditorLastDim(DocumentEditor.this.getSize());
                DocumentEditor.this.doc = null;
                DocumentEditor.this.dbService = null;
                DocumentEditor.this.currentMD5 = null;
                DocumentEditor.this.jTextPane = null;
                DocumentEditor.this.jTextPaneForEditing = null;
                DocumentEditor.this.attachmentTable = null;
                DocumentEditor.this.textField = null;
                dispose();
            }
        });

        ActionListener actionListener = actionEvent -> {
            if (isDocumentModified()) {
                try {
                    saveDocument();
                } catch (StaleStateException | OptimisticLockException e) {
                    logger.error("parallel update error ", e);
                    JOptionPane.showMessageDialog(DocumentEditor.this,
                            "Document has been updated in parallel.",
                            "save Failed due to conflict", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    logger.error("Error while saving objects", e);
                    JOptionPane.showMessageDialog(DocumentEditor.this,
                            "Error saving the document." + e.getMessage(),
                            "Error saving!", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK, false);
        textField.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setHtmlDocFont() {
        Font font = new Font("Arial", Font.TRUETYPE_FONT, AppSettings.getInstance().getEditorEditSize());
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument) jTextPane.getStyledDocument()).getStyleSheet().addRule(bodyRule);
    }

    public void saveDocument() throws Exception {
        System.out.println("saving document before exit..  " + textField.getText());
        doc.setTitle(textField.getText());
        doc.setContent(jTextPaneForEditing.getText().getBytes());
        doc.setMd5(currentMD5);
        doc.setDateUpdated(new Date());
        Document tmpDoc = dbService.saveDocument(doc);
        DateLocaleConverter converter = new DateLocaleConverter();
        converter.setLenient(true);
        ConvertUtils.register(converter, java.util.Date.class);
        BeanUtils.copyProperties(doc, tmpDoc);

    }

    public boolean isDocumentModified() {
        if ((!mayBeEdited) && doc.getTitle().equals(textField.getText()))
            return false;
        currentMD5 = getMD5(jTextPaneForEditing.getText());
        return !(currentMD5.equals(doc.getMd5()) && doc.getTitle().equals(textField.getText()));
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

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, URISyntaxException {
        JFrame frame = new JFrame("FrameDemo");
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Document doc = new Document();
        doc.setId(1L);
        doc.setContent("###this is just a dummy content".getBytes());
        doc.setTitle("test title");
        /*try {
            doc = StaticDaoFacadeLocal.getInstance().getDocument(doc);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        DocumentEditor.showEditor(doc, null, new WordService());

    }

    public static String RTFFileExport(javax.swing.text.Document doc) {
        RTFEditorKit kit = new RTFEditorKit();
        DefaultEditorKit dek = new DefaultEditorKit();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dek.write(baos, (DefaultStyledDocument) doc, 0, doc.getLength());
            return baos.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getExtension(File f) {
        String extension = "";
        int dotPos = f.getName().lastIndexOf(".");
        if (dotPos != -1)
            extension = f.getName().substring(dotPos);
        return extension;
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    //This one listens for edits that can be undone.
    protected class MyUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            //Remember the edit and update the menus.
            undo.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
        }
    }
}
