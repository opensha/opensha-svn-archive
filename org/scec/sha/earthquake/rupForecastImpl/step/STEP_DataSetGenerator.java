package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DecimalFormat;


import org.scec.sha.earthquake.rupForecastImpl.step.*;
import org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.WarningParameterAPI;
import org.scec.param.WarningDoubleParameter;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.sha.earthquake.*;
import org.scec.sha.gui.infoTools.ConnectToCVM;
import org.scec.util.*;


/**
 * <p>Title: STEP_DataSetGenerator</p>
 * <p>Description: This class generates the Dataset for the STEP Map which includes the
 * BackGround, STEP addon and combined result for both</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @created :Sept 03, 2003
 * @version 1.0
 */

public class STEP_DataSetGenerator implements ParameterChangeWarningListener{


  private final static String DELTA_RATES_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";

  // VS 30 value to be set in the IMR
  private final Double VS_30= new Double(760);

  //private final double MIN_LAT= 32.5;
  private final double MIN_LAT= 32;
  //private final double MAX_LAT= 36.6;
private final double MAX_LAT= 42.2;
  //private final double MIN_LON = -121.5 ;
  private final double MIN_LON = -124.6;
  //private final double MAX_LON= -114.50;
  private final double MAX_LON= -112;
  private final double GRID_SPACING= .1;
  private static final String STEP_DIR = "step/";
  private static final String STEP_BACKGROUND_FILE = "backGround.txt";
  private static final String STEP_ADDON_FILE_SUFFIX = "_addon.txt";
  private static final String STEP_COMBINED_FILE_SUFFIX = "_both.txt";
  private static final String METADATA_FILE_SUFFIX = "_metadata.dat";
  private static final String WILLS_SITE_CLASS_FILE_NAME = "wills_class.txt";
  private static final double IML_VALUE = Math.log(0.126);
  private ArrayList latVals;
  private ArrayList lonVals;
  //list to store the Wills Site Class Value
  private ArrayList willSiteClassVals ;
  DecimalFormat format = new DecimalFormat("0.00##");

  public STEP_DataSetGenerator() {
    // make the forecast
    STEP_EqkRupForecast forecast=null;
    try{
      forecast = new STEP_EqkRupForecast();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println("No internet connection available");
    }

    latVals = new ArrayList();
    lonVals = new ArrayList();
    //list to store the Wills Site Class Value
    willSiteClassVals = new ArrayList();

    // make the imr
    ShakeMap_2003_AttenRel attenRel = new ShakeMap_2003_AttenRel(this);
    // set the im as PGA
    attenRel.setIntensityMeasure(attenRel.PGA_NAME);
    // set the vs30
    attenRel.getParameter(attenRel.WILLS_SITE_NAME).setValue(attenRel.WILLS_SITE_D);
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(MIN_LAT, MAX_LAT, MIN_LON,
        MAX_LON, GRID_SPACING);
    region.addSiteParams(attenRel.getSiteParamsIterator());

    int numSites = region.getNumGridLocs();

    //adding the numSites to the lat and Lon ArrayList
    for(int i=0;i<numSites;++i){

      latVals.add(new Double(format.format(region.getSite(i).getLocation().getLatitude())));
      lonVals.add(new Double(format.format(region.getSite(i).getLocation().getLongitude())));
    }


    //creating the step directory in which we put all the step related files
    File stepDir = new File(this.STEP_DIR);
    if(!stepDir.isDirectory()) { // if main directory does not exist
      boolean success = (new File(STEP_DIR)).mkdir();
    }

    //generating the file for the VS30 Values if it already not exists
    File vs30File = new File(this.STEP_DIR+this.WILLS_SITE_CLASS_FILE_NAME);
    //if already exists then just read the file and get the vs30 values
    if(vs30File.exists())
      getWillsSiteClassValForLatLon(this.STEP_DIR+this.WILLS_SITE_CLASS_FILE_NAME);
    //if file does not already exists then create it.
    else{
      try{
        willSiteClassVals = ConnectToCVM.getWillsSiteTypeFromCVM(MIN_LON,MAX_LON,MIN_LAT,MAX_LAT,GRID_SPACING);
      }catch(Exception e){
        System.out.println("could not connect with wills site class servlet");
        e.printStackTrace();
      }
      this.createFile(willSiteClassVals,this.STEP_DIR+this.WILLS_SITE_CLASS_FILE_NAME);
    }
    //MetaData String
    String metadata = "IMR Info: \n"+
                      "\t"+"Name: "+attenRel.getName()+"\n"+
                      "\t"+"Intensity Measure Type: "+ attenRel.getIntensityMeasure().getName()+"\n"+
                      "\n\n"+
                      "Region Info: \n"+
                      "\t"+"MinLat: "+region.getMinLat()+"\n"+
                      "\t"+"MaxLat: "+region.getMaxLat()+"\n"+
                      "\t"+"MinLon: "+region.getMinLon()+"\n"+
                      "\t"+"MaxLon: "+region.getMaxLon()+"\n"+
                      "\t"+"GridSpacing: "+region.getGridSpacing()+"\n"+
                      "\t"+"Site Params: "+attenRel.getParameter(attenRel.WILLS_SITE_NAME).getName()+ " = "+attenRel.getParameter(attenRel.WILLS_SITE_NAME).getValue().toString()+"\n"+
                      "\n\n"+
                      "Forecast Info: \n"+
                      "\t"+"Name: "+forecast.getName()+"\n";

    //generating the background dataSet
    String dataInfo = "Step Back Ground DataSet\n\n"+metadata;
    //updating the forecast for the background Siesmicity
    forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_BACKGROUND);
    forecast.updateForecast();
    //generating the file for the BackGround
    File backSiesFile = new File(this.STEP_DIR+this.STEP_BACKGROUND_FILE);
    ArrayList backSiesProbVals = new ArrayList();
    //if the file for the backGround already exists then just pick up the values for the Prob from the file
    if(backSiesFile.exists())
      getValForLatLon(backSiesProbVals,this.STEP_DIR+this.STEP_BACKGROUND_FILE);
    //if the backGround file does not already exist then create it
    else{
      backSiesProbVals = getProbVals(attenRel,region,(EqkRupForecast)forecast);
      createFile(backSiesProbVals,this.STEP_DIR+this.STEP_BACKGROUND_FILE);
      //creting the metadata file for the backGround
      String backFile = this.STEP_BACKGROUND_FILE.substring(0,STEP_BACKGROUND_FILE.indexOf("."));
      createMetaDataFile(dataInfo,this.STEP_DIR+backFile+this.METADATA_FILE_SUFFIX);
    }

