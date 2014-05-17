package com.shunya.kb.gui;

import com.shunya.kb.jpa.Attachment;
import com.shunya.kb.jpa.Document;
import com.shunya.kb.utils.WordService;
import com.shunya.punter.gui.AppSettings;
import com.shunya.punter.gui.Main;
import com.shunya.punter.utils.DevEmailService;
import com.shunya.server.component.StaticDaoFacade;
import org.apache.commons.io.IOUtils;
import org.markdown4j.Markdown4jProcessor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardWatchEventKinds.*;

public class PunterKB extends JPanel {
    private static JFrame frame;
    private JTextField searchTextField;
    private JTable searchResultTable;
    private JComboBox categoryComboBox;
    private JToggleButton andOrToggleButton = new JToggleButton("O");
    private final StaticDaoFacade docService;
    private final List<String> categories;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private DataFlavor Linux = new DataFlavor("text/uri-list;class=java.io.Reader");
    private DataFlavor plainText = new DataFlavor("text/plain; class=java.lang.String; charset=Unicode");
    private DataFlavor Windows = DataFlavor.javaFileListFlavor;
    private DataFlavor rtfDataFlavor = new DataFlavor("text/rtf; class=java.io.InputStream");
    private DataFlavor htmlDataFlavor = new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16");
    private WordService wordService;

