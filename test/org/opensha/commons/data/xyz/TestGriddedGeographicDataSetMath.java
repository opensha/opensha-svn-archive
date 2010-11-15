package org.opensha.commons.data.xyz;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;

public class TestGriddedGeographicDataSetMath extends TestXYZ_DataSetMath {
	
	GriddedRegionDataSet griddedData;
	
	@Before
	public void setUp() throws Exception {
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(0.5);
		
		griddedData = new GriddedRegionDataSet(region, true);
		
		for (int i=0; i<griddedData.size(); i++) {
			griddedData.set(i, (double)i);
		}
	}
	
	@Override
	protected XYZ_DataSetAPI getData1() {
		return griddedData;
	}
	
	@Override
	protected XYZ_DataSetAPI getData2() {
		return (GriddedRegionDataSet)griddedData.clone();
	}
	
	@Override
	protected XYZ_DataSetAPI getData3() {
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED(0.25);
		
		GriddedRegionDataSet data3 = new GriddedRegionDataSet(region, true);
		
		for (int i=0; i<data3.size(); i++) {
			data3.set(i, (double)i);
		}
		
		return data3;
	}

}
