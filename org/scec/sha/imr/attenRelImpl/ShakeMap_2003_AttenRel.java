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
 * <b>Title:</b> ShakeMap_2003_AttenRel<p>
 *
 * <b>Description:</b> This implements the Attenuation Relationship
 * developed by the ShakeMap group (2003).  There is no written documentation
 * of this relationship <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceJBParam - closest distance to surface projection of fault
 * <LI>vs30Param - Average 30-meter shear-wave velocity at the site
 * <LI>fltTypeParam - Style of faulting
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Edward H. Field
 * @created    April, 2003
 * @version    1.0
 */


public class ShakeMap_2003_AttenRel
         extends AttenuationRelationship
         implements
        AttenuationRelationshipAPI,
        NamedObjectAPI {

    // debugging stuff:
    private final static String C = "ShakeMap_2003_AttenRel";
    private final static boolean D = false;
    public final static String NAME = "ShakeMap (2003)";

    // style of faulting options
    public final static String FLT_TYPE_UNKNOWN = "Unknown";
    public final static String FLT_TYPE_STRIKE_SLIP = "Strike-Slip";
    public final static String FLT_TYPE_REVERSE = "Reverse";
    public final static String FLT_TYPE_DEFAULT = "Unknown";

    // warning constraint fields:
    protected final static Double VS30_WARN_MIN = new Double(180.0);
    protected final static Double VS30_WARN_MAX = new Double(3500.0);
    protected final static Double MAG_WARN_MIN = new Double(3.3);
    protected final static Double MAG_WARN_MAX = new Double(7.5);

    /**
     * Joyner-Boore Distance parameter
     */
    private DistanceJBParameter distanceJBParam = null;
    private final static Double DISTANCE_JB_DEFAULT = new Double( 0 );
    protected final static Double DISTANCE_JB_WARN_MIN = new Double(0.0);
    protected final static Double DISTANCE_JB_WARN_MAX = new Double(80.0);

    /**
     * The current set of coefficients based on the selected intensityMeasure
     */
    private BJF_1997_AttenRelCoefficients coeffBJF = null;
    private BJF_1997_AttenRelCoefficients coeffSM = null;

    /**
     *  Hashtable of coefficients for the supported intensityMeasures
     */
    protected Hashtable coefficientsBJF = new Hashtable();
    protected Hashtable coefficientsSM = new Hashtable();

    // for issuing warnings:
    private transient  ParameterChangeWarningListener warningListener = null;


    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam.  Options are "Reverse" if 150>rake>30,
     * "Strike-Slip" if rake is within 30 degrees of 0 or 180, and "Unkown"
     * otherwise (which means normal-faulting events are assigned as "Unkown";
     * confirmed by David Boore via email as being correct).
     *
     * @param rake                      in degrees
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected void setFaultTypeFromRake( double rake )
        throws InvalidRangeException
    {
        FaultUtils.assertValidRake( rake );
        if( Math.abs( Math.sin( rake*Math.PI/180 ) ) <= 0.5 ) fltTypeParam.setValue(FLT_TYPE_STRIKE_SLIP);  // 0.5 = sin(30)
        else if ( rake >= 30 && rake <= 150 )                 fltTypeParam.setValue(FLT_TYPE_REVERSE);
        else                                                  fltTypeParam.setValue(FLT_TYPE_UNKNOWN);
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

        try {
        // constraints get checked
          magParam.setValue( probEqkRupture.getMag() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }

        try {
          // If fail, rollback to all old values
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
       // This may throw a constraint exception
        try{
          this.vs30Param.setValue( vs30.getValue() );
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

//      Ned replaced the following with what's below (so the dampingParam value is not part of the key)
/*
        String key = ((DependentParameterAPI)im).getIndependentParametersKey();
        if( coefficients.containsKey( key ) ) coeff = ( BJF_1997_AttenRelCoefficients )coefficients.get( key );

        else throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
*/
        StringBuffer key = new StringBuffer( im.getName() );
        if( im.getName().equalsIgnoreCase(SA_NAME) ) key.append( "/" + periodParam.getValue() );
        if( coefficientsBJF.containsKey( key.toString() ) ) {
          coeffBJF = ( BJF_1997_AttenRelCoefficients )coefficientsBJF.get( key.toString() );
          coeffSM = ( BJF_1997_AttenRelCoefficients )coefficientsSM.get( key.toString() );
        } else throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
    }


    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public ShakeMap_2003_AttenRel( ParameterChangeWarningListener warningListener ) {

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
     * Calculates the mean of the exceedence probability distribution. The exact
     * formula is: <p>
     *
     * double mean = b1 + <br>
     * coeff.b2 * ( mag - 6 ) + <br>
     * coeff.b3 * ( Math.pow( ( mag - 6 ), 2 ) ) +  <br>
     * coeff.b5 * ( Math.log( Math.pow( ( distanceJB * distanceJB  + coeff.h * coeff.h  ), 0.5 ) ) ) + <br>
     * coeff.bv * ( Math.log( vs30 / coeff.va ) ) <br>
     * @return    The mean value
     */
    public double getMean() throws IMRException{

        double mag, vs30, distanceJB;
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

        // Get b1 based on fault type
        double b1_BJF, b1_SM;
        if ( fltTypeValue.equals( FLT_TYPE_STRIKE_SLIP ) ) {
            b1_BJF = coeffBJF.b1ss;
            b1_SM = coeffSM.b1ss;
        } else if ( fltTypeValue.equals( FLT_TYPE_REVERSE ) ) {
            b1_BJF = coeffBJF.b1rv;
            b1_SM = coeffSM.b1rv;
        } else if ( fltTypeValue.equals( FLT_TYPE_UNKNOWN ) ) {
            b1_BJF = coeffBJF.b1all;
            b1_SM = coeffSM.b1all;
        } else {
            throw new ParameterException( C + ": getMean(): Invalid ProbEqkRupture Parameter value for : FaultType" );
        }

        // Calculate the log mean for BJF
        double meanBJF = b1_BJF +
            coeffBJF.b2 * ( mag - 6 ) +
            coeffBJF.b3 * ( Math.pow( ( mag - 6 ), 2 ) ) +
            coeffBJF.b5 * ( Math.log( Math.pow( ( distanceJB * distanceJB  + coeffBJF.h * coeffBJF.h  ), 0.5 ) ) ) +
            coeffBJF.bv * ( Math.log( vs30 / coeffBJF.va ) );

        // Calculate the log mean for SM
        double meanSM = b1_SM +
            coeffSM.b2 * ( mag - 6 ) +
            coeffSM.b3 * ( Math.pow( ( mag - 6 ), 2 ) ) +
            coeffSM.b5 * ( Math.log( Math.pow( ( distanceJB * distanceJB  + coeffSM.h * coeffSM.h  ), 0.5 ) ) ) +
            coeffSM.bv * ( Math.log( vs30 / coeffSM.va ) );

        // now return the appropriate mean
        if(mag <=5)
          return meanSM;
        else if (mag<=5.5)
          return meanBJF + (mag-5.5)*(meanBJF-meanSM)/0.5;
        else
          return meanBJF;
    }


    /**
     * @return    The stdDev value
     */
    public double getStdDev() throws IMRException {

        String stdDevType = stdDevTypeParam.getValue().toString();
        String component = componentParam.getValue().toString();

        // get the magnitude
        double mag;
        try{
          mag = ((Double)magParam.getValue()).doubleValue();
        }
        catch(NullPointerException e){
          throw new IMRException(C + ": getMean(): " + ERR);
        }


        // this is inefficient if the im has not been changed in any way
        updateCoefficients();

        double stdevBJF, stdevSM;
        // set the correct standard deviation depending on component and type
        if(component.equals(COMPONENT_AVE_HORZ)) {

            if ( stdDevType.equals( STD_DEV_TYPE_TOTAL ) ) {           // "Total"
              stdevBJF = Math.pow( ( coeffBJF.sigmaE * coeffBJF.sigmaE + coeffBJF.sigma1 * coeffBJF.sigma1 ) , 0.5 );
              stdevSM = Math.pow( ( coeffSM.sigmaE * coeffSM.sigmaE + coeffSM.sigma1 * coeffSM.sigma1 ) , 0.5 );
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_INTER ) ) {    // "Inter-Event"
              stdevBJF =  coeffBJF.sigmaE;
              stdevSM =  coeffSM.sigmaE;
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_INTRA ) ) {    // "Intra-Event"
              stdevBJF =  ( coeffBJF.sigma1 );
              stdevSM =  ( coeffSM.sigma1 );
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_NONE ) ) {    // "None (zero)"
              stdevBJF =   0;
              stdevSM =   0;
            }
            else {
              throw new ParameterException( C + ": getStdDev(): Invalid StdDevType" );
            }
        }
        else if ( component.equals(COMPONENT_RANDOM_HORZ ) ) {

            if ( stdDevType.equals( STD_DEV_TYPE_TOTAL ) ) {           // "Total"
              stdevBJF =  (coeffBJF.sigmaLnY );
              stdevSM =  (coeffSM.sigmaLnY );
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_INTER ) ) {    // "Inter-Event"
              stdevBJF =  (coeffBJF.sigmaE );
              stdevSM =  (coeffSM.sigmaE );
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_INTRA ) ) {    // "Intra-Event"
              stdevBJF =   (coeffBJF.sigmaR );
              stdevSM =   (coeffSM.sigmaR );
            }
            else if ( stdDevType.equals( STD_DEV_TYPE_NONE ) ) {    // "None (zero)"
              stdevBJF =   0;
              stdevSM =   0;
            }
            else {
              throw new ParameterException( C + ": getStdDev(): Invalid StdDevType" );
            }
        }
        else {
            throw new ParameterException( C + ": getStdDev(): Invalid component type" );
        }

        // now return the appropriate mean
        if(mag <=5)
          return stdevSM;
        else if (mag<=5.5)
          return stdevBJF + (mag-5.5)*(stdevBJF-stdevSM)/0.5;
        else
          return stdevBJF;


    }


    public void setParamDefaults(){

        //((ParameterAPI)this.iml).setValue( IML_DEFAULT );
        vs30Param.setValue( VS30_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_DEFAULT );
        distanceJBParam.setValue( DISTANCE_JB_DEFAULT );
        pgaParam.setValue( PGA_DEFAULT );
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );

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
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( componentParam );

        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter(stdDevTypeParam);
        stdDevIndependentParams.addParameter( componentParam );
        stdDevIndependentParams.addParameter( magParam );

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( distanceJBParam );
        exceedProbIndependentParams.addParameter( vs30Param );
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
     *  Creates the Vs30 site parameter and adds it to the siteParams list.
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

        // add it to the siteParams list:
        siteParams.clear();
        siteParams.addParameter( vs30Param );

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
        constraint.addString( FLT_TYPE_UNKNOWN );
        constraint.addString( FLT_TYPE_STRIKE_SLIP );
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

        supportedIMParams.clear();

