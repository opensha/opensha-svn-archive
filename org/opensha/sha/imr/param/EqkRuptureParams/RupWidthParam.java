package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * RupWidthParam - Down-dip width of rupture.
 * See constructors for info on editability and default values.
 */
public class RupWidthParam extends WarningDoubleParameter {

	public final static String NAME = "Down-Dip Width";
	public final static String UNITS = "km";
	public final static String INFO = "Down-dip width of the rupture";
	public final static Double MIN = new Double(0.1);
	public final static Double MAX = new Double(100.0);
	// warning values are set in subclasses

	/**
	 * This sets the default as given.
	 * This is left editable so warning constraints can be added.
	 */
	public RupWidthParam(double defaultWidth) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(defaultWidth);
	}

	/**
	 * This sets the default as 10.0.
	 * This is left editable so warning constraints can be added.
	 */
	public RupWidthParam() {this(10.0);}
}
