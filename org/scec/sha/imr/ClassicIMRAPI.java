package org.scec.sha.imr;

import java.util.ListIterator;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.earthquake.*;
import org.scec.data.function.*;


/**
 *  <b>Title:</b> ClassicIMRAPI<br>
 *  <b>Description:</b> Classic IMRs use a Gaussian distribution, so along with
 *  calculating the Exccedence Probability based on either the parameters and
 *  the Site, PE, IML and IMT for this IMR, we can also calculate the mean and
 *  Standard Deviation for this distribution. This API defines the additional
 *  required functions for a Classic IMR to implement.<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ClassicIMRAPI
    extends IntensityMeasureRelationshipAPI
{


    /**
     *  Sets the intensityMeasure parameter; this doesn't replace the
     *  intensityMeasure as in the super class, but rather selects the
     *  internal intensity measure (the one that has the same name) and
     *  sets its value with that of the passed in parameter.
     *
     * @param  intensityMeasure  The new intensityMeasure Parameter
     */
     public void setIntensityMeasure( ParameterAPI intensityMeasure ) throws ParameterException, ConstraintException ;

    /**
     *  Sets the value of the selected intensityMeasure;
     *  IS THIS NEEDED SINCE WE HAVE THE OTHER VERSION THAT TAKES AN OBJECT?
     *
     * @param  iml                     The new intensityMeasureLevel value
     * @exception  ParameterException  Description of the Exception
     */
    public void setIntensityMeasureLevel( Double iml ) throws ParameterException;


    /**
     *  This returns the mean of the natural-log shaking level for the current
     *  set of parameters.
     *
     * @return    The mean value
     */
    public Double getMean();


    /**
     *  This returns the standard deviation (stdDev) of the natural-log shaking
     *  for the current set of parameters.
     *
     * @return    The stdDev value
     */
    public Double getStdDev();


    /**
     *  This fills in the exceedance probability for multiple intensityMeasure
     *  levels (often called a "hazard curve"); the levels are obtained from
     *  the X values of the input function, and Y values are filled in with the
     *  asociated exceedance probabilities.
     *
     * @param  intensityMeasureLevel  The new IML value
     * @return                        The exceed Probability values put in a
     *      DiscretizedFunction
     */
    public DiscretizedFuncAPI getExceedProbabilities(
        DiscretizedFuncAPI intensityMeasureLevels
    ) ;


    /**
     *  Returns a handle to the component parameter.
     *
     * @return    The componentParameter value
     */
    public ParameterAPI getComponentParam();

    /**
     *  Returns an iterator over all the Parameters that the Mean depends upon.
     *
     * @return    The Mean Independent Params Iterator
     */
    public ListIterator getMeanIndependentParamsIterator();

     /**
     *  Returns an iterator over all the Parameters that the StdDev depends upon.
     *
     * @return    The stdDev Independent Params Iterator
     */

    public ListIterator getStdDevIndependentParamsIterator();

    /**
     *
     *
     * @param  intensityMeasureLevel  The new IML value
     * @return                        The exceed Probability values put in a
     *      DiscretizedFunction
     */
