package scratch.UCERF3.erf;

import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;

import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class UCERF2_FaultSysSol_ERF extends FaultSystemSolutionTimeDepERF {

	NSHMP_GridSourceGenerator nshmp_gridSrcGen;
	
	
	public UCERF2_FaultSysSol_ERF() {
		super("/Users/field/ALLCAL_UCERF2.zip");
		nshmp_gridSrcGen = new NSHMP_GridSourceGeneratorMod2();
		numOtherSources = nshmp_gridSrcGen.getNumSources();
		// treat as point sources
		nshmp_gridSrcGen.setAsPointSources(true);
		System.out.println("numOtherSources="+numOtherSources);

	}
	
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		return nshmp_gridSrcGen.getCrosshairGriddedSource(iSource - numFaultSystemSources, timeSpan.getDuration());
	}


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
		erf.aleatoryMagAreaStdDevParam.setValue(0.0);
		erf.bpt_AperiodicityParam.setValue(0.2);
		erf.getTimeSpan().setStartTimeInMillis(0);
		erf.getTimeSpan().setDuration(10000);
		long runtime = System.currentTimeMillis();
//		erf.updateForecast();
		erf.testER_Simulations();
		runtime -= System.currentTimeMillis();
		System.out.println("simulation took "+runtime/(1000*60)+" minutes");


	}

}
