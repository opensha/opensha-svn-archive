package org.scec.sha.earthquake;

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
import org.scec.sha.magdist.GuttenbergRichterMagFreqDist;
import org.scec.exceptions.FaultException;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;


/**
 * <p>Title: Frankel96_EqkRupForecast</p>
 * <p>Description:Frankel 1996 Earthquake Rupture Forecast. This class
 * reads the 3 given files namely Frankel96_CALA.char, Frankel96_CALA.char
 *  and Frankel96_CALB.gr .
 * Then it creates earthquake rupture objects according to the rates.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @Date : Aug 31, 2002
 * @version 1.0
 */

public class Frankel96_EqkRupForecast implements EqkRupForecastAPI {

  /**
   * @todo variables
   */

  //for Debug purposes
  private static String  C = new String("Frankel96_EqkRupForecast");
  private boolean D = true;

  private double GRID_SPACING = 1.0;
  private double B_VALUE =0.9;
  private double MAG_LOWER = 6.5;
  private double DELTA_MAG = 0.1;

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
  private final static int TYPE_A_CHAR_FLT = 0;
  private final static int TYPE_B_CHAR_GR_FLT = 1;

  private final static String FILE_A_CHAR_EQK="Frankel96_CALA.char";
  private final static String FILE_B_CHAR_GR_EQK="CALB.both";

  /**
   * definition of the vectors for storing the data corresponding to the file type
   */
  private Vector FrankelA_CharEqkSources = new Vector();
  private Vector FrankelB_CharEqkSources = new Vector();
  private Vector FrankelB_GR_EqkSources = new Vector();

  /**
   * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
   */
  private double timeSpan;
  private TimeSpan time;

  /**
   * This constructor reads 3 files and saves the soures in the respective vectors of sources
   *
   * No argument constructor
   */
  public Frankel96_EqkRupForecast() {
    readFrankel96_File(0);
    readFrankel96_File(1);
  }

  /**
   * Read the Characteristic files
   *
   * @param faultType : It can be TYPE_A_CHAR_FLT or TYPE_B_CHAR_FLT or TYPE_B_GR_FLT
   * @throws FaultException
   */
  private  void readFrankel96_File(int faultType) throws FaultException{

    // Debug
    String S = C + ": readFrankel96_Char: ";
    if( D ) System.out.println(S + "Starting");
    String fileName="";
    GriddedFaultFactory factory;
    GuttenbergRichterMagFreqDist guttenbergRichter;
    //file to be read based on the flag that is received as the parameter
    // variable declaration
    ArrayList rawFaultTraceData = null;
    String  faultName="", temp;
    int i ;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    int rake=0;
    double mag=0,rate=0,dip=0,downDipWidth=0,depthToTop=0;
    double bValue=0,magLower=0,magUpper=0,deltaMag=0;

    // Load in from file the data
    if(faultType == TYPE_A_CHAR_FLT)
      fileName = new String (FILE_A_CHAR_EQK);
    else if(faultType == TYPE_B_CHAR_GR_FLT)
      fileName = new String(FILE_B_CHAR_GR_EQK);


    if( D ) System.out.println(S + "Loading file = " + fileName );
    try{ rawFaultTraceData = FileUtils.loadInCharFile( fileName ); }
    catch( FileNotFoundException e){ System.out.println(S + e.toString()); }
    catch( IOException e){ System.out.println(S + e.toString());}

        // Exit if no data found in list
        if( rawFaultTraceData == null) throw new
            FaultException(S + "No data loaded from file. File may be empty or doesn't exist.");

        // Loop over data parsing and building traces, then add to list
        ListIterator it = rawFaultTraceData.listIterator();
        //reading the first line from the file
        while( it.hasNext() ){
          StringTokenizer st = new StringTokenizer(it.next().toString());
          while(st.hasMoreTokens()){
            //skipping the first word(that tells that it is char type faults)
            st.nextToken();
            //taking the 2nd word that tells that it is what fault type
            String token= new String(st.nextToken());

            //for Strike slip fault
            if(Integer.parseInt(token) == 1)
              rake =0;

            //for reverse fault
            if(Integer.parseInt(token) == 2)
              rake =90;

            //for normal fault
            if(Integer.parseInt(token) == 3)
              rake =-90;

            //reading the fault name
            faultName = new String(st.nextToken());
            if(D) System.out.println(C+":FaultName::"+faultName);
          }

          //reading the next line from the file
          st = new StringTokenizer(it.next().toString());

          // if we are reading the characteristic file
          if(faultType==TYPE_A_CHAR_FLT || faultType==TYPE_B_CHAR_GR_FLT) {
            while(st.hasMoreTokens()){
            //reading the mag
             mag=Double.parseDouble(st.nextToken());
             //reading the rate
             rate=Double.parseDouble(st.nextToken());
             if(faultType==TYPE_B_CHAR_GR_FLT && (mag>6.5)) {
               //reading the b-value
               bValue=B_VALUE;
               //reading the MagLower
               magLower=MAG_LOWER;
               //reading the MagUpper
               magUpper=mag;
               //reading the DeltaMag
               deltaMag=DELTA_MAG;

             }
            }
           }

          //reading the third line from  the file
          st=new StringTokenizer(it.next().toString());
          while(st.hasMoreTokens()){
            // reading the dip
            dip=Double.parseDouble(st.nextToken());
            //reading the downDipWidth
            downDipWidth=Double.parseDouble(st.nextToken());
            //reading the Depth to top
            depthToTop=Double.parseDouble(st.nextToken());
          }

          //reading the 4 line from the file that tells about the data points for faults
          int numOfDataLines=Integer.parseInt(it.next().toString().trim());
          // Calculate derived variables
          upperSeismoDepth = depthToTop;
          lowerSeismoDepth = depthToTop + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));