/*  COMMENTED OUT BECAUSE SA IS NOT SUPPORTED
        // Create saParam's "Period" independent parameter:
        DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
        TreeSet set = new TreeSet();
        Enumeration keys = coefficients.keys();
        while ( keys.hasMoreElements() ) {
            BJF_1997_AttenRelCoefficients coeff = ( BJF_1997_AttenRelCoefficients ) coefficients.get( keys.nextElement() );
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

        // Put parameters in the supportedIMParams list:
        supportedIMParams.addParameter( saParam );

*/

        pgaParam.addParameterChangeWarningListener( warningListener );
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
        constraint.addString( COMPONENT_RANDOM_HORZ );
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
        * get the name of this IMR
        *
        * @returns the name of this IMR
        */
    public String getName() {
       return NAME;
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

        // Make the ShakeMap coefficients
        coefficientsSM.clear();
        // PGA
        BJF_1997_AttenRelCoefficients coeffSM = new BJF_1997_AttenRelCoefficients(PGA_NAME,
            0, 2.408, 2.408, 2.408, 1.3171, 0.000, -1.757, -0.473, 760, 6.0, 0.660,
            0.328, 0.737, 0.3948, 0.8361 );
        coefficientsSM.put( coeffSM.getName(), coeffSM );

        // Now make the original BJF 1997 coefficients
        coefficientsBJF.clear();
        // PGA
        BJF_1997_AttenRelCoefficients coeff = new BJF_1997_AttenRelCoefficients(PGA_NAME,
            0, -0.313, -0.117, -0.242, 0.527, 0.000, -0.778, -0.371, 1396, 5.57, 0.431, 0.226, 0.486, 0.184, 0.520 );
        // SA/0.00
        BJF_1997_AttenRelCoefficients coeff0 = new BJF_1997_AttenRelCoefficients( SA_NAME + '/' +( new Double( "0.00" ) ).doubleValue() ,
            0.00, -0.313, -0.117, -0.242, 0.527, 0, -0.778, -0.371, 1396, 5.57, 0.431, 0.226, 0.486, 0.184, 0.520 );
        // SA/0.10
        BJF_1997_AttenRelCoefficients coeff1 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.10" ) ).doubleValue() ,
            0.10, 1.006, 1.087, 1.059, 0.753, -0.226, -0.934, -0.212, 1112, 6.27, 0.440, 0.189, 0.479, 0.000, 0.479 );
        // SA/0.11
        BJF_1997_AttenRelCoefficients coeff2 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.11" ) ).doubleValue() ,
            0.11, 1.072, 1.164, 1.13, 0.732, -0.23, -0.937, -0.211, 1291, 6.65, 0.437, 0.200, 0.481, 0.000, 0.481 );
        // SA/0.12
        BJF_1997_AttenRelCoefficients coeff3 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.12" ) ).doubleValue() ,
            0.12, 1.109, 1.215, 1.174, 0.721, -0.233, -0.939, -0.215, 1452, 6.91, 0.437, 0.210, 0.485, 0.000, 0.485 );
        // SA/0.13
        BJF_1997_AttenRelCoefficients coeff4 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.13" ) ).doubleValue() ,
            0.13, 1.128, 1.246, 1.2, 0.711, -0.233, -0.939, -0.221, 1596, 7.08, 0.435, 0.216, 0.486, 0.000, 0.486 );
        // SA/0.14
        BJF_1997_AttenRelCoefficients coeff5 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.14" ) ).doubleValue() ,
            0.14, 1.135, 1.261, 1.208, 0.707, -0.23, -0.938, -0.228, 1718, 7.18, 0.435, 0.223, 0.489, 0.000, 0.489 );
        // SA/0.15
        BJF_1997_AttenRelCoefficients coeff6 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.15" ) ).doubleValue() ,
            0.15, 1.128, 1.264, 1.204, 0.702, -0.228, -0.937, -0.238, 1820, 7.23, 0.435, 0.230, 0.492, 0.000, 0.492 );
        // SA/0.16
        BJF_1997_AttenRelCoefficients coeff7 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.16" ) ).doubleValue() ,
            0.16, 1.112, 1.257, 1.192, 0.702, -0.226, -0.935, -0.248, 1910, 7.24, 0.435, 0.235, 0.495, 0.000, 0.495 );
        // SA/0.17
        BJF_1997_AttenRelCoefficients coeff8 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.17" ) ).doubleValue() ,
            0.17, 1.09, 1.242, 1.173, 0.702, -0.221, -0.933, -0.258, 1977, 7.21, 0.435, 0.293, 0.497, 0.000, 0.497 );
        // SA/0.18
        BJF_1997_AttenRelCoefficients coeff9 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.18" ) ).doubleValue() ,
            0.18, 1.063, 1.222, 1.151, 0.705, -0.216, -0.93, -0.27, 2037, 7.16, 0.435, 0.244, 0.499, 0.002, 0.499 );
        // SA/0.19
        BJF_1997_AttenRelCoefficients coeff10 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.19" ) ).doubleValue() ,
            0.19, 1.032, 1.198, 1.122, 0.709, -0.212, -0.927, -0.281, 2080, 7.1, 0.435, 0.294, 0.501, 0.005, 0.501 );
        // SA/0.20
        BJF_1997_AttenRelCoefficients coeff11 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.20" ) ).doubleValue() ,
            0.20, 0.999, 1.17, 1.089, 0.711, -0.207, -0.924, -0.292, 2118, 7.02, 0.435, 0.251, 0.502, 0.009, 0.502 );
        // SA/0.22
        BJF_1997_AttenRelCoefficients coeff12 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.22" ) ).doubleValue() ,
            0.22, 0.925, 1.104, 1.019, 0.721, -0.198, -0.918, -0.315, 2158, 6.83, 0.437, 0.285, 0.508, 0.016, 0.508 );
        // SA/0.24
        BJF_1997_AttenRelCoefficients coeff13 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.24" ) ).doubleValue() ,
            0.24, 0.847, 1.033, 0.941, 0.732, -0.189, -0.912, -0.338, 2178, 6.62, 0.437, 0.262, 0.510, 0.025, 0.511 );
        // SA/0.26
        BJF_1997_AttenRelCoefficients coeff14 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.26" ) ).doubleValue() ,
            0.26, 0.764, 0.958, 0.861, 0.744, -0.18, -0.906, -0.36, 2173, 6.39, 0.437, 0.267, 0.513, 0.032, 0.514 );
        // SA/0.28
        BJF_1997_AttenRelCoefficients coeff15 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.28" ) ).doubleValue() ,
            0.28, 0.681, 0.881, 0.78, 0.758, -0.168, -0.899, -0.381, 2158, 6.17, 0.440, 0.272, 0.517, 0.039, 0.518 );
        // SA/0.30
        BJF_1997_AttenRelCoefficients coeff16 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.30" ) ).doubleValue() ,
            0.30, 0.598, 0.803, 0.7, 0.769, -0.161, -0.893, -0.401, 2133, 5.94, 0.440, 0.276, 0.519, 0.048, 0.522 );
        // SA/0.32
        BJF_1997_AttenRelCoefficients coeff17 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.32" ) ).doubleValue() ,
            0.32, 0.518, 0.725, 0.619, 0.783, -0.152, -0.888, -0.42, 2104, 5.72, 0.442, 0.279, 0.523, 0.055, 0.525 );
        // SA/0.34
        BJF_1997_AttenRelCoefficients coeff18 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.34" ) ).doubleValue() ,
            0.34, 0.439, 0.648, 0.54, 0.794, -0.143, -0.882, -0.438, 2070, 5.5, 0.444, 0.281, 0.526, 0.064, 0.530 );
        // SA/0.36
        BJF_1997_AttenRelCoefficients coeff19 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.36" ) ).doubleValue() ,
            0.36, 0.361, 0.57, 0.462, 0.806, -0.136, -0.877, -0.456, 2032, 5.3, 0.444, 0.283, 0.527, 0.071, 0.532 );
        // SA/0.38
        BJF_1997_AttenRelCoefficients coeff20 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.38" ) ).doubleValue() ,
            0.38, 0.286, 0.495, 0.385, 0.82, -0.127, -0.872, -0.472, 1995, 5.1, 0.447, 0.286, 0.530, 0.078, 0.536 );
        // SA/0.40
        BJF_1997_AttenRelCoefficients coeff21 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.40" ) ).doubleValue() ,
            0.40, 0.212, 0.423, 0.311, 0.831, -0.12, -0.867, -0.487, 1954, 4.91, 0.447, 0.288, 0.531, 0.085, 0.538 );
        // SA/0.42
        BJF_1997_AttenRelCoefficients coeff22 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.42" ) ).doubleValue() ,
            0.42, 0.14, 0.352, 0.239, 0.84, -0.113, -0.862, -0.502, 1919, 4.74, 0.449, 0.290, 0.535, 0.092, 0.542 );
        // SA/0.44
        BJF_1997_AttenRelCoefficients coeff23 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.44" ) ).doubleValue() ,
            0.44, 0.073, 0.282, 0.169, 0.852, -0.108, -0.858, -0.516, 1884, 4.57, 0.449, 0.292, 0.536, 0.099, 0.545 );
        // SA/0.46
        BJF_1997_AttenRelCoefficients coeff24 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.46" ) ).doubleValue() ,
            0.46, 0.005, 0.217, 0.102, 0.863, -0.101, -0.854, -0.529, 1849, 4.41, 0.451, 0.295, 0.539, 0.104, 0.549 );
        // SA/0.48
        BJF_1997_AttenRelCoefficients coeff25 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.48" ) ).doubleValue() ,
            0.48, -0.058, 0.151, 0.036, 0.873, -0.097, -0.85, -0.541, 1816, 4.26, 0.451, 0.297, 0.540, 0.111, 0.551 );
        // SA/0.50
        BJF_1997_AttenRelCoefficients coeff26 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.50" ) ).doubleValue() ,
            0.50, -0.122, 0.087, -0.025, 0.884, -0.09, -0.846, -0.553, 1782, 4.13, 0.454, 0.299, 0.543, 0.115, 0.556 );
        // SA/0.55
        BJF_1997_AttenRelCoefficients coeff27 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.55" ) ).doubleValue() ,
            0.55, -0.268, -0.063, -0.176, 0.907, -0.078, -0.837, -0.579, 1710, 3.82, 0.456, 0.302, 0.547, 0.129, 0.562 );
        // SA/0.60
        BJF_1997_AttenRelCoefficients coeff28 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.60" ) ).doubleValue() ,
            0.60, -0.401, -0.203, -0.314, 0.928, -0.069, -0.83, -0.602, 1644, 3.57, 0.458, 0.306, 0.551, 0.143, 0.569 );
        // SA/0.65
        BJF_1997_AttenRelCoefficients coeff29 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.65" ) ).doubleValue() ,
            0.65, -0.523, -0.331, -0.44, 0.946, -0.06, -0.823, -0.622, 1592, 3.36, 0.461, 0.309, 0.554, 0.154, 0.575 );
        // SA/0.70
        BJF_1997_AttenRelCoefficients coeff30 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.70" ) ).doubleValue() ,
            0.70, -0.634, -0.452, -0.555, 0.962, -0.053, -0.818, -0.639, 1545, 3.2, 0.463, 0.311, 0.558, 0.166, 0.582 );
        // SA/0.75
        BJF_1997_AttenRelCoefficients coeff31 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.75" ) ).doubleValue() ,
            0.75, -0.737, -0.562, -0.661, 0.979, -0.046, -0.813, -0.653, 1507, 3.07, 0.465, 0.313, 0.561, 0.175, 0.587 );
        // SA/0.80
        BJF_1997_AttenRelCoefficients coeff32 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.80" ) ).doubleValue() ,
            0.80, -0.829, -0.666, -0.76, 0.992, -0.041, -0.809, -0.666, 1476, 2.98, 0.467, 0.315, 0.564, 0.184, 0.593 );
        // SA/0.85
        BJF_1997_AttenRelCoefficients coeff33 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.85" ) ).doubleValue() ,
            0.85, -0.915, -0.761, -0.851, 1.006, -0.037, -0.805, -0.676, 1452, 2.92, 0.467, 0.320, 0.567, 0.191, 0.598 );
        // SA/0.90
        BJF_1997_AttenRelCoefficients coeff34 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.90" ) ).doubleValue() ,
            0.90, -0.993, -0.848, -0.933, 1.018, -0.035, -0.802, -0.685, 1432, 2.89, 0.470, 0.322, 0.570, 0.200, 0.604 );
        // SA/0.95
        BJF_1997_AttenRelCoefficients coeff35 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "0.95" ) ).doubleValue() ,
            0.95, -1.066, -0.932, -1.01, 1.027, -0.032, -0.8, -0.692, 1416, 2.88, 0.472, 0.325, 0.573, 0.207, 0.609 );
        // SA/1.00
        BJF_1997_AttenRelCoefficients coeff36 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.00" ) ).doubleValue() ,
            1.00, -1.133, -1.009, -1.08, 1.036, -0.032, -0.798, -0.698, 1406, 2.9, 0.474, 0.325, 0.575, 0.214, 0.613 );
        // SA/1.10
        BJF_1997_AttenRelCoefficients coeff37 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.10" ) ).doubleValue() ,
            1.10, -1.249, -1.145, -1.208, 1.052, -0.03, -0.795, -0.706, 1396, 2.99, 0.477, 0.329, 0.579, 0.226, 0.622 );
        // SA/1.20
        BJF_1997_AttenRelCoefficients coeff38 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.20" ) ).doubleValue() ,
            1.20, -1.345, -1.265, -1.315, 1.064, -0.032, -0.794, -0.71, 1400, 3.14, 0.479, 0.334, 0.584, 0.235, 0.629 );
        // SA/1.30
        BJF_1997_AttenRelCoefficients coeff39 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.30" ) ).doubleValue() ,
            1.30, -1.428, -1.37, -1.407, 1.073, -0.035, -0.793, -0.711, 1416, 3.36, 0.481, 0.338, 0.588, 0.244, 0.637 );
        // SA/1.40
        BJF_1997_AttenRelCoefficients coeff40 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.40" ) ).doubleValue() ,
            1.40, -1.495, -1.46, -1.483, 1.08, -0.039, -0.794, -0.709, 1442, 3.62, 0.484, 0.341, 0.592, 0.251, 0.643 );
        // SA/1.50
        BJF_1997_AttenRelCoefficients coeff41 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.50" ) ).doubleValue() ,
            1.50, -1.552, -1.538, -1.55, 1.085, -0.044, -0.796, -0.704, 1479, 3.92, 0.486, 0.345, 0.596, 0.256, 0.649 );
        // SA/1.60
        BJF_1997_AttenRelCoefficients coeff42 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.60" ) ).doubleValue() ,
            1.60, -1.598, -1.608, -1.605, 1.087, -0.051, -0.798, -0.697, 1524, 4.26, 0.488, 0.348, 0.599, 0.262, 0.654 );
        // SA/1.70
        BJF_1997_AttenRelCoefficients coeff43 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.70" ) ).doubleValue() ,
            1.70, -1.634, -1.668, -1.652, 1.089, -0.058, -0.801, -0.689, 1581, 4.62, 0.490, 0.352, 0.604, 0.267, 0.66 );
        // SA/1.80
        BJF_1997_AttenRelCoefficients coeff44 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.80" ) ).doubleValue() ,
            1.80, -1.663, -1.718, -1.689, 1.087, -0.067, -0.804, -0.679, 1644, 5.01, 0.493, 0.355, 0.607, 0.269, 0.664 );
        // SA/1.90
        BJF_1997_AttenRelCoefficients coeff45 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "1.90" ) ).doubleValue() ,
            1.90, -1.685, -1.763, -1.72, 1.087, -0.074, -0.808, -0.667, 1714, 5.42, 0.493, 0.359, 0.610, 0.274, 0.669 );
        // SA/2.00
        BJF_1997_AttenRelCoefficients coeff46 = new BJF_1997_AttenRelCoefficients( "SA/" +( new Double( "2.00" ) ).doubleValue() ,
            2.00, -1.699, -1.801, -1.743, 1.085, -0.085, -0.812, -0.655, 1795, 5.85, 0.495, 0.362, 0.613, 0.276, 0.672 );

        coefficientsBJF.put( coeff.getName(), coeff );

