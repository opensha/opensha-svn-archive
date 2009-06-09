package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Magnitude parameter, reserved for representing moment magnitude.
 * The warning constraint must be created and added after instantiation.
 */
public class MagParam extends WarningDoubleParameter {
	
	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	public final static String NAME = "Magnitude";
	// There are no units for Magnitude
	public final static String INFO = "Earthquake Moment Magnatude";
	protected final static Double DEFAULT = new Double(5.5);
	protected final static Double MIN = new Double(0);
	protected final static Double MAX = new Double(10);
	// warning values are set in subclasses

	public MagParam() {
		super(NAME, new DoubleConstraint(MIN, MAX));
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
