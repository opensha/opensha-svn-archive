package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipException;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.calc.FractileCurveCalculator;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.AbstractGeoDataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.CompoundGriddedSurface;
import org.opensha.sha.faultSurface.RupInRegionCache;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.InversionMFDs;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.oldStuff.RupsInFaultSystemInversion;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelFileParser.DeformationSection;
import scratch.UCERF3.utils.DeformationModelFileParser;
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
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;
import scratch.kevin.DeadlockDetectionThread;
import scratch.kevin.ucerf3.inversion.MiniSectRecurrenceGen;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
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
			gp.saveAsTXT(file.getAbsolutePath()+".txt");
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
			regions.add(new CaliforniaRegions.NORTHRIDGE_BOX());
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
				FaultSystemSolution sol, int solIndex) {
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
	
	public static List<PlotSpec> writeERFBasedRegionalMFDPlotSpecs(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			List<Region> regions) {
		ERFBasedRegionalMFDPlot plot = new ERFBasedRegionalMFDPlot(
				weightProvider, regions, ERFBasedRegionalMFDPlot.getDefaultFractiles());
		
		plot.buildPlot(fetch);
		
		return plot.specs;
	}
	
	public static void writeERFBasedRegionalMFDPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			List<Region> regions,
			File dir, String prefix) throws IOException {
		List<PlotSpec> specs = writeERFBasedRegionalMFDPlotSpecs(fetch, weightProvider, regions);
		
		writeERFBasedRegionalMFDPlots(specs, regions, dir, prefix);
	}
	
	public static void writeERFBasedRegionalMFDPlots(
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
			
			String fname = prefix+"_MFD_ERF";
			if (region.getName() != null && !region.getName().isEmpty())
				fname += "_"+region.getName().replaceAll("\\W+", "_");
			else
				fname += "_UNNAMED_REGION_"+(++unnamedRegionCnt);
			
			File file = new File(dir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
			gp.saveAsTXT(file.getAbsolutePath()+".txt");
		}
	}
	
	public static class ERFBasedRegionalMFDPlot extends CompoundFSSPlots {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public static List<Region> getDefaultRegions() {
			List<Region> regions = Lists.newArrayList();
			regions.add(new CaliforniaRegions.RELM_TESTING());
			regions.add(new CaliforniaRegions.LA_BOX());
			regions.add(new CaliforniaRegions.SF_BOX());
			regions.add(new CaliforniaRegions.NORTHRIDGE_BOX());
			return regions;
		}
		
		private transient BranchWeightProvider weightProvider;
		private List<Region> regions;
		private List<Double> weights;
		private double[] ucerf2Weights;
		
		// none (except min/mean/max which are always included)
		private double[] fractiles;
				
		// these are organized as (region, solution)
		private List<XY_DataSetList> solMFDs;
		private List<DiscretizedFunc[]> ucerf2MFDs;
		
		private transient Deque<UCERF2_TimeIndependentEpistemicList> ucerf2_erf_lists =
				new ArrayDeque<UCERF2_TimeIndependentEpistemicList>();
//		private transient UCERF2_TimeIndependentEpistemicList ucerf2_erf_list;
				
		private static final double minX = 5.05d;
		private static final double maxX = 9.05d;
		private static final double delta = 0.1d;
		private static final int num = (int)((maxX - minX) / delta + 1);
		
		private List<PlotSpec> specs;
		
		private int numUCEF2_ERFs;
		
		private transient Map<FaultModels, RupInRegionCache> rupInRegionsCaches = Maps.newHashMap();
		
		private static double[] getDefaultFractiles() {
			double[] ret = { 0.5 };
			return ret;
		}
		
		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultRegions());
		}
		
		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider, List<Region> regions) {
			this(weightProvider, regions, getDefaultFractiles());
		}
		
		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider, List<Region> regions, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.regions = regions;
			this.fractiles = fractiles;
			
			UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = checkOutUCERF2_ERF();
			numUCEF2_ERFs = ucerf2_erf_list.getNumERFs();
			returnUCERF2_ERF(ucerf2_erf_list);
			ucerf2_erf_list = null;
			
			solMFDs = Lists.newArrayList();
			ucerf2MFDs = Lists.newArrayList();
			weights = Lists.newArrayList();
			ucerf2Weights = new double[numUCEF2_ERFs];
			
			for (int i=0; i<regions.size(); i++) {
				solMFDs.add(new XY_DataSetList());
				ucerf2MFDs.add(new DiscretizedFunc[numUCEF2_ERFs]);
			}
		}
		
		private synchronized UCERF2_TimeIndependentEpistemicList checkOutUCERF2_ERF() {
			if (ucerf2_erf_lists.isEmpty()) {
				UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = new UCERF2_TimeIndependentEpistemicList();
				ucerf2_erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
				ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
				ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
				ucerf2_erf_list.getTimeSpan().setDuration(1d);
				ucerf2_erf_list.updateForecast();
				return ucerf2_erf_list;
			}
			return ucerf2_erf_lists.pop();
		}
		
		private synchronized void returnUCERF2_ERF(UCERF2_TimeIndependentEpistemicList erf) {
			ucerf2_erf_lists.push(erf);
		}
		
		private void calcUCERF2MFDs(int erfIndex) {
			UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = checkOutUCERF2_ERF();
			ERF erf = ucerf2_erf_list.getERF(erfIndex);
			for (int regionIndex=0; regionIndex<regions.size(); regionIndex++) {
				System.out.println("Calculating UCERF2 MFDs for branch "+erfIndex+", region "+regionIndex);
				Region region = regions.get(regionIndex);
				SummedMagFreqDist mfdPart = ERF_Calculator.getParticipationMagFreqDistInRegion(
						erf, region, minX, num, delta, true);
				ucerf2MFDs.get(regionIndex)[erfIndex] = mfdPart.getCumRateDistWithOffset();
				ucerf2Weights[erfIndex] = ucerf2_erf_list.getERF_RelativeWeight(erfIndex);
			}
			returnUCERF2_ERF(ucerf2_erf_list);
		}
		
		private void checkCalcAllUCERF2MFDs() {
			for (int erfIndex=0; erfIndex<numUCEF2_ERFs; erfIndex++) {
				if (ucerf2MFDs.get(0)[erfIndex] == null)
					calcUCERF2MFDs(erfIndex);
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
			throw new IllegalStateException("Should not be called, ERF plot!");
		}

		@Override
		protected void processERF(LogicTreeBranch branch,
				UCERF3_FaultSysSol_ERF erf, int solIndex) {
			// do UCERF2 if applicable
			if (solIndex < numUCEF2_ERFs)
				calcUCERF2MFDs(solIndex);
			
			FaultModels fm = branch.getValue(FaultModels.class);
			
			RupInRegionCache rupsCache = rupInRegionsCaches.get(fm);
			if (rupsCache == null) {
				synchronized (this) {
					if (!rupInRegionsCaches.containsKey(fm)) {
						rupInRegionsCaches.put(fm, new RupInRegionCache() {

							private ConcurrentMap<Region, ConcurrentMap<String, Boolean>> map = Maps.newConcurrentMap();

							@Override
							public boolean isRupInRegion(EqkRupture rup, int srcIndex, int rupIndex,
									Region region) {
								RuptureSurface surf = rup.getRuptureSurface();
								if (surf instanceof CompoundGriddedSurface) {
									ConcurrentMap<String, Boolean> regMap = map.get(region);
									if (regMap == null) {
										regMap = Maps.newConcurrentMap();
										map.putIfAbsent(region, regMap);
										// in case another thread put it in first
										regMap = map.get(region);
									}
									String key = srcIndex+"_"+rupIndex;
									Boolean inside = regMap.get(key);
									if (inside == null) {
										inside = false;
										for (Location loc : surf.getEvenlyDiscritizedListOfLocsOnSurface())
											if (region.contains(loc)) {
												inside = true;
												break;
											}
										regMap.putIfAbsent(key, inside);
									}
									return inside;
								}
								for (Location loc : surf.getEvenlyDiscritizedListOfLocsOnSurface())
									if (region.contains(loc))
										return true;
								return false;
							}

						});
					}
				}
				rupsCache = rupInRegionsCaches.get(fm);
			}
			
			List<DiscretizedFunc> mfds = Lists.newArrayList();
			
			for (int r=0; r<regions.size(); r++) {
				Region region = regions.get(r);
				
				Stopwatch watch = new Stopwatch();
				watch.start();
				System.out.println("Calculating branch "+solIndex+" region "+r);
				SummedMagFreqDist ucerf3_Part = ERF_Calculator.getParticipationMagFreqDistInRegion(
						erf, region, minX, num, delta, true, rupsCache);
				watch.stop();
				System.out.println("Took "+(watch.elapsedMillis()/1000d)+" secst for branch "
						+solIndex+" region "+r+" ("+region.getName()+")");
				
				mfds.add(ucerf3_Part.getCumRateDistWithOffset());
			}
			
			synchronized (this) {
				weights.add(weightProvider.getWeight(branch));
				for (int r=0; r<regions.size(); r++) {
					solMFDs.get(r).add(mfds.get(r));
				}
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ERFBasedRegionalMFDPlot o = (ERFBasedRegionalMFDPlot)otherCalc;
				for (int r=0; r<regions.size(); r++)
					solMFDs.get(r).addAll(o.solMFDs.get(r));
				weights.addAll(o.weights);
				
				for (int e=0; e<numUCEF2_ERFs; e++) {
					if (o.ucerf2MFDs.get(0)[e] != null) {
						for (int r=0; r<regions.size(); r++)
							ucerf2MFDs.get(r)[e] = o.ucerf2MFDs.get(r)[e];
						ucerf2Weights[e] = o.ucerf2Weights[e];
					}
				}
			}
		}

		@Override
		protected void finalizePlot() {
			specs = Lists.newArrayList();
			
			checkCalcAllUCERF2MFDs();
			
			MeanUCERF2 erf= new MeanUCERF2();
			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
			erf.getTimeSpan().setDuration(1d);
			erf.updateForecast();
			
			for (int r=0; r<regions.size(); r++) {
				Region region = regions.get(r);
				
				XY_DataSetList ucerf2Funcs = new XY_DataSetList();
				ArrayList<Double> ucerf2Weights = new ArrayList<Double>();
				for (int e=0; e<ucerf2MFDs.get(r).length; e++) {
					DiscretizedFunc mfd = ucerf2MFDs.get(r)[e];
					if (mfd != null) {
						ucerf2Funcs.add(ucerf2MFDs.get(r)[e]);
						ucerf2Weights.add(this.ucerf2Weights[e]);
					}
				}
				
				ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
				ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
				
				funcs.addAll(getFractiles(ucerf2Funcs, ucerf2Weights, "UCERF2 Epistemic List", fractiles));
				chars.addAll(getFractileChars(BROWN, fractiles.length));
				
				SummedMagFreqDist meanU2Part =
						ERF_Calculator.getParticipationMagFreqDistInRegion(erf, region, minX, num, delta, true);
				meanU2Part.setName("MFD for MeanUCERF2");
				meanU2Part.setInfo(" ");
				funcs.add(meanU2Part.getCumRateDistWithOffset());
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
				
				funcs.addAll(getFractiles(solMFDs.get(r), weights, "UCERF3 MFDs", fractiles));
				chars.addAll(getFractileChars(Color.RED, fractiles.length));
				
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
			return true;
		}

		@Override
		protected boolean usesERFs() {
			return true;
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
				FaultSystemSolution sol, int solIndex) {
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
				
				double weight = weightProvider.getWeight(branch);
				
				System.out.println("Building...");
				DataForPaleoFaultPlots data = DataForPaleoFaultPlots.build(
						sol, namedFaultsMap, namedFaultConstraintsMap, allParentsMap,
						paleoProbModel, weight);
				
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
				FaultSystemSolution sol, int solIndex) {
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
						faultName, corrs, solValues, paleoProbModel);
				
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
		CSVFile<String> nucleationMinCSV = new CSVFile<String>(true);
		CSVFile<String> nucleationMaxCSV = new CSVFile<String>(true);
		CSVFile<String> participationCSV = new CSVFile<String>(true);
		CSVFile<String> participationMinCSV = new CSVFile<String>(true);
		CSVFile<String> participationMaxCSV = new CSVFile<String>(true);
		
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
		nucleationMinCSV.addLine(header);
		nucleationMaxCSV.addLine(header);
		participationCSV.addLine(header);
		participationMinCSV.addLine(header);
		participationMaxCSV.addLine(header);
		
		for (Integer parentID : plot.plotNuclIncrMFDs.keySet()) {
			ArrayList<IncrementalMagFreqDist> ucerf2NuclMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, false, false);
			ArrayList<IncrementalMagFreqDist> ucerf2NuclCmlMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, false, true);
			ArrayList<IncrementalMagFreqDist> ucerf2PartMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, true, false);
			ArrayList<IncrementalMagFreqDist> ucerf2PartCmlMFDs =
					UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentID, true, true);
			
			String name = plot.namesMap.get(parentID);
			
			List<IncrementalMagFreqDist> nuclMFDs = plot.plotNuclIncrMFDs.get(parentID);
			List<EvenlyDiscretizedFunc> nuclCmlMFDs = plot.plotNuclCmlMFDs.get(parentID);
			List<IncrementalMagFreqDist> partMFDs = plot.plotPartIncrMFDs.get(parentID);
			List<EvenlyDiscretizedFunc> partCmlMFDs = plot.plotPartCmlMFDs.get(parentID);
			
			for (int i=0; i<nuclMFDs.size(); i++) {
				nuclMFDs.get(i).setInfo(" ");
				nuclCmlMFDs.get(i).setInfo(" ");
				partMFDs.get(i).setInfo(" ");
				partCmlMFDs.get(i).setInfo(" ");
			}
			
			EvenlyDiscretizedFunc nuclCmlMFD = nuclCmlMFDs.get(nuclCmlMFDs.size()-3);
			nuclCmlMFD.setInfo(getCmlMFDInfo(nuclCmlMFD, false));
			EvenlyDiscretizedFunc partCmlMFD = partCmlMFDs.get(partCmlMFDs.size()-3);
			partCmlMFD.setInfo(getCmlMFDInfo(partCmlMFD, false));
			
			EvenlyDiscretizedFunc ucerf2NuclCmlMFD = null;
			EvenlyDiscretizedFunc ucerf2NuclCmlMinMFD = null;
			EvenlyDiscretizedFunc ucerf2NuclCmlMaxMFD = null;
			EvenlyDiscretizedFunc ucerf2PartCmlMFD = null;
			EvenlyDiscretizedFunc ucerf2PartCmlMinMFD = null;
			EvenlyDiscretizedFunc ucerf2PartCmlMaxMFD = null;
			if (ucerf2NuclCmlMFDs != null) {
				ucerf2NuclCmlMFD = ucerf2NuclCmlMFDs.get(0);
				ucerf2NuclCmlMinMFD = ucerf2NuclCmlMFDs.get(1);
				ucerf2NuclCmlMaxMFD = ucerf2NuclCmlMFDs.get(2);
				ucerf2PartCmlMFD = ucerf2PartCmlMFDs.get(0);
				ucerf2PartCmlMinMFD = ucerf2PartCmlMFDs.get(1);
				ucerf2PartCmlMaxMFD = ucerf2PartCmlMFDs.get(2);
			}
			
			nucleationCSV.addLine(getCSVLine(xVals, parentID, name, nuclCmlMFD, ucerf2NuclCmlMFD));
			nucleationMinCSV.addLine(getCSVLine(xVals, parentID, name, nuclCmlMFDs.get(nuclCmlMFDs.size()-2), ucerf2NuclCmlMinMFD));
			nucleationMaxCSV.addLine(getCSVLine(xVals, parentID, name, nuclCmlMFDs.get(nuclCmlMFDs.size()-1), ucerf2NuclCmlMaxMFD));
			participationCSV.addLine(getCSVLine(xVals, parentID, name, partCmlMFD, ucerf2PartCmlMFD));
			participationMinCSV.addLine(getCSVLine(xVals, parentID, name, partCmlMFDs.get(partCmlMFDs.size()-2), ucerf2PartCmlMinMFD));
			participationMaxCSV.addLine(getCSVLine(xVals, parentID, name, partCmlMFDs.get(partCmlMFDs.size()-1), ucerf2PartCmlMaxMFD));
			
			writeParentSectionMFDPlot(dir, nuclMFDs, nuclCmlMFDs,
					ucerf2NuclMFDs, ucerf2NuclCmlMFDs, parentID, name, true);
			writeParentSectionMFDPlot(dir, partMFDs, partCmlMFDs,
					ucerf2PartMFDs, ucerf2PartCmlMFDs, parentID, name, false);
		}
		
		nucleationCSV.writeToFile(new File(dir, "cumulative_nucleation_mfd_comparisons.csv"));
		nucleationMinCSV.writeToFile(new File(dir, "cumulative_nucleation_min_mfd_comparisons.csv"));
		nucleationMaxCSV.writeToFile(new File(dir, "cumulative_nucleation_max_mfd_comparisons.csv"));
		participationCSV.writeToFile(new File(dir, "cumulative_participation_mfd_comparisons.csv"));
		participationMinCSV.writeToFile(new File(dir, "cumulative_participation_min_mfd_comparisons.csv"));
		participationMaxCSV.writeToFile(new File(dir, "cumulative_participation_max_mfd_comparisons.csv"));
	}
	
	private static List<String> getCSVLine(List<Double> xVals, int parentID, String name,
			EvenlyDiscretizedFunc cmlMFD, EvenlyDiscretizedFunc ucerf2CmlMFD) {
		List<String> line = Lists.newArrayList(parentID+"", name);
		
		for (int i=0; i<xVals.size(); i++) {
			line.add(cmlMFD.getY(i)+"");
			double x = xVals.get(i);
			if (ucerf2CmlMFD != null &&
					ucerf2CmlMFD.getMinX() <= x && ucerf2CmlMFD.getMaxX() >= x) {
				line.add(ucerf2CmlMFD.getClosestY(x)+"");
			} else {
				line.add("");
			}
		}
		
		return line;
	}
	
	private static String getCmlMFDInfo(EvenlyDiscretizedFunc cmlMFD, boolean isAlreadyRI) {
		double totRate = cmlMFD.getMaxY();
//		double rate6p7 = cmlMFD.getClosestY(6.7d);
		double rate6p7 = cmlMFD.getInterpolatedY_inLogYDomain(6.7d);
//		for (int i=0; i<cmlMFD.getNum(); i++)
//			System.out.println("CML: "+i+": "+cmlMFD.getX(i)+","+cmlMFD.getY(i));
		String info;
		if (isAlreadyRI) {
			info = "\t\tTotal RI: "+(int)Math.round(totRate)+"\n";
			info += "\t\tRI M>=6.7: "+(int)Math.round(rate6p7);
		} else {
			info = "\t\tTotal Rate: "+(float)totRate+"\n";
			info += "\t\tRate M>=6.7: "+(float)rate6p7+"\n";
			double totRI = 1d/totRate;
			double ri6p7 = 1d/rate6p7;
			info += "\t\tTotal RI: "+(int)Math.round(totRI)+"\n";
			info += "\t\tRI M>=6.7: "+(int)Math.round(ri6p7);
		}
//		System.out.println(info);
		
		return info;
	}
	
	private static void writeParentSectionMFDPlot(
			File dir, List<IncrementalMagFreqDist> mfds, List<EvenlyDiscretizedFunc> cmlMFDs,
			List<IncrementalMagFreqDist> ucerf2MFDs, List<IncrementalMagFreqDist> ucerf2CmlMFDs,
			int id, String name, boolean nucleation) throws IOException {
		CommandLineInversionRunner.writeParentSectMFDPlot(dir, mfds, cmlMFDs, false, ucerf2MFDs, ucerf2CmlMFDs, id,
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
		private Map<Integer, List<EvenlyDiscretizedFunc>> plotNuclCmlMFDs = Maps.newHashMap();
		private Map<Integer, List<EvenlyDiscretizedFunc>> plotPartCmlMFDs = Maps.newHashMap();
		
		private static double[] getDefaultFractiles() {
//			double[] ret = { 0.5 };
			double[] ret = {};
			return ret;
		}
		
		public ParentSectMFDsPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultFractiles());
		}
		
		public ParentSectMFDsPlot(BranchWeightProvider weightProvider, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.fractiles = fractiles;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
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
				plotNuclCmlMFDs.put(parentID,  asEvenly(getFractiles(asCml(
						nuclIncrMFDs.get(parentID)), weightsMap.get(parentID),
						"Cumulative Nucleation MFD", new double[0])));
				plotPartIncrMFDs.put(parentID, asIncr(getFractiles(
						partIncrMFDs.get(parentID), weightsMap.get(parentID),
						"Incremental Participation MFD", fractiles)));
				plotPartCmlMFDs.put(parentID,  asEvenly(getFractiles(asCml(
						partIncrMFDs.get(parentID)), weightsMap.get(parentID),
						"Cumulative Participation MFD", new double[0])));
			}
		}
		
		private static XY_DataSetList asCml(XY_DataSetList xyList) {
			XY_DataSetList cmlList = new XY_DataSetList();
			for (XY_DataSet xy : xyList)
				cmlList.add(((IncrementalMagFreqDist)xy).getCumRateDist());
			return cmlList;
		}
		
		private static List<IncrementalMagFreqDist> asIncr(List<DiscretizedFunc> funcs) {
			List<IncrementalMagFreqDist> incrMFDs = Lists.newArrayList();
			for (DiscretizedFunc func : funcs)
				incrMFDs.add((IncrementalMagFreqDist)func);
			return incrMFDs;
		}
		
		private static List<EvenlyDiscretizedFunc> asEvenly(List<DiscretizedFunc> funcs) {
			List<EvenlyDiscretizedFunc> incrMFDs = Lists.newArrayList();
			for (DiscretizedFunc func : funcs)
				incrMFDs.add((EvenlyDiscretizedFunc)func);
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
				FaultSystemSolution sol, int solIndex) {
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
	
	public static void writeMiniSectRITables(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir, String prefix) throws IOException {
		MiniSectRIPlot plot = new MiniSectRIPlot(weightProvider);
		plot.buildPlot(fetch);
		
		writeMiniSectRITables(plot, dir, prefix);
	}
	
	public static void writeMiniSectRITables(
			MiniSectRIPlot plot,
			File dir, String prefix) throws IOException {
		System.out.println("Making mini sect RI plot!");
		
		for (FaultModels fm : plot.solRatesMap.keySet()) {
			Map<Integer, DeformationSection> dm = plot.loadDM(fm);
			
			for (int i=0; i<plot.minMags.length; i++) {
				File file = new File(dir, prefix+"_"+fm.getShortName()
						+"_mini_sect_RIs_"+(float)plot.minMags[i]+"+.csv");
				MiniSectRecurrenceGen.writeRates(file, dm, plot.avgRatesMap.get(fm).get(i));
			}
		}
	}
	
	public static class MiniSectRIPlot extends CompoundFSSPlots {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private double[] minMags = { 6.7d };
		
		private transient BranchWeightProvider weightProvider;
		
		private transient ConcurrentMap<FaultModels, Map<Integer, List<List<Integer>>>>
				fmMappingsMap = Maps.newConcurrentMap();
		
		private Map<FaultModels, List<List<Map<Integer, List<Double>>>>> solRatesMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();
		
		private Map<FaultModels, List<Map<Integer, List<Double>>>> avgRatesMap = Maps.newHashMap();
		
		public MiniSectRIPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
		}
		
		private transient Map<FaultModels, Map<Integer, DeformationSection>> fmDMsMap =
				Maps.newHashMap();
		private synchronized Map<Integer, DeformationSection> loadDM(FaultModels fm) {
			Map<Integer, DeformationSection> dm = fmDMsMap.get(fm);
			if (dm == null) {
				try {
					dm = DeformationModelFileParser.load(DeformationModels.GEOLOGIC.getDataFileURL(fm));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
				fmDMsMap.put(fm, dm);
				List<List<Map<Integer, List<Double>>>> solRates = Lists.newArrayList();
				for (int i=0; i<minMags.length; i++)
					solRates.add(new ArrayList<Map<Integer, List<Double>>>());
				List<Double> weights = Lists.newArrayList();
				solRatesMap.put(fm, solRates);
				weightsMap.put(fm, weights);
			}
			return dm;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
			FaultModels fm = sol.getFaultModel();
			
			Map<Integer, DeformationSection> dm = loadDM(fm);
			
			Map<Integer, List<List<Integer>>> mappings = fmMappingsMap.get(fm);
			if (mappings == null) {
				synchronized (this) {
					mappings = fmMappingsMap.get(fm);
					if (mappings == null) {
						mappings = MiniSectRecurrenceGen.buildSubSectMappings(
								dm, sol.getFaultSectionDataList());
						fmMappingsMap.putIfAbsent(fm, mappings);
					}
				}
			}
			
			double weight = weightProvider.getWeight(branch);
			
			List<Map<Integer, List<Double>>> myRates = Lists.newArrayList();
			for (int i=0; i<minMags.length; i++) {
				myRates.add(MiniSectRecurrenceGen.calcMinisectionParticRates(
						sol, mappings, minMags[i], false));
			}
			synchronized (this) {
				for (int i=0; i<minMags.length; i++) {
					solRatesMap.get(fm).get(i).add(myRates.get(i));
				}
				weightsMap.get(fm).add(weight);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				MiniSectRIPlot o = (MiniSectRIPlot)otherCalc;
				for (FaultModels fm : o.solRatesMap.keySet()) {
					if (!solRatesMap.containsKey(fm)) {
						List<List<Map<Integer, List<Double>>>> solRates = Lists.newArrayList();
						List<Double> weights = Lists.newArrayList();
						solRatesMap.put(fm, solRates);
						weightsMap.put(fm, weights);
					}
					for (int i=0; i<minMags.length; i++) {
						solRatesMap.get(fm).get(i).addAll(o.solRatesMap.get(fm).get(i));
					}
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}

		@Override
		protected void finalizePlot() {
			for (int i=0; i<minMags.length; i++) {
				for (FaultModels fm : solRatesMap.keySet()) {
					List<Map<Integer, List<Double>>> avgRatesList = avgRatesMap.get(fm);
					if (!avgRatesMap.containsKey(fm)) {
						avgRatesList = Lists.newArrayList();
						avgRatesMap.put(fm, avgRatesList);
					}
					List<Map<Integer, List<Double>>> solRates = solRatesMap.get(fm).get(i);
					double[] weights = Doubles.toArray(weightsMap.get(fm));
					Set<Integer> parents = solRates.get(0).keySet();
					Map<Integer, List<Double>> avg = Maps.newHashMap();
					
					for (Integer parentID : parents) {
						List<double[]> solRatesList = Lists.newArrayList();
						int numMinis = solRates.get(0).get(parentID).size();
						for (int m=0; m<numMinis; m++)
							solRatesList.add(new double[solRates.size()]);
						for (int s=0; s<solRates.size(); s++) {
							List<Double> rates = solRates.get(s).get(parentID);
							for (int m=0; m<rates.size(); m++)
								solRatesList.get(m)[s] = rates.get(m);
						}
						
						List<Double> avgRates = Lists.newArrayList();
						
						for (int m=0; m<numMinis; m++) {
							double avgRate = FaultSystemSolutionFetcher.calcScaledAverage(
									solRatesList.get(m), weights);
							double ri = 1d/avgRate;
							avgRates.add(ri);
						}
						avg.put(parentID, avgRates);
						
//						if (parentID == 651) {
//							System.out.println("Avg: "+Joiner.on(",").join(avgRates));
//							System.out.println("Branches:");
//							for (int j=0; j<solRates.size(); j++) {
//								Map<Integer, List<Double>> sol = solRates.get(j);
//								System.out.println("\t"+Joiner.on(",").join(sol.get(parentID))+" (weight="+(float)weights[j]+")");
//							}
//						}
					}
					avgRatesList.add(avg);
				}
			}
		}

		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
		
	}
	
	public static void writePaleoRatesTables(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider,
			File dir, String prefix) throws IOException {
		PaleoRatesTable plot = new PaleoRatesTable(weightProvider);
		plot.buildPlot(fetch);
		
		writePaleoRatesTables(plot, dir, prefix);
	}
	
	public static void writePaleoRatesTables(
			PaleoRatesTable plot,
			File dir, String prefix) throws IOException {
		System.out.println("Making paleo/ave slip tables!");
		
		for (FaultModels fm : plot.aveSlipCSVOutputMap.keySet()) {
			File subDir = new File(dir, "paleo_fault_based");
			if (!subDir.exists())
				subDir.mkdir();
			File aveSlipFile = new File(subDir, fm.getShortName()
					+"_ave_slip_rates.csv");
			plot.aveSlipCSVOutputMap.get(fm).writeToFile(aveSlipFile);
			File paleoFile = new File(subDir, fm.getShortName()
					+"_paleo_rates.csv");
			plot.paleoCSVOutputMap.get(fm).writeToFile(paleoFile);
		}
	}
	
	public static class PaleoRatesTable extends CompoundFSSPlots {
		
		private transient BranchWeightProvider weightProvider;
		private transient PaleoProbabilityModel paleoProbModel;
		
		private ConcurrentMap<FaultModels, List<PaleoRateConstraint>> paleoConstraintsMap = Maps.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<AveSlipConstraint>> aveSlipConstraintsMap = Maps.newConcurrentMap();
		private transient ConcurrentMap<FaultModels, ConcurrentMap<Integer, List<Integer>>> rupsForSectsMap = Maps.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> reducedSlipsMap = Maps.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> proxyAveSlipRatesMap = Maps.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> aveSlipObsRatesMap = Maps.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> paleoObsRatesMap = Maps.newConcurrentMap();
		
		private ConcurrentMap<FaultModels, List<Double>> weightsMap = Maps.newConcurrentMap();
		
		private transient Map<FaultModels, CSVFile<String>> aveSlipCSVOutputMap = Maps.newHashMap();
		private transient Map<FaultModels, CSVFile<String>> paleoCSVOutputMap = Maps.newHashMap();
		
		public PaleoRatesTable(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
			
			try {
				paleoProbModel = UCERF3_PaleoProbabilityModel.load();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
			FaultModels fm = sol.getFaultModel();
			
			List<AveSlipConstraint> aveSlipConstraints = aveSlipConstraintsMap.get(fm);
			if (aveSlipConstraints == null) {
				synchronized (this) {
					aveSlipConstraints = aveSlipConstraintsMap.get(fm);
					List<PaleoRateConstraint> paleoConstraints = null;
					if (aveSlipConstraints == null) {
						try {
							aveSlipConstraints = AveSlipConstraint.load(sol.getFaultSectionDataList());
							paleoConstraints = UCERF3_PaleoRateConstraintFetcher.getConstraints(sol.getFaultSectionDataList());
						} catch (IOException e) {
							ExceptionUtils.throwAsRuntimeException(e);
						}
						paleoConstraintsMap.putIfAbsent(fm, paleoConstraints);
						ConcurrentMap<Integer, List<Integer>> rupsForSectsLists = Maps.newConcurrentMap();
						for (AveSlipConstraint constr : aveSlipConstraints)
							rupsForSectsLists.putIfAbsent(constr.getSubSectionIndex(),
									sol.getRupturesForSection(constr.getSubSectionIndex()));
						for (PaleoRateConstraint constr : paleoConstraints)
							rupsForSectsLists.putIfAbsent(constr.getSectionIndex(),
									sol.getRupturesForSection(constr.getSectionIndex()));
						rupsForSectsMap.putIfAbsent(fm, rupsForSectsLists);
						List<double[]> slipsList = Lists.newArrayList();
						reducedSlipsMap.putIfAbsent(fm, slipsList);
						List<double[]> proxyRatesList = Lists.newArrayList();
						proxyAveSlipRatesMap.putIfAbsent(fm, proxyRatesList);
						List<double[]> obsRatesList = Lists.newArrayList();
						aveSlipObsRatesMap.putIfAbsent(fm, obsRatesList);
						List<double[]> paleoObsRatesList = Lists.newArrayList();
						paleoObsRatesMap.putIfAbsent(fm, paleoObsRatesList);
						List<Double> weightsList = Lists.newArrayList();
						weightsMap.putIfAbsent(fm, weightsList);
						
						
						// must be last
						aveSlipConstraintsMap.putIfAbsent(fm, aveSlipConstraints);
					}
				}
			}
			
			double[] slips = new double[aveSlipConstraints.size()];
			double[] proxyRates = new double[aveSlipConstraints.size()];
			double[] obsRates = new double[aveSlipConstraints.size()];
			
			Map<Integer, List<Integer>> rupsForSectsLists = rupsForSectsMap.get(fm);
			
			for (int i=0; i<aveSlipConstraints.size(); i++) {
				AveSlipConstraint constr = aveSlipConstraints.get(i);
				int subsectionIndex = constr.getSubSectionIndex();
				
				slips[i] = sol.getSlipRateForSection(subsectionIndex);
				proxyRates[i] = slips[i] / constr.getWeightedMean();
				double obsRate = 0d;
				for (int rupID : rupsForSectsLists.get(constr.getSubSectionIndex())) {
					int sectIndexInRup = sol.getSectionsIndicesForRup(rupID).indexOf(subsectionIndex);
					double slipOnSect = sol.getSlipOnSectionsForRup(rupID)[sectIndexInRup];
					double probVisible = AveSlipConstraint.getProbabilityOfObservedSlip(slipOnSect);
					obsRate += sol.getRateForRup(rupID)*probVisible;
				}
				obsRates[i] = obsRate;
			}
			
			List<PaleoRateConstraint> paleoConstraints = paleoConstraintsMap.get(fm);
			
			double[] paleoRates = new double[paleoConstraints.size()];
			for (int i=0; i<paleoConstraints.size(); i++) {
				PaleoRateConstraint constr = paleoConstraints.get(i);
				
				double obsRate = 0d;
				for (int rupID : rupsForSectsLists.get(constr.getSectionIndex())) {
					obsRate += sol.getRateForRup(rupID)*paleoProbModel.getProbPaleoVisible(sol, rupID, constr.getSectionIndex());
				}
				paleoRates[i] = obsRate;
			}
			
			synchronized (this) {
				weightsMap.get(fm).add(weightProvider.getWeight(branch));
				reducedSlipsMap.get(fm).add(slips);
				proxyAveSlipRatesMap.get(fm).add(proxyRates);
				aveSlipObsRatesMap.get(fm).add(obsRates);
				paleoObsRatesMap.get(fm).add(paleoRates);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				PaleoRatesTable o = (PaleoRatesTable)otherCalc;
				
				for (FaultModels fm : o.weightsMap.keySet()) {
					if (!weightsMap.containsKey(fm)) {
						weightsMap.put(fm, new ArrayList<Double>());
						aveSlipConstraintsMap.put(fm, o.aveSlipConstraintsMap.get(fm));
						paleoConstraintsMap.put(fm, o.paleoConstraintsMap.get(fm));
						reducedSlipsMap.put(fm, new ArrayList<double[]>());
						proxyAveSlipRatesMap.put(fm, new ArrayList<double[]>());
						aveSlipObsRatesMap.put(fm, new ArrayList<double[]>());
						paleoObsRatesMap.put(fm, new ArrayList<double[]>());
					}
					
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
					reducedSlipsMap.get(fm).addAll(o.reducedSlipsMap.get(fm));
					proxyAveSlipRatesMap.get(fm).addAll(o.proxyAveSlipRatesMap.get(fm));
					aveSlipObsRatesMap.get(fm).addAll(o.aveSlipObsRatesMap.get(fm));
					paleoObsRatesMap.get(fm).addAll(o.paleoObsRatesMap.get(fm));
				}
			}
		}

		@Override
		protected void finalizePlot() {
			SimpleFaultSystemSolution ucerf2Sol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
			List<AveSlipConstraint> ucerf2AveSlipConstraints;
			List<PaleoRateConstraint> ucerf2PaleoConstraints;
			try {
				ucerf2AveSlipConstraints = AveSlipConstraint.load(ucerf2Sol.getFaultSectionDataList());
				ucerf2PaleoConstraints = UCERF3_PaleoRateConstraintFetcher.getConstraints(ucerf2Sol.getFaultSectionDataList());
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			
			// ave slip table
			for (FaultModels fm : weightsMap.keySet()) {
				CSVFile<String> csv = new CSVFile<String>(true);
				
				List<String> header = Lists.newArrayList(fm.getShortName()+" Mapping", "Latitude", "Longitude",
						"Weighted Mean Slip", "UCERF2 Reduced Slip Rate", "UCERF2 Proxy Event Rate",
						"UCERF3 Mean Reduced Slip Rate", "UCERF3 Mean Proxy Event Rate", "UCERF3 Mean Paleo Visible Rate",
						"UCERF3 Min Paleo Visible Rate", "UCERF3 Max Paleo Visible Rate");
				
				csv.addLine(header);
				
				List<AveSlipConstraint> constraints = aveSlipConstraintsMap.get(fm);
				
				for (int i=0; i<constraints.size(); i++) {
					AveSlipConstraint constr = constraints.get(i);
					
					// find matching UCERF2 constraint
					AveSlipConstraint ucerf2Constraint = null;
					for (AveSlipConstraint u2Constr : ucerf2AveSlipConstraints) {
						if (u2Constr.getSiteLocation().equals(constr.getSiteLocation())) {
							ucerf2Constraint = u2Constr;
							break;
						}
					}
					
					List<String> line = Lists.newArrayList();
					line.add(constr.getSubSectionName());
					line.add(constr.getSiteLocation().getLatitude()+"");
					line.add(constr.getSiteLocation().getLongitude()+"");
					line.add(constr.getWeightedMean()+"");
					if (ucerf2Constraint == null) {
						line.add("");
						line.add("");
					} else {
						double ucerf2SlipRate = ucerf2Sol.getSlipRateForSection(ucerf2Constraint.getSubSectionIndex());
						line.add(ucerf2SlipRate+"");
						double ucerf2ProxyRate = ucerf2SlipRate / constr.getWeightedMean();
						line.add(ucerf2ProxyRate+"");
					}
					List<double[]> reducedSlipList = reducedSlipsMap.get(fm);
					List<double[]> proxyRatesList = proxyAveSlipRatesMap.get(fm);
					List<double[]> obsRatesList = aveSlipObsRatesMap.get(fm);
					
					int numSols = reducedSlipList.size();
					double[] slips = new double[numSols];
					double[] proxyRates = new double[numSols];
					double[] rates = new double[numSols];
					double[] weigths = Doubles.toArray(weightsMap.get(fm));
					
					for (int j=0; j<numSols; j++) {
						slips[j] = reducedSlipList.get(j)[i];
						proxyRates[j] = proxyRatesList.get(j)[i];
						rates[j] = obsRatesList.get(j)[i];
					}
					
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(slips, weigths)+"");
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(proxyRates, weigths)+"");
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(rates, weigths)+"");
					line.add(StatUtils.min(rates)+"");
					line.add(StatUtils.max(rates)+"");
					
					csv.addLine(line);
				}
				
				aveSlipCSVOutputMap.put(fm, csv);
			}
			
			// paleo table
			for (FaultModels fm : weightsMap.keySet()) {
				CSVFile<String> csv = new CSVFile<String>(true);
				
				List<String> header = Lists.newArrayList(fm.getShortName()+" Mapping", "Latitude", "Longitude",
						"Paleo Observed Rate", "Paleo Observed Lower Bound", "Paleo Observed Upper Bound",
						"UCERF2 Proxy Event Rate", "UCERF3 Mean Paleo Visible Rate",
						"UCERF3 Min Paleo Visible Rate", "UCERF3 Max Paleo Visible Rate");
				
				csv.addLine(header);
				
				List<PaleoRateConstraint> constraints = paleoConstraintsMap.get(fm);
				
				for (int i=0; i<constraints.size(); i++) {
					PaleoRateConstraint constr = constraints.get(i);
					
					// find matching UCERF2 constraint
					PaleoRateConstraint ucerf2Constraint = null;
					for (PaleoRateConstraint u2Constr : ucerf2PaleoConstraints) {
						if (u2Constr.getPaleoSiteLoction().equals(constr.getPaleoSiteLoction())) {
							ucerf2Constraint = u2Constr;
							break;
						}
					}
					
					List<String> line = Lists.newArrayList();
					line.add(constr.getFaultSectionName());
					line.add(constr.getPaleoSiteLoction().getLatitude()+"");
					line.add(constr.getPaleoSiteLoction().getLongitude()+"");
					line.add(constr.getMeanRate()+"");
					line.add(constr.getLower95ConfOfRate()+"");
					line.add(constr.getUpper95ConfOfRate()+"");
					if (ucerf2Constraint == null) {
						line.add("");
					} else {
						line.add(PaleoFitPlotter.getPaleoRateForSect(ucerf2Sol, ucerf2Constraint.getSectionIndex(), paleoProbModel)+"");
					}
					List<double[]> obsRatesList = paleoObsRatesMap.get(fm);
					
					int numSols = obsRatesList.size();
					double[] rates = new double[numSols];
					double[] weigths = Doubles.toArray(weightsMap.get(fm));
					
					for (int j=0; j<numSols; j++)
						rates[j] = obsRatesList.get(j)[i];
					
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(rates, weigths)+"");
					line.add(StatUtils.min(rates)+"");
					line.add(StatUtils.max(rates)+"");
					
					csv.addLine(line);
				}
				
				paleoCSVOutputMap.put(fm, csv);
			}
		}

		@Override
		protected boolean usesInversionFSS() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	private static List<DiscretizedFunc> getFractiles(
			XY_DataSetList data, List<Double> weights, String name, double[] fractiles) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		
		FractileCurveCalculator calc = new FractileCurveCalculator(data, weights);
		for (double fractile : fractiles) {
			DiscretizedFunc func = (DiscretizedFunc)calc.getFractile(fractile);
			func.setName(name+" (fractile at "+fractile+")");
			funcs.add(func);
		}
		DiscretizedFunc meanFunc = (DiscretizedFunc) calc.getMeanCurve();
		meanFunc.setName(name+" (weighted mean)");
		funcs.add(meanFunc);
		DiscretizedFunc minFunc = (DiscretizedFunc) calc.getMinimumCurve();
		minFunc.setName(name+" (minimum)");
		funcs.add(minFunc);
		DiscretizedFunc maxFunc = (DiscretizedFunc) calc.getMaximumCurve();
		maxFunc.setName(name+" (maximum)");
		funcs.add(maxFunc);
		
		return funcs;
	}
	
	public static class SlipMisfitPlot extends MapBasedPlot {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final String PLOT_DATA_FILE_NAME = "slip_misfit_plots.xml";
		
		private transient BranchWeightProvider weightProvider;
		
		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps.newConcurrentMap();
		private Map<FaultModels, List<double[]>> ratiosMap = Maps.newHashMap();
		private Map<FaultModels, List<double[]>> fractDiffMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();
		
		private List<MapPlotData> plots;
		
		private static int cnt;
		
		public SlipMisfitPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
			
			cnt = 0;
		}
	
		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
			int myCnt = cnt++;
			System.out.println("Processing solution "+myCnt);
			
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;
			
			double[] solSlips = sol.calcSlipRateForAllSects();
			double[] targetSlips = sol.getSlipRateForAllSections();
			double[] ratios = new double[solSlips.length];
			double[] fractDiffs = new double[solSlips.length];
			for (int i=0; i<solSlips.length; i++) {
				if (solSlips[i] == 0 && targetSlips[i] == 0) {
					ratios[i] = 1;
					fractDiffs[i] = 0;
				} else {
					ratios[i] = solSlips[i] / targetSlips[i];
					fractDiffs[i] = (solSlips[i] - targetSlips[i]) / targetSlips[i];
				}
			}
			
			FaultModels fm = sol.getFaultModel();
			
			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(
						sol.getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}
			
			System.out.println("Archiving solution "+myCnt);
			
			synchronized (this) {
				List<double[]> ratiosList = ratiosMap.get(fm);
				if (ratiosList == null) {
					ratiosList = Lists.newArrayList();
					ratiosMap.put(fm, ratiosList);
				}
				ratiosList.add(ratios);
				
				List<double[]> fractDiffsList = fractDiffMap.get(fm);
				if (fractDiffsList == null) {
					fractDiffsList = Lists.newArrayList();
					fractDiffMap.put(fm, fractDiffsList);
				}
				fractDiffsList.add(fractDiffs);
				
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
				for (FaultModels fm : o.ratiosMap.keySet()) {
					if (!faultsMap.containsKey(fm)) {
						faultsMap.put(fm, o.faultsMap.get(fm));
						ratiosMap.put(fm, new ArrayList<double[]>());
						fractDiffMap.put(fm, new ArrayList<double[]>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					ratiosMap.get(fm).addAll(o.ratiosMap.get(fm));
					fractDiffMap.get(fm).addAll(o.fractDiffMap.get(fm));
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
				List<double[]> ratiosList = ratiosMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);
				
				double[] ratios = getWeightedAvg(faults.size(), ratiosList, weightsList);
				
				String label = "Mean(Solution Slip / Target Slip)";
				String prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName()+"_";
					label = fm.getShortName()+" "+label;
				}
				
				plots.add(new MapPlotData(linearCPT, faults, ratios, region,
						skipNans, label, prefix+"slip_misfit"));
				
				label = "Log10("+label+")";
				double[] log10Values = FaultBasedMapGen.log10(ratios);
				plots.add(new MapPlotData(logCPT, faults, log10Values, region,
						skipNans, label, prefix+"slip_misfit_log"));
				
				List<double[]> fractDiffList = fractDiffMap.get(fm);
				double[] fractDiffs = getWeightedAvg(faults.size(), fractDiffList, weightsList);
				
				label = "Mean((Solution Slip - Target Slip) / Target Slip)";
				prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName()+"_";
					label = fm.getShortName()+" "+label;
				}
				
				plots.add(new MapPlotData(logCPT, faults, fractDiffs, region,
						skipNans, label, prefix+"slip_misfit_fract_diff"));
			}
		}
	
		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
	
		@Override
		protected List<MapPlotData> getPlotData() {
			return plots;
		}
	
		@Override
		protected String getPlotDataFileName() {
			return PLOT_DATA_FILE_NAME;
		}
		
	}
	
	public static class ParticipationMapPlot extends MapBasedPlot {
		
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
		
		private List<MapPlotData> plots;
		
		public ParticipationMapPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultRanges());
		}
		
		public ParticipationMapPlot(BranchWeightProvider weightProvider, List<double[]> ranges) {
			this.weightProvider = weightProvider;
			this.ranges = ranges;
		}
	
		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
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
					
					plots.add(new MapPlotData(participationCPT, faults, logValues, region,
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
					
					plots.add(new MapPlotData(logCPT, faults, ratios, region,
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
					
					plots.add(new MapPlotData(logCPT, faults, stdNormVals, region,
							skipNans, title, name));
				}
			}
		}
	
		@Override
		protected boolean usesInversionFSS() {
			return false;
		}
	
		@Override
		protected List<MapPlotData> getPlotData() {
			return plots;
		}
	
		@Override
		protected String getPlotDataFileName() {
			return "participation_plots.xml";
		}
		
	}
	
	public static class GriddedParticipationMapPlot extends MapBasedPlot {
		
		private List<double[]> ranges;
		private double spacing;
		
		private List<List<GeoDataSet>> datas;
		private List<Double> weights;
		private GriddedRegion griddedRegion;
		
		private Table<FaultModels, MaxMagOffFault, ConcurrentMap<Integer, int[]>>
				rupNodesCache = HashBasedTable.create();
		
		private List<MapPlotData> plots;
		
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
		
		public GriddedParticipationMapPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, 0.1d);
		}
		
		public GriddedParticipationMapPlot(BranchWeightProvider weightProvider, double spacing) {
			this(weightProvider, getDefaultRanges(), spacing);
		}
		
		public GriddedParticipationMapPlot(BranchWeightProvider weightProvider, List<double[]> ranges, double spacing) {
			this.weightProvider = weightProvider;
			this.ranges = ranges;
			this.spacing = spacing;
			griddedRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
			
			datas = Lists.newArrayList();
			weights = Lists.newArrayList();
		}

		@Override
		protected String getPlotDataFileName() {
			return "gridded_participation_plots.xml";
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				FaultSystemSolution sol, int solIndex) {
			processERF(branch, new UCERF3_FaultSysSol_ERF((InversionFaultSystemSolution)sol), 0);
		}
		
		@Override
		protected void processERF(LogicTreeBranch branch,
				UCERF3_FaultSysSol_ERF erf, int solIndex) {
			FaultModels fm = branch.getValue(FaultModels.class);
			MaxMagOffFault mmax = branch.getValue(MaxMagOffFault.class);
			synchronized (this) {
				if (!rupNodesCache.contains(fm, mmax)) {
					ConcurrentMap<Integer, int[]> cache = Maps.newConcurrentMap();
					rupNodesCache.put(fm, mmax, cache);
				}
			}
			ConcurrentMap<Integer, int[]> cache = rupNodesCache.get(fm, mmax);
			cache = Maps.newConcurrentMap();
			List<GeoDataSet> datasets = Lists.newArrayList();
			for (double[] range : ranges) {
				double minMag = range[0];
				double maxMag = range[1];
				datasets.add(ERF_Calculator.getParticipationRatesInRegion(erf, griddedRegion, minMag, maxMag, cache));
			}
			synchronized (this) {
				datas.add(datasets);
				weights.add(weightProvider.getWeight(branch));
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				GriddedParticipationMapPlot o = (GriddedParticipationMapPlot)otherCalc;
				datas.addAll(o.datas);
				weights.addAll(o.weights);
			}
		}

		@Override
		protected void finalizePlot() {
			boolean debug = true;
			
			plots = Lists.newArrayList();
			
			CPT participationCPT = FaultBasedMapGen.getParticipationCPT();
			CPT ratioCPT = (CPT) FaultBasedMapGen.getLogRatioCPT().clone();
			ratioCPT.setNanColor(Color.WHITE);
			
			MeanUCERF2 ucerf2 = new MeanUCERF2();
			ucerf2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			ucerf2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
			ucerf2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
			ucerf2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
			ucerf2.getTimeSpan().setDuration(1d);
			ucerf2.updateForecast();
			
			for (int r=0; r<ranges.size(); r++) {
				double[] range = ranges.get(r);
				double minMag = range[0];
				double maxMag = range[1];
				
				XY_DataSetList funcs = new XY_DataSetList();
				
				for (int i=0; i<datas.size(); i++) {
					GeoDataSet data = datas.get(i).get(r);
					EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, data.size(), 1d);
					for (int j=0; j<data.size(); j++)
						func.set(j, data.get(j));
					
					funcs.add(func);
				}
				
				FractileCurveCalculator calc = new FractileCurveCalculator(funcs, weights);
				
				GriddedGeoDataSet data = new GriddedGeoDataSet(griddedRegion, true);
				AbstractXY_DataSet meanDataFunc = calc.getMeanCurve();
				Preconditions.checkState(meanDataFunc.getNum() == data.size());
				for (int i=0; i<data.size(); i++)
					data.set(i, meanDataFunc.getY(i));
				
				double[] weightsArray = Doubles.toArray(weights);
				
				data = new GriddedGeoDataSet(griddedRegion, true);
				for (int i=0; i<data.size(); i++) {
					double[] vals = new double[datas.size()];
					for (int j=0; j<datas.size(); j++)
						vals[j] = datas.get(j).get(r).get(i);
					data.set(i, FaultSystemSolutionFetcher.calcScaledAverage(vals, weightsArray));
				}
				
				if (debug && r == 0) {
					for (int i=0; i<datas.size() && i<10; i++) {
						GeoDataSet subData = datas.get(i).get(r).copy();
						subData.log10();
						plots.add(new MapPlotData(participationCPT, subData, spacing, griddedRegion,
								true, "Sub Participation 5+ "+i, "sub_partic_5+_"+i));
					}
				}
				
				// TODO temp remove
//				data = (GriddedGeoDataSet) datas.get(0).get(0);
				
				// take log10
				GriddedGeoDataSet logData = data.copy();
				logData.log10();
				
				String name = "gridded_partic_rates_"+(float)minMag;
				String title = "Log10(Participation Rates "+(float)+minMag;
				if (maxMag < 9) {
					name += "_"+(float)maxMag;
					title += "=>"+(float)maxMag;
				} else {
					name += "+";
					title += "+";
				}
				title += ")";
				
				plots.add(new MapPlotData(participationCPT, logData,
						true, title, name));
				
				GriddedGeoDataSet ucerf2Vals = ERF_Calculator.getParticipationRatesInRegion(
						ucerf2, griddedRegion, range[0], range[1]);
				
				GeoDataSet ratios = GeoDataSetMath.divide(data, ucerf2Vals);
				
				ratios.log10();
				
				name = "gridded_partic_ratio_"+(float)minMag;
				title = "Log10(Participation Ratios "+(float)+minMag;
				if (maxMag < 9) {
					name += "_"+(float)maxMag;
					title += "=>"+(float)maxMag;
				} else {
					name += "+";
					title += "+";
				}
				title += ")";
				
				plots.add(new MapPlotData(ratioCPT, ratios, spacing, griddedRegion,
						true, title, name));
				
//				double[] stdNormVals = new double[values.length];
//				
//				for (int s=0; s<stdNormVals.length; s++) {
//					if (ucerf2Vals[s] == 0)
//						stdNormVals[s] = Double.NaN;
//					else
//						stdNormVals[s] = (values[s] - ucerf2Vals[s]) / stdDevs[s];
//				}
//				
//				name = "partic_diffs_norm_std_dev_"+(float)minMag;
//				title = "(U3mean - U2mean)/U3std "+(float)+minMag;
//				if (maxMag < 9) {
//					name += "_"+(float)maxMag;
//					title += "=>"+(float)maxMag;
//				} else {
//					name += "+";
//					title += "+";
//				}
////				title += ")";
//				
//				if (multipleFMs) {
//					name = fm.getShortName()+"_"+name;
//					title = fm.getShortName()+" "+title;
//				}
//				
//				plots.add(new MapPlotData(logCPT, faults, stdNormVals, region,
//						skipNans, title, name));
			}
		}

		@Override
		protected List<MapPlotData> getPlotData() {
			return plots;
		}

		@Override
		protected boolean usesInversionFSS() {
			return true;
		}

		@Override
		protected boolean usesERFs() {
			return true;
		}
		
	}

	private static class MapPlotData implements XMLSaveable, Serializable {

		private static final String XML_METADATA_NAME = "FaultBasedMap";
		
		private CPT cpt;
		private List<LocationList> faults;
		private double[] faultValues;
		private GeoDataSet griddedData;
		private double spacing;
		private Region region;
		private boolean skipNans;
		private String label;
		private String fileName;
		
		public MapPlotData(CPT cpt, List<LocationList> faults,
			double[] faultValues, Region region, boolean skipNans, String label, String fileName) {
			this(cpt, faults, faultValues, null, 1d, region, skipNans, label, fileName);
		}
		
		public MapPlotData(CPT cpt, GriddedGeoDataSet griddedData, boolean skipNans,
				String label, String fileName) {
			this(cpt, griddedData, griddedData.getRegion().getSpacing(), griddedData.getRegion(), skipNans, label, fileName);
		}
		
		public MapPlotData(CPT cpt, GeoDataSet griddedData, double spacing, Region region, boolean skipNans,
				String label, String fileName) {
			this(cpt, null, null, griddedData, spacing, region, skipNans, label, fileName);
		}
		
		public MapPlotData(CPT cpt, List<LocationList> faults, double[] faultValues, GeoDataSet griddedData, double spacing,
				Region region, boolean skipNans, String label, String fileName) {
			this.cpt = cpt;
			this.faults = faults;
			this.griddedData = griddedData;
			this.spacing = spacing;
			this.faultValues = faultValues;
			this.region = region;
			this.skipNans = skipNans;
			this.label = label;
			this.fileName = fileName;
		}
		
		public static MapPlotData fromXMLMetadata(Element xml) {
			CPT cpt = CPT.fromXMLMetadata(xml.element(CPT.XML_METADATA_NAME));
			
			List<LocationList> faults;
			double[] values;
			GeoDataSet griddedData;
			double spacing = 1;
			
			Element geoEl = xml.element("GeoDataSet");
			if (geoEl != null) {
				// gridded
				values = null;
				faults = null;
				
				spacing = Double.parseDouble(geoEl.attributeValue("spacing"));
				
				Iterator<Element> it = geoEl.elementIterator("Node");
				List<Location> locs = Lists.newArrayList();
				List<Double> nodeVals = Lists.newArrayList();
				while (it.hasNext()) {
					Element nodeElem = it.next();
					locs.add(Location.fromXMLMetadata(
							nodeElem.element(Location.XML_METADATA_NAME)));
					double nodeVal = Double.parseDouble(nodeElem.attributeValue("value"));
//					if (Double.isNaN(nodeVal))
//						System.out.println("NaN!!!!");
					nodeVals.add(nodeVal);
				}
				griddedData = new ArbDiscrGeoDataSet(true);
				for (int i=0; i<locs.size(); i++)
					griddedData.set(locs.get(i), nodeVals.get(i));
			} else {
				// fault based
				griddedData = null;
				
				faults = Lists.newArrayList();
				List<Double> valuesList = Lists.newArrayList();
				Iterator<Element> it = xml.elementIterator("Fault");
				while (it.hasNext()) {
					Element faultElem = it.next();
					faults.add(LocationList.fromXMLMetadata(
							faultElem.element(LocationList.XML_METADATA_NAME)));
					valuesList.add(Double.parseDouble(faultElem.attributeValue("value")));
				}
				values = Doubles.toArray(valuesList);
			}
			
			Region region = Region.fromXMLMetadata(xml.element(Region.XML_METADATA_NAME));
			
			boolean skipNans = Boolean.parseBoolean(xml.attributeValue("skipNans"));
			String label = xml.attributeValue("label");
			String fileName = xml.attributeValue("fileName");
			
			return new MapPlotData(cpt, faults, values, griddedData, spacing, region,
					skipNans, label, fileName);
		}

		@Override
		public Element toXMLMetadata(Element root) {
			Element xml = root.addElement(XML_METADATA_NAME);
			
			cpt.toXMLMetadata(xml);
			
			if (faults != null) {
				for (int i=0; i<faults.size(); i++) {
					Element faultElem = xml.addElement("Fault");
					faultElem.addAttribute("value", faultValues[i]+"");
					faults.get(i).toXMLMetadata(faultElem);
				}
			}
			if (griddedData != null) {
				Element geoEl = xml.addElement("GeoDataSet");
				geoEl.addAttribute("spacing", spacing+"");
				for (int i=0; i<griddedData.size(); i++) {
					Location loc = griddedData.getLocation(i);
					double val = griddedData.get(i);
					Element nodeEl = geoEl.addElement("Node");
					nodeEl.addAttribute("value", val+"");
					loc.toXMLMetadata(nodeEl);
				}
			}
			
			if (region instanceof GriddedRegion) {
				GriddedRegion gridded = (GriddedRegion)region;
				if (gridded.getSpacing() <= 0.11)
					new Region(region.getBorder(), null).toXMLMetadata(xml);
				else
					new Region(new Location(gridded.getMaxGridLat(), gridded.getMaxGridLon()),
							new Location(gridded.getMinGridLat(), gridded.getMinGridLon())).toXMLMetadata(xml);
			} else {
				region.toXMLMetadata(xml);
			}
			
			xml.addAttribute("skipNans", skipNans+"");
			xml.addAttribute("label", label);
			xml.addAttribute("fileName", fileName);
			
			return root;
		}
	}
	
	private static abstract class MapBasedPlot extends CompoundFSSPlots {
		
		protected abstract List<MapPlotData> getPlotData();
		
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
			
			for (MapPlotData data : getPlotData())
				data.toXMLMetadata(root);
			
			File dataFile = new File(dir, getPlotDataFileName());
			XMLUtils.writeDocumentToFile(dataFile, doc);
		}
		
		public static List<MapPlotData> loadPlotData(File file) throws MalformedURLException, DocumentException {
			Document doc = XMLUtils.loadDocument(file);
			Element root = doc.getRootElement();
			
			List<MapPlotData> plots = Lists.newArrayList();
			
			Iterator<Element> it = root.elementIterator(MapPlotData.XML_METADATA_NAME);
			while (it.hasNext())
				plots.add(MapPlotData.fromXMLMetadata(it.next()));
			
			return plots;
		}
		
		public void makeMapPlots(File dir, String prefix)
				throws GMT_MapException, RuntimeException, IOException {
			makeMapPlots(dir, prefix, this.getPlotData());
		}
		
		public static void makeMapPlot(File dir, String prefix, MapPlotData plot)
				throws GMT_MapException, RuntimeException, IOException {
			makeMapPlots(dir, prefix, Lists.newArrayList(plot));
		}
		
		public static void makeMapPlots(File dir, String prefix, List<MapPlotData> plots)
				throws GMT_MapException, RuntimeException, IOException {
			for (MapPlotData plot : plots) {
				String plotPrefix;
				if (prefix != null && !prefix.isEmpty())
					plotPrefix = prefix+"_";
				else
					plotPrefix = "";
				plotPrefix += plot.fileName;
				System.out.println("Making fault plot with title: "+plot.label);
				if (plot.griddedData == null)
					FaultBasedMapGen.makeFaultPlot(plot.cpt, plot.faults, plot.faultValues, plot.region,
							dir, plotPrefix, false, plot.skipNans, plot.label);
				else
					FaultBasedMapGen.plotMap(dir, plotPrefix, false, FaultBasedMapGen.buildMap(plot.cpt, null, null,
							plot.griddedData, plot.spacing, plot.region, plot.skipNans, plot.label));
				System.out.println("DONE.");
			}
		}
		
	}
	
	public static List<PlotCurveCharacterstics> getFractileChars(Color color, int numFractiles) {
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		PlotCurveCharacterstics thinChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, color);
		PlotCurveCharacterstics medChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color);
		PlotCurveCharacterstics thickChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, color);
		
		for (int i=0; i<numFractiles; i++)
			chars.add(medChar);
		chars.add(thickChar);
		chars.add(thinChar);
		chars.add(thinChar);
		
		return chars;
	}
	
	/**
	 * Called once for each solution
	 * @param branch
	 * @param sol
	 * @param solIndex TODO
	 */
	protected abstract void processSolution(LogicTreeBranch branch, FaultSystemSolution sol, int solIndex);

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
	
	protected boolean usesERFs() {
		return false;
	}
	
	protected void processERF(LogicTreeBranch branch, UCERF3_FaultSysSol_ERF erf, int solIndex) {
		// do nothing unless overridden
		if (usesERFs())
			throw new IllegalStateException("Must be overridden if usesERFs() == true");
		else
			throw new IllegalStateException("Should not be called if usesERFs() == false");
	}
	
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
		private UCERF3_FaultSysSol_ERF erf;
		private int index;
		
		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher,
				LogicTreeBranch branch,
				boolean invFSS,
				int index) {
			this(plots, fetcher, branch, invFSS, false, index);
		}
		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher,
				LogicTreeBranch branch,
				boolean invFSS,
				boolean mpj,
				int index) {
			this.plots = plots;
			this.fetcher = fetcher;
			this.branch = branch;
			this.invFSS = invFSS;
			this.mpj = mpj;
			this.index = index;
		}

		@Override
		public void compute() {
			try {
				FaultSystemSolution sol = fetcher.getSolution(branch);
				
				if (invFSS)
					sol = new InversionFaultSystemSolution(sol);
				
				for (CompoundFSSPlots plot : plots) {
					if (plot.usesERFs()) {
						if (erf == null) {
							erf = new UCERF3_FaultSysSol_ERF((InversionFaultSystemSolution)sol);
							erf.getParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME).setValue(true);
							erf.updateForecast();
						}
						plot.processERF(branch, erf, index);
					} else {
						plot.processSolution(branch, sol, index);
					}
				}
				erf = null;
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
			if (plot.usesInversionFSS() || plot.usesERFs()) {
				invFSS = true;
				break;
			}
		}
		
		List<Task> tasks = Lists.newArrayList();
		int index = 0;
		for (LogicTreeBranch branch : fetcher.getBranches()) {
			tasks.add(new PlotSolComputeTask(plots, fetcher, branch, invFSS, index++));
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
			} else if (plot instanceof ERFBasedRegionalMFDPlot) {
				ERFBasedRegionalMFDPlot mfd = (ERFBasedRegionalMFDPlot)plot;
				writeERFBasedRegionalMFDPlots(mfd.specs, mfd.regions, dir, prefix);
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
			} else if (plot instanceof MiniSectRIPlot) {
				MiniSectRIPlot miniPlot = (MiniSectRIPlot)plot;
				CompoundFSSPlots.writeMiniSectRITables(miniPlot, dir, prefix);
			} else if (plot instanceof PaleoRatesTable) {
				PaleoRatesTable aveSlip = (PaleoRatesTable)plot;
				CompoundFSSPlots.writePaleoRatesTables(aveSlip, dir, prefix);
			} else if (plot instanceof MapBasedPlot) {
				MapBasedPlot faultPlot = (MapBasedPlot)plot;
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
				MapBasedPlot.makeMapPlots(
						dir, prefix, MapBasedPlot.loadPlotData(plotFile));
			}
			System.exit(0);
		}
		
		UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = new UCERF2_TimeIndependentEpistemicList();
		ucerf2_erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		ucerf2_erf_list.getTimeSpan().setDuration(1d);
		ucerf2_erf_list.updateForecast();
		
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
//		File dir = new File("/tmp/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL");
//		File file = new File(dir, "2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip");
		File dir = new File("/tmp/comp_plots");
		File file = new File(dir, "2013_01_14-stampede_3p2_production_runs_combined_COMPOUND_SOL.zip");
