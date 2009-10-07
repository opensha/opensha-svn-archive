package org.opensha.sha.imr.param.OtherParams;

import java.util.ArrayList;

import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;

/**
 * Tectonic Region Type Param, reserved for representing the type of tectonic region.
 * The constraint must be provided in the constructor, and the
 * "TYPE*" strings here represent common options that can be used in creating 
 *  the constraint (although other unique options can be added as well).
 *  See constructors for info on editability and default values.
 *  Note that this is not in the EqkRuptureParams directory because it will not
 *  be set from information in and EqkRupture object (the latter does not carry this info).
 */

public class TectonicRegionTypeParam extends StringParameter {

	public final static String NAME = "Tectonic Region";
	public final static String INFO = "Type of Tectonic Region";

	// Options for constraint:
	public final static String TYPE_ACTIVE_SHALLOW = "Active Shallow Crust";
	public final static String TYPE_STABLE_SHALLOW = "Stable Shallow Crust";
	public final static String TYPE_SUBDUCTION_INTERFACE = "Subduction Interface";
	public final static String TYPE_SUBDUCTION_SLAB = "Subduction IntraSlab";
	public final static String TYPE_VOLCANIC = "Volcanic";
	
	
	/**
	 * This no-argument constructor defaults to only Active Shallow Crust, 
	 * and sets the parameter as non editable.
	 */
	public TectonicRegionTypeParam() {
		super(NAME);
		StringConstraint options = new StringConstraint();
		options.addString(TYPE_ACTIVE_SHALLOW);
		setConstraint(options);
	    setInfo(INFO);
	    setDefaultValue(TYPE_ACTIVE_SHALLOW);
	    setNonEditable();
	}

	/**
	 * This constructor will throw an exception if the options contain a non-allowed
	 * type (as represented by the TYPE_* fields here).  The parameter is set as non editable 
	 * after creation
	 * @param options
	 * @param defaultValue
	 */
	public TectonicRegionTypeParam(StringConstraint options, String defaultValue) {
		super(NAME, options);
		// check that options are supported
		ArrayList strings = options.getAllowedStrings();
		for(int i=0; i< strings.size();i++)
			if(!isTypePotentiallySupported((String)strings.get(i))) throw new RuntimeException("Constraint type not supported by TectonicRegionTypeParam");
	    setInfo(INFO);
	    setDefaultValue(defaultValue);
	    setNonEditable();
	}
	
	/**
	 * This checks whether a type is potentially supported by this class 
	 * (whether an instance could support it, as opposed to whether an instance
	 * does support it (the latter being controlled by the string constraint)
	 * @param option
	 * @return boolean
	 */
	public static boolean isTypePotentiallySupported(String option) {
		if( option.equals(TYPE_ACTIVE_SHALLOW) || 
			option.equals(TYPE_STABLE_SHALLOW) || 
			option.equals(TYPE_SUBDUCTION_INTERFACE) || 
			option.equals(TYPE_SUBDUCTION_SLAB) || 
			option.equals(TYPE_VOLCANIC))
				return true;
			else
				return false;
	}

}
