package org.scec.sha.gui.servlets;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;
import javax.servlet.ServletException;
import java.util.StringTokenizer;
import java.text.DecimalFormat;


import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.param.ParameterList;

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
      }else if(functionDesired.equalsIgnoreCase(MAKE_MAP)){// if user wants to make the map

        // getthe set selected by the user
        String selectedSet = (String)inputFromApplet.readObject();
        // map generator object
        GMT_MapGenerator map = (GMT_MapGenerator)inputFromApplet.readObject();
        // whether IML@prob is selected ot Prob@IML
        String optionSelected = (String)inputFromApplet.readObject();
        // get the value
        double val = ((Double)inputFromApplet.readObject()).doubleValue();
        // get the prefix for output file
        String outputFilePrefix = (String)inputFromApplet.readObject();
        boolean isProbAt_IML = true;
        if(optionSelected.equalsIgnoreCase(IMLorProbSelectorGuiBean.IML_AT_PROB))
          isProbAt_IML = false;
        // xyzfilename
        String xyzFileName = this.readAndWriteFile(selectedSet, outputFilePrefix,
                                                    isProbAt_IML, val, map);
        // jpg file name
        String jpgFileName  = map.makeMap(xyzFileName);

        // make the html file
        makeHTML_File(outputFilePrefix);
        ObjectOutputStream outputToApplet =new ObjectOutputStream(response.getOutputStream());
        outputToApplet.writeObject("http://scec.usc.edu:9999/"+outputFilePrefix+".html");
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
     File dirs =new File(HazardMapCalculator.DATASETS_PATH);
     File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory
     // for each data set, read the meta data and sites info
     for(int i=0;i<dirList.length;++i){
       if(dirList[i].isDirectory()){
         // read the meta data file
         String dataSetDescription= new String();
         try {
           FileReader dataReader = new FileReader(HazardMapCalculator.DATASETS_PATH+
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
         try {
           // read the sites file
           FileReader dataReader = new FileReader(HazardMapCalculator.DATASETS_PATH+dirList[i].getName()+
               "/sites.dat");
           BufferedReader in = new BufferedReader(dataReader);
           // first line in the file contains the min lat, max lat, discretization interval
           String latitude = in.readLine();
           latHash.put(dirList[i].getName(),latitude);
           // Second line in the file contains the min lon, max lon, discretization interval
           String longitude = in.readLine();
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
  private String readAndWriteFile(String selectedSet,
                                  String outputFilePrefix,
                                  boolean isProbAt_IML,
                                  double val, GMT_MapGenerator map ){

    // get the min lat, max lat, min lon ,max lon, gridspacing
    ParameterList paramList = map.getAdjustableParamsList();
    double minLat =((Double) paramList.getValue(GMT_MapGenerator.MIN_LAT_PARAM_NAME)).doubleValue();
    double maxLat =((Double) paramList.getValue(GMT_MapGenerator.MAX_LAT_PARAM_NAME)).doubleValue();
    double minLon =((Double) paramList.getValue(GMT_MapGenerator.MIN_LON_PARAM_NAME)).doubleValue();
    double maxLon =((Double) paramList.getValue(GMT_MapGenerator.MAX_LON_PARAM_NAME)).doubleValue();
    double gridSpacing =((Double) paramList.getValue(GMT_MapGenerator.GRID_SPACING_PARAM_NAME)).doubleValue();

    String finalFile = null;;
    try {
      finalFile=outputFilePrefix+".xyz";
      FileWriter fw1= new FileWriter(finalFile);
      fw1.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
    //searching the directory for the list of the files.
    File dir = new File(HazardMapCalculator.DATASETS_PATH+selectedSet+"/");
    String[] fileList=dir.list();
    //formatting of the text double Decimal numbers for 2 places of decimal.
    DecimalFormat d= new DecimalFormat("0.00##");
    for(int i=0;i<fileList.length;++i){
      if(fileList[i].endsWith("txt")){
        String lat=fileList[i].substring(0,fileList[i].indexOf("_"));
        String lon=fileList[i].substring(fileList[i].indexOf("_")+1,fileList[i].indexOf(".txt"));
        double mLat = Double.parseDouble(lat);
        double mLon = Double.parseDouble(lon);
        double diffLat=Double.parseDouble(d.format(mLat-minLat));
        double diffLon=Double.parseDouble(d.format(mLon-minLon));

        //looking if the file we are reading has lat and lon multiple of gridSpacing
        //in Math.IEEEremainder method Zero is same as pow(10,-16)
        if(Math.abs(Math.IEEEremainder(diffLat,gridSpacing)) <.0001
           && Math.abs(Math.IEEEremainder(diffLon,gridSpacing)) < .0001){

          if(mLat>= minLat && mLat<=maxLat && mLon>=minLon && mLon<=maxLon){
            try{
              boolean readFlag=true;

              //reading the desired file line by line.
              FileReader fr= new FileReader(HazardMapCalculator.DATASETS_PATH+selectedSet+
                  "/"+fileList[i]);
              BufferedReader bf= new BufferedReader(fr);
              String dataLine=bf.readLine();
              StringTokenizer st;
              double prevIML=0 ;
              double prevProb=0;
              //reading the first of the file
              if(dataLine!=null){
                st=new StringTokenizer(dataLine);
                prevIML = Double.parseDouble(st.nextToken());
                prevProb= Double.parseDouble(st.nextToken());
              }
              while(readFlag){
                dataLine=bf.readLine();
                //if the file has been read fully break out of the loop.
                if(dataLine ==null || dataLine=="" || dataLine.trim().length()==0){
                  readFlag=false;
                  break;
                }
                st=new StringTokenizer(dataLine);
                //using the currentIML and currentProb we interpolate the iml or prob
                //value entered by the user.
                double currentIML = Double.parseDouble(st.nextToken());
                double currentProb= Double.parseDouble(st.nextToken());
                if(isProbAt_IML){
                  //taking into account the both types of curves, interpolating the value
                  //interpolating the prob value for the iml value entered by the user.
                  if((val>=prevIML && val<=currentIML) ||
                     (val<=prevIML && val>=currentIML)){

                    //final iml value returned after interpolation
                    double finalIML=interpolateIML(val, prevIML,currentIML,prevProb,currentProb);
                    String curveResult=lat+" "+lon+" "+finalIML+"\n";
                    //appending the iml result to the final output file.

                    FileWriter fw= new FileWriter(finalFile,true);
                    fw.write(curveResult);
                    fw.close();
                    break;
                  }
                }
                else if((val>=prevProb && val<=currentProb) ||
                        (val<=prevProb && val>=currentProb)){
                  //interpolating the iml value entered by the user to get the final iml for the
                  //corresponding prob.
                  double finalProb=interpolateProb(val, prevProb,currentProb,prevIML,currentIML);
                  String curveResult=lat+" "+lon+" "+finalProb+"\n";
                  finalFile=selectedSet+".xyz";
                  FileWriter fw= new FileWriter(finalFile,true);
                  fw.write(curveResult);
                  fw.close();
                  break;
                }
                prevIML=currentIML;
                prevProb=currentProb;
              }
              fr.close();
              bf.close();
            }catch(IOException e){
              System.out.println("File Not Found :"+e);
            }

          }

        }
      }
    }
    return finalFile;
  }


  /**
   * interpolating the prob values to get the final prob for the corresponding iml
   * @param x1=iml1
   * @param x2=iml2
   * @param y1=prob1
   * @param y2=prob2
   * @return prob value for the iml entered
   */
  private double interpolateIML(double iml, double x1,double x2,double y1,double y2){
    return ((iml-x1)/(x2-x1))*(y2-y1) +y1;
  }

  /**
   * interpolating the iml values to get the final iml for the corresponding prob
   * @param x1=iml1
   * @param x2=iml2
   * @param y1=prob1
   * @param y2=prob2
   * @return iml value for the prob entered
   */
  private double interpolateProb(double prob, double y1,double y2,double x1,double x2){
    return ((prob-y1)/(y2-y1))*(x2-x1)+x1;
   }

   /**
  * Creates a HTML page with the link to the file
  *
  * @param htmlFileName : name of HTML page
  * @param linkFileName : name of file to be linked
  */
 public void makeHTML_File(String outputFilePrefix) {
    BufferedWriter htmlWriter = null;
    try{

      FileOutputStream outputfile = new FileOutputStream("/export/home/scec-00/scecweb/jsdk2.1/webpages/"+outputFilePrefix+".html");
      BufferedOutputStream buffout = new BufferedOutputStream(outputfile);
      htmlWriter = new BufferedWriter(new OutputStreamWriter(buffout));
      String htmlData=new String("<html><head><title>Download Page</title></head><body><p><br>");
      htmlWriter.write(htmlData);
      htmlWriter.newLine();

      htmlWriter.write("Download the XYZ file from "
                       +"<a href= ../"+outputFilePrefix+".xyz target=htmlfile> here</a>");
      htmlWriter.write("<br>");
      htmlWriter.write("Download the ps file from "
                      +"<a href= ../"+outputFilePrefix+".ps target=htmlfile> here</a>");
      htmlWriter.write("<br>");
      htmlWriter.write("Download the jpg file from "
                      +"<a href= ../"+outputFilePrefix+".jpg target=htmlfile> here</a>");
      htmlWriter.write("<br>");

      htmlWriter.write("Click to View the file<br>");
      htmlWriter.write("To SAVE the file Right Click (or Shift+click) and Select Save Link As ");

      htmlWriter.write("</body></html>");
    } catch(Exception e){
      System.out.println("IOException ocured while outputting file:"+e);
    } finally {
       if(htmlWriter!=null) {
         try{
           htmlWriter.close();
         }catch(Exception x) {
           System.out.println("Unable to close file:"+x);
         }
       }
    }
  }
}

