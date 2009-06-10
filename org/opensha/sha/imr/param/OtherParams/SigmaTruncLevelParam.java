package org.opensha.sha.imr.param.OtherParams;

import org.opensha.commons.param.DoubleParameter;

/**
 * SigmaTruncLevelParam, a DoubleParameter that represents where truncation occurs
 * on the Gaussian distribution (in units of standard deviation, relative to the mean).
 */

public class SigmaTruncLevelParam extends DoubleParameter {

	public final static String NAME = "Truncation Level";
	public final static String UNITS = "Std Dev";
	public final static String INFO = "The number of standard deviations, from the mean, where truncation occurs";
	public final static Double DEFAULT = new Double(2.0);
	public final static Double MIN = new Double(Double.MIN_VALUE);
	public final static Double MAX = new Double(Double.MAX_VALUE);

	/**
	 * This constructor invokes the standard options (none, 1-sided, or 2-sided)
	 */
	public SigmaTruncLevelParam() {
		super(NAME, MIN, MAX, UNITS);
		setInfo(INFO);
		setDefaultValue(DEFAULT);
		setNonEditable();
	}
}
