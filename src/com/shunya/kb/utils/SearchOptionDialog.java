package com.shunya.kb.utils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.shunya.kb.jpa.StaticDaoFacade;

public class SearchOptionDialog  extends JDialog{
	private static SearchOptionDialog sod;
	private static final List<String> categories=StaticDaoFacade.getInstance().getCategories();
	private JList optionList;
	public static SearchOptionDialog getInstance(){
		if(sod==null){
			sod=new SearchOptionDialog();
		}
		sod.setVisible(true);
		return sod;
	}
	private SearchOptionDialog() {
		setAlwaysOnTop(false);
		setTitle("Search Options");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		optionList = new JList(categories.toArray());
		optionList.setVisibleRowCount(12);
		JScrollPane optionPane = new JScrollPane(optionList);
		GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
		getContentPane().add(optionPane, c);
		pack();
		setVisible(true);
	}
	public String [] getExcludedCategories(){
		int[] arr = optionList.getSelectedIndices();
		String []stra=new String[arr.length];
		for (int i = 0; i < stra.length; i++) {
			stra[i]=categories.get(arr[i]);
		}
		return stra;
	}
	public static void main(String[] args) {
		SearchOptionDialog sod=new SearchOptionDialog();
		
	}
}
