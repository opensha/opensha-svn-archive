package org.opensha.sha.calc.hazardMap.components;

import org.opensha.commons.data.Site;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.util.TectonicRegionType;

import java.util.HashMap;

/**
 * Metadata for the curves that's necessary for archiving them such as the location and
 * information about the IMRs used to caclulate them.
 * 
 * @author kevin
 *
 */
public class CurveMetadata {
	
	private Site site;
	private HashMap<TectonicRegionType, ScalarIMR> imrMap;
	private String shortLabel;
	
	public CurveMetadata(Site site,
			HashMap<TectonicRegionType, ScalarIMR> imrMap,
			String shortLabel) {
		this.site = site;
		this.imrMap = imrMap;
		this.shortLabel = shortLabel;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public HashMap<TectonicRegionType, ScalarIMR> getImrMap() {
		return imrMap;
	}

	public void setImrMap(
			HashMap<TectonicRegionType, ScalarIMR> imrMap) {
		this.imrMap = imrMap;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

}