    //metadata for the Addon Prob
    dataInfo = "Step Addon Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;

    //updating the STEP forecast for the STEP Addon Probabilities
    forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_ADD_ON);
    forecast.updateForecast();
    //getting the name of the STEP data(XYZ )file from the first line on the STEP website which basically tells the time of updation
    String stepDirName = this.getStepDirName();
    //creating the dataFile for the STEP Addon Probabilities
    ArrayList stepAddonProbVals = new ArrayList();

    File addonFile = new File(this.STEP_DIR+stepDirName+this.STEP_ADDON_FILE_SUFFIX);
    //if addon file already exists
    if(addonFile.exists())
      getValForLatLon(stepAddonProbVals,this.STEP_DIR+stepDirName+this.STEP_ADDON_FILE_SUFFIX);
    //if the file does not exists then create it.
    else{
      stepAddonProbVals = getProbVals(attenRel,region,(EqkRupForecast)forecast);
      createFile(stepAddonProbVals,this.STEP_DIR+stepDirName+this.STEP_ADDON_FILE_SUFFIX);
      //creating the metadata file for the STEP addon probabilities
      String stepFile = this.STEP_ADDON_FILE_SUFFIX.substring(0,STEP_ADDON_FILE_SUFFIX.indexOf("."));
      createMetaDataFile(dataInfo,this.STEP_DIR+stepDirName+stepFile+this.METADATA_FILE_SUFFIX);
    }

    //metadata for the Combined Prob. (Addon +BackGround)
    dataInfo = "Step Combined(Added) Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;
    //combining the backgound and Addon dataSet and wrinting the result to the file
    STEP_BackSiesDataAdditionObject addStepData = new STEP_BackSiesDataAdditionObject();
    ArrayList stepBothProbVals = addStepData.addDataSet(backSiesProbVals,stepAddonProbVals);
    File bothFile = new File(this.STEP_DIR+stepDirName+this.STEP_COMBINED_FILE_SUFFIX);
    if(!bothFile.exists()){
      createFile(stepBothProbVals,this.STEP_DIR+stepDirName+this.STEP_COMBINED_FILE_SUFFIX);
      //creating the metadata file for the STEP addon and backGround probabilities combined
      String stepBothFile = this.STEP_COMBINED_FILE_SUFFIX.substring(0,STEP_COMBINED_FILE_SUFFIX.indexOf("."));
      createMetaDataFile(dataInfo,this.STEP_DIR+stepDirName+stepBothFile+this.METADATA_FILE_SUFFIX);
    }
 }


  /**
   * craetes the output xyz files
   * @param probVals : Probablity values ArrayList for each Lat and Lon
   * @param fileName : File to create
   */
  private void createFile(ArrayList probVals, String fileName){
    int size = probVals.size();
   // System.out.println("Size of the Prob ArrayList is:"+size);
    try{
      FileWriter fr = new FileWriter(fileName);
      for(int i=0;i<size;++i)
        fr.write(latVals.get(i)+"  "+lonVals.get(i)+"  "+probVals.get(i)+"\n");
      fr.close();
    }catch(IOException ee){
      ee.printStackTrace();
    }
  }


  /**
   * returns the prob or VS30 vals in a vector(vals) for the file( fileName)
   * @param vals : ArrayList containing the values( z values)
   * @param fileName : Name of the file from which we collect the values
   */
  private void getValForLatLon(ArrayList vals,String fileName){
    try{
      ArrayList fileLines = FileUtils.loadFile(fileName);
      ListIterator it = fileLines.listIterator();
      while(it.hasNext()){
        StringTokenizer st = new StringTokenizer((String)it.next());
        st.nextToken();
        st.nextToken();
        String val =st.nextToken().trim();
        //System.out.println("Val: "+val);
        if(!val.equalsIgnoreCase("NaN"))
          vals.add(new Double(val));
        else
          vals.add(new Double(Double.NaN));
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * returns wills site class in a list for the file( fileName)
   * @param vals : ArrayList containing the values( z values)
   * @param fileName : Name of the file from which we collect the values
   */
  private void getWillsSiteClassValForLatLon(String fileName){
    try{
      ArrayList fileLines = FileUtils.loadFile(fileName);
      ListIterator it = fileLines.listIterator();
      while(it.hasNext()){
        StringTokenizer st = new StringTokenizer((String)it.next());
        st.nextToken();
        st.nextToken();
        String val =st.nextToken().trim();
        //System.out.println("Val: "+val);
        willSiteClassVals.add(val);
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }



  /**
   * Creates the metadata file for the dataSet
   * @param metadata : String that contains the metadata info
   * @param fileName : Name of the metadataFile
   */
  private void createMetaDataFile(String metadata, String fileName){
    try{
      FileWriter file = new FileWriter(fileName);
      file.write(metadata);
      file.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }



  /**
   * HazardCurve Calculator for the STEP
   * @param imr : ShakeMap_2003_AttenRel for the STEP Calculation
   * @param region
   * @param eqkRupForecast : STEP Forecast
   * @returns the ArrayList of Probability values for the given region
   */
  private ArrayList getProbVals(ShakeMap_2003_AttenRel imr,SitesInGriddedRegion region,
                                     EqkRupForecast eqkRupForecast){

    ArrayList probVals = new ArrayList();
    double MAX_DISTANCE = 200;

    // declare some varibles used in the calculation
    double qkProb, distance;
    int k,i;

    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();

    // this boolean will tell us whether a source was actually used
    // (e.g., all could be outside MAX_DISTANCE)
    boolean sourceUsed = false;

    int numSites = region.getNumGridLocs();
    for(int j=0;j<numSites;++j){
      double hazVal =1;
      double condProb =0;
      imr.setSite(region.getSite(j));
      //adding the wills site class value for each site
      String willSiteClass = (String)this.willSiteClassVals.get(j);
      //only add the wills value if we have a value available for that site else leave default "D"
      if(!willSiteClass.equals("NA"))
        imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(willSiteClass);

      // loop over sources
      for(i=0;i < numSources ;i++) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(i);

        // compute it's distance from the site and skip if it's too far away
        distance = source.getMinDistance(region.getSite(j));
        if(distance > MAX_DISTANCE)
          //update progress bar for skipped ruptures
          continue;

        // indicate that a source has been used
        sourceUsed = true;

        // get the number of ruptures for the current source
        int numRuptures = source.getNumRuptures();

        // loop over these ruptures
        for(int n=0; n < numRuptures ; n++) {

          // get the rupture probability
          qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

          // set the PQkRup in the IMR
          try {
            imr.setEqkRupture(source.getRupture(n));
          } catch (Exception ex) {
            System.out.println("Parameter change warning caught");
          }

          // get the conditional probability of exceedance from the IMR
          condProb = imr.getExceedProbability(this.IML_VALUE);

          // For poisson source
          hazVal = hazVal*StrictMath.pow(1-qkProb,condProb);
        }
      }

      // finalize the hazard function
      if(sourceUsed) {
        //System.out.println("HazVal:"+hazVal);
        hazVal = 1-hazVal;
      }
      else
        hazVal = 0.0;
      //System.out.println("HazVal: "+hazVal);
      probVals.add(new Double(hazVal));
    }

    return probVals;
  }

  /**
   * generates the output directories for the step with timestamp labelling.
   * @return
   */
  private String getStepDirName(){
    String str =getSTEP_DateTimeInfo().replace(' ','_');
    return str;
  }

  public static void main(String[] args) {
    STEP_DataSetGenerator stepDataGenerator = new STEP_DataSetGenerator();
  }

  /**
   * reads the first line from the STEP Delta rates file on thw website to get its
   * data and time info
   * @return
   */
  private String getSTEP_DateTimeInfo(){
    try{
      URL url = new URL(this.DELTA_RATES_FILE_NAME);
      URLConnection uc = url.openConnection();
      BufferedReader tis =
          new BufferedReader(new InputStreamReader((InputStream) uc.getContent()));
      String str = tis.readLine();
      return str;
    }catch(Exception e){
      System.out.println("No Internet Connection");
    }
    return null;
  }

  /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning( ParameterChangeWarningEvent e ){

    String S =  " : parameterChangeWarning(): ";

    WarningParameterAPI param = e.getWarningParameter();

    //System.out.println(b);
    param.setValueIgnoreWarning(e.getNewValue());

  }
}

