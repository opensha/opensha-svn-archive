package org.scec.sha.earthquake.rupForecastImpl.WG02;

import java.util.ArrayList;
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
import org.scec.sha.earthquake.rupForecastImpl.GriddedRegionPoissonEqkSource;
import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.*;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.magdist.GutenbergRichterMagFreqDist;
import org.scec.exceptions.FaultException;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.TimeSpan;
import org.scec.param.event.ParameterChangeListener;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.data.LocationList;
import org.scec.data.region.EvenlyGriddedGeographicRegion;


/**
 * <p>Title: WG02_EqkRupForecast</p>
 * <p>Description: Working Group 2002 Earthquake Rupture Forecast.
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
  private final static String  C = new String("WG02 Eqk Rup Forecast");
  public final static String NAME =C;
  private boolean D = false;

  /**
   * Vectors for holding the various sources, separated by type
   */
  private ArrayList allSources;

 // This is an array holding the relevant lines of the input file
  private List inputFileStrings = null;

  double rupOffset, gridSpacing, deltaMag;
  String backSeisValue;
  String grTailValue;
  String name;

  /**
   * This constructs a single forecast for a single-iteration run of the WG02 fortran code,
   * where the modal values at each branch tip were given unit weight and all other branches
   * were given a weight of zero.  This no-argument constuctor only supports a duration of
   * 30 years (this can easily be relaxed later is there is demand).
   */
  public WG02_EqkRupForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    String INPUT_FILE_NAME = "org/scec/sha/earthquake/rupForecastImpl/WG02/singleIterationWithModes.OpenSHA.30yr.txt";

    ArrayList inputFileLines=null;

    // read the lines of the input files into a list
    try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    inputFileStrings = inputFileLines.subList(2,inputFileLines.size());
    if (D) System.out.println(C+" firstLineOfStrings ="+inputFileStrings.get(0));
    if (D) System.out.println(C+" LastLineOfStrings ="+inputFileStrings.get(inputFileStrings.size()-1));

    // get the line with the timeSpan info on it
    ListIterator it = inputFileLines.listIterator();
    StringTokenizer st;
    st = new StringTokenizer((String) inputFileLines.get(1));

    st.nextToken();
    st.nextToken();
    st.nextToken();
    st.nextToken();
    int year = new Double(st.nextToken()).intValue();
    double duration = new Double(st.nextToken()).doubleValue();
    int numIterations = new Double(st.nextToken()).intValue();

    inputFileLines =null ;

    if (D) System.out.println("year="+year+"; duration="+duration+"; numIterations="+numIterations);
    timeSpan.setDuractionConstraint(duration,duration);
    timeSpan.setDuration(duration);
    timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR,year,year);
    timeSpan.setStartTime(year);

    // hard code the adjustable parameter values
    rupOffset = 2;
    gridSpacing = 1;
    deltaMag = 0.1;
    backSeisValue = WG02_ERF_Epistemic_List.SEIS_INCLUDE;
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
    allSources = new ArrayList();

    FaultTrace faultTrace;
    GriddedFaultFactory faultFactory;
    EvenlyGriddedSurface faultSurface;

    WG02_CharEqkSource wg02_source;
    GriddedRegionPoissonEqkSource backSource = null;

    double   lowerSeismoDepth, upperSeismoDepth;
    double lat, lon;
    double dip=0, downDipWidth=0, rupArea;
    double prob, meanMag, magSigma, nSigmaTrunc, rake;
    String fault, rup, sourceName;
    int numPts, i, lineIndex;

    double back_N, back_b, back_M1, back_M2;
    int back_num;

    double tail_N, tail_b, tail_M1, tail_M2;
    int tail_num;


    // Create iterator over inputFileStrings
    ListIterator it = inputFileStrings.listIterator();
    StringTokenizer st;

    // 1st line has the iteration number
    st = new StringTokenizer(it.next().toString());
    String interation = st.nextToken().toString();

    // 2nd line is background seismicity stuff
    // the vals are N(M³M1), b_val, M1, M2 -- Extrapolate this down to M = 5.0! (M1 > 5.0)
    st = new StringTokenizer(it.next().toString());

    // make the background source if it's desired
    if(backSeisValue.equals(WG02_ERF_Epistemic_List.SEIS_INCLUDE)) {
      back_N = new Double(st.nextToken()).doubleValue();
      back_b = new Double(st.nextToken()).doubleValue();
      back_M1 = new Double(st.nextToken()).doubleValue();
      back_M1 = ((double)Math.round(back_M1*100))/100.0; // round it to nice value
      back_M2 = new Double(st.nextToken()).doubleValue();
      back_num = (int)((back_M2-5.0)/0.05);
      GutenbergRichterMagFreqDist back_GR_dist = new GutenbergRichterMagFreqDist(5.0, back_num, 0.05, 1.0, back_b);
      back_GR_dist.scaleToCumRate(back_M1,back_N);

      LocationList locList = new LocationList();
      locList.addLocation(new Location(37.19, -120.61, 0.0));
      locList.addLocation(new Location(36.43, -122.09, 0.0));
      locList.addLocation(new Location(38.23, -123.61, 0.0));
      locList.addLocation(new Location(39.02, -122.08, 0.0));
      EvenlyGriddedGeographicRegion gridReg = new EvenlyGriddedGeographicRegion(locList,0.1);

      backSource = new GriddedRegionPoissonEqkSource(gridReg, back_GR_dist, timeSpan.getDuration(),
                                                     0.0, 90.0); // aveRake=0; aveDip=90


//      if(D) {
        System.out.println("back_N="+back_N+"\nback_b="+back_b+"\nback_M1="+back_M1+"\nback_M2="+back_M2+"\nback_num="+back_num);
        System.out.println("GR_rate(M1)="+back_GR_dist.getCumRate(back_M1));
        System.out.println("num_back_grid_points="+gridReg.getNumGridLocs());
//      }

      // add this source later so it's at the end of the list
    }

    // Now loop over the ruptures within this iteration
    while(it.hasNext()) {

      faultTrace = new FaultTrace("noName");

      // line with fault/rupture index
      st = new StringTokenizer(it.next().toString());
      fault = st.nextToken().toString();
      rup = st.nextToken().toString();

      // line with source name
      st = new StringTokenizer(it.next().toString());
      sourceName = st.nextToken().toString();

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
      // vals are M1, M2, N(M³M1), b_val

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
    if(backSeisValue.equals(WG02_ERF_Epistemic_List.SEIS_INCLUDE))
      allSources.add(backSource);
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
      * Get the list of all earthquake sources.
      *
      * @return ArrayList of Prob Earthquake sources
      */
     public ArrayList  getSourceList(){
       return null;
     }


    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
   public String getName(){
     return NAME;
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
     System.out.println("num_sources="+qkCast.getNumSources());
     System.out.println("num_rups(lastSrc)="+qkCast.getNumRuptures(qkCast.getNumSources()-1));

     // write out source names
//     for(int i=0;i<qkCast.getNumSources();i++)
//       System.out.println(i+"th source name = "+qkCast.getSource(i).getName());
  }

}
