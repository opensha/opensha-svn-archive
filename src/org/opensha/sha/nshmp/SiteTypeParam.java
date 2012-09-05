package org.opensha.sha.nshmp;

import static org.opensha.sha.nshmp.SiteType.FIRM_ROCK;
import static org.opensha.sha.nshmp.SiteType.HARD_ROCK;

import java.util.EnumSet;

import org.opensha.commons.param.impl.EnumParameter;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@Deprecated
public class SiteTypeParam extends EnumParameter<SiteType> {

	public SiteTypeParam() {
		super("Site Type", EnumSet.of(FIRM_ROCK, HARD_ROCK), FIRM_ROCK, null);
	}

}
