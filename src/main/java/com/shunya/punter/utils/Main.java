package com.shunya.punter.utils;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class UndoableTextArea extends JTextArea implements UndoableEditListener,
    KeyListener {
  private static final int MAX_LIMIT = 1000;
private UndoManager m_undoManager;

  public UndoableTextArea() {
    this(new String());
  }

  public UndoableTextArea(String text) {
    super(text);
    getDocument().addUndoableEditListener(this);
    this.addKeyListener(this);
    createUndoMananger();
//    removeUndoMananger();
    
  }

  private void createUndoMananger() {
    m_undoManager = new UndoManager();
    m_undoManager.setLimit(MAX_LIMIT);
  }

  private void removeUndoMananger() {
    m_undoManager.end();
  }

  public void undoableEditHappened(UndoableEditEvent e) {
    m_undoManager.addEdit(e.getEdit());
  }

  public void keyPressed(KeyEvent e) {
    if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown())) {
      try {
        m_undoManager.undo();
      } catch (CannotUndoException cue) {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown())) {
      try {
        m_undoManager.redo();
      } catch (CannotRedoException cue) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
  }
}

public class Main extends JFrame {
  UndoableTextArea m_undoableTextArea = new UndoableTextArea();

  public Main() {
    JScrollPane sc = new JScrollPane(m_undoableTextArea);
    getContentPane().setLayout(new BorderLayout(10, 10));
    getContentPane()
        .add(BorderLayout.NORTH, new JLabel("Press, CTRL+Z to Undo, CTRL+Y to Redo..."));
    getContentPane().add(BorderLayout.CENTER, sc);
  }

  public static void main(String[] arg) {
    Main m = new Main();
    m.setVisible(true);
    m.setSize(new Dimension(400, 300));
    m.validate();
  }
}