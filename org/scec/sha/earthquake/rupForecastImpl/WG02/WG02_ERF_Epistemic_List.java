package org.scec.sha.earthquake.rupForecastImpl.WG02;

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
 * <p>Title: WG02_ERF_Epistemic_List</p>
 * <p>Description: Working Group 2002 Earthquake Rupture Forecast. This class
 * reads a single file and constructs the forecast.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Edward Field
 * @Date : April, 2003
 * @version 1.0
 */

public class WG02_ERF_Epistemic_List extends EqkRupForecast
    implements ParameterChangeListener{

  //for Debug purposes
  private static String  C = new String("WG02_EqkRupForecast");
  private boolean D = true;

  /**
   * used for error checking
   */
  protected final static FaultException ERR = new FaultException(
           C + ": loadFaultTraces(): Missing metadata from trace, file bad format.");

  /**
   * Static variable for input file name
   */
  private final static String INPUT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/WG02/WG02_WRAPPER_INPUT.DAT";

  /**
   * Vectors for holding the various sources, separated by type
   */
  private Vector charEqkSources;
  private Vector tail_GR_EqkSources;

  // number of interations in the input file
  private int numIterations;


  // This is an array holding each line of the input file
  private ArrayList inputFileLines = null;

  // Stuff for background & GR tail seismicity params
  public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
  public final static String GR_TAIL_NAME = new String ("GR Tail Seismicity");
  public final static String SEIS_INCLUDE = new String ("Include");
  public final static String SEIS_EXCLUDE = new String ("Exclude");
  Vector backSeisOptionsStrings = new Vector();
  Vector grTailOptionsStrings = new Vector();
  StringParameter backSeisParam;
  StringParameter grTailParam;

  // For rupture offset lenth along fault parameter
  private final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
  private Double DEFAULT_RUP_OFFSET_VAL= new Double(5);
  private final static String RUP_OFFSET_PARAM_UNITS = "km";
  private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
  private final static double RUP_OFFSET_PARAM_MIN = 1;
  private final static double RUP_OFFSET_PARAM_MAX = 50;
  DoubleParameter rupOffset_Param;

  /**
   *
   * No argument constructor
   */
  public WG02_ERF_Epistemic_List() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // create and add adj params to list
    intiAdjParams();


    // add the change listener to parameters so that forecast can be updated
    // whenever any paramater changes
    rupOffset_Param.addParameterChangeListener(this);
    backSeisParam.addParameterChangeListener(this);
    grTailParam.addParameterChangeListener(this);

    // read the lines of the input files into a list
    try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    // Exit if no data found in list
    if( inputFileLines == null) throw new
           FaultException(C + "No data loaded from "+INPUT_FILE_NAME+". File may be empty or doesn't exist.");

    // set the timespan from the 2nd line of the file
    ListIterator it = inputFileLines.listIterator();
    StringTokenizer st;
    st = new StringTokenizer(it.next().toString()); // skip first line
    st = new StringTokenizer(it.next().toString());

    st.nextToken();
    st.nextToken();
    st.nextToken();
    st.nextToken();
    int year = new Double(st.nextToken()).intValue();
    double duration = new Double(st.nextToken()).doubleValue();
    numIterations = new Double(st.nextToken()).intValue();
    if (D) System.out.println("year="+year+"; duration="+duration+"; numIterations="+numIterations);
    timeSpan.setDuractionConstraint(duration,duration);
    timeSpan.setDuration(duration);
    timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR,year,year);
    timeSpan.setStartTime(year);
  }


  // make the adjustable parameters & the list
  private void intiAdjParams() {


    backSeisOptionsStrings.add(SEIS_EXCLUDE);
    //  backSeisOptionsStrings.add(SEIS_INCLUDE);
    backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,SEIS_EXCLUDE);

    grTailOptionsStrings.add(SEIS_EXCLUDE);
    //  grTailOptionsStrings.add(SEIS_INCLUDE);
    grTailParam = new StringParameter(GR_TAIL_NAME, backSeisOptionsStrings,SEIS_EXCLUDE);

    rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
        RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
    rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);

    // add adjustable parameters to the list
    adjustableParams.addParameter(rupOffset_Param);
    adjustableParams.addParameter(backSeisParam);
    adjustableParams.addParameter(grTailParam);

  }


  /**
   * Make the sources
   *
   * @throws FaultException
   */
  private  void makeSources() throws FaultException{

    charEqkSources = new Vector();
    tail_GR_EqkSources = new Vector();

    // Debug
    String S = C + ": makeSoureces(): ";

    FaultTrace faultTrace;
    GriddedFaultFactory faultFactory;
    EvenlyGriddedSurface faultSurface;

    WG02_CharEqkSource wg02_source;

    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    double dip=0, downDipWidth=0, rupArea;
    double prob, meanMag, magSigma, nSigmaTrunc, rake=0;
    String ruptureName;
    int iFault, iRup, numPts, i;

    // get adjustable parameters values
    double rupOffset = ((Double) rupOffset_Param.getValue()).doubleValue();

    // Loop over lines of input file and create each source in the process
    ListIterator it = inputFileLines.listIterator();
    StringTokenizer st;

    // skip first two lines of the file
    st = new StringTokenizer(it.next().toString());
    st = new StringTokenizer(it.next().toString());


    // here's the start of an iteration ************8

    // first line is header
    st = new StringTokenizer(it.next().toString());

    // 2nd line is background seismicity stuff
    st = new StringTokenizer(it.next().toString());

    // Now loop over ruptures within this iteration
    faultTrace = new FaultTrace("noName");
    // line with fault/rupture index
    st = new StringTokenizer(it.next().toString());
    iFault = new Integer(st.nextToken()).intValue();
    iRup = new Integer(st.nextToken()).intValue();

    // line with number of fault-trace points
    st = new StringTokenizer(it.next().toString());
    numPts = new Integer(st.nextToken()).intValue();

    // make the fault trace from the next numPts lines
    for(i=0;i<numPts;i++) {
      st = new StringTokenizer(it.next().toString());
      lon = new Double(st.nextToken()).doubleValue();
      lat = new Double(st.nextToken()).doubleValue();
      faultTrace.addLocation(new Location(lat,lon));
    }

    // line with dip, seisUpper, ddw, and rupArea
    st = new StringTokenizer(it.next().toString());
    dip = new Double(st.nextToken()).doubleValue();
    upperSeismoDepth = new Double(st.nextToken()).doubleValue();
    downDipWidth = new Double(st.nextToken()).doubleValue();
    lowerSeismoDepth = downDipWidth*Math.sin(dip*Math.PI/180);
    rupArea = new Double(st.nextToken()).doubleValue();

    // line with the GR tail stuff
    st = new StringTokenizer(it.next().toString());
    // skipping for now

    // line with prob, meanMag, magSigma, nSigmaTrunc
    st = new StringTokenizer(it.next().toString());
    prob = new Double(st.nextToken()).doubleValue();
    meanMag = new Double(st.nextToken()).doubleValue();
    magSigma = new Double(st.nextToken()).doubleValue();
    nSigmaTrunc = new Double(st.nextToken()).doubleValue();

    // this should be an adjustable parameter
    double gridSpacing = 1.0;

    faultFactory = new StirlingGriddedFaultFactory(faultTrace,dip,upperSeismoDepth,lowerSeismoDepth,gridSpacing);
    faultSurface = (EvenlyGriddedSurface) faultFactory.getGriddedSurface();

    wg02_source = new WG02_CharEqkSource(prob,meanMag,magSigma,nSigmaTrunc,faultSurface,rupArea,rupOffset,"noName",rake);
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
    public ProbEqkRupture getRuptureClone(int iSource, int nRupture) {
      return getSource(iSource).getRuptureClone(nRupture);
    }

    /**
     * Get the nth rupture of the ith source.
     *
     * @param source
     * @param i
     * @return
     */
    public ProbEqkRupture getRupture(int iSource, int nRupture) {
       return getSource(iSource).getRupture(nRupture);
    }

    /**
     * Returns the  ith earthquake source
     *
     * @param iSource : index of the source needed
    */
    public ProbEqkSource getSource(int iSource) {

      return null;
    }

    /**
     * Get the number of earthquake sources
     *
     * @return integer
     */
    public int getNumSources(){
      return 0;
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
    * update the forecast
    **/

   public void updateForecast() {

     // make sure something has changed
     if(parameterChangeFlag) {

       // get value of background seismicity paramter
       String backSeis = (String) backSeisParam.getValue();

       makeSources();


       parameterChangeFlag = false;

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
     WG02_EqkRupForecast qkCast = new WG02_EqkRupForecast();
     qkCast.updateForecast();


  }

}
