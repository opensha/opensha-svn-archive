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


public class STEP_AlaskanPipeEqkRupForecastObject implements ERF_API,java.io.Serializable{



  //timeSpan Object
  private TimeSpan timeSpan;
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

  // vector to hold the sources
  Vector sources;


  public STEP_AlaskanPipeEqkRupForecastObject(TimeSpan time, ArrayList inputFileLines) {
    setTimeSpan(time);
    makeSources(inputFileLines);
  }

  /**
   * Returns the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return sources.size();
  }


  /**
   *  This returns a list of sources (contains only one here)
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){
    return sources;
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
    return (ProbEqkSource) sources.get(iSource);
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
   * Make the delta rate sources
   *
   */
  private  void makeSources(ArrayList inputFileLines) {
    // Debug
    String S = ": makeSources(): ";

    this.sources = new Vector();
    double lat, lon;
    double duration = timeSpan.getDuration();

    IncrementalMagFreqDist magFreqDist;
    PointPoissonEqkSource ptSource;

    // Get iterator over input-file lines
    ListIterator it = inputFileLines.listIterator();

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

        if(i <20)
          magFreqDist.set(i,0.0);
        else {
          // Lucy's 1 day --> 18 months correction, divided by the number of days to get the rate per day
          //          rate *= 230/timeSpan.getDuration();
          // rupLen correction
          double rupLen = Math.pow(10.0,-3.55 + 0.74*magFreqDist.getX(i) );
          //          if (D) System.out.println("rupLen(mag="+magFreqDist.getX(i)+")="+rupLen);
          rate *= rupLen/10.6;
          magFreqDist.set(i,rate);
        }
      }

      ptSource = new PointPoissonEqkSource(new Location(lat,lon,DEPTH),magFreqDist,duration,RAKE,DIP);
      sources.add(ptSource);
    }
  }
}