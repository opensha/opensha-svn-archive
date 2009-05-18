package org.opensha.commons.util;

import java.io.PrintStream;

import sun.net.smtp.SmtpClient;

/**
 * <p>Title: MailUtil.java </p>
 * <p>Description: Utility to send mail throough the program </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @date May 4, 2004
 * @version 1.0
 */

public final class MailUtil {

  /**
   *
   * @param host SMTP server from which mail needs to be sent
   * @param from Email prefix of sender
   * @param emailAddr email address of receiver
   * @param mailSubject Email subject
   * @param mailMessage Email body
   */
 public static void sendMail(String host, String from,
                      String emailAddr,
                      String mailSubject,
                      String mailMessage) {
   try {

     // Create a new instance of SmtpClient.
     SmtpClient smtp = new SmtpClient(host);
     // Sets the originating e-mail address
     smtp.from(from);
     // Sets the recipients' e-mail address
     smtp.to(emailAddr);
     // Create an output stream to the connection
     PrintStream msg = smtp.startMessage();
     msg.println("To: " + emailAddr); // so mailers will display the recipient's e-mail address
     msg.println("From: " + from); // so that mailers will display the sender's e-mail address
     msg.println("Subject: " + mailSubject + "\n");
     msg.println(mailMessage);

     // Close the connection to the SMTP server and send the message out to the recipient
     smtp.closeServer();
   }
   catch (Exception e) {
     e.printStackTrace();
   }
 }

}
