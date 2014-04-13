package com.shunya.punter.utils;

import com.sun.mail.smtp.SMTPMessage;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class EmailServiceWithAuth {
	private static EmailServiceWithAuth emailService;
	private final Properties properties;
	private  final Authenticator auth;
	public static EmailServiceWithAuth getInstance(String username,String password){
		emailService=new EmailServiceWithAuth(username,password);
		return emailService;
	}
	private EmailServiceWithAuth(String username,String password){
	      properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", "");
	      properties.setProperty("mail.smtp.auth", "true");
	      properties.setProperty("name", username);
	      properties.setProperty("password", password);

	      auth = new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(properties.getProperty("name"), properties.getProperty("password"));
          }
	      };
	}
	public void sendEMail(String subject, String commaSeparatedRecipients,String body) {
	      // Get the default Session object.
	      Session session = Session.getInstance(properties,auth);
	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);
	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress("punter@ubs.com"));
	         String[] recipients = commaSeparatedRecipients.split(",");
	         InternetAddress[] addresses =new InternetAddress[recipients.length];
	         for (int i = 0; i < recipients.length; i++) {
	        	 addresses[i]=new InternetAddress(recipients[i]);
			}
//	         message.addRecipient(Message.RecipientType.TO,new InternetAddress("anu.sharmb@ubs.com"));
	         message.addRecipients(Message.RecipientType.TO,addresses);
	         // Set Subject: header field
	         message.setSubject(subject);
	         // Now set the actual message
	         message.setText(body);
	         // Send message
	         Transport transport = session.getTransport("smtp");
             transport.connect(properties.getProperty("name"),properties.getProperty("password"));
	         Transport.send(message);
//	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}
	public void sendEMail(String subject, String commaSeparatedRecipients,String body,String[] attachments,String fromAddress,String ccAddress) {
	      // Get the default Session object.
	      Session session = Session.getInstance(properties,auth);
	      try{
	    	 SMTPMessage message = new SMTPMessage(session);
	         fromAddress=fromAddress==null?"punter@ubs.com":fromAddress;
	         message.setFrom(new InternetAddress(fromAddress));
	         String[] recipients = commaSeparatedRecipients.split("[,;]");
	         InternetAddress[] addresses =new InternetAddress[recipients.length];
	         for (int i = 0; i < recipients.length; i++) {
	        	 addresses[i]=new InternetAddress(recipients[i]);
			 }
	         message.addRecipients(Message.RecipientType.TO,addresses);
	         if(ccAddress!=null && !ccAddress.isEmpty()){
		         String[] ccRecipients = ccAddress.split("[,;]");
		         InternetAddress[] ccAddresses =new InternetAddress[ccRecipients.length];
		         for (int i = 0; i < ccRecipients.length; i++) {
		        	 ccAddresses[i]=new InternetAddress(ccRecipients[i]);
				 }
		         message.addRecipients(Message.RecipientType.CC,ccAddresses);
	         }
	         message.setSubject(subject==null?"":subject);
	         MimeBodyPart messageBodyPart = new MimeBodyPart();
	         messageBodyPart.setContent(body, "text/html");
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
	         // Part two is attachment
	         for (String file : attachments) {	
	        	 if(file==null||file.isEmpty()||!new File(file).exists())
	        		 continue;
	        	 messageBodyPart = new MimeBodyPart();
	        	 messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
	        	 messageBodyPart.setFileName(new File(file).getName());
	        	 multipart.addBodyPart(messageBodyPart);
			}
	         // Put parts in message
	         message.setContent(multipart);
	         message.setSendPartial(true);
	         // Send message
	         Transport transport = session.getTransport("smtp");
	         transport.connect(properties.getProperty("name"),properties.getProperty("password"));
	         Transport.send(message);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	         throw new RuntimeException(mex);
	      }
	}
	public static void main(String[] args) {
		String[] attachments=new String[]{"c:/workbook.xls","c:/fund_feeder.xls"};
		EmailServiceWithAuth.getInstance("chandemu","Dare12dream$").sendEMail("Hi Test mail", "munish.chandel@ubs.com", "Hi<br/>This is Munish", attachments, "Punter@gmail.com","munish.chandel@ubs.com");
	}
}
