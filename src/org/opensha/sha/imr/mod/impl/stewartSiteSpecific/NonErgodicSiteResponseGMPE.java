package org.opensha.sha.imr.mod.impl.stewartSiteSpecific;

import java.util.EnumSet;
import java.util.ListIterator;

import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.mod.ModAttenRelRef;
import org.opensha.sha.imr.mod.ModAttenuationRelationship;

/**
 * Steward 2014 modified GMPE which can be used in the Attenuation Relationship app
 * @author kevin
 *
 */
public class NonErgodicSiteResponseGMPE extends ModAttenuationRelationship {
	
	public static final String NAME = "Non Ergodic Site Response GMPE 2016";
	public static final String SHORT_NAME = "NonErgodic2016";
	
	public NonErgodicSiteResponseGMPE() {
		this(null);
	}
	
	public NonErgodicSiteResponseGMPE(ParameterChangeWarningListener l) {
//		super(l, EnumSet.copyOf(AttenRelRef.get(ServerPrefUtils.SERVER_PREFS)),
//				EnumSet.of(ModAttenRelRef.STEWART_SITE_SPECIFIC));
		// for now just BSSA 2014
		super(l, EnumSet.of(AttenRelRef.BSSA_2014),
				EnumSet.of(ModAttenRelRef.STEWART_SITE_SPECIFIC));
	}

	@Override
	public Parameter getParameter(String name) throws ParameterException {
		// check IM and dependent params
		for (Parameter<?> param : supportedIMParams) {
			if (param.getName().equals(name))
				return param;
			if (param.containsIndependentParameter(name))
				return param.getIndependentParameter(name);
		}
		// check site params
		if (siteParams.containsParameter(name))
			return siteParams.getParameter(name);
		// check underlying IMR
		try {
			return getCurrentIMR().getParameter(name);
		} catch (ParameterException e) {
			// doesn't exist in the IMR
		}
		
		return super.getParameter(name);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getShortName() {
		return SHORT_NAME;
	}
	
	public NonErgodicSiteResponseMod getMod() {
		return (NonErgodicSiteResponseMod)getCurrentMod();
	}

}
