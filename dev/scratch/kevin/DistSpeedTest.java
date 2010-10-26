package scratch.kevin;

import java.util.Iterator;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

public class DistSpeedTest {
	
	public static void main(String[] args) {
		EqkRupForecast erf = new MeanUCERF2();
		erf.updateForecast();
		
		Location testLoc = new Location(34, -118);
		
		long start = System.currentTimeMillis();
		
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				EvenlyGriddedSurfaceAPI surface = rup.getRuptureSurface();
				
				Iterator<Location> it = surface.iterator();
				while (it.hasNext()) {
					Location loc = it.next();
//					LocationUtils.linearDistance(testLoc, loc);
					LocationUtils.linearDistanceFast(testLoc, loc);
				}
			}
		}
		
		long milis = System.currentTimeMillis() - start;
		double secs = (double)milis / 1000d;
		
		System.out.println("Took " + secs + " secs");
	}

}
