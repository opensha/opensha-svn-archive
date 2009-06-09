package org.opensha.sha.imr.param.IntensityMeasureParams;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * This represents Period for the Spectral Acceleration parameter (SA_Param).  
 * The constructor requires a list of supported periods (in the form of a
 * DoubleDiscreteConstraint).  Once instantiated, this can be added to the
 * SA_Param as an independent parameter.
 * 
 * Everything is set as uneditable.
 * @author field
 *
 */
public class PeriodParam extends DoubleDiscreteParameter {

	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	public final static String NAME = "SA Period";
	public final static String UNITS = "sec";
	protected final static Double DEFAULT = new Double(1.0);
	public final static String INFO = "Oscillator Period for SA";

	public PeriodParam(DoubleDiscreteConstraint peroidList) {
		super(NAME, peroidList, UNITS);
		peroidList.setNonEditable();
		this.setInfo(INFO);
		setDefaultValue(DEFAULT);
		this.setNonEditable();
	}
}
