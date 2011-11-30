package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionSolutionERF;
import scratch.UCERF3.oldStuff.OldInversionSolutionERF;

public class MultiFaultProbStats {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, DocumentException {
		File solFile = new File("D:\\Documents\\temp\\Inversion Results\\" +
		"dsa_4threads_50nodes_FAST_SA_dSub200_sub100_run3.xml");
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromXMLFile(solFile);

		ArrayList<Double> multiProbs = new ArrayList<Double>();
		MinMaxAveTracker multiTrack = new MinMaxAveTracker();
		ArrayList<Double> singleProbs = new ArrayList<Double>();
		MinMaxAveTracker singleTrack = new MinMaxAveTracker();

		int years = 30;
		
		System.out.println("Num rups: "+sol.getNumRuptures());

		for (int rupID=0; rupID<sol.getNumRuptures(); rupID++) {
			double rate = sol.getRateForRup(rupID);
			if (rate == 0)
				continue;
			List<FaultSectionPrefData> datas = sol.getFaultSectionDataForRupture(rupID);

			boolean single = OldInversionSolutionERF.isRuptureSingleParent(datas);

			double prob = OldInversionSolutionERF.calcProb(rate, years);
			
			if (!single) {
				// make sure it's not just a bunch of sections of the same fault
				boolean isActuallySame = true;
				String startsWith = null;
				for (FaultSectionPrefData data : datas) {
					String name = data.getParentSectionName();
					if (startsWith == null)
						startsWith = name.substring(0, 5);
					if (!name.startsWith(startsWith)) {
						isActuallySame = false;
						break;
					}
				}
				System.out.println("isActuallySame? "+isActuallySame+" : "
						+OldInversionSolutionERF.getRuptureSourceName(datas));
				single = isActuallySame;
			}

			if (single) {
				singleProbs.add(prob);
				singleTrack.addValue(prob);
			} else {
				multiProbs.add(prob);
				multiTrack.addValue(prob);
			}
		}

		System.out.println("Results for "+solFile.getName()+" ("+years+" years)");
		System.out.println("Num non zero: single: "+singleProbs.size()+", multi: "+multiProbs.size());
		System.out.println("Single Fault Ruptures:");
		printStats(singleProbs, singleTrack);
		System.out.println("Multi Fault Ruptures:");
		printStats(multiProbs, multiTrack);
	}

	private static void printStats(ArrayList<Double> probs, MinMaxAveTracker tracker) {
		System.out.println("Min:\t"+tracker.getMin());
		System.out.println("Max:\t"+tracker.getMax());
		System.out.println("Avg:\t"+tracker.getAverage());
		System.out.println("Med:\t"+median(probs));

	}

	public static double median(ArrayList<Double> values){
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return values.get((values.size()+1)/2-1);
		
		double lower = values.get(values.size()/2-1);
		double upper = values.get(values.size()/2);

		return (lower + upper) / 2.0;
	}

}
