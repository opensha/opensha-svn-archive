/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.MeanUCERF.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2_TimeIndependentEpistemicList;

/**
 * This claas compares the sources rates of all ruptures for all logic tree branches
 * of UCERF2 and avergae UCERF2
 * 
 * 
 * @author vipingupta
 *
 */
public class CompareUCERF_SourceRates {

	public static void main(String[] args) {
		double duration = 1;
		try {
		// UCERF 2 epistemic list
		UCERF2_TimeIndependentEpistemicList erfList = new UCERF2_TimeIndependentEpistemicList();
		int numERFs = erfList.getNumERFs();
		erfList.getTimeSpan().setDuration(duration);
		HashMap<String,Double> srcRateMapping = new HashMap<String,Double>();
		for(int erfIndex=0; erfIndex<numERFs; ++erfIndex) {
			UCERF2 ucerf2 = (UCERF2) erfList.getERF(erfIndex);
			double wt = erfList.getERF_RelativeWeight(erfIndex);
			int numSources = ucerf2.getNumSources();	
			// Iterate over all sources
			for(int srcIndex=0; srcIndex<numSources; ++srcIndex) {
				ProbEqkSource source = ucerf2.getSource(srcIndex);
				int numRups = source.getNumRuptures();
				double meanAnnualRate = 0;
				for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
					meanAnnualRate+=source.getRupture(rupIndex).getMeanAnnualRate(duration);
				}
				String srcName = source.getName();
				if(!srcRateMapping.containsKey(srcName)) srcRateMapping.put(srcName, 0.0);
				double newRate = srcRateMapping.get(srcName)+wt*meanAnnualRate;
				srcRateMapping.put(srcName, newRate);
			}

		}
		
		FileWriter fw = new FileWriter("LogicTreeUCERF2.txt");
		Iterator<String> it = srcRateMapping.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			fw.write(name+"\t"+srcRateMapping.get(name)+"\n");
		}
		fw.close();
		
		// Mean UCERF 2
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(MeanUCERF2.PROB_MODEL_PARAM_NAME, MeanUCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(MeanUCERF2.BACK_SEIS_NAME, MeanUCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.getTimeSpan().setDuration(duration);
		fw = new FileWriter("MeanUCERF2.txt");
		meanUCERF2.updateForecast();
		int numSources = meanUCERF2.getNumSources();	
		// Iterate over all sources
		for(int srcIndex=0; srcIndex<numSources; ++srcIndex) {
			ProbEqkSource source = meanUCERF2.getSource(srcIndex);
			int numRups = source.getNumRuptures();
			double meanAnnualRate = 0;
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
				meanAnnualRate+=source.getRupture(rupIndex).getMeanAnnualRate(duration);
			}
			fw.write(source.getName()+"\t"+meanAnnualRate+"\n");
		}
		
		fw.close();
		}catch(Exception e) { e.printStackTrace(); }
	}
	
}
