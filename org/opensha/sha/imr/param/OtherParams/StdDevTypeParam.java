package org.opensha.sha.imr.param.OtherParams;

import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 * StdDevTypeParam represents the various types of standard deviations that an IMR might 
 * support  The constraint must be provided in the constructor, and the "STD_DEV_TYPE_*" 
 * strings here represent common options that can be used in creating the constraint 
 * (although other unique options can be added as well).
 * "InterEvent" is the event to event variability, "Intra-Event" is the variability 
 * within an event, and "Total" (the most common) is the other two two added in quadrature.
 * Other options should be self explanatory. 
 */

public class StdDevTypeParam extends StringParameter {

	
	public final static String NAME = "Std Dev Type";
	// No units for this one
	public final static String INFO = "Type of Standard Deviation";
	
	// Options
	public final static String STD_DEV_TYPE_TOTAL = "Total";
	public final static String STD_DEV_TYPE_INTER = "Inter-Event";
	public final static String STD_DEV_TYPE_INTRA = "Intra-Event";
	public final static String STD_DEV_TYPE_NONE = "None (zero)";
	public final static String STD_DEV_TYPE_TOTAL_MAG_DEP = "Total (Mag Dependent)";
	public final static String STD_DEV_TYPE_TOTAL_PGA_DEP = "Total (PGA Dependent)";
	public final static String STD_DEV_TYPE_INTRA_MAG_DEP = "Intra-Event (Mag Dependent)";

	public final static String DEFAULT = STD_DEV_TYPE_TOTAL;


	public StdDevTypeParam(StringConstraint options) {
		super(NAME, options);
	    setInfo(INFO);
	    setDefaultValue(DEFAULT);
	}
}
