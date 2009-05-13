package org.opensha.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;

import org.opensha.data.TimeSpan;

import org.opensha.param.*;
import org.opensha.sha.param.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.calc.magScalingRelations.*;
import org.opensha.sha.param.SimpleFaultParameter;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.faultSurface.*;


/**
 * <p>Title: FloatingPoissonFaultERF</p>
 * <p>Description: T  </p>
 *
 * @author Ned Field
 * Date : Oct 24 , 2002
 * @version 1.0
 */

public class FloatingPoissonFaultERF extends EqkRupForecast{

  //for Debug purposes
  private static String  C = new String("FloatingPoissonFaultERF");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = "Floating Poisson Fault ERF";

  // this is the source (only 1 for this ERF)
  private FloatingPoissonFaultSource source;

  //mag-freq dist parameter Name
  public final static String MAG_DIST_PARAM_NAME = "Mag Freq Dist";

  // fault parameter name
  public final static String FAULT_PARAM_NAME = "Fault Parameter";

  // rupture offset parameter stuff
  public final static String OFFSET_PARAM_NAME =  "Rupture Offset";
  private final static String OFFSET_PARAM_INFO =  "The amount floating ruptures are offset along the fault";
  private final static String OFFSET_PARAM_UNITS = "km";
  private final static double OFFSET_PARAM_MIN = .01;
  private final static double OFFSET_PARAM_MAX = 100;
  private Double OFFSET_PARAM_DEFAULT = new Double(1);

  // Mag-scaling relationship parameter stuff
  public final static String MAG_SCALING_REL_PARAM_NAME = "Mag-Scaling Relationship";
  private final static String MAG_SCALING_REL_PARAM_INFO = "Relationship to use for Area(Mag) or Area(Length) calculations";
  private ArrayList magScalingRelOptions;


  // Mag-scaling sigma parameter stuff
  public final static String SIGMA_PARAM_NAME =  "Mag Scaling Sigma";
  private final static String SIGMA_PARAM_INFO =  "The standard deviation of the Area(mag) or Length(M) relationship";
  private Double SIGMA_PARAM_MIN = new Double(0);
  private Double SIGMA_PARAM_MAX = new Double(1);
  private Double SIGMA_PARAM_DEFAULT = new Double(0.0);

  // rupture aspect ratio parameter stuff
  public final static String ASPECT_RATIO_PARAM_NAME = "Rupture Aspect Ratio";
  private final static String ASPECT_RATIO_PARAM_INFO = "The ratio of rupture length to rupture width";
  private Double ASPECT_RATIO_PARAM_MIN = new Double(Double.MIN_VALUE);
  private Double ASPECT_RATIO_PARAM_MAX = new Double(Double.MAX_VALUE);
  private Double ASPECT_RATIO_PARAM_DEFAULT = new Double(1.0);

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  private final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  private Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // min mag parameter stuff
  public final static String MIN_MAG_PARAM_NAME = "Min Mag";
  private final static String MIN_MAG_PARAM_INFO = "The minimum mag to be considered from the mag freq dist";
  private Double MIN_MAG_PARAM_MIN = new Double(0);
  private Double MIN_MAG_PARAM_MAX = new Double(10);
  private Double MIN_MAG_PARAM_DEFAULT = new Double(5);

  // parameter declarations
  MagFreqDistParameter magDistParam;
  SimpleFaultParameter faultParam;
  DoubleParameter rupOffsetParam;
  StringParameter magScalingRelParam;
  DoubleParameter sigmaParam;
  DoubleParameter aspectRatioParam;
  DoubleParameter rakeParam;
  DoubleParameter minMagParam;


  /**
   * Constructor for this source (no arguments)
   */
  public FloatingPoissonFaultERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the magFreqDistParameter
    ArrayList supportedMagDists=new ArrayList();
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(YC_1985_CharMagFreqDist.NAME);
    supportedMagDists.add(SummedMagFreqDist.NAME);
    supportedMagDists.add(ArbIncrementalMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    faultParam = new SimpleFaultParameter(FAULT_PARAM_NAME);

    // create the rupOffset spacing param
    rupOffsetParam = new DoubleParameter(OFFSET_PARAM_NAME,OFFSET_PARAM_MIN,
        OFFSET_PARAM_MAX,OFFSET_PARAM_UNITS,OFFSET_PARAM_DEFAULT);
    rupOffsetParam.setInfo(OFFSET_PARAM_INFO);

    // create the mag-scaling relationship param
    magScalingRelOptions = new ArrayList();
    magScalingRelOptions.add(WC1994_MagAreaRelationship.NAME);
    magScalingRelOptions.add(WC1994_MagLengthRelationship.NAME);
    magScalingRelOptions.add(PEER_testsMagAreaRelationship.NAME);
    magScalingRelParam = new StringParameter(MAG_SCALING_REL_PARAM_NAME,magScalingRelOptions,
                                             WC1994_MagAreaRelationship.NAME);
    magScalingRelParam.setInfo(MAG_SCALING_REL_PARAM_INFO);

    // create the mag-scaling sigma param
    sigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
        SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, SIGMA_PARAM_DEFAULT);
    sigmaParam.setInfo(SIGMA_PARAM_INFO);

    // create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);

    // create the aspect ratio param
    aspectRatioParam = new DoubleParameter(ASPECT_RATIO_PARAM_NAME,ASPECT_RATIO_PARAM_MIN,
        ASPECT_RATIO_PARAM_MAX,ASPECT_RATIO_PARAM_DEFAULT);
    aspectRatioParam.setInfo(ASPECT_RATIO_PARAM_INFO);

    // create the min mag param
    minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME,MIN_MAG_PARAM_MIN,
        MIN_MAG_PARAM_MAX,MIN_MAG_PARAM_DEFAULT);
    minMagParam.setInfo(MIN_MAG_PARAM_INFO);

    // add the adjustable parameters to the list
    adjustableParams.addParameter(rupOffsetParam);
    adjustableParams.addParameter(magScalingRelParam);
    adjustableParams.addParameter(sigmaParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(aspectRatioParam);
    adjustableParams.addParameter(minMagParam);
    adjustableParams.addParameter(faultParam);
    adjustableParams.addParameter(magDistParam);

    // register the parameters that need to be listened to
    rupOffsetParam.addParameterChangeListener(this);
    magScalingRelParam.addParameterChangeListener(this);
    sigmaParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    aspectRatioParam.addParameterChangeListener(this);
    minMagParam.addParameterChangeListener(this);
    faultParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
  }



   /**
    * update the source based on the paramters (only if a parameter value has changed)
    */
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       // set the mag-scaling relationship
       String magScalingRelString = (String) magScalingRelParam.getValue();
       if (D) System.out.println(S+"  "+magScalingRelString);

       MagScalingRelationship magScalingRel;

       if (magScalingRelString.equals(WC1994_MagAreaRelationship.NAME))
          magScalingRel = new WC1994_MagAreaRelationship();
       else if (magScalingRelString.equals(WC1994_MagLengthRelationship.NAME))
          magScalingRel = new WC1994_MagLengthRelationship();
       else
          magScalingRel = new PEER_testsMagAreaRelationship();

       if (D) System.out.println(S+"  "+magScalingRel.getName());
       
//       System.out.println(((EvenlyGriddedSurface) faultParam.getValue()).getSurfaceLength());

       source = new FloatingPoissonFaultSource((IncrementalMagFreqDist) magDistParam.getValue(),
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
