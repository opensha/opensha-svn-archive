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
 * <b>Title:</b> Field_2000_AttenRel<p>
 *
 * <b>Description:</b> This implements the classicIMR (attenuation relationship)
 * developed by Field (2000, Bulletin of the Seismological Society of America, vol
 * 90, num 6b, pp S209-S221) <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceJBParam - closest distance to surface projection of fault
 * <LI>vs30Param - Average 30-meter shear-wave velocity at the site
 * <LI>basinDepthParam - Depth to 2.5 km/sec S-wave velocity
 * <LI>fltTypeParam - Style of faulting
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Edward H. Field
 * @created    February 27, 2002
 * @version    1.0
 */


public class Field_2000_AttenRel
         extends AttenuationRelationship
         implements
        AttenuationRelationshipAPI,
        NamedObjectAPI {

    // debugging stuff:
    private final static String C = "Field_2000_AttenRel";
    private final static boolean D = false;
    public final static String NAME = "Field (2000)";

    // style of faulting options
    public final static String FLT_TYPE_OTHER = "Other/Unknown";
    public final static String FLT_TYPE_REVERSE = "Reverse";
    public final static String FLT_TYPE_DEFAULT = "Other/Unknown";

    protected WarningDoubleParameter basinDepthParam = null;
    public final static String BASIN_DEPTH_NAME = "Basin-Depth-2.5";
    public final static String BASIN_DEPTH_UNITS = "km";
    public final static String BASIN_DEPTH_INFO =
      "Depth to 2.5 km/sec S-wave-velocity isosurface, from SCEC Phase III Report";
    protected final static Double BASIN_DEPTH_DEFAULT = new Double(0.0);
    protected final static Double BASIN_DEPTH_MIN = new Double(0);
    protected final static Double BASIN_DEPTH_MAX = new Double(30);
    protected final static Double BASIN_DEPTH_WARN_MIN = new Double(0);
    protected final static Double BASIN_DEPTH_WARN_MAX = new Double(10);


    // warning constraint fields:
    protected final static Double VS30_WARN_MIN = new Double(180.0);
    protected final static Double VS30_WARN_MAX = new Double(3500.0);
    protected final static Double MAG_WARN_MIN = new Double(5.0);
    protected final static Double MAG_WARN_MAX = new Double(8.0);

    /**
     * Joyner-Boore Distance parameter
     */
    private DistanceJBParameter distanceJBParam = null;
    private final static Double DISTANCE_JB_DEFAULT = new Double( 0 );
    protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
    protected final static Double DISTANCE_JB_WARN_MAX = new Double(150.0);

    /**
     * The current set of coefficients based on the selected intensityMeasure
     */
    private Field_2000_AttenRelCoefficients coeff = null;

    /**
     *  Hashtable of coefficients for the supported intensityMeasures
     */
    protected Hashtable coefficients = new Hashtable();

    // for issuing warnings:
    ParameterChangeWarningListener warningListener = null;


    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam.
     *
     * @param rake                      Input determines the fault type
     * @return                          Fault Type, either Strike-Slip,
     * Reverse, or Unknown if the rake is within 30 degrees of 0 or 180 degrees,
     * between 30 and 150 degrees, or not one of these two cases, respectivly.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( double rake )
        throws InvalidRangeException
    {
        FaultUtils.assertValidRake( rake );
        if( rake >= 45 && rake <= 135 ) return FLT_TYPE_REVERSE;
        else return FLT_TYPE_OTHER;
    }

    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam.
     *
     * @param rake                      Input determines the fault type
     * @return                          Fault Type, either Strike-Slip,
     * Reverse, or Unknown if the rake is within 30 degrees of 0 or 180 degrees,
     * between 30 and 150 degrees, or not one of these two cases, respectivly.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( Double rake )
        throws InvalidRangeException
    {
        if ( rake == null ) return FLT_TYPE_OTHER;
        else return determineFaultTypeFromRake( rake.doubleValue() );
    }


    /**
     *  This sets the potential-earthquake related parameters (magParam
     *  and fltTypeParam) based on the probEqkRupture passed in.
     *  The internally held probEqkRupture object is also set as that
     *  passed in. Since this object updates more than one parameter, an
     *  attempt is made to rollback to the original parameter values in case
     *  there are any errors thrown in the process.
     *
     * @param  pe  The new probEqkRupture value
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) throws ConstraintException{


        Double magOld = (Double)magParam.getValue( );
        String fltOld = (String)fltTypeParam.getValue();

        try {
          // constraints get checked
          magParam.setValue( probEqkRupture.getMag() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }

        // If fail, rollback to all old values
        try{
            String fltTypeStr = determineFaultTypeFromRake( probEqkRupture.getAveRake() );
            fltTypeParam.setValue(fltTypeStr);
        }
        catch( ConstraintException e ){
            magParam.setValue( magOld );
            throw e;
        }

        // Set the PE
        this.probEqkRupture = probEqkRupture;

       /* Calculate the PropagationEffectParameters; this is
        * not efficient if both the site and probEqkRupture
        * are set before getting the mean, stdDev, or ExceedProbability
        */
        setPropagationEffectParams();

    }


    /**
     *  This sets the site-related parameter (vs30Param) based on what is in
     *  the Site object passed in (the Site object must have a parameter with
     *  the same name as that in vs30Param).  This also sets the internally held
     *  Site object as that passed in.
     *
     * @param  site             The new site value which contains a Vs30 Parameter
     * @throws ParameterException Thrown if the Site object doesn't contain a
     * Vs30 parameter
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


        // This will throw a parameter exception if the Vs30Param doesn't exist
        // in the Site object
        ParameterAPI vs30 = site.getParameter( VS30_NAME );

         ParameterAPI basinDepth = site.getParameter(BASIN_DEPTH_NAME);

        // This may throw a constraint exception
         try{
           this.vs30Param.setValue( vs30.getValue() );
         } catch (WarningException e){
           if(D) System.out.println(C+"Warning Exception:"+e);
         }

         try{
           this.basinDepthParam.setValue( basinDepth.getValue() );
         } catch (WarningException e){
           if(D) System.out.println(C+"Warning Exception:"+e);
         }


         // Now pass function up to super to set the site
         super.setSite( site );

         // Calculate the PropagationEffectParameters; this is
         // not efficient if both the site and probEqkRupture
         // are set before getting the mean, stdDev, or ExceedProbability
         setPropagationEffectParams();

    }

    /**
     * This calculates the Distance JB propagation effect parameter based
     * on the current site and probEqkRupture. <P>
     */
    protected void setPropagationEffectParams(){

        if( ( this.site != null ) && ( this.probEqkRupture != null ) ){
          try{
            distanceJBParam.setValue( probEqkRupture, site );
          }catch (WarningException e){
            if(D) System.out.println(C+"Warning Exception:"+e);
          }
        }
    }

    /**
     * This function determines which set of coefficients in the HashMap
     * are to be used given the current intensityMeasure (im) Parameter. The
     * lookup is done keyed on the name of the im, plus the period value if
     * im.getName() == "SA" (seperated by "/").
     *
     * SWR: I choose the name <code>update</code> instead of set, because set is so common
     * to java bean fields, i.e. getters and setters, that set() usually implies
     * passing in a new value to the java bean field. I prefer update or refresh
     * to functions that change internal values internally
     */
    protected void updateCoefficients() throws ParameterException {

        // Check that parameter exists
        if( im == null ) throw new ParameterException( C +
            ": updateCoefficients(): " +
            "The Intensity Measusre Parameter has not been set yet, unable to process."
        );


        StringBuffer key = new StringBuffer( im.getName() );
        if( im.getName() == SA_NAME ) key.append( "/" + periodParam.getValue() );
        if( coefficients.containsKey( key.toString() ) ) coeff = ( Field_2000_AttenRelCoefficients )coefficients.get( key.toString() );
        else throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public Field_2000_AttenRel( ParameterChangeWarningListener warningListener ) {

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

        double mag, vs30, distanceJB, depth;
        String fltTypeValue;

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
            vs30 = ((Double)vs30Param.getValue()).doubleValue();
            distanceJB = ((Double)distanceJBParam.getValue()).doubleValue();
            fltTypeValue = fltTypeParam.getValue().toString();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }



        // the following is inefficient if the im Parameter has not been changed in any way
        updateCoefficients();

        // Calculate b1 based on fault type
        double b1;
        if ( fltTypeValue.equals( FLT_TYPE_REVERSE ) ) {
            b1 = coeff.b1rv;
        } else if ( fltTypeValue.equals( FLT_TYPE_OTHER ) ) {
            b1 = coeff.b1ss;
        } else {
            throw new ParameterException( C + ": getMean(): Invalid ProbEqkRupture Parameter value for : FaultType" );
        }

        // Calculate the log mean
        double mean = b1 +
            coeff.b2 * ( mag - 6 ) +
            coeff.b3 * ( Math.pow( ( mag - 6 ), 2 ) ) +
            coeff.b5 * ( Math.log( Math.pow( ( distanceJB * distanceJB  + coeff.h * coeff.h  ), 0.5 ) ) ) +
            coeff.bv * ( Math.log( vs30 / 760 ) );


        if( basinDepthParam.getValue() != null ) {
            depth = ((Double)basinDepthParam.getValue()).doubleValue();
            mean += coeff.bdSlope*depth + coeff.bdIntercept;
        }


        // No longer part of out framework. Always deal with log space
        // Convert back to normal value
        // mean = Math.exp(mean);


        // return the result
        return (mean);
    }


    /**
     * @return    The stdDev value
     */
    public double getStdDev() throws IMRException {

        double mag;
        String stdDevType = stdDevTypeParam.getValue().toString();

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }


        // this is inefficient if the im has not been changed in any way
        updateCoefficients();

        // set the correct standard deviation depending on component and type

        if ( stdDevType.equals( STD_DEV_TYPE_TOTAL ) ) {           // "Total"
            double stdev =  Math.pow( ( coeff.intra_slope*mag + coeff.intra_intercept + coeff.tau*coeff.tau ) , 0.5) ;
            return  stdev;
        }
        else if ( stdDevType.equals( STD_DEV_TYPE_INTER ) ) {    // "Inter-Event"
            return  coeff.tau;
        }
        else if ( stdDevType.equals( STD_DEV_TYPE_INTRA ) ) {    // "Intra-Event"
            return Math.pow(coeff.intra_slope*mag + coeff.intra_intercept , 0.5 ) ;
        }
        else if ( stdDevType.equals( STD_DEV_TYPE_NONE) ) {    // "None (zero)"
            return 0 ;
        }
            else {
              throw new ParameterException( C + ": getStdDev(): Invalid StdDevType" );
        }
    }


    public void setParamDefaults(){

        //((ParameterAPI)this.iml).setValue( IML_DEFAULT );
        vs30Param.setValue( VS30_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_DEFAULT );
        distanceJBParam.setValue( DISTANCE_JB_DEFAULT );
        saParam.setValue( SA_DEFAULT );
        periodParam.setValue( PERIOD_DEFAULT );
        dampingParam.setValue(DAMPING_DEFAULT);
        pgaParam.setValue( PGA_DEFAULT );
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );
        basinDepthParam.setValue( BASIN_DEPTH_DEFAULT );

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
        meanIndependentParams.addParameter( distanceJBParam );
        meanIndependentParams.addParameter( vs30Param );
        meanIndependentParams.addParameter( basinDepthParam );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( componentParam );


        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        stdDevIndependentParams.addParameter( magParam );

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( distanceJBParam );
        exceedProbIndependentParams.addParameter( vs30Param );
        exceedProbIndependentParams.addParameter( basinDepthParam );
        exceedProbIndependentParams.addParameter( magParam );
        exceedProbIndependentParams.addParameter( fltTypeParam );
        exceedProbIndependentParams.addParameter( componentParam );
        exceedProbIndependentParams.addParameter(stdDevTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncLevelParam);

        // params that the IML at exceed. prob. depends upon
        imlAtExceedProbIndependentParams.addParameterList(exceedProbIndependentParams);
        imlAtExceedProbIndependentParams.addParameter(exceedProbParam);

    }


    /**
     *  Creates the Vs30 & basinDepth arameters and adds them to the siteParams list.
     *  Makes the parameters noneditable.
     */
    protected void initSiteParams( ) {

        // create vs30 Parameter:
        super.initSiteParams();

        // create and add the warning constraint:
        DoubleConstraint warn = new DoubleConstraint(VS30_WARN_MIN, VS30_WARN_MAX);
        warn.setNonEditable();
        vs30Param.setWarningConstraint(warn);
        vs30Param.addParameterChangeWarningListener( warningListener );
        vs30Param.setNonEditable();

        DoubleConstraint basinDepthConstraint = new DoubleConstraint(BASIN_DEPTH_MIN, BASIN_DEPTH_MAX);
        basinDepthConstraint.setNullAllowed(true);
        basinDepthConstraint.setNonEditable();
        basinDepthParam = new WarningDoubleParameter( BASIN_DEPTH_NAME, basinDepthConstraint, BASIN_DEPTH_UNITS);
        basinDepthParam.setInfo( BASIN_DEPTH_INFO );
        basinDepthParam.setNonEditable();


        // add it to the siteParams list:
        siteParams.clear();
        siteParams.addParameter( vs30Param );
        siteParams.addParameter( basinDepthParam );

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
        constraint.addString( FLT_TYPE_OTHER );
        constraint.addString( FLT_TYPE_REVERSE );
        constraint.setNonEditable();
        fltTypeParam = new StringParameter( FLT_TYPE_NAME, constraint, null);
        fltTypeParam.setInfo( FLT_TYPE_INFO );
        fltTypeParam.setNonEditable();

        probEqkRuptureParams.clear();
        probEqkRuptureParams.addParameter( magParam );
        probEqkRuptureParams.addParameter( fltTypeParam );

    }

    /**
     *  Creates the single Propagation Effect parameter and adds it to the
     *  propagationEffectParams list. Makes the parameters noneditable.
     */
    protected void initPropagationEffectParams( ) {
        distanceJBParam = new DistanceJBParameter();
        distanceJBParam.addParameterChangeWarningListener( warningListener );
        DoubleConstraint warn = new DoubleConstraint(DISTANCE_JB_WARN_MIN, DISTANCE_JB_WARN_MAX);
        warn.setNonEditable();
        distanceJBParam.setWarningConstraint(warn);
        distanceJBParam.setNonEditable();
        propagationEffectParams.addParameter( distanceJBParam );
    }


    /**
     *  Creates the two supported IM parameters (PGA and SA), as well as the
     *  independenParameters of SA (periodParam and dampingParam) and adds
     *  them to the supportedIMParams list. Makes the parameters noneditable.
     */
    protected void initSupportedIntensityMeasureParams( ) {

        // Create saParam (& its dampingParam) and pgaParam:
        super.initSupportedIntensityMeasureParams();

        // Create saParam's "Period" independent parameter:
        DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
        TreeSet set = new TreeSet();
        Enumeration keys = coefficients.keys();
        while ( keys.hasMoreElements() ) {
            Field_2000_AttenRelCoefficients coeff = ( Field_2000_AttenRelCoefficients ) coefficients.get( keys.nextElement() );
            if ( coeff.period >= 0 )  set.add( new Double( coeff.period ) );
        }
        Iterator it = set.iterator();
        while ( it.hasNext() ) periodConstraint.addDouble( ( Double ) it.next() );
        periodConstraint.setNonEditable();
        periodParam = new DoubleDiscreteParameter( PERIOD_NAME, periodConstraint, PERIOD_UNITS, null );
        periodParam.setInfo( PERIOD_INFO );
        periodParam.setNonEditable();

        // Set damping constraint as non editable since no other options exist
        dampingConstraint.setNonEditable();

        // Add SA's independent parameters:
        saParam.addIndependentParameter(dampingParam);
        saParam.addIndependentParameter(periodParam);

        // Now Make the parameter noneditable:
        saParam.setNonEditable();

        // Add the warning listeners:
        saParam.addParameterChangeWarningListener( warningListener );
        pgaParam.addParameterChangeWarningListener( warningListener );

        // Put parameters in the supportedIMParams list:
        supportedIMParams.clear();
        supportedIMParams.addParameter( saParam );
        supportedIMParams.addParameter( pgaParam );

    }

    /**
     *  Creates other Parameters that the mean or stdDev depends upon,
     *  such as the Component or StdDevType parameters.
     */
    protected void initOtherParams( ) {

        // init other params defined in parent class
        super.initOtherParams();

        // the Component Parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( COMPONENT_AVE_HORZ );
        constraint.setNonEditable();
        componentParam = new StringParameter( COMPONENT_NAME, constraint, COMPONENT_DEFAULT );
        componentParam.setInfo( COMPONENT_INFO );
        componentParam.setNonEditable();

        // the stdDevType Parameter
        StringConstraint stdDevTypeConstraint = new StringConstraint();
        stdDevTypeConstraint.addString( STD_DEV_TYPE_TOTAL );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_INTER );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_INTRA );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_NONE );
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_DEFAULT );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();

        // add these to the list
        otherParams.addParameter( componentParam );
        otherParams.addParameter( stdDevTypeParam );

    }


    /**
     *  This creates the hashtable of coefficients for the supported
     *  intensityMeasures (im).  The key is the im parameter name, plus the
     *  period value for SA (separated by "/").  For example, the key for SA
     *  at 1.0 second period is "SA/1.0".
     */
    protected void initCoefficients(  ) {

        String S = C + ": initCoefficients():";
        if ( D ) System.out.println( S + "Starting" );

        coefficients.clear();

        // PGA
        Field_2000_AttenRelCoefficients coeff = new Field_2000_AttenRelCoefficients(PGA_NAME,
              0.0, 0.853, 0.872, 0.442, -0.067, -0.960, -0.154, 8.90, 0.067, -0.14, -0.1, 0.87, 0.23 );

        // SA/0.00
        Field_2000_AttenRelCoefficients coeff0 = new Field_2000_AttenRelCoefficients( SA_NAME + '/' +( new Double( "0.0" ) ).doubleValue() ,
              0.0, 0.853, 0.872, 0.442, -0.067, -0.960, -0.154, 8.90, 0.067, -0.14, -0.1, 0.87, 0.23 );
        // SA/0.3
        Field_2000_AttenRelCoefficients coeff1 = new Field_2000_AttenRelCoefficients( "SA/" +( new Double( "0.3" ) ).doubleValue() ,
              0.3, 0.995, 1.096, 0.501, -0.112, -0.841, -0.350, 7.20, 0.057, -0.12, -0.11, 0.99, 0.26 );
        // SA/1.0
        Field_2000_AttenRelCoefficients coeff2 = new Field_2000_AttenRelCoefficients( "SA/" +( new Double( "1.0" ) ).doubleValue() ,
              1.0, -0.164, -0.267, 0.903, 0.0, -0.914, -0.704, 6.20, 0.12, -0.25, -0.1, 0.95, 0.22 );
        // SA/3.0
        Field_2000_AttenRelCoefficients coeff3 = new Field_2000_AttenRelCoefficients( "SA/" +( new Double( "3.0" ) ).doubleValue() ,
              3.0, -2.267, -2.681, 1.083, 0.0, -0.720, -0.674, 3.00, 0.11, -0.18, 0.14, -0.66, 0.3 );

        coefficients.put( coeff.getName(), coeff );
        coefficients.put( coeff0.getName(), coeff0 );
        coefficients.put( coeff1.getName(), coeff1 );
        coefficients.put( coeff2.getName(), coeff2 );
        coefficients.put( coeff3.getName(), coeff3 );

    }


    /**
     *  <b>Title:</b> Field_2000_AttenRelCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the Field_2000_AttenRel.  One instance of this class holds the set of
     *  coefficients for each period (one row of their table 8).<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W Rock
     * @created    February 27, 2002
     * @version    1.0
     */

    class Field_2000_AttenRelCoefficients
             implements NamedObjectAPI {


        protected final static String C = "Field_2000_AttenRelCoefficients";
        protected final static boolean D = false;

        protected String name;
        protected double period = -1;
        protected double b1ss;
        protected double b1rv;
        protected double b2;
        protected double b3;
        protected double b5;
        protected double bv;
        protected double h;
        protected double bdSlope;
        protected double bdIntercept;
        protected double intra_slope;
        protected double intra_intercept;
        protected double tau;

        /**
         *  Constructor for the Field_2000_AttenRelCoefficients object
         *
         * @param  name  Description of the Parameter
         */
        public Field_2000_AttenRelCoefficients( String name ) { this.name = name; }

        /**
         *  Constructor for the Field_2000_AttenRelCoefficients object that sets all values at once
         *
         * @param  name  Description of the Parameter
         */
        public Field_2000_AttenRelCoefficients( String name,  double period,
            double b1ss,  double b1rv,  double b2,       double b3,  double b5,
            double bv,    double h,     double bdSlope,  double bdIntercept,
            double intra_slope, double intra_intercept,  double tau )
        {
            this.name = name;       this.period = period;   this.b1ss = b1ss;
            this.b1rv = b1rv;       this.b2 = b2;           this.b3 = b3;
            this.b5 = b5;           this.bv = bv;           this.h = h;
            this.bdSlope = bdSlope; this.bdIntercept = bdIntercept;
            this.intra_slope = intra_slope;
            this.intra_intercept = intra_intercept;         this.tau = tau;
        }

        /**
         *  Gets the name attribute of the Field_2000_AttenRelCoefficients object
         *
         * @return    The name value
         */
        public String getName() {
            return name;
        }

        /**
         *  Debugging - prints out all cefficient names and values
         *
         * @return    Description of the Return Value
         */
        public String toString() {

            StringBuffer b = new StringBuffer();
            b.append( C );
            b.append( "\n  Period = " + period );
            b.append( "\n  b1ss = " + b1ss );       b.append( "\n  b1rv = " + b1rv );
            b.append( "\n  b2 = " + b2 );           b.append( "\n  b3 = " + b3 );
            b.append( "\n  b5 = " + b5 );           b.append( "\n  bv = " + bv );
            b.append( "\n  h = " + h );
            b.append( "\n  bdSlope = " + bdSlope );
            b.append( "\n  bdIntercept = " + bdIntercept );
            b.append( "\n  intra_slope = " + intra_slope );
            b.append( "\n  intra_intercept = " + intra_intercept );
            b.append( "\n  tau = " + tau );
            return b.toString();
        }
    }
}



