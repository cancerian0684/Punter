package com.sapient.punter.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.sapient.punter.test.TextSamplerDemo;

public class DocumentationDialog extends JDialog {
	private static final long serialVersionUID = -8214339161211735741L;
	private static DocumentationDialog docDialog;
	private JEditorPane editorPane = createEditorPane();
	public static void displayHelp(final String docUrl,boolean modal,JFrame parent){
		if(docDialog==null){
			docDialog=new DocumentationDialog(parent,modal);
		}
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 //Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				//"com/sapient/punter/test/TextSamplerDemoHelp.html"
				try {
				java.net.URL helpURL = TextSamplerDemo.class.getClassLoader().getResource(docUrl);
				if(helpURL!=null){
					docDialog.editorPane.setPage(helpURL);
				}else{
					System.err.println("URL is null");
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
				docDialog.pack();
				docDialog.setVisible(true);
            }
        });
	}
	private DocumentationDialog(JFrame parent,boolean modal){
		super(parent,modal);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Punter Task Documentation");
        setLayout(new GridLayout(1,0));
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(100, 100));
        add(new JScrollPane(editorPane));
        setPreferredSize(new Dimension(500, 350));
	}
	
	 private JEditorPane createEditorPane() {
	        JEditorPane editorPane = new JEditorPane();
	        editorPane.setEditable(false);
	        java.net.URL helpURL = TextSamplerDemo.class.getClassLoader().getResource("docs/docs/TextSamplerDemoHelp.html");
	        if (helpURL != null) {
	            try {
	                editorPane.setPage(helpURL);
	            } catch (IOException e) {
	            	e.printStackTrace();
	                System.err.println("Attempted to read a bad URL: " + helpURL);
	            }
	        } else {
	            System.err.println("Couldn't find file: TextSampleDemoHelp.html");
	        }

	        return editorPane;
	    }
	 private void createAndShowGUI() {
	        //Create and set up the window.
	        
	    }

	    public static void main(String[] args) {
	        //Schedule a job for the event dispatching thread:
	        //creating and showing this application's GUI.
	    	DocumentationDialog.displayHelp(null, false, null);
	    }
}
