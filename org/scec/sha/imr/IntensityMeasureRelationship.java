package org.scec.sha.imr;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;


/**
 *  <b>Title:</b> IntensityMeasureRelationship<p>
 *
 *  <b>Description:</b> Abstract base class for Intensity Measure Relationship (IMR).
 *  All IMRs compute the probability of exceeding a particular shaking level (specified
 *  by an intenisty-measure Parameter) given a Site and ProbEqkRupture object.
 *  Subclasses will implement specific types of IMRs (e.g., AttenuationRelationship).
 *  This abstract IMR class also contains seperate parameterList objects for the
 *  site, potential-earthquake, and propagation-effect related parameters, as well
 *  as a list of "other" parameters that don't fit into those three categories.
 *  This class also contains a list of supported intensity-measure parameters (which
 *  may have internal independent parameters). These five lists combined (siteParams,
 *  probEqkRuptureParams, propagationEffectParams, supportedIMParams, and otherParams)
 *  constitutes the complete list of parameters that the exceedance probability depends
 *  upon.  The only other paramter is exceedProbParam, which is used to compute the
 *  IML at a particular probability in subclasses that support the getIML_AtExceedProb()
 *  method. <p>
 *
 * @author     Edward H. Field & Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 * @see        IntensityMeasureRelationshipAPI
 */

