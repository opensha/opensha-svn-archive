package org.scec.sha.imr.attenRelImpl;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.propagation.*;
import org.scec.util.*;

/**
 * <b>Title:</b> WC94_DisplMagRel<p>
 *
 * <b>Description:</b> This implements the Wells and Coppersmith average displacement
 * versus magnitude relationship in their table 2B (1994, Bulletin of the Seismological
 * Society of America, vol 84, pp 974-1002).  This relationship assumes the site is right
 * on the fault where the event occurs (i.e., the Site object is irrelevant and is ignored).
 * This is a quick and dirty implementation done for Lucy Jones' Alaskan Pipeline problem <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>faultDisplParam - Fault Displacement (average)
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>fltTypeParam - Style of faulting
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Edward H. Field
 * @created    May, 2003
 * @version    1.0
 */


public class WC94_DisplMagRel
         extends AttenuationRelationship
         implements
        AttenuationRelationshipAPI,
        NamedObjectAPI {

    // debugging stuff:
    private final static String C = "WC94_DisplMagRel";
    private final static boolean D = false;
    public final static String NAME = "Wells & Coppersmith (1994)";

    /**
     * maximum rupture distance (rupture distances greater than this will always
     * return ln(mean_slip) that is negative infinity (slip that is zero)
     */
    private final static double MAX_DIST = 1.0;

    // supported IMT is fault displacement
    /**
     * fault displacement Intensity-Measure parameter (actually natural log thereof)
     */
    protected  WarningDoubleParameter faultDisplParam = null;
    public final static String FAULT_DISPL_NAME = "Fault Displacement";
    public final static String FAULT_DISPL_UNITS = "m";
    protected final static Double FAULT_DISPL_DEFAULT = new Double( Math.log( 1.0 ) );
    public final static String FAULT_DISPL_INFO = "Average Fault Displacement";
    protected final static Double FAULT_DISPL_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double FAULT_DISPL_MAX = new Double( Double.MAX_VALUE );
    protected final static Double FAULT_DISPL_WARN_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double FAULT_DISPL_WARN_MAX = new Double( Math.log( 50.0 ) );



    // style of faulting options
    public final static String FLT_TYPE_SS = "Strike Slip";
    public final static String FLT_TYPE_ALL = "Any Type";

    // warning constraint fields:
    protected final static Double MAG_WARN_MIN = new Double(5.0);
    protected final static Double MAG_WARN_MAX = new Double(9.0);
    protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
    protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);

    /**
     * The DistanceRupParameter, closest distance to fault surface.
     */
    private DistanceRupParameter distanceRupParam = null;
    private final static Double DISTANCE_RUP_DEFAULT = new Double( 0 );

    // for issuing warnings:
    private transient  ParameterChangeWarningListener warningListener = null;


    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam.
     *
     * @param rake                      in degrees
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected void setFaultTypeFromRake( double rake )
        throws InvalidRangeException
    {
        FaultUtils.assertValidRake( rake );
        if( rake >= 45 && rake <= 135 ) fltTypeParam.setValue(FLT_TYPE_ALL);
        else if( rake <= -45 && rake >= -135 ) fltTypeParam.setValue(FLT_TYPE_ALL);
        else fltTypeParam.setValue(FLT_TYPE_SS);
    }


    /**
     * this does nothing
     */
    protected void initCoefficients(  ) {

    }


    /**
     *  This sets the probEqkRupture & the related parameters (magParam
     *  and fltTypeParam).
     *
     * @param  pe  The new probEqkRupture value
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) throws ConstraintException{


        Double magOld = (Double)magParam.getValue( );

        try {
          // constraints get checked
          magParam.setValue( probEqkRupture.getMag() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }

        // If fail, rollback to all old values
        try{
            setFaultTypeFromRake( probEqkRupture.getAveRake() );
        }
        catch( ConstraintException e ){
            magParam.setValue( magOld );
            throw e;
        }

        // Set the probEqkRupture
        this.probEqkRupture = probEqkRupture;

       /* Calculate the PropagationEffectParameters; this is
        * not efficient if both the site and probEqkRupture
        * are set before getting the mean, stdDev, or ExceedProbability
        */
        setPropagationEffectParams();

    }


    /**
     *
     * @param  site
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {

         // Now pass function up to super to set the site
         super.setSite( site );

         // Calculate the PropagationEffectParameters; this is
         // not efficient if both the site and probEqkRupture
         // are set before getting the mean, stdDev, or ExceedProbability
         setPropagationEffectParams();

    }

    /**
     *  <P>
     */
    protected void setPropagationEffectParams(){

      if( ( this.site != null ) && ( this.probEqkRupture != null ) ){
          try{
            distanceRupParam.setValue( probEqkRupture, site );
          }catch (WarningException e){
            if(D) System.out.println(C+"Warning Exception:"+e);
          }
      }

    }

    /**
     * This does nothing
     */
    protected void updateCoefficients() throws ParameterException {

    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public WC94_DisplMagRel( ParameterChangeWarningListener warningListener ) {

        super();

        this.warningListener = warningListener;

        initCoefficients( );  // This must be called before the next one
        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initIndependentParamLists(); // Do this after the above

    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public WC94_DisplMagRel(  ) {

        super();

        initCoefficients( );  // This must be called before the next one
        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initIndependentParamLists(); // Do this after the above

    }


    /**
    * get the name of this IMR
    *
    * @returns the name of this IMR
    */
    public String getName() {
     return NAME;
    }


    /**
     * Calculates the mean of the exceedence probability distribution. <p>
     *
     * @return    The mean value
     */
    public double getMean() throws IMRException{

        double mag, dist;
        String fltTypeValue;

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
            dist = ((Double)distanceRupParam.getValue()).doubleValue();
            fltTypeValue = fltTypeParam.getValue().toString();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }

        double mean;

        if (dist < MAX_DIST) {
          if ( fltTypeValue.equals( FLT_TYPE_SS ) )
            mean = 0.9*mag - 6.32;
          else
            mean = 0.69*mag - 4.8;
        }
        else
          return FAULT_DISPL_MIN.doubleValue();

        // convert log10 to natural log
        mean *= 2.3026;

        // return the result
        return (mean);
    }


    /**
     * @return    The stdDev value
     */
    public double getStdDev() throws IMRException {
      String fltTypeValue;
      String stdDevType = stdDevTypeParam.getValue().toString();

      try{
          fltTypeValue = fltTypeParam.getValue().toString();
      }
      catch(NullPointerException e){
          throw new IMRException(C + ": getStdDev(): " + ERR);
      }

      if (stdDevType.equals(this.STD_DEV_TYPE_NONE))
          return 0.0;
      else {
        if ( fltTypeValue.equals( FLT_TYPE_SS ) )
          return 0.28;
        else
          return 0.36;
      }

    }


    public void setParamDefaults(){

        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_SS );
        distanceRupParam.setValue( DISTANCE_RUP_DEFAULT );
        faultDisplParam.setValue( FAULT_DISPL_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_INTER );
    }


    /**
     * This creates the lists of independent parameters that the various dependent
     * parameters (mean, standard deviation, exceedance probability, and IML at
     * exceedance probability) depend upon. NOTE: these lists do not include anything
     * about the intensity-measure parameters or any of thier internal
     * independentParamaters.
     */
    protected void initIndependentParamLists(){

        // params that the mean depends upon
        meanIndependentParams.clear();
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( distanceRupParam );


        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter( fltTypeParam );
        stdDevIndependentParams.addParameter(stdDevTypeParam);

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( magParam );
        exceedProbIndependentParams.addParameter( fltTypeParam );
        exceedProbIndependentParams.addParameter( distanceRupParam );
        exceedProbIndependentParams.addParameter( stdDevTypeParam );
        exceedProbIndependentParams.addParameter(this.sigmaTruncTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncLevelParam);

        // params that the IML at exceed. prob. depends upon
        imlAtExceedProbIndependentParams.addParameterList(exceedProbIndependentParams);
        imlAtExceedProbIndependentParams.addParameter(exceedProbParam);

    }


    /**
     *  does nothing.
     */
    protected void initSiteParams( ) {

    }


    /**
     *  Creates the two Potential Earthquake parameters (magParam and
     *  fltTypeParam) and adds them to the probEqkRuptureParams
     *  list. Makes the parameters noneditable.
     */
    protected void initProbEqkRuptureParams(  ) {

        // Create magParam
        super.initProbEqkRuptureParams();

        //  Create and add warning constraint to magParam:
        DoubleConstraint warn = new DoubleConstraint(MAG_WARN_MIN, MAG_WARN_MAX);
        warn.setNonEditable();
        magParam.setWarningConstraint(warn);
        magParam.addParameterChangeWarningListener( warningListener );
        magParam.setNonEditable();

        StringConstraint constraint = new StringConstraint();
        constraint.addString( FLT_TYPE_SS );
        constraint.addString( FLT_TYPE_ALL );
        constraint.setNonEditable();
        fltTypeParam = new StringParameter( FLT_TYPE_NAME, constraint, null);
        fltTypeParam.setInfo( FLT_TYPE_INFO );
        fltTypeParam.setNonEditable();

        probEqkRuptureParams.clear();
        probEqkRuptureParams.addParameter( magParam );
        probEqkRuptureParams.addParameter( fltTypeParam );

    }

    /**
     *
     */
    protected void initPropagationEffectParams( ) {
      distanceRupParam = new DistanceRupParameter();
      distanceRupParam.addParameterChangeWarningListener( warningListener );
      DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN, DISTANCE_RUP_WARN_MAX);
      warn.setNonEditable();
      distanceRupParam.setWarningConstraint(warn);
      distanceRupParam.setNonEditable();

      propagationEffectParams.addParameter( distanceRupParam );

    }


    /**
     *  Creates the one supported IM parameter.
     */
    protected void initSupportedIntensityMeasureParams( ) {

      // Create FAULT_DISPL Parameter
      DoubleConstraint fDisplConstraint = new DoubleConstraint(FAULT_DISPL_MIN, FAULT_DISPL_MAX);
      fDisplConstraint.setNonEditable();
      faultDisplParam = new WarningDoubleParameter( FAULT_DISPL_NAME, fDisplConstraint, FAULT_DISPL_UNITS);
      faultDisplParam.setInfo( FAULT_DISPL_INFO );
      DoubleConstraint warn2 = new DoubleConstraint(FAULT_DISPL_WARN_MIN, FAULT_DISPL_WARN_MAX);
      warn2.setNonEditable();
      faultDisplParam.setWarningConstraint(warn2);
      faultDisplParam.setNonEditable();

      // Add the warning listeners:
      faultDisplParam.addParameterChangeWarningListener( warningListener );

      // Put parameters in the supportedIMParams list:
      supportedIMParams.clear();
      supportedIMParams.addParameter( faultDisplParam );
    }

    /**
     *  Creates other Parameters that the mean or stdDev depends upon,
     *  such as the Component or StdDevType parameters.
     */
    protected void initOtherParams( ) {

        // init other params defined in parent class
        super.initOtherParams();

        // the stdDevType Parameter
        StringConstraint stdDevTypeConstraint = new StringConstraint();
        stdDevTypeConstraint.addString( STD_DEV_TYPE_INTER );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_NONE );
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_INTER );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();

        otherParams.addParameter( stdDevTypeParam );

    }


    public static void main(String[] args) {
      WC94_DisplMagRel testRel = new WC94_DisplMagRel();
      testRel.setIntensityMeasure("Fault Displacement");

      Site site = new Site(new Location(30.01,30.0),"test");
      testRel.setSite(site);

      ProbEqkRupture rup = new ProbEqkRupture();
      rup.setPointSurface(new Location(30.0,30.0),90.0);
      rup.setAveRake(0.0);

      rup.setMag(5);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 5: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(6);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 6: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(7);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 7: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(8);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 8: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(9);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 9: " + testRel.getMean() + "; " + testRel.getStdDev());


      rup.setAveRake(90);

      rup.setMag(5);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 5: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(6);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 6: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(7);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 7: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(8);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 8: " + testRel.getMean() + "; " + testRel.getStdDev());

      rup.setMag(9);
      testRel.setProbEqkRupture(rup);
      System.out.println("Mag 9: " + testRel.getMean() + "; " + testRel.getStdDev());
    }


}



