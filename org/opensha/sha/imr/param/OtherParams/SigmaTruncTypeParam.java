package org.opensha.sha.imr.param.OtherParams;

import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 * SigmaTruncTypeParam, a StringParameter that represents the type of
 * truncation to be applied to the probability distribution.  The 
 * constraint/options are hard-coded here because changes will require
 * changes in the probability calculations elsewhere in the code.
 * The parameter is left non editable
 */
public class SigmaTruncTypeParam extends StringParameter {

	public final static String NAME = "Gaussian Truncation";
	public final static String INFO = "Type of distribution truncation to apply when computing exceedance probabilities";
	// Options
	public final static String SIGMA_TRUNC_TYPE_NONE = "None";
	public final static String SIGMA_TRUNC_TYPE_1SIDED = "1 Sided";
	public final static String SIGMA_TRUNC_TYPE_2SIDED = "2 Sided";
	
	/**
	 * This constructor invokes the standard options ("None", "1 Sided", or "2 Sided"),
	 * and sets the default as "None".  The parameter is left non editable.
	 */
	public SigmaTruncTypeParam() {
		super(NAME);
		StringConstraint options = new StringConstraint();
		options.addString(SIGMA_TRUNC_TYPE_NONE);
		options.addString(SIGMA_TRUNC_TYPE_1SIDED);
		options.addString(SIGMA_TRUNC_TYPE_2SIDED);
		setInfo(INFO);
		setDefaultValue(SIGMA_TRUNC_TYPE_NONE);
		setNonEditable();
	}
}
