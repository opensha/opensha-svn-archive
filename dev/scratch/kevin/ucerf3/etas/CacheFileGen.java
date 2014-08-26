package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator;
import scratch.UCERF3.erf.ETAS.FaultSystemSolutionERF_ETAS;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ParameterList;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.RELM_RegionUtils;

public class CacheFileGen {

	public static void main(String[] args) throws IOException, DocumentException {
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		File solFile = new File(args[0]);
		FaultSystemSolution fss = FaultSystemIO.loadSol(solFile);
//		FaultSystemSolutionERF_ETAS erf = MPJ_ETAS_Simulator.buildERF(fss, false, 1d);
//		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF(fss);
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF();
		erf.updateForecast();
		
		File resultsDir = new File("/tmp");
		GriddedRegion reg = RELM_RegionUtils.getGriddedRegionInstance();
		
		long randSeed = 1408453138855l;
		
		boolean includeEqkRates = true;
		boolean includeIndirectTriggering = true;
		boolean includeSpontEvents = true;
		
		double gridSeisDiscr = 0.1;
		
		ObsEqkRupList histQkList = new ObsEqkRupList();
		
		ETAS_Simulator.D = false;
		
//		ETAS_EqkRupture mainshockRup = null;
		ETAS_EqkRupture mainshockRup = ETAS_Simulator.buildScenarioRup(TestScenario.MOJAVE, erf);
//		ETAS_EqkRupture mainshockRup = new ETAS_EqkRupture();
//		long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
//		mainshockRup.setOriginTime(ot);
//		
//		// Mojave M 7.05 rupture
//		int fssScenarioRupID = 30473;
//		mainshockRup.setAveRake(fss.getRupSet().getAveRakeForRup(fssScenarioRupID));
//		mainshockRup.setMag(fss.getRupSet().getMagForRup(fssScenarioRupID));
//		mainshockRup.setRuptureSurface(fss.getRupSet().getSurfaceForRupupture(fssScenarioRupID, 1d, false));
//		mainshockRup.setID(0);
//		erf.setFltSystemSourceOccurranceTimeForFSSIndex(fssScenarioRupID, ot);
//		
//		erf.updateForecast();
		
		ETAS_Simulator.testETAS_Simulation(resultsDir, erf, reg, mainshockRup, histQkList, includeSpontEvents,
				includeIndirectTriggering, includeEqkRates, gridSeisDiscr, null, randSeed, new ETAS_ParameterList());
	}

}
