package org.shunya.punter.utils;

import javax.swing.*;
import java.awt.*;

public class LogWindow extends JFrame {
	private TextAreaFIFO logArea;
	public LogWindow() {
		super("Log Console");
		logArea=new TextAreaFIFO();
		logArea.setRows(10);
		logArea.setColumns(25);
		logArea.setEditable(false);
		logArea.setFont(new Font("Arial Unicode MS",0,12));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().add(new JScrollPane(logArea));
		setLocationRelativeTo(null);
		pack();
	}
	public TextAreaFIFO getLogArea() {
		return logArea;
	}
	public void setLogArea(TextAreaFIFO logArea) {
		this.logArea = logArea;
	}
	public static void main(String[] args) {
		LogWindow lw=new LogWindow();
		lw.setVisible(true);
	}
}
