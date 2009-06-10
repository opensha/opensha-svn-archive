package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Vs30 Parameter, reserved for representing the average shear-wave velocity
 * in the upper 30 meters of a site (a commonly used parameter).  The warning 
 * constraint must be created and added when instantiated.
 */
public class Vs30_Param extends WarningDoubleParameter {

	public final static String NAME = "Vs30";
	public final static String UNITS = "m/sec";
	public final static String INFO = "The average shear-wave velocity between 0 and 30-meters depth";
	public final static Double DEFAULT = new Double("760");
	protected final static Double MIN = new Double(0.0);
	protected final static Double MAX = new Double(5000.0);

	/**
	 * This constructor leaves the param editable
	 */
	public Vs30_Param() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    this.setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}

	/**
	 * This takes the warning constraint, and sets everything as non-editable.
	 * @param warnMin
	 * @param warnMax
	 */
	public Vs30_Param(double warnMin, double warnMax) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    this.setInfo(INFO);
	    setDefaultValue(DEFAULT);
	    DoubleConstraint warn = new DoubleConstraint(warnMin, warnMax);
	    this.setWarningConstraint(warn);
	    warn.setNonEditable();
	    this.setNonEditable();
	}

}
