package org.scec.sha.imr;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;
import org.scec.util.*;

/**
 *  <b>Title:</b> IntensityMeasureRelationshipAPI<br>
 *  <b>Description:</b> This is the interface defined for all
 *  IntensityMeasureRelationship classes.  See the abstract class for more
 *  description.<br>
 *
 * @author     Edward H. Field & Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface IntensityMeasureRelationshipAPI extends NamedObjectAPI {


    /**
     *  Returns a reference to the current Site object of the IMR
     *
     * @return    The site object
     */
    public Site getSite();


    /**
     *  Sets the Site object as a reference to that passed in, and sets
     *  any internal site-related parameters that the IMR depends upon.
     *
     * @param  site  The new site object
     */
    public void setSite( Site site );



    /**
     *  Returns a reference to the current probEqkRupture object in the IMR
     *
     * @return    The probEqkRupture object
     */
    public ProbEqkRupture getProbEqkRupture();


    /**
     *  Sets the probEqkRupture object in the IMR as a reference
     *  to the one passed in, and sets any earthquake-rupture related
     *  parameters that the IMR depends upon.
     *
     * @param  probEqkRupture  The new probEqkRupture object
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture );



    /**
     *  Returns the "value" object of the currently chosen Intensity-Measure
     *  Parameter.
     *
     * @return    The value field of the currently chosen intensityMeasure
     */
    public Object getIntensityMeasureLevel() throws ParameterException;

    /**
     *  Sets the value of the currently chosen Intensity-Measure Parameter.
     *  This is the value that the probability of exceedance is computed for.
     *
     * @param  iml  The new value for the intensityMeasure Parameter
     */
     public void setIntensityMeasureLevel( Object iml ) throws ParameterException;



     /**
      *  Sets the intensityMeasure parameter, not as a  pointer to that passed in,
      *  but by finding the internally held one with the same name and then setting
      *  its value (and the value of any of its independent parameters) to be equal
      *  to that passed in.
      *
      * @param  intensityMeasure  The new intensityMeasure Parameter
      */
     public void setIntensityMeasure(ParameterAPI intensityMeasure) throws ParameterException;


     /**
      *  This sets the intensityMeasure parameter as that which has the name
      *  passed in; no value (level) is set, nor are any of the IM's independent
      *  parameters set (since it's only given the name).
      *
      * @param  intensityMeasure  The new intensityMeasureParameter name
      */
     public void setIntensityMeasure( String intensityMeasureName ) throws ParameterException;


    /**
     *  Gets a reference to the currently chosen Intensity-Measure Parameter
     *  from the IMR.
     *
     * @return    The intensityMeasure Parameter
     */
    public ParameterAPI getIntensityMeasure();

    /**
     *  Checks if the Parameter is a supported intensity-Measure (checking
     *  both the name and value, as well as any dependent parameters
     *  (names and values) of the IM).
     *
     * @param  intensityMeasure  Description of the Parameter
     * @return                   True if this is a supported IMT
     */
    public boolean isIntensityMeasureSupported( ParameterAPI type );



    /**
     *  Sets the probEqkRupture, site, and intensityMeasure objects
     *  simultaneously.
     *
     * @param  probEqkRupture         The new probEqkRupture
     * @param  site                   The new Site
     * @param  intensityMeasure       The new IM
     */
    public void setAll(
            ProbEqkRupture probEqkRupture,
            Site site,
            ParameterAPI intensityMeasure
             );


    /**
     * Returns a pointer to a parameter if it exists in one of the parameter lists
     *
     * @param name                  Parameter key for lookup
     * @return                      The found parameter
     * @throws ParameterException   If parameter with that name doesn't exist
     */
    public ParameterAPI getParameter(String name) throws ParameterException;


    /**
     * This sets the defaults for all the parameters.
     */
    public void setParamDefaults();


    /**
     *  This calculates the probability that the intensity-measure level
     *  (the value in the Intensity-Measure Parameter) will be exceeded.
     *
     * @return    The exceed Probability value
     */
    public double getExceedProbability();


    /**
     *  Returns an iterator over all Site-related parameters.
     *
     * @return    The Site Parameters Iterator
     */
    public ListIterator getSiteParamsIterator();


    /**
     *  Returns an iterator over all other parameters.  Other parameters are those
     *  that the exceedance probability depends upon, but that are not a
     *  supported IMT (or one of their independent parameters) and are not contained
     *  in, or computed from, the site or eqkRutpure objects.  Note that this does not
     *  include the exceedProbParam (which exceedance probability does not depend on).
     *
     * @return    Iterator for otherParameters
     */
    public ListIterator getOtherParamsIterator();


    /**
     *  Returns an iterator over all earthquake-rupture related parameters.
     *
     * @return    The Earthquake-Rupture Parameters Iterator
     */
    public ListIterator getProbEqkRuptureParamsIterator();


    /**
     *  Returns the iterator over all Propagation-Effect related parameters
     * (perhaps this method should exist only in subclasses that have these types
     * of parameters).
     *
     * @return    The Propagation Effect Parameters Iterator
     */
    public ListIterator getPropagationEffectParamsIterator();


    /**
     *  Returns the iterator over all supported Intensity-Measure
     *  Parameters.
     *
     * @return    The Supported Intensity-Measures Iterator
     */
    public ListIterator getSupportedIntensityMeasuresIterator();

}
