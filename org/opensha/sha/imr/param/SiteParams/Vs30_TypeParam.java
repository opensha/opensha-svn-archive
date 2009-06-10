package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 *  Vs flag Parameter - indicates whether vs was measured or inferred/estimated.
 *  Options can either be default or customized (depending on the constructor)
 */

public class Vs30_TypeParam extends StringParameter {

	public final static String NAME = "Vs30 Type";
	public final static String INFO = "Indicates how Vs30 was obtained";
	// Options for constraint:
	public final static String VS30_TYPE_MEASURED = "Measured";
	public final static String VS30_TYPE_INFERRED = "Inferred";

	public final static String DEFAULT = VS30_TYPE_INFERRED;

	public Vs30_TypeParam(StringConstraint options) {
		super(NAME, options);
	    this.setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
	
	public Vs30_TypeParam() {
		super(NAME);
		StringConstraint options = new StringConstraint();
		options.addString(VS30_TYPE_MEASURED);
		options.addString(VS30_TYPE_INFERRED);
		options.setNonEditable();
		this.setConstraint(options);
	    this.setInfo(INFO);
	    setDefaultValue(DEFAULT);
	    this.setNonEditable();
	}
}
