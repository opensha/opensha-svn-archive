package org.opensha.sha.imr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.data.*;
import org.opensha.data.function.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;

/**
 * <b>Title:</b> AttenuationRelationshipAPI<br>
 * <b>Description:</b> AttenuationRelationship is a subclass of IntensityMeasureParameter
 * that uses a Gaussian distribution to compute probabilities.  It also assumes the
 * intensity-measure type is a scalar value (DoubleParameter).   See the abstract class
 * of AttenuationRelationship for more info. <br>
 *
 * @author     Edward H. Field & Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface AttenuationRelationshipAPI
    extends IntensityMeasureRelationshipAPI {


    /**
     * This returns metadata for all parameters (only showing the independent parameters
     * relevant for the presently chosen imt)
     * @return
     */
    public String getAllParamMetadata();

  /**
   * This method sets the user-defined distance beyond which ground motion is
   * set to effectively zero (the mean is a large negative value).
   * @param maxDist
   */
  public void setUserMaxDistance(double maxDist);

  /**
   *  Sets the intensityMeasure parameter.
   *
   * IS THIS REALLY NEEDED SINCE IT'S DEFINED IN THE PARENT?
   *
   * @param  intensityMeasure  The new intensityMeasure Parameter
   */
  public void setIntensityMeasure(ParameterAPI intensityMeasure) throws
      ParameterException, ConstraintException;

  /**
   *  Sets the value of the selected intensityMeasure;
   *  IS THIS NEEDED SINCE WE HAVE THE OTHER VERSION THAT TAKES AN OBJECT?
   *  IS IT EVER USED?
   *
   * @param  iml                     The new intensityMeasureLevel value
   * @exception  ParameterException  Description of the Exception
   */
  public void setIntensityMeasureLevel(Double iml) throws ParameterException;

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
   *  This calculates the intensity-measure level associated with probability
   *  held by the exceedProbParam given the mean and standard deviation
   * (according to the chosen truncation type and level).  Note
   *  that this does not store the answer in the value of the internally held
   *  intensity-measure parameter.
   * @param exceedProb : Sets the Value of the exceed Prob param with this value.
   * @return                         The intensity-measure level
   * @exception  ParameterException  Description of the Exception
   */
  public double getIML_AtExceedProb(double exceedProb);

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
      );

  /**
   * This calculates the intensity-measure level for each Sa Period
   * associated with probability
   * held by the exceedProbParam given the mean and standard deviation
   * (according to the chosen truncation type and level).  Note
   * that this does not store the answer in the value of the internally held
   * intensity-measure parameter.
   * @param exceedProb : Sets the Value of the exceed Prob param with this value.
   * @return                         The intensity-measure level
   * @exception  ParameterException  Description of the Exception
   */
  public DiscretizedFuncAPI getSA_IML_AtExceedProbSpectrum(double exceedProb) throws
      ParameterException,
      IMRException;


  /**
   *  This calculates the exceed-probability for each SA-Period that
   *  the supplied intensity-measure level
   *  will be exceeded given the mean and stdDev computed from current independent
   *  parameter values.  Note that the answer is not stored in the internally held
   *  exceedProbParam (this latter param is used only for the
   *  getIML_AtExceedProb() method).
   *
   * @return     DiscretizedFuncAPI  The DiscretizedFuncAPI function with each
   * value corresponding the SA Period
   * @exception  ParameterException  Description of the Exception
   * @exception  IMRException        Description of the Exception
   */
  public DiscretizedFuncAPI getSA_ExceedProbSpectrum(double iml) throws ParameterException,
      IMRException ;



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
   * This returns (iml-mean)/stdDev, ignoring any truncation.  This gets the iml
   * from the value in the Intensity-Measure Parameter.
   * @return double
   */
  public double getEpsilon();


  /**
   * This returns (iml-mean)/stdDev, ignoring any truncation.
   *
   * @param iml double
   * @return double
   */
  public double getEpsilon(double iml);

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

  /**
   * This method sets the location in the site.
   * This is helpful because it allows to  set the location within the
   * site without setting the Site Parameters. Thus allowing the capability
   * of setting the site once and changing the location of the site to do the
   * calculations.
   * After setting the location within the site, it calls the method
   * setPropagationEffectsParams().
   */
  public void setSiteLocation(Location loc);

  /**
   * Returns the Short Name of each AttenuationRelationship
   * @return String
   */
  public String getShortName();
  

}
