package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DecimalFormat;


import org.scec.sha.earthquake.rupForecastImpl.step.*;
import org.scec.sha.earthquake.rupForecastImpl.step.STEP_EqkRupForecast;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.WarningParameterAPI;
import org.scec.param.WarningDoubleParameter;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.calc.HazardMapCalculator;
import org.scec.sha.earthquake.*;
import org.scec.sha.gui.infoTools.ConnectToCVM;
import org.scec.util.*;
import org.scec.data.*;
import org.scec.data.region.*;
/**
 * <p>Title: STEP_DataSetGenerator</p>
 * <p>Description: This class generates the Dataset for the STEP Map which includes the
 * BackGround, STEP addon and combined result for both</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @created :Sept 03, 2003
 * @version 1.0
 */

public class STEP_DataSetGeneratorTest implements ParameterChangeWarningListener{


  private final static String DELTA_RATES_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";

   private final double MIN_LAT= 33.7;
  //private final double MIN_LAT=32;
  private final double MAX_LAT= 34.4;
  //private final double MAX_LAT= 42.2;
  private final double MIN_LON = -119.0 ;
  //private final double MIN_LON = -124.6;
  private final double MAX_LON= -118.20;
  //private final double MAX_LON= -112;
  private final double GRID_SPACING= 0.1;

  private static final double IML_VALUE = Math.log(0.126);
  private ArrayList latVals;
  private ArrayList lonVals;
  //list to store the Wills Site Class Value
  private ArrayList willSiteClassVals ;
  DecimalFormat format = new DecimalFormat("0.00##");


  private static final boolean D = true;

