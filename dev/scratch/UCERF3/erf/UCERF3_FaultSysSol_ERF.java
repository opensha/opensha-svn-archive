package scratch.UCERF3.erf;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;


import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.griddedSeismicity.UCERF3_GridSourceGenerator;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.ModUCERF2.NSHMP_GridSourceGeneratorMod2;


/**
 * Note that this does not yet include C zones (fixed strike sources)
 * @author field
 *
 */
public class UCERF3_FaultSysSol_ERF extends FaultSystemSolutionPoissonERF {

	private UCERF3_GridSourceGenerator ucerf3_gridSrcGen;
	
	public static final String NAME = "UCERF3 Poisson ERF";
	
	public UCERF3_FaultSysSol_ERF() {
		
		String f = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/refCH/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_run5_sol.zip";
		File file = new File(f);

		fileParam.setValue(file);
		
		bgIncludeParam.getEditor().setEnabled(true);
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
		bgRupTypeParam.getEditor().setEnabled(true);
		

	}
	
	public UCERF3_FaultSysSol_ERF(File file) {
		
		fileParam.setValue(file);
		
		bgIncludeParam.getEditor().setEnabled(true);
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
		bgRupTypeParam.getEditor().setEnabled(true);
	}

	
	public UCERF3_FaultSysSol_ERF(InversionFaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		bgIncludeParam.getEditor().setEnabled(true);
		bgIncludeParam.setValue(IncludeBackgroundOption.INCLUDE);
		bgRupTypeParam.getEditor().setEnabled(true);

	}
		
	@Override
	protected ProbEqkSource getOtherSource(int iSource) {
		return ucerf3_gridSrcGen.getSource(iSource, 
			timeSpan.getDuration(), applyAftershockFilter, true);
	}

	@Override
	protected void initOtherSources() {
			System.out.println("Initing other sources...");
	//		double totalRate = faultSysSolution.getTotalRateForAllFaultSystemRups() +
	//			faultSysSolution.getImpliedOffFaultStatewideMFD()
	//				.getTotalIncrRate();
	//		System.out.println("rateTest=" + totalRate);
	
			// KLUDGY need to have Inversion view of fault system solution
			
			InversionFaultSystemSolution ifss = new InversionFaultSystemSolution(faultSysSolution);
			ucerf3_gridSrcGen = new UCERF3_GridSourceGenerator(ifss);
			
			if (bgRupType.equals(BackgroundRupType.POINT)) {
				// default is false; gridGen will create point sources for those
				// with M<6 anyway; this forces those M>6 to be points as well
				ucerf3_gridSrcGen.setAsPointSources(true); 
				System.out.println("SET TO PTS");
			}
	
			// update parent source count
			numOtherSources = ucerf3_gridSrcGen.getNumSources();
	
			System.out.println(ucerf3_gridSrcGen.getNodeMFD(1000, 5.05));
			// treat as point sources
			System.out.println("numOtherSources=" + numOtherSources);
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String f = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/refGR/FM3_1_NEOK_EllB_DsrUni_GRConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_run5_sol.zip";
//		String f = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/refCH/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_run5_sol.zip";
		File file = new File(f);
				
		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF();
		erf.getParameter(FILE_PARAM_NAME).setValue(file);
		erf.updateForecast();
//		UCERF3_FaultSysSol_ERF erf = FaultSysSolutionERF_Calc.getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL_OFF,SmallMagScaling.MO_REDUCTION);
		int otherRups = 0;
		for (int i=0; i<erf.ucerf3_gridSrcGen.getNumSources(); i++) {
			ProbEqkSource src = erf.ucerf3_gridSrcGen.getSource(i, 1d, false, false);
			otherRups += src.getNumRuptures();
		}
		System.out.println("NumOtherRups: " + otherRups);
		System.out.println("src100rups: " + erf.getSource(100).getNumRuptures());
		System.out.println();
	}

}
