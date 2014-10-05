package org.shunya.kb.gui;

import org.shunya.kb.model.Document;
import org.shunya.punter.gui.Main;
import org.shunya.server.component.DBService;

import javax.swing.*;
import java.awt.*;

public class TagDialog extends JDialog {
    private static TagDialog tagDialog;
    private JScrollPane jScrollPane1;
    private JTextArea textArea;
    private JButton saveBtn;
    private Document doc;
    private DBService dbService;

    public static void getInstance(Document doc, DBService dbService) {
        if (tagDialog == null) {
            tagDialog = new TagDialog();
        }
        tagDialog.dbService = dbService;
        tagDialog.doc = dbService.getDocument(doc.getId());
        tagDialog.textArea.setText(tagDialog.doc.getTag());
        tagDialog.setLocationRelativeTo(Main.KBFrame);
        tagDialog.pack();
        tagDialog.setVisible(true);
        tagDialog.textArea.requestFocus();
    }

    public TagDialog() {
        super(Main.KBFrame, "; separated Tags");
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        textArea = new JTextArea();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        jScrollPane1 = new JScrollPane(textArea);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        add(jScrollPane1, c);
        saveBtn = new JButton("Save Tags");
        saveBtn.setPreferredSize(new Dimension(25, 30));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.2;
        c.gridx = 0;
        c.gridy = 1;
        add(saveBtn, c);
        saveBtn.addActionListener(e -> {
            System.err.println("saving Tags..");
            doc.setTag(textArea.getText());
            dbService.saveDocument(doc);
            dispose();
        });
    }
}