package org.shunya.punter.utils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.util.Properties;

/** MailNotifier - a utility class to send a SMTP mail notification **/
public class MailNotifier {
    final String localhost;
    final String mailhost;
    final String mailuser;
    final String email_notify;
    protected Session session= null;

    public MailNotifier(String _localhost, String _mailhost, String _mailuser, String _email_notify) {
  localhost= _localhost;
  mailhost= _mailhost;
  mailuser= _mailuser;
  email_notify= _email_notify;
    }

    public void send(String subject, String text)  throws Exception {
  send(email_notify, subject, text);
    }
    public void send(String _to, String subject, String text)  throws Exception {
  if (session== null) {
      Properties p = new Properties();
      p.put("mail.host", mailhost);
      p.put("mail.user", mailuser);
      session = Session.getDefaultInstance(p, null);

      // Try to fake out SMTPTransport.java and get working EHLO:
      Properties properties = session.getProperties();
      String key= "mail.smtp.localhost";
      String prop= properties.getProperty(key);
      if (prop== null)   properties.put(key, localhost);
      else  System.out.println(key+ ": "+ prop);

      //session.setDebug(true);
  }
  MimeMessage msg = new MimeMessage(session);
  msg.setText(text);
  msg.setSubject(subject);
  Address fromAddr = new InternetAddress(mailuser);
  msg.setFrom(fromAddr);
  Address toAddr = new InternetAddress(_to);
  msg.addRecipient(Message.RecipientType.TO, toAddr);       
  Transport.send(msg);
  // Note: will use results of getLocalHost() to fill in EHLO domain
    }

    /**
     * Get the name of the local host, for use in the EHLO and HELO commands.
     * The property mail.smtp.localhost overrides what InetAddress would tell
     * us.
      Adapted from SMTPTransport.java
     */
    public String getLocalHost() {
  String localHostName= null;
  String name = "smtp";  // Name of this protocol
  try {
      // get our hostname and cache it for future use
      if (localHostName == null || localHostName.length() <= 0)
    localHostName =  session.getProperty("mail." + name + ".localhost");
      if (localHostName == null || localHostName.length() <= 0)
    localHostName = InetAddress.getLocalHost().getHostName();
  } catch (Exception uhex) {
  }
  return localHostName;
    }

    /** main() for testing pursposes **/
    public static void main(String args[]) {
  // Adapt to your needs:
  String localhost= "";
  String mailhost= "namail.corp.adobe.com";
  String mailuser= "munishc@adobe.com";
  String email_notify= "munishc@adobe.com";

  MailNotifier mn= new MailNotifier(localhost, mailhost, mailuser, email_notify);
  try {
      mn.send("PDFG-Sync-Client-Crash", "Test body from MailNotifier.java main()");
  } catch (Exception E) {
      System.err.println(E.toString());
  }
    }
}
