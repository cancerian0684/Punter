package com.sapient.punter.utils;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
	private static EmailService emailService;
	private final Properties properties;
	private  final Authenticator auth;
	public static EmailService getInstance(){
		if(emailService==null){
			emailService=new EmailService();
		}
		return emailService;
	}
	private EmailService(){
	      properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", "NLDNC108PEX1.ubsw.net");
	      properties.setProperty("mail.smtp.auth", "true");
	      properties.setProperty("name", "chandemu");
	      properties.setProperty("password", "Dare11dream$");

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
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }

	}
}
