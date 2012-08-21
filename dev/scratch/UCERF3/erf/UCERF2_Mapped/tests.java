package scratch.UCERF3.erf.UCERF2_Mapped;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;

import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1_wOutAftershocks;

public class tests {
	
	
	public static void testMFDs() {
		UCERF2_FM2pt1_FaultSysSolERF erf = getUCERF2_FM2pt1_FaultSysSolERF();
		System.out.println("ADJUSTABLE PARAMS FOR UCERF2_FM2pt1_FaultSysSolERF:");
		System.out.println(erf.getAdjustableParameterList().getParameterListMetadataString("\n"));
		
		ModMeanUCERF2_FM2pt1_wOutAftershocks erf_orig = getModMeanUCERF2_FM2pt1_wOutAftershocks();
		System.out.println("ADJUSTABLE PARAMS FOR ModMeanUCERF2_FM2pt1_wOutAftershocks:");
		System.out.println(erf_orig.getAdjustableParameterList().getParameterListMetadataString("\n"));


	}
	
	/**
	 * fault source indices: 0 to 6978
	 * grid src indices: 6979 to 14721
	 * fixed strike sources: 14722 to 16232
	 * @return
	 */
	public static UCERF2_FM2pt1_FaultSysSolERF getUCERF2_FM2pt1_FaultSysSolERF() {
		UCERF2_FM2pt1_FaultSysSolERF erf = new UCERF2_FM2pt1_FaultSysSolERF();
		erf.getTimeSpan().setDuration(50.0);
		erf.updateForecast();
//		System.out.println("getNumSources()=\t"+erf.getNumSources());
//		System.out.println("6978\t"+erf.getSource(6978).getName());
//		System.out.println("6979\t"+erf.getSource(6979).getName());
//		System.out.println("14721\t"+erf.getSource(14721).getName());
//		System.out.println("14722\t"+erf.getSource(14722).getName());
//		System.out.println("16232\t"+erf.getSource(16232).getName());
		return erf;

	}
	
	/**
	 * 
	 * CA fault source indices:	0 to 274
	 * Non CA fault source indices: 274 to 393
	 * fixed strike grid source indices: 394 to 1904
	 * background seis source indices: 1905 to 9647
	 * @return
	 */
	public static ModMeanUCERF2_FM2pt1_wOutAftershocks getModMeanUCERF2_FM2pt1_wOutAftershocks() {
		ModMeanUCERF2_FM2pt1_wOutAftershocks erf = new ModMeanUCERF2_FM2pt1_wOutAftershocks();
		erf.getTimeSpan().setDuration(50);
		erf.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		erf.updateForecast();
//		for(int s=0; s<testERF.getNumSources();s++) {
//			System.out.println(s+"\t"+testERF.getSource(s).getName());
//		}
		return erf;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		testMFDs();
	}

}
