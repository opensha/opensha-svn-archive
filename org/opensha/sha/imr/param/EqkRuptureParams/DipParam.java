package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Dip Parameter, reserved for representing the average dip of the earthquake
 * rupture.
 */
public class DipParam extends DoubleParameter {

	  public final static String NAME = "Dip";
	  public final static String UNITS = "degrees";
	  public final static String INFO = "Average dip of earthquake rupture";
	  public final static Double DEFAULT = new Double("90");
	  protected final static Double MIN = new Double(0);
	  protected final static Double MAX = new Double(90);

	public DipParam() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
