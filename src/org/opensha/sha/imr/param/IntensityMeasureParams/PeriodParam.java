package org.opensha.sha.imr.param.IntensityMeasureParams;

import java.util.ArrayList;

import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * This represents Period for the Spectral Acceleration parameter (SA_Param).  
 * The constructor requires a list of supported periods (in the form of a
 * DoubleDiscreteConstraint).  Once instantiated, this can be added to the
 * SA_Param as an independent parameter.
 * See constructors for info on editability and default values.
 * @author field
 *
 */
public class PeriodParam extends DoubleDiscreteParameter {

	public final static String NAME = "SA Period";
	public final static String UNITS = "sec";
	public final static String INFO = "Oscillator Period for SA";

	/**
	 * This is the most general constructor
	 * @param peroidList - desired constraints
	 * @param defaultPeriod - desired default value
	 * @param leaveEditable - whether or not to leave editable
	 */
	public PeriodParam(DoubleDiscreteConstraint peroidList, double defaultPeriod, boolean leaveEditable) {
		super(NAME, peroidList, UNITS);
		peroidList.setNonEditable();
		this.setInfo(INFO);
		setDefaultValue(defaultPeriod);
		if(!leaveEditable) setNonEditable();
	}
	
	/**
	 * This sets the default as 1.0 and leaves the parameter non editable
	 * @param peroidList
	 */
	public PeriodParam(DoubleDiscreteConstraint peroidList) { this(peroidList,1.0,false);}
	
	/**
	 * Helper method to quickly get the supported periods.
	 * 
	 * @return
	 */
	public ArrayList<Double> getSupportedPeriods() {
		DoubleDiscreteConstraint constr = (DoubleDiscreteConstraint) getConstraint();
		ArrayList<Double> periods = constr.getAllowedDoubles();
		return periods;
	}

}
