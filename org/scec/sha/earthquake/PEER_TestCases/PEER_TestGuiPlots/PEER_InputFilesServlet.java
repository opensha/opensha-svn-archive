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

  private static String JAR_PATH = "/export/home/scec-00/scecweb/jsdk2.1/webpages/";

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try {
       System.out.println("initialized to upload/delete file");
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      // get whether the user has requested the addition/deletion of the file
      String functionDesired  = (String) inputFromApplet.readObject();
      // read the filename from applet
      String fileName =  (String) inputFromApplet.readObject();

      // if file is to be added
      if(functionDesired.equalsIgnoreCase("Add"))
        addFile(inputFromApplet, fileName);
      // if file is to be deleted
      else if(functionDesired.equalsIgnoreCase("Delete"))
        deleteFile(fileName);

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

  /**
   * to add the specified file to the jar file
   *
   * @param inputFromApplet  : Input stream from the applet
   * @param fileName : Name of the file to be added
   */
  private void addFile(ObjectInputStream inputFromApplet, String fileName) {

    try {
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
      Process p=Runtime.getRuntime().exec("jar uf "+JAR_PATH+"PEER_TestResultsPlotterApp.jar GroupTestDataFiles");
      p.waitFor();
      System.out.println("::PEER_TestResultsPlotterApp.jar updated");

      // add this file to the JAR also
      p=Runtime.getRuntime().exec("jar uf "+JAR_PATH+"PEER_TestResultsSubmApp.jar GroupTestDataFiles");
      p.waitFor();
      System.out.println("::PEER_TestResultsSubmApp.jar updated");

      Runtime.getRuntime().exec("rm GroupTestDataFiles/"+fileName);
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }
  }
  /**
   * to delete the specified filename
   * @param fileName
   */
  private void deleteFile(String fileName) {
   try {
     // now update the files.log file to reflect the removed file
     FileReader logFile = new FileReader("GroupTestDataFiles/files.log");
     LineNumberReader lin = new LineNumberReader(logFile);
     Vector fileNamesVector = new Vector();
     String str = lin.readLine();
     //str = str.replace('\n',' ');
     //str = str.trim();
     while(str!=null) {
       if(!str.equals(fileName)) fileNamesVector.add(str);
       System.out.println("str="+str+",length(str)="+str.length()+
                          "fileName="+fileName+",length(fileName)="+fileName.length()+
                          "sizeofvector="+fileNamesVector.size());
       str = lin.readLine();
       //str = str.replace('\n',' ');
       //str = str.trim();
     }
     lin.close();
     logFile.close();
     RunScript.runScript("rm GroupTestDataFiles/files.log");
     // rewrite the log file after removing the name of the removed file
     FileWriter newLogFile = new FileWriter("GroupTestDataFiles/files.log");
     int size = fileNamesVector.size();
     for(int i =0; i< size; ++i)
       newLogFile.write((String)fileNamesVector.get(i)+"\n");
     newLogFile.close();

     // remove this file from the GUI Plotter JAR also
     RunScript.runScript("jar xf "+JAR_PATH+"PEER_TestResultsPlotterApp.jar");
     RunScript.runScript("rm  GroupTestDataFiles/"+fileName);
     RunScript.runScript("rm "+JAR_PATH+"PEER_TestResultsPlotterApp.jar");
     RunScript.runScript("jar cfm "+JAR_PATH+"PEER_TestResultsPlotterApp.jar "+
                                    "META-INF/MANIFEST.MF org/");
     RunScript.runScript("jar uf "+JAR_PATH+"PEER_TestResultsPlotterApp.jar "+
                                   "com/");
     RunScript.runScript("jar uf "+JAR_PATH+"PEER_TestResultsPlotterApp.jar "+
                                   "GroupTestDataFiles/");
     // remove the files created by the above
     RunScript.runScript("rm -rf com/");
     RunScript.runScript("rm -rf org/");
     for(int i =0; i< size; ++i)
       RunScript.runScript("rm GroupTestDataFiles/"+(String)fileNamesVector.get(i));
     RunScript.runScript("rm -rf META-INF");


     // remove this file from the Data Submission JAR also
     RunScript.runScript("jar xf "+JAR_PATH+"PEER_TestResultsSubmApp.jar");
     RunScript.runScript("rm GroupTestDataFiles/"+fileName);
     RunScript.runScript("rm "+JAR_PATH+"PEER_TestResultsSubmApp.jar");
     RunScript.runScript("jar cf "+JAR_PATH+"PEER_TestResultsSubmApp.jar "+
                                    "org/");
     RunScript.runScript("jar uf "+JAR_PATH+"PEER_TestResultsSubmApp.jar "+
                                    "GroupTestDataFiles/");
     // remove the files created by above
     RunScript.runScript("rm -rf org/");
     for(int i =0; i< size; ++i)
       RunScript.runScript("rm GroupTestDataFiles/"+(String)fileNamesVector.get(i));


    System.out.println("::PEER_TestResultsPlotterApp.jar updated after file deletion");
    System.out.println("::PEER_TestResultsSubmApp.jar updated after file deletion");
   } catch(Exception e) {
     e.printStackTrace();
   }
  }
}




/**
 * <p>Title: RunScript</p>
 * <p>Description : Accepts the command and runs the shell script
 * @author: Nitin Gupta and Vipin Gupta
 * @created:Dec 27, 2002
 * @version 1.0
 */

class RunScript {


  /**
   * accepts the command and executes on java runtime environment
   *
   * @param command : command to execute
   */
  public static void runScript(String command) {
    try {
      // wait for the shell script to end
      System.out.println("Command to execute: " +command);
      Process p=Runtime.getRuntime().exec(command);
      p.waitFor();
      int i=p.exitValue();

      // check the process status after the process ends
      if ( i == 0 ) {
        // Display the normal o/p if script completed successfully.
        System.out.println("script exited with i =" + i);
        displayOutput(p.getInputStream());
      }
      else {
        // Display the normal and error o/p if script failed.
        System.out.println("script exited with i =" + i);
        displayOutput(p.getErrorStream());
        displayOutput(p.getInputStream());
      }

    } catch(Exception e) {
      // if there is some other exception, print the detailed explanation
      System.out.println("Exception in Executing Shell Script:"+e);
      e.printStackTrace();
    }
  }

  /**
   * display the input stream
   * @param is inputstream
   * @throws Exception
   */
  public static void displayOutput(InputStream is) throws Exception {
    String s;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      while ((s = br.readLine()) != null)
        System.out.println(s);
    } catch (Exception e) {
      System.out.println("Exception in RunCoreCode:displayOutput:"+e);
      e.printStackTrace();
    }
  }
}
