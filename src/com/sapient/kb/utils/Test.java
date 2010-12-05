package com.sapient.kb.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.itextpdf.text.pdf.PdfReader;

public class Test {
public static void main(String[] args) {
	try {
		PdfReader reader = new PdfReader("E:\\HDD Backup\\Munish's PC\\Documents\\Munish resume.pdf");
//		reader.getNumberOfPages()
//		System.err.println(PdfTextExtractor.getTextFromPage(new PdfReader("E:\\HDD Backup\\Munish's PC\\Documents\\Munish resume.pdf"), 1));
	} catch (IOException e) {
		e.printStackTrace();
	}
	String name="IndiaDen cancerian0684@gmail.com +91-8010106513 munish chandel dAISy Sapient Corp. munishc,mchandel,mchand";
	StringTokenizer stk=new StringTokenizer(name, " ,");
	Set<String> lines = new HashSet<String>(1000); 
	while (stk.hasMoreTokens()) {
		String token=stk.nextToken();
		System.out.println(token);
		System.out.println(getPunterParsedSubText(token));
		lines.add(token);
		lines.addAll(getPunterParsedSubText(token));
	}
//	String name="+91-8010106513";
	
	System.err.println(getPunterParsedSubText(name));
	System.err.println(lines);
}
public static String getPunterParsedText2(String inText){
	StringTokenizer stk=new StringTokenizer(inText, " ,");
	Set<String> words = new HashSet<String>(1000); 
	while (stk.hasMoreTokens()) {
		String token=stk.nextToken();
		words.add(token);
		words.addAll(getPunterParsedSubText(token));
	}
	StringBuilder sb=new StringBuilder(10000);
	for(String word :words){
		sb.append(word+" ");
	}
	return sb.toString();
}
public static List<String> getPunterParsedSubText(String inText){
	StringBuilder sb=new StringBuilder();
	List<String> wordsList =new ArrayList<String>(5);
	char curr;
	char prev='\0';
	for (int i = 0; i < inText.length(); i++) {
		curr=inText.charAt(i);
		if(!Character.isLetter(curr)&&Character.isLetter(prev)){
//			System.err.println("boundary .. "+curr);
//			sb.append(' ');
			if(sb.length()>0)
			wordsList.add(sb.toString());
			sb.setLength(0);
			if(Character.isLetterOrDigit(curr))
				sb.append(curr);
		}
		else if(!Character.isLowerCase(curr)&&Character.isLowerCase(prev)){ 
//          System.err.println("boundary .. "+curr); 
            if(sb.length()>0) 
            wordsList.add(sb.toString()); 
            sb.setLength(0); 
            if(Character.isLetterOrDigit(curr)) 
                    sb.append(curr); 
		} 
		else if(Character.isLetter(curr)&&!Character.isLetter(prev)){
//			System.err.println("boundary .. "+curr);
//			sb.append(' ');
			if(sb.length()>0)
			wordsList.add(sb.toString());
			sb.setLength(0);
			if(Character.isLetterOrDigit(curr))
				sb.append(curr);
		}else{	
			if(Character.isLetterOrDigit(curr)){
				sb.append(curr);
			}
			else{
//				sb.append(' ');
				if(sb.length()>0)
				wordsList.add(sb.toString());
				sb.setLength(0);
			}
		}
		prev=curr;
	}
	if(sb.length()>0)
		wordsList.add(sb.toString());
	return wordsList;
}
}
