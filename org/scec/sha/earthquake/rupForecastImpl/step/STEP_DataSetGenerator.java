package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import java.net.*;
import java.util.*;
import org.scec.sha.earthquake.rupForecastImpl.step.*;
import org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.WarningParameterAPI;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.sha.earthquake.*;
import org.scec.util.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class STEP_DataSetGenerator implements ParameterChangeWarningListener{


  private final static String DELTA_RATES_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";

  // VS 30 value to be set in the IMR
  private final Double VS_30= new Double(760);

  private final double MIN_LAT= 32.45;
  private final double MAX_LAT= 36.60;
  private final double MIN_LON = -121.45 ;
  private final double MAX_LON= -114.50;
  private final double GRID_SPACING= .5;
  private static final String STEP_DIR = "step/";
  private static final String STEP_BACKGROUND_FILE = "backGround.txt";
  private static final String STEP_ADDON_FILE_SUFFIX = "_addon.txt";
  private static final String STEP_COMBINED_FILE_SUFFIX = "_both.txt";
  private static final double IML_VALUE = 0.126;
  private Vector latVals = new Vector();
  private Vector lonVals = new Vector();


  public STEP_DataSetGenerator() {
    // make the forecast
    STEP_EqkRupForecast forecast=null;
    try{
      forecast = new STEP_EqkRupForecast();
    }catch(Exception e){
      System.out.println("No internet connection available");
    }

    // make the imr
    ShakeMap_2003_AttenRel imr = new ShakeMap_2003_AttenRel(this);
    // set the im as PGA
    imr.setIntensityMeasure(imr.PGA_NAME);
    // set the vs30
    imr.getParameter(imr.VS30_NAME).setValue(VS_30);
    //make the Gridded Region object
    SitesInGriddedRegion region = new SitesInGriddedRegion(MIN_LAT, MAX_LAT, MIN_LON,
        MAX_LON, GRID_SPACING);
    region.addSiteParams(imr.getSiteParamsIterator());

    int numSites = region.getNumGridLocs();

    //adding the numSites to the lat and Lon Vector
    for(int i=0;i<numSites;++i){
      latVals.add(new Double(region.getSite(i).getLocation().getLatitude()));
      lonVals.add(new Double(region.getSite(i).getLocation().getLongitude()));
    }


    //creating the step directory in which we put all the step related files
    File stepDir = new File(this.STEP_DIR);
    if(!stepDir.isDirectory()) { // if main directory does not exist
      boolean success = (new File(STEP_DIR)).mkdir();
    }

    //MetaData String
    String metadata = "IMR Info: \n"+
                      "\t"+"Name: "+imr.getName()+"\n"+
                      "\t"+"Intensity Measure Type: "+ imr.getIntensityMeasure().getName()+"\n"+
                      "\n\n"+
                      "Region Info: \n"+
                      "\t"+"MinLat: "+region.getMinLat()+"\n"+
                      "\t"+"MaxLat: "+region.getMaxLat()+"\n"+
                      "\t"+"MinLon: "+region.getMinLon()+"\n"+
                      "\t"+"MaxLon: "+region.getMaxLon()+"\n"+
                      "\t"+"GridSpacing: "+region.getGridSpacing()+"\n"+
                      "\t"+"Site Params: "+imr.getParameter(imr.VS30_NAME).getName()+ " = "+imr.getParameter(imr.VS30_NAME).getValue().toString()+"\n"+
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
    Vector backSiesProbVals = new Vector();
    //if the file for the backGround already exists then just pick up the values for the Prob from the file
    if(backSiesFile.exists()){
      try{
        ArrayList fileLines=FileUtils.loadFile(this.STEP_DIR+this.STEP_BACKGROUND_FILE);
        ListIterator it =fileLines.listIterator();
        while(it.hasNext()){
          StringTokenizer st = new StringTokenizer((String)it.next());
          st.nextToken();
          st.nextToken();
          backSiesProbVals.add(new Double(st.nextToken().trim()));
        }
      }catch(Exception  e){
        e.printStackTrace();
      }
    }
    //if the backGround file does not already exist then create it
    else{
      backSiesProbVals = getBackSeisProbVals(imr,region,(EqkRupForecast)forecast);
      createFile(backSiesProbVals,this.STEP_DIR+this.STEP_BACKGROUND_FILE);
    }

    //metadata for the Addon Prob
    dataInfo = "Step Addon Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;

    //updating the STEP forecast for the STEP Addon Probabilities
    forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_ADD_ON);
    forecast.updateForecast();
    //creating the dataFile for the STEP Addon Probabilities
    Vector stepAddonProbVals = getBackSeisProbVals(imr,region,(EqkRupForecast)forecast);
    createFile(backSiesProbVals,this.STEP_DIR+this.getStepDirName()+this.STEP_ADDON_FILE_SUFFIX);

    //combining the backgound and Addon dataSet and wrinting the result to the file
    STEP_BackSiesDataAdditionObject addStepData = new STEP_BackSiesDataAdditionObject();
    Vector stepBothProbVals = addStepData.addDataSet(backSiesProbVals,stepAddonProbVals);
    createFile(backSiesProbVals,this.STEP_DIR+this.getStepDirName()+this.STEP_COMBINED_FILE_SUFFIX);

    dataInfo = "Step Combined(Added) Data Set for :\n"+
               "\t"+this.getSTEP_DateTimeInfo()+"\n\n"+
               metadata;


    /*fr.write(region.getMinLat()+" "+region.getMaxLat()+" "+
    region.getGridSpacing()+"\n"+region.getMinLon()+" "+
    region.getMaxLon()+" "+ region.getGridSpacing()+"\n");*/

  }


  /**
   * craetes the output xyz files
   * @param probVals : Probablity values Vector for each Lat and Lon
   * @param fileName : File to create
   */
  private void createFile(Vector probVals, String fileName){
    int size = probVals.size();
    try{
      FileWriter fr = new FileWriter(fileName);
      for(int i=0;i<size;++i)
        fr.write(latVals.get(i)+"  "+lonVals.get(i)+"  "+probVals.get(i)+"\n");
    }catch(IOException ee){
      ee.printStackTrace();
    }
  }



  /**
   * HazardCurve Calculator for the STEP
   * @param imr : ShakeMap_2003_AttenRel for the STEP Calculation
   * @param region
   * @param eqkRupForecast : STEP Forecast
   * @returns the Vector of Probability values for the given region
   */
  private Vector getBackSeisProbVals(ShakeMap_2003_AttenRel imr,SitesInGriddedRegion region,
                                     EqkRupForecast eqkRupForecast){

    Vector probVals = new Vector();
    double MAX_DISTANCE = 300;


    /* this determines how the calucations are done (doing it the way it's outlined
    in the paper SRL gives probs greater than 1 if the total rate of events for the
    source exceeds 1.0, even if the rates of individual ruptures are << 1).
    */
    boolean poissonSource = false;


    // declare some varibles used in the calculation
    double qkProb, distance;
    int k,i;

    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();

    // compute the total number of ruptures for updating the progress bar
    int totRuptures = 0;
    for(i=0;i<numSources;++i)
      totRuptures+=eqkRupForecast.getSource(i).getNumRuptures();



    // this boolean will tell us whether a source was actually used
    // (e.g., all could be outside MAX_DISTANCE)
    boolean sourceUsed = false;
    double sourceHazVal =0;
    double hazVal =0;
    double condProb =0;
    int numSites = region.getNumGridLocs();
    for(int j=0;j<numSites;++j){
      imr.setSite(region.getSite(j));
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

        // determine whether it's poissonian
        poissonSource = source.isSourcePoissonian();

        if(!poissonSource)
          sourceHazVal=0;


        // get the number of ruptures for the current source
        int numRuptures = source.getNumRuptures();

        // loop over these ruptures
        for(int n=0; n < numRuptures ; n++) {

          // get the rupture probability
          qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();

          // set the PQkRup in the IMR
          try {
            imr.setProbEqkRupture((ProbEqkRupture)source.getRupture(n));
          } catch (Exception ex) {
            System.out.println("Parameter change warning caught");
          }

          // get the conditional probability of exceedance from the IMR
          condProb = 0.4343*Math.log(imr.getExceedProbability(Math.log(this.IML_VALUE)));


          // For poisson source
          if(poissonSource)
              hazVal = hazVal*Math.pow(1-qkProb,condProb);
          // For non-Poissin source
          else
              sourceHazVal =sourceHazVal + qkProb*condProb;
        }
        // for non-poisson source:
        if(!poissonSource)
            hazVal = hazVal*(1-condProb);
      }

      // finalize the hazard function
      if(sourceUsed)
        hazVal = 1-hazVal;
      else
        hazVal = 0.0;

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

