package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Depth 1.0 km/sec Parameter, reserved for representing the depth to where
 * shear-wave velocity = 1.0 km/sec.
 * The warning constraint must be created and added when instantiated.
 */
public class DepthTo1pt0kmPerSecParam extends WarningDoubleParameter {

	
	public final static String NAME = "Depth 1.0 km/sec";
	public final static String UNITS = "m";
	public final static String INFO = "The depth to where shear-wave velocity = 1.0 km/sec";
	public final static Double DEFAULT = new Double("100.0");
	protected final static Double MIN = new Double(0.0);
	protected final static Double MAX = new Double(30000.0);


	/**
	 * This constructor leaves the param editable
	 */
	public DepthTo1pt0kmPerSecParam() {
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
	public DepthTo1pt0kmPerSecParam(double warnMin, double warnMax) {
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
