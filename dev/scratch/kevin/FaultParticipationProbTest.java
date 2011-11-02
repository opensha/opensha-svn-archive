package scratch.kevin;

import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.GriddedSurfaceInterface;

public class FaultParticipationProbTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MeanUCERF2 ucerf = new MeanUCERF2();
		ucerf.updateForecast();
		
		GriddedSurfaceInterface bigSurface = ucerf.getSource(128).getSourceSurface();
		
		for (int sourceID=0; sourceID<ucerf.getNumSources(); sourceID++) {
			int numOnSurface = 0;
			int numOffSurface = 0;
			ProbEqkSource source = ucerf.getSource(sourceID);
			String name = source.getName();
			if (name.toLowerCase().contains("andreas")) {
				for (Location loc : source.getSourceSurface()) {
					boolean found = false;
					for (Location bigLoc : bigSurface) {
						if (bigLoc.equals(loc)) {
							found = true;
							break;
						}
					}
					if (!found)
						numOffSurface++;
					else
						numOnSurface++;
				}
				System.out.println(" on: " + numOnSurface + "\toff: " + numOffSurface + "\t" + name);
			}
		}
	}

}
