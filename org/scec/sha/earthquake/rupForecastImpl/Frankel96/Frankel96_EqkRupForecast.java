package org.scec.sha.earthquake.rupForecastImpl.Frankel96;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.scec.param.*;
import org.scec.calc.MomentMagCalc;
import org.scec.util.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.*;
import org.scec.sha.fault.GriddedFaultFactory;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
import org.scec.exceptions.FaultException;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;


/**
 * <p>Title: Frankel96_EqkRupForecast</p>
 * <p>Description:Frankel 1996 Earthquake Rupture Forecast. This class
 * reads a single file for the fault sources, and another file for the
 * background seismicity, and then creates the USGS/CGS 1996 California ERF.
 * This does not yet include any C zones.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta, Vipin Gupta, and Edward Field
 * @Date : Aug 31, 2002
 * @version 1.0
 */

public class Frankel96_EqkRupForecast extends EqkRupForecast
    implements ParameterChangeListener{

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
   * Static variable for input file names
   */
  private final static String INPUT_FAULT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/Frankel96/Frankel96_CAL_all.txt";
  private final static String INPUT_BACK_SEIS_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/Frankel96/CAagrid.asc";

  /**
   * Vectors for holding the various sources, separated by type
   */
  private Vector FrankelA_CharEqkSources;
  private Vector FrankelB_CharEqkSources;
  private Vector FrankelB_GR_EqkSources;
  private Vector FrankelBackgrSeisSources;
  private Vector allSources;


  // This is an array holding each line of the input file
  private ArrayList inputFaultFileLines = null;
  private ArrayList inputBackSeisFileLines = null;

  /**
   * timespan field in yrs for now (but have to ultimately make it a TimeSpan class variable)
   */
  private double timeSpan;
  private TimeSpan time;


  // adjustable parameters stuff
    public final static String TIMESPAN_PARAM_NAME ="Time Span";
    private Double DEFAULT_TIMESPAN_VAL= new Double(50);
    public final static String TIMESPAN_PARAM_UNITS = "yrs";
    private final static double TIMESPAN_PARAM_MIN = 1e-10;
    private final static double TIMESPAN_PARAM_MAX = 1e10;
    DoubleParameter timeSpanParam;

    // fault-model parameter stuff
    public final static String FAULT_MODEL_NAME = new String ("Fault Model");
    public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
    public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");
    // make the fault-model parameter
    Vector faultModelNamesStrings = new Vector();
    StringParameter faultModelParam;

    // fault-model parameter stuff
    public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
    public final static String BACK_SEIS_INCLUDE = new String ("Include");
    public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
    public final static String BACK_SEIS_ONLY = new String ("Only Background");
    // make the fault-model parameter
    Vector backSeisOptionsStrings = new Vector();
    StringParameter backSeisParam;


    // For fraction of moment rate on GR parameter
    private final static String FRAC_GR_PARAM_NAME ="GR Fraction on B Faults";
    private Double DEFAULT_FRAC_GR_VAL= new Double(0.5);
    private final static String FRAC_GR_PARAM_UNITS = null;
    private final static String FRAC_GR_PARAM_INFO = "Fraction of moment-rate put into GR dist on class-B faults";
    private final static double FRAC_GR_PARAM_MIN = 0;
    private final static double FRAC_GR_PARAM_MAX = 1;
    DoubleParameter fracGR_Param;

    // For rupture offset lenth along fault parameter
    private final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
    private Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
    private final static String RUP_OFFSET_PARAM_UNITS = "km";
    private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
    private final static double RUP_OFFSET_PARAM_MIN = 1;
    private final static double RUP_OFFSET_PARAM_MAX = 100;
    DoubleParameter rupOffset_Param;

    // private declaration of the flag to check if any parameter has been changed from its original value.
    private boolean  parameterChangeFlag = true;



  /**
   *
   * No argument constructor
   */
  public Frankel96_EqkRupForecast() {

    // create and add adj params to list
    intiAdjParams();


    // add the change listener to parameters so that forecast can be updated
    // whenever any paramater changes
    timeSpanParam.addParameterChangeListener(this);
    faultModelParam.addParameterChangeListener(this);
    fracGR_Param.addParameterChangeListener(this);
    rupOffset_Param.addParameterChangeListener(this);
    backSeisParam.addParameterChangeListener(this);

    // read the lines of the input files into a list
    try{ inputFaultFileLines = FileUtils.loadFile( INPUT_FAULT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    try{ inputBackSeisFileLines = FileUtils.loadFile( INPUT_BACK_SEIS_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    // Exit if no data found in list
    if( inputFaultFileLines == null) throw new
           FaultException(C + "No data loaded from "+INPUT_FAULT_FILE_NAME+". File may be empty or doesn't exist.");

    // Exit if no data found in list
    if( inputBackSeisFileLines == null) throw new
           FaultException(C + "No data loaded from "+INPUT_BACK_SEIS_FILE_NAME+". File may be empty or doesn't exist.");

  }

// make the adjustable parameters & the list
private void intiAdjParams() {

  faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
  faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
  faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,
                                        (String)faultModelNamesStrings.get(0));

  backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
  backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
  backSeisOptionsStrings.add(BACK_SEIS_ONLY);
  backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_ONLY);

  timeSpanParam = new DoubleParameter(TIMESPAN_PARAM_NAME,TIMESPAN_PARAM_MIN,
                                      TIMESPAN_PARAM_MAX,TIMESPAN_PARAM_UNITS,DEFAULT_TIMESPAN_VAL);

  fracGR_Param = new DoubleParameter(FRAC_GR_PARAM_NAME,FRAC_GR_PARAM_MIN,
                                     FRAC_GR_PARAM_MAX,FRAC_GR_PARAM_UNITS,DEFAULT_FRAC_GR_VAL);
  fracGR_Param.setInfo(FRAC_GR_PARAM_INFO);

  rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
                                        RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
  rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);


// add adjustable parameters to the list
  adjustableParams.addParameter(timeSpanParam);
  adjustableParams.addParameter(faultModelParam);
  adjustableParams.addParameter(fracGR_Param);
  adjustableParams.addParameter(rupOffset_Param);
  adjustableParams.addParameter(backSeisParam);


}


  /**
   * Read the Fault file and make the sources
   *
   * @throws FaultException
   */
  private  void makeFaultSources() throws FaultException{

    FrankelA_CharEqkSources = new Vector();
    FrankelB_CharEqkSources = new Vector();
    FrankelB_GR_EqkSources = new Vector();

    // Debug
    String S = C + ": makeSoureces(): ";
    if( D ) System.out.println(S + "Starting");
    GriddedFaultFactory factory;
    String  faultClass="", faultingStyle, faultName="";
    int i;
    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon, rake=0;
    double mag=0;  // used for magChar and magUpper (latter for the GR distributions)
    double charRate=0, dip=0, downDipWidth=0, depthToTop=0;

    // get adjustable parameters values
    double fracGR = ((Double) fracGR_Param.getValue()).doubleValue();
    String faultModel = (String) faultModelParam.getValue();
    double rupOffset = ((Double) rupOffset_Param.getValue()).doubleValue();

    timeSpan = ((Double) timeSpanParam.getValue()).doubleValue();

    // Loop over lines of input file and create each source in the process
    ListIterator it = inputFaultFileLines.listIterator();
    while( it.hasNext() ){
          StringTokenizer st = new StringTokenizer(it.next().toString());

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
          }

         // reverse data ordering if dip negative, make positive and reverse trace order
          if( dip < 0 ) {
             faultTrace.reverse();
             dip *= -1;
          }

          if( D ) System.out.println(C+":faultTrace::"+faultTrace.toString());

          if(faultModel.equals(FAULT_MODEL_FRANKEL)) {
            factory = new FrankelGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
                                                   lowerSeismoDepth, GRID_SPACING);
          }
          else {
            factory = new StirlingGriddedFaultFactory( faultTrace, dip, upperSeismoDepth,
                                                   lowerSeismoDepth, GRID_SPACING);
          }
          GriddedSurfaceAPI surface = factory.getGriddedSurface();

          // Now make the source(s)
          if(faultClass.equalsIgnoreCase(FAULT_CLASS_B) && mag>6.5){
            // divide the rate according the faction assigned to GR dist
            double rate = (1.0-fracGR)*charRate;
            double moRate = fracGR*charRate*MomentMagCalc.getMoment(mag);

            // make the GR source
            if(moRate>0.0) {
              Frankel96_GR_EqkSource frankel96_GR_src = new Frankel96_GR_EqkSource(rake,B_VALUE,MAG_LOWER,
                                                   mag,moRate,DELTA_MAG,rupOffset,(EvenlyGriddedSurface)surface, faultName);
              frankel96_GR_src.setTimeSpan(timeSpan);
              FrankelB_GR_EqkSources.add(frankel96_GR_src);
            }
            // now make the Char source
            if(rate>0.0) {
              Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,rate,
                                                      (EvenlyGriddedSurface)surface, faultName);
              frankel96_Char_src.setTimeSpan(timeSpan);
              FrankelB_CharEqkSources.add(frankel96_Char_src);
            }
          }
          else if (faultClass.equalsIgnoreCase(FAULT_CLASS_B)) {    // if class B and mag<=6.5, it's all characteristic
            Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface, faultName);
            frankel96_Char_src.setTimeSpan(timeSpan);
            FrankelB_CharEqkSources.add(frankel96_Char_src);

          }
          else if (faultClass.equalsIgnoreCase(FAULT_CLASS_A)) {   // class A fault
            Frankel96_CharEqkSource frankel96_Char_src = new  Frankel96_CharEqkSource(rake,mag,charRate,
                                                      (EvenlyGriddedSurface)surface, faultName);
            frankel96_Char_src.setTimeSpan(timeSpan);
            FrankelA_CharEqkSources.add(frankel96_Char_src);
          }
          else {
            throw new FaultException(C+" Error - Bad fault Class :"+faultClass);
          }

    }  // bottom of loop over input-file lines

  }









  /**
  * Read the Background Seismicity file and make the sources
  *
  */
  private  void makeBackSeisSources() {

    // Debug
    String S = C + ": makeBackSeisSources(): ";
    if( D ) System.out.println(S + "Starting");

    FrankelBackgrSeisSources = new Vector();

    double lat, lon, rate, rateAtMag5;

    double aveRake=0.0;
    double aveDip=90;
    double tempMoRate = 1.0;
    double bValue = B_VALUE;
    double magUpper=7.0;
    double magDelta=0.2;
    double magLower1=0.0;
    int    numMag1=36;
    double magLower2=5.0;
    int    numMag2=11;

    // GR dist between mag 0 and 7, delta=0.2
    GutenbergRichterMagFreqDist grDist1 = new GutenbergRichterMagFreqDist(magLower1,numMag1,magDelta,
                                                                          tempMoRate,bValue);

    // GR dist between mag 5 and 7, delta=0.2
    GutenbergRichterMagFreqDist grDist2;

    PointGR_EqkSource grSource;

    // set timespan
    timeSpan = ((Double) timeSpanParam.getValue()).doubleValue();

    // Get iterator over input-file lines
    ListIterator it = inputBackSeisFileLines.listIterator();

    // skip first five header lines
    StringTokenizer st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());

    while( it.hasNext() ){

      // get next line
      st = new StringTokenizer(it.next().toString());

      lon =  Double.parseDouble(st.nextToken());
      lat =  Double.parseDouble(st.nextToken());
      rate = Double.parseDouble(st.nextToken());

      if (rate > 0.0) {  // ignore locations with a zero rate

        // scale all so the incremental rate at mag=0 index equals rate
        grDist1.scaleToIncrRate((int) 0,rate);

        // now get the rate at the mag=5 index
        rateAtMag5 = grDist1.getIncrRate((int) 25);

        // now scale all in the dist we want by rateAtMag5 (index 0 here)
        grDist2 = new GutenbergRichterMagFreqDist(magLower2,numMag2,magDelta,tempMoRate,bValue);
        grDist2.scaleToIncrRate((int) (0),rateAtMag5);

        // now make the source
        grSource = new PointGR_EqkSource(new Location(lat,lon),grDist2,aveRake,aveDip);

        // set the timespan
        grSource.setTimeSpan(timeSpan);

        // add the source
        FrankelBackgrSeisSources.add(grSource);
      }
    }
  }


  /**
   * sets the timeSpan field
   * @param yrs : does not do anything yet (timespan is an adjustable
   * DoubleParameter for now)
   */
  public void setTimeSpan(double yrs){

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
   * Gets the number of ruptures for the source at index iSource
   * @param iSource
   */
    public int getNumRuptures(int iSource){
      return getSource(iSource).getNumRuptures();
    }

    /**
     * Returns a clone of (rather than a reference to) the nth rupture of the
     * ith source.
     *
     * @param source
     * @param i
     * @return
     */
    public EqkRupture getRuptureClone(int iSource, int nRupture) {
      return getSource(iSource).getRuptureClone(nRupture);
    }

    /**
     * Get the nth rupture of the ith source.
     *
     * @param source
     * @param i
     * @return
     */
    public EqkRupture getRupture(int iSource, int nRupture) {
       return getSource(iSource).getRupture(nRupture);
    }

    /**
     * Returns the  ith earthquake source
     *
     * @param iSource : index of the source needed
    */
    public ProbEqkSource getSource(int iSource) {

      return (ProbEqkSource) allSources.get(iSource);
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer
     */
    public int getNumSources(){
      return allSources.size();
    }

    /**
     * Return a clone of (rather than a reference to) the ith earthquake source
     *
     * @param iSource : index of the source needed
     *
     * @return Returns the ProbEqkSource at index i
     *
     * FIX:FIX :: This function has not been implemented yet. Have to give it thought
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
      * Get the list of all earthquake sources.
      *
      * @return Vector of Prob Earthquake sources
      */
     public Vector  getSourceList(){

       return allSources;
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
    * update the forecast
    **/

   public void updateForecast() {

     // make sure something has changed
     if(parameterChangeFlag) {

       // get value of background seismicity paramter
       String backSeis = (String) backSeisParam.getValue();

       allSources = new Vector();

       if (backSeis.equalsIgnoreCase(BACK_SEIS_INCLUDE)) {
         makeFaultSources();
         makeBackSeisSources();
         // now create the allSources list:
         allSources.addAll(FrankelA_CharEqkSources);
         allSources.addAll(FrankelB_CharEqkSources);
         allSources.addAll(FrankelB_GR_EqkSources);
         allSources.addAll(FrankelBackgrSeisSources);
       }
       else if (backSeis.equalsIgnoreCase(BACK_SEIS_EXCLUDE)) {
         makeFaultSources();
         // now create the allSources list:
         allSources.addAll(FrankelA_CharEqkSources);
         allSources.addAll(FrankelB_CharEqkSources);
         allSources.addAll(FrankelB_GR_EqkSources);
       }
       else {// only background sources
        makeBackSeisSources();
        // now create the allSources list:
        allSources.addAll(FrankelBackgrSeisSources);
       }

       parameterChangeFlag = false;

/* TEMP STUFF
       double totRate=0, totProb=1, prob;
       int i,j, totRup;
       int totSrc= allSources.size();
       for(i=totSrc; i<totSrc; i++){
         ProbEqkSource src = (ProbEqkSource) allSources.get(i);
         totRup=src.getNumRuptures();
         for(j=0; j<totRup; j++) {
           prob = src.getRupture(j).getProbability();
           totProb *= (1-prob);
           totRate += -1*Math.log(1-prob)/50;
         }
       }
       System.out.println("updateForecast: totRate="+totRate+"; totProb="+totProb);
// END TEMP STUFF
*/
     }

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
   public static void main(String[] args) {

     Frankel96_EqkRupForecast frankCast = new Frankel96_EqkRupForecast();
     frankCast.updateForecast();
     System.out.println("num sources="+frankCast.getNumSources());
//     for(int i=0; i<frankCast.getNumSources(); i++)
//       System.out.println(frankCast.getSource(i).getName());
     double totRate=0, totProb=1, prob;
     int i,j, totRup;
     int totSrc= frankCast.getNumSources();
     for(i=0; i<totSrc; i++){
       ProbEqkSource src = (ProbEqkSource) frankCast.getSource(i);
       totRup=src.getNumRuptures();
       if(i==0) System.out.println("numRup for src0 ="+totRup);
       for(j=0; j<totRup; j++) {
         prob = src.getRupture(j).getProbability();
         totProb *= (1-prob);
         totRate += -1*Math.log(1-prob)/50;
         if(j==0 && i==0)
           System.out.println("mag, prob for src0, rup 0="+src.getRupture(j).getMag()+"; "+prob);
         if(j==0 && i==1)
           System.out.println("mag, prob for src1, rup 0="+src.getRupture(j).getMag()+"; "+prob);
         if(j==0 && i==2)
           System.out.println("mag, prob for src2, rup 0="+src.getRupture(j).getMag()+"; "+prob);

       }
     }
       System.out.println("main(): totRate="+totRate+"; totProb="+totProb);
  }

}
