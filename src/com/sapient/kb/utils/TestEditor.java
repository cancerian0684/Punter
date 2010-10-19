package com.sapient.kb.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JFrame;

import com.hexidec.ekit.EkitCore;

public class TestEditor extends JFrame{
	TestEditor(){
		EkitCore core  =new EkitCore();
		add(core);
		pack();
		setVisible(true);
		
	}
	public static void main(String[] args) {
		Properties props = new Properties();

        //try retrieve data from file
           try {
           props.load(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords"));
           System.out.println(props.getProperty("stopwords"));
           
           Scanner scanner = new Scanner(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords"));
           while (scanner.hasNextLine()) {
               String line = scanner.nextLine();
               System.out.println(line);
           }

            }

           //catch exception in case properties file does not exist

           catch(IOException e)
           {
           e.printStackTrace();
           }


//		ek.
	}
}
