package org.scec.sha.gui.servlets.erf;

import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;
import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.calc.MomentMagCalc;
import org.scec.util.FileUtils;
import org.scec.sha.gui.servlets.erf.STEP_ERF_AdjustableParamClass;

/**
 * <p>Title: STEP_EqkRupForecastObject</p>
 * <p>Description: Make the ERF Object for the STEP ERF as the Servlet mode</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */


public class STEP_EqkRupForecastObject implements ERF_API,java.io.Serializable{


  //Background rate file
  private final static String BACKGROUND_RATES_FILE_NAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/DailyRates96Model.txt";
  //BackGroundRate File Lines
  private ArrayList backgroundRateFileLines;

  /**
   * timespan field in yrs for now (but have to ultimately make it a TimeSpan class variable)
   */
  //  private TimeSpan timeSpan;

  private static final double RAKE=0.0;
  private static final double DIP=90.0;
  private static final double MAG_LOWER=4;
  private static final double MAG_UPPER=8;
  private static final int    NUM_MAG=41;
  private static final double DEPTH=0;
  private double oldMinMag=MAG_LOWER;


  // vectors to hold the sources
  private Vector deltaRateSources;
  private Vector backgroundRateSources;
  private Vector allSources;

  // booleans to help decide if sources need to be made
  private boolean deltaSourcesAlreadyMade = false;
  private boolean backgroundSourcesAlreadyMade = false;
  private boolean backgroundRatesFileAlreadyRead = false;

  //timeSpan Object
  private TimeSpan timeSpan;

  public STEP_EqkRupForecastObject(TimeSpan time, ArrayList inputFileLines,ParameterList param) {
    setTimeSpan(time);
    allSources = new Vector();
    String seisType = (String) param.getParameter(STEP_ERF_AdjustableParamClass.SEIS_TYPE_NAME).getValue();
    double minMag = ((Double) param.getParameter(STEP_ERF_AdjustableParamClass.MIN_MAG_PARAM_NAME).getValue()).doubleValue();
    // add delta rates if needed
    if(seisType.equals(STEP_ERF_AdjustableParamClass.SEIS_TYPE_ADD_ON) || seisType.equals(STEP_ERF_AdjustableParamClass.SEIS_TYPE_BOTH)) {
      // make them if needed
      //if(!deltaSourcesAlreadyMade || minMag != oldMinMag)
        makeDeltaRateSources(inputFileLines, minMag);
      allSources.addAll(deltaRateSources);
    }

    if(seisType.equals(STEP_ERF_AdjustableParamClass.SEIS_TYPE_BACKGROUND) || seisType.equals(STEP_ERF_AdjustableParamClass.SEIS_TYPE_BOTH)) {
     // if(!backgroundSourcesAlreadyMade || minMag != oldMinMag)
        makeBackgroundRateSources(minMag);
      allSources.addAll(backgroundRateSources);
    }
    //oldMinMag = minMag;
  }

  /**
   * Returns the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return allSources.size();
  }


  /**
   *  This returns a list of sources (contains only one here)
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){
    return allSources;
  }



  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public ProbEqkRupture getRupture(int iSource, int nRupture) {
    return getSource(iSource).getRupture(nRupture);
  }

  /**
   * Return the earhthquake source at index i.   Note that this returns a
   * pointer to the source held internally, so that if any parameters
   * are changed, and this method is called again, the source obtained
   * by any previous call to this method will no longer be valid.
   *
   * @param iSource : index of the desired source .
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public ProbEqkSource getSource(int iSource) {
    return (ProbEqkSource) allSources.get(iSource);
  }

  /**
   * Get number of ruptures for source at index iSource
   * This method iterates through the list of 3 vectors for charA , charB and grB
   * to find the the element in the vector to which the source corresponds
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource){
    return getSource(iSource).getNumRuptures();
  }


  /**
   * set the TimeSpan in the ERF
   * @param timeSpan : TimeSpan object
   */
  public void setTimeSpan(TimeSpan time) {
    this.timeSpan=time;
  }


  /**
   * Make the background rate sources
   *
   */
  private  void makeBackgroundRateSources(double minMag) {

    // Debug
    String S = ": makeBackgroundRateSources(): ";

    //read background rates file if needed
    if(!backgroundRatesFileAlreadyRead){
      try {
        backgroundRateFileLines = FileUtils.loadFile( BACKGROUND_RATES_FILE_NAME );
      } catch(Exception e) {
        throw new RuntimeException("Background file could not be loaded");
      }
      backgroundRatesFileAlreadyRead = true;
    }

    backgroundRateSources = new Vector();
    double lat, lon;
    double duration = timeSpan.getDuration();

    IncrementalMagFreqDist magFreqDist;
    PointPoissonEqkSource ptSource;

    // Get iterator over input-file lines
    ListIterator it = backgroundRateFileLines.listIterator();

    StringTokenizer st;

    while( it.hasNext() ) {

      // get next line
      st = new StringTokenizer(it.next().toString());

      // skip the event ID
      st.nextToken();

      // get lat and lon
      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());

      magFreqDist = new IncrementalMagFreqDist(MAG_LOWER,MAG_UPPER,NUM_MAG);

      // skip the mag=2, 2.1, ... 3.9
      for(int j=0; j<20; j++) st.nextToken();

      for(int i=0;i<NUM_MAG;i++) {
        double rate = Double.parseDouble(st.nextToken());
        magFreqDist.set(i,rate);
      }

      ptSource = new PointPoissonEqkSource(new Location(lat,lon,DEPTH),magFreqDist,duration,RAKE,DIP,minMag);
      if(ptSource.getNumRuptures() > 0) {
          backgroundRateSources.add(ptSource);

      }
    }
    backgroundSourcesAlreadyMade = true;
  }



  /**
   * Make the delta rate sources
   *
   */
  private  void makeDeltaRateSources(ArrayList deltaRateFileLines,double minMag) {

    // Debug
    String S = ": makeDeltaRateSources(): ";

    deltaRateSources = new Vector();
    double lat, lon;
    double duration = timeSpan.getDuration();

    IncrementalMagFreqDist magFreqDist;
    PointPoissonEqkSource ptSource;

    // Get iterator over input-file lines
    ListIterator it = deltaRateFileLines.listIterator();

    // skip first two lines
    StringTokenizer st;
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());

    while( it.hasNext() ) {

      // get next line
      st = new StringTokenizer(it.next().toString());

      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());

      magFreqDist = new IncrementalMagFreqDist(MAG_LOWER,MAG_UPPER,NUM_MAG);

      for(int i=0;i<NUM_MAG;i++) {
        double rate = Double.parseDouble(st.nextToken());
        magFreqDist.set(i,rate);
      }

      ptSource = new PointPoissonEqkSource(new Location(lat,lon,DEPTH),magFreqDist,duration,RAKE,DIP,minMag);
      if(ptSource.getNumRuptures() > 0) {
        deltaRateSources.add(ptSource);
      }
    }
    deltaSourcesAlreadyMade = true;
  }
}
