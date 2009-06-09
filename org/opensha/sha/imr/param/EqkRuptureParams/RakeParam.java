package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Rake Parameter, reserved for representing the average rake of the earthquake
 * rupture.
 */
public class RakeParam extends DoubleParameter {
	
	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	public final static String NAME = "Rake";
	public final static String UNITS = "degrees";
	public final static String INFO = "Average rake of earthquake rupture";
	public final static Double DEFAULT = new Double("0");
	protected final static Double MIN = new Double( -180);
	protected final static Double MAX = new Double(180);

	public RakeParam() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
