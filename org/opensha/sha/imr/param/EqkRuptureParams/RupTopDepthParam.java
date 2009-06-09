package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * RupTopDepth parameter - Depth to top of rupture.
 */
public class RupTopDepthParam extends WarningDoubleParameter {

	public final static String NAME = "Rupture Top Depth";
	public final static String UNITS = "km";
	public final static String INFO =
	    "The depth to the shallowest point on the earthquake rupture surface";
	public final static Double DEFAULT = new Double(0);
	protected final static Double MIN = new Double(0);
	protected final static Double MAX = new Double(30);
	// warning values are set in subclasses

	public RupTopDepthParam() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
