package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * RupTopDepth parameter - Depth to top of rupture.
 * See constructors for info on editability and default values.
 */
public class RupTopDepthParam extends WarningDoubleParameter {

	public final static String NAME = "Rupture Top Depth";
	public final static String UNITS = "km";
	public final static String INFO = "The depth to the shallowest point on the earthquake rupture surface";
	protected final static Double MIN = new Double(0);
	protected final static Double MAX = new Double(30);
	// warning values are set in subclasses
	
	/**
	 * This sets the default as given
	 * This is left editable so warning constraints can be added.
	 */
	public RupTopDepthParam(double defaultDepth) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(defaultDepth);
	}

	/**
	 * This sets the default as 0.0
	 * This is left editable so warning constraints can be added.
	 */
	public RupTopDepthParam() { this(0.0); }

}
