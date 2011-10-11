package com.sapient.punter.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class TextAreaEditor extends JDialog {
    UndoableTextArea _resultArea = new UndoableTextArea(10, 10);
    EditorListener listener;
    private static TextAreaEditor instance;
    JFrame parent;
    String originalText;

    public static TextAreaEditor getInstance(String text, EditorListener listener, JFrame parent) {
        if (instance == null) {
            instance = new TextAreaEditor(parent, true);
            instance.registerKeyBindings();
        }
        instance.registerListener(listener);
        instance.setText(text);
        instance.setVisible(true);
        instance._resultArea.createUndoMananger();
        return instance;
    }

    public void registerKeyBindings() {
//        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
//        KeyStroke.getKeyStroke("F2")
        KeyStroke escapeKey = KeyStroke.getKeyStroke("ESCAPE");
        _resultArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "escapeEvent");
        _resultArea.getActionMap().put("escapeEvent", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExitAction();
            }
        });

    }

    public void registerListener(EditorListener listener) {
        this.listener = listener;
    }

    public void setText(String text) {
        _resultArea.setText(text);
        this.originalText = text;
    }

    //====================================================== constructor
    public TextAreaEditor(JFrame parent, boolean modal) {
        super(parent, modal);
        _resultArea.setText("Enter more text to see scrollbars");
        JScrollPane scrollingArea = new JScrollPane(_resultArea);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollingArea, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.01;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.01;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        this.setTitle("Input");
        setLocation(AppSettings.getInstance().getTextAreaEditorLocation());
        setPreferredSize(AppSettings.getInstance().getTextAreaEditorLastDim());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.pack();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExitAction();
            }
        });
    }

    public static void main(String[] args) {
        TextAreaEditor win = new TextAreaEditor(new JFrame(), true);
        win.setVisible(true);
    }

    public static interface EditorListener {
        public void save(String text);
    }

    private void onExitAction() {
        if (originalText.equals(_resultArea.getText())) {
            cancelTransaction();
            return;
        }
        int option = JOptionPane.showConfirmDialog(TextAreaEditor.this, "Do you want to Save Contents ?", "Confirm Save ?", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (listener != null) {
                listener.save(_resultArea.getText());
                _resultArea.setText("");
                _resultArea.removeUndoMananger();
                listener = null;
                AppSettings.getInstance().setTextAreaEditorLocation(instance.getLocation());
                AppSettings.getInstance().setTextAreaEditorLastDim(instance.getSize());
                dispose();
            }
        } else {
            cancelTransaction();
        }
    }

    private void cancelTransaction() {
        listener = null;
        _resultArea.setText("");
        AppSettings.getInstance().setTextAreaEditorLocation(instance.getLocation());
        AppSettings.getInstance().setTextAreaEditorLastDim(instance.getSize());
        dispose();
    }
}