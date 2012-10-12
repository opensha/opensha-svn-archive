package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter.DataForPaleoFaultPlots;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public abstract class CompoundFSSPlots {
	
	private static final Color BROWN = new Color(130, 86, 5);
	
	public static List<PlotSpec> getRegionalMFDPlotSpecs(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			List<Region> regions) {
		RegionalMFDPlot plot = new RegionalMFDPlot(weightProvider, regions);
		
		plot.buildPlot(fetch);
		
		return plot.specs;
	}
	
	public static void writeRegionalMFDPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			List<Region> regions,
			File dir, String prefix) throws IOException {
		List<PlotSpec> specs = getRegionalMFDPlotSpecs(fetch, weightProvider, regions);
		
		writeRegionalMFDPlots(specs, regions, dir, prefix);
	}
	
	public static void writeRegionalMFDPlots(
			List<PlotSpec> specs,
			List<Region> regions,
			File dir, String prefix) throws IOException {
		
		int unnamedRegionCnt = 0;
		
		for (int i=0; i<regions.size(); i++) {
			PlotSpec spec = specs.get(i);
			Region region = regions.get(i);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			CommandLineInversionRunner.setFontSizes(gp);
			gp.setYLog(true);
			gp.setUserBounds(5d, 9d, 1e-6, 1e0);
			
			gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
					spec.getFuncs(), spec.getChars(), true, spec.getTitle());
			
			String fname = prefix+"_MFD";
			if (region.getName() != null && !region.getName().isEmpty())
				fname += "_"+region.getName().replaceAll("\\W+", "_");
			else
				fname += "_UNNAMED_REGION_"+(++unnamedRegionCnt);
			
			File file = new File(dir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
		}
	}
	
	/**
	 * This creates MFD plots for a range of solutions
	 * 
	 * @author kevin
	 *
	 */
	public static class RegionalMFDPlot extends CompoundFSSPlots {
		
		private BranchWeightProvider weightProvider;
		private List<Region> regions;
		private List<Double> weights;
		
		// none (except min/mean/max which are always included)
		private double[] fractiles;
				
				// these are organized as (region, solution)
		private List<XY_DataSetList> solMFDs;
		private List<XY_DataSetList> solOffMFDs;
		private List<XY_DataSetList> solTotalMFDs;
				
		private static final double minX = 5.05d;
		private static final double maxX = 9.05d;
		private static final double delta = 0.1d;
		
		private List<PlotSpec> specs;
		
		public RegionalMFDPlot(BranchWeightProvider weightProvider,
				List<Region> regions) {
			this(weightProvider, regions, new double[0]);
		}
		
		public RegionalMFDPlot(BranchWeightProvider weightProvider,
				List<Region> regions, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.regions = regions;
			this.fractiles = fractiles;
			
			solMFDs = Lists.newArrayList();
			solOffMFDs = Lists.newArrayList();
			solTotalMFDs = Lists.newArrayList();
			
			for (int i=0; i<regions.size(); i++) {
				solMFDs.add(new XY_DataSetList());
				solOffMFDs.add(new XY_DataSetList());
				solTotalMFDs.add(new XY_DataSetList());
			}
			
			weights = Lists.newArrayList();
		}
		
		private static boolean isStatewide(Region region) {
			// TODO dirty...
			return region.getName().startsWith("RELM_TESTING");
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			double wt = weightProvider.getWeight(branch);
			
			InversionFaultSystemSolution invSol = null;
			
			if (sol instanceof InversionFaultSystemSolution)
				invSol = (InversionFaultSystemSolution)sol;
			
			List<DiscretizedFunc> onMFDs = Lists.newArrayList();
			List<DiscretizedFunc> offMFDs = Lists.newArrayList();
			List<DiscretizedFunc> totMFDs = Lists.newArrayList();
			
			for (int i=0; i<regions.size(); i++) {
				Region region = regions.get(i);
				
				IncrementalMagFreqDist onMFD = sol.calcNucleationMFD_forRegion(region, minX, maxX, delta, true);
				onMFDs.add(onMFD);
				if (isStatewide(region)) {
					// we only have off fault for statewide right now
					if (invSol == null)
						invSol = new InversionFaultSystemSolution(sol);
					IncrementalMagFreqDist offMFD = invSol.getFinalTrulyOffFaultMFD();
					EvenlyDiscretizedFunc trimmedOffMFD = new EvenlyDiscretizedFunc(
							onMFD.getMinX(), onMFD.getMaxX(), onMFD.getNum());
					EvenlyDiscretizedFunc totMFD = new EvenlyDiscretizedFunc(
							onMFD.getMinX(), onMFD.getMaxX(), onMFD.getNum());
					for (int n=0; n<trimmedOffMFD.getNum(); n++) {
						double x = trimmedOffMFD.getX(n);
						if (x <= offMFD.getMaxX())
							trimmedOffMFD.set(n, offMFD.getY(x));
						totMFD.set(n, onMFD.getY(n)+trimmedOffMFD.getY(n));
					}
					offMFDs.add(trimmedOffMFD);
					totMFDs.add(totMFD);
				} else {
					offMFDs.add(null);
					totMFDs.add(null);
				}
			}
			synchronized (this) {
				weights.add(wt);
				for (int i=0; i<regions.size(); i++) {
					solMFDs.get(i).add(onMFDs.get(i));
					if (offMFDs.get(i) != null)
						solOffMFDs.get(i).add(offMFDs.get(i));
					if (totMFDs.get(i) != null)
						solTotalMFDs.get(i).add(totMFDs.get(i));
				}
			}
		}

		@Override
		protected void finalizePlot() {
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch = null;
			
			specs = Lists.newArrayList();
			for (int i=0; i<regions.size(); i++) {
				Region region = regions.get(i);
				
				XY_DataSetList solMFDsForRegion = solMFDs.get(i);
				XY_DataSetList solOffMFDsForRegion = solOffMFDs.get(i);
				XY_DataSetList totalMFDsForRegion = solTotalMFDs.get(i);
				
				if (ucerf2Fetch == null)
					ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher(region);
				else
					ucerf2Fetch.setRegion(region);
				
				DiscretizedFunc ucerf2TotalMFD = ucerf2Fetch.getTotalMFD();
				DiscretizedFunc ucerf2OffMFD = ucerf2Fetch.getBackgroundSeisMFD();
				
				ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
				ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
				
				funcs.add(ucerf2TotalMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, BROWN));
				funcs.add(ucerf2OffMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.MAGENTA));
				
				if (!solOffMFDsForRegion.isEmpty()) {
					funcs.addAll(getFractiles(solOffMFDsForRegion, weights, "Solution Off Fault MFDs", fractiles));
					chars.addAll(getFractileChars(Color.GRAY, fractiles));
				}
				
				funcs.addAll(getFractiles(solMFDsForRegion, weights, "Solution On Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.BLUE, fractiles));
				
				if (!totalMFDsForRegion.isEmpty()) {
					funcs.addAll(getFractiles(totalMFDsForRegion, weights, "Solution Total MFDs", fractiles));
					chars.addAll(getFractileChars(Color.RED, fractiles));
				}
				
				String title = "Magnitude Histogram for Final Rates";
				if (region.getName() != null && !region.getName().isEmpty())
					title += " ("+region.getName()+")";
				
				String xAxisLabel = "Magnitude";
				String yAxisLabel = "Frequency (per bin)";
				
				PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel, yAxisLabel);
				specs.add(spec);
			}
		}

		@Override
		protected boolean usesInversionFSS() {
			for (Region region : regions)
				if (isStatewide(region))
					return true;
			return false;
		}
		
	}
	
	public static void writePaleoFaultPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir) throws IOException {
		PaleoFaultPlot plot = new PaleoFaultPlot(weightProvider);
		plot.buildPlot(fetch);
		
		writePaleoFaultPlots(plot.plotsMap, dir);
	}
	
	public static void writePaleoFaultPlots(
			Map<FaultModels, Map<String, PlotSpec[]>> plotsMap,
			File dir) throws IOException {
		boolean multiple = plotsMap.keySet().size() > 1;
		
		for (FaultModels fm : plotsMap.keySet()) {
			Map<String, PlotSpec[]> specs = plotsMap.get(fm);
			
			String prefix = null;
			if (multiple)
				prefix = fm.getShortName();
			
			CommandLineInversionRunner.writePaleoFaultPlots(specs, prefix, dir);
		}
	}
	
	public static class PaleoFaultPlot extends CompoundFSSPlots {
		
		private PaleoProbabilityModel paleoProbModel;
		private BranchWeightProvider weightProvider;
		
		// on demand
		private Map<FaultModels, Map<String, List<Integer>>> namedFaultsMaps = Maps.newHashMap();
		private Map<FaultModels, List<PaleoRateConstraint>> paleoConstraintMaps = Maps.newHashMap();
		private Map<FaultModels, List<AveSlipConstraint>> slipConstraintMaps = Maps.newHashMap();
		private Map<FaultModels, Map<Integer, List<FaultSectionPrefData>>> allParentsMaps = Maps.newHashMap();
		private Map<FaultModels, Map<Integer, Double>> traceLengthCaches = Maps.newHashMap();
		private Map<FaultModels, List<FaultSectionPrefData>> fsdsMap = Maps.newHashMap();
		
		// results
		private Map<FaultModels, List<DataForPaleoFaultPlots>> datasMap = Maps.newHashMap();
		private Map<FaultModels, List<List<Double>>> slipRatesMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();
		
		// plots
		private Map<FaultModels, Map<String, PlotSpec[]>> plotsMap = Maps.newHashMap();
		
		public PaleoFaultPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
			
			try {
				paleoProbModel = UCERF3_PaleoProbabilityModel.load();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			FaultModels fm = sol.getFaultModel();
			
			try {
				System.out.println("Preparing...");
				List<PaleoRateConstraint> paleoRateConstraints = paleoConstraintMaps.get(fm);
				if (paleoRateConstraints == null) {
					synchronized (fm) {
						paleoRateConstraints = paleoConstraintMaps.get(fm);
						if (paleoRateConstraints == null) {
							System.out.println("I'm in the synchronized block! "+fm);
							// do a bunch of FM specific stuff
							paleoRateConstraints = CommandLineInversionRunner.getPaleoConstraints(fm, sol);
							paleoConstraintMaps.put(fm, paleoRateConstraints);
							slipConstraintMaps.put(fm, AveSlipConstraint.load(sol.getFaultSectionDataList()));
							allParentsMaps.put(fm, PaleoFitPlotter.getAllParentsMap(sol.getFaultSectionDataList()));
							namedFaultsMaps.put(fm, fm.getNamedFaultsMapAlt());
							Map<Integer, Double> traceLengthCache = Maps.newConcurrentMap();
							traceLengthCaches.put(fm, traceLengthCache);
							fsdsMap.put(fm, sol.getFaultSectionDataList());
						}
					}
				}
				
				List<Double> slipsForConstraints = Lists.newArrayList();
				paleoRateConstraints = Lists.newArrayList(paleoRateConstraints);
				List<AveSlipConstraint> aveSlipConstraints = slipConstraintMaps.get(fm);
				for (AveSlipConstraint aveSlip : aveSlipConstraints) {
					double slip = sol.getSlipRateForSection(aveSlip.getSubSectionIndex());
					paleoRateConstraints.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(
							aveSlip, aveSlip.getSubSectionIndex(), slip));
					slipsForConstraints.add(slip);
				}
				
				Map<String, List<Integer>> namedFaultsMap = namedFaultsMaps.get(fm);
				
				Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap =
						PaleoFitPlotter.getNamedFaultConstraintsMap(
								paleoRateConstraints, sol.getFaultSectionDataList(), namedFaultsMap);
				
				Map<Integer, List<FaultSectionPrefData>> allParentsMap = allParentsMaps.get(fm);
				
				Map<Integer, Double> traceLengthCache = traceLengthCaches.get(fm);
				
				double weight = weightProvider.getWeight(branch);
				
				System.out.println("Building...");
				DataForPaleoFaultPlots data = DataForPaleoFaultPlots.build(
						sol, namedFaultsMap, namedFaultConstraintsMap, allParentsMap,
						paleoProbModel, traceLengthCache, weight);
				
				System.out.println("Archiving results...");
				
				synchronized (fm) {
					List<DataForPaleoFaultPlots> datasList = datasMap.get(fm);
					if (datasList == null) {
						datasList = Lists.newArrayList();
						datasMap.put(fm, datasList);
					}
					datasList.add(data);
					
					
					List<List<Double>> slipRates = slipRatesMap.get(fm);
					if (slipRates == null) {
						slipRates = Lists.newArrayList();
						for (int i=0; i<slipsForConstraints.size(); i++)
							slipRates.add(new ArrayList<Double>());
						slipRatesMap.put(fm, slipRates);
					}
					Preconditions.checkState(slipRates.size() == slipsForConstraints.size(),
							"Slip rate sizes inconsistent!");
					for (int i=0; i<slipRates.size(); i++)
						slipRates.get(i).add(slipsForConstraints.get(i));
					
					
					List<Double> weightsList = weightsMap.get(fm);
					if (weightsList == null) {
						weightsList = Lists.newArrayList();
						weightsMap.put(fm, weightsList);
					}
					weightsList.add(weight);
					
					System.out.println("Done calculating data for "+fm.getShortName()
							+" #"+(weightsList.size()+1));
				}
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}

		@Override
		protected void finalizePlot() {
			for (FaultModels fm : datasMap.keySet()) {
				// build compound ave slips
				List<AveSlipConstraint> aveSlips = slipConstraintMaps.get(fm);
				
				List<List<Double>> slipVals = slipRatesMap.get(fm);
				
				List<PaleoRateConstraint> paleoRateConstraints = paleoConstraintMaps.get(fm);
				
				double[] weights = Doubles.toArray(weightsMap.get(fm));
				
				for (int i=0; i<aveSlips.size(); i++) {
					List<Double> slipList = slipVals.get(i);
					double[] slipArray = Doubles.toArray(slipList);
					Preconditions.checkState(slipArray.length == weights.length);
					
					AveSlipConstraint constr = aveSlips.get(i);
					
					paleoRateConstraints.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(
							constr, constr.getSubSectionIndex(), slipArray, weights));
				}
				
				Map<String, List<Integer>> namedFaultsMap = namedFaultsMaps.get(fm);
				Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap =
						PaleoFitPlotter.getNamedFaultConstraintsMap(
								paleoRateConstraints, fsdsMap.get(fm), namedFaultsMap);
				
				List<DataForPaleoFaultPlots> datas = datasMap.get(fm);
				
				Map<Integer, List<FaultSectionPrefData>> allParentsMap = allParentsMaps.get(fm);
				
				Map<String, PlotSpec[]> specsMap = PaleoFitPlotter.getFaultSpecificPaleoPlotSpecs(
						namedFaultsMap, namedFaultConstraintsMap, datas, allParentsMap);
				
				plotsMap.put(fm, specsMap);
			}
		}

		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
		
	}
	
	private static List<DiscretizedFunc> getFractiles(
			XY_DataSetList data, List<Double> weights, String name, double[] fractiles) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		
		FractileCurveCalculator calc = new FractileCurveCalculator(data, weights);
		for (double fractile : fractiles) {
			DiscretizedFunc func = calc.getFractile(fractile);
			func.setName(name+" (fractile at "+fractile+")");
			funcs.add(func);
		}
		DiscretizedFunc minFunc = (DiscretizedFunc) calc.getMinimumCurve();
		minFunc.setName(name+" (minimum)");
		funcs.add(minFunc);
		DiscretizedFunc maxFunc = (DiscretizedFunc) calc.getMaximumCurve();
		maxFunc.setName(name+" (maximum)");
		funcs.add(maxFunc);
		DiscretizedFunc meanFunc = (DiscretizedFunc) calc.getMeanCurve();
		meanFunc.setName(name+" (weighted mean)");
		funcs.add(meanFunc);
		
		return funcs;
	}
	
	private static List<PlotCurveCharacterstics> getFractileChars(Color color, double[] fractiles) {
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		PlotCurveCharacterstics thinChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, color);
		PlotCurveCharacterstics thickChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color);
		
		for (int i=0; i<fractiles.length; i++)
			chars.add(thinChar);
		chars.add(thinChar);
		chars.add(thinChar);
		chars.add(thickChar);
		
		return chars;
	}
	
	/**
	 * Called once for each solution
	 * @param branch
	 * @param sol
	 */
	protected abstract void processSolution(LogicTreeBranch branch, FaultSystemSolution sol);
	
	/**
	 * Called at the end to finalize the plot
	 */
	protected abstract void finalizePlot();
	
	/**
	 * @return true if this plotter uses InversionFaultSystemSolution objects
	 */
	protected abstract boolean usesInversionFSS();
	
	/**
	 * This builds the plot individually (without utilizing efficiencies of working on multiple
	 * plots at once as you iterate over the solutions).
	 * 
	 * @param fetcher
	 */
	public void buildPlot(FaultSystemSolutionFetcher fetcher) {
		batchPlot(Lists.newArrayList(this), fetcher);
	}
	
	/**
	 * This builds multiple plots at once, only iterating through the solutions once. This should
	 * be much faster than calling buildPlot() on each plot.
	 * 
	 * @param plots
	 * @param fetcher
	 */
	public static void batchPlot(
			Collection<CompoundFSSPlots> plots,
			FaultSystemSolutionFetcher fetcher) {
		int threads = Runtime.getRuntime().availableProcessors();
		threads /= 2;
		if (threads < 1)
			threads = 1;
		batchPlot(plots, fetcher, threads);
	}
	
	private static class PlotSolComputeTask implements Task {
		
		private Collection<CompoundFSSPlots> plots;
		private FaultSystemSolutionFetcher fetcher;
		private LogicTreeBranch branch;
		private boolean invFSS;
		
		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher,
				LogicTreeBranch branch,
				boolean invFSS) {
			this.plots = plots;
			this.fetcher = fetcher;
			this.branch = branch;
			this.invFSS = invFSS;
		}

		@Override
		public void compute() {
			FaultSystemSolution sol = fetcher.getSolution(branch);
			
			if (invFSS)
				sol = new InversionFaultSystemSolution(sol);
			
			for (CompoundFSSPlots plot : plots)
				plot.processSolution(branch, sol);
		}
		
	}
	
	public static void batchPlot(
			Collection<CompoundFSSPlots> plots,
			FaultSystemSolutionFetcher fetcher,
			int threads) {
		boolean invFSS = false;
		for (CompoundFSSPlots plot : plots) {
			if (plot.usesInversionFSS()) {
				invFSS = true;
				break;
			}
		}
		
		List<Task> tasks = Lists.newArrayList();
		for (LogicTreeBranch branch : fetcher.getBranches()) {
			tasks.add(new PlotSolComputeTask(plots, fetcher, branch, invFSS));
		}
		
		System.out.println("Making "+plots.size()+" plot(s) with "+tasks.size()+" branches");
		
		// InvFSS objects use tons of memory
		if (invFSS && threads > 4)
			threads = 4;
		
		ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
		try {
			comp.computThreaded(threads);
		} catch (InterruptedException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		for (CompoundFSSPlots plot : plots)
			plot.finalizePlot();
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, IOException {
		File file = new File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
		FaultSystemSolutionFetcher fetch = CompoundFaultSystemSolution.fromZipFile(file);
//		fetch = FaultSystemSolutionFetcher.getRandomSample(fetch, 3);
		
		List<Region> regions = Lists.newArrayList();
		regions.add(new CaliforniaRegions.RELM_TESTING());
		regions.add(new CaliforniaRegions.RELM_NOCAL());
		regions.add(new CaliforniaRegions.RELM_SOCAL());
		regions.add(new CaliforniaRegions.LA_BOX());
		regions.add(new CaliforniaRegions.SF_BOX());
		
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
		File dir = new File("/tmp");
		String prefix = "2012_10_10-fm3-logic-tree-sample-first-247";
//		writeRegionalMFDPlots(fetch, weightProvider, regions, dir, prefix);
		File paleoDir = new File(dir, prefix+"-paleo-faults");
		writePaleoFaultPlots(fetch, weightProvider, paleoDir);
	}

}
