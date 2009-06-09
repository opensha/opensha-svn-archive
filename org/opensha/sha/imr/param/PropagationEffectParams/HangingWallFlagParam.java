package org.opensha.sha.imr.param.PropagationEffectParams;

import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleConstraint;
import org.opensha.commons.param.WarningDoubleParameter;

/**
 * HangingWallFlagParam parameter - indicates whether a site is 
 * on the hanging wall of a rupture surface.  Exact definition and setting 
 * of value must be handled in the implementing class
 */
public class HangingWallFlagParam extends BooleanParameter {
	
	private static final long serialVersionUID = 0xBD103F5;	// is this correct

	  public final static String NAME = "Site on Hanging Wall";
	  public final static String INFO = "Indicates whether the site is on the hanging wall";
	  public final static Boolean DEFAULT = false;


	public HangingWallFlagParam() {
		super(NAME);
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
