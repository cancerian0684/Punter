package com.sapient.server;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hsmf.MAPIMessage;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PunterTextExtractor {

	public static String getText(byte [] contents,String title,String ext){
	StringBuilder text=new StringBuilder();
	ByteArrayInputStream bais=new ByteArrayInputStream(contents);
	String tt=title.toLowerCase();
	text.append(tt+" ");
	try{
		if(ext.isEmpty()){
			Source source = new Source(new StringReader(new String(contents)));
			TextExtractor te=new TextExtractor(source);
			text.append(te.toString());
		}
		else if(ext.equalsIgnoreCase(".txt")){
			text.append(new String(contents));
		}
		else if(ext.equalsIgnoreCase(".html")||ext.equalsIgnoreCase(".htm")){
			Source source = new Source(new StringReader(new String(contents)));
			TextExtractor te=new TextExtractor(source);
			text.append(te.toString());
		}
		else if(ext.equalsIgnoreCase(".pdf")){
			PdfReader reader = new PdfReader(bais);
			int pages=reader.getNumberOfPages()>10?10:reader.getNumberOfPages();
			for (int i = 0; i < pages; i++) {
				text.append(PdfTextExtractor.getTextFromPage(reader, i+1));
		}
		}
		//APache POI
		else if(ext.equalsIgnoreCase(".doc")||ext.equalsIgnoreCase(".docx")||ext.equalsIgnoreCase(".xls")||ext.equalsIgnoreCase(".xlsx")||ext.equalsIgnoreCase(".ppt")||ext.equalsIgnoreCase(".pptx")){
			POITextExtractor oleTextExtractor = ExtractorFactory.createExtractor(bais);
			String ta=oleTextExtractor.getText();
			text.append(ta.substring(0, ta.length()>10000?10000:ta.length()));
		}
		else if(ext.equalsIgnoreCase(".msg")){
			MAPIMessage msg=new MAPIMessage(bais);
			text.append(msg.getSubject()+" ");
			text.append(msg.getTextBody()+" ");
		}
		System.err.println("Indexed Document : "+ext);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return text.toString();
}
}