//    public DiscretizedFunction2DAPI getExceedProbabilities( Object imtensityMeasureLevels );



    /* *
     *  Gets the mean attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  potentialEarthquake  Description of the Parameter
     * @return                      The mean value
     * /
    public Double getMean( ProbEqkRupture potentialEarthquake );


    /* *
     *  Gets the mean attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  site  Description of the Parameter
     * @return       The mean value
     * /
    public Double getMean( Site site );


    /* *
     *  Gets the mean attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  intensityMeasureType  Description of the Parameter
     * @return                       The mean value
     * /
    public Double getMean( ParameterAPI intensityMeasureType );


    /* *
     *  Gets the mean attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  potentialEarthquake   Description of the Parameter
     * @param  site                  Description of the Parameter
     * @param  intensityMeasureType  Description of the Parameter
     * @return                       The mean value
     * /
    public Double getMean(
            ProbEqkRupture potentialEarthquake,
            Site site,
            ParameterAPI intensityMeasureType
             );






    /* *
     *  Gets the stdDev attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  potentialEarthquake  Description of the Parameter
     * @return                      The stdDev value
     * /
    public Double getStdDev( ProbEqkRupture potentialEarthquake );


    /* *
     *  Gets the stdDev attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  site  Description of the Parameter
     * @return       The stdDev value
     * /
    public Double getStdDev( Site site );


    /* *
     *  Gets the stdDev attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  intensityMeasureType  Description of the Parameter
     * @return                       The stdDev value
     * /
    public Double getStdDev( ParameterAPI intensityMeasureType );


    /* *
     *  Gets the stdDev attribute of the ClassicIntensityMeasureRelationshipAPI
     *  object
     *
     * @param  potentialEarthquake   Description of the Parameter
     * @param  site                  Description of the Parameter
     * @param  intensityMeasureType  Description of the Parameter
     * @return                       The stdDev value
     * /
    public Double getStdDev(
            ProbEqkRupture potentialEarthquake,
            Site site,
            ParameterAPI intensityMeasureType
             );

    */

    /* *
     *  Gets the exceedProbability attribute of the
     *  ClassicIntensityMeasureRelationshipAPI object
     *
     * @param  intensityMeasureLevel  Description of the Parameter
     * @return                        The exceedProbability value
     * /
    public Double getExceedProbability( ParameterAPI intensityMeasureLevel );


    /* *
     *  Gets the exceedProbability attribute of the
     *  ClassicIntensityMeasureRelationshipAPI object
     *
     * @param  potentialEarthquake    Description of the Parameter
     * @param  site                   Description of the Parameter
     * @param  intensityMeasureLevel  Description of the Parameter
     * @param  intensityMeasureType   Description of the Parameter
     * @return                        The exceedProbability value
     * /
    public Double getExceedProbability(
            ProbEqkRupture potentialEarthquake,
            Site site,
            ParameterAPI intensityMeasureLevel,
            DoubleParameter intensityMeasureType
             );


    /* *
     *  First sets the PE, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  potentialEarthquake  The new potential earthquake value
     * @return                      The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        ProbEqkRupture potentialEarthquake ,
        DiscretizedFunction2DAPI intensityMeasureLevels
    ) ;


    /* *
     *  First sets the Site, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  site  The new site value
     * @return       The exceed Probability values put in a DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        Site site,
        DiscretizedFunction2DAPI intensityMeasureLevels
    );

    */


    /* *
     *  First sets the IMT, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  intensityMeasureType  The new IMT value
     * @return                       The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        DiscretizedFunction2DAPI intensityMeasureLevels,
        ParameterAPI intensityMeasureType
    );


    /* *
     *  Sets all parameters, then calculates the Exceedence Probability from all
     *  the variables of this IMR, calcualting over a range of parameter values.
     *
     * @param  potentialEarthquake    The new potential earthquake value
     * @param  site                   The new site value
     * @param  intensityMeasureLevel  The new IML value
     * @param  intensityMeasureType   The new IMT value
     * @return                        The exceed Probability values put in a
     *      DiscretizedFunction
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
            ProbEqkRupture potentialEarthquake,
            Site site,
            DiscretizedFunction2DAPI intensityMeasureLevels,
            ParameterAPI intensityMeasureType
             );




    /* *
     *  Gets the exceedProbabilities attribute of the
     *  ClassicIntensityMeasureRelationshipAPI object
     *
     * @param  intensityMeasureLevel  Description of the Parameter
     * @return                        The exceedProbabilities value
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
        DiscretizedFunction2DAPI
    ); */


    /* *
     *  Gets the exceedProbabilities attribute of the
     *  ClassicIntensityMeasureRelationshipAPI object
     *
     * @param  potentialEarthquake    Description of the Parameter
     * @param  site                   Description of the Parameter
     * @param  intensityMeasureLevel  Description of the Parameter
     * @param  intensityMeasureType   Description of the Parameter
     * @return                        The exceedProbabilities value
     * /
    public DiscretizedFunction2DAPI getExceedProbabilities(
            ProbEqkRupture potentialEarthquake,
            Site site,
            DiscretizedFunction2DAPI intensityMeasureLevels,
            ParameterAPI intensityMeasureType
             );
    */



}
