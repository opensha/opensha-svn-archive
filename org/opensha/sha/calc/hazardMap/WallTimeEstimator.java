package org.opensha.sha.calc.hazardMap;

import java.rmi.RemoteException;
import java.util.Random;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.grid.HazardMapPortionCalculator;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.imr.AttenuationRelationship;

public class WallTimeEstimator {
	
	public static double calcTimePerCurve(EqkRupForecast erf, AttenuationRelationship attenRel,
			SitesInGriddedRegionAPI sites, ArbitrarilyDiscretizedFunc hazFunction, int numSamples) {
		int numLocs = sites.getNumGridLocs();
		erf.updateForecast();
		if (numLocs < numSamples) {
			numSamples = numLocs;
		}
		
		HazardCurveCalculator calc = null;
		try {
			calc = new HazardCurveCalculator();
		} catch (RemoteException e) {
			return -1;
		}
		
		Random rand = new Random(System.currentTimeMillis());
		
		double times = 0;
		for (int i=0; i<numSamples; i++) {
			long start = System.currentTimeMillis();
			ArbitrarilyDiscretizedFunc logFunc = HazardMapPortionCalculator.getLogFunction(hazFunction);
			
			int siteNum = rand.nextInt(numLocs);
			
			try {
				calc.getHazardCurve(hazFunction, sites.getSite(siteNum), attenRel, erf);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (RegionConstraintException e) {
				e.printStackTrace();
			}
			
			hazFunction = HazardMapPortionCalculator.unLogFunction(hazFunction, logFunc);
			long end = System.currentTimeMillis();
			times += (double)(end - start) / 1000d;
		}
		
		return times / (double)numSamples;
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
