package org.opensha.data.region;


/**
 * This represents the smaller RELM Region
 * @author vipingupta
 *
 */
public class EvenlyGriddedRELM_TestingRegion extends EvenlyGriddedGeographicRegion {
	 private final static double GRID_SPACING = 0.10;

	  public EvenlyGriddedRELM_TestingRegion() {
		RELM_TestingRegion relmTestingRegion = new RELM_TestingRegion();
	    // make polygon from the location list
	    createEvenlyGriddedGeographicRegion(relmTestingRegion.getRegionOutline(), GRID_SPACING);
	  }
}