public abstract class IntensityMeasureRelationship
    implements IntensityMeasureRelationshipAPI
{

    private final static String NAME = "Intensity Measure Relationship";

    /** Classname constant used for debugging statements */
    protected final static String C = "IntensityMeasureRelationship";

    /** Prints out debugging statements if true */
    protected final static boolean D = false;

    /**
     * Exceed Prob parameter, used only to store the exceedance probability to be
     * used by the getIML_AtExceedProb() method of subclasses (if a subclass supports
     * this method).  Note that calling the getExceedProbability() does not store the
     * value in this parameter.
     */
    protected  DoubleParameter exceedProbParam = null;
    public final static String EXCEED_PROB_NAME = "Exceed. Prob.";
    protected final static Double EXCEED_PROB_DEFAULT = new Double( 0.5 );
    public final static String EXCEED_PROB_INFO = "Exceedance Probability";
    protected final static Double EXCEED_PROB_MIN = new Double( 1.0e-6 );
    protected final static Double EXCEED_PROB_MAX = new Double( 1.0 - 1e-6 );

    /** ParameterList of all Site parameters */
    protected ParameterList siteParams = new ParameterList();

    /** ParameterList of all ProbEqkRupture parameters */
    protected ParameterList probEqkRuptureParams = new ParameterList();

    /**
     * ParameterList of all Propagation-Effect parameters (this should perhaps
     * exist only in subclasses since not all IMRs will have these?)
     */
    protected ParameterList propagationEffectParams = new ParameterList();

    /** ParameterList of all supported Intensity Measure parameters */
    protected ParameterList supportedIMParams = new ParameterList();

    /**
     * ParameterList of other parameters that don't fit into above categories.
     * These are any parameters that the exceedance probability depends upon that is
     * not a supported IMT (or one of their independent parameters) and is not contained
     * in, or computed from, the site or eqkRutpure objects.  Note that this does not
     * include the exceedProbParam (which exceedance probability does not depend on).
     */
    protected ParameterList otherParams = new ParameterList();

    /** The current Site object (passing one in will set site-related parameters). */
    protected Site site;

    /** The current ProbEqkRupture object (passing one in will set Earthquake-
     *  Rupture related parameters.
     */
    protected ProbEqkRupture probEqkRupture;

    /**
     *  Intensity Measure.  This is a specification of the type of shaking one
     *  is concered about.  Its representation as a Parameter makes the
     *  specification quite general and flexible.  IMRs compute the probability
     *  of exceeding the "value" field of this im Parameter.
     */
    protected ParameterAPI im;



    /**
     *  No-Arg Constructor for the IntensityMeasureRelationship object. This only
     *  creates one parameter (exceedProbParam) used by some subclasses.
     */
    public IntensityMeasureRelationship() {

        exceedProbParam = new DoubleParameter( EXCEED_PROB_NAME, EXCEED_PROB_MIN, EXCEED_PROB_MAX, EXCEED_PROB_DEFAULT);
        exceedProbParam.setInfo( EXCEED_PROB_INFO );
        exceedProbParam.setNonEditable();

    }



    /**
     *  Returns name of the IntensityMeasureRelationship.
     *
     * @return    The name string
     */
    public String getName() { return NAME; }



    /**
     *  Sets the Site object as a reference to that passed in, and sets
     *  any internal site-related parameters that the IMR depends upon.
     *
     * @param  site  The new site object
     */
    public void setSite( Site site ) { this.site = site; }

    /**
     *  Returns a reference to the current Site object of the IMR
     *
     * @return    The site object
     */
    public Site getSite() { return site; }



    /**
     *  Returns a reference to the current probEqkRupture object in the IMR
     *
     * @return    The probEqkRupture object
     */
    public ProbEqkRupture getProbEqkRupture() { return probEqkRupture; }

    /**
     *  Sets the probEqkRupture object in the IMR as a reference
     *  to the one passed in, and sets any earthquake-rupture related
     *  parameters that the IMR depends upon.
     *
     * @param  probEqkRupture  The new probEqkRupture object
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) {
        this.probEqkRupture = probEqkRupture;
    }



    /**
     *  Returns the "value" object of the currently chosen Intensity-Measure
     *  Parameter.
     *
     * @return    The value field of the currently chosen intensityMeasure
     */
    public Object getIntensityMeasureLevel() { return im.getValue(); }

    /**
     *  Sets the value of the currently chosen Intensity-Measure Parameter.
     *  This is the value that the probability of exceedance is computed for.
     *
     * @param  iml  The new value for the intensityMeasure Parameter
     */
    public void setIntensityMeasureLevel( Object iml ) throws ParameterException {

        if( this.im == null) throw new ParameterException(C +
            ": setIntensityMeasureLevel(): " +
            "The Intensity Measure has not been set yet, unable to set the level."
        );

        im.setValue( iml );

    }


    /**
     *  Gets a reference to the currently chosen Intensity-Measure Parameter
     *  from the IMR.
     *
     * @return    The intensityMeasure Parameter
     */
    public ParameterAPI getIntensityMeasure() { return im; }


    /**
     *  Sets the intensityMeasure parameter, not as a  pointer to that passed in,
     *  but by finding the internally held one with the same name and then setting
     *  its value (and the value of any of its independent parameters) to be equal
     *  to that passed in.  PROBLEM: THE PRESENT IMPLEMENTATION ASSUMES THAT ALL THE
     *  DEPENDENT PARAMETERS ARE OF TYPE DOUBLE - WE NEED TO RELAX THIS.
     *
     * @param  intensityMeasure  The new intensityMeasure Parameter
     */
    public void setIntensityMeasure( ParameterAPI intensityMeasure ) throws ParameterException, ConstraintException {

        if( isIntensityMeasureSupported( intensityMeasure ) ) {
            setIntensityMeasure( intensityMeasure.getName() );
            ListIterator it=((DependentParameterAPI)intensityMeasure).getIndependentParametersIterator();
            while(it.hasNext()){
              ParameterAPI param = (ParameterAPI)it.next();
              getParameter(param.getName()).setValue(new Double((String)param.getValue()));
            }
        }
        else throw new ParameterException("This im is not supported, name = " + intensityMeasure.getName() );
    }


    /**
     *  This sets the intensityMeasure parameter as that which has the name
     *  passed in; no value (level) is set, nor are any of the IM's independent
     *  parameters set (since it's only given the name).
     *
     * @param  intensityMeasure  The new intensityMeasureParameter name
     */
    public void setIntensityMeasure( String intensityMeasureName ) throws ParameterException, ConstraintException {

        if( supportedIMParams.containsParameter( intensityMeasureName ) ) {
            im = supportedIMParams.getParameter( intensityMeasureName );
        }
        else throw new ParameterException("This im is not supported, name = " + intensityMeasureName );
    }



    /**
     *  Checks if the Parameter is a supported intensity-Measure (checking
     *  both the name and value, as well as any dependent parameters
     *  (names and values) of the IM).  PROBLEM: THE VALUE OF THE IM IS NOT CHECKED,
     *  AND THIS IMPLEMENTATION ASSUMES THAT ALL THE DEPENDENT PARAMETERS ARE DOUBLE
     *  PARAMETERS - WE NEED TO FIX THE FORMER AND RELAX THAT LATTER.
     *
     * @param  intensityMeasure  Description of the Parameter
     * @return                   True if this is a supported IMT
     */
    public boolean isIntensityMeasureSupported( ParameterAPI intensityMeasure ) {

        if ( supportedIMParams.containsParameter( intensityMeasure ) ) {
         //   ParameterAPI param = supportedIMParams.getParameter( intensityMeasure.getName() );
            ListIterator it=((DependentParameterAPI)intensityMeasure).getIndependentParametersIterator();
            while(it.hasNext()){
              ParameterAPI param = (ParameterAPI)it.next();
              if(getParameter(param.getName()).isAllowed(new Double((String)param.getValue())))
                 continue;
               else
                 return false;
            }
            return true;
        }
        else
            return false;
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
     * @param  probEqkRupture           The new probEqkRupture
     * @param  site                     The new Site
     * @param  intensityMeasure         The new intensityMeasure
     * @exception  ParameterException   Description of the Exception
     * @exception  IMRException         Description of the Exception
     * @exception  ConstraintException  Description of the Exception
     */
    public void setAll(
            ProbEqkRupture probEqkRupture,
            Site site,
            ParameterAPI intensityMeasure
             ) throws ParameterException, IMRException, ConstraintException
    {
        setSite(site);
        setProbEqkRupture( probEqkRupture );
        setIntensityMeasure( intensityMeasure );
    }



    /**
     * Returns a pointer to a parameter if it exists in one of the parameter lists
     *
     * @param name                  Parameter key for lookup
     * @return                      The found parameter
     * @throws ParameterException   If parameter with that name doesn't exist
     */
    public ParameterAPI getParameter(String name) throws ParameterException{

        // check whether it's the exceedProbParam
        if(name.equals(EXCEED_PROB_NAME))
          return exceedProbParam;

        try{ return siteParams.getParameter(name); }
        catch(ParameterException e){}

        try{ return probEqkRuptureParams.getParameter(name); }
        catch(ParameterException e){}

        try{ return propagationEffectParams.getParameter(name); }
        catch(ParameterException e){}

        try{ return supportedIMParams.getParameter(name); }
        catch(ParameterException e){}

        ListIterator it = supportedIMParams.getParametersIterator();
        while( it.hasNext() ){

            DependentParameterAPI param = (DependentParameterAPI)it.next();
            if( param.containsIndependentParameter( name ) ){
                return param.getIndependentParameter( name );
            }

        }

        try{ return otherParams.getParameter(name); }
        catch(ParameterException e){}

        throw new ParameterException( C + ": getParameter(): Parameter doesn't exist named " + name );
    }





    /**
     *  Returns an iterator over all Site-related parameters.
     *
     * @return    The Site Parameters Iterator
     */
    public ListIterator getSiteParamsIterator() {
        return siteParams.getParametersIterator();
    }


    /**
     *  Returns an iterator over all other parameters.  Other parameters are those
     *  that the exceedance probability depends upon, but that are not a
     *  supported IMT (or one of their independent parameters) and are not contained
     *  in, or computed from, the site or eqkRutpure objects.  Note that this does not
     *  include the exceedProbParam (which exceedance probability does not depend on).
     *
     * @return    Iterator for otherParameters
     */
    public ListIterator getOtherParamsIterator() {
        return otherParams.getParametersIterator();
    }



    /**
     *  Returns an iterator over all ProbEqkRupture related parameters.
     *
     * @return    The ProbEqkRupture Parameters Iterator
     */
    public ListIterator getProbEqkRuptureParamsIterator() {
        return probEqkRuptureParams.getParametersIterator();
    }


    /**
     *  Returns the iterator over all Propagation-Effect related parameters
     * (perhaps this method should exist only in subclasses that have these types
     * of parameters).
     *
     * @return    The Propagation Effect Parameters Iterator
     */
    public ListIterator getPropagationEffectParamsIterator() {
        return propagationEffectParams.getParametersIterator();
    }


    /**
     *  Returns the iterator over all supported Intensity-Measure
     *  Parameters.
     *
     * @return    The Supported Intensity-Measures Iterator
     */
    public ListIterator getSupportedIntensityMeasuresIterator() {
        return supportedIMParams.getParametersIterator();

    }
}

