package org.scec.sha.earthquake.rupForecastImpl.Frankel96;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.scec.calc.MomentMagCalc;
import org.scec.util.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.FrankelGriddedFaultFactory;
import org.scec.sha.fault.GriddedFaultFactory;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
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

public class Frankel96_EqkRupForecast extends EqkRupForecast {

  //for Debug purposes
  private static String  C = new String("Frankel96_EqkRupForecast");
  private boolean D = false;

  private double GRID_SPACING = 1.0;
  private double B_VALUE =0.9;
  private double MAG_LOWER = 6.5;
  private double DELTA_MAG = 0.1;

  private String FAULT_CLASS_A = "A";
  private String FAULT_CLASS_B = "B";
  private String FAULTING_STYLE_SS = "SS";
  private String FAULTING_STYLE_R = "R";
  private String FAULTING_STYLE_N = "N";


  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format.");

  /**
   * Static variable for input file name
   */
  private final static String INPUT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/Frankel96/Frankel96_CAL_all.txt";

  /**
   * Vectors for holding the various forecast types
   */
  private Vector FrankelA_CharEqkSources = new Vector();
  private Vector FrankelB_CharEqkSources = new Vector();
  private Vector FrankelB_GR_EqkSources = new Vector();

  /**
   * timespan field in yrs for now (but have to ultimately make it a TimeSpan class variable)
   */
  private double timeSpan;
  private TimeSpan time;

  /**
   * This constructor reads the input file and creates all the soures
   *
   * No argument constructor
   */
  public Frankel96_EqkRupForecast() {
    readFrankel96_File();
  }

  /**
   * Read the file and make the sources
   *
   * @throws FaultException
   */
  private  void readFrankel96_File() throws FaultException{

    // Debug
    String S = C + ": readFrankel96: ";
    if( D ) System.out.println(S + "Starting");
    GriddedFaultFactory factory;
    GutenbergRichterMagFreqDist gutenbergRichter;
    ArrayList inputFileLines = null;
    String  faultClass="", faultingStyle, faultName="", temp;
    int i;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    int rake=0;
    double mag=0;  // used for magChar and magUpper (latter for the GR distributions)
    double charRate=0, dip=0, downDipWidth=0, depthToTop=0;
    double bValue=0.9, magLower=6.5, deltaMag=0.1;

    // Load in from file the data
    if( D ) System.out.println(S + "Loading file = " + INPUT_FILE_NAME );
    try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(S + e.toString()); }
    catch( IOException e){ System.out.println(S + e.toString());}

    // Exit if no data found in list
    if( inputFileLines == null) throw new
           FaultException(S + "No data loaded from file. File may be empty or doesn't exist.");

    // Loop over lines of input file and create each source in the process
    ListIterator it = inputFileLines.listIterator();

    // loope over all lines of the input file
    while( it.hasNext() ){
          StringTokenizer st = new StringTokenizer(it.next().toString());

          // WHILE LOOP REALLY NEEDED HERE?
          while(st.hasMoreTokens()){

            //first word of first line is the fault class (A or B)
            faultClass = new String(st.nextToken());

            // 2nd word is the faulting style; set rake accordingly
            faultingStyle = new String(st.nextToken());

            //for Strike slip fault
            if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_SS))
              rake =0;

            //for reverse fault
            if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_R))
              rake =90;

            //for normal fault
            if(faultingStyle.equalsIgnoreCase(FAULTING_STYLE_N))
              rake =-90;

            //reading the fault name
            faultName = new String(st.nextToken());

            if(D) System.out.println(C+":FaultName::"+faultName);
          }

          // get the 2nd line from the file
          st = new StringTokenizer(it.next().toString());

          // 1st word is magnitude
          mag=Double.parseDouble(st.nextToken());

          // 2nd word is charRate
          charRate=Double.parseDouble(st.nextToken());


          // get the third line from the file
          st=new StringTokenizer(it.next().toString());

          // 1st word is dip
          dip=Double.parseDouble(st.nextToken());
          // 2nd word is down dip width
          downDipWidth=Double.parseDouble(st.nextToken());
          // 3rd word is the depth to top of fault
          depthToTop=Double.parseDouble(st.nextToken());

          // Calculate upper and lower seismogenic depths
          upperSeismoDepth = depthToTop;
          lowerSeismoDepth = depthToTop + downDipWidth*Math.sin((Math.toRadians(Math.abs(dip))));

          // get the 4th line from the file that gives the number of points on the fault trace
          int numOfDataLines = Integer.parseInt(it.next().toString().trim());

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

          // Make the fault surface
          factory = new FrankelGriddedFaultFactory(faultTrace,
                                                   dip,
                                                   upperSeismoDepth,
                                                   lowerSeismoDepth,
                                                   GRID_SPACING);

          GriddedSurfaceAPI surface = factory.getGriddedSurface();

          // Now make the source(s)
          if(faultClass == FAULT_CLASS_B && mag>6.5){
            // divide the rate in half for the GR and Char parts, respectively
            double rate = 0.5*charRate;
            double moRate = rate*MomentMagCalc.getMoment(mag);
            // make the GR source
            Frankel96_GR_EqkSource frankel96_GR_src = new Frankel96_GR_EqkSource(rake,bValue,magLower,
                                                   mag,moRate,deltaMag,(EvenlyGriddedSurface)surface);
            FrankelB_GR_EqkSources.add(frankel96_GR_src);
            // now make the Char source
            Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,rate,
                                                      (EvenlyGriddedSurface)surface);
            FrankelB_CharEqkSources.add(frankel96_Char_src);
          }
          else if (faultClass == FAULT_CLASS_B) {    // if class B and mag<=6.5, it's all characteristic
            Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface);
            FrankelB_CharEqkSources.add(frankel96_Char_src);

          }
          else {   // it must be a class A fault
            Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface);
            FrankelA_CharEqkSources.add(frankel96_Char_src);
          }
    }  // bottom of loop over linse

    // Done
    if( D ) System.out.println(S + "readFrankel96_file() is done");
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
     * faults that corresponds to that parameter source.
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
     return C;
   }

   /**
    * this function is needed to prepare for the forecast; nothing needs to be done here because
    * the constructor does it all
    **/

   public void updateForecast() {
     if(D) System.out.println(C+" updateForecast() is done");

   }

}
