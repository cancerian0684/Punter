package com.sapient.kb.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import net.sf.memoranda.ui.htmleditor.HTMLEditor;

import com.sapient.kb.jpa.Document;
import com.sapient.kb.jpa.StaticDaoFacade;

public class TestEditor extends JDialog{
	private static TestEditor testEditor=null;
	protected JTextField textField;
	net.sf.memoranda.ui.htmleditor.HTMLEditor editor;
	private Document doc;
	private StaticDaoFacade docService;
	public static void showEditor(Document doc,StaticDaoFacade docService,JFrame parent){
		if(testEditor==null){
		}
		testEditor=new TestEditor(parent);
		testEditor.doc=doc;
		testEditor.docService=docService;
		testEditor.editor.editor.setText(doc.getContent());
		testEditor.textField.setText(doc.getTitle());
		testEditor.setTitle(doc.getTitle().substring(0, doc.getTitle().length()>20?20:doc.getTitle().length())+" ... [ "+doc.getAccessCount()+" ]");
		testEditor.setVisible(true);
	}
	public TestEditor(JFrame parent) {
		super(parent,false);
		setAlwaysOnTop(false);
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		editor=new HTMLEditor();
		editor.editor.setAntiAlias(true);
		editor.registerSavAble(this);
//		editor.initEditor();
		editor.editToolbar.setFloatable(false);
		// editor.editor.enableInputMethods(false);
		// editor.editor.getInputContext().selectInputMethod(Locale.getDefault());
		/*titleField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_ENTER)
					editor.editor.requestFocus();
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
		});*/
		//HTMLFileExport
//		editor.editor.setEditable(false);
		GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
        textField = new JTextField(20);
		getContentPane().add(textField, c);
        textField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				  String text = textField.getText();
//			        textArea.append(text + newline);
			        textField.selectAll();

			        //Make sure the new text is visible, even if there
			        //was a selection in the text area.
//			        textArea.setCaretPosition(textArea.getDocument().getLength());
				
			}
		});
		editor.editToolbar.setVisible(true);
		c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        getContentPane().add(editor, c);
//		getContentPane().add(editor, BorderLayout.CENTER);
		
//		com.hexidec.ekit.EkitCore ec=new EkitCore();
		
//		getContentPane().add(ec, c);
		//4. Size the frame.
		setPreferredSize(new Dimension(700, 600));
		pack();

		//5. Show it.
		setVisible(true);
		
		 ActionListener actionListener = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		          int dotPosition = textField.getCaretPosition();
		          System.out.println("event called "+textField.getText());
		          doc.setTitle(textField.getText());
		          doc.setContent(editor.getContent());
//		          RTFFileExport(editor.document);
		          doc.setPlainContent(RTFFileExport(editor.document).toLowerCase().replace('\n', ' '));
		      	  docService.saveDocument(doc);
		      }
		    };
		    KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK , false);
		    textField.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
public static void main(String[] args) {
	//1. Create the frame.
	JFrame frame = new JFrame("FrameDemo");

	//2. Optional: What happens when the frame closes?
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	net.sf.memoranda.ui.htmleditor.HTMLEditor editor=new HTMLEditor();
	editor.editor.setAntiAlias(true);
//	editor.initEditor();
	editor.editToolbar.setFloatable(true);
	// editor.editor.enableInputMethods(false);
	// editor.editor.getInputContext().selectInputMethod(Locale.getDefault());
	/*titleField.addKeyListener(new KeyListener() {

		public void keyPressed(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				editor.editor.requestFocus();
		}

		public void keyReleased(KeyEvent arg0) {
		}

		public void keyTyped(KeyEvent arg0) {
		}
	});*/
	//HTMLFileExport
//	editor.editor.setEditable(false);
	editor.editToolbar.setVisible(true);
	frame.getContentPane().add(editor, BorderLayout.CENTER);
//	com.hexidec.ekit.EkitCore ec=new EkitCore();
//	frame.getContentPane().add(ec, BorderLayout.CENTER);
	//4. Size the frame.
	frame.setPreferredSize(new Dimension(500, 500));
	frame.pack();

	//5. Show it.
	frame.setVisible(true);
	while(true)
	try {
		TimeUnit.SECONDS.sleep(5);
		System.err.println(editor.document.getText(0, editor.document.getLength()));
		System.err.println(editor.getContent());
	} catch (BadLocationException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}

}
public static String RTFFileExport(javax.swing.text.Document doc) {
    RTFEditorKit kit = new RTFEditorKit();   
    DefaultEditorKit dek=new DefaultEditorKit();
    try {        
    	ByteArrayOutputStream baos=new ByteArrayOutputStream();
    	dek.write(baos, (DefaultStyledDocument)doc, 0, doc.getLength());
        return baos.toString();
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }
    return null;
}
}
