package org.opensha.sha.nshmp.imr;

import org.opensha.commons.param.event.ParameterChangeWarningListener;

/**
 * Kludgy solution tot allow different flavors of the same attenuation
 * relationship to be used at the same time.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class AB2006_200_AttenRel extends AB2006_140_AttenRel {

	private final static String SHORT_NAME = "AB2006_200";
	private static final long serialVersionUID = 1234567890987654353L;
	public final static String NAME = "Atkinson and Boore (2002) 200bar";

	public AB2006_200_AttenRel(ParameterChangeWarningListener listener) {
		super(listener);
	}
	
	@Override
	public void setParamDefaults() {
		super.setParamDefaults();
		getParameter("Stress Drop").setValue(StressDrop.SD_200);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
}