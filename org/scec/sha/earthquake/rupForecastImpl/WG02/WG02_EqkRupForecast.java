package org.scec.sha.earthquake.rupForecastImpl.WG02;

import java.util.Vector;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.List;
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
 * <p>Title: WG02_EqkRupForecast</p>
 * <p>Description: Working Group 2002 Earthquake Rupture Forecast. This class
 * reads a single file and constructs the forecast.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Edward Field
 * @Date : April, 2003
 * @version 1.0
 */

public class WG02_EqkRupForecast extends EqkRupForecast
    implements ParameterChangeListener{

  //for Debug purposes
  private static String  C = new String("WG02 Eqk Rup Forecast");
  private boolean D = false;

  /**
   * Vectors for holding the various sources, separated by type
   */
  private Vector allSources;

 // This is an array holding the relevant lines of the input file
  private List inputFileStrings = null;

  double rupOffset, gridSpacing, deltaMag;
  String backSeisValue;
  String grTailValue;
  String name;

  /**
   * This constructs a single forecast using the first realization
   */
  public WG02_EqkRupForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    String INPUT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/WG02/WG02_WRAPPER_INPUT.DAT";

    ArrayList inputFileLines=null;

    // read the lines of the input files into a list
    try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    ListIterator it = inputFileLines.listIterator();
    StringTokenizer st;

    // Find the end of the first iteration
    int endIndex = 3;
    st = new StringTokenizer((String) inputFileLines.get(endIndex));
    st.nextToken();
    String test = st.nextToken();
    while(!test.equals("ITERATIONS")) {
      endIndex+=1;
      st = new StringTokenizer((String) inputFileLines.get(endIndex));
      st.nextToken();
      if (st.hasMoreTokens())
        test = st.nextToken();
    }

    if (D) System.out.println(C+" endIndex="+endIndex);
    if (D) System.out.println(C+" line(endIndex) ="+inputFileLines.get(endIndex));

    inputFileStrings = inputFileLines.subList(2,endIndex);

    if (D) System.out.println(C+" firstLineOfStrings ="+inputFileStrings.get(0));
    if (D) System.out.println(C+" LastLineOfStrings ="+inputFileStrings.get(inputFileStrings.size()-1));

    // get the line with the timeSpan info on it
    st = new StringTokenizer((String) inputFileLines.get(1));

    st.nextToken();
    st.nextToken();
    st.nextToken();
    st.nextToken();
    int year = new Double(st.nextToken()).intValue();
    double duration = new Double(st.nextToken()).doubleValue();
    int numIterations = new Double(st.nextToken()).intValue();
    if (D) System.out.println("year="+year+"; duration="+duration+"; numIterations="+numIterations);
    timeSpan.setDuractionConstraint(duration,duration);
    timeSpan.setDuration(duration);
    timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR,year,year);
    timeSpan.setStartTime(year);

    // hard code the adjustable parameter values
    rupOffset = 2;
    gridSpacing = 1;
    deltaMag = 0.1;
    backSeisValue = WG02_ERF_Epistemic_List.SEIS_EXCLUDE;
    grTailValue = WG02_ERF_Epistemic_List.SEIS_EXCLUDE;
    name = "noName";

    // now make the sources
    makeSources();
  }



  public WG02_EqkRupForecast(List inputFileStrings, double rupOffset, double gridSpacing,
                             double deltaMag, String backSeisValue, String grTailValue, String name,
                             TimeSpan timespan) {

    this.inputFileStrings = inputFileStrings;
    this.rupOffset=rupOffset;
    this.gridSpacing=gridSpacing;
    this.deltaMag = deltaMag;
    this.backSeisValue=backSeisValue;
    this.grTailValue=grTailValue;
    this.name = name;
    this.timeSpan = timeSpan;

    // now make the sources
    makeSources();

  }

  /**
   * Make the sources
   *
   * @throws FaultException
   */
  private  void makeSources() throws FaultException{

    if(D) System.out.println(C+": last line of inputFileStrings = "+inputFileStrings.get(inputFileStrings.size()-1));
    allSources = new Vector();

    FaultTrace faultTrace;
    GriddedFaultFactory faultFactory;
    EvenlyGriddedSurface faultSurface;

    WG02_CharEqkSource wg02_source;

    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    double dip=0, downDipWidth=0, rupArea;
    double prob, meanMag, magSigma, nSigmaTrunc, rake;
    String ruptureName, fault, rup, sourceName;
    int numPts, i, lineIndex;

    // Create iterator over inputFileStrings
    ListIterator it = inputFileStrings.listIterator();
    StringTokenizer st;

    // 1st line has the iteration number
    st = new StringTokenizer(it.next().toString());
    String interation = st.nextToken().toString();

    // 2nd line is background seismicity stuff (Ignored for now)
    st = new StringTokenizer(it.next().toString());

    // Now loop over ruptures within this iteration

    while(it.hasNext()) {

      faultTrace = new FaultTrace("noName");

      // line with fault/rupture index
      st = new StringTokenizer(it.next().toString());
      fault = st.nextToken().toString();
      rup = st.nextToken().toString();
      sourceName = "fault "+fault+"; rupture "+rup;

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

      // reverse the order of point if it's the Mt Diable fault
      // so it will be dipping to the right
      if( fault.equals("7") )
        faultTrace.reverse();;



      // line with dip, seisUpper, ddw, and rupArea
      st = new StringTokenizer(it.next().toString());
      dip = new Double(st.nextToken()).doubleValue();
      upperSeismoDepth = new Double(st.nextToken()).doubleValue();
      downDipWidth = new Double(st.nextToken()).doubleValue();
      lowerSeismoDepth = upperSeismoDepth+downDipWidth*Math.sin(dip*Math.PI/180);
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

      faultFactory = new StirlingGriddedFaultFactory(faultTrace,dip,upperSeismoDepth,lowerSeismoDepth,gridSpacing);
      faultSurface = (EvenlyGriddedSurface) faultFactory.getGriddedSurface();


      // change the rupArea if it's one of the floating ruptures
      if( rup.equals("11")  || rup.equals("12") )
        rupArea = Math.pow(10.0, meanMag-4.2);

      // set the rake (only diff for Mt Diable thrust)
      if( fault.equals("7") )
        rake=90.0;
      else
        rake = 0.0;


      // create the source
      wg02_source = new WG02_CharEqkSource(prob,meanMag,magSigma,nSigmaTrunc, deltaMag,
          faultSurface,rupArea,rupOffset,sourceName,rake);

      // add the source
      allSources.add(wg02_source);
    }
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

     // does nothing for now
     if(parameterChangeFlag) {
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
  }

}
