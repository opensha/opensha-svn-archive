package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_MultiSimAnalysisTools;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.utils.FaultSystemIO;

public class BombayBeachSwarm2016Calcs {

	public static void main(String[] args) throws IOException, DocumentException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_09_26-2016_bombay_swarm-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-noSpont");
		File binFile = new File(dir, "results.bin");
		
		long ot = 1474920000000l;
		long oneWeekOT = ot + 7*ProbabilityModelsCalc.MILLISEC_PER_DAY;
		
		List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(binFile);
		
		File plotDir = new File(dir, "plots");
		Preconditions.checkState(plotDir.exists() || plotDir.mkdir());
		
		String name = "2016 Bombay Swarm";
		
		ETAS_MultiSimAnalysisTools.mfdMaxY = 1e2;
		ETAS_MultiSimAnalysisTools.mfdMinY = 1e-6;
		
		ETAS_MultiSimAnalysisTools.plotMagNum(catalogs, plotDir, name, "10_year", null, 0, null);
		
		ETAS_MultiSimAnalysisTools.plotMagNum(catalogs, plotDir, name, "one_week", null, 0, null, oneWeekOT);
		
//		ETAS_MultiSimAnalysisTools.plotScalesOfHazardChange(catalogs, null, TestScenario.BOMBAY_BEACH_M4pt8, ot,
//				erf, outputDir, name, inputDuration, rates, subSects);
		
		double[] radii = { 200d, 150d, 100d, 50d, 25d };
		
		File fssFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
		FaultSystemSolution fss = FaultSystemIO.loadSol(fssFile);
		
		for (double radius : radii) {
			Region region = new Region(new Location(33.298, -115.713), radius);
			
			IncrementalMagFreqDist nuclMFD = fss.calcNucleationMFD_forRegion(region, 5.05, 9.05, 0.1, true);
			// scale to 1 week
			nuclMFD.scale(7d/365.25);
			EvenlyDiscretizedFunc nuclCmlMFD = nuclMFD.getCumRateDistWithOffset();
			
//			System.out.println("1 week Cumulative Nucleation MFD:");
//			System.out.println(nuclCmlMFD);
			double m7 = nuclCmlMFD.getY(7d);
			
			System.out.println((float)radius+"km: "+(float)m7);
		}
	}

}
