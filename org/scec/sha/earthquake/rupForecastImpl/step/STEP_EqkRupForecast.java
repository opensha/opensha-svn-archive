package org.scec.sha.earthquake.rupForecastImpl.step;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.net.URL;
import java.lang.reflect.*;

import org.scec.param.*;
import org.scec.util.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.magdist.IncrementalMagFreqDist;
import org.scec.data.Location;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.data.TimeSpan;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;


/**
 * <p>Title: STEP_EqkRupForecast</p>
 * <p>Description:
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author :Edward Field
 * @Date : March 24, 2003
 * @version 1.0
 */

  public class STEP_EqkRupForecast extends EqkRupForecast
    implements ParameterChangeListener{

  //for Debug purposes
  private static String  C = new String("STEP_EqkRupForecast");
  private boolean D = false;

  // name of this ERF
  private static String  name = new String("STEP ERF");

  // Input file name
  //private final static String INPUT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/step/SoCalDeltaRates.txt";
  private final static String INPUT_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";

  // ArrayList of input file lines
  private ArrayList inputFileLines;

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


  /**
   *
   * No argument constructor
   */
  public STEP_EqkRupForecast() throws Exception{

    // read the lines of the input files into a list
    inputFileLines = FileUtils.loadFile( new URL(INPUT_FILE_NAME) );

    // Create the timeSpan & set its constraints
    StringTokenizer st = new StringTokenizer(inputFileLines.get(0).toString());
    int year =  (new Integer(st.nextToken())).intValue();
    int month =  (new Integer(st.nextToken())).intValue();
    int day =  (new Integer(st.nextToken())).intValue();
    int hour =  (new Integer(st.nextToken())).intValue();
    int minute =  (new Integer(st.nextToken())).intValue();
    int second =  (new Integer(st.nextToken())).intValue();

    if(D) System.out.println("year="+year+"; month="+month+"; day="+day+"; hour="+
                             hour+"; minute="+minute+"; second="+second);

    st = new StringTokenizer(inputFileLines.get(1).toString());
    double duration = (new Double(st.nextToken())).doubleValue();
    if(D) System.out.println("duration="+duration);

    this.timeSpan = new TimeSpan(TimeSpan.SECONDS,TimeSpan.DAYS);
    timeSpan.setStartTime(year,month,day,hour,minute,second);
    timeSpan.setDuration(duration);
    timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR, year,year);
    timeSpan.setStartTimeConstraint(TimeSpan.START_MONTH, month,month);
    timeSpan.setStartTimeConstraint(TimeSpan.START_DAY, day,day);
    timeSpan.setStartTimeConstraint(TimeSpan.START_HOUR, hour,hour);
    timeSpan.setStartTimeConstraint(TimeSpan.START_MINUTE, minute,minute);
    timeSpan.setStartTimeConstraint(TimeSpan.START_SECOND, second,second);
    timeSpan.setDuractionConstraint(duration,duration);

    if (D) System.out.println("Start-Time Calendar toString: \n"+(timeSpan.getStartTimeCalendar()).toString());

    if (D) System.out.println("Number of lines in file = "+inputFileLines.size());

    // Make the sources
    makeSources();
  }


  /**
  * Make the sources
  *
  */
  private  void makeSources() {

    // Debug
    String S = C + ": makeSources(): ";
    if( D ) System.out.println(S + "Starting");

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
        magFreqDist.set(i,rate);
      }

      ptSource = new PointPoissonEqkSource(new Location(lat,lon,DEPTH),magFreqDist,duration,RAKE,DIP);
      sources.add(ptSource);

      if(D) System.out.println(C+"makeSources(): numRups="+ptSource.getNumRuptures()+
                               " for source "+sources.size());
    }
  }





  /**
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
  }


    /**
     * Returns the  ith earthquake source
     *
     * @param iSource : index of the source needed
    */
    public ProbEqkSource getSource(int iSource) {

      return (ProbEqkSource) sources.get(iSource);
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer
     */
    public int getNumSources(){
      return sources.size();
    }

    /**
     * Return  iterator over all the earthquake sources
     *
     * @return Iterator over all earhtquake sources
     */
    public Iterator getSourcesIterator() {
      Iterator i = getSourceList().iterator();
      return i;
    }

     /**
      * Get the list of all earthquake sources.
      *
      * @return Vector of Prob Earthquake sources
      */
     public Vector getSourceList(){
       return sources;
     }


    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
   public String getName(){
     return name;
   }


   /**
    * update the forecast
    **/

   public void updateForecast() {

     // make sure something has changed
//     if(parameterChangeFlag) {


   }

   /**
    *  This is the main function of this interface. Any time a control
    *  paramater or independent paramater is changed by the user in a GUI this
    *  function is called, and a paramater change event is passed in.
    *
    *  This sets the flag to indicate that the sources need to be updated
    *
    * @param  event
    */
   public void parameterChange( ParameterChangeEvent event ) {
     parameterChangeFlag=true;
   }


   // this is temporary for testing purposes
   public static void main(String[] args) throws Exception{

     STEP_EqkRupForecast forecast = new STEP_EqkRupForecast();
     System.out.println("startTimeFromCal:\n " + forecast.getTimeSpan().getStartTimeCalendar().toString());
     System.out.println("Duration: " + forecast.getTimeSpan().getDuration()+"  "+
                        forecast.getTimeSpan().getDurationUnits());
     System.out.println("getNumSources(): "+forecast.getNumSources());
     ProbEqkRupture rup;
     double rate;

     // check first one
     int index = 0;
     PointPoissonEqkSource qkSrc = (PointPoissonEqkSource) forecast.getSource(index);
     System.out.println("getNumRuptures(): "+qkSrc.getNumRuptures());
     double duration = qkSrc.getDuration();
     for(int i=0;i<qkSrc.getNumRuptures();i++) {
       rup = qkSrc.getRupture(i);
       Location loc = (Location) rup.getRuptureSurface().get(0,0);
       if(i==0) System.out.println("First Source:\n" + loc.getLongitude()+"  "+loc.getLatitude());
       rate = -Math.log(1-rup.getProbability())/duration;
       System.out.println((float)rup.getMag()+"  "+rate);
     }
     // check last one
     index = forecast.getNumSources()-1;
     qkSrc = (PointPoissonEqkSource) forecast.getSource(index);
     System.out.println("getNumRuptures(): "+qkSrc.getNumRuptures());
     duration = qkSrc.getDuration();
     for(int i=0;i<qkSrc.getNumRuptures();i++) {
       rup = qkSrc.getRupture(i);
       Location loc = (Location) rup.getRuptureSurface().get(0,0);
       if(i==0) System.out.println("Last Source:\n" + loc.getLongitude()+"  "+loc.getLatitude());
       rate = -Math.log(1-rup.getProbability())/duration;
       System.out.println((float)rup.getMag()+"  "+rate);
     }

   }

}
