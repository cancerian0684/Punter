package com.sapient.punter.utils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
 
public class TextAreaFIFO extends JTextArea implements DocumentListener,TextAreaFIFOMBean
{
	private int lineBufferSize=5000;
	private boolean followTails=true;
	private MouseClickListener listener;
	@Override
	public void setDocument(Document doc) {
		super.setDocument(doc);
		getDocument().addDocumentListener( this );
		listener=new MouseClickListener();
		addMouseListener(listener);
	}
	public TextAreaFIFO()
	{
		getDocument().addDocumentListener( this );
		listener=new MouseClickListener();
		addMouseListener(listener);
		/*MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this,new ObjectName("punter.log.mbean:type=Punter-LogTextArea"+Thread.currentThread().getId()));
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}*/
	}
	
	public void insertUpdate(DocumentEvent e)
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				removeLines();
				if(followTails)
				setCaretPosition(getDocument().getLength() );
			}
		});
	}
 
	public void removeUpdate(DocumentEvent e) {}
	public void changedUpdate(DocumentEvent e) {}
 
	public void removeLines()
	{
		Element root = getDocument().getDefaultRootElement();
 
		while (root.getElementCount() > lineBufferSize)
		{
			Element firstLine = root.getElement(0);
 
			try
			{
				getDocument().remove(0, firstLine.getEndOffset());
				
			}
			catch(BadLocationException ble)
			{
				System.out.println(ble+" = "+lineBufferSize);
			}
		}
	}
 
	public static void main(String[] args)
	{
		final TextAreaFIFO textArea = new TextAreaFIFO();
		textArea.setRows(7);
		textArea.setColumns(40);
		JScrollPane scrollPane = new JScrollPane( textArea );
 
		final Timer timer = new Timer(10, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				textArea.append( new Date().toString() + "\n");
			}
		});
 
		JButton start = new JButton( "Start" );
		start.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				timer.start();
			}
		});
 
		JButton stop = new JButton( "Stop" );
		stop.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				timer.stop();
			}
		});
 
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( start, BorderLayout.NORTH );
		frame.getContentPane().add( scrollPane );
		frame.getContentPane().add( stop, BorderLayout.SOUTH );
		frame.pack();
		frame.setVisible(true);
	}
	
	private class MouseClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
        /*    System.out.println("TitleMouseListener");
            System.out.println("is consumed: " + e.isConsumed() + ", click count: " + e.getClickCount() + ", right button: "
                               + (e.getButton() != MouseEvent.BUTTON3));
        */    if(e.getClickCount()==1){
            	if(followTails){
            		followTails=false;
            	}
            	else{
            		followTails=true;
            	}
            }
        }
    }

	@Override
	public int getLineBufferSize() {
		return lineBufferSize;
	}

	@Override
	public void setLineBufferSize(int lineBufferSize) {
		if(lineBufferSize>0)
			this.lineBufferSize=lineBufferSize;
	}
}