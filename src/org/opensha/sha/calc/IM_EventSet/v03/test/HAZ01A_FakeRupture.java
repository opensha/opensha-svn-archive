package org.opensha.sha.calc.IM_EventSet.v03.test;

import org.opensha.commons.data.Location;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

public class HAZ01A_FakeRupture extends ProbEqkRupture {
	
	int sourceID;
	int rupID;
	
	public HAZ01A_FakeRupture(ProbEqkRupture rup, int sourceID, int rupID) {
		this(rup.getMag(), rup.getAveRake(), rup.getProbability(), rup.getRuptureSurface(),
				rup.getHypocenterLocation(), sourceID, rupID);
	}
	
	public HAZ01A_FakeRupture(double mag,
            double aveRake,
            double probability,
            EvenlyGriddedSurfaceAPI ruptureSurface,
            Location hypocenterLocation, int sourceID, int rupID) {
		super(mag, aveRake, probability, ruptureSurface, hypocenterLocation);
		this.sourceID = sourceID;
		this.rupID = rupID;
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getRupID() {
		return rupID;
	}

}
