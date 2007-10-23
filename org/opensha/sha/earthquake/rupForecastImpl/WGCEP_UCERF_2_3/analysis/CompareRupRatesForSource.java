package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.MeanUCERF.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2_TimeIndependentEpistemicList;


/**
 * It chooses a source and then compares the rupture rates from Logic Tree UCERF2
 * and MeanUCERF2 for that source.
 * 
 * 
 * @author vipingupta
 *
 */
public class CompareRupRatesForSource {
	public static void main(String[] args) {
		double duration = 1;
		try {
		// UCERF 2 epistemic list
		UCERF2_TimeIndependentEpistemicList erfList = new UCERF2_TimeIndependentEpistemicList();
		int numERFs = erfList.getNumERFs();
		erfList.getTimeSpan().setDuration(duration);
		HashMap<Integer,Double> rupRateMapping = new HashMap<Integer,Double>();
		for(int erfIndex=0; erfIndex<numERFs; ++erfIndex) {
			System.out.println("Doing "+erfIndex+ " of "+numERFs)
;			UCERF2 ucerf2 = (UCERF2) erfList.getERF(erfIndex);
			double wt = erfList.getERF_RelativeWeight(erfIndex);
			int numSources = ucerf2.getNumSources();	
			// Iterate over all sources
			for(int srcIndex=0; srcIndex<numSources; ++srcIndex) {
				ProbEqkSource source = ucerf2.getSource(srcIndex);
				String srcName = source.getName();
				if(!srcName.equalsIgnoreCase("Sierra Madre")) continue;
				int numRups = source.getNumRuptures();
				for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
					double rate = source.getRupture(rupIndex).getMeanAnnualRate(duration);
					if(!rupRateMapping.containsKey(rupIndex)) rupRateMapping.put(rupIndex, 0.0);
					double newRate = rupRateMapping.get(rupIndex)+wt*rate;
					rupRateMapping.put(rupIndex, newRate);
				}
			}

		}
		
		FileWriter fw = new FileWriter("SierraMadreLogicTreeUCERF2.txt");
		Iterator<Integer> it = rupRateMapping.keySet().iterator();
		while(it.hasNext()) {
			int rupIndex = it.next();
			fw.write(rupIndex+"\t"+rupRateMapping.get(rupIndex)+"\n");
		}
		fw.close();
		
		// Mean UCERF 2
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(MeanUCERF2.PROB_MODEL_PARAM_NAME, MeanUCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(MeanUCERF2.BACK_SEIS_NAME, MeanUCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.getTimeSpan().setDuration(duration);
		fw = new FileWriter("SierraMadreMeanUCERF2.txt");
		meanUCERF2.updateForecast();
		int numSources = meanUCERF2.getNumSources();	
		// Iterate over all sources
		for(int srcIndex=0; srcIndex<numSources; ++srcIndex) {
			ProbEqkSource source = meanUCERF2.getSource(srcIndex);
			if(!source.getName().equalsIgnoreCase("Sierra Madre")) continue;
			int numRups = source.getNumRuptures();
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
				double rate=source.getRupture(rupIndex).getMeanAnnualRate(duration);
				fw.write(rupIndex+"\t"+rate+"\n");
			}
		}
		
		fw.close();
		}catch(Exception e) { e.printStackTrace(); }
	}
}
