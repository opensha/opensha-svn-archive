package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.opensha.commons.exceptions.GMT_MapException;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.griddedSeismicity.SpatialSeisPDF;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoRateConstraintFetcher;

public class UCERF2_ComparisonSolutionFetcher {
	
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
	public static SimpleFaultSystemSolution getUCERF2Solution(FaultModels fm) {
		DeformationModels dm;
		if (fm == FaultModels.FM2_1)
			dm = DeformationModels.UCERF2_ALL;
		else
			dm = DeformationModels.GEOLOGIC_PLUS_ABM;
		FaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
				fm, dm, MagAreaRelationships.AVE_UCERF2, AveSlipForRupModels.AVE_UCERF2,
				SlipAlongRuptureModels.TAPERED, InversionModels.CHAR, LaughTestFilter.getDefault(), 0,
				8.7, 7.6, false, SpatialSeisPDF.UCERF2);
		
		ArrayList<double[]> ucerf2_magsAndRates = InversionConfiguration.getUCERF2MagsAndrates(rupSet);
		
		SimpleFaultSystemRupSet modRupSet = new SimpleFaultSystemRupSet(rupSet);
		
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
		
		return new SimpleFaultSystemSolution(modRupSet, rates);
	}
	
	public static void main(String[] args) throws GMT_MapException, RuntimeException, IOException, DocumentException {
		FaultModels fm = FaultModels.FM3_1;
		String prefix = "ucerf2_fm3_compare";
		File dir = new File("/tmp");
		
		SimpleFaultSystemSolution sol = getUCERF2Solution(fm);
		BatchPlotGen.makeMapPlots(sol, dir, prefix);
		sol.toZipFile(new File(dir, prefix+".zip"));
		
		ArrayList<PaleoRateConstraint> paleoConstraints = UCERF2_PaleoRateConstraintFetcher.getConstraints(sol.getFaultSectionDataList());
		CommandLineInversionRunner.writePaleoPlots(paleoConstraints, sol, dir, prefix);
		
//		SimpleFaultSystemSolution sol = getUCERF2Solution(FaultModels.FM3_1);
//		BatchPlotGen.makeMapPlots(sol, new File("/tmp"), "ucerf2_fm3_compare");
	}

}
