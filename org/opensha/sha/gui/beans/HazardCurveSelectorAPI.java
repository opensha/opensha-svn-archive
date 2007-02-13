package org.opensha.sha.gui.beans;

import org.opensha.data.function.*;


/**
 * 
 * @author nitingupta
 *
 */
public interface HazardCurveSelectorAPI {

	
	public void getHazardCurve(DiscretizedFuncAPI function,String imt,double level);
	
}
