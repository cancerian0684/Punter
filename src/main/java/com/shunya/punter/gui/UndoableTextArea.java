package com.shunya.punter.gui;

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

  public UndoableTextArea(int column, int rows) {
    this(new String(),rows,column);
  }

  public UndoableTextArea(String text, int rows, int column) {
    super(text,rows,column);
    getDocument().addUndoableEditListener(this);
    this.addKeyListener(this);
    createUndoMananger();
  }

  public void createUndoMananger() {
    m_undoManager = new UndoManager();
    m_undoManager.setLimit(MAX_LIMIT);
  }

  public void removeUndoMananger() {
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