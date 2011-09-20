package scratch.kevin.ucerf3;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;

public class PureScratch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ERF erf = new MeanUCERF2();
		erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		erf.getTimeSpan().setDuration(1, TimeSpan.YEARS);
		erf.updateForecast();
		
//		for (ProbEqkSource source : erf) {
//			for (ProbEqkRupture rup : erf) {
//				
//			}
//		}
		MinMaxAveTracker track = new MinMaxAveTracker();
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); sourceID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				track.addValue(rup.getProbability());
			}
		}
		System.out.println(track);
	}

}
