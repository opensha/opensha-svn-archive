package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipException;

import org.apache.commons.math.stat.StatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.InversionMFDs;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.UCERF2_Section_MFDs.UCERF2_Section_MFDsCalc;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter.DataForPaleoFaultPlots;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoSiteCorrelationData;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;
import scratch.kevin.DeadlockDetectionThread;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;

public abstract class CompoundFSSPlots implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		
		public static List<Region> getDefaultRegions() {
			List<Region> regions = Lists.newArrayList();
			regions.add(new CaliforniaRegions.RELM_TESTING());
			regions.add(new CaliforniaRegions.RELM_NOCAL());
			regions.add(new CaliforniaRegions.RELM_SOCAL());
			regions.add(new CaliforniaRegions.LA_BOX());
			regions.add(new CaliforniaRegions.SF_BOX());
			return regions;
		}
		
		private transient BranchWeightProvider weightProvider;
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
					IncrementalMagFreqDist offMFD = invSol.getFinalTotalGriddedSeisMFD();
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
			
			System.out.println("Finalizing MFD plot for "+solMFDs.get(0).size()+" branches!");
			
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
					// now add target GRs
					funcs.add(InversionMFDs.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_10p0.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
					funcs.add(InversionMFDs.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_8p7.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
					funcs.add(InversionMFDs.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_7p6.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
					
					funcs.addAll(getFractiles(solOffMFDsForRegion, weights, "Solution Off Fault MFDs", fractiles));
					chars.addAll(getFractileChars(Color.GRAY, fractiles.length));
				}
				
				funcs.addAll(getFractiles(solMFDsForRegion, weights, "Solution On Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.BLUE, fractiles.length));
				
				if (!totalMFDsForRegion.isEmpty()) {
					funcs.addAll(getFractiles(totalMFDsForRegion, weights, "Solution Total MFDs", fractiles));
					chars.addAll(getFractileChars(Color.RED, fractiles.length));
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

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				RegionalMFDPlot o = (RegionalMFDPlot)otherCalc;
				weights.addAll(o.weights);
				for (int r=0; r<regions.size(); r++) {
					solMFDs.get(r).addAll(o.solMFDs.get(r));
					solOffMFDs.get(r).addAll(o.solOffMFDs.get(r));
					solTotalMFDs.get(r).addAll(o.solTotalMFDs.get(r));
				}
			}
		}

		protected List<Region> getRegions() {
			return regions;
		}

		protected List<PlotSpec> getSpecs() {
			return specs;
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
		
		if (!dir.exists())
			dir.mkdir();
		
		for (FaultModels fm : plotsMap.keySet()) {
			Map<String, PlotSpec[]> specs = plotsMap.get(fm);
			
			String prefix = null;
			if (multiple)
				prefix = fm.getShortName();
			
			CommandLineInversionRunner.writePaleoFaultPlots(specs, prefix, dir);
		}
	}
	
	public static class PaleoFaultPlot extends CompoundFSSPlots {
		
		private transient PaleoProbabilityModel paleoProbModel;
		private transient BranchWeightProvider weightProvider;
		
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
					synchronized (this) {
						paleoRateConstraints = paleoConstraintMaps.get(fm);
						if (paleoRateConstraints == null) {
							System.out.println("I'm in the synchronized block! "+fm);
							// do a bunch of FM specific stuff
							paleoRateConstraints = CommandLineInversionRunner.getPaleoConstraints(fm, sol);
							slipConstraintMaps.put(fm, AveSlipConstraint.load(sol.getFaultSectionDataList()));
							allParentsMaps.put(fm, PaleoFitPlotter.getAllParentsMap(sol.getFaultSectionDataList()));
							namedFaultsMaps.put(fm, fm.getNamedFaultsMapAlt());
							Map<Integer, Double> traceLengthCache = Maps.newConcurrentMap();
							traceLengthCaches.put(fm, traceLengthCache);
							fsdsMap.put(fm, sol.getFaultSectionDataList());
							paleoConstraintMaps.put(fm, paleoRateConstraints);
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
				
				synchronized (this) {
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
					for (int i=0; i<slipsForConstraints.size(); i++)
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
					Preconditions.checkState(slipArray.length == weights.length,
							slipArray.length+" != "+weights.length);
					
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

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				PaleoFaultPlot o = (PaleoFaultPlot )otherCalc;
				for (FaultModels fm : o.allParentsMaps.keySet()) {
					if (!allParentsMaps.containsKey(fm)) {
						// add the fault model specific values
						namedFaultsMaps.put(fm, o.namedFaultsMaps.get(fm));
						paleoConstraintMaps.put(fm, o.paleoConstraintMaps.get(fm));
						slipConstraintMaps.put(fm, o.slipConstraintMaps.get(fm));
						allParentsMaps.put(fm, o.allParentsMaps.get(fm));
						traceLengthCaches.put(fm, o.traceLengthCaches.get(fm));
						fsdsMap.put(fm, o.fsdsMap.get(fm));
					}
					// now add data
					List<DataForPaleoFaultPlots> datasList = datasMap.get(fm);
					if (datasList == null) {
						datasList = Lists.newArrayList();
						datasMap.put(fm, datasList);
					}
					datasList.addAll(o.datasMap.get(fm));

					List<List<Double>> slipRatesList = slipRatesMap.get(fm);
					if (slipRatesList == null) {
						List<AveSlipConstraint> slipConstraints = slipConstraintMaps.get(fm);
						slipRatesList = Lists.newArrayList();
						for (int i=0; i<slipConstraints.size(); i++)
							slipRatesList.add(new ArrayList<Double>());
						slipRatesMap.put(fm, slipRatesList);
					}
					for (int i=0; i<slipRatesList.size(); i++)
						slipRatesList.get(i).addAll(o.slipRatesMap.get(fm).get(i));

					List<Double> weightsList = weightsMap.get(fm);
					if (weightsList == null) {
						weightsList = Lists.newArrayList();
						weightsMap.put(fm, weightsList);
					}
					weightsList.addAll(o.weightsMap.get(fm));
				}
			}
		}

		protected Map<FaultModels, Map<String, PlotSpec[]>> getPlotsMap() {
			return plotsMap;
		}
		
	}
	
	public static void writePaleoCorrelationPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir) throws IOException {
		PaleoSiteCorrelationPlot plot = new PaleoSiteCorrelationPlot(weightProvider);
		plot.buildPlot(fetch);
		
		writePaleoCorrelationPlots(plot.plotsMap, dir);
	}
	
	public static void writePaleoCorrelationPlots(
			Map<String, PlotSpec> plotsMap,
			File dir) throws IOException {
		System.out.println("Making paleo corr plots for "+plotsMap.keySet().size()+" Faults");
		
		if (!dir.exists())
			dir.mkdir();
		
		CommandLineInversionRunner.writePaleoCorrelationPlots(dir, plotsMap);
	}
	
	public static class PaleoSiteCorrelationPlot extends CompoundFSSPlots {
		
		private transient PaleoProbabilityModel paleoProbModel;
		private transient BranchWeightProvider weightProvider;
		
		private Map<FaultModels, Map<String, List<PaleoSiteCorrelationData>>> corrsListsMap =
				Maps.newHashMap();
		
		// <fault model, data list of: <fault name, corr values>>
		private List<Map<String, double[]>> data = Lists.newArrayList();
		private List<Double> weights = Lists.newArrayList();
		
		private Map<String, PlotSpec> plotsMap = Maps.newHashMap();
		
		public PaleoSiteCorrelationPlot(BranchWeightProvider weightProvider) {
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
				Map<String, List<PaleoSiteCorrelationData>> corrs =
						corrsListsMap.get(fm);
				if (corrs == null) {
					synchronized (fm) {
						corrs = corrsListsMap.get(fm);
						if (corrs == null) {
							System.out.println("I'm in the synchronized block! "+fm);
							corrs = Maps.newHashMap();
							
							Map<String, Table<String, String, PaleoSiteCorrelationData>> table =
									PaleoSiteCorrelationData.loadPaleoCorrelationData(sol);
							
							for (String faultName : table.keySet()) {
								List<PaleoSiteCorrelationData> corrsToPlot =
										PaleoSiteCorrelationData.getCorrelataionsToPlot(
												table.get(faultName));
								corrs.put(faultName, corrsToPlot);
							}
							corrsListsMap.put(fm, corrs);
						}
					}
				}
				
				double weight = weightProvider.getWeight(branch);
				
				Map<String, double[]> myData = Maps.newHashMap();
				
				System.out.println("Building...");
				for (String faultName : corrs.keySet()) {
					List<PaleoSiteCorrelationData> corrsToPlot = corrs.get(faultName);
					
					double[] vals = new double[corrsToPlot.size()];
					for (int i=0; i<vals.length; i++) {
						PaleoSiteCorrelationData corr = corrsToPlot.get(i);
						vals[i] = PaleoSiteCorrelationData.getRateCorrelated(
								paleoProbModel, sol, corr.getSite1SubSect(), corr.getSite2SubSect());
					}
					
					myData.put(faultName, vals);
				}
				
				
				System.out.println("Archiving results...");
				
				synchronized (this) {
					data.add(myData);
					weights.add(weight);
				}
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				PaleoSiteCorrelationPlot o = (PaleoSiteCorrelationPlot)otherCalc;
				
				data.addAll(o.data);
				weights.addAll(o.weights);
				
				for (FaultModels fm : o.corrsListsMap.keySet()) {
					if (!corrsListsMap.containsKey(fm))
						corrsListsMap.put(fm, o.corrsListsMap.get(fm));
				}
			}
		}

		@Override
		protected void finalizePlot() {
			Map<String, List<PaleoSiteCorrelationData>> allCorrsMap = Maps.newHashMap();
			for (FaultModels fm : corrsListsMap.keySet()) {
				Map<String, List<PaleoSiteCorrelationData>> corrsForFM = corrsListsMap.get(fm);
				for (String faultName : corrsForFM.keySet()) {
					if (!allCorrsMap.containsKey(faultName))
						allCorrsMap.put(faultName, corrsForFM.get(faultName));
				}
			}
			
			for (String faultName : allCorrsMap.keySet()) {
				List<double[]> solValsForFault = Lists.newArrayList();
				List<Double> weightsForFault = Lists.newArrayList();
				
				for (int s=0; s<data.size(); s++) {
					double[] solData = data.get(s).get(faultName);
					if (solData != null) {
						solValsForFault.add(solData);
						weightsForFault.add(weights.get(s));
					}
				}
				
				List<PaleoSiteCorrelationData> corrs = allCorrsMap.get(faultName);
				
				List<double[]> solValues = Lists.newArrayList();
				double[] weights = Doubles.toArray(weightsForFault);
				
				for (int i=0; i<corrs.size(); i++) {
					double[] vals = new double[solValsForFault.size()];
					for (int s=0; s<solValsForFault.size(); s++)
						vals[s] = solValsForFault.get(s)[i];
					double min = StatUtils.min(vals);
					double max = StatUtils.max(vals);
					double mean = FaultSystemSolutionFetcher.calcScaledAverage(vals, weights);
					
					double[] ret = { min, max, mean };
					System.out.println("Vals for "+faultName+" CORR "+i+": "+min+","+max+","+mean
							+" ("+vals.length+" sols)");
					solValues.add(ret);
				}
				
				PlotSpec spec = PaleoSiteCorrelationData.getCorrelationPlotSpec(
						faultName, FaultModels.FM3_1, corrs, solValues, paleoProbModel);
				
				plotsMap.put(faultName, spec);
			}
		}

		@Override
		protected boolean usesInversionFSS() {
			return false;
		}

		public Map<String, PlotSpec> getPlotsMap() {
			return plotsMap;
		}
	}
	
	public static void writeParentSectionMFDPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir) throws IOException {
		ParentSectMFDsPlot plot = new ParentSectMFDsPlot(weightProvider);
		plot.buildPlot(fetch);
		
		writeParentSectionMFDPlots(plot, dir);
	}
	
	public static void writeParentSectionMFDPlots(
			ParentSectMFDsPlot plot,
			File dir) throws IOException {
		System.out.println("Making parent sect MFD plots for "
			+plot.plotNuclIncrMFDs.keySet().size()+" Faults");
		
		if (!dir.exists())
			dir.mkdir();
		
		CSVFile<String> nucleationCSV = new CSVFile<String>(true);
		CSVFile<String> participationCSV = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList();
		List<Double> xVals = Lists.newArrayList();
		for (double x=ParentSectMFDsPlot.minX; x<=ParentSectMFDsPlot.maxX;
				x+=ParentSectMFDsPlot.delta)
			xVals.add(x);
		header.add("Fault ID");
		header.add("Fault Name");
		for (double x : xVals) {
			header.add("M"+(float)x);
			header.add("(UCERF2)");
		}
		nucleationCSV.addLine(header);
		participationCSV.addLine(header);
		
		for (Integer parentID : plot.plotNuclIncrMFDs.keySet()) {
			ArrayList<IncrementalMagFreqDist> ucerf2NuclMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, false, false);
			ArrayList<IncrementalMagFreqDist> ucerf2PArtMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, true, false);
			
			String name = plot.namesMap.get(parentID);
			
			List<IncrementalMagFreqDist> nuclMFDs = plot.plotNuclIncrMFDs.get(parentID);
			List<IncrementalMagFreqDist> partMFDs = plot.plotPartIncrMFDs.get(parentID);
			
			EvenlyDiscretizedFunc nuclCmlMFD = nuclMFDs.get(nuclMFDs.size()-1).getCumRateDist();
			EvenlyDiscretizedFunc partCmlMFD = partMFDs.get(nuclMFDs.size()-1).getCumRateDist();
			
			EvenlyDiscretizedFunc ucerf2NuclCmlMFD = null;
			EvenlyDiscretizedFunc ucerf2PartCmlMFD = null;
			if (ucerf2NuclMFDs != null) {
				ucerf2NuclCmlMFD = ucerf2NuclMFDs.get(0).getCumRateDist();
				ucerf2PartCmlMFD= ucerf2PArtMFDs.get(0).getCumRateDist();
			}
			
			List<String> partLine = Lists.newArrayList(parentID+"", name);
			List<String> nuclLine = Lists.newArrayList(parentID+"", name);
			
			for (int i=0; i<xVals.size(); i++) {
				nuclLine.add(nuclCmlMFD.getY(i)+"");
				partLine.add(partCmlMFD.getY(i)+"");
				double x = xVals.get(i);
				if (ucerf2NuclCmlMFD != null &&
						ucerf2NuclCmlMFD.getMinX() <= x && ucerf2NuclCmlMFD.getMaxX() >= x) {
					nuclLine.add(ucerf2NuclCmlMFD.getClosestY(x)+"");
					partLine.add(ucerf2PartCmlMFD.getClosestY(x)+"");
				} else {
					nuclLine.add("");
					partLine.add("");
				}
			}
			
			nucleationCSV.addLine(nuclLine);
			participationCSV.addLine(partLine);
			
			writeParentSectionMFDPlot(dir, nuclMFDs,
					ucerf2NuclMFDs, parentID, name, true);
			writeParentSectionMFDPlot(dir, partMFDs,
					ucerf2PArtMFDs, parentID, name, false);
		}
		
		nucleationCSV.writeToFile(new File(dir, "cumulative_nucleation_mfd_comparisons.csv"));
		participationCSV.writeToFile(new File(dir, "cumulative_participation_mfd_comparisons.csv"));
	}
	
	private static void writeParentSectionMFDPlot(
			File dir, List<IncrementalMagFreqDist> mfds, List<IncrementalMagFreqDist> ucerf2MFDs,
			int id, String name, boolean nucleation) throws IOException {
		CommandLineInversionRunner.writeParentSectMFDPlot(dir, mfds, false, ucerf2MFDs, id,
				name, nucleation);
	}
	
	
	public static class ParentSectMFDsPlot extends CompoundFSSPlots {
		
		private transient BranchWeightProvider weightProvider;
		
		// none (except min/mean/max which are always included)
		private double[] fractiles;
		
		private ConcurrentMap<FaultModels, HashSet<Integer>>
				parentMapsCache = Maps.newConcurrentMap();
				
		// these are organized as (region, solution)
		private Map<Integer, XY_DataSetList> nuclIncrMFDs = Maps.newHashMap();
		private Map<Integer, XY_DataSetList> partIncrMFDs = Maps.newHashMap();
		
		private Map<Integer, List<Double>> weightsMap = Maps.newHashMap();
		private ConcurrentMap<Integer, String> namesMap = Maps.newConcurrentMap();
				
		private static final double minX = 5.05d;
		private static final double maxX = 9.05d;
		private static final double delta = 0.1d;
		private static final int num = (int)((maxX - minX) / delta) + 1;
		
		private Map<Integer, List<IncrementalMagFreqDist>> plotNuclIncrMFDs = Maps.newHashMap();
		private Map<Integer, List<IncrementalMagFreqDist>> plotPartIncrMFDs = Maps.newHashMap();
		
		public ParentSectMFDsPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, new double[0]);
		}
		
		public ParentSectMFDsPlot(BranchWeightProvider weightProvider, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.fractiles = fractiles;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			FaultModels fm = sol.getFaultModel();
			
			HashSet<Integer> parentIDs = parentMapsCache.get(fm);
			if (parentIDs == null) {
				parentIDs = new HashSet<Integer>();
				for (int sectIndex=0; sectIndex<sol.getNumSections(); sectIndex++) {
					FaultSectionPrefData sect = sol.getFaultSectionData(sectIndex);
					Integer parentID = sect.getParentSectionId();
					if (!parentIDs.contains(parentID)) {
						parentIDs.add(parentID);
						namesMap.putIfAbsent(parentID, sect.getParentSectionName());
					}
				}
				parentMapsCache.putIfAbsent(fm, parentIDs);
			}
			
			double weight = weightProvider.getWeight(branch);
			
			for (Integer parentID : parentIDs) {
				SummedMagFreqDist nuclMFD =
						sol.calcNucleationMFD_forParentSect(parentID, minX, maxX, num);
				IncrementalMagFreqDist partMFD =
						sol.calcParticipationMFD_forParentSect(parentID, minX, maxX, num);
				
				synchronized (this) {
					if (!nuclIncrMFDs.containsKey(parentID)) {
						nuclIncrMFDs.put(parentID, new XY_DataSetList());
						partIncrMFDs.put(parentID, new XY_DataSetList());
						weightsMap.put(parentID, new ArrayList<Double>());
					}
					nuclIncrMFDs.get(parentID).add(nuclMFD);
					partIncrMFDs.get(parentID).add(partMFD);
					weightsMap.get(parentID).add(weight);
				}
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ParentSectMFDsPlot o = (ParentSectMFDsPlot)otherCalc;
				
				for (Integer parentID : o.nuclIncrMFDs.keySet()) {
					if (!nuclIncrMFDs.containsKey(parentID)) {
						nuclIncrMFDs.put(parentID, new XY_DataSetList());
						partIncrMFDs.put(parentID, new XY_DataSetList());
						weightsMap.put(parentID, new ArrayList<Double>());
					}
					nuclIncrMFDs.get(parentID).addAll(o.nuclIncrMFDs.get(parentID));
					partIncrMFDs.get(parentID).addAll(o.partIncrMFDs.get(parentID));
					weightsMap.get(parentID).addAll(o.weightsMap.get(parentID));
					if (!namesMap.containsKey(parentID))
						namesMap.put(parentID, o.namesMap.get(parentID));
				}
			}
		}

		@Override
		protected void finalizePlot() {
			for (Integer parentID : nuclIncrMFDs.keySet()) {
				plotNuclIncrMFDs.put(parentID, asIncr(getFractiles(
						nuclIncrMFDs.get(parentID), weightsMap.get(parentID),
						"Incremental Nucleation MFD", fractiles)));
				plotPartIncrMFDs.put(parentID, asIncr(getFractiles(
						partIncrMFDs.get(parentID), weightsMap.get(parentID),
						"Incremental Participation MFD", fractiles)));
			}
		}
		
		private static List<IncrementalMagFreqDist> asIncr(List<DiscretizedFunc> funcs) {
			List<IncrementalMagFreqDist> incrMFDs = Lists.newArrayList();
			for (DiscretizedFunc func : funcs)
				incrMFDs.add((IncrementalMagFreqDist)func);
			return incrMFDs;
		}

		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
		
	}
	
	public static void writeJumpPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir, String prefix) throws IOException {
		RupJumpPlot plot = new RupJumpPlot(weightProvider);
		plot.buildPlot(fetch);
		
		writeJumpPlots(plot, dir, prefix);
	}
	
	public static void writeJumpPlots(
			RupJumpPlot plot,
			File dir, String prefix) throws IOException {
		System.out.println("Making rup jump plots for "
			+plot.weights.size()+" sols");
		
		for (int i=0; i<plot.minMags.length; i++) {
			CommandLineInversionRunner.writeJumpPlot(dir, prefix, plot.plotSolFuncs.get(i),
					plot.plotRupSetFuncs.get(i), RupJumpPlot.jumpDist, plot.minMags[i],
					plot.paleoProbs[i]);
		}
	}
	
	public static class RupJumpPlot extends CompoundFSSPlots {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private double[] minMags = { 7d, 0d };
		private boolean[] paleoProbs = { false, true };
		
		private double[] fractiles;
		
		private static final double jumpDist = 1d;
		
		private transient BranchWeightProvider weightProvider;
		private transient PaleoProbabilityModel paleoProbModel;
		
		private transient ConcurrentMap<FaultModels, Map<IDPairing, Double>> distancesCache = Maps.newConcurrentMap();
		
		private List<XY_DataSetList> solFuncs = Lists.newArrayList();
		private List<XY_DataSetList> rupSetFuncs = Lists.newArrayList();
		private List<Double> weights = Lists.newArrayList();
		
		private List<DiscretizedFunc[]> plotSolFuncs = Lists.newArrayList();
		private List<DiscretizedFunc[]> plotRupSetFuncs = Lists.newArrayList();
		
		public RupJumpPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, new double[0]);
		}
		
		public RupJumpPlot(BranchWeightProvider weightProvider, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.fractiles = fractiles;
			
			try {
				paleoProbModel = UCERF3_PaleoProbabilityModel.load();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			
			for (int i=0; i<minMags.length; i++) {
				solFuncs.add(new XY_DataSetList());
				rupSetFuncs.add(new XY_DataSetList());
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			FaultModels fm = sol.getFaultModel();
			
			Map<IDPairing, Double> distances = distancesCache.get(fm);
			if (distances == null) {
				synchronized (this) {
					distances = distancesCache.get(fm);
					if (distances == null) {
						distances = new DeformationModelFetcher(fm, sol.getDeformationModel(),
								UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1)
								.getSubSectionDistanceMap(5d);
						distancesCache.putIfAbsent(fm, distances);
					}
				}
			}
			
			double weight = weightProvider.getWeight(branch);
			
			List<EvenlyDiscretizedFunc[]> myFuncs = Lists.newArrayList();
			for (int i=0; i<minMags.length; i++) {
				EvenlyDiscretizedFunc[] funcs = CommandLineInversionRunner.getJumpFuncs(
						sol, distances, jumpDist, minMags[i], paleoProbModel);
				myFuncs.add(funcs);
			}
			synchronized (this) {
				for (int i=0; i<myFuncs.size(); i++) {
					EvenlyDiscretizedFunc[] funcs = myFuncs.get(i);
					solFuncs.get(i).add(funcs[0]);
					rupSetFuncs.get(i).add(funcs[1]);
				}
				weights.add(weight);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				RupJumpPlot o = (RupJumpPlot)otherCalc;
				
				for (int i=0; i<minMags.length; i++) {
					solFuncs.get(i).addAll(o.solFuncs.get(i));
					rupSetFuncs.get(i).addAll(o.rupSetFuncs.get(i));
				}
				weights.addAll(o.weights);
			}
		}
		
		private static DiscretizedFunc[] toArray(List<DiscretizedFunc> funcs) {
			DiscretizedFunc[] array = new DiscretizedFunc[funcs.size()];
			for (int i=0; i<funcs.size(); i++)
				array[i] = funcs.get(i);
			return array;
		}

		@Override
		protected void finalizePlot() {
			for (int i=0; i<solFuncs.size(); i++) {
				List<DiscretizedFunc> solFractiles =
						getFractiles(solFuncs.get(i), weights, "Solution Jumps", fractiles);
				List<DiscretizedFunc> rupSetFractiles =
						getFractiles(rupSetFuncs.get(i), weights, "Rup Set Jumps", fractiles);
				plotSolFuncs.add(toArray(solFractiles));
				plotRupSetFuncs.add(toArray(rupSetFractiles));
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
	
	public static class SlipMisfitPlot extends FaultBasedMapPlot {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final String PLOT_DATA_FILE_NAME = "slip_misfit_plots.xml";
		
		private transient BranchWeightProvider weightProvider;
		
		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps.newConcurrentMap();
		private Map<FaultModels, List<double[]>> valuesMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();
		
		private List<FaultBasedMapPlotData> plots;
		
		private static int cnt;
		
		public SlipMisfitPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
			
			cnt = 0;
		}
	
		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			int myCnt = cnt++;
			System.out.println("Processing solution "+myCnt);
			
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;
			
			double[] solSlips = sol.calcSlipRateForAllSects();
			double[] targetSlips = sol.getSlipRateForAllSections();
			double[] values = new double[solSlips.length];
			for (int i=0; i<solSlips.length; i++) {
				if (solSlips[i] == 0 && targetSlips[i] == 0)
					values[i] = 1;
				else
					values[i] = solSlips[i] / targetSlips[i];
			}
			
			FaultModels fm = sol.getFaultModel();
			
			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(
						sol.getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}
			
			System.out.println("Archiving solution "+myCnt);
			
			synchronized (this) {
				List<double[]> valuesList = valuesMap.get(fm);
				if (valuesList == null) {
					valuesList = Lists.newArrayList();
					valuesMap.put(fm, valuesList);
				}
				valuesList.add(values);
				
				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
				}
				weightsList.add(weight);
			}
			
			System.out.println("Done with solution "+myCnt);
		}
	
		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				SlipMisfitPlot o = (SlipMisfitPlot)otherCalc;
				for (FaultModels fm : o.valuesMap.keySet()) {
					if (!faultsMap.containsKey(fm)) {
						faultsMap.put(fm, o.faultsMap.get(fm));
						valuesMap.put(fm, new ArrayList<double[]>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					valuesMap.get(fm).addAll(o.valuesMap.get(fm));
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}
	
		@Override
		protected void finalizePlot() {
			plots = Lists.newArrayList();
			
			boolean multipleFMs = faultsMap.keySet().size() > 1;
			
			CPT linearCPT = FaultBasedMapGen.getLinearRatioCPT();
			CPT logCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-1, 1);
			
			Region region = new CaliforniaRegions.RELM_TESTING();
			
			boolean skipNans = false;
			
			for (FaultModels fm : faultsMap.keySet()) {
				List<LocationList> faults = faultsMap.get(fm);
				List<double[]> valuesList = valuesMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);
				
				double[] values = getWeightedAvg(faults.size(), valuesList, weightsList);
				
				String label = "Mean(Solution Slip / Target Slip)";
				String prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName()+"_";
					label = fm.getShortName()+" "+label;
				}
				
				plots.add(new FaultBasedMapPlotData(linearCPT, faults, values, region,
						skipNans, label, prefix+"slip_misfit"));
				
				label = "Log10("+label+")";
				double[] log10Values = FaultBasedMapGen.log10(values);
				plots.add(new FaultBasedMapPlotData(logCPT, faults, log10Values, region,
						skipNans, label, prefix+"slip_misfit_log"));
			}
		}
	
		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
	
		@Override
		protected List<FaultBasedMapPlotData> getPlotData() {
			return plots;
		}
	
		@Override
		protected String getPlotDataFileName() {
			return PLOT_DATA_FILE_NAME;
		}
		
	}
	
	public static class ParticipationMapPlot extends FaultBasedMapPlot {
		
		private List<double[]> ranges;
		
		public static List<double[]> getDefaultRanges() {
			List<double[]> ranges = Lists.newArrayList();
			
			ranges.add(toArray(5d, 9d));
			ranges.add(toArray(6.7d, 9d));
			ranges.add(toArray(7.7d, 9d));
			ranges.add(toArray(8d, 9d));
			
			return ranges;
		}
		private static double[] toArray(double... vals) {
			return vals;
		}
		
		private transient BranchWeightProvider weightProvider;
		
		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps.newConcurrentMap();
		private Map<FaultModels, List<List<double[]>>> valuesMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();
		
		private List<FaultBasedMapPlotData> plots;
		
		public ParticipationMapPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultRanges());
		}
		
		public ParticipationMapPlot(BranchWeightProvider weightProvider, List<double[]> ranges) {
			this.weightProvider = weightProvider;
			this.ranges = ranges;
		}
	
		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol) {
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;
			
			List<double[]> myValues = Lists.newArrayList();
			for (double[] range : ranges) {
				myValues.add(sol.calcParticRateForAllSects(range[0], range[1]));
			}
			
			FaultModels fm = sol.getFaultModel();
			
			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(
						sol.getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}
			
			synchronized (this) {
				List<List<double[]>> valuesList = valuesMap.get(fm);
				if (valuesList == null) {
					valuesList = Lists.newArrayList();
					valuesMap.put(fm, valuesList);
				}
				valuesList.add(myValues);
				
				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
				}
				weightsList.add(weight);
			}
		}
	
		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ParticipationMapPlot o = (ParticipationMapPlot)otherCalc;
				for (FaultModels fm : o.valuesMap.keySet()) {
					if (!faultsMap.containsKey(fm)) {
						faultsMap.put(fm, o.faultsMap.get(fm));
						valuesMap.put(fm, new ArrayList<List<double[]>>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					valuesMap.get(fm).addAll(o.valuesMap.get(fm));
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}
	
		@Override
		protected void finalizePlot() {
			plots = Lists.newArrayList();
			
			boolean multipleFMs = faultsMap.keySet().size() > 1;
			
			CPT participationCPT = FaultBasedMapGen.getParticipationCPT();
			CPT logCPT = FaultBasedMapGen.getLogRatioCPT();
			
			Region region = new CaliforniaRegions.RELM_TESTING();
			
			boolean skipNans = true;
			boolean omitInfinites = true;
			
			for (FaultModels fm : faultsMap.keySet()) {
				List<LocationList> faults = faultsMap.get(fm);
				List<List<double[]>> valuesList = valuesMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);
				
				FaultSystemSolution ucerf2 = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(fm);
				
				for (int i=0; i<ranges.size(); i++) {
					double minMag = ranges.get(i)[0];
					double maxMag = ranges.get(i)[1];
					
					List<double[]> rangeValsList = Lists.newArrayList();
					for (int s=0; s<valuesList.size(); s++)
						rangeValsList.add(valuesList.get(s).get(i));
					
//					double[] values = getWeightedAvg(faults.size(), rangeValsList, weightsList);
					double[] values = new double[faults.size()];
					// TODO
//					double[] stdDevs = getStdDevs(faults.size(), valuesList.get(i));
					double[] stdDevs = new double[values.length];
					for (int s=0; s<values.length; s++) {
						ArbDiscrEmpiricalDistFunc func = new ArbDiscrEmpiricalDistFunc();
						for (int j=0; j<weightsList.size(); j++)
							// val, weight
							func.set(rangeValsList.get(j)[s], weightsList.get(j));
						stdDevs[s] = func.getStdDev();
						values[s] = func.getMean();
					}
					double[] logValues = FaultBasedMapGen.log10(values);
					
					String name = "partic_rates_"+(float)minMag;
					String title = "Log10(Participation Rates "+(float)+minMag;
					if (maxMag < 9) {
						name += "_"+(float)maxMag;
						title += "=>"+(float)maxMag;
					} else {
						name += "+";
						title += "+";
					}
					title += ")";
					
					if (multipleFMs) {
						name = fm.getShortName()+"_"+name;
						title = fm.getShortName()+" "+title;
					}
					
					plots.add(new FaultBasedMapPlotData(participationCPT, faults, logValues, region,
							skipNans, title, name));
					
					double[] ucerf2Vals = ucerf2.calcParticRateForAllSects(minMag, maxMag);
					
					double[] ratios = new double[ucerf2Vals.length];
					for (int j=0; j<values.length; j++) {
						ratios[j] = values[j] / ucerf2Vals[j];
						if (omitInfinites && Double.isInfinite(ratios[j]))
							ratios[j] = Double.NaN;
					}
					ratios = FaultBasedMapGen.log10(ratios);
					
					name = "partic_ratio_"+(float)minMag;
					title = "Log10(Participation Ratios "+(float)+minMag;
					if (maxMag < 9) {
						name += "_"+(float)maxMag;
						title += "=>"+(float)maxMag;
					} else {
						name += "+";
						title += "+";
					}
					title += ")";
					
					if (multipleFMs) {
						name = fm.getShortName()+"_"+name;
						title = fm.getShortName()+" "+title;
					}
					
					plots.add(new FaultBasedMapPlotData(logCPT, faults, ratios, region,
							skipNans, title, name));
					
					double[] stdNormVals = new double[values.length];
					
					for (int s=0; s<stdNormVals.length; s++) {
						if (ucerf2Vals[s] == 0)
							stdNormVals[s] = Double.NaN;
						else
							stdNormVals[s] = (values[s] - ucerf2Vals[s]) / stdDevs[s];
					}
					
					name = "partic_diffs_norm_std_dev_"+(float)minMag;
					title = "(U3mean - U2mean)/U3std "+(float)+minMag;
					if (maxMag < 9) {
						name += "_"+(float)maxMag;
						title += "=>"+(float)maxMag;
					} else {
						name += "+";
						title += "+";
					}
//					title += ")";
					
					if (multipleFMs) {
						name = fm.getShortName()+"_"+name;
						title = fm.getShortName()+" "+title;
					}
					
					plots.add(new FaultBasedMapPlotData(logCPT, faults, stdNormVals, region,
							skipNans, title, name));
				}
			}
		}
	
		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
	
		@Override
		protected List<FaultBasedMapPlotData> getPlotData() {
			return plots;
		}
	
		@Override
		protected String getPlotDataFileName() {
			return "participation_plots.xml";
		}
		
	}

	private static class FaultBasedMapPlotData implements XMLSaveable, Serializable {
		
		private static final String XML_METADATA_NAME = "FaultBasedMap";
		
		private CPT cpt;
		private List<LocationList> faults;
		private double[] values;
		private Region region;
		private boolean skipNans;
		private String label;
		private String fileName;
		
		public FaultBasedMapPlotData(CPT cpt, List<LocationList> faults,
			double[] values, Region region, boolean skipNans, String label, String fileName) {
			this.cpt = cpt;
			this.faults = faults;
			this.values = values;
			this.region = region;
			this.skipNans = skipNans;
			this.label = label;
			this.fileName = fileName;
		}
		
		public static FaultBasedMapPlotData fromXMLMetadata(Element xml) {
			CPT cpt = CPT.fromXMLMetadata(xml.element(CPT.XML_METADATA_NAME));
			
			List<LocationList> faults = Lists.newArrayList();
			List<Double> valuesList = Lists.newArrayList();
			Iterator<Element> it = xml.elementIterator("Fault");
			while (it.hasNext()) {
				Element faultElem = it.next();
				faults.add(LocationList.fromXMLMetadata(
						faultElem.element(LocationList.XML_METADATA_NAME)));
				valuesList.add(Double.parseDouble(faultElem.attributeValue("value")));
			}
			double[] values = Doubles.toArray(valuesList);
			
			Region region = Region.fromXMLMetadata(xml.element(Region.XML_METADATA_NAME));
			
			boolean skipNans = Boolean.parseBoolean(xml.attributeValue("skipNans"));
			String label = xml.attributeValue("label");
			String fileName = xml.attributeValue("fileName");
			
			return new FaultBasedMapPlotData(cpt, faults, values, region,
					skipNans, label, fileName);
		}

		@Override
		public Element toXMLMetadata(Element root) {
			Element xml = root.addElement(XML_METADATA_NAME);
			
			cpt.toXMLMetadata(xml);
			
			for (int i=0; i<faults.size(); i++) {
				Element faultElem = xml.addElement("Fault");
				faultElem.addAttribute("value", values[i]+"");
				faults.get(i).toXMLMetadata(faultElem);
			}
			
			region.toXMLMetadata(xml);
			
			xml.addAttribute("skipNans", skipNans+"");
			xml.addAttribute("label", label);
			xml.addAttribute("fileName", fileName);
			
			return root;
		}
	}
	
	private static abstract class FaultBasedMapPlot extends CompoundFSSPlots {
		
		protected abstract List<FaultBasedMapPlotData> getPlotData();
		
		protected abstract String getPlotDataFileName();
		
		protected double[] getWeightedAvg(
				int numFaults, List<double[]> valuesList, List<Double> weightsList) {
			
			double[] weights = Doubles.toArray(weightsList);
			
			double[] values = new double[numFaults];
			for (int i=0; i<numFaults; i++) {
				double[] faultVals = new double[weights.length];
				for (int s=0; s<weights.length; s++)
					faultVals[s] = valuesList.get(s)[i];
				values[i] = FaultSystemSolutionFetcher.calcScaledAverage(faultVals, weights);
			}
			
			return values;
		}
		
		protected double[] getStdDevs(
				int numFaults, List<double[]> valuesList) {
			
			double[] stdDevs = new double[numFaults];
			for (int i=0; i<numFaults; i++) {
				double[] faultVals = new double[valuesList.size()];
				for (int s=0; s<valuesList.size(); s++)
					faultVals[s] = valuesList.get(s)[i];
				stdDevs[i] = Math.sqrt(StatUtils.variance(faultVals));
			}
			
			return stdDevs;
		}
		
		public void writePlotData(File dir) throws IOException {
			Document doc = XMLUtils.createDocumentWithRoot();
			Element root = doc.getRootElement();
			
			for (FaultBasedMapPlotData data : getPlotData())
				data.toXMLMetadata(root);
			
			File dataFile = new File(dir, getPlotDataFileName());
			XMLUtils.writeDocumentToFile(dataFile, doc);
		}
		
		public static List<FaultBasedMapPlotData> loadPlotData(File file) throws MalformedURLException, DocumentException {
			Document doc = XMLUtils.loadDocument(file);
			Element root = doc.getRootElement();
			
			List<FaultBasedMapPlotData> plots = Lists.newArrayList();
			
			Iterator<Element> it = root.elementIterator(FaultBasedMapPlotData.XML_METADATA_NAME);
			while (it.hasNext())
				plots.add(FaultBasedMapPlotData.fromXMLMetadata(it.next()));
			
			return plots;
		}
		
		public void makeMapPlots(File dir, String prefix)
				throws GMT_MapException, RuntimeException, IOException {
			makeMapPlots(dir, prefix, this.getPlotData());
		}
		
		public static void makeMapPlot(File dir, String prefix, FaultBasedMapPlotData plot)
				throws GMT_MapException, RuntimeException, IOException {
			makeMapPlots(dir, prefix, Lists.newArrayList(plot));
		}
		
		public static void makeMapPlots(File dir, String prefix, List<FaultBasedMapPlotData> plots)
				throws GMT_MapException, RuntimeException, IOException {
			for (FaultBasedMapPlotData plot : plots) {
				String plotPrefix;
				if (prefix != null && !prefix.isEmpty())
					plotPrefix = prefix+"_";
				else
					plotPrefix = "";
				plotPrefix += plot.fileName;
				System.out.println("Making fault plot with title: "+plot.label);
				FaultBasedMapGen.makeFaultPlot(plot.cpt, plot.faults, plot.values, plot.region,
						dir, plotPrefix, false, plot.skipNans, plot.label);
				System.out.println("DONE.");
			}
		}
		
	}
	
	public static List<PlotCurveCharacterstics> getFractileChars(Color color, int numFractiles) {
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		PlotCurveCharacterstics thinChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, color);
		PlotCurveCharacterstics thickChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color);
		
		for (int i=0; i<numFractiles; i++)
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
	 * This is used when doing distributed calculations. This method will be called on the
	 * root method to combine all of the other plots with this one.
	 * @param otherCalcs
	 */
	protected abstract void combineDistributedCalcs(Collection<CompoundFSSPlots> otherCalcs);
	
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
		ArrayList<CompoundFSSPlots> plots = new ArrayList<CompoundFSSPlots>();
		plots.add(this);
		batchPlot(plots, fetcher);
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
		threads *= 3;
		threads /= 4;
		if (threads < 1)
			threads = 1;
		batchPlot(plots, fetcher, threads);
	}
	
	protected static class PlotSolComputeTask implements Task {
		
		private Collection<CompoundFSSPlots> plots;
		private FaultSystemSolutionFetcher fetcher;
		private LogicTreeBranch branch;
		private boolean invFSS;
		private boolean mpj;
		
		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher,
				LogicTreeBranch branch,
				boolean invFSS) {
			this(plots, fetcher, branch, invFSS, false);
		}
		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher,
				LogicTreeBranch branch,
				boolean invFSS,
				boolean mpj) {
			this.plots = plots;
			this.fetcher = fetcher;
			this.branch = branch;
			this.invFSS = invFSS;
			this.mpj = mpj;
		}

		@Override
		public void compute() {
			try {
				FaultSystemSolution sol = fetcher.getSolution(branch);
				
				if (invFSS)
					sol = new InversionFaultSystemSolution(sol);
				
				for (CompoundFSSPlots plot : plots)
					plot.processSolution(branch, sol);
			} catch (Exception e) {
				e.printStackTrace();
				if (mpj)
					MPJTaskCalculator.abortAndExit(1);
				else
					System.exit(1);
			}
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
	
	public static void batchWritePlots(Collection<CompoundFSSPlots> plots, File dir, String prefix)
			throws Exception {
		batchWritePlots(plots, dir, prefix, true);
	}
	
	public static void batchWritePlots(
			Collection<CompoundFSSPlots> plots, File dir, String prefix, boolean makeMapPlots)
			throws Exception {
		for (CompoundFSSPlots plot : plots) {
			if (plot instanceof RegionalMFDPlot) {
				RegionalMFDPlot mfd = (RegionalMFDPlot)plot;
				
				CompoundFSSPlots.writeRegionalMFDPlots(mfd.getSpecs(), mfd.getRegions(), dir, prefix);
			} else if (plot instanceof PaleoFaultPlot) {
				PaleoFaultPlot paleo = (PaleoFaultPlot)plot;
				File paleoPlotsDir = new File(dir, CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME);
				if (!paleoPlotsDir.exists())
					paleoPlotsDir.mkdir();
				CompoundFSSPlots.writePaleoFaultPlots(paleo.getPlotsMap(), paleoPlotsDir);
			} else if (plot instanceof PaleoSiteCorrelationPlot) {
				PaleoSiteCorrelationPlot paleo = (PaleoSiteCorrelationPlot)plot;
				File paleoPlotsDir = new File(dir, CommandLineInversionRunner.PALEO_CORRELATION_DIR_NAME);
				if (!paleoPlotsDir.exists())
					paleoPlotsDir.mkdir();
				CompoundFSSPlots.writePaleoCorrelationPlots(paleo.getPlotsMap(), paleoPlotsDir);
			} else if (plot instanceof ParentSectMFDsPlot) {
				ParentSectMFDsPlot parentPlots = (ParentSectMFDsPlot)plot;
				File parentPlotsDir = new File(dir, CommandLineInversionRunner.PARENT_SECT_MFD_DIR_NAME);
				if (!parentPlotsDir.exists())
					parentPlotsDir.mkdir();
				CompoundFSSPlots.writeParentSectionMFDPlots(parentPlots, parentPlotsDir);
			} else if (plot instanceof RupJumpPlot) {
				RupJumpPlot jumpPlot = (RupJumpPlot)plot;
				CompoundFSSPlots.writeJumpPlots(jumpPlot, dir, prefix);
			} else if (plot instanceof FaultBasedMapPlot) {
				FaultBasedMapPlot faultPlot = (FaultBasedMapPlot)plot;
				faultPlot.writePlotData(dir);
				if (makeMapPlots)
					faultPlot.makeMapPlots(dir, prefix);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public static void main(String[] args) throws ZipException, Exception {
		if (args.length >= 3) {
			File dir = new File(args[0]);
			String prefix = args[1];
			for (int i=2; i<args.length; i++) {
				File plotFile = new File(dir, args[i]);
				FaultBasedMapPlot.makeMapPlots(
						dir, prefix, FaultBasedMapPlot.loadPlotData(plotFile));
			}
			System.exit(0);
		}
		
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
//		File dir = new File("/tmp/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL");
//		File file = new File(dir, "2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip");
		File dir = new File("/tmp");
//		File file = new File(dir, "2012_10_14-fm3-logic-tree-sample-x5_run0_COMPOUND_SOL.zip");
		File file = new File(dir, "zeng_convergence_compound.zip");
//		File file = new File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
		FaultSystemSolutionFetcher fetch = CompoundFaultSystemSolution.fromZipFile(file);
		double wts = 0;
		for (LogicTreeBranch branch : fetch.getBranches())
			wts += weightProvider.getWeight(branch);
		System.out.println("Total weight: "+wts);
//		System.exit(0);
//		fetch = FaultSystemSolutionFetcher.getRandomSample(fetch, 5);
		
		new DeadlockDetectionThread(3000).start();
		
		List<Region> regions = RegionalMFDPlot.getDefaultRegions();
		
//		File dir = new File("/tmp");
//		String prefix = "2012_10_10-fm3-logic-tree-sample-first-247";
		String prefix = dir.getName();
//		for (PlotSpec spec : getRegionalMFDPlotSpecs(fetch, weightProvider, regions)) {
//			GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(spec);
//			gw.setYLog(true);
//		}
//		writeRegionalMFDPlots(fetch, weightProvider, regions, dir, prefix);
//		File paleoDir = new File(dir, prefix+"-paleo-faults");
//		writePaleoFaultPlots(fetch, weightProvider, paleoDir);
//		File paleoCorrDir = new File(dir, prefix+"-paleo-corr");
//		writePaleoCorrelationPlots(fetch, weightProvider, paleoCorrDir);
//		File parentSectMFDsDir = new File(dir, prefix+"-parent-sect-mfds");
//		writeParentSectionMFDPlots(fetch, weightProvider, parentSectMFDsDir);
//		writeJumpPlots(fetch, weightProvider, dir, prefix);
		List<CompoundFSSPlots> plots = Lists.newArrayList();
		plots.add(new RegionalMFDPlot(weightProvider, regions));
		plots.add(new PaleoFaultPlot(weightProvider));
		plots.add(new PaleoSiteCorrelationPlot(weightProvider));
		plots.add(new ParentSectMFDsPlot(weightProvider));
		plots.add(new RupJumpPlot(weightProvider));
		plots.add(new SlipMisfitPlot(weightProvider));
		plots.add(new ParticipationMapPlot(weightProvider));
		
		batchPlot(plots, fetch, 4);
		batchWritePlots(plots, dir, prefix, true);
//		FaultBasedMapPlot.makeMapPlots(dir, prefix,
//				FaultBasedMapPlot.loadPlotData(new File(dir, SlipMisfitPlot.PLOT_DATA_FILE_NAME)));
		
		System.exit(0);
		
	}

}
