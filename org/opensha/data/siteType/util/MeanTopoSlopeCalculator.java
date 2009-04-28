package org.opensha.data.siteType.util;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedCircularGeographicRegion;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.impl.SRTM30PlusTopoSlope;

public class MeanTopoSlopeCalculator {
	
	SiteDataAPI<Double> topoSlopeProvider;
	
	public MeanTopoSlopeCalculator(SiteDataAPI<Double> topoSlopeProvider) {
		if (!topoSlopeProvider.getDataType().equals(SiteDataAPI.TYPE_TOPOGRAPHIC_SLOPE)) {
			throw new IllegalArgumentException("The given Site Data provider must be of type 'Topographic Slope'");
		}
		
		this.topoSlopeProvider = topoSlopeProvider;
	}
	
	private EvenlyGriddedGeographicRegionAPI createRegionAroundSite(Location loc, double radius, double gridSpacing) {
		return new EvenlyGriddedCircularGeographicRegion(loc, radius, gridSpacing);
	}
	
	/**
	 * Get mean topographic slope for a circular region around the given location
	 * 
	 * @param loc - location for center of circle
	 * @param radius - radius in KM
	 * @param gridSpacing - grid spacing in degrees
	 * @return
	 * @throws IOException
	 */
	public double getMeanSlope(Location loc, double radius, double gridSpacing) throws IOException {
		EvenlyGriddedGeographicRegionAPI region = createRegionAroundSite(loc, radius, gridSpacing);
		
		return getMeanSlope(region);
	}
	
	public double getMeanSlope(EvenlyGriddedGeographicRegionAPI region) throws IOException {
		return getMeanSlope(region.getGridLocationsList());
	}
	
	public double getMeanSlope(LocationList locs) throws IOException {
		ArrayList<Double> vals = topoSlopeProvider.getValues(locs);
		
		double tot = 0;
		
		for (double val : vals) {
			tot += val;
		}
		
		double mean = tot / (double)vals.size();
		
		return mean;
	}
	
	public static void main(String args[]) throws IOException {
		SiteDataAPI<Double> topoSlopeProvider = new SRTM30PlusTopoSlope();
		MeanTopoSlopeCalculator calc = new MeanTopoSlopeCalculator(topoSlopeProvider);
		
		System.out.println("34, -118: " + calc.getMeanSlope(new Location(34, -118), 300, 0.1));
	}

}
