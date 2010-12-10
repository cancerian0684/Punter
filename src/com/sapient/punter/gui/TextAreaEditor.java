package com.sapient.punter.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class TextAreaEditor extends JDialog {
   JTextArea _resultArea = new JTextArea(10,20);
   EditorListener listener;
   private static TextAreaEditor instance;
   JFrame parent;
   public static TextAreaEditor getInstance(String text,EditorListener listener,JFrame parent){
	   if(instance==null){
		   instance=new TextAreaEditor(parent,true);
	   }
	   instance.registerListener(listener);
	   instance.setText(text);
	   instance.setVisible(true);
	   return instance;
   }
   public void registerListener(EditorListener listener){
	   this.listener=listener;
   }
   public void setText(String text){
	   _resultArea.setText(text);
   }
    //====================================================== constructor
    public TextAreaEditor(JFrame parent,boolean modal) {
    	super(parent,modal);
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
        add(scrollingArea,c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.01;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        JButton btnSave=new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(listener!=null){
					listener.save(_resultArea.getText());
					_resultArea.setText("");
					listener=null;
					AppSettings.getInstance().setTextAreaEditorLocation(instance.getLocation());
					AppSettings.getInstance().setTextAreaEditorLastDim(instance.getSize());
					dispose();
				}
			}
		});
        add(btnSave,c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.01;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        JButton btnCancel=new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener=null;
				_resultArea.setText("");
				AppSettings.getInstance().setTextAreaEditorLocation(instance.getLocation());
				AppSettings.getInstance().setTextAreaEditorLastDim(instance.getSize());
				dispose();
			}
		});
        add(btnCancel,c);
        this.setTitle("Input");
        setLocation(AppSettings.getInstance().getTextAreaEditorLocation());
        setPreferredSize(AppSettings.getInstance().getTextAreaEditorLastDim());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.pack();
    }
    
    public static void main(String[] args) {
    	TextAreaEditor win = new TextAreaEditor(new JFrame(),true);
        win.setVisible(true);
    }
    public static interface EditorListener{
    	public void save(String text);
    }
}