package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * RupWidthParam - Down-dip width of rupture.
 */
public class RupWidthParam extends WarningDoubleParameter {
	
	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	public final static String NAME = "Down-Dip Width";
	public final static String UNITS = "km";
	public final static String INFO = "Down-dip width of the rupture";
	public final static Double MIN = new Double(0.1);
	public final static Double MAX = new Double(100.0);
	public final static Double DEFAULT = new Double(10.0);
	// warning values are set in subclasses

	public RupWidthParam() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
