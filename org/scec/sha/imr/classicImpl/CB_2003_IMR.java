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
 * <b>Title:</b> CB_2003_IMR<p>
 * <b>Description:</b> This implements the classicIMR (attenuation relationship)
 * developed by Campbell & Bozorgnia (2003), Bull. Seism. Soc. Am., vol 93
 * , In press)<p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceSeisParam - closest distance to seismogenic part of fault
 * <LI>siteTypeParam - "Firm Soil", "Very Firm Soil", "Soft Rock", "Firm Rock", "Generic Soil", "Generic Rock", or "NEHRP BC"
 * <LI>fltTypeParam - Style of faulting
 * <LI>hangingWallParam - Hanging wall parameter
 * <LI>componentParam - Component of shaking
 * <LI>stdDevTypeParam - The type of standard deviation (mag or PGA dependent)
 * </UL><p>
 *
 * NOTE: The mean calculation for the site class "BC Boundary" is only approximate at
 * 0.05- and 0.075-second periods (because "bv" is not available from BJF_1997 at those
 * periods, so values were linearly interpolated between 0 and 0.1 seconds) and at 3-
 * and 4-seconds ("bv" not available from BJF_1997 above 2 seconds, so value at 2-seconds
 * was applied).
 *
 *
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Edward H. Field
 * @created    February 27, 2002
 * @version    1.0
 */


