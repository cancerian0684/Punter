package com.sapient.punter.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Random;

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
	private volatile boolean followTails=true;
	private MouseClickListener listener;
	private static Color [] colorArray={Color.BLUE,Color.MAGENTA,Color.RED,new Color(51,132,0),new Color(51,0,153),Color.darkGray};
	private Color currentColor;
	private static int colorSeq=0;
	public int getColorSeq() {
		int tmp=colorSeq;
		colorSeq++;
		if(colorSeq>4)
			colorSeq=0;
		return tmp;
	}
	public boolean isFollowTails() {
		return followTails;
	}
	@Override
	public void setDocument(Document doc) {
		super.setDocument(doc);
		currentColor=colorArray[getColorSeq()];
		getDocument().addDocumentListener( this );
		listener=new MouseClickListener();
		addMouseListener(listener);
		if(followTails)
			setForeground(currentColor);
	}
	public TextAreaFIFO()
	{
		currentColor=colorArray[getColorSeq()];
		getDocument().addDocumentListener( this );
		listener=new MouseClickListener();
		addMouseListener(listener);
		if(followTails)
			setForeground(currentColor);
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
				if(followTails){
					setCaretPosition(getDocument().getLength());
				}
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
 
		final Timer timer = new Timer(200, new ActionListener()
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
        */    if(e.getClickCount()==2){
            	if(followTails){
            		followTails=false;
            		setForeground(Color.black);
            	}
            	else{
            		followTails=true;
            		setForeground(currentColor);
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