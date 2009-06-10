package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 * FaulltTypeParam, a StringParameter for representing different
 * styles of faulting.  The options are not specified here because
 * nomenclature generally differs among subclasses.  The default must
 * also be specified in the constructor.
 */

public class FaultTypeParam extends StringParameter {

	public final static String NAME = "Fault Type";
	// No units for this one
	public final static String INFO = "Style of faulting";

	public String DEFAULT;

	public FaultTypeParam(StringConstraint options, String defaultValue) {
		super(NAME, options);
	    setInfo(INFO);
		DEFAULT = defaultValue;
	    setDefaultValue(DEFAULT);
	}
}
