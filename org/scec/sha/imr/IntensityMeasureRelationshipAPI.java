package org.scec.sha.imr;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;
import org.scec.util.*;

/**
 *  <b>Title:</b> IntensityMeasureRelationshipAPI<br>
 *  <b>Description:</b> This interface forms the basis for building other
 *  instatiable classes of IntensityMeasureRelationships (e.g., Campbell_1997).
 *  In addition to the methods defined below, each subclass will have additional
 *  ones unique to that particular model. <br>
 *  The subclasses will take a Site and ProbEqkRupture object as input, as
 *  well as a specified earthquake IntensityMeasure, and will give back mean and
 *  standard deviation of the Intensity Measure. Alternatively one can give it a
 *  PropagationEffect in place of the Site and ProbEqkRupture objects (it
 *  will get the latter two from the former), which will be useful when several
 *  different intensity measure relationships use the same propagation effect
 *  parameters (compute them all at once and then pass them to each). <br>
 *  If a Site and ProbEqkRupture object are passed, use the appropriate
 *  PropagationEffectCalculator rather than creating an internal
 *  PropagationEffect object?<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
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
     *  to the one passed in, and sets any potential-earthquake related
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
     * Sets the intensity measure Parameter if it is supported.
     *
     * @param  imt  The new intensityMeasureType value
     */
     public void setIntensityMeasure(ParameterAPI intensityMeasure) throws ParameterException;

    /**
     *  Gets a reference to the currently chosen Intensity-Measure Parameter
     *  from the IMR.
     *
     * @return    The intensityMeasure Parameter
     */
    public ParameterAPI getIntensityMeasure();

    /**
     *  Checks whether the Parameter parameter passed in is a supported
     *  Intensity Measure.
     * @param  type  The Parameter to check
     * @return       True if this is a supported IMT
     */
    public boolean isIntensityMeasureSupported( ParameterAPI type );



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
             );


    /**
     * Returns a pointer to a parameter if it exists in one of the parameter lists
     *
     * @param name                  Parameter key for lookup
     * @return                      The found parameter
     * @throws ParameterException   If parameter with that name doesn't exist
     */
    public ParameterAPI getParameter(String name) throws ParameterException;

    public void setParamDefaults();






    /**
     *  This calculates the probability that the intensity-measure level
     *  (the value in the Intensity-Measure Parameter) will be exceeded.
     *
     * @return    The exceed Probability value
     */
    public Double getExceedProbability();


    /**
     *  Returns an iterator over all Site-related parameters.
     *
     * @return    The Site Parameters Iterator
     */
    public ListIterator getSiteParamsIterator();


    /**
     *  Returns an iterator over all Potential-Earthquake related parameters.
     *
     * @return    The Potential Earthquake Parameters Iterator
     */
    public ListIterator getProbEqkRuptureParamsIterator();


    /**
     *  Returns the iterator over all Propagation-Effect related parameters.
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





    /**
     *  Returns the iterator over all controls parameters, such as x and y axis values.
     *
     * @return    The Controls Iterator
     */
    //public ListIterator getControlsIterator();


    /* *
     *  Useful for debugging. These parameters are all needed to determine what
     *  data set to generate for plotting.
     *
     * @return    Vector of all parameter values

    public Vector getParameterValuesAsStrings(); */

    /* * Adds a generic parameter to the list of all parameters
    public void addParameter(ParameterAPI parameter); */


    /* *
     *  First sets the PE, then calculates the Exceedence Probability from all
     *  the variables of this IMR.
     *
     * @param  probEqkRupture  The new potential earthquake value
     * @return                      The exceed Probability value
     * /
    public Double getExceedProbability( ProbEqkRupture probEqkRupture );


    /* *
     *  First sets the Site, then calculates the Exceedence Probability from all
     *  the variables of this IMR.
     *
     * @param  site  The new site value
     * @return       The exceed Probability value
     * /
    public Double getExceedProbability( Site site );


    /* *
     *  First sets the IMT, then calculates the Exceedence Probability from all
     *  the variables of this IMR.
     *
     * @param  intensityMeasureType  The new IMT value
     * @return                       The exceed Probability value
     * /
    public Double getExceedProbability( ParameterAPI intensityMeasureType );


    /* *
     *  First sets the IML, then calculates the Exceedence Probability from all
     *  the variables of this IMR.
     *
     * @param  intensityMeasureLevel  The new IML value
     * @return                        The exceed Probability value
     * /
    public Double getExceedProbability( Object intensityMeasureLevel );


    /* *
     *  Sets all parameters, then calculates the Exceedence Probability from all
     *  the variables of this IMR.
     *
     * @param  probEqkRupture    The new potential earthquake value
     * @param  site                   The new site value
     * @param  intensityMeasureLevel  The new IML value
     * @param  intensityMeasureType   The new IMT value
     * @return                        The exceed Probability value
     * /
    public Double getExceedProbability(
            ProbEqkRupture probEqkRupture,
            Site site,
            Object intensityMeasureLevel,
            ParameterAPI intensityMeasureType
             );
    */

    /* *
     *  First sets the PE, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  probEqkRupture  The new potential earthquake value
     * @return                      The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        ProbEqkRupture probEqkRupture ,
        Object imtensityMeasureLevels
    );


    /* *
     *  First sets the Site, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  site  The new site value
     * @return       The exceed Probability values put in a DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        Site site,
        Object imtensityMeasureLevels
    );


    /* *
     *  First sets the IMT, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  intensityMeasureType  The new IMT value
     * @return                       The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        Object imtensityMeasureLevels,
        ParameterAPI intensityMeasureType
    );


    /**
     *  Sets all parameters, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  probEqkRupture    The new potential earthquake value
     * @param  site                   The new site value
     * @param  intensityMeasureLevel  The new IML value
     * @param  intensityMeasureType   The new IMT value
     * @return                        The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
            ProbEqkRupture probEqkRupture,
            Site site,
            Object intensityMeasureLevels,
            ParameterAPI intensityMeasureType
             );
    */

}
