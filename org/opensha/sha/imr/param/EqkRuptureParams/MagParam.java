package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Magnitude parameter, reserved for representing moment magnitude.
 * The warning constraint must be created and added after instantiation.
 * See constructors for info on editability and default values.
 */
public class MagParam extends WarningDoubleParameter {

	public final static String NAME = "Magnitude";
	public final static String INFO = "Earthquake Moment Magnatude";
	protected final static Double MIN = new Double(0);
	protected final static Double MAX = new Double(10);
	// warning values are set in subclasses
	
	/**
	 * This sets the default value as given.
	 * This is left editable so warning constraints can be added.
	 */
	public MagParam(double defaultMag) {
		super(NAME, new DoubleConstraint(MIN, MAX));
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(defaultMag);
	}

	/**
	 * This sets the default value as 5.5.
	 * This is left editable so warning constraints can be added.
	 */
	public MagParam() { this(5.5);}
}
