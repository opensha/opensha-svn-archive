package org.scec.sha.gui.servlets;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import javax.servlet.ServletException;
import java.io.File;

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
  private static final String DATASETS_PATH = "HazardMapDataSets/";
  private static final String GET_DATA = "Get Data";
  private static final String MAKE_MAP = "Make Map";

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
        // if user wants to get the existing data
        loadDataSets(new ObjectOutputStream(response.getOutputStream()));
      }else if(functionDesired.equalsIgnoreCase(MAKE_MAP)){
        // if user wants to make the map

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
     File dirs =new File(DATASETS_PATH);
     File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory
     // for each data set, read the meta data and sites info
     for(int i=0;i<dirList.length;++i){
       if(dirList[i].isDirectory()){
         // read the meta data file
         String dataSetDescription= new String();
         try {
           FileReader dataReader = new FileReader(DATASETS_PATH+
               dirList[i].getName()+"/metadata.dat");
           BufferedReader in = new BufferedReader(dataReader);
           dataSetDescription = "";
           String str=in.readLine();
           while(str!=null) {
             dataSetDescription += str+"\n";
             str=in.readLine();
           }
           in.close();
         }catch(Exception ee) {
           ee.printStackTrace();
         }
         metaDataHash.put(dirList[i].getName(),dataSetDescription);

         // read the sites file
         FileReader dataReader = new FileReader(DATASETS_PATH+dirList[i].getName()+
             "/sites.dat");
         BufferedReader in = new BufferedReader(dataReader);
         // first line in the file contains the min lat, max lat, discretization interval
         String latitude = in.readLine();
         latHash.put(dirList[i].getName(),latitude);
         // Second line in the file contains the min lon, max lon, discretization interval
         String longitude = in.readLine();
         lonHash.put(dirList[i].getName(),longitude);
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
 }

