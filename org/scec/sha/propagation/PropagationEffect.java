package org.scec.sha.propagation;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;

// FIX *** FIX *** SWR: Many functions not implemented. Is this class still needed???

/**
 * <b>Title:</b> PropagationEffect<p>
 *
 * <b>WARNING:</b> SWR: I noticed alot of incomplete functions in this class. Is
 * this class even being used??? <p>
 *
 * <b>Description:</b> This is a ParameterList of PropagationEffectParameters that also maintains
 * a reference to the Site and probEqkRupture objects that are common
 * to all the parameters. Recall from the PropagationEffectParameter documentation
 * these two parameters can be set in the PropagationEffectParameter to
 * uniquly determine the parameters's value, bypassing the normal useage
 * of setValue() to update the parameter's value. <p>
 *
 * The parameter options are held internally as a ParamterList
 * of PropagationEffectParameter objects which extend Paramter. These parameters
 * can be access by name, and the value can also be returned. <p>
 *
 * Since this class is a ParameterList one can create and add a new, arbitrary
 * PropagationEffectCalculator() to the vector of options. More importantly
 * this class also maintains a list of pre-defined parameters. This class
 * recognizes (and checks for first) the following
 * common propagation-effect parameter names (used in existing
 * IntensityMeasureRelationships) and performs some of the calculations
 * simultaneously to increase efficiency (e.g., it's faster to compute
 * Rrup, Rjb,and Rseis simultaneously, for the same Site and
 * ProbEqkRupture, rather than in series):<p>
 *
 * This can be accomplished by spawning new threads to return the desired
 * requested result first. These threads should be set at low priority.<p>
 *
 * <br><br>
 * <br>     Rrup	\
 * <br>     Rjb	 > (km; these are three common distance
 * <br>                 measures used by the Rseis	/
 * <br>                 various IntensityMeasureRelationships)
 * <br>     AS_1997_HangingWall	(int 0 or 1)
 * <br>     Abrahamson_2000_X   (fraction of fault length that ruptures toward
 * <br>                          the site; a directivity parameter)
 * <br>     Abrahamson_2000_Theta 	(angle between strike and
 * <br>                               epicentral azimuth; a directivity parameter)
 * <p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */
public class PropagationEffect extends ParameterList {


    /** The Site used for calculating the PropagationEffect parameter values. */
    protected Site site = null;

    /** The ProbEqkRupture used for calculating the PropagationEffect parameter values.*/
    protected ProbEqkRupture probEqkRupture = null;

    /** is held in object, and added to vector of PropagationEffectParameters
     * values - int 0 or 1. SWR: ??? Not sure what this means.
     */
    protected DoubleParameter AS_1997_HangingWall;

    /** is held in object, and added to vector of PropagationEffectParameters
     * fraction of fault length that ruptures toward
     * the site; a directivity parameter.  SWR: ??? Not sure what this means.
     */
    protected DoubleParameter abrahamson_2000_X;

    /** is held in object, and added to vector of PropagationEffectParameters.
     * Angle between strike and epicentral azimuth; a directivity
     * parameter.
     */
    protected DoubleParameter abrahamson_2000_Theta;

    /** is held in object, and added to vector of PropagationEffectParameters */
   	protected DoubleParameter distanceRup;

    /** is held in object, and added to vector of PropagationEffectParameters */
   	protected DoubleParameter distanceJB;

    /** is held in object, and added to vector of PropagationEffectParameters */
   	protected DoubleParameter distanceSeis;


    /** No Argument consructor */
    public PropagationEffect() { }

    /** FIX - Currently does nothing, should set the variables */
    public PropagationEffect( Site site, ProbEqkRupture pe) {}


    /** Returns the common Site fo all internal parametes */
    public Site getSite() { return site; }
	/**
     *  Sets the common Site fo all internal parameters.
     *  FIX *** Should update all parameters in the list.
     */
    public void setSite(Site site) { this.site = site; }

	/** Returns the common ProbEqkRupture fo all internal parametes */
    public ProbEqkRupture getProbEqkRupture() { return probEqkRupture; }
	/**
     *  Sets the ProbEqkRupture Site fo all internal parameters.
     *  FIX *** Should update all parameters in the list.
     */
    public void setProbEqkRupture(ProbEqkRupture pe) { probEqkRupture = pe; }



    /**
     * Used to add Propagation Effect Parameters to list. Also can use the
     * more general parameter list API in the parent class. This one is here
     * for convinience.
     *
     * FIX *** Currently does nothing.
     */
    public void AddPropagationEffectParameter(PropagationEffectParameter parameter) {

    }

    /**
     * Update both existing PE and Site, recalculates all parameter.
     * FIX *** Currently does nothing.
    */
    public Object getValue(String paramName, Site site, ProbEqkRupture pe ) {
    	return null;
    }

    /**
     * Update existing Site, then recalculates the parameters,
     * returning a result for a specific parameter.
     * FIX *** Currently does nothing.
     */
    public Object getValue(String paramName, Site site) {
    	return null;
    }

    /**
     * Update existing ProbEqkRupture, then recalculates the parameters,
     * returning a result for a specific parameter.
     * FIX *** Currently does nothing.
     */
    public Object getValue(String paramName,
			   ProbEqkRupture probEqkRuptureObj ) {
    	return null;
    }

    /**
     * Returns the calculated value for one specific parameter in this list.
     * FIX *** Currently does nothing.
     */
    public Object getValue(String paramName) {
    	return null;
    }

    /**
     * Set's a new value to a Parameter in the list, if it exists, else throws exception
     * FIX *** Currently does nothing.
     */
    public void setValue(String name, Object value) throws ParameterException, ConstraintException {
        throw new java.lang.UnsupportedOperationException("This subclass doesn't permit modifications");
    }



    /** Returns the value for the specified rpe-defined parameter */
    public Double getAS_1997_HangingWall() { return (Double)AS_1997_HangingWall.getValue(); }
    /** Returns the value for the specified rpe-defined parameter */
    public Double getAbrahamson_2000_X() { return (Double)abrahamson_2000_X.getValue(); }
    /** Returns the value for the specified rpe-defined parameter */
    public Double getAbrahamson_2000_Theta() { return (Double)abrahamson_2000_Theta.getValue(); }

    /** Returns the value for the specified rpe-defined parameter */
    public Double getDistanceRup() { return (Double)distanceRup.getValue(); }
    /** Returns the value for the specified rpe-defined parameter */
    public Double getDistanceJB() { return (Double)distanceJB.getValue(); }
    /** Returns the value for the specified rpe-defined parameter */
    public Double getDistanceSeis() { return (Double)distanceSeis.getValue(); }


}
