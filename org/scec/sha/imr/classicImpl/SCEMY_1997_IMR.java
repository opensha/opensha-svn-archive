package org.scec.sha.imr.classicImpl;

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
 * <b>Title:</b> SCEMY_1997_IMR<p>
 * <b>Description:</b> This implements the classicIMR (attenuation relationship)
 * developed by Sadigh et al. (1997, Seismological Research Letters, vol
 * 68, num 1, pp 180-189) <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <LI>siteTypeParam - "Rock" versus "Deep-Soil"
 * <LI>fltTypeParam - Style of faulting ("Reverse" or "Other")
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation (only one)
 * </UL><p>
 *
 *
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Edward (Ned) Field
 * @created    February 27, 2002
 * @version    1.0
 */


public class SCEMY_1997_IMR
         extends ClassicIMR
         implements
        ClassicIMRAPI,
        NamedObjectAPI {


    private final static String C = "SCEMY_1997_IMR";
    private final static boolean D = false;
    private final static String NAME = "Sadigh et al (1997)";

    // style of faulting options
    private final static String FLT_TYPE_DEFAULT = "Other";
    private final static String FLT_TYPE_OTHER = "Other";
    private final static String FLT_TYPE_REVERSE = "Reverse";

    /**
     * Site Type Parameter ("Rock/Shallow-Soil" versus "Deep-Soil")
     */
     private StringParameter siteTypeParam = null;
     private final static String SITE_TYPE_NAME = "SCEMY Site Type";
     // no units
     private final static String SITE_TYPE_INFO = "Geological conditions as the site";
     private final static String SITE_TYPE_ROCK =  "Rock";
     private final static String SITE_TYPE_SOIL =  "Deep-Soil";
     private final static String SITE_TYPE_DEFAULT =  "Deep-Soil";

     // warning constraints:
    protected final static Double MAG_WARN_MIN = new Double(4);
    protected final static Double MAG_WARN_MAX = new Double(8.25);
    protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
    protected final static Double DISTANCE_RUP_WARN_MAX = new Double(100.0);


    /**
     * The DistanceRupParameter, closest distance to fault surface.
     */
    private DistanceRupParameter distanceRupParam = null;
    private final static Double DISTANCE_RUP_DEFAULT = new Double( 0 );

    /**
     * The current set of coefficients based on the selected intensityMeasure
     */
    private SCEMY_1997_IMRCoefficients coeff = null;

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
     * "Reverse" if the rake is > 45 and < 135; "Other" otherwise.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( double rake )
        throws InvalidRangeException
    {
        FaultUtils.assertValidRake( rake );
        if( rake > 45 && rake < 135)  return FLT_TYPE_REVERSE;
        else return FLT_TYPE_OTHER;
    }

    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam.
     *
     * @param rake                      Input determines the fault type
     * @return                          Fault Type, either Strike-Slip,
     * Reverse, or Unknown if the rake is <= 30 degrees or within 30 degrees of 180,
     * between 30 and 150 degrees, or not one of these two cases, respectivly.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( Double rake )
        throws InvalidRangeException
    {
        if ( rake == null ) return FLT_TYPE_DEFAULT;
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

        // constraints get checked
        magParam.setValue( probEqkRupture.getMag() );

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
     *  This sets the site-related parameter (siteTypeParam) based on what is in
     *  the Site object passed in (the Site object must have a parameter with
     *  the same name as that in siteTypeParam).  This also sets the internally held
     *  Site object as that passed in.
     *
     * @param  site             The new site value which contains a Vs30 Parameter
     * @throws ParameterException Thrown if the Site object doesn't contain a
     * Vs30 parameter
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


        // This will throw a parameter exception if the Vs30Param doesn't exist
        // in the Site object
        ParameterAPI siteType = site.getParameter( SITE_TYPE_NAME );

        // This may throw a constraint exception
        this.siteTypeParam.setValue( siteType.getValue() );

        // Now pass function up to super to set the site
        // Why not just say "this.Site = site" ?? (Ned)

        super.setSite( site );

        // Calculate the PropagationEffectParameters; this is
        // not efficient if both the site and probEqkRupture
        // are set before getting the mean, stdDev, or ExceedProbability
        setPropagationEffectParams();

    }

    /**
     * This calculates the distanceRupParam value based on the current
     * site and probEqkRupture. <P>
     */
    protected void setPropagationEffectParams(){

        if( ( this.site != null ) && ( this.probEqkRupture != null ) )
            distanceRupParam.getValue( probEqkRupture, site );

    }

    /**
     * This function determines which set of coefficients in the HashMap
     * are to be used given the current intensityMeasure (im) Parameter. The
     * lookup is done keyed on the name of the im, plus the period value if
     * im.getName() == "SA" (seperated by "/").
     */
    protected void updateCoefficients() throws ParameterException {

        // Check that parameter exists
        if( im == null ) throw new ParameterException( C +
            ": updateCoefficients(): " +
            "The Intensity Measusre Parameter has not been set yet, unable to process."
        );

        StringBuffer key = new StringBuffer( im.getName() );
        if( im.getName() == SA_NAME ) key.append( "/" + periodParam.getValue() );
        if( coefficients.containsKey( key.toString() ) ) coeff = ( SCEMY_1997_IMRCoefficients )coefficients.get( key.toString() );
        else throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public SCEMY_1997_IMR(ParameterChangeWarningListener warningListener) {

        this.warningListener = warningListener;
        initCoefficients( );  // This must be called before the next one
        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initMeanIndependentParamsList(); // These last two must be called
        initStdDevIndependentParamsList();  // after the above four
    }



    /**
     * Calculates the mean of the exceedence probability distribution <p>
     *
     * @return    The mean value
     */
    public Double getMean() throws IMRException{

        double mag, dist;
        String fltType, siteType, component;

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
            dist = ((Double)distanceRupParam.getValue()).doubleValue();
            fltType = fltTypeParam.getValue().toString();
            siteType = siteTypeParam.getValue().toString();
            component = componentParam.getValue().toString();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }

        // the following is inefficient if the im Parameter has not been changed in any way
        updateCoefficients();


      // Coefficients that do not depend on intensity measure:
      // NOTE: HARD CODE THESE IN WHEN I KNOW IT'S WORKING
      double c2_rlt =1.0;       // c2 for rock, mag\uFFFD6.5
      double c2_rgt = 1.1;      // c2 for rock, mag>6.5
      double c5_rlt = 1.29649;
      double c5_rgt = -0.48451;
      double c6_rlt = 0.250;
      double c6_rgt = 0.524;

      double c1_s_ss = -2.17;   // c1 for soil, strike-slip (and normal)
      double c1_s_rv = -1.92;   // c1 for soil, reverse faulting
      double c2_s = 1.0;
      double c3_s = 1.7;
      double c4_slt = 2.1863;   // soil, mag\uFFFD6.5
      double c4_sgt = 0.3825;   // soil, mag>6.5
      double c5_slt = 0.32;
      double c5_sgt = 0.5882;

      double mean;

        // if Site Type is Rock:
        if ( siteType.equals( SITE_TYPE_ROCK ) ) {
            if (mag <= 6.5 ) {
                mean = coeff.c1_rlt + c2_rlt*mag + coeff.c3*( Math.pow( ( 8.5 - mag), 2 ) ) +
                      coeff.c4 * ( Math.log( dist + Math.exp( c5_rlt + c6_rlt * mag ) ) ) +
                      coeff.c7_r * ( Math.log( dist + 2 ) );
            }
            else {
                mean = coeff.c1_rgt + c2_rgt*mag + coeff.c3*( Math.pow( ( 8.5 - mag), 2 ) ) +
                      coeff.c4 * ( Math.log( dist + Math.exp( c5_rgt + c6_rgt * mag ) ) ) +
                      coeff.c7_r * ( Math.log( dist + 2 ) );
            }
            // apply 1.2 factor for reverse faults (ln(1.2)=0.1823)
            if (fltType.equals( FLT_TYPE_REVERSE )) mean = mean + 0.1823;

        // if Site Type is Deep Soil
        } else {
            if (mag <= 6.5 ) {
                mean = c2_s * mag - c3_s * Math.log( dist + c4_slt * Math.exp( c5_slt * mag ) ) +
                      coeff.c7_s * Math.pow( 8.5 - mag, 2.5 );
            }
            else {
                mean = c2_s * mag - c3_s * Math.log( dist + c4_sgt * Math.exp( c5_sgt * mag ) ) +
                      coeff.c7_s * Math.pow( 8.5 - mag, 2.5 );
            }
            // apply fault-type dependent terms:
            if (fltType.equals( FLT_TYPE_REVERSE ))
                mean += c1_s_rv + coeff.c6_s_rv;
            else
                mean += c1_s_ss + coeff.c6_s_ss;
        }

        // Convert back to normal value
        mean = Math.pow(Math.E, mean);

        // return the result
        return new Double(mean);
    }


    /**
     * @return    The stdDev value
     */
    public Double getStdDev() throws IMRException {

        String siteType;
        double mag;

        try{
            siteType = siteTypeParam.getValue().toString();
            mag = ((Double)magParam.getValue()).doubleValue();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }

        // this is inefficient if the im has not been changed in any way
        updateCoefficients();

        if ( siteType.equals( SITE_TYPE_ROCK ) ) {
            if ( mag <= 7.21 )
                return new Double ( coeff.sigma_ri - mag * 0.14 );
            else
                return new Double ( coeff.sigma_ri - 1.01 ); // 1.01=7.21*0.14
        }
        else {
            if ( mag <= 7.0 )
                return new Double ( coeff.sigma_si - mag * 0.16 );
            else
                return new Double ( coeff.sigma_si - 1.12 ); // 1.12=7.0*0.16
        }

//        return  new Double( coeff.sigmaLnY );
    }


    public void setParamDefaults(){

        siteTypeParam.setValue( SITE_TYPE_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_DEFAULT );
        distanceRupParam.setValue( DISTANCE_RUP_DEFAULT );
        saParam.setValue( SA_DEFAULT );
        periodParam.setValue( PERIOD_DEFAULT );
        dampingParam.setValue(DAMPING_DEFAULT);
        pgaParam.setValue( PGA_DEFAULT );
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );

    }

    /**
     * This creates the list of paramters that the Mean depends upon
     * NOTE: This doesn not include the intensity-measure parameters
     * or any of thier internal independentParamaters
     */
    protected void initMeanIndependentParamsList(){
        meanIndependentParams.clear();
        meanIndependentParams.clear();
        meanIndependentParams.addParameter( siteTypeParam );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( distanceRupParam );
        meanIndependentParams.addParameter( componentParam );
     }

    /**
     * This creates the list of paramters that StdDev depends upon
     * NOTE: This doesn not include the intensity-measure parameters
     * or any of thier internal independentParamaters
     */
    protected void initStdDevIndependentParamsList(){
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        stdDevIndependentParams.addParameter( siteTypeParam );
    }


    /**
     *  Creates the site-type parameter and adds it to the siteParams list.
     *  Makes the parameters noneditable.
     */
    /**
     *  Creates the Site-Type parameter and adds it to the siteParams list.
     *  Makes the parameters noneditable.
     */
    protected void initSiteParams( ) {

        StringConstraint siteConstraint = new StringConstraint();
        siteConstraint.addString( SITE_TYPE_ROCK );
        siteConstraint.addString( SITE_TYPE_SOIL );
        siteConstraint.setNonEditable();
        siteTypeParam = new StringParameter( SITE_TYPE_NAME, siteConstraint, null);
        siteTypeParam.setInfo( SITE_TYPE_INFO );
        siteTypeParam.setNonEditable();

        siteParams.clear();
        siteParams.addParameter( siteTypeParam );

    }


    /**
     *  Creates the two Potential Earthquake parameters (magParam and
     *  fltTypeParam) and adds them to the probEqkRuptureParams
     *  list. Makes the parameters noneditable.
     */
    protected void initProbEqkRuptureParams(  ) {

        // create magParam
        super.initProbEqkRuptureParams();

        //  Create and add warning constraint to magParam:
        DoubleConstraint warn = new DoubleConstraint(MAG_WARN_MIN, MAG_WARN_MAX);
        warn.setNonEditable();
        magParam.setWarningConstraint(warn);
        magParam.addParameterChangeWarningListener( warningListener );
        magParam.setNonEditable();

        // Create fault-type parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( FLT_TYPE_REVERSE );
        constraint.addString( FLT_TYPE_OTHER );
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
     *  propagationEffectParams list.
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
            SCEMY_1997_IMRCoefficients coeff = ( SCEMY_1997_IMRCoefficients ) coefficients.get( keys.nextElement() );
            if ( coeff.period >= 0 )  set.add( new Double( coeff.period ) );
        }
        Iterator it = set.iterator();
        while ( it.hasNext() ) periodConstraint.addDouble( ( Double ) it.next() );
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
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_DEFAULT );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();
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
        SCEMY_1997_IMRCoefficients coeff = new SCEMY_1997_IMRCoefficients(PGA_NAME,
              0., -0.624, -1.274, 0.000, -2.100, 0.0, 0.0, 0.0, 0.0, 1.39, 1.52 );
        // SA/0.0
        SCEMY_1997_IMRCoefficients coeff0 = new SCEMY_1997_IMRCoefficients( SA_NAME + '/' +( new Double( "0.0" ) ).doubleValue() ,
              0.0, -0.624, -1.274, 0.000, -2.100, 0.0, 0.0, 0.0, 0.0, 1.39, 1.52);
        // SA/0.075
        SCEMY_1997_IMRCoefficients coeff1 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.075" ) ).doubleValue() ,
              0.075, 0.110, -0.540, 0.006, -2.128, -0.082, 0.4572, 0.4572, 0.005, 1.40, 1.54);
        // SA/0.1
        SCEMY_1997_IMRCoefficients coeff2 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.1" ) ).doubleValue() ,
              0.1, 0.275, -0.375, 0.006, -2.148, -0.041, 0.6395, 0.6395, 0.005, 1.41, 1.54);
        // SA/0.2
        SCEMY_1997_IMRCoefficients coeff3 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.2" ) ).doubleValue() ,
              0.2, 0.153, -0.497, -0.004, -2.080, 0.0, 0.9187, 0.9187, -0.004, 1.43, 1.565);
        // SA/0.3
        SCEMY_1997_IMRCoefficients coeff4 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.3" ) ).doubleValue() ,
              0.3, -0.057, -0.707, -0.017, -2.028, 0.0, 0.9547, 0.9547, -0.014, 1.45, 1.58);
        // SA/0.4
        SCEMY_1997_IMRCoefficients coeff5 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.4" ) ).doubleValue() ,
              0.4, -0.298, -0.948, -0.028, -1.990, 0.0, 0.9251, 0.9005, -0.024, 1.48, 1.595);
        // SA/0.5
        SCEMY_1997_IMRCoefficients coeff6 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.5" ) ).doubleValue() ,
              0.5, -0.588, -1.238, -0.040, -1.945, 0.0, 0.8494, 0.8285, -0.033, 1.50, 1.61);
        // SA/0.75
        SCEMY_1997_IMRCoefficients coeff7 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "0.75" ) ).doubleValue() ,
              0.75, -1.208, -1.858, -0.050, -1.865, 0.0, 0.7010, 0.6802, -0.051, 1.52, 1.635);
        // SA/1.0
        SCEMY_1997_IMRCoefficients coeff8 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "1.0" ) ).doubleValue() ,
              1.0, -1.705, -2.355, -0.055, -1.800, 0.0, 0.5665, 0.5075, -0.065, 1.53, 1.66);
        // SA/1.5
        SCEMY_1997_IMRCoefficients coeff9 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "1.5" ) ).doubleValue() ,
              1.5, -2.407, -3.057, -0.065, -1.725, 0.0, 0.3235, 0.2215, -0.090, 1.53, 1.69);
        // SA/2.0
        SCEMY_1997_IMRCoefficients coeff10 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "2.0" ) ).doubleValue() ,
              2.0, -2.945, -3.595, -0.070, -1.670, 0.0, 0.1001, -0.0526, -0.108, 1.53, 1.70);
        // SA/3.0
        SCEMY_1997_IMRCoefficients coeff11 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "3.0" ) ).doubleValue() ,
              3.0, -3.700, -4.350, -0.080, -1.610, 0.0, -0.2801, -0.4905, -0.139, 1.53, 1.71);
        // SA/4.0
        SCEMY_1997_IMRCoefficients coeff12 = new SCEMY_1997_IMRCoefficients( "SA/" +( new Double( "4.0" ) ).doubleValue() ,
              4.0, -4.230, -4.880, -0.100, -1.570, 0.0, -0.6274, -0.8907, -0.160, 1.53, 1.71);

        coefficients.put( coeff.getName(), coeff );
        coefficients.put( coeff0.getName(), coeff0 );
        coefficients.put( coeff1.getName(), coeff1 );
        coefficients.put( coeff2.getName(), coeff2 );
        coefficients.put( coeff3.getName(), coeff3 );
        coefficients.put( coeff4.getName(), coeff4 );
        coefficients.put( coeff5.getName(), coeff5 );
        coefficients.put( coeff6.getName(), coeff6 );
        coefficients.put( coeff7.getName(), coeff7 );
        coefficients.put( coeff8.getName(), coeff8 );
        coefficients.put( coeff9.getName(), coeff9 );
        coefficients.put( coeff10.getName(), coeff10 );
        coefficients.put( coeff11.getName(), coeff11 );
        coefficients.put( coeff12.getName(), coeff12 );
    }


    /**
     *  <b>Title:</b> SCEMY_1997_IMRCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the SCEMY_1997_IMR.  One instance of this class holds the set of
     *  coefficients for each period (one row of their table 8).<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W Rock
     * @created    February 27, 2002
     * @version    1.0
     */

    class SCEMY_1997_IMRCoefficients
             implements NamedObjectAPI {

        /*     Coefficient Naming Convention:
        \uFFFDrlt\uFFFD = rock, less than 6.5
        \uFFFDrgt\uFFFD = rock, greater than 6.5
        "s_ss" = soil, strike slip
        "s_rv" = soil, reverse
        "sigma_ri" = slope for rock intercept
        "sigma_si" = slope for soil intercept
        */

        protected final static String C = "SCEMY_1997_IMRCoefficients";
        protected final static boolean D = true;

        protected String name;
        protected double period = -1;
        protected double c1_rlt;
        protected double c1_rgt;
        protected double c3;
        protected double c4;
        protected double c7_r;
        protected double c6_s_ss;
        protected double c6_s_rv;
        protected double c7_s;
        protected double sigma_ri;
        protected double sigma_si;


        /**
         *  Constructor for the SCEMY_1997_IMRCoefficients object
         *
         * @param  name  Description of the Parameter
         */
        public SCEMY_1997_IMRCoefficients( String name ) { this.name = name; }

        /**
         *  Constructor for the SCEMY_1997_IMRCoefficients object that sets all values at once
         *
         * @param  name  Description of the Parameter
         */
        public SCEMY_1997_IMRCoefficients( String name,  double period,
            double c1_rlt,  double c1_rgt,  double c3, double c4,  double c7_r,
            double c6_s_ss,  double c6_s_rv,  double c7_s,  double sigma_ri,
            double sigma_si )
        {
            this.name = name;
            this.period = period;   this.c1_rlt = c1_rlt;
            this.c1_rgt = c1_rgt;   this.c3 = c3;
            this.c4 = c4;           this.c7_r = c7_r;
            this.c6_s_ss = c6_s_ss; this.c6_s_rv = c6_s_rv;
            this.c7_s = c7_s;       this.sigma_ri = sigma_ri;
            this.sigma_si = sigma_si;
        }

        /**
         *  Gets the name attribute of the SCEMY_1997_IMRCoefficients object
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
            b.append( "\n  Period = " + period );   b.append( "\n  c1_rlt = " + c1_rlt );
            b.append( "\n  c1_rgt = " + c1_rgt );   b.append( "\n  c3 = " + c3 );
            b.append( "\n  c4 = " + c4 );           b.append( "\n  c7_r = " + c7_r );
            b.append( "\n  c6_s_ss = " + c6_s_ss ); b.append( "\n  c6_s_rv = " + c6_s_rv );
            b.append( "\n c7_s = " + c7_s );        b.append( "\n  sigma_ri = " + sigma_ri );
            b.append( "\n  sigma_si = " + sigma_si );
            return b.toString();
        }
    }
}



