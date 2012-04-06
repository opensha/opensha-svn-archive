package scratch.kevin;

import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;

public class UCERF2EpistemicBackSeisTest {
	
	private static void print(AbstractEpistemicListERF listERF, String backSeis) {
		listERF.setParameter(UCERF2.BACK_SEIS_NAME, backSeis);
		ERF erf = listERF.getERF(0);
		System.out.println(backSeis+": "+erf.getNumSources());
		System.out.println("Back Seis according to ERF: "+
				erf.getAdjustableParameterList().getParameter(String.class, UCERF2.BACK_SEIS_NAME).getValue());
	}
	
	public static void print(AbstractEpistemicListERF erf) {
		print(erf, UCERF2.BACK_SEIS_INCLUDE);
		print(erf, UCERF2.BACK_SEIS_EXCLUDE);
		print(erf, UCERF2.BACK_SEIS_ONLY);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("*** UCERF2_TimeDependentEpistemicList ***");
		print(new UCERF2_TimeDependentEpistemicList());
		System.out.println("*** UCERF2_TimeIndependentEpistemicList ***");
		print(new UCERF2_TimeIndependentEpistemicList());
	}

}
