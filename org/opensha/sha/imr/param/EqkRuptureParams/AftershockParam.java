package org.opensha.sha.imr.param.EqkRuptureParams;

import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * Aftershock parameter, indicates whether or not an event is an aftershock
 */
public class AftershockParam extends BooleanParameter {

	public final static String NAME = "Aftershock";
	public final static String INFO = "Indicates whether earthquake is an aftershock";
	public final Boolean  DEFAULT = false;


	public AftershockParam() {
		super(NAME);
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
