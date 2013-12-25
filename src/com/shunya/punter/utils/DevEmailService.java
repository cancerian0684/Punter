package com.shunya.punter.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class DevEmailService {
    private static DevEmailService emailService;
    private final Properties properties;

    public static DevEmailService getInstance() {
        if (emailService == null) {
            emailService = new DevEmailService();
        }
        return emailService;
    }

    public static void main(String[] args) {
        DevEmailService.getInstance().sendEmail("Hi-Munish Test Email", "cancerian0684@gmail.com", "<h3>Hello world</h3> this is a test email");
    }

    private DevEmailService() {
        properties = new Properties();
        try {
            properties.load(DevEmailService.class.getResourceAsStream("/resources/devMail.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(String subject, String commaSeparatedRecipients, String body) {
        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(properties.getProperty("mail.sender")));
            String[] recipients = commaSeparatedRecipients.split("[,;]");
            InternetAddress[] addresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addresses[i] = new InternetAddress(recipients[i]);
            }
            message.addRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(subject);
//	         message.setText(body);
            message.setContent(body, "text/html");
            message.saveChanges();
//            Transport transport = session.getTransport("smtps");
//            transport.connect(properties.getProperty("mail.smtp.host"), properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
//            transport.sendMessage(message, message.getAllRecipients());
//            transport.close();
            Transport.send(message);
            System.out.println("Sent Expense successfully....");
        } catch (MessagingException mex) {
            throw new RuntimeException(mex);
        }
    }
}