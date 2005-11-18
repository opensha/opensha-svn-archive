package org.opensha.refFaultParamDb.gui.infotools;

import java.net.URL;
import java.net.URLConnection;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * <p>Title: ConnectToEmailServlet.java </p>
 * <p>Description: Connect to email servlet to email whenever an addition/deletion/update
 * is done to the database. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ConnectToEmailServlet {
  private final static String SERVLET_ADDRESS = "http://gravity.usc.edu:8080/UCERF/servlet/EmailServlet";

  /**
   * Send email to database curator whenever a data is addded/removed/updated
   * from the database.
   *
   * @param message
   */
  public final static void sendEmail(String message) {
    try {
      URL emailServlet = new URL(SERVLET_ADDRESS);

      URLConnection servletConnection = emailServlet.openConnection();

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);
      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches(false);
      servletConnection.setDefaultUseCaches(false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty("Content-Type",
                                           "application/octet-stream");
      ObjectOutputStream toServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());
      //sending the email message
      toServlet.writeObject(message);
      toServlet.flush();
      toServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
      // from the servlet after it has received all the data
      ObjectInputStream fromServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      String outputFromServlet = (String) fromServlet.readObject();
      fromServlet.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
}