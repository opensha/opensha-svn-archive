package org.scec.sha.propagation;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> PropagationEffect<p>
 *
 * <b>Description:</b> The parameter options are held internally as a ParamterList
 * of PropagationEffectCalculator objects, which extend Paramter. When passed a
 * paramName String, the code will return: <p>
 *
 * <code>PropagationEffectCalculator.getParamterName() == paramName</code> <p>
 *
 * and will then return the value given by the getParameterValue() method for that
 * PropagationEffectCalculator.  One can create and add a new, arbitrary
 * PropagationEffectCalculator() to the vector of options.<p>
 *
 * However, this class recognizes (and checks for first) the following
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

    protected Site site;
	protected ProbEqkRupture probEqkRupture;

    /** is held in object, and added to vector of PropagationEffectParameters
     * values - int 0 or 1
     */
    protected DoubleParameter AS_1997_HangingWall;

    /** is held in object, and added to vector of PropagationEffectParameters
     * fraction of fault length that ruptures toward
     * the site; a directivity parameter*/
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



    public PropagationEffect() {
    }

    public PropagationEffect( Site site, ProbEqkRupture pe) {

    }


    public Site getSite() { return site; }
	/** also update all calculators with new site? */
    public void setSite(Site site) { this.site = site; }

	public ProbEqkRupture getProbEqkRupture() { return probEqkRupture; }
	/** also update all calculators with new ProbEqkRupture? */
    public void setProbEqkRupture(ProbEqkRupture pe) { probEqkRupture = pe; }



    /**
     * Used to add calculators to list. Also can use the
     * more general parameter list API
     */
    public void AddPropagationEffectParameter(PropagationEffectParameter parameter) {

    }

    /** update both existing PE and Site */
    public Object getValue(String paramName, Site site, ProbEqkRupture pe ) {
    	return null;
    }

    /** update existing Site */
    public Object getValue(String paramName, Site site) {
    	return null;
    }

    /** update existing Earthquake */
    public Object getValue(String paramName,
			   ProbEqkRupture probEqkRuptureObj ) {
    	return null;
    }

    public Object getValue(String paramName) {
    	return null;
    }

    /** set's a new value to a Parameter in the list, if it exists, else throws exception */
    public void setValue(String name, Object value) throws ParameterException, ConstraintException {
        throw new java.lang.UnsupportedOperationException("This subclass doesn't permit modifications");
    }







    public Double getAS_1997_HangingWall() { return (Double)AS_1997_HangingWall.getValue(); }
    public Double getAbrahamson_2000_X() { return (Double)abrahamson_2000_X.getValue(); }
    public Double getAbrahamson_2000_Theta() { return (Double)abrahamson_2000_Theta.getValue(); }

    public Double getDistanceRup() { return (Double)distanceRup.getValue(); }
    public Double getDistanceJB() { return (Double)distanceJB.getValue(); }
    public Double getDistanceSeis() { return (Double)distanceSeis.getValue(); }



}
