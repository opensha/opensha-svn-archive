package org.opensha.sha.cybershake.timeDep;

import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;

public class TimeDependentRupProbMod implements RuptureProbabilityModifier {
	
	private EqkRupForecastAPI erf;
	
	public TimeDependentRupProbMod() {
		erf = MeanUCERF2_ToDB.createUCERF2ERF();
		erf.getAdjustableParameterList().getParameter(UCERF2.PROB_MODEL_PARAM_NAME)
				.setValue(MeanUCERF2.PROB_MODEL_WGCEP_PREF_BLEND);
		erf.getTimeSpan().setStartTime(2010);
		erf.updateForecast();
	}

	public double getModifiedProb(int sourceID, int rupID, double origProb) {
		return erf.getRupture(sourceID, rupID).getProbability();
	}

}
