package org.scec.sha.earthquake;

import java.util.Vector;

import org.scec.data.TimeSpan;

import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.sha.param.*;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.calc.magScalingRelations.*;
import org.scec.sha.param.SimpleFaultParameter;


/**
 * <p>Title: SimplePoissonFaultERF</p>
 * <p>Description: T  </p>
 *
 * @author Ned Field
 * Date : Oct 24 , 2002
 * @version 1.0
 */

public class SimplePoissonFaultERF extends EqkRupForecast
    implements ParameterChangeListener {

  //for Debug purposes
  private static String  C = new String("Poisson Fault ERF");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  // this is the source (only 1 for this ERF)
  private SimplePoissonFaultSource source;

  //mag-freq dist parameter Name
  public final static String MAG_DIST_PARAM_NAME = "Mag Freq Dist";  // this is never shown by the MagFreqDistParameterEditor?

  // fault parameter name
  public final static String FAULT_PARAM_NAME = "Fault Parameter";

  // rupture offset parameter stuff
  public final static String OFFSET_PARAM_NAME =  "Rupture Offset";
  public final static String OFFSET_PARAM_INFO =  "The amount floating ruptures are offset along the fault";
  public final static String OFFSET_PARAM_UNITS = "km";
  private final static double OFFSET_PARAM_MIN = .01;
  private final static double OFFSET_PARAM_MAX = 100;
  private Double OFFSET_PARAM_DEFAULT = new Double(1);

  // Mag-scaling sigma parameter stuff
  public final static String SIGMA_PARAM_NAME =  "Mag Scaling Sigma";
  public final static String SIGMA_PARAM_INFO =  "The standard deviation of the Area(mag) or Length(M) relationship";
  private Double SIGMA_PARAM_MIN = new Double(0);
  private Double SIGMA_PARAM_MAX = new Double(1);
  public Double SIGMA_PARAM_DEFAULT = new Double(0.0);

  // rupture aspect ratio parameter stuff
  public final static String ASPECT_RATIO_PARAM_NAME = "Rupture Aspect Ratio";
  public final static String ASPECT_RATIO_PARAM_INFO = "The ratio of rupture length to rupture width";
  private Double ASPECT_RATIO_PARAM_MIN = new Double(Double.MIN_VALUE);
  private Double ASPECT_RATIO_PARAM_MAX = new Double(Double.MAX_VALUE);
  public Double ASPECT_RATIO_PARAM_DEFAULT = new Double(1.0);

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  public final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  public final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  public Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // min mag parameter stuff
  public final static String MIN_MAG_PARAM_NAME = "Min Mag";
  public final static String MIN_MAG_PARAM_INFO = "The minimum mag to be considered from the mag freq dist";
  private Double MIN_MAG_PARAM_MIN = new Double(0);
  private Double MIN_MAG_PARAM_MAX = new Double(10);
  public Double MIN_MAG_PARAM_DEFAULT = new Double(5);

  // parameter declarations
  MagFreqDistParameter magDistParam;
  SimpleFaultParameter faultParam;
  DoubleParameter rupOffsetParam;
  DoubleParameter sigmaParam;
  DoubleParameter aspectRatioParam;
  DoubleParameter rakeParam;
  DoubleParameter minMagParam;


  /**
   * Constructor for this source (no arguments)
   */
  public SimplePoissonFaultERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

System.out.println("magDistParam");
    // make the magFreqDistParameter
    Vector supportedMagDists=new Vector();
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(YC_1985_CharMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    faultParam = new SimpleFaultParameter(FAULT_PARAM_NAME);

    // create the rupOffset spacing param
    rupOffsetParam = new DoubleParameter(OFFSET_PARAM_NAME,OFFSET_PARAM_MIN,
        OFFSET_PARAM_MAX,OFFSET_PARAM_UNITS,OFFSET_PARAM_DEFAULT);
    rupOffsetParam.setInfo(OFFSET_PARAM_INFO);

    // create the mag-scaling sigma param
    sigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
        SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, SIGMA_PARAM_DEFAULT);
    sigmaParam.setInfo(SIGMA_PARAM_INFO);

// create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);

System.out.println("aspectRatioParam");
// create the aspect ratio param
    aspectRatioParam = new DoubleParameter(ASPECT_RATIO_PARAM_NAME,ASPECT_RATIO_PARAM_MIN,
        ASPECT_RATIO_PARAM_MAX,ASPECT_RATIO_PARAM_DEFAULT);
    aspectRatioParam.setInfo(ASPECT_RATIO_PARAM_INFO);


System.out.println("minMagParam");
// create the min mag param
    minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME,MIN_MAG_PARAM_MIN,
        MIN_MAG_PARAM_MAX,MIN_MAG_PARAM_DEFAULT);
    minMagParam.setInfo(MIN_MAG_PARAM_INFO);


// add the adjustable parameters to the list
    adjustableParams.addParameter(rupOffsetParam);
    adjustableParams.addParameter(sigmaParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(aspectRatioParam);
    adjustableParams.addParameter(minMagParam);
    adjustableParams.addParameter(faultParam);
    adjustableParams.addParameter(magDistParam);

    // register the parameters that need to be listened to
    rupOffsetParam.addParameterChangeListener(this);
    sigmaParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    aspectRatioParam.addParameterChangeListener(this);
    minMagParam.addParameterChangeListener(this);
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

       PEER_testsMagAreaRelationship magScalingRel = new PEER_testsMagAreaRelationship();

      // Now make the source
       source = new SimplePoissonFaultSource((IncrementalMagFreqDist) magDistParam.getValue(),
                                             (EvenlyGriddedSurface) faultParam.getValue(),
                                             (MagScalingRelationship) magScalingRel,
                                             ((Double) sigmaParam.getValue()).doubleValue(),
                                             ((Double) aspectRatioParam.getValue()).doubleValue(),
                                             ((Double) rupOffsetParam.getValue()).doubleValue(),
                                             ((Double)rakeParam.getValue()).doubleValue(),
                                             timeSpan.getDuration(),
                                             ((Double) minMagParam.getValue()).doubleValue());
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
