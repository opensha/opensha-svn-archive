package org.opensha.sha.imr.param.IntensityMeasureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * This represents Damping for the Spectral Acceleration parameter (SA_Param).  
 * The constructor requires a list of supported damping levels (in the form of a
 * DoubleDiscreteConstraint).  Once instantiated, this can be added to the
 * SA_Param as an independent parameter.
 * 
 * Everything is set as uneditable.
 * @author field
 *
 */
public class DampingParam extends DoubleDiscreteParameter {

	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	public final static String NAME = "SA Damping";
	public final static String UNITS = " % ";
	protected final static Double DEFAULT = new Double(5);
	public final static String INFO = "Oscillator Damping for SA";

	/**
	 * 
	 * @param dampingConstraint
	 */
	public DampingParam(DoubleDiscreteConstraint dampingConstraint) {
		super(NAME, dampingConstraint, UNITS);
		dampingConstraint.setNonEditable();
		this.setInfo(INFO);
		setDefaultValue(DEFAULT);
	}

	/**
	 * This constructor assumes that only 5% damping (the default) is supported.
	 */
	public DampingParam() {
		super(NAME, UNITS);
		DoubleDiscreteConstraint dampingConstraint = new DoubleDiscreteConstraint();
		dampingConstraint.addDouble(DEFAULT);
		dampingConstraint.setNonEditable();
		setConstraint(dampingConstraint);
		setInfo(INFO);
		setDefaultValue(DEFAULT);
		setNonEditable();
	}
}
