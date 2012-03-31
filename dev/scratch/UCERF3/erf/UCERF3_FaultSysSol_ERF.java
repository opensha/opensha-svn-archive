package scratch.UCERF3.erf;

import java.io.File;
import java.io.IOException;

import org.opensha.sha.earthquake.ProbEqkSource;


import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.griddedSeismicity.GridSourceType;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.griddedSeismicity.SpatialSeisPDF;
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
	
	
	public UCERF3_FaultSysSol_ERF(InversionFaultSystemSolution faultSysSolution, SpatialSeisPDF spatialPDF, SmallMagScaling scalingMethod) {
		super(faultSysSolution);
		
		double totalRate = faultSysSolution.getTotalRateForAllFaultSystemRups()+
							faultSysSolution.getImpliedOffFaultStatewideMFD().getTotalIncrRate();
		
		System.out.println("rateTest="+totalRate);
		
//		ucerf3_gridSrcGen = new UCERF3_GridSourceGenerator(faultSysSolution,null,SpatialSeisPDF.AVG_DEF_MODEL,8.54,SmallMagScaling.MO_REDUCTION);
		ucerf3_gridSrcGen = new UCERF3_GridSourceGenerator(faultSysSolution,null,spatialPDF,8.54,scalingMethod);

		numOtherSources = ucerf3_gridSrcGen.getNumSources();
//		numOtherSources=0;
		// treat as point sources
		ucerf3_gridSrcGen.setAsPointSources(true);
		System.out.println("numOtherSources="+numOtherSources);

	}
	
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		int idx = iSource - numFaultSystemSources;
		return ucerf3_gridSrcGen.getSource(GridSourceType.CROSSHAIR, idx, 
			timeSpan.getDuration());
	}


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
		UCERF3_FaultSysSol_ERF erf = FaultSysSolutionERF_Calc.getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.MO_REDUCTION);
		
	}

}