    public void registerKeyBindings() {
//        KeyStroke.getKeyStroke("F2")
//        KeyStroke escapeKey = KeyStroke.getKeyStroke("ESCAPE");
        KeyStroke pasteKey = KeyStroke.getKeyStroke("F2");
//        KeyStroke pasteKey = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK);
        searchResultTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pasteKey, "pasteKeyEvent");
        searchResultTable.getActionMap().put("pasteKeyEvent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("CTRL+V got called.");
                getContentsFromTransferrable(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null));
            }
        });
    }

    private DelayedQueueHandlerThread<SearchQuery> punterDelayedQueueHandlerThread;

    public PunterKB(StaticDaoFacade staticDaoFacade) throws ClassNotFoundException, RemoteException, URISyntaxException {
        wordService = new WordService();
        docService = staticDaoFacade;
        docService.getDocList(new SearchQuery.SearchQueryBuilder().query("").build());
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        categories = docService.getCategories();
        searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 12));
        searchTextField.setPreferredSize(new Dimension(searchTextField.getWidth(), 30));
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
        punterDelayedQueueHandlerThread = new DelayedQueueHandlerThread<>(new DelayedQueueHandlerThread.CallBackHandler<SearchQuery>() {
            @Override
            public void process(SearchQuery query) {
                try {
                    populateDocumentsInTable((DocumentTableModel) searchResultTable.getModel(), docService.getDocList(query));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        punterDelayedQueueHandlerThread.start();
        searchResultTable = new JTable(new DocumentTableModel()) {
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                column = convertColumnIndexToModel(column);
                if (column == 1 || column == 2) {
                    if (isEditing()) {
                        getCellEditor().stopCellEditing();
                    }
                    if (e instanceof MouseEvent) {
                        JTable table = (JTable) e.getSource();
                        MouseEvent mEvent = ((MouseEvent) e);

                        if (((MouseEvent) e).getClickCount() == 1 && this.isRowSelected(row)) {
                            return false;
                        }
                        if (mEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                            Document luceneDoc = (Document) ((DocumentTableModel) table.getModel()).getRow(table.convertRowIndexToModel(table.getSelectedRow())).get(0);
                            try {
                                docService.updateAccessCounter(luceneDoc);
                            } catch (RemoteException e2) {
                                e2.printStackTrace();
                            }
                            Document doc = docService.getDocument(luceneDoc);
                            if (column == 1) {
                                if (doc.getExt().isEmpty())
                                    DocumentEditor.showEditor(doc, docService, wordService);
                                else {
                                    if (Desktop.isDesktopSupported()) {
                                        String docId;
                                        if (doc.getCategory().equalsIgnoreCase("/all/uploads")) {
                                            docId = "uploads/" + doc.getTitle();
                                            docId = docId.replaceAll(" ", "%20");
                                            try {
                                                String url = "http://" + docService.getServerHostAddress().getHostAddress() + ":"
                                                        + docService.getWebServerPort()
                                                        + "/" + docId;
                                                Desktop.getDesktop().browse(URI.create(url));
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        } else {
                                            System.out.println("Opening up the file.." + doc.getTitle());
                                            File temp = new File("Temp");
                                            temp.mkdir();
                                            File nf = new File(temp, "D_" + doc.getId() + doc.getExt());
                                            try {
                                                FileOutputStream fos = new FileOutputStream(nf);
                                                fos.write(doc.getContent());
                                                fos.close();
                                                nf.deleteOnExit();
                                                Desktop.getDesktop().open(nf);
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            String category = doc.getCategory();
                            if (column == 2) {
                                String s = (String) JOptionPane.showInputDialog(
                                        Main.KBFrame,
                                        "Select the Category:",
                                        "Choose Category",
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        categories.toArray(),
                                        category);
                                if ((s != null) && (s.length() > 0) && (!s.equals(category))) {
                                    System.err.println("updating category.");
                                    doc.setCategory(s);
                                    luceneDoc.setCategory(s);
                                    try {
                                        docService.saveDocument(doc);
                                    } catch (RemoteException e1) {
                                        e1.printStackTrace();
                                    }
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
        searchResultTable.setAutoCreateRowSorter(false);
        searchResultTable.setRowHeight(60);
        searchResultTable.setRowMargin(0);
        searchResultTable.setDragEnabled(true);
        searchResultTable.setIntercellSpacing(new Dimension(0, 0));
        searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultTable.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 11));
//		 TableCellRenderer dcr=searchResultTable.getColumn("<html><b>Document Name").getCellRenderer();
        TableCellRenderer dcr = searchResultTable.getDefaultRenderer(String.class);
        if (dcr instanceof JLabel) {
            ((JLabel) dcr).setVerticalAlignment(SwingConstants.TOP);
            ((JLabel) dcr).setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        JTableHeader header = searchResultTable.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 15));
        TableColumn column;
        for (int i = 0; i < 3; i++) {
            column = searchResultTable.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setPreferredWidth(800); //second column is bigger
            } else if (i == 0) {
                column.setPreferredWidth(120);
            } else {
                column.setPreferredWidth(120);
            }
        }
        searchResultTable.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                try {
                    if (support.isDataFlavorSupported(Linux) || support.isDataFlavorSupported(Windows) || support.isDataFlavorSupported(rtfDataFlavor) || support.isDataFlavorSupported(htmlDataFlavor)) {
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
                return importContentsIntoSystem(support);
            }

            private boolean importContentsIntoSystem(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                System.err.println("Import possible");
                Transferable transferable = support.getTransferable();
                return getContentsFromTransferrable(transferable);
            }

            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                final DataFlavor flavors[] = {Windows, Linux};
                JTable table = (JTable) c;
                int[] selectedRows = table.getSelectedRows();
                final List<File> files = new java.util.ArrayList<>();
                for (int selectedRow : selectedRows) {
                    DocumentTableModel dtm = (DocumentTableModel) searchResultTable.getModel();
                    Document doc = (Document) dtm.getRow(searchResultTable.convertRowIndexToModel(selectedRow)).get(0);
                    doc = docService.getDocument(doc);
                    //Punter Doc
                    if (doc.getExt().isEmpty()) {
                        files.add(createZipFromDocument(doc));
                    }
                    //System Doc
                    else {
                        System.out.println("Opening up the file.." + doc.getTitle());
                        File temp = new File("Temp");
                        temp.mkdir();
                        String filename = "D_" + doc.getId() + doc.getExt();
                        if (!doc.getExt().isEmpty()) {
                            filename = doc.getTitle();
                            if (filename.lastIndexOf(".") == -1) {
                                filename += doc.getExt();
                            }
                        }
                        File nf = new File(temp, filename);
                        try {
                            FileOutputStream fos = new FileOutputStream(nf);
                            fos.write(doc.getContent());
                            fos.close();
                            files.add(nf);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (files.size() > 0) {
                    Transferable transferable = new Transferable() {
                        public Object getTransferData(DataFlavor flavor) {
                            if (flavor.equals(Windows)) {
                                return files;
                            } else if (flavor.equals(Linux)) {
                                String data = "";
                                for (File file : files) {
                                    data += file.toURI() + "\r\n";
                                }
                                return new InputStreamReader(IOUtils.toInputStream(data));
                            }
                            return null;
                        }

                        public DataFlavor[] getTransferDataFlavors() {
                            return flavors;
                        }

                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return flavor.equals(Windows) || flavor.equals(Linux);
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
        categoryComboBox = new JComboBox(categories.toArray());
        categoryComboBox.setPreferredSize(new Dimension((int) categoryComboBox.getPreferredSize().getWidth(), 30));
        categoryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSearchResult();
            }
        });

        andOrToggleButton.setOpaque(true);
        andOrToggleButton.setFocusPainted(true);
        andOrToggleButton.setBorderPainted(true);
        andOrToggleButton.setContentAreaFilled(true);
        andOrToggleButton.setPreferredSize(new Dimension(22, 32));
        andOrToggleButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        andOrToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (andOrToggleButton.isSelected()) {
                    andOrToggleButton.setText("A");
                } else {
                    andOrToggleButton.setText("O");
                }
                updateSearchResult();
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.9;
        c.gridx = 0;
        c.gridy = 0;
        add(searchTextField, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.01;
        c.gridx = 1;
        c.gridy = 0;
        add(andOrToggleButton, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
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

        final JMenuItem addProcessMenu, openDocMenu, renameMenu, deleteDocMenu, docTagsMenu, reindexDocsMenu, copyURL, pasteMenu, emailMenu;
        final JPopupMenu popupProcess = new JPopupMenu();
        addProcessMenu = new JMenuItem("Add");
        addProcessMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Adding Document");
                Document doc = null;
                try {
                    doc = docService.createDocument(docService.getUsername());
                    doc.setCategory(getSelectedCategory());
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                DocumentEditor.showEditor(doc, docService, wordService);
            }
        });
        popupProcess.add(addProcessMenu);
        openDocMenu = new JMenuItem("Open");
        openDocMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Opening Document");
                if (searchResultTable.getSelectedRow() >= 0) {
                    Document doc = (Document) ((DocumentTableModel) searchResultTable.getModel()).getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                    doc = docService.getDocument(doc);
                    if (doc.getExt().isEmpty())
                        DocumentEditor.showEditor(doc, docService, wordService);
                    else {
                        if (Desktop.isDesktopSupported()) {
                            System.out.println("Opening up the file.." + doc.getExt());
                            File temp = new File("Temp");
                            temp.mkdir();
                            File nf = new File(temp, "D_" + doc.getId() + doc.getExt());
                            try {
                                FileOutputStream fos = new FileOutputStream(nf);
                                fos.write(doc.getContent());
                                fos.close();
                                nf.deleteOnExit();
                                Desktop.getDesktop().open(nf);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        popupProcess.add(openDocMenu);

        renameMenu = new JMenuItem("Rename");
        renameMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (searchResultTable.getSelectedRow() >= 0) {
                    System.out.println("Renaming Document");
                    Document localDoc = (Document) ((DocumentTableModel) searchResultTable.getModel())
                            .getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                    Document persisted = null;
                    try {
                        persisted = docService.getDocument(localDoc);
                        final String newTitle = JOptionPane.showInputDialog(Main.KBFrame, "rename title to - ", persisted.getTitle());
                        if (newTitle != null) {
                            persisted.setTitle(newTitle);
                            docService.saveDocument(persisted);
                            localDoc.setTitle(persisted.getTitle());
                            ((DocumentTableModel) searchResultTable.getModel()).refreshTable();
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
        popupProcess.add(renameMenu);

        deleteDocMenu = new JMenuItem("Delete");
        deleteDocMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Delete Document");
                if (searchResultTable.getSelectedRow() >= 0) {
                    DocumentTableModel dtm = (DocumentTableModel) searchResultTable.getModel();
                    Document doc = (Document) dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                    if (doc.getAuthor().equalsIgnoreCase(AppSettings.getInstance().getUsername())) {
                        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.YES_OPTION) {
                            try {
                                docService.deleteDocument(doc);
                                dtm.deleteRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow()));
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "You do not have rights to delete this doc.");
                    }
                }
            }
        });
        popupProcess.add(deleteDocMenu);

        docTagsMenu = new JMenuItem("Tags");
        docTagsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Document Tags");
                if (searchResultTable.getSelectedRow() >= 0) {
                    DocumentTableModel dtm = (DocumentTableModel) searchResultTable.getModel();
                    Document doc = (Document) dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                    TagDialog.getInstance(doc, docService);
                }
            }
        });
        popupProcess.add(docTagsMenu);

        pasteMenu = new JMenuItem("Paste");
        pasteMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Paste ClipBoard Contents");
                getContentsFromTransferrable(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null));
            }
        });
        popupProcess.add(pasteMenu);

        reindexDocsMenu = new JMenuItem("Rebuild Indexes");
        reindexDocsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                System.out.println("Rebuilding Index");
                PunterKB.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try {
                    docService.rebuildIndex();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                PunterKB.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        popupProcess.add(reindexDocsMenu);
//        reindexDocsMenu.setEnabled(false);

        copyURL = new JMenuItem("Copy URL");
        copyURL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    DocumentTableModel dtm = (DocumentTableModel) searchResultTable.getModel();
                    Document doc = (Document) dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                    String docId;
                    if (doc.getCategory().equalsIgnoreCase("/all/uploads")) {
                        doc = docService.getDocument(doc);
//                        Base64.getEncoder().encode(doc.getTitle().getBytes())
                        docId = "uploads/" + doc.getTitle();
                        docId = docId.replaceAll(" ", "%20");
                    } else
                        docId = "" + doc.getId();
                    String url = "http://" + docService.getServerHostAddress().getHostAddress() + ":"
                            + docService.getWebServerPort()
                            + "/rest/punter/get/" + docId;
                    System.out.println(url);
                    StringSelection stringSelection = new StringSelection(url);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        popupProcess.add(copyURL);

        emailMenu = new JMenuItem("Email");
        emailMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();
                            DocumentTableModel dtm = (DocumentTableModel) searchResultTable.getModel();
                            Document doc = (Document) dtm.getRow(searchResultTable.convertRowIndexToModel(searchResultTable.getSelectedRow())).get(0);
                            List<File> files = new ArrayList<>();
                            File temp = new File("Temp");
                            if (!temp.exists())
                                temp.mkdir();
                            doc = docService.getDocument(doc);
                            //Punter Doc
                            if (doc.getExt().isEmpty()) {
                                Collection<Attachment> attchs = doc.getAttachments();
                                for (Attachment attch : attchs) {
                                    File nf = new File(temp, attch.getTitle());
                                    try {
                                        FileOutputStream fos = new FileOutputStream(nf);
                                        fos.write(attch.getContent());
                                        fos.close();
                                        files.add(nf);
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                final String htmlContent = markdown4jProcessor.process(new String(doc.getContent()));
                                DevEmailService.getInstance().sendEmail(doc.getTitle(), "cancerian0684@gmail.com", htmlContent, files);
                            }
                            //System Doc
                            else {
                                System.out.println("Opening up the file.." + doc.getTitle());
                                String filename = "D_" + doc.getId() + doc.getExt();
                                if (!doc.getExt().isEmpty()) {
                                    filename = doc.getTitle();
                                    if (!filename.endsWith(doc.getExt())) {
                                        filename += doc.getExt();
                                    }
                                }
                                File nf = new File(temp, filename);
                                try {
                                    FileOutputStream fos = new FileOutputStream(nf);
                                    fos.write(doc.getContent());
                                    fos.close();
                                    files.add(nf);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                DevEmailService.getInstance().sendEmail(doc.getTitle(), "cancerian0684@gmail.com", "PFA", files);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
        popupProcess.add(emailMenu);

        registerKeyBindings();
        searchResultTable.addMouseListener(new MouseAdapter() {
            //JPopupMenu popup;
            public void mousePressed(MouseEvent e) {
                if (searchResultTable.getSelectedRowCount() > 1) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        searchResultTable.clearSelection();
                        popupProcess.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    int selectedRow = searchResultTable.rowAtPoint(e.getPoint());
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (selectedRow != -1) {
                            searchResultTable.setRowSelectionInterval(selectedRow, selectedRow);
                            int actualRow = searchResultTable.convertRowIndexToModel(selectedRow);
                            openDocMenu.setEnabled(true);
                            deleteDocMenu.setEnabled(true);
                            docTagsMenu.setEnabled(true);
                        } else {
                            openDocMenu.setEnabled(false);
                            deleteDocMenu.setEnabled(false);
                            docTagsMenu.setEnabled(false);
                        }
                        popupProcess.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Path dir = Paths.get("Temp");
                    if (!Files.exists(dir)) {
                        Files.createDirectory(dir);
                        System.out.println("dir created = " + dir);
                    }
                    new WatchDir(dir, false, new WatchDir.FileObserver() {
                        @Override
                        public void notify(Path path) {
                            if (path.toFile().getName().startsWith("~") || path.toFile().getName().endsWith(".tmp"))
                                return;
                            if (path.toFile().getName().startsWith("D_")) {
                                Document doc = new Document();
                                try {
                                    System.err.println("Picked file :" + path.toFile().getName().substring(2, path.toFile().getName().lastIndexOf(".")));
                                    int id = Integer.parseInt(path.toFile().getName().substring(2, path.toFile().getName().lastIndexOf(".")));
                                    doc.setId(id);
                                    doc = docService.getDocument(doc);
                                    doc.setContent(getBytesFromFile(path.toFile()));
                                    docService.saveDocument(doc);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            } else if (path.toFile().getName().startsWith("A_")) {
                                Attachment attachment = new Attachment();
                                try {
                                    System.err.println("Picked Attachment for saving :" + path.toFile().getName());
                                    int id = Integer.parseInt(path.toFile().getName().substring(2, path.toFile().getName().lastIndexOf(".")));
                                    attachment.setId(id);
                                    attachment = docService.getAttachment(attachment);
                                    attachment.setContent(getBytesFromFile(path.toFile()));
                                    docService.saveAttachment(attachment);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            } else {
                                System.out.println("ignoring file : " + path.toFile().getAbsoluteFile());
                            }
                        }
                    }, ENTRY_MODIFY).processEvents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setName("Background.Document.Listener");
        thread.start();

        Thread uploadScanner = new Thread(new Runnable() {
            @Override
            public void run() {
                updateUploadsCache();
                Path dir = Paths.get("uploads");
                try {
                    new WatchDir(dir, false, new WatchDir.FileObserver() {
                        @Override
                        public void notify(Path path) {
                            updateUploadsCache();
                        }
                    }, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE).processEvents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        uploadScanner.start();
    }

    private void updateUploadsCache() {
        Path dir = Paths.get("uploads");
        try {
            System.out.println("Deleting all for category");
            docService.deleteAllForCategory("/all/uploads");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Scanning directory for uploads - " + dir.toFile().getAbsolutePath());
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (path.toFile().getName().startsWith("~") || path.toFile().getName().endsWith(".tmp"))
                        return CONTINUE;
                    final String fileName = path.getFileName().toString();
                    Document doc = new Document();
                    doc.setAuthor(AppSettings.getInstance().getUsername());
                    doc.setTitle(fileName);
//                    doc.setContent(fileName.getBytes());
                    doc.setExt(getExtension(path.toFile()));
                    doc.setDateCreated(new Date(path.toFile().lastModified()));
                    doc.setDateUpdated(new Date());
                    doc.setCategory("/all/uploads");
                    docService.saveDocument(doc);
                    System.err.println("Document added : " + fileName);
                    return CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getContentsFromTransferrable(Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(Windows)) {
                System.err.println("Import possible .. file");
                try {
                    List<File> l = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : l) {
                        System.err.println(file.getName());
                        Document doc = new Document();
                        doc.setAuthor(AppSettings.getInstance().getUsername());
                        doc.setTitle(file.getName());
                        doc.setContent(getBytesFromFile(file));
                        doc.setExt(getExtension(file));
                        if (getExtension(file) == null || getExtension(file).isEmpty())
                            doc.setExt(".txt");
                        doc.setDateCreated(new Date());
                        doc.setDateUpdated(new Date());
                        doc.setCategory(getSelectedCategory());
                        doc = docService.saveDocument(doc);
                        System.err.println("Document added : " + file.getName());
                    }
                    return true;
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
            } else if (transferable.isDataFlavorSupported(new DataFlavor("text/rtf; class=java.io.InputStream"))) {
                System.err.println("Import possible .. rtf");
                try {
                    String docName = JOptionPane.showInputDialog("Enter Document Name : ", "Test");
                    DataFlavor[] dfs = {new DataFlavor("text/rtf; class=java.io.InputStream")};
                    //	df.s
                    InputStream in = (InputStream) transferable.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
//                    //  System.out.println(out);
                    File f = File.createTempFile("test", ".rtf");
                    FileOutputStream fo = new FileOutputStream(f);
                    copy(in, fo);
                    //  fo.write(out.getBytes());
                    fo.close();
                    Document doc = new Document();
                    doc.setAuthor(AppSettings.getInstance().getUsername());
                    if (docName != null && !docName.isEmpty())
                        doc.setTitle(docName);
                    else
                        doc.setTitle(f.getName());
                    doc.setContent(getBytesFromFile(f));
                    doc.setCategory(getSelectedCategory());
                    doc.setExt(getExtension(f));
                    if (getExtension(f) == null || getExtension(f).isEmpty())
                        doc.setExt(".txt");
                    doc.setDateCreated(new Date());
                    doc.setDateUpdated(new Date());
                    doc = docService.saveDocument(doc);
                    System.err.println("Document added : test");
//                      fileListerWorker.getFileListQueue().add(f);
//                       System.out.println(f.getAbsolutePath());
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
            } else if (transferable.isDataFlavorSupported(new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16"))) {
                System.err.println("Import possible .. html");
                try {
                    DataFlavor[] dfs = {new DataFlavor("text/html; class=java.io.InputStream; charset=UTF-16")};
                    InputStream in = (InputStream) transferable.getTransferData(DataFlavor.selectBestTextFlavor(dfs));
                    File f = File.createTempFile("test", ".html");
                    FileOutputStream fo = new FileOutputStream(f);
                    copy(in, fo);
                    fo.close();
                    System.out.println(f.getAbsolutePath());
                    Document doc = new Document();
                    doc.setAuthor(AppSettings.getInstance().getUsername());
                    String docName = JOptionPane.showInputDialog("Enter Document Name : ", "Test");
                    if (docName != null && !docName.isEmpty())
                        doc.setTitle(docName);
                    else
                        doc.setTitle(f.getName());
                    doc.setContent(getBytesFromFile(f));
                    doc.setExt(getExtension(f));
                    if (getExtension(f) == null || getExtension(f).isEmpty())
                        doc.setExt(".txt");
                    doc.setDateCreated(new Date());
                    doc.setDateUpdated(new Date());
                    doc.setCategory(getSelectedCategory());
                    docService.saveDocument(doc);
                    System.err.println("Document added : test");
                    return true;
                } catch (UnsupportedFlavorException e) {
                    System.out.println("UnsupportedFlavorException");
                } catch (IOException e) {
                    System.out.println("IOException");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (transferable.isDataFlavorSupported(plainText)) {
                System.out.println(System.getProperty("os.name"));
                String data = (String) transferable.getTransferData(plainText);

                Document doc = new Document();
                doc.setAuthor(AppSettings.getInstance().getUsername());
                final String newTitle = JOptionPane.showInputDialog(Main.KBFrame, "Title ", "test text file");
                if (newTitle != null) {
                    doc.setTitle(newTitle);
                    doc.setContent(data.getBytes());
                    doc.setExt(".txt");
                    doc.setDateCreated(new Date());
                    doc.setDateUpdated(new Date());
                    doc.setCategory(getSelectedCategory());
                    doc = docService.saveDocument(doc);
                    System.err.println("Document added : " + newTitle);
                }
            } else if (transferable.isDataFlavorSupported(Linux)) {
//                        String data = (String) transferable.getTransferData(Linux);
                String data = IOUtils.toString((InputStreamReader) transferable.getTransferData(Linux));
                for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens(); ) {
                    String token = st.nextToken().trim();
                    if (token.startsWith("#") || token.isEmpty()) {
                        // comment line, by RFC 2483
                        continue;
                    }
                    File file = new File(new URI(token));
                    System.err.println(file.getName());
                    Document doc = new Document();
                    doc.setAuthor(AppSettings.getInstance().getUsername());
                    doc.setTitle(file.getName());
                    doc.setContent(getBytesFromFile(file));
                    doc.setExt(getExtension(file));
                    if (getExtension(file) == null || getExtension(file).isEmpty())
                        doc.setExt(".txt");
                    doc.setDateCreated(new Date());
                    doc.setDateUpdated(new Date());
                    doc.setCategory(getSelectedCategory());
                    doc = docService.saveDocument(doc);
                    System.err.println("Document added : " + file.getName());
                }
            } else {
                System.err.println("Data Flavors not supported yet :");
                DataFlavor dfs[] = transferable.getTransferDataFlavors();
                for (DataFlavor df : dfs) {
                    System.err.println(df.getMimeType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void packColumns(JTable table, int margin) {
        for (int c = 0; c < table.getColumnCount(); c++) {
            packColumn(table, c, 2);
        }
    }

    // Sets the preferred width of the visible column specified by vColIndex. The column
    // will be just wide enough to show the column head and the widest cell in the column.
    // margin pixels are added to the left and right
    // (resulting in an additional width of 2*margin pixels).
    public void packColumn(JTable table, int vColIndex, int margin) {
        TableModel model = table.getModel();
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
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
        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += 2 * margin;

        // Set the width
        col.setPreferredWidth(width);
    }

    private void populateDocumentsInTable(final DocumentTableModel searchResultTableModel, final List<Document> docs) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                searchResultTableModel.clearTable();
                for (Document doc : docs) {
                    ArrayList<Document> docList = new ArrayList<Document>();
                    docList.add(doc);
                    searchResultTableModel.insertRow(docList);
                }
            }
        });
    }

    private void updateSearchResult() {
        punterDelayedQueueHandlerThread.put(new SearchQuery.SearchQueryBuilder().query(searchTextField.getText().trim() + "*").category(getSelectedCategory()).andFilter(andOrToggleButton.isSelected()).maxResults(AppSettings.getInstance().getMaxResults()).build());
    }

    private String getSelectedCategory() {
        return categoryComboBox.getSelectedItem().toString();
    }

    public static File createZipFromDocument(Document doc) {
        try {
            // These are the files to include in the ZIP file
            Collection<Attachment> attchs = doc.getAttachments();
            // Create a buffer for reading the files
            byte[] buf = new byte[1024];
            // Create the ZIP file
            String outFilename = doc.getId() + ".zip";
            File tmpDir = new File("Temp");
            if (!tmpDir.exists())
                tmpDir.mkdirs();
            File resultFile = new File(tmpDir, outFilename);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultFile));

            out.putNextEntry(new ZipEntry(doc.getId() + ".htm"));
            // Transfer bytes from the file to the ZIP file
            out.write(doc.getContent());
            // Complete the entry
            out.closeEntry();
            // Compress the files
            for (Attachment attch : attchs) {
                out.putNextEntry(new ZipEntry(attch.getId() + attch.getExt()));
                out.write(attch.getContent());
                out.closeEntry();
            }
            // Complete the ZIP file
            out.close();
            return resultFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static String getExtension(File f) {
        String extension = "";
        int dotPos = f.getName().lastIndexOf(".");
        if (dotPos != -1)
            extension = f.getName().substring(dotPos);
        return extension;
    }

    //save last modified time then after 2 seconds poll for modified time of files. pick all files after that last scan time.
    public void indexAllTempDocs() {
        File temp = new File("Temp");
        if (temp.exists()) {
            File[] files = temp.listFiles();
            for (File file : files) {
                Document doc = new Document();
                try {
                    System.err.println("Picked file :" + file.getName().substring(1, file.getName().indexOf("_")));
                    int id = Integer.parseInt(file.getName().substring(1, file.getName().indexOf("_")));
                    doc.setId(id);
                    doc = docService.getDocument(doc);
                    doc.setContent(getBytesFromFile(file));
                    docService.saveDocument(doc);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
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

    private void createAndShowGUI() throws Exception {
        //Create and set up the window.
        frame = new JFrame("Search");
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        //Create and set up the content pane.
        frame.setContentPane(this);
        frame.pack();
        frame.setVisible(true);
    }
}