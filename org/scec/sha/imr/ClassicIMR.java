package org.scec.sha.imr;

import java.util.*;

import org.scec.calc.GaussianDistCalc;
import org.scec.data.*;
import org.scec.data.function.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;
import org.scec.util.*;

/**
 *  <p>
 *
 *  <b>Title:</b> ClassicIMR</p> <p>
 *
 *  <b>Description:</b> This subclass of IntensityMeasureRealtionship is an
 *  implementation of what's traditionally been called an "Attenuation
 *  Relationship".  The shaking is assumed to follow a Gaussian distribution,
 *  which is why there's a getMean() and a getStdDev() method in addition to
 *  the getExceedProbability() method of its parent class.  For this subclass of IMR
 *  the value field of the Intensity-Measure Parameter must be a Double.  As an
 *  abstract class, this implements the basic functionality common to all
 *  subclasses. Various parameters and their attributes (*_NAME, *_UNITS, *_INFO
 *  *_DEFAULT,*_MIN, *_MAX) are declared here to encourage the use of uniform
 *  conventions. Some of these parameters are instantiated in the various init*
 *  methods here, some are not.  If subclasses do not need these params, they can
 *  be ignored.  They can also be overridden if different attributes are desired.<p>
 *
 *  <b>pgaParam</b> - a WarningDoubleParameter representing the natural-log of the
 *  <b>Peak Ground Acceleration</b> Intensity-Measure parameter.
 *  This parameter is instantiated in its entirety in the
 *  initSupportedIntenistyMeasureParams() method here.<br>
 *  PGA_NAME = "PGA"<br>
 *  PGA_UNITS = "g"<br>
 *  PGA_INFO = "Peak Ground Acceleration"<br>
 *  PGA_MIN = 0<br>
 *  PGA_MAX = Double.MAX_VALUE<br>
 *  PGA_WARN_MIN - <i>see code</i><br>
 *  PGA_WARN_MAX - <i>see code</i><br>
 *  PGA_DEFAULT - <i>see code</i><p>
 *
 *  <b>pgvParam</b> - a WarningDoubleParameter representing the natural-log of the
 *  <b>Peak Ground Velocity</b> Intensity-Measure parameter.
 *  This parameter is not instantiated here due to limited use.<br>
 *  PGV_NAME = "PGV"<br>
 *  PGV_UNITS = "g"<br>
 *  PGV_INFO = "Peak Ground Acceleration"<br>
 *  PGV_MIN = 0<br>
 *  PGV_MAX = Double.MAX_VALUE<br>
 *  PGV_WARN_MIN - <i>see code</i><br>
 *  PGV_WARN_MAX - <i>see code</i><br>
 *  PGV_DEFAULT - <i>see code</i><p>
 *
 *  <b>saParam</b> - a WarningDoubleParameter representing the natural-log of the
 *  <b>Response Spectral Acceleration</b> Intensity-Measure Parameter
 *  This parameter is instantiated in its entirety in the
 *  initSupportedIntenistyMeasureParams() method here. However its periodParam independent-
 *  parameter must be created and added in subclasses.<br>
 *  SA_NAME = "SA"<br>
 *  SA_UNITS = "g"<br>
 *  SA_INFO = "Response Spectral Acceleration"<br>
 *  SA_MIN = 0<br>
 *  SA_MAX = Double.MAX_VALUE<br>
 *  SA_WARN_MIN - <i>see code</i><br>
 *  SA_WARN_MAX - <i>see code</i><br>
 *  SA_DEFAULT - <i>see code</i><p>
 *
 * <b>periodParam</b> - a DoubleDiscreteParameter representing the <b>Period</b> associated
 * with the Response-Spectral-Acceleration Parameter (periodParam is an
 * independentParameter of saParam).  This must be created and added to saParam in subclasses.<br>
 * PERIOD_NAME = "SA Period"<br>
 * PERIOD_UNITS = "sec"<br>
 * PERIOD_INFO = "Oscillator Period for SA"<br>
 * PERIOD_DEFAULT = new Double( 0 )<br>
 * <i>Constraint is created and added in sublclasses</i><p>
 *
 * <b>dampingParam</b> - a DoubleDiscreteParameter representing the <b>Damping</b> level
 * associated with the Response-Spectral-Acceleration Parameter (dampingParam is
 * an independentParameter of saParam).  This parameter is instantiated in its entirety
 * in the initSupportedIntenistyMeasureParams() method here.  This must be added to
 * saParam in subclasses.  If damping values besides 5% are available, add them in
 * subclass and then set to non-editable.<br>
 * DAMPING_NAME = "SA Damping"<br>
 * DAMPING_UNITS = " % "<br>
 * DAMPING_INFO = "Oscillator Damping for SA"<br>
 * DAMPING_DEFAULT = new Double( 5.0 )<br>
 * <i>Constraint is created and added in sublclasses</i><p>
 *
 * <b>magParam</b> - a WarningDoubleParameter representing the earthquake moment <b>Magnitude</b><br>
 * MAG_NAME = "Magnitude".  This parameter is created in the initProbEqkRuptureParams()
 * method here, but the warning constraint must be created and added in subclasses.<br>
 * <i>There are no units for Magnitude</i><br>
 * MAG_INFO = "Earthquake Moment Magnatude"<br>
 * MAG_DEFAULT - <i>see code</i><br>
 * MAG_MIN - <i>see code</i><br>
 * MAG_MAX - <i>see code</i><p>
 *
 * <b>componentParam</b> - a StringParameter representing the <b>Components</b> of shaking
 * that the IMR supports.<br>
 * COMPONENT_NAME = "Component"<br>
 * <i>There are no units for Component</i><br>
 * COMPONENT_DEFAULT = "Average Horizontal"<br>
 * COMPONENT_AVE_HORZ = "Average Horizontal"<br>
 * COMPONENT_RANDOM_HORZ = "Random Horizontal"<br>
 * COMPONENT_VERT = "Vertical";<br>
 * COMPONENT_INFO = "Component of shaking"<br>
 * <i>Constraint will be created and added in subclass</i><p>
 *
 * <b>vs30Param</b> - A WarningDoubleParameter representing the <b>average 30-meter shear-
 * wave velocity at the surface of a site</b>.  This parameter is created in the initSiteParams()
 * method here, but the warning constraint must be created and added in subclasses.<br>
 * VS30_NAME = "Vs30"<br>
 * VS30_UNITS = "m/sec"<br>
 * VS30_INFO = "Average 30 meter shear wave velocity at surface"<br>
 * VS30_DEFAULT - <i>see code</i><br>
 * VS30_MIN = - <i>see code</i><br>
 * VS30_MAX = - <i>see code</i><p>
 *
 * <b>stdDevTypeParam</b> - A StringParameter representing the various <b>types of standard
 * deviations</b> that an IMR might support; "Total" is the most common (see description
 * below for more details).<br>
 * STD_DEV_TYPE_NAME = "Std Dev Type"<br>
 * STD_DEV_TYPE_INFO = "Type of Standard Deviation"<br>
 * STD_DEV_TYPE_DEFAULT = "Total"<br>
 * STD_DEV_TYPE_TOTAL = "Total"<br>
 * STD_DEV_TYPE_INTER = "Inter-Event"<br>
 * STD_DEV_TYPE_NONE = "None (zero)"<br>
 * STD_DEV_TYPE_INTRA = "Intra-Event"<p>
 *
 * <b>fltTypeParam</b> - A StringParameter representing <b>different styles of faulting</b>;
 * options are specified in subclasses because nomenclature varies.<br>
 * FLT_TYPE_NAME = "Fault Type"<br>
 * <i>No units for this one</i><br>
 * FLT_TYPE_INFO = "Style of faulting"<p>
 *
 * <b>sigmaTruncTypeParam</b> - A StringParameter representing the <b>various types of truncation
 * available for the Gaussian probability distribution</b>; "1 Sided" is an upper
 * truncation (zero probabilities above); see <i>sigmaTruncLevelParam</i> below for more info.<br>
 * SIGMA_TRUNC_TYPE_NAME = "Gaussian Distribution Truncation"<br>
 * SIGMA_TRUNC_TYPE_INFO = "Type of distribution truncation to apply when computing exceedance probabilities"<br>
 * SIGMA_TRUNC_TYPE_NONE = "None"<br>
 * SIGMA_TRUNC_TYPE_1SIDED = "1 Sided"<br>
 * SIGMA_TRUNC_TYPE_2SIDED = "2 Sided"<br>
 * SIGMA_TRUNC_TYPE_DEFAULT = "None"<p>
 *
 * <b>sigmaTruncLevelParam</b> - A DoubleParameter defining <b>where the Gaussian
 * distribution is truncated </b> (this is ignored if sigmaTruncTypeParam is "None");
 * This level is defined in terms of some number of standard deviations above
 * (and perhaps below) the mean.<br>
 * SIGMA_TRUNC_LEVEL_NAME = "Truncation Level"<br>
 * SIGMA_TRUNC_LEVEL_UNITS = "Std Dev"<br>
 * SIGMA_TRUNC_LEVEL_INFO = "The number of standard deviations, from the mean, where truncation occurs"<br>
 * SIGMA_TRUNC_LEVEL_DEFAULT = 2.0 <br>
 * SIGMA_TRUNC_LEVEL_MIN = Double.MIN_VALUE<br>
 * SIGMA_TRUNC_LEVEL_MAX = Double.MAX_VALUE<p>
 *
 *
 *
 *  </p> Note: SWR - SetAll() is not truly robust in case of error. <p>
 *
 *  <b>Copyright:</b> Copyright (c) 2002</p> <p>
 *
 *  <b>Company:</b> </p>
 *
 * @author     Steven W. Rock & Edward H. Field
 * @created    April 1st, 2002
 * @version    1.0
 */

 /* Note: SWR - For the sake of simplicity when calling getMean(), getStdDev() and
 * getExceedenceProbability all parameters are looked up and/or calculated
 * within the function call. This may not provide the best performance when calling
 * these functions many times from within a loop where only one parameter is
 * updated. This was done intentionally so that scientists who implement other
 * IMRs can use this one as a template without having to learn the complexities
 * of advanced programming techniques. <p>
 *
 * An alternative approach that is elegant and the most optimised is to use java's
 * event based notification. To do this you need three classes. First you need a
 * ParameterChangeEvent which records the old value, new value, and a reference
 * to the source object which generated the change event. Secondly you need a
 * ParameterChangeListener Interface that has one function: valueChanged(
 * ParameterChangeEvent). Thirdly you need to update the ParameterAPI to allow
 * registration and removal of ParameterChangeListeners, and add a fireParameterChange(
 * ParameterChangeEvent ) function that is called whenever the value is updated.
 * In fact, this is exactly how the ParameterEditors all work. Look at the
 * org.scec.param.editor for a reference implementation. <p>
 *
 * This helps us in that this IMR would then implement the ParameterChangeListener
 * interface and be notified whenever Parameters are changed in the GUI.
 * The corresponding dependent variables can then be updated with the new Parameter
 * value. This calculation would not have to be done in the getMean() and other functions.
 * This is the best optimized solution and recorded here just for reference. <p>
 */

