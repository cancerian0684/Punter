package com.sapient.kb.utils;

import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class Test {
public static void main(String[] args) {
	try {
		PdfReader reader = new PdfReader("E:\\HDD Backup\\Munish's PC\\Documents\\Munish resume.pdf");
//		reader.getNumberOfPages()
//		System.err.println(PdfTextExtractor.getTextFromPage(new PdfReader("E:\\HDD Backup\\Munish's PC\\Documents\\Munish resume.pdf"), 1));
	} catch (IOException e) {
		e.printStackTrace();
	}
	String name="cancerian0684@gmail.com +91-8010106513";
//	String name="+91-8010106513";
	StringBuilder sb=new StringBuilder();
	char curr;
	char prev='\0';
	for (int i = 0; i < name.length(); i++) {
		curr=name.charAt(i);
		if(!Character.isLetter(curr)&&Character.isLetter(prev)){
			System.err.println("boundary .. "+curr);
			sb.append(' ');
			if(Character.isLetterOrDigit(curr))
				sb.append(curr);
		}
		else if(Character.isLetter(curr)&&!Character.isLetter(prev)){
			System.err.println("boundary .. "+curr);
			sb.append(' ');
			if(Character.isLetterOrDigit(curr))
				sb.append(curr);
		}else{	
			if(Character.isLetterOrDigit(curr))
				sb.append(curr);
			else
				sb.append(' ');
				
		}
		prev=curr;
	}
	System.err.println(sb);
}
}
