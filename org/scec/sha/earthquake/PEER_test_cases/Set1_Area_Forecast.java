package org.scec.sha.earthquake.PEER_test_cases;


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
import org.scec.sha.magdist.*;
import org.scec.param.event.*;


/**
 * <p>Title: Set1_Area_Forecast</p>
 * <p>Description: Area Equake rupture forecast. The Peer Group Test cases </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * Date : Oct 24, 2002
 * @version 1.0
 */

public class Set1_Area_Forecast extends EqkRupForecast
    implements ParameterChangeListener{

  /**
   * @todo variables
   */
  //for Debug purposes
  private static String  C = new String("Set1_Area_Forecast");
  private boolean D = false;

  /**
   * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
   */
  private double timeSpan;
  private TimeSpan time;

  /**
   * Declaration for the static lat and longs for the Area
   */
  private static final double LAT_TOP= 38.901;
  private static final double LAT_BOTTOM = 37.099;
  private static final double LAT_CENTER = 38.0;
  private static final double LONG_LEFT= -123.138;
  private static final double LONG_RIGHT= -120.862;
  private static final double LONG_CENTER= -122.0;

  private static final double MAX_DISTANCE =100;
  /**
   * definition of the vectors for storing the sources
   */
  private Vector area_EqkSources = new Vector();


  //Param Name
  private final static String GRID_PARAM_NAME =  "Area Grid Spacing";
  private final static String GRID_PARAM_UNITS =  "km";
  private final static double GRID_PARAM_MIN = 0.001;
  private final static double GRID_PARAM_MAX = 100;
  private final static String DEPTH_LOWER_PARAM_NAME =  "Lower Seis Depth";
  private final static String DEPTH_UPPER_PARAM_NAME =  "Upper Seis Depth";
  private final static String DEPTH_PARAM_UNITS = "km";
  private final static double DEPTH_PARAM_MIN = 0;
  private final static double DEPTH_PARAM_MAX = 30;
  private final static Double DEPTH_PARAM_DEFAULT = new Double(5);
  private final static String MAG_DIST_PARAM_NAME = "Area Mag Dist";

  //timespan Variable
  private final static String TIMESPAN_PARAM_NAME = "Timespan";
  private final static String TIMESPAN_PARAM_UNITS = "yrs";
  private final static Double TIMESPAN_PARAM_DEFAULT = new Double(1);
  private final static double TIMESPAN_PARAM_MIN = 1e-10;
  private final static double TIMESPAN_PARAM_MAX = 1e10;

   //Rake Variable
  private final static String RAKE_PARAM_NAME = "Rake Angle";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private final static Double RAKE_PARAM_DEFAULT = new Double(0);
  private final static double RAKE_PARAM_MIN = -180;
  private final static double RAKE_PARAM_MAX = 180;

  // default grid spacing is 1km
  private Double DEFAULT_GRID_VAL = new Double(1);

  //top loaction
  private LocationList locationList;


  // create the grid spacing field
  DoubleParameter gridParam=new DoubleParameter(GRID_PARAM_NAME,GRID_PARAM_MIN,
                                                GRID_PARAM_MAX,GRID_PARAM_UNITS,
                                                DEFAULT_GRID_VAL);

  // create Depth Lower
  DoubleParameter depthLowerParam = new DoubleParameter(DEPTH_LOWER_PARAM_NAME,DEPTH_PARAM_MIN,
                                                        DEPTH_PARAM_MAX,DEPTH_PARAM_UNITS,
                                                        DEPTH_PARAM_DEFAULT);

  // create depth Upper
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
  public Set1_Area_Forecast() {

    // make adj params list
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(depthLowerParam);
    adjustableParams.addParameter(depthUpperParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(timespanParam);

    // create the supported Mag-Dist parameter
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);
    //add the magdist parameter
    adjustableParams.addParameter(this.magDistParam);


    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    depthLowerParam.addParameterChangeListener(this);
    depthUpperParam.addParameterChangeListener(this);
    timespanParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);

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
   * update the sources based on the user paramters, only when user has changed any parameter
   */
  public void updateForecast(){

    if(parameterChangeFlag) {

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
          if(RelativeLocation.getLatLonDistance(LAT_CENTER,LONG_CENTER,lat,lon) <= MAX_DISTANCE)
            for(double depth=depthLower;depth<=depthUpper;depth+=gridSpacing)
                locationList.addLocation(new Location(lat,lon,depth));

      double numLocs = locationList.size();

      /* getting the Gutenberg magnitude distribution and scaling its cumRate to the original cumRate
       * divided by the number of the locations (note that this is a clone of what's in the magDistParam)
       */
      GutenbergRichterMagFreqDist gR = (GutenbergRichterMagFreqDist) ((GutenbergRichterMagFreqDist)magDistParam.getValue()).deepClone();

      double cumRate = gR.getCumRate((int) 0);
      cumRate /= numLocs;
      gR.scaleToCumRate(0,cumRate);

      //creating the PointGR sources  and adding the objects for sources in the vector.
      //FIX FIX have to have rake parameter;
      for(int i=0;i<numLocs;++i){
        PointGR_EqkSource pointGR_EqkSource = new PointGR_EqkSource(locationList.getLocationAt(i),gR,90);
        area_EqkSources.add(pointGR_EqkSource);
      }
      setTimeSpan(((Double) timespanParam.getValue()).doubleValue());
    }
    parameterChangeFlag = false;
  }




  /**
   * sets the timeSpan field
   * @param yrs : have to be modfied from the double varible to the timeSpan field variable
   */
  public void setTimeSpan(double yrs){
    timeSpan =yrs;
    int size = area_EqkSources.size();
    for( int i =0; i<size; ++i)
      ((PointGR_EqkSource)area_EqkSources.get(i)).setTimeSpan(yrs);

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
    return (ProbEqkSource) area_EqkSources.get(iSource);
  }

  /**
   * Get the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return area_EqkSources.size();
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

    return area_EqkSources.iterator();
  }

  /**
   * Get the list of all earthquake sources. Clone is returned.
   * All the 3 different Vector source List are combined into the one Vector list
   * So, list can be save in Vector and this object subsequently destroyed
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){

    return area_EqkSources;
  }



  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
  public String getName(){
    return C;
  }


}