package org.scec.sha.gui.servlets;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.*;
import javax.servlet.ServletException;
import java.util.StringTokenizer;
import java.text.DecimalFormat;
import java.util.Collections;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.param.ParameterList;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.util.FileUtils;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.sha.gui.servlets.*;

/**
 * <p>Title: HazardMapViewerServlet</p>
 * <p>Description: It reads the data directory and sends the metadata info back
 * to the applet to be shown in the applet.
 * It also generates the GMT map based on the settings of the user</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HazardMapViewerServlet  extends HttpServlet {

  // directory where all the hazard map data sets will be saved
  public static final String GET_DATA = "Get Data";
  public static final String MAKE_MAP = "Make Map";

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      /**
       * get the function desired by th user
       */
      String functionDesired  = (String) inputFromApplet.readObject();

      if(functionDesired.equalsIgnoreCase(GET_DATA)) {
        // if USER WANTS TO LOAD EXISTING DATA SETS
        loadDataSets(new ObjectOutputStream(response.getOutputStream()));
      }else if(functionDesired.equalsIgnoreCase(MAKE_MAP)){ // IF USER WANTS TO MAKE MAP
        // get the set selected by the user
        String selectedSet = (String)inputFromApplet.readObject();
        // map generator object
        GMT_MapGenerator map = (GMT_MapGenerator)inputFromApplet.readObject();
        // whether IML@prob is selected or Prob@IML
        String optionSelected = (String)inputFromApplet.readObject();
        // get the value
        double val = ((Double)inputFromApplet.readObject()).doubleValue();
        // get the metadata
        String metadata = (String)inputFromApplet.readObject();

        boolean isProbAt_IML = true;
        if(optionSelected.equalsIgnoreCase(IMLorProbSelectorGuiBean.IML_AT_PROB))
          isProbAt_IML = false;
        // create the XYZ data set
        XYZ_DataSetAPI xyzData = getXYZ_DataSet(selectedSet, isProbAt_IML, val, map);
        String metadataFileName = HazardMapCalcServlet.PARENT_DIR+
               selectedSet+"/"+"map_info.txt";
        FileWriter fw = new FileWriter(metadataFileName);
        fw.write(metadata);
        fw.close();
        // jpg file name
        map.setMetatdataFileName(metadataFileName);
        //get the map scale label
        String mapLabel = getMapLabel(isProbAt_IML);

        String jpgFileName  = map.makeMapUsingServlet(xyzData,mapLabel,metadata,null);
        ObjectOutputStream outputToApplet =new ObjectOutputStream(response.getOutputStream());
        outputToApplet.writeObject(jpgFileName);
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
   *
   * @returns the Map label based on the selected Map Type( Prob@IML or IML@Prob)
   */
  private String getMapLabel(boolean isProbAtIML){
    //making the map
    String label;

    if(isProbAtIML)
      label="Prob";
    else
      label="IML";
    return label;
  }

 /**
  * Read the data sets, their names, their params needed to generate map
  * and site range
  * @param metaDataHash : Hashtable to save metadata
  * @param lonHash : hashtable to save longitude range
  * @param latHash : hashtable to save latitude range
  */
 private void loadDataSets(ObjectOutputStream outputToApplet) {
   //HashTables for storing the metadata for each dataset
   Hashtable metaDataHash = new Hashtable();
   //Hashtable for storing the lons from each dataSet
   Hashtable lonHash= new Hashtable();
   //Hashtable for storing the lats from each dataSet
   Hashtable latHash= new Hashtable();
   try {
     File dirs =new File(HazardMapCalcServlet.PARENT_DIR);
     File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory

     // for each data set, read the meta data and sites info
     for(int i=0;i<dirList.length;++i){
       if(dirList[i].isDirectory()){

         // READ THE METADATA FILE
         String dataSetDescription= new String();
         try {
           File f = new File(HazardMapCalcServlet.PARENT_DIR+
               dirList[i].getName()+"/"+HazardMapCalcServlet.METADATA_FILE_NAME);
           if (!f.exists()) continue;
           FileReader dataReader = new FileReader(f);
           BufferedReader in = new BufferedReader(dataReader);
           dataSetDescription = "";
           String str=in.readLine();
           while(str!=null) {
             dataSetDescription += str+"\n";
             str=in.readLine();
           }
           metaDataHash.put(dirList[i].getName(),dataSetDescription);
           in.close();

           // READ THE SITES FILE
           FileReader sitesReader = new FileReader(HazardMapCalcServlet.PARENT_DIR
               + dirList[i].getName() +
               "/"+HazardMapCalcServlet.SITES_FILE_NAME);
           BufferedReader sitesin = new BufferedReader(sitesReader);
           // first line in the file contains the min lat, max lat, discretization interval
           String latitude = sitesin.readLine();
           latHash.put(dirList[i].getName(),latitude);
           // Second line in the file contains the min lon, max lon, discretization interval
           String longitude = sitesin.readLine();
           lonHash.put(dirList[i].getName(),longitude);

         }catch(Exception e) {
           e.printStackTrace();
         }
        }
     }

     // report to the user whether the operation was successful or not
     // get an ouput stream from the applet
     outputToApplet.writeObject(metaDataHash);
     outputToApplet.writeObject(lonHash);
     outputToApplet.writeObject(latHash);
     outputToApplet.close();
   }catch(Exception e) {
     e.printStackTrace();
   }
 }

 /**
  * This method reads the file and generates the final outputfile
  * for the range of the lat and lon selected by the user . The final output is
  * generated based on the selcetion made by the user either for the iml@prob or
  * prob@iml. The data is appended to the end of the until all the list of the
  * files have been searched for thr input iml or prob value. The final output
  * file is given as the input to generate the grd file.
  * @param minLat
  * @param maxLat
  * @param minLon
  * @param maxLon
  */
  private XYZ_DataSetAPI getXYZ_DataSet(String selectedSet,
                                  boolean isProbAt_IML,
                                  double val, GMT_MapGenerator map ){

    // get the min lat, max lat, min lon ,max lon, gridspacing
    ParameterList paramList = map.getAdjustableParamsList();
    String minLat = paramList.getValue(GMT_MapGenerator.MIN_LAT_PARAM_NAME).toString();
    String maxLat = paramList.getValue(GMT_MapGenerator.MAX_LAT_PARAM_NAME).toString();
    String minLon = paramList.getValue(GMT_MapGenerator.MIN_LON_PARAM_NAME).toString();
    String maxLon = paramList.getValue(GMT_MapGenerator.MAX_LON_PARAM_NAME).toString();
    double gridSpacing =((Double) paramList.getValue(GMT_MapGenerator.GRID_SPACING_PARAM_NAME)).doubleValue();

    //adding the xyz data set to the object of XYZ_DataSetAPI
    XYZ_DataSetAPI xyzData;
    ArrayList xVals= new ArrayList();
    ArrayList yVals= new ArrayList();
    ArrayList zVals= new ArrayList();

    //searching the directory for the list of the files.
    File dir = new File(HazardMapCalcServlet.PARENT_DIR+selectedSet+"/");
    File[] fileList=dir.listFiles();

    int numFiles = fileList.length;
    //creating the arraylist to get the lat and lons in this dataset
    ArrayList latList = new ArrayList();
    ArrayList lonList = new ArrayList();

    /*
    *Reading all the Hazard files in the dataset to get their Lat and Lons
    */

    for(int i=0;i<numFiles;++i){
      //only taking the files into consideration
      if(fileList[i].isFile()){
        String fileName = fileList[i].getName();
        //files that ends with ".txt"
        if(fileName.endsWith(".txt")){
          int index = fileName.indexOf("_");
          int firstIndex = fileName.indexOf(".");
          int lastIndex = fileName.lastIndexOf(".");
          // Hazard data files have 3 "." in their names
          if(firstIndex != lastIndex){

            //getting the lat and Lon values from file names
            String latVal = fileName.substring(0,index).trim();
            String lonVal = fileName.substring(index+1,lastIndex).trim();
            if(!latList.contains(latVal))
              latList.add(latVal);
            if(!lonList.contains(lonVal))
              lonList.add(lonVal);
          }
        }
      }
    }

    Collections.sort(latList);
    Collections.sort(lonList);

    double interpolatedVal=0;
    ArrayList fileLines;
    //getting the indexes of the lat and lon( filled by the user)
   // from the list of Lat and Lons( for which we computed the dataset).
    int latListSize = latList.size();
    int lonListSize = lonList.size();
    int minLatIndex =0;
    int maxLatIndex =0;
    double minLatVal = Double.parseDouble(minLat);
    double maxLatVal = Double.parseDouble(maxLat);

    double gridSpacingForCloseValue = gridSpacing/2;

    for(int i=0;i<latListSize;++i){
      if(Math.abs(minLatVal - Double.parseDouble((String)latList.get(i))) < gridSpacingForCloseValue)
         minLatIndex = i;
      if(Math.abs(maxLatVal - Double.parseDouble((String)latList.get(i))) < gridSpacingForCloseValue)
         maxLatIndex = i;
    }


    int minLonIndex =0;
    int maxLonIndex =0;
    double minLonVal = Double.parseDouble(minLon);
    double maxLonVal = Double.parseDouble(maxLon);
    for(int i=0;i<lonListSize;++i){
      if(Math.abs(minLonVal - Double.parseDouble((String)lonList.get(i))) < gridSpacingForCloseValue)
         minLonIndex = i;
      if(Math.abs(maxLonVal - Double.parseDouble((String)lonList.get(i))) < gridSpacingForCloseValue)
         maxLonIndex = i;
    }

    System.out.println("Indexes: "+minLatIndex+" "+maxLatIndex+" "+minLonIndex+" "+maxLonIndex);

    for(int k=minLatIndex;k<=maxLatIndex;++k){
      for(int j=minLonIndex;j>=maxLonIndex;--j) {
        //getting Lat and Lons
        String lat = (String)latList.get(k);
        String lon = (String)lonList.get(j);

        try {
          String fileToRead = lat+"_"+ lon+".txt";
          System.out.println("File to read: "+fileToRead);
          fileLines = FileUtils.loadFile(HazardMapCalcServlet.PARENT_DIR+selectedSet+"/"+fileToRead);
          String dataLine;
          StringTokenizer st;
          ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();

          if(fileLines.size() ==0)
            System.out.println("File to read but could not found:"+fileToRead);

          for(int i=0;i<fileLines.size();++i) {
            dataLine=(String)fileLines.get(i);
            st=new StringTokenizer(dataLine);
            //using the currentIML and currentProb we interpolate the iml or prob
            //value entered by the user.
            double currentIML = Double.parseDouble(st.nextToken());
            double currentProb= Double.parseDouble(st.nextToken());
            func.set(currentIML, currentProb);
          }

          if (isProbAt_IML)
            //final iml value returned after interpolation in log space
            interpolatedVal = func.getInterpolatedY_inLogXLogYDomain(val);
            // for  IML_AT_PROB
          else //interpolating the iml value in log space entered by the user to get the final iml for the
            //corresponding prob.
            interpolatedVal = func.getFirstInterpolatedX_inLogXLogYDomain(val);

        }catch(Exception e) {
          e.printStackTrace();
        } // catch invalid range exception etc.
        xVals.add(new Double(lat));
        yVals.add(new Double(lon));
        zVals.add(new Double(interpolatedVal));
        System.out.println("Interpolated Z Val: "+interpolatedVal);
      }
    }

    // return the XYZ Data set
    xyzData = new ArbDiscretizedXYZ_DataSet(xVals,yVals,zVals);
    return xyzData;
  }
}

