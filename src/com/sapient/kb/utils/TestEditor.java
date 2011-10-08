package com.sapient.kb.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JFrame;

import com.hexidec.ekit.EkitCore;
import com.sapient.punter.utils.Stopwatch;

public class TestEditor extends JFrame{
	TestEditor(){
		EkitCore core  =new EkitCore();
		add(core);
		pack();
		setVisible(true);
		
	}
	public static void main(String[] args) {
		Stopwatch sw=new Stopwatch();
		sw.start();
		
		Properties props = new Properties();

        //try retrieve data from file
           try {
           props.load(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords.properties"));
           System.out.println(props.getProperty("stopwords.properties"));
           
           Scanner scanner = new Scanner(TestEditor.class.getClassLoader().getResourceAsStream("resources/stopwords.properties"));
           while (scanner.hasNextLine()) {
               String line = scanner.nextLine();
               System.out.println(line);
           }

           System.out.println(sw.getElapsedTime());
           sw.reset();
           System.out.println(sw.getElapsedTime());
            }
           //catch exception in case properties file does not exist

           catch(IOException e)
           {
           e.printStackTrace();
           }


//		ek.
	}
}
