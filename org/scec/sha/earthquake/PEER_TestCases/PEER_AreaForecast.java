package org.scec.sha.earthquake.PEER_TestCases;


import java.util.Vector;
import java.util.Iterator;


import org.scec.data.TimeSpan;
import org.scec.data.Location;
import org.scec.data.LocationList;
import org.scec.data.Direction;
import org.scec.calc.RelativeLocation;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_CharEqkSource;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;


/**
 * <p>Title: PEER_AreaForecast</p>
 * <p>Description: Area Equake rupture forecast. The Peer Group Test cases </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * Date : Oct 24, 2002
 * @version 1.0
 */

public class PEER_AreaForecast extends EqkRupForecast
    implements ParameterChangeListener{

  //for Debug purposes
  private static String  C = new String("PEER Area");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  /**
   * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
   */
  private double timeSpan;
  private TimeSpan time;

  // this is the GR distribution used for all sources
  private GutenbergRichterMagFreqDist dist_GR;

  // this is the source
  private PointGR_EqkSource pointGR_EqkSource;


  /**
   * Declaration for the static lat and lons for the Area
   */
  private static final double LAT_TOP= 38.901;
  private static final double LAT_BOTTOM = 37.099;
  private static final double LAT_CENTER = 38.0;
  private static final double LONG_LEFT= -123.138;
  private static final double LONG_RIGHT= -120.862;
  private static final double LONG_CENTER= -122.0;

  private static final double MAX_DISTANCE =100;

  //Param Name
  public final static String GRID_PARAM_NAME =  "Area Grid Spacing";
  public final static String GRID_PARAM_UNITS =  "km";
  private final static double GRID_PARAM_MIN = 0.001;
  private final static double GRID_PARAM_MAX = 100;
  public final static String DEPTH_LOWER_PARAM_NAME =  "Lower Seis Depth";
  public final static String DEPTH_UPPER_PARAM_NAME =  "Upper Seis Depth";
  public final static String DEPTH_PARAM_UNITS = "km";
  private final static double DEPTH_PARAM_MIN = 0;
  private final static double DEPTH_PARAM_MAX = 30;
  private final static Double DEPTH_PARAM_DEFAULT = new Double(5);
  public final static String MAG_DIST_PARAM_NAME = "Mag Dist";

  //timespan Variable
  public final static String TIMESPAN_PARAM_NAME = "Area Timespan";
  public final static String TIMESPAN_PARAM_UNITS = "yrs";
  private final static Double TIMESPAN_PARAM_DEFAULT = new Double(1);
  private final static double TIMESPAN_PARAM_MIN = 1e-10;
  private final static double TIMESPAN_PARAM_MAX = 1e10;

   //Rake Variable
  public final static String RAKE_PARAM_NAME = "Ave Rake";
  public final static String RAKE_PARAM_UNITS = "degrees";
  private final static Double RAKE_PARAM_DEFAULT = new Double(0);
  private final static double RAKE_PARAM_MIN = -180;
  private final static double RAKE_PARAM_MAX = 180;

  //Rake Variable
  public final static String DIP_PARAM_NAME = "Ave Dip";
  public final static String DIP_PARAM_UNITS = "degrees";
  private final static Double DIP_PARAM_DEFAULT = new Double(90);
  private final static double DIP_PARAM_MIN = 0;
  private final static double DIP_PARAM_MAX = 90;


  // default grid spacing is 1km
  private Double DEFAULT_GRID_VAL = new Double(1);

  // list of forecast locations
  private LocationList locationList;


  // create the grid spacing parameter
  DoubleParameter gridParam=new DoubleParameter(GRID_PARAM_NAME,GRID_PARAM_MIN,
                                                GRID_PARAM_MAX,GRID_PARAM_UNITS,
                                                DEFAULT_GRID_VAL);

  // create Depth Lower parameter
  DoubleParameter depthLowerParam = new DoubleParameter(DEPTH_LOWER_PARAM_NAME,DEPTH_PARAM_MIN,
                                                        DEPTH_PARAM_MAX,DEPTH_PARAM_UNITS,
                                                        DEPTH_PARAM_DEFAULT);

  // create depth Upper parameter
  DoubleParameter depthUpperParam = new DoubleParameter(DEPTH_UPPER_PARAM_NAME,DEPTH_PARAM_MIN,
                                                        DEPTH_PARAM_MAX,DEPTH_PARAM_UNITS,
                                                        DEPTH_PARAM_DEFAULT);
  // create the timespan parameter
  DoubleParameter timespanParam = new DoubleParameter(TIMESPAN_PARAM_NAME, TIMESPAN_PARAM_MIN,
                                                      TIMESPAN_PARAM_MAX,TIMESPAN_PARAM_UNITS,
                                                      TIMESPAN_PARAM_DEFAULT);
  // create the rake parameter
  DoubleParameter rakeParam = new DoubleParameter(RAKE_PARAM_NAME, RAKE_PARAM_MIN,
                                                      RAKE_PARAM_MAX,RAKE_PARAM_UNITS,
                                                      RAKE_PARAM_DEFAULT);
  // create the dip parameter
  DoubleParameter dipParam = new DoubleParameter(DIP_PARAM_NAME, DIP_PARAM_MIN,
                                                      DIP_PARAM_MAX,DIP_PARAM_UNITS,
                                                      DIP_PARAM_DEFAULT);
  // create the supported MagDists
  Vector supportedMagDists=new Vector();

  //Mag Freq Dist Parameter
  MagFreqDistParameter magDistParam ;

  // private declaration of the flag to check if any parameter has been changed from its original value.
  private boolean  parameterChangeFlag = true;


  /**
   * This constructor constructs the source
   *
   * No argument constructor
   */
  public PEER_AreaForecast() {

    // make adj params list
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(depthLowerParam);
    adjustableParams.addParameter(depthUpperParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(dipParam);
    adjustableParams.addParameter(timespanParam);

    // create the supported Mag-Dist parameter
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);
    adjustableParams.addParameter(this.magDistParam);


    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    depthLowerParam.addParameterChangeListener(this);
    depthUpperParam.addParameterChangeListener(this);
    timespanParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);

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


  /**
   * update the sources based on the user paramters, only when user has changed a parameter
   */
  public void updateForecast(){

    if(parameterChangeFlag) {

      // check if magDist is null
      if(this.magDistParam==null)
          throw new RuntimeException("Magnitude Distribution is null");

      double gridSpacing = ((Double)gridParam.getValue()).doubleValue();
      double depthLower =((Double)this.depthLowerParam.getValue()).doubleValue();
      double depthUpper =((Double)this.depthUpperParam.getValue()).doubleValue();

      if (depthUpper > depthLower)
          throw new RuntimeException("Upper Seis Depth must be ² Lower Seis Depth");

      //gets the change in latitude for grid spacing specified
      double latDiff = RelativeLocation.getDeltaLatFromKm(gridSpacing);
      double longDiff= RelativeLocation.getDeltaLonFromKm(LAT_CENTER,gridSpacing);

      // Create the grid of locations in the circular area
      locationList = new LocationList();
      for(double lat=LAT_TOP;lat >=LAT_BOTTOM; lat-=latDiff)
        for(double lon=LONG_LEFT;lon <=LONG_RIGHT; lon+=longDiff)
          if(RelativeLocation.getHorzDistance(LAT_CENTER,LONG_CENTER,lat,lon) <= MAX_DISTANCE)
            for(double depth=depthUpper;depth<=depthLower;depth+=gridSpacing)
                locationList.addLocation(new Location(lat,lon,depth));

      int numLocs = locationList.size();

      /* getting the Gutenberg magnitude distribution and scaling its cumRate to the original cumRate
       * divided by the number of the locations (note that this is a clone of what's in the magDistParam)
       */
      dist_GR = (GutenbergRichterMagFreqDist) ((GutenbergRichterMagFreqDist)magDistParam.getValue()).deepClone();

      double cumRate = dist_GR.getCumRate((int) 0);
      cumRate /= numLocs;
      dist_GR.scaleToCumRate(0,cumRate);

      double rake = ((Double) rakeParam.getValue()).doubleValue();
      double dip = ((Double) dipParam.getValue()).doubleValue();

      setTimeSpan(((Double) timespanParam.getValue()).doubleValue());

      // Dip is hard wired at 90 degrees
      pointGR_EqkSource = new PointGR_EqkSource(new Location(),dist_GR, rake, dip);

      if (D) System.out.println(C+" updateForecast(): rake="+pointGR_EqkSource.getRupture(0).getAveRake() +
                          "; dip="+ pointGR_EqkSource.getRupture(0).getRuptureSurface().getAveDip());

    }
    parameterChangeFlag = false;
  }




  /**
   * sets the timeSpan field
   * @param yrs : have to be modfied from the double varible to the timeSpan field variable
   */
  public void setTimeSpan(double yrs){
    timeSpan =yrs;
  }

  /**
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
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
   * method call may no longer bevalid.
   * this is secret, fast but dangerous method
   *
   * @param iSource : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public ProbEqkSource getSource(int iSource) {

    pointGR_EqkSource.setLocation(locationList.getLocationAt(iSource));
    pointGR_EqkSource.setTimeSpan(timeSpan);

    if (D) System.out.println(iSource + "th source location: "+ locationList.getLocationAt(iSource).toString() +
                              "; numRups="+pointGR_EqkSource.getNumRuptures());
    if (D) System.out.println("                     rake="+pointGR_EqkSource.getRupture(0).getAveRake() +
                              "; dip="+ pointGR_EqkSource.getRupture(0).getRuptureSurface().getAveDip());

    return pointGR_EqkSource;
  }

  /**
   * Get the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return locationList.size();
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

  }


  /**
   * Not yet implemented
   *
   * @return Iterator over all earhtquake sources
   */
  public Iterator getSourcesIterator() {

    return null;
  }

  /**
   * Not yet implemented
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
  public String getName() {
    return C;
  }


}