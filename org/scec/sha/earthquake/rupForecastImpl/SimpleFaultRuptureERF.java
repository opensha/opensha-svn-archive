package org.scec.sha.earthquake.rupForecastImpl;

import java.util.Vector;

import org.scec.data.TimeSpan;

import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.sha.param.*;
import org.scec.param.event.*;
import org.scec.sha.magdist.*;
import org.scec.sha.param.SimpleFaultParameter;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureSource;


/**
 * <p>Title: SimpleFaultRuptureERF</p>
 * <p>Description: This ERF creates a single SimpleFaultRuptureSource (full fault rupture)
 * for the following user-defined parameters:  </p>
 * <UL>
 * <LI>mag-freq-dist
 * <LI>ruptureSurface - any EvenlyDiscretizedSurface
 * <LI>rake - that rake (in degrees) assigned to all ruptures.
 * <LI>timeSpan - the duration of the forecast (in same units as in the magFreqDist)
 * </UL><p>
 * The source is Poissonain, and the timeSpan is in years.
 * @author Ned Field
 * Date : Jan , 2004
 * @version 1.0
 */

public class SimpleFaultRuptureERF extends EqkRupForecast
    implements ParameterChangeListener {

  //for Debug purposes
  private static String  C = new String("Simple Fault Rupture ERF");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  // this is the source (only 1 for this ERF)
  private SimpleFaultRuptureSource source;

  //mag-freq dist parameter Name
  public final static String MAG_DIST_PARAM_NAME = "Mag Freq Dist";

  // fault parameter name
  public final static String FAULT_PARAM_NAME = "Fault Parameter";

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  private final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  private Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // parameter declarations
  MagFreqDistParameter magDistParam;
  SimpleFaultParameter faultParam;
  DoubleParameter rakeParam;


  /**
   * Constructor for this source (no arguments)
   */
  public SimpleFaultRuptureERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the magFreqDistParameter
    Vector supportedMagDists=new Vector();
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    // make the fault parameter
    faultParam = new SimpleFaultParameter(FAULT_PARAM_NAME);

    // create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);

    // add the adjustable parameters to the list
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(faultParam);
    adjustableParams.addParameter(magDistParam);

    // register the parameters that need to be listened to
    rakeParam.addParameterChangeListener(this);
    faultParam.addParameterChangeListener(this);
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

       source = new SimpleFaultRuptureSource((IncrementalMagFreqDist) magDistParam.getValue(),
                                             (EvenlyGriddedSurface) faultParam.getValue(),
                                             ((Double)rakeParam.getValue()).doubleValue(),
                                             timeSpan.getDuration());
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
     * @return Vector of Prob Earthquake sources
     */
    public Vector  getSourceList(){
      Vector v =new Vector();
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
