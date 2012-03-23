package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.enumTreeBranches.DeformationModels;

public class BatchPlotGen {
	
	private static ArrayList<double[]> partic_mag_ranges = new ArrayList<double[]>();
	static {
		partic_mag_ranges.add(toArray(6d, 7d));
		partic_mag_ranges.add(toArray(7d, 8d));
		partic_mag_ranges.add(toArray(8d, 10d));
	}
	
	private static double[] toArray(double... vals) {
		return vals;
	}
	
	public static void handleDir(File dir) throws IOException, DocumentException, GMT_MapException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				handleDir(file);
				continue;
			}
			String fileName = file.getName();
			if (!fileName.endsWith("_sol.zip"))
				continue;
			
			SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(file);
			
			Region region;
			if (sol.getDeformationModel() == DeformationModels.UCERF2_NCAL
					|| sol.getDeformationModel() == DeformationModels.UCERF2_BAYAREA)
				region = new CaliforniaRegions.RELM_NOCAL();
			else
				region = new CaliforniaRegions.RELM_TESTING();
			
			String prefix = fileName.substring(0, fileName.indexOf("_sol.zip"));
			
			FaultBasedMapGen.plotOrigNonReducedSlipRates(sol, region, dir, prefix, false);
			FaultBasedMapGen.plotOrigCreepReducedSlipRates(sol, region, dir, prefix, false);
			FaultBasedMapGen.plotTargetSlipRates(sol, region, dir, prefix, false);
			FaultBasedMapGen.plotSolutionSlipRates(sol, region, dir, prefix, false);
			FaultBasedMapGen.plotSolutionSlipMisfit(sol, region, dir, prefix, false);
			double[] ucerf2_rates = InversionConfiguration.getUCERF2Solution(sol);
			FaultSystemSolution ucerf2 = new SimpleFaultSystemSolution(sol, ucerf2_rates);
			for (double[] range : partic_mag_ranges) {
				FaultBasedMapGen.plotParticipationRates(sol, region, dir, prefix, false, range[0], range[1]);
				FaultBasedMapGen.plotParticipationRatios(sol, ucerf2, region, dir, prefix, false, range[0], range[1], true);
			}
			FaultBasedMapGen.plotSectionPairRates(sol, region, dir, prefix, false);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(BatchPlotGen.class)+" <directory>");
			System.exit(2);
		}
		
		try {
			File dir = new File(args[0]);
			Preconditions.checkArgument(dir.exists(), dir.getAbsolutePath()+" doesn't exist!");
			
			handleDir(dir);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

}
