package scratch.UCERF3.erf;

import java.util.ArrayList;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;

import java.awt.Toolkit;


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
	
	
	public UCERF3_FaultSysSol_ERF(String fullPathInputFileForFltSysSol) {
//		super("/Users/field/ALLCAL_UCERF2.zip");
		super(fullPathInputFileForFltSysSol);
		
		ucerf3_gridSrcGen = new UCERF3_GridSourceGenerator((InversionFaultSystemSolution)faultSysSolution, null,null, 8.54);

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
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF("/Users/field/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
		erf.aleatoryMagAreaStdDevParam.setValue(0.0);
		erf.getTimeSpan().setDuration(1);
		long runtime = System.currentTimeMillis();

		// update forecast to we can get a main shock
		erf.updateForecast();
		System.out.println("numSrc="+erf.getNumSources());
		
	}

}
