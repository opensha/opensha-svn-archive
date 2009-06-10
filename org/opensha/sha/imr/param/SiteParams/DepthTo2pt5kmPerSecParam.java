package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Depth 2.5 km/sec Parameter, reserved for representing the depth to where
 * shear-wave velocity = 2.5 km/sec.
 *   The warning constraint must be created and added when instantiated.
 */
public class DepthTo2pt5kmPerSecParam extends WarningDoubleParameter {

	
	public final static String NAME = "Depth 2.5 km/sec";
	public final static String UNITS = "km";
	public final static String INFO = "The depth to where shear-wave velocity = 2.5 km/sec";
	public final static Double DEFAULT = new Double("1.0");
	protected final static Double MIN = new Double(0.0);
	protected final static Double MAX = new Double(30000.0);


	/**
	 * This constructor leaves the param editable
	 */
	public DepthTo2pt5kmPerSecParam() {
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
	public DepthTo2pt5kmPerSecParam(double warnMin, double warnMax) {
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
