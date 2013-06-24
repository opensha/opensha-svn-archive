package scratch.kevin.ucerf3.erf;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.faultSurface.CompoundGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;

import com.google.common.base.Stopwatch;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class ERF_Tester {
	
	private static void asdf(FaultSystemSolution sol) throws ZipException, IOException {
		FaultSystemRupSet rupSet = sol.getRupSet();
//		double upperDepthTol = 1000d;
		double upperDepthTol = 1d;
		boolean combineRakes = true;
		double combineMags = 0.01d;
//		Map<HashSet<String>, Double> rakeBasis = RuptureCombiner.loadRakeBasis(DeformationModels.GEOLOGIC);
		Map<HashSet<String>, Double> rakeBasis = null;
		if (combineRakes) {
			System.out.println("Loading rake basis...");
			ZipFile rakeBasisZip = new ZipFile(new File("/tmp/rake_basis.zip"));
			rakeBasis = RakeBasisWriter.loadRakeBasis(rakeBasisZip, DeformationModels.GEOLOGIC);
		}
		System.out.println("Combining rups for tol="+upperDepthTol+", combineRakes="+combineRakes+", combineMags="+combineMags);
		Stopwatch watch = new Stopwatch();
		watch.start();
		FaultSystemSolution reducedSol = RuptureCombiner.getCombinedSolution(sol, upperDepthTol, false, combineRakes, rakeBasis);
		if (combineMags > 0)
			reducedSol.setRupMagDists(RuptureCombiner.combineMFDs(combineMags, reducedSol.getRupMagDists()));
		FaultSystemRupSet reducedRupSet = reducedSol.getRupSet();
		watch.stop();
		System.out.println("DONE. Took "+watch.elapsed(TimeUnit.SECONDS)+"s to reduce to "
				+reducedRupSet.getNumRuptures()+"/"+rupSet.getNumRuptures()+" rups and "
				+reducedRupSet.getNumSections()+"/"+rupSet.getNumSections()+" sects");
		
		// make sure identical
//		RuptureCombiner.checkIdentical(sol, reducedSol, true);
		System.exit(0);
	}
	
	private static void debugOrig(FaultSystemSolution sol) {
		HashSet<HashSet<Integer>> rupsList = new HashSet<HashSet<Integer>>();
		FaultSystemRupSet rupSet = sol.getRupSet();
		for (int i=0; i<rupSet.getNumRuptures(); i++) {
			HashSet<Integer> sects = new HashSet<Integer>(rupSet.getSectionsIndicesForRup(i));
			
		}
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File meanSolFile = new File(invDir, "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL.zip");
		
		System.out.println("Loading solution...");
		FaultSystemSolution sol = FaultSystemIO.loadSol(meanSolFile);
//		asdf(sol);
		FaultSystemRupSet rupSet = sol.getRupSet();
		System.out.println("done.");
		
		System.out.println("Instantiating ERF...");
		MeanUCERF3 erf = new MeanUCERF3(sol);
		System.out.println("done.");
		
		System.out.println("Updating forecast...");
		erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
		erf.updateForecast();
		System.out.println("done.");
		
		System.out.println("sources: "+erf.getNumSources());
		int totRups = 0;
		long surfPts = 0;
		for (int i=0; i<erf.getNumSources(); i++) {
			ProbEqkSource source = erf.getSource(i);
			totRups += source.getNumRuptures();
			CompoundGriddedSurface surf = (CompoundGriddedSurface)source.getSourceSurface();
			for (EvenlyGriddedSurface subSurf : surf.getSurfaceList())
				surfPts += subSurf.size();
		}
		System.out.println("ruptures: "+totRups);
		System.out.println("surf locs: "+surfPts);
		System.exit(0);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		csv.addLine("Upper Depth Tol (km)", "Combine Mags?", "Combine Rakes?", "# FSS Rups", "# FSS Sects", "# Sources", "# Rups");
		
		double[] upperDepthTols = { 0, 1d, 5d, Double.POSITIVE_INFINITY };
		boolean[] rakeCombines = { false, true };
		
		for (double upperDepthTol : upperDepthTols) {
			for (boolean combineRakes : rakeCombines) {
				erf = null;
				System.gc();
				System.out.println("Combining rups for tol="+upperDepthTol+", combineRakes="+combineRakes);
				Stopwatch watch = new Stopwatch();
				watch.start();
				FaultSystemSolution reducedSol = RuptureCombiner.getCombinedSolution(sol, upperDepthTol, false, combineRakes, null);
				FaultSystemRupSet reducedRupSet = reducedSol.getRupSet();
				watch.stop();
				System.out.println("DONE. Took "+watch.elapsed(TimeUnit.SECONDS)+"s to reduce to "
						+reducedRupSet.getNumRuptures()+"/"+rupSet.getNumRuptures()+" rups and "
						+reducedRupSet.getNumSections()+"/"+rupSet.getNumSections()+" sects");
				System.out.println("Instantiating ERF...");
				erf = new MeanUCERF3(reducedSol);
				System.out.println("done.");

				System.out.println("Updating forecast...");
				erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
				erf.updateForecast();
				System.out.println("done.");

				System.out.println("sources: "+erf.getNumSources());
				totRups = 0;
				for (int i=0; i<erf.getNumSources(); i++)
					totRups += erf.getNumRuptures(i);
				System.out.println("ruptures: "+totRups);

				csv.addLine(upperDepthTol+"", false+"", combineRakes+"", reducedRupSet.getNumRuptures()+"",
						reducedRupSet.getNumSections()+"", erf.getNumSources()+"", totRups+"");
				
				// clear rup MFDs for averaged mags
				System.out.println("Clearning rup MFDs for mean mags");
				reducedSol.setRupMagDists(null);
				
				System.out.println("Instantiating ERF...");
				erf = new MeanUCERF3(reducedSol);
				System.out.println("done.");

				System.out.println("Updating forecast...");
				erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
				erf.updateForecast();
				System.out.println("done.");

				System.out.println("sources: "+erf.getNumSources());
				totRups = 0;
				for (int i=0; i<erf.getNumSources(); i++)
					totRups += erf.getNumRuptures(i);
				System.out.println("ruptures: "+totRups);

				csv.addLine(upperDepthTol+"", true+"", combineRakes+"", reducedRupSet.getNumRuptures()+"",
						reducedRupSet.getNumSections()+"", erf.getNumSources()+"", totRups+"");
			}
		}
		csv.writeToFile(new File("/tmp/mean_ucerf3_counts.csv"));
	}

}
