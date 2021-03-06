package org.shunya.punter.utils;

import com.sun.mail.smtp.SMTPMessage;
import org.shunya.PunterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DevEmailService {
    private static final Logger logger = LoggerFactory.getLogger(DevEmailService.class);

    private static DevEmailService emailService;
    private final Properties properties;

    public static DevEmailService getInstance() {
        if (emailService == null) {
            emailService = new DevEmailService();
        }
        return emailService;
    }

    public static void main(String[] args) {
        DevEmailService.getInstance().sendEmail("Hi-Munish Test Email", "cancerian0684@gmail.com", "<h3>Hello world</h3> this is a test email", Collections.<File>emptyList());
    }

    private DevEmailService() {
        properties = new Properties();
        try {
            properties.load(DevEmailService.class.getClassLoader().getResourceAsStream("resources/devMail.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(String subject, String commaSeparatedRecipients, String body, List<File> attachments) {
        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
                    }
                });

        try {
            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress(properties.getProperty("mail.sender")));
            String[] recipients = commaSeparatedRecipients.split("[,;]");
            InternetAddress[] addresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                if (!recipients[i].trim().isEmpty())
                    addresses[i] = new InternetAddress(recipients[i]);
            }
            message.addRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(subject);
//            message.setHeader("Content-Type", "text/html; charset=UTF-8");
//            message.setText( body, "UTF-8", "html" );
//            message.setContent(body, "text/html");
            message.saveChanges();

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setHeader("Content-Type", "text/html; charset=UTF-8");
//            messageBodyPart.setContent(body, "text/html");
            messageBodyPart.setText(body, "UTF-8", "html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            // Part two is attachment
            for (File file : attachments) {
                if (file == null || !file.exists())
                    continue;
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                messageBodyPart.setFileName(file.getName());
                multipart.addBodyPart(messageBodyPart);
            }
            // Put parts in message
            message.setContent(multipart);
            message.setSendPartial(true);
//            Transport transport = session.getTransport("smtps");
//            transport.connect(properties.getProperty("mail.smtp.host"), properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"));
//            transport.sendMessage(message, message.getAllRecipients());
//            transport.close();
            Transport.send(message);
            logger.info("Email sent successfully....");
        } catch (MessagingException mex) {
            logger.error("Error sending the email", mex);
            throw new RuntimeException(mex);
        }
    }
}