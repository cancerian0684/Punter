package com.sapient.kb.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import com.sapient.kb.jpa.Document;
import com.sapient.kb.jpa.StaticDaoFacade;
import com.sapient.punter.gui.Main;

public class TagDialog extends JDialog{
	private static TagDialog tagDialog;
	    private JScrollPane jScrollPane1;
	    private JTextArea textArea;
	    private JButton saveBtn;
	    private Document doc;
	    private StaticDaoFacade docService;
	    public static void getInstance(Document doc,StaticDaoFacade docService){
	    	if(tagDialog==null){
	    		tagDialog=new TagDialog();
	    	}
	    	tagDialog.docService=docService;
	    	tagDialog.doc=docService.getDocument(doc);
	    	tagDialog.textArea.setText(tagDialog.doc.getTag());
	    	tagDialog.setLocationRelativeTo(Main.main);
	    	tagDialog.pack();
	    	tagDialog.setVisible(true);
	    	tagDialog.textArea.requestFocus();
	    }
	    public TagDialog() {
	    	 super(Main.main,"; separated Tags");
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
	         c.gridwidth=3;
	         add(jScrollPane1,c);
	         saveBtn=new JButton("Save Tags");
	         saveBtn.setPreferredSize(new Dimension(25, 30));
	         c.fill = GridBagConstraints.HORIZONTAL;
	         c.weightx = 0.2;
	         c.gridx = 0;
	         c.gridy = 1;
	         add(saveBtn,c);
	         saveBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.err.println("saving Tags..");
					doc.setTag(textArea.getText());
					docService.saveDocument(doc);
					dispose();
				}
			});
	    	 
		}
}