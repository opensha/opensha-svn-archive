package org.opensha.sha.imr.param.IntensityMeasureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * This constitutes the natural-log Peak Ground Acceleration intensity measure
 * parameter.  The default, internally held constraint objects are set as non editable, 
 * but the parameter is otherwise editable (including the constraints), so call the
 * setNonEditable() method when you no longer want it editable.
 * @author field
 *
 */
public class PGA_Param extends WarningDoubleParameter {

	public final static String NAME = "PGA";
	public final static String UNITS = "g";
	public final static Double DEFAULT = new Double(Math.log(0.1));
	public final static String INFO = "Peak Ground Acceleration";
	public final static Double MIN = new Double(Math.log(Double.MIN_VALUE));
	public final static Double MAX = new Double(Double.MAX_VALUE);
	public final static Double WARN_MIN = new Double(Math.log(Double.MIN_VALUE));
	public final static Double WARN_MAX = new Double(Math.log(2.5));

	public PGA_Param() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    this.setInfo(INFO);
	    DoubleConstraint warn2 = new DoubleConstraint(WARN_MIN, WARN_MAX);
	    warn2.setNonEditable();
	    setWarningConstraint(warn2);
	    setDefaultValue(DEFAULT);
	}
	
}
