package org.scec.sha.earthquake.PEER_TestCases.PEER_TestGuiPlots;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: PEER_InputFilesServlet </p>
 * <p>Description: This servlet is needed whenever the files are input from the
 * web using the Applet for inputting the data for PEER test cases</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @date Dec 17 2002
 * @version 1.0
 */


public class PEER_InputFilesServlet extends HttpServlet {

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try {
       System.out.println("initialized to upload file");
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      // read the filename from applet
      String fileName =  (String) inputFromApplet.readObject();
      // read the data
      Vector data  = (Vector) inputFromApplet.readObject();

      inputFromApplet.close();

      System.out.println("filename to upload:"+fileName);

      FileWriter file = new FileWriter("GroupTestDataFiles/"+fileName);
      BufferedWriter oBuf= new BufferedWriter(file);
      // now read all the points from function and put into file
      int num = data.size();
      System.out.println("num of points:"+num);
      for(int i=0;i<num;++i)
        oBuf.write(data.get(i)+"\n");
      oBuf.close();

      // now update the files.log file to reflect the newly added file
      FileWriter logFile = new FileWriter("GroupTestDataFiles/files.log",true);
      logFile.write(fileName+"\n");
      logFile.close();

      // add this file to the JAR also
       Process p=Runtime.getRuntime().exec("jar uf /export/home/scec-00/scecweb/jsdk2.1/webpages/PEER.jar GroupTestDataFiles");
       p.waitFor();
       System.out.println("::PEER.jar updated");
       Runtime.getRuntime().exec("rm GroupTestDataFiles/"+fileName);

      // report to the user whether the operation was successful or not
      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
      outputToApplet.writeObject(new String("Success"));
      outputToApplet.close();
    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
  }


  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // call the doPost method
    doGet(request,response);
  }

}
