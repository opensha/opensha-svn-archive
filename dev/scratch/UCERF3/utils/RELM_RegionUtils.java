package scratch.UCERF3.utils;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;

public class RELM_RegionUtils {
	
	
	public static CaliforniaRegions.RELM_TESTING_GRIDDED getGriddedRegionInstance() {
		return new CaliforniaRegions.RELM_TESTING_GRIDDED();
	}
	
	
	/**
	 * this creates a GriddedGeoDataSet based on a gridded RELM region and initializes 
	 * all the z values to zero
	 * @return
	 */
	public static GriddedGeoDataSet getRELM_RegionGeoDataSetInstance() {
		GriddedGeoDataSet data = new GriddedGeoDataSet(getGriddedRegionInstance(), true);
		for(int i=0;i<data.size();i++) data.set(i,0.0);
		return data;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
