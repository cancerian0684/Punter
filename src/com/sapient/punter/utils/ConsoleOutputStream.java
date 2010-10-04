package com.sapient.punter.utils;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
 
public class ConsoleOutputStream extends OutputStream
{
	private Document document = null;
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
	private PrintStream ps = null;
 
	public ConsoleOutputStream(Document document, PrintStream ps)
	{
		this.document = document;
		this.ps = ps;
	}
 
	public void write(int b)
	{
		outputStream.write (b);
	}
 
	public void flush() throws IOException
	{
		super.flush();
 
		try
		{
			if (document != null)
			{
				document.insertString (document.getLength (),
					new String (outputStream.toByteArray ()), null);
			}
 
			if (ps != null)
			{
				ps.write (outputStream.toByteArray ());
			}
 
			outputStream.reset ();
		}
		catch(Exception e) {}
	}
 
	public static void main(String[] args)
	{
		JTextArea textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane( textArea );
 
		JFrame frame = new JFrame("Redirect Output");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( scrollPane );
		frame.setSize(300, 600);
		frame.setVisible(true);
 
		System.setOut( new PrintStream(
			new ConsoleOutputStream (textArea.getDocument (), null), true));
		System.setErr( new PrintStream(
			new ConsoleOutputStream (textArea.getDocument (), null), true));
 
		Timer timer = new Timer(1000, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println( new java.util.Date().toString() );
				System.err.println( System.currentTimeMillis() );
			}
		});
		timer.start();
	}
}

