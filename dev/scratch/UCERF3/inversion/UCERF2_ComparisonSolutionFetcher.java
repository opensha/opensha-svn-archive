package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.DocumentException;
import org.opensha.commons.exceptions.GMT_MapException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.utils.RupSetIO;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoRateConstraintFetcher;

public class UCERF2_ComparisonSolutionFetcher {
	
//	private static Map<FaultModels, SimpleFaultSystemSolution> cache = Maps.newHashMap();
	private static Table<FaultModels, SlipAlongRuptureModels,
			InversionFaultSystemSolution> cache= HashBasedTable.create();
	
	/**
	 * This creates a UCERF2 reference solution for the given Fault Model. It uses
	 * Magnitudes from UCERF2 where available (when no mapping exists for a rupture, the Average
	 * UCERF2 MA is used which should be almost identical), rates form UCERF2, Tapered Dsr model
	 * and a constant subseismogenic thickness moment reduction (which is only relevant if you anneal
	 * this model, not if you just use the UCERF2 reference solution).
	 * 
	 * @param fm
	 * @return
	 */
	public static InversionFaultSystemSolution getUCERF2Solution(FaultModels fm) {
		return getUCERF2Solution(fm, SlipAlongRuptureModels.TAPERED);
	}
	
	public static InversionFaultSystemSolution getUCERF2Solution(FaultModels fm, SlipAlongRuptureModels slipModel) {
		InversionFaultSystemSolution sol = cache.get(fm, slipModel);
		if (sol == null) {
			DeformationModels dm;
			if (fm == FaultModels.FM2_1)
				dm = DeformationModels.UCERF2_ALL;
			else
				dm = DeformationModels.GEOLOGIC;
			InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					LaughTestFilter.getDefault(), 0, fm, dm, ScalingRelationships.AVE_UCERF2,
					slipModel, InversionModels.CHAR_CONSTRAINED, SpatialSeisPDF.UCERF2);

			sol = getUCERF2Solution(rupSet);
			
			cache.put(fm, slipModel, sol);
		}
		return sol;
	}
	
	public static InversionFaultSystemSolution getUCERF2Solution(InversionFaultSystemRupSet rupSet) {
		ArrayList<double[]> ucerf2_magsAndRates = InversionConfiguration.getUCERF2MagsAndrates(rupSet);

		InversionFaultSystemRupSet modRupSet = new InversionFaultSystemRupSet(rupSet, rupSet.getLogicTreeBranch(),
				rupSet.getLaughTestFilter(), rupSet.getAveSlipForAllRups(), rupSet.getCloseSectionsListList(),
				rupSet.getRupturesForClusters(), rupSet.getSectionsForClusters());

		double[] mags = new double[ucerf2_magsAndRates.size()];
		double[] rates = new double[ucerf2_magsAndRates.size()];
		for (int i=0; i<ucerf2_magsAndRates.size(); i++) {
			double[] ucerf2_vals = ucerf2_magsAndRates.get(i);
			if (ucerf2_vals == null) {
				mags[i] = rupSet.getMagForRup(i);
				rates[i] = 0;
			} else {
				mags[i] = ucerf2_vals[0];
				rates[i] = ucerf2_vals[1];
			}
		}

		modRupSet.setMagForallRups(mags);

		return new InversionFaultSystemSolution(modRupSet, rates, null, null);
	}
	
	public static void main(String[] args) throws GMT_MapException, RuntimeException, IOException, DocumentException {
		FaultModels fm = FaultModels.FM3_1;
		String prefix = "FM3_1_UCERF2_DsrTap_CharConst_COMPARE";
		File dir = new File("/tmp");
		
		InversionFaultSystemSolution sol = getUCERF2Solution(fm, SlipAlongRuptureModels.TAPERED);
//		BatchPlotGen.makeMapPlots(sol, dir, prefix);
		RupSetIO.writeSol(sol, new File(dir, prefix+"_sol.zip"));
		
//		ArrayList<PaleoRateConstraint> paleoConstraints = UCERF2_PaleoRateConstraintFetcher.getConstraints(sol.getFaultSectionDataList());
//		CommandLineInversionRunner.writePaleoPlots(paleoConstraints, null, sol, dir, prefix);
		
//		SimpleFaultSystemSolution sol = getUCERF2Solution(FaultModels.FM3_1);
//		BatchPlotGen.makeMapPlots(sol, new File("/tmp"), "ucerf2_fm3_compare");
	}

}
