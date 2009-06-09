package org.opensha.sha.imr.param.IntensityMeasureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * This constitutes is for the natural-log Spectral Acceleration intensity measure
 * parameter.  Note also that periodParam and dampingParam are internal 
 * independentParameters of saParam, both of which must be created and added to this.
 * 
 * The default, internally held constraint objects are set as non editable, 
 * but the parameter is otherwise editable (including the constraints), so call the
 * setNonEditable() method when you no longer want it editable.
 * @author field
 *
 */
public class SA_Param extends WarningDoubleParameter {

	  public final static String NAME = "SA";
	  public final static String UNITS = "g";
	  protected final static Double DEFAULT = new Double(Math.log(0.5));
	  public final static String INFO = "Response Spectral Acceleration";
	  protected final static Double MIN = new Double(Math.log(Double.MIN_VALUE));
	  protected final static Double MAX = new Double(Double.MAX_VALUE);
	  protected final static Double WARN_MIN = new Double(Math.log(Double.MIN_VALUE));
	  protected final static Double WARN_MAX = new Double(Math.log(3.0));

	public SA_Param() {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
		this.setInfo(INFO);
		DoubleConstraint warn2 = new DoubleConstraint(WARN_MIN, WARN_MAX);
		warn2.setNonEditable();
		setWarningConstraint(warn2);
		setDefaultValue(DEFAULT);
	}
}
