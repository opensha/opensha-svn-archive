package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import javax.swing.JOptionPane;
import java.util.*;
import java.text.DecimalFormat;
/**
 * <p>Title: STEP_BackSiesDataAdditionObject</p>
 * <p>Description: This Class adds the BackSies ERF dataset the Step ERF dataSet</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 29,2003
 * @version 1.0
 */

public class STEP_BackSiesDataAdditionObject {

  // message to display if no data exits
  private static final String NO_DATA_EXISTS = "No Hazard Map Data Exists";

  //String to store the Metadata,Lats and Lons for the Step DataSet
  String stepMetaData;

  //Stores the Region definition for the step
  String stepLatitude;
  String stepLongitude;

  //String to store the Metadata,Lats and Lons for the Step DataSet
  String backSiesMetaData;

  //Stores the Region definition for the step
  String backSeisLatitude;
  String backSiesLongitude;


  //default class constructor
  public STEP_BackSiesDataAdditionObject() {
  }

  void addDataSet(String backSiesDataSetDir,String stepDataSetDir,String finalDataSetDir){
    //gets the info for the step dataset
    loadStepDataSet(stepDataSetDir);
    //gets the info for the back sies dataset
    loadBackSiesDataSet(backSiesDataSetDir);
    //adds the 2 datasets and generates a final result
    generateFinalDataSet(backSiesDataSetDir,stepDataSetDir,finalDataSetDir);
  }

  /**
   * Reads the step dataset metadata and site info file for the given region
   */
  private void loadStepDataSet(String stepDir) {
    try {
      File dirs =new File(stepDir);
      File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory
      if(dirList==null)
        throw new Exception(this.NO_DATA_EXISTS);

      // for each data set, read the meta data and sites info
      for(int i=0;i<dirList.length;++i){
          // read the meta data file
          String dataSetDescription= new String();
          try {
            FileReader dataReader = new FileReader(stepDir+"/metadata.dat");
            BufferedReader in = new BufferedReader(dataReader);
            dataSetDescription = "";
            String str=in.readLine();
            while(str!=null) {
              dataSetDescription += str+"\n";
              str=in.readLine();
            }
            stepMetaData = dataSetDescription;
            in.close();
          }catch(Exception ee) {
            ee.printStackTrace();
          }

          try {
            // read the sites file
            FileReader dataReader =
                new FileReader(stepDir+"/sites.dat");
            BufferedReader in = new BufferedReader(dataReader);
            // first line in the file contains the min lat, max lat, discretization interval
            stepLatitude = in.readLine();

            // Second line in the file contains the min lon, max lon, discretization interval
            stepLongitude = in.readLine();

          } catch(Exception e) {
            e.printStackTrace();
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Reads the BackSies MetaData and Site info files for the given region
   */
  private void loadBackSiesDataSet(String backSiesDir) {
    try {
      File dirs =new File(backSiesDir);
      File[] dirList=dirs.listFiles(); // get the list of all the data in the parent directory
      if(dirList==null)
        throw new Exception(this.NO_DATA_EXISTS);

      // for each data set, read the meta data and sites info
      for(int i=0;i<dirList.length;++i){
          // read the meta data file
          String dataSetDescription= new String();
          try {
            FileReader dataReader = new FileReader(backSiesDir+"/metadata.dat");
            BufferedReader in = new BufferedReader(dataReader);
            dataSetDescription = "";
            String str=in.readLine();
            while(str!=null) {
              dataSetDescription += str+"\n";
              str=in.readLine();
            }
            stepMetaData = dataSetDescription;
            in.close();
          }catch(Exception ee) {
            ee.printStackTrace();
          }

          try {
            // read the sites file
            FileReader dataReader =
                new FileReader(backSiesDir+"/sites.dat");
            BufferedReader in = new BufferedReader(dataReader);
            // first line in the file contains the min lat, max lat, discretization interval
            stepLatitude = in.readLine();

            // Second line in the file contains the min lon, max lon, discretization interval
            stepLongitude = in.readLine();

          } catch(Exception e) {
            e.printStackTrace();
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds the 2 dataset for the backSies and Step and generates a final dataset
   * @param backSiesDir : Directory name for the Step BackGround Prob
   * @param stepDir :Directory name for the Step Addon Prob
   * @param resultedDir : Directory that contains the combined result for the
   * backGround and Addon
   */
  private void generateFinalDataSet(String backSiesDir, String stepDir, String resultedDir){
    //reading latitude info line
    StringTokenizer st = new StringTokenizer(this.stepLatitude);
    String minLat = st.nextToken();
    String maxLat = st.nextToken();
    String gridSpacingLat = st.nextToken();

    //reading the longitude info line
    st = new StringTokenizer(this.stepLongitude);
    String minLon= st.nextToken();
    String maxLon = st.nextToken();
    String gridSpacingLon = st.nextToken();

    File resultFile = new File(resultedDir);
    if(!resultFile.isDirectory()){
    //creating a new directory that stores all the HazardCurves for that region
      boolean success = (new File(resultedDir)).mkdir();
    }

    //getting the instance for the step addon and back ground directories
    File stepFileDir = new File(stepDir);
    File backSiesFileDir = new File(backSiesDir);

    //checking if the backGround and Addon directories exist for the step
    if(stepFileDir.isDirectory() && backSiesFileDir.isDirectory()){
      //getting the listing of all the step files
      File[] stepFiles = stepFileDir.listFiles();
      //looping over all the files of the step addon directory
      for(int i=0;i<stepFiles.length;++i){
        //getting one file at time
        String stepFileName = stepFiles[i].getName();
        if(!stepFileName.endsWith(".txt"))
          continue;
        try{
          //reading the step file
          FileReader frStep = new FileReader(stepDir+"/"+stepFileName);
          BufferedReader brStep = new BufferedReader(frStep);

          //Here we are assuming that name of the files in the step addon and step backGround are
          //the same and also assuming that region for the step addon is either smaller or
          //equal to the step backGround.
          //reading the step background file
          FileReader frBack = new FileReader(backSiesDir+"/"+stepFileName);
          BufferedReader brBack = new BufferedReader(frBack);
          //creating the file combined result in a new directory craeted earler
          // the file name is same as the step file names
          FileWriter fw = new FileWriter(resultedDir+"/"+stepFileName);
          BufferedWriter bw = new BufferedWriter(fw);

          //reading the first line form the step addon file
          String stepLine = brStep.readLine();
          //reading the first line from the step backGround file
          String backLine = brBack.readLine();
          //looping over all the lines in the step file
          while(stepLine !=null){
            StringTokenizer stStep = new StringTokenizer(stepLine);
            StringTokenizer stBack = new StringTokenizer(backLine);
            //Also assuming that values for the same lat and lons will same in both
            //step files
            String iml = stStep.nextToken();
            stBack.nextToken();
            //taking the prob values for the backGround and Addon
            String stepProb = stStep.nextToken();
            String backProb = stBack.nextToken();
            //summing up the Probability for the backGround and Addon to create a final
            //prob. value
            double prob = new Double(stepProb).doubleValue() +
                          new Double(backProb).doubleValue() -
                          (new Double(stepProb).doubleValue() * new Double(backProb).doubleValue());
            //writing the combined values to the file
            bw.write(iml+"  "+prob+"\n");
            stepLine = brStep.readLine();
            backLine = brBack.readLine();
          }
          bw.close();
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
  }
}