/* COMMENTED OUT BECAUSE SA NOT YET SUPPORTED
        coefficientsBJF.put( coeff0.getName(), coeff0 );
        coefficientsBJF.put( coeff1.getName(), coeff1 );
        coefficientsBJF.put( coeff2.getName(), coeff2 );
        coefficientsBJF.put( coeff3.getName(), coeff3 );
        coefficientsBJF.put( coeff4.getName(), coeff4 );
        coefficientsBJF.put( coeff5.getName(), coeff5 );
        coefficientsBJF.put( coeff6.getName(), coeff6 );
        coefficientsBJF.put( coeff7.getName(), coeff7 );
        coefficientsBJF.put( coeff8.getName(), coeff8 );
        coefficientsBJF.put( coeff9.getName(), coeff9 );

        coefficientsBJF.put( coeff10.getName(), coeff10 );
        coefficientsBJF.put( coeff11.getName(), coeff11 );
        coefficientsBJF.put( coeff12.getName(), coeff12 );
        coefficientsBJF.put( coeff13.getName(), coeff13 );
        coefficientsBJF.put( coeff14.getName(), coeff14 );
        coefficientsBJF.put( coeff15.getName(), coeff15 );
        coefficientsBJF.put( coeff16.getName(), coeff16 );
        coefficientsBJF.put( coeff17.getName(), coeff17 );
        coefficientsBJF.put( coeff18.getName(), coeff18 );
        coefficientsBJF.put( coeff19.getName(), coeff19 );

        coefficientsBJF.put( coeff20.getName(), coeff20 );
        coefficientsBJF.put( coeff21.getName(), coeff21 );
        coefficientsBJF.put( coeff22.getName(), coeff22 );
        coefficientsBJF.put( coeff23.getName(), coeff23 );
        coefficientsBJF.put( coeff24.getName(), coeff24 );
        coefficientsBJF.put( coeff25.getName(), coeff25 );
        coefficientsBJF.put( coeff26.getName(), coeff26 );
        coefficientsBJF.put( coeff27.getName(), coeff27 );
        coefficientsBJF.put( coeff28.getName(), coeff28 );
        coefficientsBJF.put( coeff29.getName(), coeff29 );

        coefficientsBJF.put( coeff30.getName(), coeff30 );
        coefficientsBJF.put( coeff31.getName(), coeff31 );
        coefficientsBJF.put( coeff32.getName(), coeff32 );
        coefficientsBJF.put( coeff33.getName(), coeff33 );
        coefficientsBJF.put( coeff34.getName(), coeff34 );
        coefficientsBJF.put( coeff35.getName(), coeff35 );
        coefficientsBJF.put( coeff36.getName(), coeff36 );
        coefficientsBJF.put( coeff37.getName(), coeff37 );
        coefficientsBJF.put( coeff38.getName(), coeff38 );
        coefficientsBJF.put( coeff39.getName(), coeff39 );

        coefficientsBJF.put( coeff40.getName(), coeff40 );
        coefficientsBJF.put( coeff41.getName(), coeff41 );
        coefficientsBJF.put( coeff42.getName(), coeff42 );
        coefficientsBJF.put( coeff43.getName(), coeff43 );
        coefficientsBJF.put( coeff44.getName(), coeff44 );
        coefficientsBJF.put( coeff45.getName(), coeff45 );
        coefficientsBJF.put( coeff46.getName(), coeff46 );
*/
    }


    /**
     *  <b>Title:</b> BJF_1997_AttenRelCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the BJF_1997_AttenRel.  One instance of this class holds the set of
     *  coefficients for each period (one row of their table 8).<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W Rock
     * @created    February 27, 2002
     * @version    1.0
     */

    class BJF_1997_AttenRelCoefficients
             implements NamedObjectAPI {


        protected final static String C = "BJF_1997_AttenRelCoefficients";
        protected final static boolean D = false;

        protected String name;
        protected double period = -1;
        protected double b1all;
        protected double b1ss;
        protected double b1rv;
        protected double b2;
        protected double b3;
        protected double b5;
        protected double bv;
        protected int va;
        protected double h;
        protected double sigma1;
        protected double sigmaC;
        protected double sigmaR;
        protected double sigmaE;
        protected double sigmaLnY;

        /**
         *  Constructor for the BJF_1997_AttenRelCoefficients object
         *
         * @param  name  Description of the Parameter
         */
        public BJF_1997_AttenRelCoefficients( String name ) { this.name = name; }

        /**
         *  Constructor for the BJF_1997_AttenRelCoefficients object that sets all values at once
         *
         * @param  name  Description of the Parameter
         */
        public BJF_1997_AttenRelCoefficients( String name,  double period,
            double b1ss,  double b1rv,  double b1all, double b2,  double b3,
            double b5,  double bv,  int va,  double h, double sigma1, double sigmaC,
            double sigmaR, double sigmaE, double sigmaLnY
        ) {
            this.period = period;   this.b1ss = b1ss;
            this.b1rv = b1rv;       this.b1all = b1all;
            this.b2 = b2;           this.b3 = b3;
            this.b5 = b5;           this.bv = bv;
            this.va = va;           this.h = h;
            this.name = name;       this.sigma1 = sigma1;
            this.sigmaC = sigmaC;   this.sigmaR = sigmaR;
            this.sigmaE = sigmaE;   this.sigmaLnY = sigmaLnY;
        }

        /**
         *  Gets the name attribute of the BJF_1997_AttenRelCoefficients object
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
            b.append( "\n  Period = " + period );   b.append( "\n  b1all = " + b1all );
            b.append( "\n  b1ss = " + b1ss );       b.append( "\n  b1rv = " + b1rv );
            b.append( "\n  b2 = " + b2 );           b.append( "\n  b3 = " + b3 );
            b.append( "\n  b5 = " + b5 );           b.append( "\n  bv = " + bv );
            b.append( "\n va = " + va );            b.append( "\n  h = " + h );
            b.append( "\n  sigma1 = " + sigma1 );   b.append( "\n  sigmaC = " + sigmaC );
            b.append( "\n  sigmaR = " + sigmaR );   b.append( "\n  sigmaE = " + sigmaE );
            b.append( "\n  sigmaLnY = " + sigmaLnY );
            return b.toString();
        }
    }
}



