package org.scec.sha.gui.servlets;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.io.*;
import javax.servlet.ServletException;
import java.util.StringTokenizer;
import java.text.DecimalFormat;


import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.param.ParameterList;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.util.FileUtils;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

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
        // whether IML@prob is selected ot Prob@IML
        String optionSelected = (String)inputFromApplet.readObject();
        // get the value
        double val = ((Double)inputFromApplet.readObject()).doubleValue();

        boolean isProbAt_IML = true;
        if(optionSelected.equalsIgnoreCase(IMLorProbSelectorGuiBean.IML_AT_PROB))
          isProbAt_IML = false;
        // create the XYZ data set
        XYZ_DataSetAPI xyzData = getXYZ_DataSet(selectedSet, isProbAt_IML, val, map);
        // jpg file name
        String jpgFileName  = map.makeMapUsingServlet(xyzData,"IML");
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
           FileReader dataReader = new FileReader(HazardMapCalcServlet.PARENT_DIR+
               dirList[i].getName()+"/"+HazardMapCalcServlet.METADATA_FILE_NAME);
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
    double minLat =((Double) paramList.getValue(GMT_MapGenerator.MIN_LAT_PARAM_NAME)).doubleValue();
    double maxLat =((Double) paramList.getValue(GMT_MapGenerator.MAX_LAT_PARAM_NAME)).doubleValue();
    double minLon =((Double) paramList.getValue(GMT_MapGenerator.MIN_LON_PARAM_NAME)).doubleValue();
    double maxLon =((Double) paramList.getValue(GMT_MapGenerator.MAX_LON_PARAM_NAME)).doubleValue();
    double gridSpacing =((Double) paramList.getValue(GMT_MapGenerator.GRID_SPACING_PARAM_NAME)).doubleValue();

    //adding the xyz data set to the object of XYZ_DataSetAPI
    XYZ_DataSetAPI xyzData;
    Vector xVals= new Vector();
    Vector yVals= new Vector();
    Vector zVals= new Vector();

    //searching the directory for the list of the files.
    File dir = new File(HazardMapCalcServlet.PARENT_DIR+selectedSet+"/");
    String[] fileList=dir.list();
    //formatting of the text double Decimal numbers for 2 places of decimal.
    DecimalFormat d= new DecimalFormat("0.00##");

    double interpolatedVal=0;
    ArrayList fileLines;
    for(double lat = minLat; lat<=maxLat; lat=lat+gridSpacing){
      for(double lon = minLon; lon<=maxLon; lon=lon+gridSpacing) {
        try {
          fileLines = FileUtils.loadFile(HazardMapCalcServlet.PARENT_DIR+selectedSet+"/"+d.format(lat)+"_"+ d.format(lon)+".txt");
          String dataLine;
          StringTokenizer st;
          ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
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
            //final iml value returned after interpolation
            interpolatedVal = func.getInterpolatedY(val);
            // for  IML_AT_PROB
          else //interpolating the iml value entered by the user to get the final iml for the
            //corresponding prob.
            interpolatedVal = func.getFirstInterpolatedX(val);

        }catch(Exception e) { } // catch invalid range exception etc.
        xVals.add(new Double(lon));
        yVals.add(new Double(lat));
        zVals.add(new Double(interpolatedVal));
      }
    }

    // return the XYZ Data set
    xyzData = new ArbDiscretizedXYZ_DataSet(xVals,yVals,zVals);
    return xyzData;
  }
}

