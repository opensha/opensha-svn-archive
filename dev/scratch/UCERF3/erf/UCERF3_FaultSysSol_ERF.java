package scratch.UCERF3.erf;

import java.util.ArrayList;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;


import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.griddedSeismicity.UCERF3_GridSourceGenerator;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class UCERF3_FaultSysSol_ERF extends FaultSystemSolutionPoissonERF {

	UCERF3_GridSourceGenerator ucerf3_gridSrcGen;
	
	
	public UCERF3_FaultSysSol_ERF(InversionFaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		
		ucerf3_gridSrcGen = new UCERF3_GridSourceGenerator(faultSysSolution, null,null, 8.54);

		numOtherSources = ucerf3_gridSrcGen.getNumSources();
//		numOtherSources=0;
		// treat as point sources
		ucerf3_gridSrcGen.setAsPointSources(true);
		System.out.println("numOtherSources="+numOtherSources);

	}
	
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		return ucerf3_gridSrcGen.getCrosshairSource(iSource - numFaultSystemSources, timeSpan.getDuration());
	}


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		super("/Users/field/ALLCAL_UCERF2.zip");

		InversionFaultSystemSolution invFss;
		SimpleFaultSystemSolution tmp = null;
		try {
			File f = new File("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
			tmp =  SimpleFaultSystemSolution.fromFile(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		invFss = new InversionFaultSystemSolution(tmp);

		
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss);

		
		erf.aleatoryMagAreaStdDevParam.setValue(0.0);
		erf.getTimeSpan().setDuration(1);
		long runtime = System.currentTimeMillis();

		// update forecast to we can get a main shock
		erf.updateForecast();
		System.out.println("numSrc here="+erf.getNumSources());
		
		try {
			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, "testUCERF3_ERF", "test", "testUCERF3_ERF");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
