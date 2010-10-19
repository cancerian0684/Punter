package com.sapient.kb.utils;

import javax.swing.JFrame;

import com.hexidec.ekit.Ekit;
import com.hexidec.ekit.EkitCore;

public class TestEditor extends JFrame{
	TestEditor(){
		EkitCore core  =new EkitCore();
		add(core);
		pack();
		setVisible(true);
		
	}
	public static void main(String[] args) {
		Ekit ek=new Ekit();
//		ek.
	}
}
