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
 *  Subclasses will implement specific types of IMRs.  For example, AttenuationRelationship
 *  implements what is traditionally called an "Attenuation Relationship".
 *  This abstract IMR class also contains seperate parameterList objects for the
 *  site, potential-earthquake, and propagation-effect related parameters, as well
 *  as a list of "other" parameters that don't fit into those three categories.
 *  This class also contains a list of supported intensity-measure parameters (which
 *  may have internal independent parameters). These five lists combined should
 *  constitute a complete list of parameters for the IMR. <p>
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

    /** ParameterList of all Site parameters */
    protected ParameterList siteParams = new ParameterList();

    /** ParameterList of all ProbEqkRupture parameters */
    protected ParameterList probEqkRuptureParams = new ParameterList();

    /** ParameterList of all Propagation-Effect parameters */
    protected ParameterList propagationEffectParams = new ParameterList();

    /** ParameterList of all supported Intensity Measure parameters */
    protected ParameterList supportedIMParams = new ParameterList();

    /** ParameterList of other parameters that don't fit into above categories */
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
     *  No-Arg Constructor for the IntensityMeasureRelationship object. Currently does nothing
     */
    public IntensityMeasureRelationship() { }



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
     *  its value to be equal to that passed in.
     *
     * @param  intensityMeasure  The new intensityMeasure Parameter
     */
    public void setIntensityMeasure( ParameterAPI intensityMeasure ) throws ParameterException, ConstraintException {

        if( isIntensityMeasureSupported( intensityMeasure ) ) {
            im = supportedIMParams.getParameter( intensityMeasure.getName() );
            im.setValue( intensityMeasure.getValue() );
        }
        else throw new ParameterException( C + ": setIntensityMeasure(): " + "This im is not supported, name = " + intensityMeasure.getName() );
    }


    /**
     *  This sets the intensityMeasure parameter as that which has the name
     *  passed in; no value (level) is set.
     *
     * @param  intensityMeasure  The new intensityMeasure Parameter
     */
    public void setIntensityMeasure( String intensityMeasureName ) throws ParameterException, ConstraintException {

        if( supportedIMParams.containsParameter( intensityMeasureName ) ) {
            im = supportedIMParams.getParameter( intensityMeasureName );
        }
        else throw new ParameterException( C + ": setIntensityMeasure(): " + "This im is not supported, name = " + intensityMeasureName );
    }



    /**
     *  Checks if the Parameter is a supported intensity-Measure; checking
     *  both the name and that the value is allowed.
     *
     * @param  intensityMeasure  Description of the Parameter
     * @return                   True if this is a supported IMT
     */
    public boolean isIntensityMeasureSupported( ParameterAPI intensityMeasure ) {

        if ( supportedIMParams.containsParameter( intensityMeasure ) ) {
            ParameterAPI param = supportedIMParams.getParameter( intensityMeasure.getName() );
            if ( param.isAllowed( intensityMeasure.getValue() ) )
                return true;
            else
                return false;
        }
        else
            return false;
    }




    /**
     *  Sets the probEqkRupture, site and intensityMeasure objects
     *  simultaneously.
     *
     * @param  probEqkRupture    The new PE value
     * @param  site                   The new Site value
     * @param  intensityMeasure       The new IM value
     */
    public void setAll(
            ProbEqkRupture probEqkRupture,
            Site site,
            ParameterAPI intensityMeasure
             ) {
        setSite(site);
        setProbEqkRupture( probEqkRupture );
        setIntensityMeasure( intensityMeasure );
    }



    /* *
     * Helper function that allows any class to look up one of this IMRs
     * parameters.
     * @param name      Name of the parameter to fetch
     * @return          The named parameter
     * @throws ParameterException   Thrown if the named parameter doesn't exist
     */
    public ParameterAPI getParameter(String name) throws ParameterException{

        // try{ return independentParams.getParameter(name); }
        // catch(ParameterException e){}

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
     *  Returns an iterator over all Potential-Earthquake related parameters.
     *
     * @return    The Potential Earthquake Parameters Iterator
     */
    public ListIterator getProbEqkRuptureParamsIterator() {
        return probEqkRuptureParams.getParametersIterator();
    }


    /**
     *  Returns the iterator over all Propagation-Effect related parameters.
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

