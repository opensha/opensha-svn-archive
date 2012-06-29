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
public class BackgroundRupParam extends EnumParameter<BackgroundRupType> {

	public BackgroundRupParam() {
		super("Treat Background Seismicity As", EnumSet
			.allOf(BackgroundRupType.class), BackgroundRupType.POINT, null);
	}

}