public abstract class ClassicIMR
         extends IntensityMeasureRelationship
         implements ClassicIMRAPI {

    /**
     *  Classname constant used for debugging statements
     */
    protected final static String C = "ClassicIMR";

    /**
     *  Prints out debugging statements if true
     */
    protected final static boolean D = false;


    /**
     * PGA parameter, reserved for the natural log of the "Peak Ground Acceleration"
     * Intensity-Measure Parameter that most subclasses will support; all of the "PGA_*"
     * class variables relate to this Parameter. This parameter is instantiated
     * in its entirety in the initSupportedIntenistyMeasureParams() method here.
     */
    protected  WarningDoubleParameter pgaParam = null;
    protected final static String PGA_NAME = "PGA";
    protected final static String PGA_UNITS = "g";
    protected final static Double PGA_DEFAULT = new Double( Math.log( 0.1 ) );
    protected final static String PGA_INFO = "Peak Ground Acceleration";
    protected final static Double PGA_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double PGA_MAX = new Double( Double.MAX_VALUE );
    protected final static Double PGA_WARN_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double PGA_WARN_MAX = new Double( Math.log( 2.0 ) );





    /**
     * PGV parameter, reserved for the natural log of the "Peak Ground Velocity" Intensity-
     * Measure Parameter that most subclasses will support; all of the "PGV_*"
     * class variables relate to this Parameter.This parameter is not instantiated
     * here due to limited use.
     */
    protected  WarningDoubleParameter pgvParam = null;
    protected final static String PGV_NAME = "PGV";
    protected final static String PGV_UNITS = "cm/sec";
    protected final static Double PGV_DEFAULT = new Double( Math.log( 0.1 ) );
    protected final static String PGV_INFO = "Peak Ground Velocity";
    protected final static Double PGV_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double PGV_MAX = new Double( Math.log(500) );
    protected final static Double PGV_WARN_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double PGV_WARN_MAX = new Double( Math.log(500) );


    /**
     * SA parameter, reserved for the the natural log of "Spectral Acceleration"
     * Intensity-Measure Parameter that most subclasses will support; all of the
     * "SA_*" class variables relate to this Parameter.  Note also that periodParam and
     * dampingParam are internal independentParameters of saParam. This parameter
     * is instantiated in its entirety in the initSupportedIntenistyMeasureParams()
     * method here. However its periodParam independent-parameter must be created
     * and added in subclasses.
     */
    protected  WarningDoubleParameter saParam = null;
    protected final static String SA_NAME = "SA";
    protected final static String SA_UNITS = "g";
    protected final static Double SA_DEFAULT = new Double( Math.log(0.5) );
    protected final static String SA_INFO = "Response Spectral Acceleration";
    protected final static Double SA_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double SA_MAX = new Double( Math.log(4) );
    protected final static Double SA_WARN_MIN = new Double( Math.log(Double.MIN_VALUE) );
    protected final static Double SA_WARN_MAX = new Double( Math.log(2) );

    /**
     * Period parameter, reserved for the oscillator period that Spectral
     * Acceleration (saParam) depends on (periodParam is an independentParameter
     * of saParam); all of the "PERIOD_*" class variables relate to this
     * Parameter.  This parameter is created and added to saParam in subclasses.
     */
    protected  DoubleDiscreteParameter periodParam = null;
    protected final static String PERIOD_NAME = "SA Period";
    protected final static String PERIOD_UNITS = "sec";
    protected final static Double PERIOD_DEFAULT = new Double( 0 );
    protected final static String PERIOD_INFO = "Oscillator Period for SA";
    // The constraint is created and added in the subclass.


    /**
     * Damping parameter, reserved for the damping level that Spectral
     * Acceleration (saParam) depends on (dampingParam is an independentParameter
     * of saParam); all of the "DAMPING_*" class variables relate to this
     * Parameter.  This parameter is instantiated in its entirety in the
     * initSupportedIntenistyMeasureParams() method here.  This must be added to
     * saParam in subclasses.  If damping values besides 5% are available, add
     * them in subclass and then set to non-editable.
     */
    protected DoubleDiscreteParameter dampingParam = null;
    protected DoubleDiscreteConstraint dampingConstraint = null;
    protected final static String DAMPING_NAME = "SA Damping";
    protected final static String DAMPING_UNITS = " % ";
    protected final static Double DAMPING_DEFAULT = new Double( 5 );
    protected final static String DAMPING_INFO = "Oscillator Damping for SA";
    //The constraint is created and added in the subclass; most will support only this default value


    /**
     * Magnitude parameter, reserved for representing moment magnitude in all
     * subclasses; all of the "MAG_*" class variables relate to this magParam
     * object.  This parameter is created in the initProbEqkRuptureParams()
     * method here, but the warning constraint must be created and added in subclasses.
     */
    protected WarningDoubleParameter magParam = null;
    protected final static String MAG_NAME = "Magnitude";
    // There are no units for Magnitude
    protected final static String MAG_INFO = "Earthquake Moment Magnatude";
    protected final static Double MAG_DEFAULT = new Double(5.5);
    protected final static Double MAG_MIN = new Double(0);
    protected final static Double MAG_MAX = new Double(10);
    // warning values are set in subclasses


    /**
     * Component Parameter, reserved for representing the component of shaking
     * (in 3D space); all of the "COMPONENT_*" class variables relate to this
     * Parameter.
     */
    protected StringParameter componentParam = null;
    protected final static String COMPONENT_NAME = "Component";
    // not units for Component
    protected final static String COMPONENT_DEFAULT = "Average Horizontal";
    protected final static String COMPONENT_AVE_HORZ = "Average Horizontal";
    protected final static String COMPONENT_RANDOM_HORZ = "Random Horizontal";
    protected final static String COMPONENT_VERT = "Vertical";
    protected final static String COMPONENT_INFO = "Component of shaking";
    // constraint will be created and added in subclass


    /**
     * Vs30 Parameter, reserved for representing the average shear-wave velocity
     * in the upper 30 meters of a site (a commonly used parameter); all of the
     * "VS30_*" class variables relate to this Parameter.  This parameter is
     * created in the initSiteParams() method here, but the warning constraint
     * must be created and added in subclasses.
     */
    protected WarningDoubleParameter vs30Param = null;
    protected final static String VS30_NAME = "Vs30";
    protected final static String VS30_UNITS = "m/sec";
    protected final static String VS30_INFO = "Average 30 meter shear wave velocity at surface";
    protected final static Double VS30_DEFAULT = new Double( "360" );
    protected final static Double VS30_MIN = new Double(0.0);
    protected final static Double VS30_MAX = new Double(5000.0);
    // warning values set in subclasses


    /**
     * StdDevType, a StringParameter, reserved for representing the various types of
     * standard deviations that various IMR might support:  "InterEvent" is
     * the event to event variability, "Intra-Event" is the variability within
     * an event, and "Total" (the most common) is the other two two added in quadrature.
     * Other options are defined in some subclasses.
     */
    protected StringParameter stdDevTypeParam = null;
    protected final static String STD_DEV_TYPE_NAME = "Std Dev Type";
    // No units for this one
    protected final static String STD_DEV_TYPE_INFO = "Type of Standard Deviation";
    protected final static String STD_DEV_TYPE_DEFAULT = "Total";
    protected final static String STD_DEV_TYPE_TOTAL = "Total";
    protected final static String STD_DEV_TYPE_INTER = "Inter-Event";
    protected final static String STD_DEV_TYPE_INTRA = "Intra-Event";
    protected final static String STD_DEV_TYPE_NONE = "None (zero)";


    /**
     * FltTypeParam, a StringParameter reserved for representing different
     * styles of faulting.  The options are not specified here because
     * nomenclature generally differs among subclasses.
     */
    protected StringParameter fltTypeParam = null;
    protected final static String FLT_TYPE_NAME = "Fault Type";
    // No units for this one
    protected final static String FLT_TYPE_INFO = "Style of faulting";

    /**
     * SigmaTruncTypeParam, a StringParameter that represents the type of
     * probability distribution truncation.
     */
    protected StringParameter sigmaTruncTypeParam = null;
    public final static String SIGMA_TRUNC_TYPE_NAME = "Gaussian Truncation";
    protected final static String SIGMA_TRUNC_TYPE_INFO = "Type of distribution truncation to apply when computing exceedance probabilities";
    protected final static String SIGMA_TRUNC_TYPE_NONE = "None";
    protected final static String SIGMA_TRUNC_TYPE_1SIDED = "1 Sided";
    protected final static String SIGMA_TRUNC_TYPE_2SIDED = "2 Sided";
    protected final static String SIGMA_TRUNC_TYPE_DEFAULT = "None";

    /**
     * SigmaTruncLevelParam, a DoubleParameter that represents where truncation occurs
     * on the Gaussian distribution (in units of standard deviation, relative to the mean).
     */
    protected DoubleParameter sigmaTruncLevelParam = null;
    public final static String SIGMA_TRUNC_LEVEL_NAME = "Truncation Level";
    protected final static String SIGMA_TRUNC_LEVEL_UNITS = "Std Dev";
    protected final static String SIGMA_TRUNC_LEVEL_INFO = "The number of standard deviations, from the mean, where truncation occurs";
    protected final static Double SIGMA_TRUNC_LEVEL_DEFAULT = new Double(2.0);
    protected final static Double SIGMA_TRUNC_LEVEL_MIN = new Double(Double.MIN_VALUE);
    protected final static Double SIGMA_TRUNC_LEVEL_MAX = new Double(Double.MAX_VALUE);


    /**
     * Exceed Prob parameter, used only to store the exceedance probability to be
     * used by the getIML_AtExceedProb() method.  Note that calling the
     * getExceedProbability() DOES NOT store the value in this parameter.
     */
    protected  DoubleParameter exceedProbParam = null;
    protected final static String EXCEED_PROB_NAME = "Exceed. Prob.";
    protected final static Double EXCEED_PROB_DEFAULT = new Double( 0.5 );
    protected final static String EXCEED_PROB_INFO = "Exceedance Probability";
    protected final static Double EXCEED_PROB_MIN = new Double( 1.0e-4 );
    protected final static Double EXCEED_PROB_MAX = new Double( 1.0 - 1e-4 );



    /**
     *  Common error message = "Not all parameters have been set"
     */
    protected final static String ERR = "Not all parameters have been set";


    /**
     *  List of all Parameters that the mean calculation depends upon, except for
     *  the intensity-measure related parameters (type/level) and any independentdent parameters
     *  they contain.
     */
    protected ParameterList meanIndependentParams = new ParameterList();


    /**
     *  List of all Parameters that the stdDev calculation depends upon, except for
     *  the intensity-measure related parameters (type/level) and any independentdent parameters
     *  they contain.
     */
    protected ParameterList stdDevIndependentParams = new ParameterList();


    /**
     *  List of all Parameters that the exceed. prob. calculation depends upon, except for
     *  the intensity-measure related parameters (type/level) and any independentdent parameters
     *  they contain.  Note that this and its iterator method could be applied in the parent class.
     */
    protected ParameterList exceedProbIndependentParams = new ParameterList();


    /**
     *  List of all Parameters that the IML at exceed. prob. calculation depends upon, except for
     *  the intensity-measure related parameters (type/level) and any independentdent parameters
     *  they contain.
     */
    protected ParameterList imlAtExceedProbIndependentParams = new ParameterList();


    /**
     *  Constructor for the ClassicIMR object - subclasses should execute the
     *  various init*() methods (in proper order)
     */
    public ClassicIMR() {
        super();
    }

    /**
     *  Sets the probEqkRupture, site, and intensityMeasure objects
     *  simultaneously.<p>
     *
     *  SWR: Warning - this function doesn't provide full rollback in case of
     *  failure. There are 4 method calls that sets parameters with new values.
     *  If one function fails, the previous functions effects are not undone,
     *  i.e. the transaction is not handled gracefully.<p>
     *
     *  This will take alot of design and work so it is held off for now until
     *  it is decided that it is needed.<p>
     *
     *
     *
     * @param  probEqkRupture      The new all value
     * @param  site                     The new all value
     * @param  intensityMeasure         The new all value
     * @exception  ParameterException   Description of the Exception
     * @exception  IMRException         Description of the Exception
     * @exception  ConstraintException  Description of the Exception
     */
    public void setAll(
            ProbEqkRupture probEqkRupture,
            Site site,
            ParameterAPI intensityMeasure
             )
             throws ParameterException, IMRException, ConstraintException {
        super.setAll( probEqkRupture, site, intensityMeasure );
    }


    /**
     *  Sets the value of the currently selected intensityMeasure (if the
     *  value is allowed); this will reject anything that is not a Double.
     *
     * @param  iml                     The new intensityMeasureLevel value
     * @exception  ParameterException  Description of the Exception
     */
    public void setIntensityMeasureLevel( Object iml ) throws ParameterException {

        if ( !( iml instanceof Double ) )
            throw new ParameterException( C +
                    ": setIntensityMeasureLevel(): Object not a DoubleParameter, unable to set." );

        setIntensityMeasureLevel( ( Double ) iml );
    }


    /**
     *  Sets the value of the selected intensityMeasure;
     *
     * @param  iml                     The new intensityMeasureLevel value
     * @exception  ParameterException  Description of the Exception
     */
    public void setIntensityMeasureLevel( Double iml ) throws ParameterException {

        if ( im == null )
            throw new ParameterException( C +
                    ": setIntensityMeasureLevel(): Intensity Measure is null, unable to set."
                     );

        this.im.setValue( iml );
    }


    /**
     *  Calculates the value of each propagation effect parameter from the
     *  current Site and ProbEqkRupture objects. <P>
     */
    protected abstract void setPropagationEffectParams();


    /**
     *  Returns a handle to the component parameter.
     *
     * @return    The componentParameter value
     */
    public ParameterAPI getComponentParam() {
        return componentParam;
    }



    /**
     *  This calculates the probability that the intensity-measure level
     *  (the value in the Intensity-Measure Parameter) will be exceeded
     *  given the mean and stdDev computed from current independent parameter
     *  values.  Note that the answer is not stored in the internally held
     *  exceedProbParam (this latter param is used only for the
     *  getIML_AtExceedProb() method).
     *
     * @return                         The exceedProbability value
     * @exception  ParameterException  Description of the Exception
     * @exception  IMRException        Description of the Exception
     */
    public double getExceedProbability() throws ParameterException, IMRException {

        // is this really needed; is it slowing us down?
        if ( ( im == null ) || ( im.getValue() == null ) )
            throw new ParameterException( C +
                    ": getExceedProbability(): " + "Intensity measure or value is null, unable to run this calculation."
                     );

        // Calculate the standardized random variable
        double iml = ( ( Double ) ( ( ParameterAPI ) im ).getValue() ).doubleValue();
        double stdDev = getStdDev();
        double mean = getMean();

        if( stdDev != 0) {
           double stRndVar = (iml-mean)/stdDev;
            // compute exceedance probability based on truncation type
            if ( sigmaTruncTypeParam.getValue().equals( SIGMA_TRUNC_TYPE_NONE ) )
                return GaussianDistCalc.getExceedProb(stRndVar);
            else {
                double numSig = ( ( Double ) ( ( ParameterAPI ) sigmaTruncLevelParam ).getValue() ).doubleValue();
                if ( sigmaTruncTypeParam.getValue().equals( SIGMA_TRUNC_TYPE_1SIDED ) )
                    return GaussianDistCalc.getExceedProb(stRndVar, 1, numSig);
                else
                   return GaussianDistCalc.getExceedProb(stRndVar, 2, numSig);
           }
        }
        else {
            if( iml > mean )  return 0;
            else              return 1;
        }
    }


    /**
     *  This fills in the exceedance probability for multiple intensityMeasure
     *  levels (often called a "hazard curve"); the levels are obtained from
     *  the X values of the input function, and Y values are filled in with the
     *  asociated exceedance probabilities.
     *
     * @param  intensityMeasureLevels  Description of the Parameter
     * @return                         The exceed Probability values put in a
     *      DiscretizedFunction
     * @exception  ParameterException  Description of the Exception
     */
    public DiscretizedFuncAPI getExceedProbabilities(
            DiscretizedFuncAPI intensityMeasureLevels
             ) throws ParameterException {

        if ( im == null )
            throw new ParameterException( C +
                    ": getExceedProbabilities(): " +
                    "No Intensity Measure has been set yet, unable to calculate the Exceedence Probability."
                     );

        Double iml;
        Iterator it = intensityMeasureLevels.getPointsIterator();
        while ( it.hasNext() ) {

            DataPoint2D point = ( DataPoint2D ) it.next();
            iml = new Double(point.getX());
            this.im.setValue( iml );
            point.setY(getExceedProbability());

        }

        return intensityMeasureLevels;
    }



    /**
     *  This calculates the intensity-measure level associated with probability
     *  held by the exceedProbParam given the mean and standard deviation.  Note
     *  that this does not store the answer in the value of the internally held
     *  intensity-measure parameter.
     *
     * @return                         The intensity-measure level
     * @exception  ParameterException  Description of the Exception
     */
    public double getIML_AtExceedProb() throws ParameterException {

        if ( ( exceedProbParam == null ) || ( exceedProbParam.getValue() == null ) )
            throw new ParameterException( C +
                    ": getExceedProbability(): " + "exceedProbParam or its value is null, unable to run this calculation."
                     );

        double exceedProb = ( ( Double ) ( ( ParameterAPI ) exceedProbParam ).getValue() ).doubleValue();
        double stRndVar;

        // compute the iml from exceed probability based on truncation type
        if ( sigmaTruncTypeParam.getValue().equals( SIGMA_TRUNC_TYPE_NONE ) )
            stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 0, 0, 1e-5);
        else {
            double numSig = ( ( Double ) ( ( ParameterAPI ) sigmaTruncLevelParam ).getValue() ).doubleValue();
            if ( sigmaTruncTypeParam.getValue().equals( SIGMA_TRUNC_TYPE_1SIDED ) )
                stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 1, numSig, 1e-5);
            else
                stRndVar = GaussianDistCalc.getStandRandVar(exceedProb, 2, numSig, 1e-5);
        }
        return getMean() + stRndVar*getStdDev();
    }


    /**
     *  Returns an iterator over all the Parameters that the Mean calculation depends upon.
     *  (not including the intensity-measure related paramters and their internal,
     *  independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getMeanIndependentParamsIterator() {
        return meanIndependentParams.getParametersIterator();
    }

    /**
     *  Returns an iterator over all the Parameters that the StdDev calculation depends upon
     *  (not including the intensity-measure related paramters and their internal,
     *  independent parameters).
     *
     * @return    The Independent Parameters Iterator
     */
    public ListIterator getStdDevIndependentParamsIterator() {
        return stdDevIndependentParams.getParametersIterator();
    }


    /**
     *  Returns an iterator over all the Parameters that the exceedProb calculation
     *  depends upon (not including the intensity-measure related paramters and
     *  their internal, independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getExceedProbIndependentParamsIterator() {
        return exceedProbIndependentParams.getParametersIterator();
    }

    /**
     *  Returns an iterator over all the Parameters that the IML-at-exceed-
     *  probability calculation depends upon. (not including the intensity-measure
     *  related paramters and their internal, independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getIML_AtExceedProbIndependentParamsIterator() {
        return imlAtExceedProbIndependentParams.getParametersIterator();
    }



    /**
     *  This function populates the hashtable of coefficients (if there
     *  are any for the subclass).  The hashtable keys vary among
     *  subclasses, but are generally the intensityMeasure name plus
     *  other strings (separated by "/") that distinguish coefficient sets.
     */
    protected abstract void initCoefficients();

    /**
     *  This creates the following parameters for subclasses: saParam; dampingParam,
     *  and pgaParam; the periodParam (one of saParam's independent parameters) is
     *  created and added in subclasses.  All these parameters are added to the
     *  supportedIMParams list in subclasses.  Note: this must generally be executed
     *  after
     *  the initCoefficients() method.
     *  lists:<br>
     *
     */
    protected void initSupportedIntensityMeasureParams() {

        // Create SA Parameter:
        DoubleConstraint saConstraint = new DoubleConstraint(SA_MIN, SA_MAX);
        saConstraint.setNonEditable();
        saParam = new WarningDoubleParameter( SA_NAME, saConstraint, SA_UNITS);
        saParam.setInfo( SA_INFO );
        DoubleConstraint warn1 = new DoubleConstraint(SA_WARN_MIN, SA_WARN_MAX);
        warn1.setNonEditable();
        saParam.setWarningConstraint(warn1);

        // Damping-level parameter for SA
        // (overide this in subclass of other damping levels are available)
        dampingConstraint = new DoubleDiscreteConstraint();
        dampingConstraint.addDouble(DAMPING_DEFAULT);
        // leave constrain editable in case subclasses want to add other options
        dampingParam = new DoubleDiscreteParameter( DAMPING_NAME, dampingConstraint, DAMPING_UNITS, DAMPING_DEFAULT );
        dampingParam.setInfo(DAMPING_INFO);
        dampingParam.setNonEditable();

        // Create PGA Parameter
        DoubleConstraint pgaConstraint = new DoubleConstraint(PGA_MIN, PGA_MAX);
        pgaConstraint.setNonEditable();
        pgaParam = new WarningDoubleParameter( PGA_NAME, pgaConstraint, PGA_UNITS);
        pgaParam.setInfo( PGA_INFO );
        DoubleConstraint warn2 = new DoubleConstraint(PGA_WARN_MIN, PGA_WARN_MAX);
        warn2.setNonEditable();
        pgaParam.setWarningConstraint(warn2);
        pgaParam.setNonEditable();

    }

    /**
     *  Creates the Vs30 parameter for subclasses that use it; subclasses must
     *  create and added the warning constraint and add this parameter to the
     *  siteParams list; subclasses that don't use this vs30Param can simply not
     *  call this method.
     *  <br>
     *
     */
    protected void initSiteParams() {

         // create Vs30Param:
        DoubleConstraint vs30Constraint = new DoubleConstraint(VS30_MIN, VS30_MAX);
        vs30Constraint.setNonEditable();
        vs30Param = new WarningDoubleParameter( VS30_NAME, vs30Constraint, VS30_UNITS);
        vs30Param.setInfo( VS30_INFO );

    }

    /**
     *  Creates the following potential-Earthquake Parameters for subclasses: magParam
     *  (moment Magnitude parameter).  Warning constraints must be created and added
     *  in subclasses. This parameter is also added to the probEqkRuptureParams list
     *  in subclasses.<br>
     *
     */
    protected void initProbEqkRuptureParams() {

        // Moment Magnitude Parameter:
        DoubleConstraint magConstraint = new DoubleConstraint(MAG_MIN, MAG_MAX);
        magConstraint.setNonEditable();
        magParam = new WarningDoubleParameter( MAG_NAME, magConstraint);
        magParam.setInfo( MAG_INFO );

        // Warning constraint is created and added in subclass
    }


    /**
     *  Creates any Propagation-Effect related parameters and adds them to the
     *  propagationEffectParams list<br>
     *
     */
    protected abstract void initPropagationEffectParams();

    /**
     *  Creates other Parameters that the mean or stdDev depends upon,
     *  such as the Component or StdDevType parameters.
     *
     */
    protected void initOtherParams() {

        // Sigma truncation type parameter:
        StringConstraint sigmaTruncTypeConstraint = new StringConstraint();
        sigmaTruncTypeConstraint.addString( SIGMA_TRUNC_TYPE_NONE );
        sigmaTruncTypeConstraint.addString( SIGMA_TRUNC_TYPE_1SIDED );
        sigmaTruncTypeConstraint.addString( SIGMA_TRUNC_TYPE_2SIDED );
        sigmaTruncTypeConstraint.setNonEditable();
        sigmaTruncTypeParam = new StringParameter( SIGMA_TRUNC_TYPE_NAME, sigmaTruncTypeConstraint, SIGMA_TRUNC_TYPE_DEFAULT);
        sigmaTruncTypeParam.setInfo( SIGMA_TRUNC_TYPE_INFO );
        sigmaTruncTypeParam.setNonEditable();

        // Sigma truncation level parameter:
        DoubleConstraint sigmaTruncLevelConstraint = new DoubleConstraint(SIGMA_TRUNC_LEVEL_MIN, SIGMA_TRUNC_LEVEL_MAX);
        sigmaTruncLevelConstraint.setNonEditable();
        sigmaTruncLevelParam = new DoubleParameter( SIGMA_TRUNC_LEVEL_NAME, sigmaTruncLevelConstraint, SIGMA_TRUNC_LEVEL_UNITS, SIGMA_TRUNC_LEVEL_DEFAULT);
        sigmaTruncLevelParam.setInfo( SIGMA_TRUNC_LEVEL_INFO );
        sigmaTruncLevelParam.setNonEditable();

        /* Exceed Propability Parameter used only by the getIML_AtExceedProb() method.
           The attributes are hard-coded here because they won't change and do not
           generally need to be known by others.
        */
        exceedProbParam = new DoubleParameter( EXCEED_PROB_NAME, EXCEED_PROB_MIN, EXCEED_PROB_MAX, EXCEED_PROB_DEFAULT);
        exceedProbParam.setInfo( EXCEED_PROB_INFO );
        exceedProbParam.setNonEditable();

        // Put parameters in the otherParams list:
        otherParams.clear();

        otherParams.addParameter( sigmaTruncTypeParam );
        otherParams.addParameter( sigmaTruncLevelParam );
        otherParams.addParameter( exceedProbParam );


    }


}
