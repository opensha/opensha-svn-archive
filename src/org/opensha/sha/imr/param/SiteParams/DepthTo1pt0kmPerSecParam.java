package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Depth 1.0 km/sec Parameter, reserved for representing the depth to where
 * shear-wave velocity = 1.0 km/sec.
 * See constructors for info on editability and default values.
 */
public class DepthTo1pt0kmPerSecParam extends WarningDoubleParameter {


	public final static String NAME = "Depth 1.0 km/sec";
	public final static String UNITS = "m";
	public final static String INFO = "The depth to where shear-wave velocity = 1.0 km/sec";
	protected final static Double MIN = new Double(0.0);
	protected final static Double MAX = new Double(30000.0);


	/**
	 * This constructor sets the default as given, and leaves the param editable 
	 * so the warning constraint can be added later.
	 * @param defaultDepth
	 */
	public DepthTo1pt0kmPerSecParam(double defaultDepth) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
		setInfo(INFO);
		setDefaultValue(defaultDepth);
	}

	/**
	 * This constructor sets the default as 100, and leaves the param editable 
	 * so the warning constraint can be added later.
	 */
	public DepthTo1pt0kmPerSecParam() {this(100.0);}

	/**
	 * This uses the given default and warning-constraint limits, and sets 
	 * everything as non-editable.
	 * @param defaultDepth
	 * @param warnMin
	 * @param warnMax
	 */
	public DepthTo1pt0kmPerSecParam(double defaultDepth, double warnMin, double warnMax) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
		setInfo(INFO);
		setDefaultValue(defaultDepth);
		DoubleConstraint warn = new DoubleConstraint(warnMin, warnMax);
		setWarningConstraint(warn);
		warn.setNonEditable();
		setNonEditable();
	}
	
	/**
	 * This sets default as 100, uses the given warning-constraint limits, and sets 
	 * everything as non-editable.
	 * @param warnMin
	 * @param warnMax
	 */
	public DepthTo1pt0kmPerSecParam(double warnMin, double warnMax) {this(100.0,warnMin,warnMax);}
}
