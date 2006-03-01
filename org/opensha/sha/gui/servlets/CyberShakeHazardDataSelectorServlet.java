package org.opensha.sha.gui.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;
import javax.servlet.ServletException;
import java.util.StringTokenizer;
import java.text.DecimalFormat;
import java.util.Collections;

import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.opensha.param.ParameterList;
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.util.FileUtils;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.servlets.*;

/**
 * <p>Title: CyberShakeHazardDataSelectorServlet</p>
 *
 * <p>Description: This servlet selects the different Sites completed for
 * CyberShake and then collects the hazard dataset for different SA values.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */


public class CyberShakeHazardDataSelectorServlet  extends HttpServlet {


  public static final String GET_SITES_AND_SA_PERIODS = "SA Period for Cybershake Sites";
  public static final String GET_HAZARD_DATA = "Read the CyberShake Hazard Data";
  public static final String CYBERSHAKE_HAZARD_DATASET = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/CyberShake/";
  private static final String hazardDataFilesStartString = "hazcurve_";

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      /**
       * get the function desired by th user
       */
      String functionDesired  = (String) inputFromApplet.readObject();

      if(functionDesired.equalsIgnoreCase(GET_SITES_AND_SA_PERIODS)) {
        // gets the CyberShake Sites and the SA period associated with it.
        loadDataSets(new ObjectOutputStream(response.getOutputStream()));
      }
      else if(functionDesired.equalsIgnoreCase(GET_HAZARD_DATA)) {
        String siteName = (String)inputFromApplet.readObject();
        String saPeriod = (String)inputFromApplet.readObject();
        ArrayList fileData = readHazardDataSet(siteName,saPeriod);
        ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
        // report to the user whether the operation was successful or not
        // get an ouput stream from the applet
        outputToApplet.writeObject(fileData);
        outputToApplet.close();
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // call the doPost method
    doGet(request,response);
  }



  /**
   * This method reads the Hazard data file for a given Cybershake Site and Sa Period.
   * @param siteName String Cybershake site.
   * @param saPeriod String SA period for which hazard data needs to be read.
   * @return ArrayList
   */
  public ArrayList readHazardDataSet(String siteName,String saPeriod){
    String saPeriodFile = hazardDataFilesStartString+saPeriod;
    String fileToRead = this.CYBERSHAKE_HAZARD_DATASET+siteName+"/"+saPeriodFile;
    ArrayList fileLines = null;
    try {
      fileLines = FileUtils.loadFile(fileToRead);
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    return fileLines;
  }


  /**
   *
   * @param outputToApplet ObjectOutputStream
   */
  private void loadDataSets(ObjectOutputStream outputToApplet) {
    //Hashtable for storing the lons from each dataSet
    HashMap siteHazardDatasetMap = new HashMap();

    try {
      File dirs = new File(CYBERSHAKE_HAZARD_DATASET);
      //getting the list of dirs for sites completed in Cybershake.
      File[] dirList = dirs.listFiles();

      // for each data set, read the meta data and sites info
      for (int i = 0; i < dirList.length; ++i) {
        //getting the hazard data files in each site
        if (dirList[i].isDirectory()) {
          try {

            //listing all the SA values files in a given directory
            File hazFiles = new File(dirList[i].getAbsolutePath());
            //listing the files in Cybershake Site directory
            File[] hazardFiles = hazFiles.listFiles();
            //total number of files in the directory
            int numTotalFiles  = hazardFiles.length;

            //creating the Array of ArrayList, with each Arraylist being the
            //filelines read from the hazard data file.
            ArrayList saPeriodVals = new ArrayList();

            for (int j = 0; j < numTotalFiles; ++j) {
              String fileName = hazardFiles[i].getName();
              if(fileName.startsWith(hazardDataFilesStartString)){
                String saPeriod = fileName.substring(fileName.indexOf("_")+1);
                saPeriodVals.add(saPeriod);
              }
            }
            siteHazardDatasetMap.put(dirList[i].getName(), saPeriodVals);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      // report to the user whether the operation was successful or not
      // get an ouput stream from the applet
      outputToApplet.writeObject(siteHazardDatasetMap);
      outputToApplet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

