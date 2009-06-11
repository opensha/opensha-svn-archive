package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Dip Parameter, for representing the average dip of the earthquake rupture.
 * See constructors for info on editability and default values.
 */
public class DipParam extends DoubleParameter {

	  public final static String NAME = "Dip";
	  public final static String UNITS = "degrees";
	  public final static String INFO = "Average dip of earthquake rupture";
	  protected final static Double MIN = new Double(0);
	  protected final static Double MAX = new Double(90);
	
	  /**
	   * This sets the default dip as given.  
	   * This also makes the parameter non editable.
	   */
	public DipParam(double dipDefault) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(dipDefault);
	    setNonEditable();
	}

	  /**
	   * This sets the default dip as 90 degrees.  
	   * This also makes the parameter non editable.
	   */
	public DipParam() { this(90.0); }

}
