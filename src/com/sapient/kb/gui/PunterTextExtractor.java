package com.sapient.kb.gui;

import java.io.ByteArrayInputStream;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

public class PunterTextExtractor {
public static String getText(byte [] contents,String title){
	StringBuilder text=new StringBuilder();
	String tt=title.toLowerCase();
	try{
	if(tt.endsWith(".html")||tt.endsWith(".htm")){
		
	}
	else if(tt.endsWith(".pdf")){
		
	}
	else if(tt.endsWith(".txt")){
		
	}
	//APache POI
	else{
		ByteArrayInputStream bais=new ByteArrayInputStream(contents);
		POITextExtractor oleTextExtractor = ExtractorFactory.createExtractor(bais);
		String ta=oleTextExtractor.getText();
		text.append(ta.substring(0, ta.length()>10000?10000:ta.length()));
	}
	System.err.println("Indexed Document : "+title);
	}catch (Exception e) {
		e.printStackTrace();
	}
	return text.toString();
}
}
