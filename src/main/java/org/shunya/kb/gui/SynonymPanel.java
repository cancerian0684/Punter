package org.shunya.kb.gui;

import org.shunya.kb.model.SynonymWord;
import org.shunya.punter.gui.SynonymTableModel;
import org.shunya.server.component.DBService;
import org.shunya.server.component.SynonymService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SynonymPanel extends JPanel {
    private JTextField searchTextField;
    private JButton jButton;
    private JTable searchResultTable;
    private DelayedQueueHandlerThread<String> punterDelayedQueueHandlerThread;
    private final DBService dbService;
    private final SynonymService synonymService;

    public SynonymPanel(DBService dbService, SynonymService synonymService) {
        this.dbService = dbService;
        this.synonymService = synonymService;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 13));
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
        punterDelayedQueueHandlerThread = new DelayedQueueHandlerThread<>(query -> populateDocumentsInTable(query));

        punterDelayedQueueHandlerThread.start();

        jButton = new JButton();
        jButton.setText(" + ");
        jButton.setOpaque(true);
        jButton.setFocusPainted(true);
        jButton.setBorderPainted(true);
        jButton.setContentAreaFilled(true);
        jButton.setPreferredSize(new Dimension(22, 32));
        jButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jButton.addActionListener(e -> {
            this.synonymService.saveWord(searchTextField.getText());
            this.synonymService.addWordsToCache(searchTextField.getText());
            updateSearchResult();
        });

        searchResultTable = new JTable(new SynonymTableModel(dbService, synonymService));
        //		 table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        searchResultTable.setShowGrid(true);
//		 table.setPreferredScrollableViewportSize(new Dimension(800, 700));
//		 table.setMinimumSize(new Dimension(600, 600));
        searchResultTable.setFillsViewportHeight(true);
        searchResultTable.setAutoCreateRowSorter(false);
        searchResultTable.setRowHeight(30);
        searchResultTable.setRowMargin(0);
        searchResultTable.setDragEnabled(true);
        searchResultTable.setIntercellSpacing(new Dimension(0, 0));
        searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultTable.setFont(new Font("Arial Unicode MS", Font.TRUETYPE_FONT, 13));
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
        for (int i = 0; i < 1; i++) {
            column = searchResultTable.getColumnModel().getColumn(i);
            if (i == 1) {
                column.setPreferredWidth(800); //second column is bigger
            } else if (i == 0) {
                column.setPreferredWidth(120);
            } else {
                column.setPreferredWidth(120);
            }
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.9;
        c.gridx = 0;
        c.gridy = 0;
        add(searchTextField, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.gridx = 1;
        c.gridy = 0;
        add(jButton, c);

        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0;      //make this component tall
        c.weightx = 0.0;
        c.weighty = 0.9;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        add(new JScrollPane(searchResultTable), c);
    }

    private void populateDocumentsInTable(String query) {
        SwingUtilities.invokeLater(() -> {
            SynonymTableModel model = (SynonymTableModel) searchResultTable.getModel();
            model.clearTable();
            List<SynonymWord> synonymWords = dbService.getSynonymWords(query);
            for (SynonymWord doc : synonymWords) {
                ArrayList<SynonymWord> docList = new ArrayList<>();
                docList.add(doc);
                model.insertRow(docList);
            }
        });
    }

    private void updateSearchResult() {
        punterDelayedQueueHandlerThread.put(searchTextField.getText().trim());
    }

}
