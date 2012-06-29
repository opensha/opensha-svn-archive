package org.opensha.sha.earthquake.param;

import java.util.EnumSet;

import org.opensha.commons.param.impl.EnumParameter;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class IncludeBackgroundParam extends EnumParameter<IncludeBackgroundOption> {
	
	public IncludeBackgroundParam() {
		super("Background Seismicity", EnumSet
			.allOf(IncludeBackgroundOption.class),
			IncludeBackgroundOption.EXCLUDE, null);
	}

}