          FaultTrace faultTrace= new FaultTrace(faultName);

          //based on the num of the data lines reading the lat and long points for rthe faults
          for(i=0;i<numOfDataLines;++i) {
              if( !it.hasNext() ) throw ERR;
              st =new StringTokenizer(it.next().toString().trim());

              try{ lat = new Double(st.nextToken()).doubleValue(); }
              catch( NumberFormatException e){ throw ERR; }
              try{ lon = new Double(st.nextToken()).doubleValue(); }
              catch( NumberFormatException e){ throw ERR; }

              Location loc = new Location(lat, lon, upperSeismoDepth);
              faultTrace.addLocation( (Location)loc.clone());
              if( D ) System.out.println(S + "Location" + loc.toString());
          }
         // reverse data ordering if dip negative, make positive and reverse trace order
          if( dip < 0 ) {
             faultTrace.reverse();
             dip *= -1;
          }

          if( D ) System.out.println(C+":faultTrace::"+faultTrace.toString());



          // value of gridspacing has been set to 1 km
          factory = new FrankelGriddedFaultFactory(faultTrace,
                                                   dip,
                                                   upperSeismoDepth,
                                                   lowerSeismoDepth,
                                                   GRID_SPACING);

          GriddedSurfaceAPI surface = factory.getGriddedSurface();

          if(faultType == TYPE_B_CHAR_GR_FLT && mag>6.5){
            Frankel96_GR_EqkSource frankel96_GRF = new Frankel96_GR_EqkSource(rake,bValue,magLower,
                                                   magUpper,deltaMag,rate,(EvenlyGriddedSurface)surface);
            this.FrankelB_GR_EqkSources.add(frankel96_GRF);
          }