//		File file = new File(dir, "zeng_convergence_compound.zip");
//		File file = new File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
		FaultSystemSolutionFetcher fetch = CompoundFaultSystemSolution.fromZipFile(file);
		double wts = 0;
		for (LogicTreeBranch branch : fetch.getBranches())
			wts += weightProvider.getWeight(branch);
		System.out.println("Total weight: "+wts);
//		System.exit(0);
		fetch = FaultSystemSolutionFetcher.getRandomSample(fetch, 10);
		
		new DeadlockDetectionThread(3000).start();
		
//		List<Region> regions = RegionalMFDPlot.getDefaultRegions();
//		List<Region> regions = RegionalMFDPlot.getDefaultRegions().subList(3, 5);
		List<Region> regions = RegionalMFDPlot.getDefaultRegions().subList(0, 1);
		regions = Lists.newArrayList(regions);
		
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
//		plots.add(new RegionalMFDPlot(weightProvider, regions));
//		plots.add(new PaleoFaultPlot(weightProvider));
//		plots.add(new PaleoSiteCorrelationPlot(weightProvider));
//		plots.add(new ParentSectMFDsPlot(weightProvider));
//		plots.add(new RupJumpPlot(weightProvider));
//		plots.add(new SlipMisfitPlot(weightProvider));
//		plots.add(new ParticipationMapPlot(weightProvider));
//		plots.add(new GriddedParticipationMapPlot(weightProvider, 0.1d));
//		plots.add(new ERFBasedRegionalMFDPlot(weightProvider, regions));
		plots.add(new MiniSectRIPlot(weightProvider));
//		plots.add(new PaleoRatesTable(weightProvider));
		
		batchPlot(plots, fetch, 1);
		
//		for (CompoundFSSPlots plot : plots)
//			FileUtils.saveObjectInFile("/tmp/asdf.obj", plot);
		batchWritePlots(plots, dir, prefix, true);
//		MapBasedPlot.makeMapPlots(dir, prefix,
//				MapBasedPlot.loadPlotData(new File(dir, SlipMisfitPlot.PLOT_DATA_FILE_NAME)));
		
		System.exit(0);
		
	}

}
