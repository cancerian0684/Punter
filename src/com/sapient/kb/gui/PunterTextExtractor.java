package com.sapient.kb.gui;

import java.io.ByteArrayInputStream;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hsmf.MAPIMessage;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PunterTextExtractor {
public static String getText(byte [] contents,String title){
	StringBuilder text=new StringBuilder();
	ByteArrayInputStream bais=new ByteArrayInputStream(contents);
	String tt=title.toLowerCase();
	try{
	if(tt.endsWith(".html")||tt.endsWith(".htm")){
		
	}
	else if(tt.endsWith(".pdf")){
		PdfReader reader = new PdfReader(bais);
		int pages=reader.getNumberOfPages()>5?5:reader.getNumberOfPages();
		for (int i = 0; i < pages; i++) {
			text.append(PdfTextExtractor.getTextFromPage(reader, i+1));
		}
	}
	else if(tt.endsWith(".txt")){
		
	}
	//APache POI
	else if(tt.endsWith(".doc")||tt.endsWith(".docx")||tt.endsWith(".xls")||tt.endsWith(".xlsx")||tt.endsWith(".ppt")||tt.endsWith(".pptx")){
		POITextExtractor oleTextExtractor = ExtractorFactory.createExtractor(bais);
		String ta=oleTextExtractor.getText();
		text.append(ta.substring(0, ta.length()>10000?10000:ta.length()));
	}
	else if(tt.endsWith(".msg")){
		MAPIMessage msg=new MAPIMessage(bais);
		text.append(msg.getSubject()+" ");
		text.append(msg.getTextBody()+" ");
	}
	System.err.println("Indexed Document : "+title);
	}catch (Exception e) {
		e.printStackTrace();
	}
	return text.toString();
}
}
