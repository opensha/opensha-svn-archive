package org.scec.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;

import org.scec.data.TimeSpan;

import org.scec.data.Location;
import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.sha.param.*;
import org.scec.param.event.*;
import org.scec.sha.magdist.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureSource;


/**
 * <p>Title: PointPoissonERF</p>
 * <p>Description: This ERF creates a single PointPoissonEqkSource
 * for the following user-defined parameters:  </p>
 * <UL>
 * <LI>mag-freq-dist
 * <LI>source location (lat, lon, depth params; should be one LocationParam)
 * <LI>rake - the rake (in degrees) assigned to all ruptures.
 * <LI>dip - the dip (in degrees) assigned to all rupture surfaces.
 * <LI>timeSpan - the duration of the forecast (in same units as in the magFreqDist)
 * </UL><p>
 * The source is Poissonain, and the timeSpan is in years.
 * @author Ned Field
 * Date : June , 2004
 * @version 1.0
 */

public class PointPoissonERF extends EqkRupForecast
    implements ParameterChangeListener {

  //for Debug purposes
  private static String  C = new String("Point Poisson ERF");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  // this is the source (only 1 for this ERF)
  private PointPoissonEqkSource source;

  //mag-freq dist parameter Name
  public final static String MAG_DIST_PARAM_NAME = "Mag Freq Dist";

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  private final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  private Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // dip parameter stuff
  public final static String DIP_PARAM_NAME = "Dip";
  private final static String DIP_PARAM_INFO = "The dip of the rupture surface";
  private final static String DIP_PARAM_UNITS = "degrees";
  private Double DIP_PARAM_MIN = new Double(0);
  private Double DIP_PARAM_MAX = new Double(90);
  private Double DIP_PARAM_DEFAULT = new Double(90);

  // the source-location parameters (this should be a location parameter)
  public final static String SRC_LAT_PARAM_NAME = "Source Latitude";
  private final static String SRC_LAT_PARAM_INFO = "Latitude of the point source";
  private final static String SRC_LAT_PARAM_UNITS = "Degrees";
  private Double SRC_LAT_PARAM_MIN = new Double (-90.0);
  private Double SRC_LAT_PARAM_MAX = new Double (90.0);
  private Double SRC_LAT_PARAM_DEFAULT = new Double (34.053);

  public final static String SRC_LON_PARAM_NAME = "Source Longitude";
  private final static String SRC_LON_PARAM_INFO = "Longitude of the point source";
  private final static String SRC_LON_PARAM_UNITS = "Degrees";
  private Double SRC_LON_PARAM_MIN = new Double (-360);
  private Double SRC_LON_PARAM_MAX = new Double (360);
  private Double SRC_LON_PARAM_DEFAULT = new Double (-118.243);

  public final static String SRC_DEPTH_PARAM_NAME = "Source Depth";
  private final static String SRC_DEPTH_PARAM_INFO = "Depth of the point source";
  private final static String SRC_DEPTH_PARAM_UNITS = "km";
  private Double SRC_DEPTH_PARAM_MIN = new Double (0);
  private Double SRC_DEPTH_PARAM_MAX = new Double (50);
  private Double SRC_DEPTH_PARAM_DEFAULT = new Double (10);


  // parameter declarations
  MagFreqDistParameter magDistParam;
  DoubleParameter dipParam;
  DoubleParameter rakeParam;
  DoubleParameter srcLatParam;
  DoubleParameter srcLonParam;
  DoubleParameter srcDepthParam;


  /**
   * Constructor for this source (no arguments)
   */
  public PointPoissonERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the magFreqDistParameter
    ArrayList supportedMagDists=new ArrayList();
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    // create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);

    // create the rake param
    dipParam = new DoubleParameter(DIP_PARAM_NAME,DIP_PARAM_MIN,
        DIP_PARAM_MAX,DIP_PARAM_UNITS,DIP_PARAM_DEFAULT);
    dipParam.setInfo(DIP_PARAM_INFO);

    // create src lat, lon, & depth param
    srcLatParam = new DoubleParameter(SRC_LAT_PARAM_NAME,SRC_LAT_PARAM_MIN,
        SRC_LAT_PARAM_MAX,SRC_LAT_PARAM_UNITS,SRC_LAT_PARAM_DEFAULT);
    srcLatParam.setInfo(SRC_LAT_PARAM_INFO);
    srcLonParam = new DoubleParameter(SRC_LON_PARAM_NAME,SRC_LON_PARAM_MIN,
        SRC_LON_PARAM_MAX,SRC_LON_PARAM_UNITS,SRC_LON_PARAM_DEFAULT);
    srcLonParam.setInfo(SRC_LON_PARAM_INFO);
    srcDepthParam = new DoubleParameter(SRC_DEPTH_PARAM_NAME,SRC_DEPTH_PARAM_MIN,
        SRC_DEPTH_PARAM_MAX,SRC_DEPTH_PARAM_UNITS,SRC_DEPTH_PARAM_DEFAULT);
    srcDepthParam.setInfo(SRC_DEPTH_PARAM_INFO);

    // add the adjustable parameters to the list
    adjustableParams.addParameter(srcLatParam);
    adjustableParams.addParameter(srcLonParam);
    adjustableParams.addParameter(srcDepthParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(dipParam);
    adjustableParams.addParameter(magDistParam);

    // register the parameters that need to be listened to
    rakeParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    srcLatParam.addParameterChangeListener(this);
    srcLonParam.addParameterChangeListener(this);
    srcDepthParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
  }


  /**
    *  This is the method called by any parameter whose value has been changed
    *
    * @param  event
    */
   public void parameterChange( ParameterChangeEvent event ) {
      parameterChangeFlag=true;
   }


   /**
    * update the source based on the paramters (only if a parameter value has changed)
    */
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       Location loc = new Location( ((Double)srcLatParam.getValue()).doubleValue(),
                                    ((Double)srcLonParam.getValue()).doubleValue(),
                                    ((Double)srcDepthParam.getValue()).doubleValue());
       source = new PointPoissonEqkSource(loc,
                                          (IncrementalMagFreqDist) magDistParam.getValue(),
                                          timeSpan.getDuration(),
                                          ((Double)rakeParam.getValue()).doubleValue(),
                                          ((Double)dipParam.getValue()).doubleValue());
       parameterChangeFlag = false;
     }

   }


   /**
    * Return the earhthquake source at index i.   Note that this returns a
    * pointer to the source held internally, so that if any parameters
    * are changed, and this method is called again, the source obtained
    * by any previous call to this method will no longer be valid.
    *
    * @param iSource : index of the desired source (only "0" allowed here).
    *
    * @return Returns the ProbEqkSource at index i
    *
    */
   public ProbEqkSource getSource(int iSource) {

     // we have only one source
    if(iSource!=0)
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

    return source;
   }


   /**
    * Returns the number of earthquake sources (always "1" here)
    *
    * @return integer value specifying the number of earthquake sources
    */
   public int getNumSources(){
     return 1;
   }


    /**
     *  This returns a list of sources (contains only one here)
     *
     * @return ArrayList of Prob Earthquake sources
     */
    public ArrayList  getSourceList(){
      ArrayList v =new ArrayList();
      v.add(source);
      return v;
    }


  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
   public String getName(){
     return NAME;
   }
}
