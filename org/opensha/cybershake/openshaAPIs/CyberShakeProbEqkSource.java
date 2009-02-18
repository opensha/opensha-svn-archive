package org.opensha.cybershake.openshaAPIs;

import java.util.ArrayList;

import org.opensha.calc.RelativeLocation;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.Site;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;

public class CyberShakeProbEqkSource extends ProbEqkSource {
	
	private ArrayList<CyberShakeEqkRupture> rups = new ArrayList<CyberShakeEqkRupture>();
	
	ProbEqkSource wrapperSource = null;
	
	public CyberShakeProbEqkSource(String name) {
		this.name = name;
	}
	
	public CyberShakeProbEqkSource(ProbEqkSource source, int sourceID, int erfID) {
		this.name = source.getName();
		wrapperSource = source;
		int rupID = 0;
		for (ProbEqkRupture rup : (ArrayList<ProbEqkRupture>)source.getRuptureList()) {
			CyberShakeEqkRupture csRup = new CyberShakeEqkRupture(rup, sourceID, rupID, erfID);
			rups.add(csRup);
			rupID++;
		}
	}
	
	public void addRupture(CyberShakeEqkRupture rup) {
		this.rups.add(rup);
	}

	@Override
	public double getMinDistance(Site site) {
		if (wrapperSource != null)
			return wrapperSource.getMinDistance(site);
		LocationList locs = this.getAllSourceLocs();
		
		double minDistance = Double.MAX_VALUE;
		
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.getLocationAt(i);
			
			double dist = RelativeLocation.getHorzDistance(loc, site.getLocation());
			
			if (dist < minDistance) {
				minDistance = dist;
			}
		}
		return minDistance;
	}

	@Override
	public int getNumRuptures() {
		return rups.size();
	}

	@Override
	public ProbEqkRupture getRupture(int rupture) {
		return rups.get(rupture);
	}

	public LocationList getAllSourceLocs() {
		EvenlyGriddedSurfaceAPI surface = this.getSourceSurface();
		
		return surface.getLocationList();
	}

	public EvenlyGriddedSurfaceAPI getSourceSurface() {
		if (wrapperSource != null)
			return wrapperSource.getSourceSurface();
		double maxMag = 0;
		int maxId = 0;
		int id = 0;
		for (CyberShakeEqkRupture rup : rups) {
			if (rup ==  null)
				throw new RuntimeException("Null rupture!");
			if (rup.getMag() > maxMag) {
				maxMag = rup.getMag();
				maxId = id;
			}
			id++;
		}
		
		return rups.get(maxId).getRuptureSurface();
	}

	@Override
	public boolean isPoissonianSource() {
		return false;
	}

}