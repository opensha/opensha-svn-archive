package org.opensha.sha.gui.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.RunScript;
import org.opensha.commons.util.SystemPropertiesUtils;



/**
 * <p>Title: GMT_MapGeneratorServlet </p>
 * <p>Description: this servlet runs the GMT script based on the parameters and generates the
 * image file and returns that back to the calling application applet </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GMT_MapGeneratorServlet
    extends HttpServlet {

  private static final String GMT_URL_PATH = "http://gravity.usc.edu/gmtWS/";
  private final static String FILE_PATH =
      "/opt/install/apache-tomcat-5.5.20/webapps/gmtWS/";
  private final static String GMT_DATA_DIR = "gmtData/";
  private final static String GMT_SCRIPT_FILE = "gmtScript.txt";

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {

    // get an ouput stream from the applet
    ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
        getOutputStream());

    //string that decides the name of the output gmt files
    String outFile = null;
    //gets the current time in milliseconds to be the new director for each user
    String currentMilliSec = "";
    currentMilliSec += System.currentTimeMillis();
    //Name of the directory in which we are storing all the gmt data for the user
    String newDir = null;

    try {
      //all the user gmt stuff will be stored in this directory
      File mainDir = new File(FILE_PATH + GMT_DATA_DIR);
      //create the main directory if it does not exist already
      if (!mainDir.isDirectory()) {
        boolean success = (new File(FILE_PATH + GMT_DATA_DIR)).mkdir();
      }

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.
          getInputStream());

      //receiving the name of the input directory
      String dirName = (String) inputFromApplet.readObject();
      if (dirName != null) {
        File f = new File(dirName);
        int fileCounter = 1;
        //checking if the directory already exists then add
        while (f.exists()) {
          String tempDirName = dirName + fileCounter;
          f = new File(tempDirName);
          ++fileCounter;
        }
        newDir = FILE_PATH + GMT_DATA_DIR + f.getName();
      }
      else {
        dirName = currentMilliSec;
        newDir = FILE_PATH + GMT_DATA_DIR + currentMilliSec;
      }

      //create a gmt directory for each user in which all his gmt files will be stored
      boolean success = (new File(newDir)).mkdir();
      //reading the gmtScript file that user sent as the attachment and create
      //a new gmt script inside the directory created for the user.
      //The new gmt script file created also has one minor modification
      //at the top of the gmt script file I am adding the "cd ... " command so
      //that it should pick all the gmt related files from the directory cretade for the user.
      //reading the gmt script file sent by user as te attchment

      String gmtScriptFile = newDir + "/" + this.GMT_SCRIPT_FILE;

      //gets the object for the GMT_MapGenerator script
      ArrayList gmtMapScript = (ArrayList) inputFromApplet.readObject();

      //XYZ dataset object being received from the applet
      XYZ_DataSetAPI xyzDataSet = (XYZ_DataSetAPI) inputFromApplet.readObject();

      //Name of the XYZ file
      String xyzFileName = (String) inputFromApplet.readObject();

      //Metadata content: Map Info
      String metadata = (String) inputFromApplet.readObject();

      //Name of the Metadata file
      String metadataFileName = (String) inputFromApplet.readObject();

      //creating a new gmt script for the user and writing it ot the directory created for the user
      FileWriter fw = new FileWriter(gmtScriptFile);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write("cd " + newDir + "/" + "\n");
      int size = gmtMapScript.size();
      for (int i = 0; i < size; ++i) {
        bw.write( (String) gmtMapScript.get(i) + "\n");
      }
      bw.close();

      String metadataFile = newDir + "/" + metadataFileName;
      //creating the metadata (map Info) file in the new directory created for user
      fw = new FileWriter(metadataFile);
      bw = new BufferedWriter(fw);
      bw.write(" " + (String) metadata + "\n");
      bw.close();

      //creating the XYZ file from the XYZ file from the XYZ dataSet
      ArrayList xVals = xyzDataSet.getX_DataSet();
      ArrayList yVals = xyzDataSet.getY_DataSet();
      ArrayList zVals = xyzDataSet.getZ_DataSet();
      //file follows the convention lat, lon and Z value
      if (xyzDataSet.checkXYZ_NumVals()) {
        size = xVals.size();
        fw = new FileWriter(newDir + "/" + xyzFileName);
        bw = new BufferedWriter(fw);
        for (int i = 0; i < size; ++i) {
          //System.out.println(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
          bw.write(xVals.get(i) + " " + yVals.get(i) + " " + zVals.get(i) +
                   "\n");
        }
        bw.close();
      }
      else {
        throw new RuntimeException(
            "X, Y and Z dataset does not have equal size");
      }

      //running the gmtScript file
      String[] command = {
          "sh", "-c", "sh " + gmtScriptFile};
      RunScript.runScript(command);

      //create the Zip file for all the files generated
      FileUtils.createZipFile(newDir);
      //URL path to folder where all GMT related files and map data file for this
      //calculations reside.
      String mapImagePath = this.GMT_URL_PATH + this.GMT_DATA_DIR +
                                 dirName + SystemPropertiesUtils.getSystemFileSeparator();
      //returns the URL to the folder where map image resides
      outputToApplet.writeObject(mapImagePath);
      outputToApplet.close();

    }catch (Exception e) {
      //sending the error message back to the application
      outputToApplet.writeObject(new RuntimeException(e.getMessage()));
      outputToApplet.close();
    }
  }
  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    // call the doPost method
    doGet(request, response);
  }

}