          if( faultType==TYPE_A_CHAR_FLT || (faultType==TYPE_B_CHAR_GR_FLT && mag<=6.5)) {
            Frankel96_CharEqkSource frankel96_CharF = new  Frankel96_CharEqkSource(rake,mag,rate,
                                                      (EvenlyGriddedSurface)surface);

            if(faultType ==  TYPE_A_CHAR_FLT)
              this.FrankelA_CharEqkSources.add(frankel96_CharF);
            if(faultType ==  TYPE_B_CHAR_GR_FLT  && mag<=6.5)
              this.FrankelB_CharEqkSources.add(frankel96_CharF);
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
    int size = this.FrankelA_CharEqkSources.size();
    for( int i =0; i<size; ++i)
      ((Frankel96_CharEqkSource)FrankelA_CharEqkSources.get(i)).setTimeSpan(yrs);

    size = this.FrankelB_CharEqkSources.size();
    for( int i =0; i<size; ++i)
      ((Frankel96_CharEqkSource)FrankelB_CharEqkSources.get(i)).setTimeSpan(yrs);

    size = this.FrankelB_GR_EqkSources.size();
    for( int i =0; i<size; ++i)
      ((Frankel96_GR_EqkSource)FrankelB_GR_EqkSources.get(i)).setTimeSpan(yrs);


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
      return getSourceVector(iSource).getNumRuptures();
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
      return getSourceVector(iSource).getRuptureClone(nRupture);
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
       return getSourceVector(iSource).getRupture(nRupture);
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
      return getSourceVector(iSource);
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer value specifying the number of earthquake sources
     */
    public int getNumSources(){
      return (FrankelA_CharEqkSources.size() + FrankelB_CharEqkSources.size() + FrankelB_GR_EqkSources.size());
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
      if(probEqkSource instanceof Frankel96_CharEqkSource){
          Frankel96_CharEqkSource probEqkSource1 = (Frankel96_CharEqkSource)probEqkSource;
          ProbEqkRupture r = probEqkSource1.getRupture(0);
          r.
          Frankel96_CharEqkSource frankel96_Char = new Frankel96_CharEqkSource(;

      }*/

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
      * Get the list of all earthquake sources. Clone is returned.
      * All the 3 different Vector source List are combined into the one Vector list
      * So, list can be save in Vector and this object subsequently destroyed
      *
      * @return Vector of Prob Earthquake sources
      */
     public Vector  getSourceList(){
       Vector v =new Vector();
       int charASize = FrankelA_CharEqkSources.size();
       int charBSize = FrankelB_CharEqkSources.size();
       int grBSize = FrankelB_GR_EqkSources.size();
       for(int i=0;i<charASize;++i)
         v.add(FrankelA_CharEqkSources.get(i));
       for(int i=0;i<charBSize;++i)
         v.add(FrankelB_CharEqkSources.get(i));
       for(int i=0;i<grBSize;++i)
         v.add(FrankelB_GR_EqkSources.get(i));

       return v;
     }



    /**
     * This method helps in finding the object stored in the vector for char and gr
     * faultsthat corresponds to that parameter source.
     *
     * first source starts from 0
     *
     * @param iSource
     * @return the object of the char or gr type vector list depending to which list
     * the source corresponds
     */
    private ProbEqkSource getSourceVector(int iSource){
      int charASize = FrankelA_CharEqkSources.size();
      int charBSize = FrankelB_CharEqkSources.size();
      int grBSize = FrankelB_GR_EqkSources.size();
      int i =0;
      if(iSource < charASize){
        while(i != iSource)
           ++i;
        Frankel96_CharEqkSource frankel96_CharEqkSource = (Frankel96_CharEqkSource)FrankelA_CharEqkSources.get(i) ;
        return frankel96_CharEqkSource;
      }
      else if(iSource < (charASize + charBSize)){
        i= charASize ;
        while(i!=iSource)
           ++i;
        Frankel96_CharEqkSource frankel96_CharEqkSource = (Frankel96_CharEqkSource)FrankelB_CharEqkSources.get(i-charASize);
        return frankel96_CharEqkSource;
      }
      else if(iSource <= (charASize + charBSize + grBSize)){
        i=charASize + charBSize;
        while(i!=iSource)
          ++i;
        Frankel96_GR_EqkSource frankel96_GR_EqkSource = (Frankel96_GR_EqkSource)FrankelB_GR_EqkSources.get(i - charASize - charBSize) ;
        return frankel96_GR_EqkSource;
      }

      return null;
    }


    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
   public String getName(){
     return "Frankel96_EqkRupForecast";
   }

}