public class CB_2003_IMR
         extends ClassicIMR
         implements
        ClassicIMRAPI,
        NamedObjectAPI {


    private final static String C = "CB_2003_IMR";
    private final static boolean D = false;
    private final static String NAME = "Campbell and Bozorgnia (2003)";

    // style of faulting options
    private final static String FLT_TYPE_THRUST = "Thrust (dip<45)";
    private final static String FLT_TYPE_REVERSE = "Reverse (dip>45)";
    private final static String FLT_TYPE_OTHER = "Strike Slip or Normal";
    private final static String FLT_TYPE_UNKNOWN = "Unknown";
    private final static String FLT_TYPE_DEFAULT = FLT_TYPE_OTHER;

    /**
     * Site Type Parameter
     */
     private StringParameter siteTypeParam = null;
     private final static String SITE_TYPE_NAME = "Campbell-2003 Site Type";
     // no units
     private final static String SITE_TYPE_INFO = "Geological conditions as the site";
     private final static String SITE_TYPE_FIRM_SOIL =  "Firm Soil";
     private final static String SITE_TYPE_VERY_FIRM_SOIL =  "Very Firm Soil";
     private final static String SITE_TYPE_SOFT_ROCK =  "Soft Rock";
     private final static String SITE_TYPE_FIRM_ROCK =  "Firm Rock";
     private final static String SITE_TYPE_SOIL =  "Generic Soil";
     private final static String SITE_TYPE_ROCK =  "Generic Rock";
     private final static String SITE_TYPE_NEHRP_BC =  "BC Boundary";
     private final static String SITE_TYPE_DEFAULT =  "Firm Soil";

    // warning constraints:
    protected final static Double MAG_WARN_MIN = new Double(5);
    protected final static Double MAG_WARN_MAX = new Double(8);
    protected final static Double DISTANCE_SEIS_WARN_MIN = new Double(3.0);
    protected final static Double DISTANCE_SEIS_WARN_MAX = new Double(60.0);
    // the minimum warning will get overridden by seisDepth is less than seisDepth

    // types of standard deviation
    protected final static String STD_DEV_TYPE_MAG_DEP = "Total (Mag Dependent)";
    protected final static String STD_DEV_TYPE_PGA_DEP = "Total (PGA Dependent)";
    protected final static String STD_DEV_TYPE_DEFAULT = "Total (Mag Dependent)";


    /**
     * The DistanceSeisParameter, which is the closest distance to the seimogenic
     * part of the fault.
     */
    private DistanceSeisParameter distanceSeisParam = null;
    private final static Double DISTANCE_SEIS_DEFAULT = new Double( 3 );


    /**
     * Hanging-Wall Parameter, defined as 1 above the surface projection, tapering
     * linearly to 0.0 at 5 km from surface projection.
     */
    protected DoubleParameter hangingWallParam = null;
    protected final static String HANGING_WALL_NAME = "Hanging Wall Param.";
    // no units
    protected final static String HANGING_WALL_INFO = "1.0 if site is within surface projection of rupture, tapering (linearly) to 0.0 at 5 km beyond";
    protected final static Double HANGING_WALL_MIN = new Double(0);
    protected final static Double HANGING_WALL_MAX = new Double(1);
    protected final static Double HANGING_WALL_DEFAULT = new Double(0.0);


    /**
     * Joyner-Boore Distance parameter, used to compute the hanging-wall
     * parameter from a site and probEqkRupture.
     */
    private DistanceJBParameter distanceJBParam = null;
    // No waring constraint needed for this

    /**
     * The current set of coefficients based on the selected intensityMeasure
     */
    private CB_2003_IMRCoefficients coeff = null;

    /**
     *  Hashtables of coefficients for the supported intensityMeasures
     */
    protected Hashtable horzCoefficients = new Hashtable();
    protected Hashtable vertCoefficients = new Hashtable();

    // for issuing warnings:
    ParameterChangeWarningListener warningListener = null;


    /**
     * Determines the style of faulting from the rake & dip angles (which
     * come from the probEqkRupture object).
     *
     * @param rake                      ave. rake of rupture (degrees)
     * @param dip                       ave. dip (degrees)
     * @return                          Fault-Type String, Thrust if
     * 22.5<rake<=67.5  & dip<45 degrees, Reverse if 22.5<rake<=67.5  & dip>=45
     * degrees, and "Other" if the rake is any other value.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultType( double rake, double dip )
        throws InvalidRangeException
    {

        // note that "Uknown" is not an option as implemented here
        FaultUtils.assertValidRake( rake );
        FaultUtils.assertValidDip( dip );

        if( rake >= 22.5 && rake <= 157.5 )
            if( dip >= 45)  return FLT_TYPE_REVERSE;
            else            return FLT_TYPE_THRUST;
        else                return FLT_TYPE_OTHER;
    }

    /**
     * Determines the style of faulting from the rake & dip angles (which
     * come from the probEqkRupture object).
     *
     * @param rake                      ave. rake of rupture (degrees)
     * @param dip                       ave. dip (degrees)
     * @return                          Fault-Type String, Thrust if
     * 22.5<rake<=67.5  & dip<45 degrees, Reverse if 22.5<rake<=67.5  & dip>=45
     * degrees, and "Other" if the rake is any other value.
     * @throws InvalidRangeException    If not valid rake angle
     */
     protected static String determineFaultType( Double rake, Double dip )
        throws InvalidRangeException
    {
        return determineFaultType( rake.doubleValue(), dip.doubleValue() );
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
            String fltTypeStr = determineFaultType( probEqkRupture.getAveRake(), probEqkRupture.getRuptureSurface().getAveDip() );
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
     *  This sets the site-type and basin-depth parameters based on what is in
     *  the Site object passed in (the Site object must have these parameters in it).
     *  This also sets the internally held Site object as that passed in.
     *
     * @param  site             The new site object which contains the
     * "Campbell Site Type" and "Campbell Basin Depth" Parameters
     * @throws ParameterException Thrown if the Site object doesn't contain
     * either of these parameters.
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


        // This will throw a parameter exception if the Parameter doesn't exist
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
     * This sets the two propagation-effect parameters based on the current
     * site and probEqkRupture.
     */
    protected void setPropagationEffectParams(){

        if( ( this.site != null ) && ( this.probEqkRupture != null ) ){
          try {
            distanceSeisParam.setValue( probEqkRupture, site );
          }catch (WarningException e){
            if(D) System.out.println(C+"Warning Exception:"+e);
          }

          /* There is a problem that this term will apply to vertical strike-slip faults
             when on the fault trace.  The 80-degree dip threshold was the solution
             recommended by Ken Cambell over the phone today, he will confirm this with
             Norm next week (Ned, 9-20-02).
          */

            if(probEqkRupture.getRuptureSurface().getAveDip() < 80) {
                double jbDist = ( (Double) distanceJBParam.getValue( probEqkRupture, site ) ).doubleValue();
                if ( jbDist < 1.0 )
                    hangingWallParam.setValue(1.0);
                else if  ( jbDist < 5.0 )
                    hangingWallParam.setValue((5.0-jbDist)/5.0);
                else
                    hangingWallParam.setValue(0.0);
            }
            else // turn it off for vertically dipping faults
                hangingWallParam.setValue(0.0);
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

        // Set coefficients depending on component:
        if ( componentParam.equals( COMPONENT_AVE_HORZ ) ) {
            if( horzCoefficients.containsKey( key.toString() ) )
                coeff = ( CB_2003_IMRCoefficients ) horzCoefficients.get( key.toString() );
            else
                throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
        }
        else { // vertical component
            if( vertCoefficients.containsKey( key.toString() ) )
                coeff = ( CB_2003_IMRCoefficients ) vertCoefficients.get( key.toString() );
            else
                throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
        }
    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public CB_2003_IMR(ParameterChangeWarningListener warningListener) {

        super();

        this.warningListener = warningListener;

        initCoefficients();  // These must be called before the next one
        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initMeanIndependentParamsList(); // These last two must be called
        initStdDevIndependentParamsList();  // after the above four
        initExceedProbIndependentParamsList();
    }



    /**
     * Calculates the mean of the exceedence probability distribution. <p>
     * @return    The mean value
     */
    public double getMean() throws IMRException{

        updateCoefficients();
        return calcMean();
    }

    /**
     * This calulates the mean assuming the coefficients have already been assigned
     * (this way PGA can be computed in getStdDev() without having to override the
     * current im).
     * @return
     * @throws IMRException
     */
    protected double calcMean() throws IMRException {

        double mag, dist, hw;
        String fltType, siteType;

        // default is alluvium
        double S_vfs=0, S_sr=0, S_fr=0; // default is "Firm Soil""
        double F_rv, F_th;
        double f1, f2, g, f3, f4, f5, f_HW_m, f_HW_r;

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
            dist = ((Double)distanceSeisParam.getValue()).doubleValue();
            hw = ((Double)hangingWallParam.getValue()).doubleValue();
            fltType = fltTypeParam.getValue().toString();
            siteType = siteTypeParam.getValue().toString();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }


        // Set fault type
        if ( fltType.equals( FLT_TYPE_REVERSE ) ) {
            F_rv = 1;
            F_th = 0;
        }
        else if ( fltType.equals( FLT_TYPE_THRUST ) ) {
           F_rv = 0;
           F_th = 1;
        }
        else if ( fltType.equals( FLT_TYPE_OTHER ) ) {
           F_rv = 0;
           F_th = 0;
        }
        else { // must be "Unknown"
           F_rv = 0.25;
           F_th = 0.25;
        }


        // Set the site parameters (if defaults not correct)
        if ( siteType.equals( SITE_TYPE_VERY_FIRM_SOIL ) )    S_vfs = 1.0;
        else if ( siteType.equals( SITE_TYPE_SOFT_ROCK ) )    S_sr = 1.0;
        else if ( siteType.equals( SITE_TYPE_FIRM_ROCK ) )    S_fr = 1.0;
        else if ( siteType.equals( SITE_TYPE_SOIL ) )         S_vfs = 0.25;
        else if ( siteType.equals( SITE_TYPE_ROCK ) ){
            S_fr = 0.5;
            S_sr = 0.5;
        }
        else if ( siteType.equals( SITE_TYPE_NEHRP_BC ) ){
            S_fr = 0.5;
            S_sr = 0.5;
        }


        f1 = coeff.c2*mag + coeff.c3*Math.pow(8.5-mag,2);

        g = coeff.c5 + coeff.c6*(S_vfs+S_sr) + coeff.c7*S_fr;
        double temp = Math.exp(coeff.c8*mag + coeff.c9*(8.5-mag)*(8.5-mag));
        f2 = dist*dist + g*g*temp*temp;

        f3 = coeff.c10*F_rv + coeff.c11*F_th;

        f4 = coeff.c12*S_vfs + coeff.c13*S_sr + coeff.c14*S_fr;

        if      (mag < 5.5)               f_HW_m = 0;
        else if (mag >= 5.5 && mag < 6.5) f_HW_m = mag-5.5;
        else                              f_HW_m = 1;

        if (dist < 8.0) f_HW_r = coeff.c15*(dist/8);
        else            f_HW_r = coeff.c15;

        f5 = hw*(S_vfs+S_sr+S_fr)*f_HW_m*f_HW_r;

        // Make BC Boundary correction if needed
        if ( siteType.equals( SITE_TYPE_NEHRP_BC )) f1 += coeff.bv*Math.log(620/760);

        return coeff.c1 + f1 + coeff.c4*0.5*Math.log(f2) + f3 + f4 + f5;

    }



    /**
     * @return    The stdDev value
     */
    public double getStdDev() throws IMRException {

        double mag;
        String stdevType = stdDevTypeParam.getValue().toString();

        if ( stdevType.equals( STD_DEV_TYPE_NONE ) )
            return 0;
        else if ( stdevType.equals( STD_DEV_TYPE_MAG_DEP ) ) {
            try{
                mag = ((Double)magParam.getValue()).doubleValue();
            }
            catch(NullPointerException e){
               throw new IMRException(C + ": getMean(): " + ERR);
            }

            updateCoefficients();
            if (mag < 7.4) return coeff.c16-0.07*mag;
            else           return coeff.c16-0.518;
        }
        else {  // PGA dependent

            // Set PGA coefficients depending on component:
            if ( componentParam.equals( COMPONENT_AVE_HORZ ) ) {
                if( horzCoefficients.containsKey( PGA_NAME ) )
                    coeff = ( CB_2003_IMRCoefficients ) horzCoefficients.get( PGA_NAME );
                else
                    throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + PGA_NAME );
            }
            else { // vertical component
                if( vertCoefficients.containsKey( PGA_NAME ) )
                    coeff = ( CB_2003_IMRCoefficients ) vertCoefficients.get( PGA_NAME );
                else
                    throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + PGA_NAME );
            }

            double pga = Math.exp(calcMean());
            updateCoefficients();
            if (pga <= 0.07)                    return coeff.c17+0.351;
            else if (pga > 0.07 && pga < 0.25)  return coeff.c17-0.132*Math.log(pga);
            else                                return coeff.c17+0.183;
        }

    }


    public void setParamDefaults(){

        siteTypeParam.setValue( SITE_TYPE_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_DEFAULT );
        distanceSeisParam.setValue( DISTANCE_SEIS_DEFAULT );
        hangingWallParam.setValue( HANGING_WALL_DEFAULT );
        saParam.setValue( SA_DEFAULT );
        periodParam.setValue( PERIOD_DEFAULT );
        dampingParam.setValue(DAMPING_DEFAULT);
        pgaParam.setValue( PGA_DEFAULT );
        pgvParam.setValue( PGV_DEFAULT );
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );
   }

   protected void initExceedProbIndependentParamsList(){
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter(this.sigmaTruncTypeParam);
        exceedProbIndependentParams.addParameter(this.sigmaTruncLevelParam);
    }

    /**
     * This creates the list of paramters that the Mean depends upon <br>
     * NOTE: This doesn not include the intensity-measure parameters
     * or any of thier internal independentParameters
     */
    protected void initMeanIndependentParamsList(){
        meanIndependentParams.clear();
        meanIndependentParams.addParameter( siteTypeParam );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( distanceSeisParam );
        meanIndependentParams.addParameter( hangingWallParam );
        meanIndependentParams.addParameter( componentParam );
    }


    /**
     * This creates the list of parameters that StdDev depends upon <br>
     * NOTE: This doesn not include the intensity-measure parameters
     * or any of thier internal independentParamaters.
     */
    protected void initStdDevIndependentParamsList(){
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( distanceSeisParam );
        meanIndependentParams.addParameter( hangingWallParam );
        meanIndependentParams.addParameter( componentParam );
    }


    /**
     *  Creates the Site-Type parameter and adds it to the siteParams list.
     *  Makes the parameters noneditable.
     */
    protected void initSiteParams( ) {

        StringConstraint siteConstraint = new StringConstraint();
        siteConstraint.addString( SITE_TYPE_FIRM_SOIL );
        siteConstraint.addString( SITE_TYPE_VERY_FIRM_SOIL );
        siteConstraint.addString( SITE_TYPE_SOFT_ROCK );
        siteConstraint.addString( SITE_TYPE_FIRM_ROCK );
        siteConstraint.addString( SITE_TYPE_NEHRP_BC );
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
        constraint.addString( FLT_TYPE_THRUST );
        constraint.addString( FLT_TYPE_REVERSE );
        constraint.addString( FLT_TYPE_UNKNOWN );
        constraint.setNonEditable();
        fltTypeParam = new StringParameter( FLT_TYPE_NAME, constraint, null);
        fltTypeParam.setInfo( FLT_TYPE_INFO );
        fltTypeParam.setNonEditable();

        probEqkRuptureParams.clear();
        probEqkRuptureParams.addParameter( magParam );
        probEqkRuptureParams.addParameter( fltTypeParam );

    }

    /**
     *  Creates the Propagation Effect parameters and adds them to the
     *  propagationEffectParams list. Makes the parameters noneditable.
     */
    protected void initPropagationEffectParams( ) {

        distanceSeisParam = new DistanceSeisParameter();
        distanceSeisParam.addParameterChangeWarningListener( warningListener );
        DoubleConstraint warn = new DoubleConstraint(DISTANCE_SEIS_WARN_MIN, DISTANCE_SEIS_WARN_MAX);
        warn.setNonEditable();
        distanceSeisParam.setWarningConstraint(warn);
        distanceSeisParam.setNonEditable();


        // create hanging wall parameter
        hangingWallParam = new DoubleParameter( HANGING_WALL_NAME, HANGING_WALL_MIN, HANGING_WALL_MAX);
        hangingWallParam.setInfo( HANGING_WALL_INFO );
        hangingWallParam.setNonEditable();

        propagationEffectParams.addParameter( distanceSeisParam );
        propagationEffectParams.addParameter( hangingWallParam );
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
        Enumeration keys = horzCoefficients.keys(); // same as for vertCoeffs
        while ( keys.hasMoreElements() ) {
            CB_2003_IMRCoefficients coeff = ( CB_2003_IMRCoefficients ) horzCoefficients.get( keys.nextElement() );
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

        // init other params defined in parent class
        super.initOtherParams();

        // the Component Parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( COMPONENT_AVE_HORZ );
        constraint.addString( COMPONENT_VERT );
        constraint.setNonEditable();
        componentParam = new StringParameter( COMPONENT_NAME, constraint, COMPONENT_DEFAULT );
        componentParam.setInfo( COMPONENT_INFO );
        componentParam.setNonEditable();

        // the stdDevType Parameter
        StringConstraint stdDevTypeConstraint = new StringConstraint();
        stdDevTypeConstraint.addString( STD_DEV_TYPE_MAG_DEP );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_PGA_DEP );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_NONE );
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_DEFAULT );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();
    }


    /**
     *  This creates the hashtables of coefficients for the supported
     *  intensityMeasures (im).  The key is the im parameter name, plus the
     *  period value for SA (separated by "/").  For example, the key for SA
     *  at 1.00 second period is "SA/1.00".
     */
    protected void initCoefficients() {

        initHorzCoefficients();
        initVertCoefficients();
    }

    /**
     *  This initializes the horizontal-component coeffiecients. Note that the "bv" values
     *  at 0.05- and 0.075-second period were obtained by linearly interpolating those at 0
     *  and 0.1 seconds (these come from BJF_1997, and are used to compute the mean for the
     *  "BC Boundary")
     */

    protected void initHorzCoefficients(  ) {

        String S = C + ": initCoefficients():";
        if ( D ) System.out.println( S + "Starting" );

        horzCoefficients.clear();

         // PGA - this is "Unc. PGA" (uncorrected, as he recommends for PGA) from his Table 4
        CB_2003_IMRCoefficients coeffPGA = new CB_2003_IMRCoefficients( PGA_NAME,
              -1, -2.896, 0.812, 0.000, -1.318, 0.187, -0.029, -0.064, 0.616, 0, 0.179, 0.307, -0.062, -0.195, -0.320, 0.370, 0.964, 0.263, -0.371);

        // SA/0.0 - this is "Cor. PGA" (corrected, as he recommends for SA at zero period) from his Table 4
        CB_2003_IMRCoefficients coeff0 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.0" ) ).doubleValue() ,
              0.0, -4.033, 0.812, 0.036, -1.061, 0.041, -0.005, -0.018, 0.766, 0.034, 0.343, 0.351, -0.123, -0.138, -0.289, 0.370, 0.920, 0.219, -0.371);
        // SA/0.05
        CB_2003_IMRCoefficients coeff1 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.05" ) ).doubleValue() ,
              0.05, -3.740, 0.812, 0.036, -1.121, 0.058, -0.004, -0.028, 0.724, 0.032, 0.302, 0.362, -0.140, -0.158, -0.205, 0.370, 0.940, 0.239, -0.292);
        // SA/0.075
        CB_2003_IMRCoefficients coeff2 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.075" ) ).doubleValue() ,
              0.075, -3.076, 0.812, 0.050, -1.252, 0.121, -0.005, -0.051, 0.648, 0.040, 0.243, 0.333, -0.150, -0.196, -0.208, 0.370, 0.952, 0.251, -0.252);
        // SA/0.1
        CB_2003_IMRCoefficients coeff3 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.1" ) ).doubleValue() ,
              0.10, -2.661, 0.812, 0.060, -1.308, 0.166, -0.009, -0.068, 0.621, 0.046, 0.224, 0.313, -0.146, -0.253, -0.258, 0.370, 0.958, 0.257, -0.212);
        // SA/0.15
        CB_2003_IMRCoefficients coeff4 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.15" ) ).doubleValue() ,
              0.15, -2.270, 0.812, 0.041, -1.324, 0.212, -0.033, -0.081, 0.613, 0.031, 0.318, 0.344, -0.176, -0.267, -0.284, 0.370, 0.974, 0.273, -0.238);
        // SA/0.2
        CB_2003_IMRCoefficients coeff5 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.2" ) ).doubleValue() ,
              0.20, -2.771, 0.812, 0.030, -1.153, 0.098, -0.014, -0.038, 0.704, 0.026, 0.296, 0.342, -0.148, -0.183, -0.359, 0.370, 0.981, 0.280, -0.292);
        // SA/0.3
        CB_2003_IMRCoefficients coeff6 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.3" ) ).doubleValue() ,
              0.30, -2.999, 0.812, 0.007, -1.080, 0.059, -0.007, -0.022, 0.752, 0.007, 0.359, 0.385, -0.162, -0.157, -0.585, 0.370, 0.984, 0.283, -0.401);
        // SA/0.4
        CB_2003_IMRCoefficients coeff7 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.4" ) ).doubleValue() ,
              0.40, -3.511, 0.812, -0.015, -0.964, 0.024, -0.002, -0.005, 0.842, -0.016, 0.379, 0.438, -0.078, -0.129, -0.557, 0.370, 0.987, 0.286, -0.487);
        // SA/0.5
        CB_2003_IMRCoefficients coeff8 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.5" ) ).doubleValue() ,
              0.50, -3.556, 0.812, -0.035, -0.964, 0.023, -0.002, -0.004, 0.842, -0.036, 0.406, 0.479, -0.122, -0.130, -0.701, 0.370, 0.990, 0.289, -0.553);
        // SA/0.75
        CB_2003_IMRCoefficients coeff9 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.75" ) ).doubleValue() ,
              0.75, -3.709, 0.812, -0.071, -0.964, 0.021, -0.002, -0.002, 0.842, -0.074, 0.347, 0.419, -0.108, -0.124, -0.796, 0.331, 1.021, 0.320, -0.653);
        // SA/1.0
        CB_2003_IMRCoefficients coeff10 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "1.0" ) ).doubleValue() ,
              1.0, -3.867, 0.812, -0.101, -0.964, 0.019, 0, 0, 0.842, -0.105, 0.329, 0.338, -0.073, -0.072, -0.858, 0.281, 1.021, 0.320, -0.698);
        // SA/1.5
        CB_2003_IMRCoefficients coeff11 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "1.5" ) ).doubleValue() ,
              1.5, -4.093, 0.812, -0.150, -0.964, 0.019, 0, 0, 0.842, -0.155, 0.217, 0.188, -0.079, -0.056, -0.954, 0.210, 1.021, 0.320, -0.704);
        // SA/2.0
        CB_2003_IMRCoefficients coeff12 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "2.0" ) ).doubleValue() ,
              2.0, -4.311, 0.812, -0.180, -0.964, 0.019, 0, 0, 0.842, -0.187, 0.060, 0.064, -0.124, -0.116, -0.916, 0.160, 1.021, 0.320, -0.665);
        // SA/3.0
        CB_2003_IMRCoefficients coeff13 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "3.0" ) ).doubleValue() ,
              3.0, -4.817, 0.812, -0.193, -0.964, 0.019, 0, 0, 0.842, -0.200, -0.079, 0.021, -0.154, -0.117, -0.873, 0.089, 1.021, 0.320, -0.665);
        // SA/4.0
        CB_2003_IMRCoefficients coeff14 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "4.0" ) ).doubleValue() ,
              4.0, -5.211, 0.812, -0.202, -0.964, 0.019, 0, 0, 0.842, -0.209, -0.061, 0.057, -0.054, -0.261, -0.889, 0.039, 1.021, 0.320, -0.665);

        horzCoefficients.put( coeffPGA.getName(), coeffPGA );
        horzCoefficients.put( coeff0.getName(), coeff0 );
        horzCoefficients.put( coeff1.getName(), coeff1 );
        horzCoefficients.put( coeff2.getName(), coeff2 );
        horzCoefficients.put( coeff3.getName(), coeff3 );
        horzCoefficients.put( coeff4.getName(), coeff4 );
        horzCoefficients.put( coeff5.getName(), coeff5 );
        horzCoefficients.put( coeff6.getName(), coeff6 );
        horzCoefficients.put( coeff7.getName(), coeff7 );
        horzCoefficients.put( coeff8.getName(), coeff8 );
        horzCoefficients.put( coeff9.getName(), coeff9 );
        horzCoefficients.put( coeff10.getName(), coeff10 );
        horzCoefficients.put( coeff11.getName(), coeff11 );
        horzCoefficients.put( coeff12.getName(), coeff12 );
        horzCoefficients.put( coeff13.getName(), coeff13 );
        horzCoefficients.put( coeff14.getName(), coeff14 );
    }



    /**
     * This initializes the Vertical component coeffiecients.  Note that the "bv" values
     * at 0.05- and 0.075-second period were obtained by linearly interpolating those at 0
     * and 0.1 seconds (these come from BJF_1997, and are used to compute the mean for the
     * "BC Boundary")
     */
    protected void initVertCoefficients(  ) {

        String S = C + ": initCoefficients():";
        if ( D ) System.out.println( S + "Starting" );

        vertCoefficients.clear();

         // PGA - this is "Unc. PGA" (uncorrected, as he recommends for PGA) from his Table 4
        CB_2003_IMRCoefficients coeffPGA = new CB_2003_IMRCoefficients( PGA_NAME,
              -1, -2.807, 0.756, 0, -1.391, 0.191, 0.044, -0.014, 0.544, 0, 0.091, 0.223, -0.096, -0.212, -0.199, 0.630, 1.003, 0.302, -0.371);

        // SA/0.0 - this is "Cor. PGA" (corrected, as he recommends for SA at zero period) from his Table 4
        CB_2003_IMRCoefficients coeff0 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.0" ) ).doubleValue() ,
              0.0, -3.108, 0.756, 0, -1.287, 0.142, 0.046, -0.040, 0.587, 0, 0.253, 0.173, -0.135, -0.138, -0.256, 0.630, 0.975, 0.274, -0.371);
        // SA/0.05
        CB_2003_IMRCoefficients coeff1 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.05" ) ).doubleValue() ,
              0.05, -1.918, 0.756, 0, -1.517, 0.309, 0.069, -0.023, 0.498, 0, 0.058, 0.100, -0.195, -0.274, -0.219, 0.630, 1.031, 0.330, -0.292);
        // SA/0.075
        CB_2003_IMRCoefficients coeff2 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.075" ) ).doubleValue() ,
              0.075, -1.504, 0.756, 0, -1.551, 0.343, 0.083, 0.000, 0.487, 0, 0.135, 0.182, -0.224, -0.303, -0.263, 0.630, 1.031, 0.330, -0.252);
        // SA/0.1
        CB_2003_IMRCoefficients coeff3 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.1" ) ).doubleValue() ,
              0.10, -1.672, 0.756, 0, -1.473, 0.282, 0.062, 0.001, 0.513, 0, 0.168, 0.210, -0.198, -0.275, -0.252, 0.630, 1.031, 0.330, -0.212);
        // SA/0.15
        CB_2003_IMRCoefficients coeff4 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.15" ) ).doubleValue() ,
              0.15, -2.323, 0.756, 0, -1.280, 0.171, 0.045, 0.008, 0.591, 0, 0.223, 0.238, -0.170, -0.175, -0.270, 0.630, 1.031, 0.330, -0.238);
        // SA/0.2
        CB_2003_IMRCoefficients coeff5 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.2" ) ).doubleValue() ,
              0.20, -2.998, 0.756, 0, -1.131, 0.089, 0.028, 0.004, 0.668, 0, 0.234, 0.256, -0.098, -0.041, -0.311, 0.571, 1.031, 0.330, -0.292);
        // SA/0.3
        CB_2003_IMRCoefficients coeff6 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.3" ) ).doubleValue() ,
              0.30, -3.721, 0.756, 0.007, -1.028, 0.050, 0.010, 0.004, 0.736, 0.007, 0.249, 0.328, -0.026, 0.082, -0.265, 0.488, 1.031, 0.330, -0.401);
        // SA/0.4
        CB_2003_IMRCoefficients coeff7 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.4" ) ).doubleValue() ,
              0.40, -4.536, 0.756, -0.015, -0.812, 0.012, 0, 0, 0.931, -0.018, 0.299, 0.317, -0.017, 0.022, -0.257, 0.428, 1.031, 0.330, -0.487);
        // SA/0.5
        CB_2003_IMRCoefficients coeff8 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.5" ) ).doubleValue() ,
              0.50, -4.651, 0.756, -0.035, -0.812, 0.012, 0, 0, 0.931, -0.043, 0.243, 0.354, -0.020, 0.092, -0.293, 0.383, 1.031, 0.330, -0.553);
        // SA/0.75
        CB_2003_IMRCoefficients coeff9 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "0.75" ) ).doubleValue() ,
              0.75, -4.903, 0.756, -0.071, -0.812, 0.012, 0, 0, 0.931, -0.087, 0.295, 0.418, 0.078, 0.091, -0.349, 0.299, 1.031, 0.330, -0.653);
        // SA/1.0
        CB_2003_IMRCoefficients coeff10 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "1.0" ) ).doubleValue() ,
              1.0, -4.950, 0.756, -0.101, -0.812, 0.012, 0, 0, 0.931, -0.124, 0.266, 0.315, 0.043, 0.101, -0.481, 0.240, 1.031, 0.330, -0.698);
        // SA/1.5
        CB_2003_IMRCoefficients coeff11 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "1.5" ) ).doubleValue() ,
              1.5, -5.073, 0.756, -0.150, -0.812, 0.012, 0, 0, 0.931, -0.184, 0.171, 0.211, -0.038, -0.018, -0.518, 0.240, 1.031, 0.330, -0.704);
        // SA/2.0
        CB_2003_IMRCoefficients coeff12 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "2.0" ) ).doubleValue() ,
              2.0, -5.292, 0.756, -0.180, -0.812, 0.012, 0, 0, 0.931, -0.222, 0.114, 0.115, 0.033, -0.022, -0.503, 0.240, 1.031, 0.330, -0.665);
        // SA/3.0
        CB_2003_IMRCoefficients coeff13 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "3.0" ) ).doubleValue() ,
              3.0, -5.748, 0.756, -0.193, -0.812, 0.012, 0, 0, 0.931, -0.238, 0.179, 0.159, -0.010, -0.047, -0.539, 0.240, 1.031, 0.330, -0.665);
        // SA/4.0
        CB_2003_IMRCoefficients coeff14 = new CB_2003_IMRCoefficients( SA_NAME + '/' +( new Double( "4.0" ) ).doubleValue() ,
              4.0, -6.042, 0.756, -0.202, -0.812, 0.012, 0, 0, 0.931, -0.248, 0.237, 0.134, -0.059, -0.267, -0.606, 0.240, 1.031, 0.330, -0.665);

        vertCoefficients.put( coeffPGA.getName(), coeffPGA );
        vertCoefficients.put( coeff0.getName(), coeff0 );
        vertCoefficients.put( coeff1.getName(), coeff1 );
        vertCoefficients.put( coeff2.getName(), coeff2 );
        vertCoefficients.put( coeff3.getName(), coeff3 );
        vertCoefficients.put( coeff4.getName(), coeff4 );
        vertCoefficients.put( coeff5.getName(), coeff5 );
        vertCoefficients.put( coeff6.getName(), coeff6 );
        vertCoefficients.put( coeff7.getName(), coeff7 );
        vertCoefficients.put( coeff8.getName(), coeff8 );
        vertCoefficients.put( coeff9.getName(), coeff9 );
        vertCoefficients.put( coeff10.getName(), coeff10 );
        vertCoefficients.put( coeff11.getName(), coeff11 );
        vertCoefficients.put( coeff12.getName(), coeff12 );
        vertCoefficients.put( coeff13.getName(), coeff13 );
        vertCoefficients.put( coeff14.getName(), coeff14 );
    }





    /**
     *  <b>Title:</b> CB_2003_IMRCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the CB_2003_IMR.  One instance of this class holds the set of
     *  coefficients for each period.  The "bv" coefficient is that from BJF_1997
     *  needed for the "BC Boundary" site category.<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Edward H Field
     * @created    September 2002
     * @version    1.0
     */

    class CB_2003_IMRCoefficients
             implements NamedObjectAPI {


        protected final static String C = "CB_2003_IMRCoefficients";
        protected final static boolean D = true;

        protected String name;
        protected double period = -1;
        protected double c1;
        protected double c2;
        protected double c3;
        protected double c4;
        protected double c5;
        protected double c6;
        protected double c7;
        protected double c8;
        protected double c9;
        protected double c10;
        protected double c11;
        protected double c12;
        protected double c13;
        protected double c14;
        protected double c15;
        protected double c16;
        protected double c17;
        protected double bv;

        /**
         *  Constructor for the CB_2003_IMRCoefficients object
         *
         * @param  name  Description of the Parameter
         */
        public CB_2003_IMRCoefficients( String name ) { this.name = name; }

        /**
         *  Constructor for the CB_2003_IMRCoefficients object that sets all values at once
         *
         * @param  name  Description of the Parameter
         */
        public CB_2003_IMRCoefficients( String name,  double period,
            double c1,  double c2,  double c3,  double c4,  double c5,
            double c6,  double c7,  double c8,  double c9,  double c10,
            double c11, double c12, double c13, double c14, double c15,
            double c16, double c17, double bv)
       {
            this.name = name;     this.period = period;
            this.c1 = c1;   this.c2 = c2;   this.c3 = c3;
            this.c4 = c4;   this.c5 = c5;   this.c6 = c6;
            this.c7 = c7;   this.c8 = c8;   this.c9 = c9;
            this.c10 = c10; this.c11 = c11; this.c12 = c12;
            this.c13 = c13; this.c14 = c14; this.c15 = c15;
            this.c16 = c16; this.c17 = c17; this.bv = bv;
        }

        /**
         *  Gets the name attribute of the CB_2003_IMRCoefficients object
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
            b.append( "\n  Name = " + name );
            b.append( "\n  c1 = " + c1 );
            b.append( "\n  c2 = " + c2 );       b.append( "\n  c3 = " + c3 );
            b.append( "\n  c4 = " + c4 );       b.append( "\n  c5 = " + c5 );
            b.append( "\n  c6 = " + c6 );       b.append( "\n  c7 = " + c7 );
            b.append( "\n  c8 = " + c8 );       b.append( "\n  c9 = " + c9 );
            b.append( "\n  c10 = " + c10 );     b.append( "\n  c11 = " + c11 );
            b.append( "\n  c12 = " + c12 );     b.append( "\n  c13 = " + c13 );
            b.append( "\n  c14 = " + c14 );     b.append( "\n  c15 = " + c15 );
            b.append( "\n  c16 = " + c16 );     b.append( "\n  c17 = " + c17 );
            b.append( "\n  bv = " + bv );
           return b.toString();
        }
    }
}

