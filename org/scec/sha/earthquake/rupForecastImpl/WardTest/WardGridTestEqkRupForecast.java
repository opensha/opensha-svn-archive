package org.scec.sha.earthquake.rupForecastImpl.WardTest;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;


import org.scec.util.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.FrankelGriddedFaultFactory;
import org.scec.sha.fault.GriddedFaultFactory;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
import org.scec.exceptions.FaultException;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;
import org.scec.sha.earthquake.*;

/**
 * <p>Title: WardGridTestEqkRupForecast</p>
 * <p>Description:Frankel 1996 Earthquake Rupture Forecast. This class
 * reads the file namely ?????????.
 * Then it creates earthquake rupture objects according to the GR params.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @Date : Aug 31, 2002
 * @version 1.0
 */

public class WardGridTestEqkRupForecast extends EqkRupForecast {

  /**
   * @todo variables
   */

  //for Debug purposes
  private static String  C = new String("Ward Grid Test");
  private boolean D =true;

  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format."
    );

  /**
   * These are the decelaration of the static varibles to determine the type
   * and name of the file to be read
   */
  private final static String FILE_PATH = "org/scec/sha/earthquake/rupForecastImpl/WardTest/";
  private final static String FILE=FILE_PATH + "WardTestRELM.dat";

  /**
   * definition of the vectors for storing the data corresponding to the file type
   */
  private Vector wardGR_EqkSources = new Vector();

  /**
   * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
   */
  private double timeSpan;
  private TimeSpan time;

  /**
   * This constructor file and creates the sources
   *
   * No argument constructor
   */
  public WardGridTestEqkRupForecast() {
      readFileAndMakeSources();
  }

  /**
   * @throws FaultException
   */
  private void readFileAndMakeSources() throws FaultException{

    // Debug
    String S = C + ": readField: ";
    if( D ) System.out.println(S + "Starting");

    ArrayList rawData = null;

    // variable declaration
    double lat=0, lon=0, cumRate=0, bValue=0, magLower=0,magUpper=0;

    double delta=0.1;
    double rake = 0;      // all treated as strike slip
    double depth = 0;

    if( D ) System.out.println(S + "Loading file = " + FILE );

    try{ rawData = FileUtils.loadInCharFile( FILE ); }
    catch( FileNotFoundException e){ System.out.println(S + e.toString()); }
    catch( IOException e){ System.out.println(S + e.toString());}

        // Exit if no data found in list
        if( rawData == null) throw new
            FaultException(S + "No data loaded from file. File may be empty or doesn't exist.");

        // Loop over data parsing and building traces, then add to list
        ListIterator it = rawData.listIterator();
        it.next();
        //reading the first line from the file
        while( it.hasNext() ){
          StringTokenizer st = new StringTokenizer(it.next().toString());
          while(st.hasMoreTokens()){

            lat = Double.parseDouble(st.nextToken());
            lon = Double.parseDouble(st.nextToken());
            st.nextToken(); // skip moment rate since it's redundant
            cumRate = Double.parseDouble(st.nextToken());
            bValue =   0.0-(Double.parseDouble(st.nextToken()));  // note sign change
            magLower = Double.parseDouble(st.nextToken());
            magUpper = Double.parseDouble(st.nextToken());

          }

          if(cumRate >= Double.MIN_VALUE) {
              PointGR_EqkSource pointGR_EqkSource = new PointGR_EqkSource(lat, lon,
                                                  depth, rake, cumRate, bValue,
                                                  magLower, magUpper, delta);

              this.wardGR_EqkSources.add(pointGR_EqkSource);

              if (D) System.out.println("NonZeroData: "+lat+"  "+lon+"  "+cumRate+"  "+bValue+"  "+magLower+"  "+magUpper);
          }

        }

    // Done
    if( D ) System.out.println(S + "Ending");

  }

  /**
   * sets the timeSpan field
   * @param yrs : have to be modfied from the double varible to the timeSpan field variable
   */
  public void setTimeSpan(double yrs){
    timeSpan =yrs;
    int size = this.wardGR_EqkSources.size();
    for( int i =0; i<size; ++i)
      ((PointGR_EqkSource)wardGR_EqkSources.get(i)).setTimeSpan(yrs);

  }




  /**
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
    time = new TimeSpan();
    time= timeSpan;
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
     * Get the ith rupture of the source. this method DOES NOT return reference
     * to the object. So, when you call this method again, result from previous
     * method call is valid. This behavior is in contrast with
     * getRupture(int source, int i) method
     *
     * @param source
     * @param i
     * @return
     */
    public EqkRupture getRuptureClone(int iSource, int nRupture) {
      return getSource(iSource).getRuptureClone(nRupture);
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
    public EqkRupture getRupture(int iSource, int nRupture) {
       return getSource(iSource).getRupture(nRupture);
    }

    /**
     * Return the earhthquake source at index i. This methos returns the reference to
     * the class variable. So, when you call this method again, result from previous
     * method call is no longer valid.
     * this is secret, fast but dangerous method
     *
     * @param iSource : index of the source needed
     *
     * @return Returns the ProbEqkSource at index i
     *
     */
    public ProbEqkSource getSource(int iSource) {
      return (ProbEqkSource) wardGR_EqkSources.get(iSource);
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer value specifying the number of earthquake sources
     */
    public int getNumSources(){
      return wardGR_EqkSources.size();
    }

    /**
     * Return the earthquake source at index i. This methos DOES NOT return the
     * reference to the class variable. So, when you call this method again,
     * result from previous method call is still valid. This behavior is in contrast
     * with the behavior of method getSource(int i)
     *
     * @param iSource : index of the source needed
     *
     * @return Returns the ProbEqkSource at index i
     *
     * FIX:FIX :: This function has not been implemented yet. Have to give a thought on that
     *
     */
    public ProbEqkSource getSourceClone(int iSource) {
      return null;
      /*ProbEqkSource probEqkSource =getSource(iSource);
      if(probEqkSource instanceof WardGridTestCharEqkSource){
          WardGridTestCharEqkSource probEqkSource1 = (WardGridTestCharEqkSource)probEqkSource;
          ProbEqkRupture r = probEqkSource1.getRupture(0);
          r.
          WardGridTestCharEqkSource frankel96_Char = new WardGridTestCharEqkSource(;

      }*/

    }




    /**
     * Return  iterator over all the earthquake sources
     *
     * @return Iterator over all earhtquake sources
     */
    public Iterator getSourcesIterator() {

        return wardGR_EqkSources.iterator();
    }

     /**
      * Get the list of all earthquake sources. Clone is returned.
      * All the 3 different Vector source List are combined into the one Vector list
      * So, list can be save in Vector and this object subsequently destroyed
      *
      * @return Vector of Prob Earthquake sources
      */
     public Vector  getSourceList(){

       return wardGR_EqkSources;
     }




    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
   public String getName(){
     return C;
   }

   /**
    * this function is needed to prepare for the forecast
    */
   public void updateForecast() {
     throw new UnsupportedOperationException(C+"this function Not implemented.");
   }

   public static void main(String args[]) {

      WardGridTestEqkRupForecast test = new WardGridTestEqkRupForecast();

      //test.setTimeSpan();

   }

}