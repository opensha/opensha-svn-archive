package org.scec.sha.imr;

import java.util.ListIterator;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.earthquake.*;
import org.scec.data.function.*;


/**
 *  <b>Title:</b> AttenuationRelationshipAPI<br>
 *  <b>Description:</b> AttenuationRelationship is a subclass of IntensityMeasureParameter that
 *  uses a Gaussian distribution to compute probabilities.  Thus in addition to the
 *  method for getting the exceedance probability, this has methods to get the mean
 *  and standard deviation of the gaussian distribution. This API defines these
 *  additional methods.<br>
 *
 * @author     Edward H. Field & Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface AttenuationRelationshipAPI
    extends IntensityMeasureRelationshipAPI
{


    /**
     *  Sets the intensityMeasure parameter.
     *
     * IS THIS REALLY NEEDED SINCE IT'S DEFINED IN THE PARENT?
     *
     * @param  intensityMeasure  The new intensityMeasure Parameter
     */
     public void setIntensityMeasure( ParameterAPI intensityMeasure ) throws ParameterException, ConstraintException ;

    /**
     *  Sets the value of the selected intensityMeasure;
     *  IS THIS NEEDED SINCE WE HAVE THE OTHER VERSION THAT TAKES AN OBJECT?
     *  IS IT EVER USED?
     *
     * @param  iml                     The new intensityMeasureLevel value
     * @exception  ParameterException  Description of the Exception
     */
    public void setIntensityMeasureLevel( Double iml ) throws ParameterException;


   /**
     *  This calculates the intensity-measure level associated with probability
     *  held by the exceedProbParam given the mean and standard deviation.  Note
     *  that this does not store the answer in the value of the internally held
     *  intensity-measure parameter.
     *
     * @return                         The intensity-measure level
     */
    public double getIML_AtExceedProb();


    /**
     *  This returns the mean intensity-measure level for the current
     *  set of parameters.
     *
     * @return    The mean value
     */
    public double getMean();


    /**
     *  This returns the standard deviation (stdDev) of the intensity-measure
     *  level for the current set of parameters.
     *
     * @return    The stdDev value
     */
    public double getStdDev();


    /**
     *  This fills in the exceedance probability for multiple intensityMeasure
     *  levels (often called a "hazard curve"); the levels are obtained from
     *  the X values of the input function, and Y values are filled in with the
     *  asociated exceedance probabilities.
     *
     * @param  intensityMeasureLevel  The function to be filled in
     * @return                        The same function
     */
    public DiscretizedFuncAPI getExceedProbabilities(
        DiscretizedFuncAPI intensityMeasureLevels
    ) ;


    /**
     *  This calculates the probability that the supplied intensity-measure level
     *  will be exceeded given the mean and stdDev computed from current independent
     *  parameter values.  Note that the answer is not stored in the internally held
     *  exceedProbParam (this latter param is used only for the
     *  getIML_AtExceedProb() method).
     *
     * @return                         The exceedProbability value
     * @exception  ParameterException  Description of the Exception
     * @exception  IMRException        Description of the Exception
     */
    public double getExceedProbability(double iml);



    /**
     *  Returns a handle to the component parameter.
     *
     * @return    The componentParameter value
     */
    public ParameterAPI getComponentParam();


    /**
     *  Returns an iterator over all the Parameters that the Mean calculation depends upon.
     *  (not including the intensity-measure related paramters and their internal,
     *  independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getMeanIndependentParamsIterator();


    /**
     *  Returns an iterator over all the Parameters that the StdDev calculation depends upon
     *  (not including the intensity-measure related paramters and their internal,
     *  independent parameters).
     *
     * @return    The Independent Parameters Iterator
     */
    public ListIterator getStdDevIndependentParamsIterator();


    /**
     *  Returns an iterator over all the Parameters that the exceedProb calculation
     *  depends upon (not including the intensity-measure related paramters and
     *  their internal, independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getExceedProbIndependentParamsIterator();


    /**
     *  Returns an iterator over all the Parameters that the IML-at-exceed-
     *  probability calculation depends upon. (not including the intensity-measure
     *  related paramters and their internal, independent parameters).
     *
     * @return    The Independent Params Iterator
     */
    public ListIterator getIML_AtExceedProbIndependentParamsIterator();

}
