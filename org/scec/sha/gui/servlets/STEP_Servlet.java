package org.scec.sha.gui.servlets;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import javax.servlet.ServletException;
import java.net.URL;
import java.util.ArrayList;

import org.scec.util.FileUtils;

/**
 * <p>Title: STEP_Servlet</p>
 * <p>Description: It reads the STEP forecast file  and sends the data back
 * to the applet.
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class STEP_Servlet  extends HttpServlet {

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());
      inputFromApplet.close();
      // get the URL of the file to be read
      String urlString  = (String) inputFromApplet.readObject();
      // read the file
      ArrayList file = FileUtils.loadFile(new URL(urlString));
      // return the array list back to the forecast

      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
      outputToApplet.writeObject(file);
      outputToApplet.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // call the doPost method
    doGet(request,response);
  }
}