  public STEP_DataSetGeneratorTest() {
    // make the forecast
    STEP_EqkRupForecast forecast=null;
    try{
      if(D)
        System.out.println("Creating the STEP forecast");
      forecast = new STEP_EqkRupForecast();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println("No internet connection available");
    }

    latVals = new ArrayList();
    lonVals = new ArrayList();
    //list to store the Wills Site Class Value
    willSiteClassVals = new ArrayList();

    if(D)
        System.out.println("Creating the ShakeMap Attenuation Relationship object");
    // make the imr
    ShakeMap_2003_AttenRel attenRel = new ShakeMap_2003_AttenRel(this);
    // set the im as PGA
    attenRel.setIntensityMeasure(attenRel.PGA_NAME);
    // set the vs30
    attenRel.getParameter(attenRel.WILLS_SITE_NAME).setValue(attenRel.WILLS_SITE_D);
    //make the Gridded Region object
    if(D)
      System.out.println("Creating SitesInGriddedRectangularRegion Object");
    SitesInGriddedRectangularRegion region = new SitesInGriddedRectangularRegion(MIN_LAT,
        MAX_LAT,MIN_LON,MAX_LON,GRID_SPACING);
    region.addSiteParams(attenRel.getSiteParamsIterator());

    int numSites = region.getNumGridLocs();

    //adding the numSites to the lat and Lon ArrayList
    for(int i=0;i<numSites;++i){

      latVals.add(new Double(format.format(region.getSite(i).getLocation().getLatitude())));
      lonVals.add(new Double(format.format(region.getSite(i).getLocation().getLongitude())));
    }

    //generating the file for the Wills site class values if it already not exists
    if(D)
      System.out.println("Getting the Wills Site Class Values for the Region");
      try{
        willSiteClassVals = ConnectToCVM.getWillsSiteTypeFromCVM(MIN_LON,MAX_LON,MIN_LAT,
        MAX_LAT,GRID_SPACING);
      }catch(Exception e){
        System.out.println("could not connect with wills site class servlet");
        e.printStackTrace();
      }

    try{
      if(D)
        System.out.println("Updating forecast for just doing Addon Rates");
      //updating the STEP forecast for the STEP Addon Probabilities
      forecast.getParameter(forecast.SEIS_TYPE_NAME).setValue(forecast.SEIS_TYPE_ADD_ON);
      forecast.updateForecast();
      //getting the name of the STEP data(XYZ )file from the first line on the STEP website which basically tells the time of updation
      //creating the dataFile for the STEP Addon Probabilities
      ArrayList stepAddonProbVals = new ArrayList();

      //File addonFile = new File(this.STEP_DIR+stepDirName+this.STEP_ADDON_FILE_SUFFIX);
      if(D)
        System.out.println("Getting the Prob. values for the addon rates for STEP");

        stepAddonProbVals = getProbVals_faster(attenRel,region,(EqkRupForecast)forecast);

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
  private ArrayList getProbVals(ShakeMap_2003_AttenRel imr,SitesInGriddedRectangularRegion region,
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

    if(D)
      System.out.println("Number of sites:"+numSites);
    if(D)
      System.out.println("Starting with hazard calculations for STEP");
    for(int j=0;j<numSites;++j){
      double hazVal =1;
      double condProb =0;
      Site site = region.getSite(j);
      imr.setSite(site);
      //adding the wills site class value for each site
      String willSiteClass = (String)this.willSiteClassVals.get(j);
      //only add the wills value if we have a value available for that site else leave default "D"
      if(!willSiteClass.equals("NA"))
        imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(willSiteClass);
      else
        imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(imr.WILLS_SITE_D);

      // loop over sources
      for(i=0;i < numSources ;i++) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(i);

        // compute it's distance from the site and skip if it's too far away
        distance = source.getMinDistance(site);
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
      if(D)
        System.out.println("Calculation completed for site#"+j+" with Lat:"+site.getLocation().getLatitude()+
                           " Lon:"+site.getLocation().getLongitude()+ "  with prob val:"+hazVal);
    }

    if(D)
      System.out.println("Done with Hazard Calc. for Addon Rates for STEP");
    return probVals;
  }


  /**
   * HazardCurve Calculator for the STEP
   * @param imr : ShakeMap_2003_AttenRel for the STEP Calculation
   * @param region
   * @param eqkRupForecast : STEP Forecast
   * @returns the ArrayList of Probability values for the given region
   */
  private ArrayList getProbVals_faster(ShakeMap_2003_AttenRel imr,SitesInGriddedRectangularRegion region,
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
    if(D)
      System.out.println("Number of sites for faster method:"+numSites);
    if(D)
      System.out.println("Starting with hazard calculations for STEP");
    for(int j=0;j<numSites;++j){
      double hazVal =1;
      double condProb =0;
      Site site = region.getSite(j);
      imr.setSite(site);
      //adding the wills site class value for each site
      String willSiteClass = (String)this.willSiteClassVals.get(j);
      //only add the wills value if we have a value available for that site else leave default "D"
      if(!willSiteClass.equals("NA"))
        imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(willSiteClass);
      else
        imr.getSite().getParameter(imr.WILLS_SITE_NAME).setValue(imr.WILLS_SITE_D);

      // loop over sources
      for(i=0;i < numSources ;i++) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(i);

        // compute it's distance from the site and skip if it's too far away
        distance = source.getMinDistance(site);
        if(distance > MAX_DISTANCE)
          //update progress bar for skipped ruptures
          continue;

        // indicate that a source has been used
        sourceUsed = true;

        hazVal *= (1.0 - imr.getTotExceedProbability((PointEqkSource)source,IML_VALUE));

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
      if(D)
        System.out.println("Calculation completed for site#"+j+" with Lat:"+site.getLocation().getLatitude()+
                           " Lon:"+site.getLocation().getLongitude()+ "  with prob val:"+hazVal);
    }
    if(D)
      System.out.println("Done with Hazard Calc. for Addon Rates for STEP");
    return probVals;
  }




  public static void main(String[] args) {
    STEP_DataSetGeneratorTest stepDataGeneratorTest = new STEP_DataSetGeneratorTest();
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

