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
 *  IntensityMeasureRelationship classes.<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
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
     * Sets the intensity measure Parameter if it is supported.
     *
     * @param  intensityMeasure  The desired intensityMeasure
     */
     public void setIntensityMeasure(ParameterAPI intensityMeasure) throws ParameterException;


     /**
     * Sets the intensity measure Parameter by name if supported (value not set).
     *
     * @param  imt  The new intensityMeasureType value
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
    public double getExceedProbability();


    /**
     *  Returns an iterator over all Site-related parameters.
     *
     * @return    The Site Parameters Iterator
     */
    public ListIterator getSiteParamsIterator();


    /**
     *  Returns an iterator over all earthquake-rupture related parameters.
     *
     * @return    The Earthquake-Rupture Parameters Iterator
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

}
