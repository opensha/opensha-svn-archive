package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.util.RunScript;
import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;

/**
 * <p>Title: GMT_MapGeneratorServlet </p>
 * <p>Description: this servlet runs the GMT script based on the parameters and generates the
 * image file and returns that back to the calling application applet </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GMT_MapGeneratorServlet extends HttpServlet {

  private static final String GMT_URL_PATH="http://gravity.usc.edu/gmtWS/";
  private final static String FILE_PATH="/opt/install/jakarta-tomcat-4.1.24/webapps/gmtWS/";
  private final static String GMT_DATA_DIR ="gmtData/" ;
  private final static String GMT_SCRIPT_FILE = "gmtScript.txt";


  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    //string that decides the name of the output gmt files
    String outFile = null;
    //gets the current time in milliseconds to be the new director for each user
    String currentMilliSec ="";
    currentMilliSec += System.currentTimeMillis();
    //Name of the directory in which we are storing all the gmt data for the user
    String newDir= null;

    try{
      //all the user gmt stuff will be stored in this directory
      File mainDir = new File(FILE_PATH+GMT_DATA_DIR);
      //create the main directory if it does not exist already
      if(!mainDir.isDirectory()){
        boolean success = (new File(FILE_PATH+GMT_DATA_DIR)).mkdir();
      }
      newDir = FILE_PATH+GMT_DATA_DIR+currentMilliSec;
      //create a gmt directory for each user in which all his gmt files will be stored
      boolean success =(new File(newDir)).mkdir();

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      //gets the object for the GMT_MapGenerator script
      Vector gmtMapScript = (Vector) inputFromApplet.readObject();

      //XYZ dataset object being received from the applet
      XYZ_DataSetAPI xyzDataSet = (XYZ_DataSetAPI)inputFromApplet.readObject();

      //Name of the XYZ file
      String xyzFileName = (String)inputFromApplet.readObject();

      //Metadata content: Map Info
      Vector metadataVector = (Vector)inputFromApplet.readObject();

      //Name of the Metadata file
      String metadataFileName = (String)inputFromApplet.readObject();

      //reading the gmtScript file that user sent as the attachment and create
      //a new gmt script inside the directory created for the user.
      //The new gmt script file created also has one minor modification
      //at the top of the gmt script file I am adding the "cd ... " command so
      //that it should pick all the gmt related files from the directory cretade for the user.
      //reading the gmt script file sent by user as te attchment

      String gmtScriptFile = newDir+"/"+this.GMT_SCRIPT_FILE;
      //creating a new gmt script for the user and writing it ot the directory created for the user
      FileWriter fw = new FileWriter(gmtScriptFile);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write("cd "+newDir+"/"+"\n");
      int size= gmtMapScript.size();
      for(int i=0;i<size;++i)
        bw.write((String)gmtMapScript.get(i)+"\n");
      bw.close();


      String metadataFile = newDir+"/"+metadataFileName;
      //creating the metadata (map Info) file in the new directory created for user
      fw = new FileWriter(metadataFile);
      bw = new BufferedWriter(fw);
      size = metadataVector.size();

      for(int i=0;i<size;++i)
        bw.write(" "+(String)metadataVector.get(i)+"\n");
      bw.close();

      //creating the XYZ file from the XYZ file from the XYZ dataSet
      Vector xVals = xyzDataSet.getX_DataSet();
      Vector yVals = xyzDataSet.getY_DataSet();
      Vector zVals = xyzDataSet.getZ_DataSet();
      //file follows the convention lat, lon and Z value
      if(xyzDataSet.checkXYZ_NumVals()){
        size = xVals.size();
        fw = new FileWriter(newDir+"/"+xyzFileName);
        bw = new BufferedWriter(fw);
        for(int i=0;i<size;++i){
          //System.out.println(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
          bw.write(xVals.get(i)+" "+yVals.get(i)+" "+zVals.get(i)+"\n");
        }
        bw.close();
      }
      else
        throw new RuntimeException("X, Y and Z dataset does not have equal size");

      //running the gmtScript file
      String[] command ={"sh","-c","sh "+gmtScriptFile};
      RunScript.runScript(command);

      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());

      //name of the image file as the URL
      outputToApplet.writeObject(this.GMT_URL_PATH+this.GMT_DATA_DIR+currentMilliSec+"/");
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