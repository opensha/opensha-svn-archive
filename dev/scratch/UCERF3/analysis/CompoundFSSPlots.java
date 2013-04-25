package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Attribute;
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
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.CompoundGriddedSurface;
import org.opensha.sha.faultSurface.RupInRegionCache;
import org.opensha.sha.faultSurface.RupNodesCache;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.griddedSeismicity.GridSourceFileReader;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.InversionTargetMFDs;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.VariableLogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelFileParser;
import scratch.UCERF3.utils.DeformationModelFileParser.DeformationSection;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
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

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public abstract class CompoundFSSPlots implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Color BROWN = new Color(130, 86, 5);

	public static List<PlotSpec> getRegionalMFDPlotSpecs(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, List<Region> regions) {
		RegionalMFDPlot plot = new RegionalMFDPlot(weightProvider, regions);

		plot.buildPlot(fetch);

		return plot.specs;
	}

	public static void writeRegionalMFDPlots(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, List<Region> regions,
			File dir, String prefix) throws IOException {
		List<PlotSpec> specs = getRegionalMFDPlotSpecs(fetch, weightProvider,
				regions);

		writeRegionalMFDPlots(specs, regions, dir, prefix);
	}

	public static void writeRegionalMFDPlots(List<PlotSpec> specs,
			List<Region> regions, File dir, String prefix) throws IOException {
		
		File subDir = new File(dir, "fss_mfd_plots");
		if (!subDir.exists())
			subDir.mkdir();

		int unnamedRegionCnt = 0;

		for (int i = 0; i < regions.size(); i++) {
			PlotSpec spec = specs.get(i);
			Region region = regions.get(i);

			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			CommandLineInversionRunner.setFontSizes(gp);
			gp.setYLog(true);
			gp.setUserBounds(5d, 9d, 1e-6, 1e0);

			gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
					spec.getFuncs(), spec.getChars(), true, spec.getTitle());

			String fname = prefix + "_MFD";
			if (region.getName() != null && !region.getName().isEmpty())
				fname += "_" + region.getName().replaceAll("\\W+", "_");
			else
				fname += "_UNNAMED_REGION_" + (++unnamedRegionCnt);

			File file = new File(subDir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath() + ".pdf");
			gp.saveAsPNG(file.getAbsolutePath() + ".png");
			gp.saveAsTXT(file.getAbsolutePath() + ".txt");
			File smallDir = new File(dir, "small_MFD_plots");
			if (!smallDir.exists())
				smallDir.mkdir();
			file = new File(smallDir, fname + "_small");
			gp.getCartPanel().setSize(500, 400);
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

			for (int i = 0; i < regions.size(); i++) {
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
				InversionFaultSystemSolution sol, int solIndex) {
			double wt = weightProvider.getWeight(branch);

			List<DiscretizedFunc> onMFDs = Lists.newArrayList();
			List<DiscretizedFunc> offMFDs = Lists.newArrayList();
			List<DiscretizedFunc> totMFDs = Lists.newArrayList();

			for (int i = 0; i < regions.size(); i++) {
				debug(solIndex, "calculating region " + i);
				Region region = regions.get(i);

				IncrementalMagFreqDist onMFD = sol.calcNucleationMFD_forRegion(
						region, minX, maxX, delta, true);
				onMFDs.add(onMFD);
				if (isStatewide(region)) {
					// we only have off fault for statewide right now
					IncrementalMagFreqDist offMFD = sol.getFinalTotalGriddedSeisMFD();
					EvenlyDiscretizedFunc trimmedOffMFD = new EvenlyDiscretizedFunc(
							onMFD.getMinX(), onMFD.getMaxX(), onMFD.getNum());
					EvenlyDiscretizedFunc totMFD = new EvenlyDiscretizedFunc(
							onMFD.getMinX(), onMFD.getMaxX(), onMFD.getNum());
					for (int n = 0; n < trimmedOffMFD.getNum(); n++) {
						double x = trimmedOffMFD.getX(n);
						if (x <= offMFD.getMaxX())
							trimmedOffMFD.set(n, offMFD.getY(x));
						totMFD.set(n, onMFD.getY(n) + trimmedOffMFD.getY(n));
					}
					offMFDs.add(trimmedOffMFD);
					totMFDs.add(totMFD);
				} else {
					offMFDs.add(null);
					totMFDs.add(null);
				}
				debug(solIndex, "DONE calculating region " + i);
			}
			debug(solIndex, "archiving");
			synchronized (this) {
				weights.add(wt);
				for (int i = 0; i < regions.size(); i++) {
					solMFDs.get(i).add(onMFDs.get(i));
					if (offMFDs.get(i) != null)
						solOffMFDs.get(i).add(offMFDs.get(i));
					if (totMFDs.get(i) != null)
						solTotalMFDs.get(i).add(totMFDs.get(i));
				}
			}
			debug(solIndex, "DONE archiving");
		}

		@Override
		protected void doFinalizePlot() {
			UCERF2_MFD_ConstraintFetcher ucerf2Fetch = null;

			System.out.println("Finalizing MFD plot for "
					+ solMFDs.get(0).size() + " branches!");

			specs = Lists.newArrayList();
			for (int i = 0; i < regions.size(); i++) {
				Region region = regions.get(i);

				XY_DataSetList solMFDsForRegion = solMFDs.get(i);
				XY_DataSetList solOffMFDsForRegion = solOffMFDs.get(i);
				XY_DataSetList totalMFDsForRegion = solTotalMFDs.get(i);

				if (ucerf2Fetch == null)
					ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher(region);
				else
					ucerf2Fetch.setRegion(region);

				DiscretizedFunc ucerf2TotalMFD = ucerf2Fetch.getTotalMFD();
				DiscretizedFunc ucerf2OffMFD = ucerf2Fetch
						.getBackgroundSeisMFD();

				ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
				ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();

				funcs.add(ucerf2TotalMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f,
						BROWN));
				funcs.add(ucerf2OffMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f,
						Color.MAGENTA));

				if (!solOffMFDsForRegion.isEmpty()) {
					// now add target GRs
					funcs.add(InversionTargetMFDs
							.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_9p6
									.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID,
							1f, Color.BLACK));
					funcs.add(InversionTargetMFDs
							.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_7p9
									.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID,
							2f, Color.BLACK));
					funcs.add(InversionTargetMFDs
							.getTotalTargetGR_upToM9(TotalMag5Rate.RATE_6p5
									.getRateMag5()));
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID,
							1f, Color.BLACK));

					funcs.addAll(getFractiles(solOffMFDsForRegion, weights,
							"Solution Off Fault MFDs", fractiles));
					chars.addAll(getFractileChars(Color.GRAY, fractiles.length));
				}

				funcs.addAll(getFractiles(solMFDsForRegion, weights,
						"Solution On Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.BLUE, fractiles.length));

				if (!totalMFDsForRegion.isEmpty()) {
					funcs.addAll(getFractiles(totalMFDsForRegion, weights,
							"Solution Total MFDs", fractiles));
					chars.addAll(getFractileChars(Color.RED, fractiles.length));
				}

				String title = region.getName();
				if (title == null || title.isEmpty())
					title = "Unnamed Region";

				String xAxisLabel = "Magnitude";
				String yAxisLabel = "Incremental Rate (per yr)";

				PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel,
						yAxisLabel);
				specs.add(spec);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				RegionalMFDPlot o = (RegionalMFDPlot) otherCalc;
				weights.addAll(o.weights);
				for (int r = 0; r < regions.size(); r++) {
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
			BranchWeightProvider weightProvider, List<Region> regions) {
		ERFBasedRegionalMFDPlot plot = new ERFBasedRegionalMFDPlot(
				weightProvider, regions,
				ERFBasedRegionalMFDPlot.getDefaultFractiles());

		plot.buildPlot(fetch);

		return plot.specs;
	}

	public static void writeERFBasedRegionalMFDPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, List<Region> regions,
			File dir, String prefix) throws IOException {
		List<PlotSpec> specs = writeERFBasedRegionalMFDPlotSpecs(fetch,
				weightProvider, regions);

		writeERFBasedRegionalMFDPlots(specs, regions, dir, prefix);
	}

	public static void writeERFBasedRegionalMFDPlots(List<PlotSpec> specs,
			List<Region> regions, File dir, String prefix) throws IOException {
		
		File subDir = new File(dir, "erf_mfd_plots");
		if (!subDir.exists())
			subDir.mkdir();

		int unnamedRegionCnt = 0;

		for (int i = 0; i < regions.size(); i++) {
			PlotSpec spec = specs.get(i);
			Region region = regions.get(i);

			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			CommandLineInversionRunner.setFontSizes(gp);
			gp.setYLog(true);
//			gp.setUserBounds(5d, 9d, 1e-6, 1e0);
			gp.setUserBounds(5d, 9d, 1e-5, 1e1);

			gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
					spec.getFuncs(), spec.getChars(), true, spec.getTitle());

			String fname = prefix + "_MFD_ERF";
			if (region.getName() != null && !region.getName().isEmpty())
				fname += "_" + region.getName().replaceAll("\\W+", "_");
			else
				fname += "_UNNAMED_REGION_" + (++unnamedRegionCnt);

			File file = new File(subDir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath() + ".pdf");
			gp.saveAsPNG(file.getAbsolutePath() + ".png");
			gp.saveAsTXT(file.getAbsolutePath() + ".txt");
			File smallDir = new File(dir, "small_MFD_plots");
			if (!smallDir.exists())
				smallDir.mkdir();
			file = new File(smallDir, fname + "_small");
			gp.getCartPanel().setSize(500, 400);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
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
		
		private static final boolean infer_off_fault = false;

		private transient BranchWeightProvider weightProvider;
		private List<Region> regions;
		private List<Double> weights;
		private double[] ucerf2Weights;

		// none (except min/mean/max which are always included)
		private double[] fractiles;

		// these are organized as (region, solution)
		private List<XY_DataSetList> solMFDs;
		private List<XY_DataSetList> solOnMFDs;
		private List<XY_DataSetList> solOffMFDs;
		private List<DiscretizedFunc[]> ucerf2MFDs;
		private List<DiscretizedFunc[]> ucerf2OnMFDs;
		private List<DiscretizedFunc[]> ucerf2OffMFDs;

		private transient Deque<UCERF2_TimeIndependentEpistemicList> ucerf2_erf_lists = new ArrayDeque<UCERF2_TimeIndependentEpistemicList>();
		// private transient UCERF2_TimeIndependentEpistemicList
		// ucerf2_erf_list;

		private static final double minX = 5.05d;
		private static final double maxX = 9.05d;
		private static final double delta = 0.1d;
		private static final int num = (int) ((maxX - minX) / delta + 1);

		private List<PlotSpec> specs;

		private int numUCEF2_ERFs;

		private transient Map<FaultModels, RupInRegionCache> rupInRegionsCaches = Maps
				.newHashMap();
		private transient Map<FaultModels, Map<String, Integer>> rupCountsMap = Maps
				.newHashMap();

		private static double[] getDefaultFractiles() {
//			double[] ret = { 0.5 };
			double[] ret = {};
			return ret;
		}

		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultRegions());
		}

		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider,
				List<Region> regions) {
			this(weightProvider, regions, getDefaultFractiles());
		}

		public ERFBasedRegionalMFDPlot(BranchWeightProvider weightProvider,
				List<Region> regions, double[] fractiles) {
			this.weightProvider = weightProvider;
			this.regions = regions;
			this.fractiles = fractiles;

			UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = checkOutUCERF2_ERF();
			numUCEF2_ERFs = ucerf2_erf_list.getNumERFs();
			returnUCERF2_ERF(ucerf2_erf_list);
			ucerf2_erf_list = null;

			solMFDs = Lists.newArrayList();
			solOnMFDs = Lists.newArrayList();
			solOffMFDs = Lists.newArrayList();
			ucerf2MFDs = Lists.newArrayList();
			ucerf2OnMFDs = Lists.newArrayList();
			ucerf2OffMFDs = Lists.newArrayList();
			weights = Lists.newArrayList();
			ucerf2Weights = new double[numUCEF2_ERFs];

			for (int i = 0; i < regions.size(); i++) {
				solMFDs.add(new XY_DataSetList());
				solOnMFDs.add(new XY_DataSetList());
				solOffMFDs.add(new XY_DataSetList());
				ucerf2MFDs.add(new DiscretizedFunc[numUCEF2_ERFs]);
				ucerf2OnMFDs.add(new DiscretizedFunc[numUCEF2_ERFs]);
				ucerf2OffMFDs.add(new DiscretizedFunc[numUCEF2_ERFs]);
			}
		}

		private synchronized UCERF2_TimeIndependentEpistemicList checkOutUCERF2_ERF() {
			if (ucerf2_erf_lists.isEmpty()) {
				UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = new UCERF2_TimeIndependentEpistemicList();
				ucerf2_erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
						UCERF2.FULL_DDW_FLOATER);
				ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_NAME,
						UCERF2.BACK_SEIS_INCLUDE);
				ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME,
						UCERF2.BACK_SEIS_RUP_POINT);
				ucerf2_erf_list.getTimeSpan().setDuration(1d);
				ucerf2_erf_list.updateForecast();
				return ucerf2_erf_list;
			}
			return ucerf2_erf_lists.pop();
		}

		private synchronized void returnUCERF2_ERF(
				UCERF2_TimeIndependentEpistemicList erf) {
			ucerf2_erf_lists.push(erf);
		}

		private void calcUCERF2MFDs(int erfIndex) {
			UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = checkOutUCERF2_ERF();
			ERF erf = ucerf2_erf_list.getERF(erfIndex);
			System.out.println("Calculating UCERF2 MFDs for branch "
					+ erfIndex + ", "+regions.size()+" regions");
			for (int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				Region region = regions.get(regionIndex);
				SummedMagFreqDist mfdPart = ERF_Calculator
						.getParticipationMagFreqDistInRegion(erf, region, minX,
								num, delta, true);
				ucerf2MFDs.get(regionIndex)[erfIndex] = mfdPart
						.getCumRateDistWithOffset();
				ucerf2Weights[erfIndex] = ucerf2_erf_list
						.getERF_RelativeWeight(erfIndex);
			}
			System.out.println("Calculating UCERF2 On Fault MFDs for branch "
					+ erfIndex + ", "+regions.size()+" regions");
			// on fault
			ucerf2_erf_list.getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_EXCLUDE);
			erf = ucerf2_erf_list.getERF(erfIndex);
			for (int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				Region region = regions.get(regionIndex);
				SummedMagFreqDist mfdPart = ERF_Calculator
						.getParticipationMagFreqDistInRegion(erf, region, minX,
								num, delta, true);
				ucerf2OnMFDs.get(regionIndex)[erfIndex] = mfdPart
						.getCumRateDistWithOffset();
			}
			System.out.println("Calculating UCERF2 Off Fault MFDs for branch "
					+ erfIndex + ", "+regions.size()+" regions");
			// off fault
			ucerf2_erf_list.getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_ONLY);
			erf = ucerf2_erf_list.getERF(erfIndex);
			for (int regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
				Region region = regions.get(regionIndex);
				SummedMagFreqDist mfdPart = ERF_Calculator
						.getParticipationMagFreqDistInRegion(erf, region, minX,
								num, delta, true);
				ucerf2OffMFDs.get(regionIndex)[erfIndex] = mfdPart
						.getCumRateDistWithOffset();
			}
			ucerf2_erf_list.getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_INCLUDE);
			
			returnUCERF2_ERF(ucerf2_erf_list);
		}

		private void checkCalcAllUCERF2MFDs() {
			for (int erfIndex = 0; erfIndex < numUCEF2_ERFs; erfIndex++) {
				if (ucerf2MFDs.get(0)[erfIndex] == null)
					calcUCERF2MFDs(erfIndex);
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			throw new IllegalStateException("Should not be called, ERF plot!");
		}
		
		private void checkRupCount(ERF erf, IncludeBackgroundOption back, Map<String, Integer> countsMap) {
			int count = 0;
			for (int i=0; i<erf.getNumSources(); i++)
				count += erf.getNumRuptures(i);
			Integer prevCount = countsMap.get(back.name());
			if (prevCount == null)
				countsMap.put(back.name(), count);
//			else
//				Preconditions.checkState(count == prevCount.intValue(), "Uh oh, rup counts don't match! "+count+" != "+prevCount);
		}

		@Override
		protected void processERF(LogicTreeBranch branch,
				UCERF3_FaultSysSol_ERF erf, int solIndex) {
			debug(solIndex, "checking UCERF2");
			// do UCERF2 if applicable
			if (solIndex < numUCEF2_ERFs)
				calcUCERF2MFDs(solIndex);
			debug(solIndex, " done UCERF2");

			FaultModels fm = branch.getValue(FaultModels.class);

			RupInRegionCache rupsCache = rupInRegionsCaches.get(fm);
			if (rupsCache == null) {
				synchronized (this) {
					if (!rupInRegionsCaches.containsKey(fm)) {
						rupInRegionsCaches.put(fm, new RupInRegionsCache());
						rupCountsMap.put(fm, new HashMap<String, Integer>());
					}
				}
				rupsCache = rupInRegionsCaches.get(fm);
			}

			debug(solIndex, "done cache");

			List<DiscretizedFunc> mfds = Lists.newArrayList();
			List<DiscretizedFunc> offMFDs = Lists.newArrayList();
			List<DiscretizedFunc> onMFDs = Lists.newArrayList();
			
			Map<String, Integer> countsMap = rupCountsMap.get(fm);
			
			checkRupCount(erf, IncludeBackgroundOption.INCLUDE, countsMap);

			for (int r = 0; r < regions.size(); r++) {
				Region region = regions.get(r);

				Stopwatch watch = new Stopwatch();
				watch.start();
				debug(solIndex, "calculating region (COMBINED) " + r);
				// System.out.println("Calculating branch "+solIndex+" region "+r);
				SummedMagFreqDist ucerf3_Part = ERF_Calculator
						.getParticipationMagFreqDistInRegion(erf, region, minX,
								num, delta, true, rupsCache);
				watch.stop();
				debug(solIndex,
						"done region (COMBINED) " + r + " ("
								+ (watch.elapsedMillis() / 1000d) + " s)");
				// System.out.println("Took "+(watch.elapsedMillis()/1000d)+" secst for branch "
				// +solIndex+" region "+r+" ("+region.getName()+")");

				mfds.add(ucerf3_Part.getCumRateDistWithOffset());
			}

			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.EXCLUDE);
			erf.updateForecast();
			checkRupCount(erf, IncludeBackgroundOption.EXCLUDE, countsMap);
			
			for (int r = 0; r < regions.size(); r++) {
				Region region = regions.get(r);

				Stopwatch watch = new Stopwatch();
				watch.start();
				debug(solIndex, "calculating region (ON FAULT) " + r);
				// System.out.println("Calculating branch "+solIndex+" region "+r);
				SummedMagFreqDist ucerf3_Part = ERF_Calculator
						.getParticipationMagFreqDistInRegion(erf, region, minX,
								num, delta, true, rupsCache);
				watch.stop();
				debug(solIndex,
						"done region (ON FAULT) " + r + " ("
								+ (watch.elapsedMillis() / 1000d) + " s)");
				// System.out.println("Took "+(watch.elapsedMillis()/1000d)+" secst for branch "
				// +solIndex+" region "+r+" ("+region.getName()+")");

				onMFDs.add(ucerf3_Part.getCumRateDistWithOffset());
			}
			
			if (infer_off_fault) {
				for (int r = 0; r < regions.size(); r++) {
					DiscretizedFunc totMFD = mfds.get(r);
					DiscretizedFunc onMFD = mfds.get(r);
					DiscretizedFunc offMFD = new EvenlyDiscretizedFunc(totMFD.getMinX(), totMFD.getMaxX(), totMFD.getNum());
					for (int i=0; i<totMFD.getNum(); i++)
						offMFD.set(i, totMFD.getY(i) - onMFD.getY(i));
					offMFDs.add(offMFD);
				}
			} else {
				erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
				erf.updateForecast();
				checkRupCount(erf, IncludeBackgroundOption.ONLY, countsMap);
				
				for (int r = 0; r < regions.size(); r++) {
					Region region = regions.get(r);

					Stopwatch watch = new Stopwatch();
					watch.start();
					debug(solIndex, "calculating region (OFF FAULT) " + r);
					// System.out.println("Calculating branch "+solIndex+" region "+r);
					SummedMagFreqDist ucerf3_Part = ERF_Calculator
							.getParticipationMagFreqDistInRegion(erf, region, minX,
									num, delta, true, rupsCache);
					watch.stop();
					debug(solIndex,
							"done region (OFF FAULT) " + r + " ("
									+ (watch.elapsedMillis() / 1000d) + " s)");
					// System.out.println("Took "+(watch.elapsedMillis()/1000d)+" secst for branch "
					// +solIndex+" region "+r+" ("+region.getName()+")");

					offMFDs.add(ucerf3_Part.getCumRateDistWithOffset());
				}
			}
			
			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
			erf.updateForecast();

			debug(solIndex, " archiving");
			synchronized (this) {
				weights.add(weightProvider.getWeight(branch));
				for (int r = 0; r < regions.size(); r++) {
					solMFDs.get(r).add(mfds.get(r));
					solOnMFDs.get(r).add(onMFDs.get(r));
					solOffMFDs.get(r).add(offMFDs.get(r));
				}
			}
			debug(solIndex, " archiving done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ERFBasedRegionalMFDPlot o = (ERFBasedRegionalMFDPlot) otherCalc;
				for (int r = 0; r < regions.size(); r++) {
					solMFDs.get(r).addAll(o.solMFDs.get(r));
					solOnMFDs.get(r).addAll(o.solOnMFDs.get(r));
					solOffMFDs.get(r).addAll(o.solOffMFDs.get(r));
				}
				weights.addAll(o.weights);

				for (int e = 0; e < numUCEF2_ERFs; e++) {
					if (o.ucerf2MFDs.get(0)[e] != null) {
						for (int r = 0; r < regions.size(); r++) {
							ucerf2MFDs.get(r)[e] = o.ucerf2MFDs.get(r)[e];
							ucerf2OnMFDs.get(r)[e] = o.ucerf2OnMFDs.get(r)[e];
							ucerf2OffMFDs.get(r)[e] = o.ucerf2OffMFDs.get(r)[e];
						}
						ucerf2Weights[e] = o.ucerf2Weights[e];
					}
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			specs = Lists.newArrayList();

			checkCalcAllUCERF2MFDs();

//			MeanUCERF2 erf = new MeanUCERF2();
//			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME,
//					UCERF2.PROB_MODEL_POISSON);
//			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
//					UCERF2.FULL_DDW_FLOATER);
//			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME,
//					UCERF2.BACK_SEIS_RUP_POINT);
//			erf.getTimeSpan().setDuration(1d);
//			erf.updateForecast();

			for (int r = 0; r < regions.size(); r++) {
				Region region = regions.get(r);

				XY_DataSetList ucerf2Funcs = new XY_DataSetList();
				XY_DataSetList ucerf2OnFuncs = new XY_DataSetList();
				XY_DataSetList ucerf2OffFuncs = new XY_DataSetList();
				ArrayList<Double> ucerf2Weights = new ArrayList<Double>();
				for (int e = 0; e < ucerf2MFDs.get(r).length; e++) {
					DiscretizedFunc mfd = ucerf2MFDs.get(r)[e];
					if (mfd != null) {
						ucerf2Funcs.add(ucerf2MFDs.get(r)[e]);
						ucerf2OnFuncs.add(ucerf2OnMFDs.get(r)[e]);
						ucerf2OffFuncs.add(ucerf2OffMFDs.get(r)[e]);
						ucerf2Weights.add(this.ucerf2Weights[e]);
					}
				}

				ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
				ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
				
				funcs.addAll(getFractiles(ucerf2OnFuncs, ucerf2Weights,
						"UCERF2 Epistemic List On Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.GREEN, fractiles.length));
				
				funcs.addAll(getFractiles(ucerf2OffFuncs, ucerf2Weights,
						"UCERF2 Epistemic List Off Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.MAGENTA, fractiles.length));
				
				funcs.addAll(getFractiles(solOnMFDs.get(r), weights,
						"UCERF3 On Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.ORANGE, fractiles.length));
				
				funcs.addAll(getFractiles(solOffMFDs.get(r), weights,
						"UCERF3 Off Fault MFDs", fractiles));
				chars.addAll(getFractileChars(Color.GRAY, fractiles.length));

				funcs.addAll(getFractiles(ucerf2Funcs, ucerf2Weights,
						"UCERF2 Epistemic List", fractiles));
				chars.addAll(getFractileChars(Color.RED, fractiles.length));

//				SummedMagFreqDist meanU2Part = ERF_Calculator
//						.getParticipationMagFreqDistInRegion(erf, region, minX,
//								num, delta, true);
//				meanU2Part.setName("MFD for MeanUCERF2");
//				meanU2Part.setInfo(" ");
//				funcs.add(meanU2Part.getCumRateDistWithOffset());
//				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f,
//						Color.BLUE));

				funcs.addAll(getFractiles(solMFDs.get(r), weights,
						"UCERF3 MFDs", fractiles));
				chars.addAll(getFractileChars(Color.BLUE, fractiles.length));

				String title = region.getName();
				if (title == null || title.isEmpty())
					title = "Unnamed Region";

				String xAxisLabel = "Magnitude";
				String yAxisLabel = "Cumulative Rate (per yr)";

				PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel,
						yAxisLabel);
				specs.add(spec);
			}
		}

		@Override
		protected boolean usesERFs() {
			return true;
		}

		protected List<PlotSpec> getSpecs() {
			return specs;
		}

	}
	
	private static int getInversionIndex(ProbEqkSource source) {
		String srcName = source.getName();
//		System.out.println(srcName);
		return Integer.parseInt(srcName.substring(srcName.indexOf("#")+1, srcName.indexOf(";")));
	}
	
	private static class RupInRegionsCache implements RupInRegionCache {
		private ConcurrentMap<Region, ConcurrentMap<Integer, Boolean>> map = Maps
				.newConcurrentMap();

		@Override
		public boolean isRupInRegion(ProbEqkSource source, EqkRupture rup,
				int srcIndex, int rupIndex, Region region) {
			RuptureSurface surf = rup.getRuptureSurface();
			if (surf instanceof CompoundGriddedSurface) {
				int invIndex = getInversionIndex(source);
				ConcurrentMap<Integer, Boolean> regMap = map
						.get(region);
				if (regMap == null) {
					regMap = Maps.newConcurrentMap();
					map.putIfAbsent(region, regMap);
					// in case another thread put it in
					// first
					regMap = map.get(region);
				}
				Boolean inside = regMap.get(invIndex);
				if (inside == null) {
					inside = false;
					for (Location loc : surf
							.getEvenlyDiscritizedListOfLocsOnSurface())
						if (region.contains(loc)) {
							inside = true;
							break;
						}
					regMap.putIfAbsent(invIndex, inside);
				}
				return inside;
			}
			for (Location loc : surf
					.getEvenlyDiscritizedListOfLocsOnSurface())
				if (region.contains(loc))
					return true;
			return false;
		}
	}

	public static void writePaleoFaultPlots(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir) throws IOException {
		PaleoFaultPlot plot = new PaleoFaultPlot(weightProvider);
		plot.buildPlot(fetch);

		writePaleoFaultPlots(plot.plotsMap, dir);
	}

	public static void writePaleoFaultPlots(
			Map<FaultModels, Map<String, PlotSpec[]>> plotsMap, File dir)
			throws IOException {
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
		private Map<FaultModels, Map<String, List<Integer>>> namedFaultsMaps = Maps
				.newHashMap();
		private Map<FaultModels, List<PaleoRateConstraint>> paleoConstraintMaps = Maps
				.newHashMap();
		private Map<FaultModels, List<AveSlipConstraint>> slipConstraintMaps = Maps
				.newHashMap();
		private Map<FaultModels, Map<Integer, List<FaultSectionPrefData>>> allParentsMaps = Maps
				.newHashMap();
		private Map<FaultModels, List<FaultSectionPrefData>> fsdsMap = Maps
				.newHashMap();

		// results
		private Map<FaultModels, List<DataForPaleoFaultPlots>> datasMap = Maps
				.newHashMap();
		private Map<FaultModels, List<List<Double>>> slipRatesMap = Maps
				.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		// plots
		private Map<FaultModels, Map<String, PlotSpec[]>> plotsMap = Maps
				.newHashMap();

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
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			FaultModels fm = rupSet.getFaultModel();

			try {
				debug(solIndex, "Preparing...");
				List<PaleoRateConstraint> paleoRateConstraints = paleoConstraintMaps
						.get(fm);
				if (paleoRateConstraints == null) {
					synchronized (this) {
						paleoRateConstraints = paleoConstraintMaps.get(fm);
						if (paleoRateConstraints == null) {
							System.out
									.println("I'm in the synchronized block! "
											+ fm);
							// do a bunch of FM specific stuff
							paleoRateConstraints = CommandLineInversionRunner.getPaleoConstraints(
									fm, rupSet);
							slipConstraintMaps.put(fm,
									AveSlipConstraint.load(rupSet.getFaultSectionDataList()));
							allParentsMaps.put(fm, PaleoFitPlotter.getAllParentsMap(
									rupSet.getFaultSectionDataList()));
							namedFaultsMaps.put(fm, fm.getNamedFaultsMapAlt());
							fsdsMap.put(fm, rupSet.getFaultSectionDataList());
							paleoConstraintMaps.put(fm, paleoRateConstraints);
						}
					}
				}

				List<Double> slipsForConstraints = Lists.newArrayList();
				paleoRateConstraints = Lists.newArrayList(paleoRateConstraints);
				List<AveSlipConstraint> aveSlipConstraints = slipConstraintMaps
						.get(fm);
				for (AveSlipConstraint aveSlip : aveSlipConstraints) {
					double slip = rupSet.getSlipRateForSection(aveSlip.getSubSectionIndex());
					paleoRateConstraints.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(
									aveSlip, aveSlip.getSubSectionIndex(), slip));
					slipsForConstraints.add(slip);
				}

				Map<String, List<Integer>> namedFaultsMap = namedFaultsMaps
						.get(fm);

				Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap = PaleoFitPlotter
						.getNamedFaultConstraintsMap(paleoRateConstraints,
								rupSet.getFaultSectionDataList(), namedFaultsMap);

				Map<Integer, List<FaultSectionPrefData>> allParentsMap = allParentsMaps
						.get(fm);

				double weight = weightProvider.getWeight(branch);

				debug(solIndex, "Building...");
				DataForPaleoFaultPlots data = DataForPaleoFaultPlots.build(sol,
						namedFaultsMap, namedFaultConstraintsMap,
						allParentsMap, paleoProbModel, weight);

				debug(solIndex, "Archiving results...");

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
						for (int i = 0; i < slipsForConstraints.size(); i++)
							slipRates.add(new ArrayList<Double>());
						slipRatesMap.put(fm, slipRates);
					}
					Preconditions.checkState(
							slipRates.size() == slipsForConstraints.size(),
							"Slip rate sizes inconsistent!");
					for (int i = 0; i < slipsForConstraints.size(); i++)
						slipRates.get(i).add(slipsForConstraints.get(i));

					List<Double> weightsList = weightsMap.get(fm);
					if (weightsList == null) {
						weightsList = Lists.newArrayList();
						weightsMap.put(fm, weightsList);
					}
					weightsList.add(weight);

					debug(solIndex,
							"Done calculating data for " + fm.getShortName()
									+ " #" + (weightsList.size()));
				}
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}

		@Override
		protected void doFinalizePlot() {
			for (FaultModels fm : datasMap.keySet()) {
				// build compound ave slips
				List<AveSlipConstraint> aveSlips = slipConstraintMaps.get(fm);

				List<List<Double>> slipVals = slipRatesMap.get(fm);

				List<PaleoRateConstraint> paleoRateConstraints = paleoConstraintMaps
						.get(fm);

				double[] weights = Doubles.toArray(weightsMap.get(fm));

				for (int i = 0; i < aveSlips.size(); i++) {
					List<Double> slipList = slipVals.get(i);
					double[] slipArray = Doubles.toArray(slipList);
					Preconditions.checkState(
							slipArray.length == weights.length,
							slipArray.length + " != " + weights.length);

					AveSlipConstraint constr = aveSlips.get(i);

					paleoRateConstraints
							.add(new PaleoFitPlotter.AveSlipFakePaleoConstraint(
									constr, constr.getSubSectionIndex(),
									slipArray, weights));
				}

				Map<String, List<Integer>> namedFaultsMap = namedFaultsMaps
						.get(fm);
				Map<String, List<PaleoRateConstraint>> namedFaultConstraintsMap = PaleoFitPlotter
						.getNamedFaultConstraintsMap(paleoRateConstraints,
								fsdsMap.get(fm), namedFaultsMap);

				List<DataForPaleoFaultPlots> datas = datasMap.get(fm);

				Map<Integer, List<FaultSectionPrefData>> allParentsMap = allParentsMaps
						.get(fm);

				Map<String, PlotSpec[]> specsMap = PaleoFitPlotter
						.getFaultSpecificPaleoPlotSpecs(namedFaultsMap,
								namedFaultConstraintsMap, datas, allParentsMap);

				plotsMap.put(fm, specsMap);
			}
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				PaleoFaultPlot o = (PaleoFaultPlot) otherCalc;
				for (FaultModels fm : o.allParentsMaps.keySet()) {
					if (!allParentsMaps.containsKey(fm)) {
						// add the fault model specific values
						namedFaultsMaps.put(fm, o.namedFaultsMaps.get(fm));
						paleoConstraintMaps.put(fm,
								o.paleoConstraintMaps.get(fm));
						slipConstraintMaps
								.put(fm, o.slipConstraintMaps.get(fm));
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
						List<AveSlipConstraint> slipConstraints = slipConstraintMaps
								.get(fm);
						slipRatesList = Lists.newArrayList();
						for (int i = 0; i < slipConstraints.size(); i++)
							slipRatesList.add(new ArrayList<Double>());
						slipRatesMap.put(fm, slipRatesList);
					}
					for (int i = 0; i < slipRatesList.size(); i++)
						slipRatesList.get(i).addAll(
								o.slipRatesMap.get(fm).get(i));

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
			BranchWeightProvider weightProvider, File dir) throws IOException {
		PaleoSiteCorrelationPlot plot = new PaleoSiteCorrelationPlot(
				weightProvider);
		plot.buildPlot(fetch);

		writePaleoCorrelationPlots(plot.plotsMap, dir);
	}

	public static void writePaleoCorrelationPlots(
			Map<String, PlotSpec> plotsMap, File dir) throws IOException {
		System.out.println("Making paleo corr plots for "
				+ plotsMap.keySet().size() + " Faults");

		if (!dir.exists())
			dir.mkdir();

		CommandLineInversionRunner.writePaleoCorrelationPlots(dir, plotsMap);
	}

	public static class PaleoSiteCorrelationPlot extends CompoundFSSPlots {

		private transient PaleoProbabilityModel paleoProbModel;
		private transient BranchWeightProvider weightProvider;

		private Map<FaultModels, Map<String, List<PaleoSiteCorrelationData>>> corrsListsMap = Maps
				.newHashMap();

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
				InversionFaultSystemSolution sol, int solIndex) {
			FaultModels fm = sol.getRupSet().getFaultModel();

			try {
				debug(solIndex, "Preparing...");
				Map<String, List<PaleoSiteCorrelationData>> corrs = corrsListsMap
						.get(fm);
				if (corrs == null) {
					synchronized (fm) {
						corrs = corrsListsMap.get(fm);
						if (corrs == null) {
							debug(solIndex, "I'm in the synchronized block! "
									+ fm);
							corrs = Maps.newHashMap();

							Map<String, Table<String, String, PaleoSiteCorrelationData>> table = PaleoSiteCorrelationData
									.loadPaleoCorrelationData(sol);

							for (String faultName : table.keySet()) {
								List<PaleoSiteCorrelationData> corrsToPlot = PaleoSiteCorrelationData
										.getCorrelataionsToPlot(table
												.get(faultName));
								corrs.put(faultName, corrsToPlot);
							}
							corrsListsMap.put(fm, corrs);
						}
					}
				}

				double weight = weightProvider.getWeight(branch);

				Map<String, double[]> myData = Maps.newHashMap();

				debug(solIndex, "Building...");
				for (String faultName : corrs.keySet()) {
					List<PaleoSiteCorrelationData> corrsToPlot = corrs
							.get(faultName);

					double[] vals = new double[corrsToPlot.size()];
					for (int i = 0; i < vals.length; i++) {
						PaleoSiteCorrelationData corr = corrsToPlot.get(i);
						vals[i] = PaleoSiteCorrelationData.getRateCorrelated(
								paleoProbModel, sol, corr.getSite1SubSect(),
								corr.getSite2SubSect());
					}

					myData.put(faultName, vals);
				}

				debug(solIndex, "Archiving results...");

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
				PaleoSiteCorrelationPlot o = (PaleoSiteCorrelationPlot) otherCalc;

				data.addAll(o.data);
				weights.addAll(o.weights);

				for (FaultModels fm : o.corrsListsMap.keySet()) {
					if (!corrsListsMap.containsKey(fm))
						corrsListsMap.put(fm, o.corrsListsMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			Map<String, List<PaleoSiteCorrelationData>> allCorrsMap = Maps
					.newHashMap();
			for (FaultModels fm : corrsListsMap.keySet()) {
				Map<String, List<PaleoSiteCorrelationData>> corrsForFM = corrsListsMap
						.get(fm);
				for (String faultName : corrsForFM.keySet()) {
					if (!allCorrsMap.containsKey(faultName))
						allCorrsMap.put(faultName, corrsForFM.get(faultName));
				}
			}

			for (String faultName : allCorrsMap.keySet()) {
				List<double[]> solValsForFault = Lists.newArrayList();
				List<Double> weightsForFault = Lists.newArrayList();

				for (int s = 0; s < data.size(); s++) {
					double[] solData = data.get(s).get(faultName);
					if (solData != null) {
						solValsForFault.add(solData);
						weightsForFault.add(weights.get(s));
					}
				}

				List<PaleoSiteCorrelationData> corrs = allCorrsMap
						.get(faultName);

				List<double[]> solValues = Lists.newArrayList();
				double[] weights = Doubles.toArray(weightsForFault);

				for (int i = 0; i < corrs.size(); i++) {
					double[] vals = new double[solValsForFault.size()];
					for (int s = 0; s < solValsForFault.size(); s++)
						vals[s] = solValsForFault.get(s)[i];
					double min = StatUtils.min(vals);
					double max = StatUtils.max(vals);
					double mean = FaultSystemSolutionFetcher.calcScaledAverage(
							vals, weights);

					double[] ret = { min, max, mean };
					System.out.println("Vals for " + faultName + " CORR " + i
							+ ": " + min + "," + max + "," + mean + " ("
							+ vals.length + " sols)");
					solValues.add(ret);
				}

				PlotSpec spec = PaleoSiteCorrelationData
						.getCorrelationPlotSpec(faultName, corrs, solValues,
								paleoProbModel);

				plotsMap.put(faultName, spec);
			}
		}

		public Map<String, PlotSpec> getPlotsMap() {
			return plotsMap;
		}
	}

	public static void writeParentSectionMFDPlots(
			FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir) throws IOException {
		ParentSectMFDsPlot plot = new ParentSectMFDsPlot(weightProvider);
		plot.buildPlot(fetch);

		writeParentSectionMFDPlots(plot, dir);
	}

	public static void writeParentSectionMFDPlots(ParentSectMFDsPlot plot,
			File dir) throws IOException {
		System.out.println("Making parent sect MFD plots for "
				+ plot.plotNuclIncrMFDs.keySet().size() + " Faults");

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
		for (double x = ParentSectMFDsPlot.minX; x <= ParentSectMFDsPlot.maxX; x += ParentSectMFDsPlot.delta)
			xVals.add(x);
		header.add("Fault ID");
		header.add("Fault Name");
		for (double x : xVals) {
			header.add("M" + (float) x);
			header.add("(UCERF2)");
		}
		nucleationCSV.addLine(header);
		nucleationMinCSV.addLine(header);
		nucleationMaxCSV.addLine(header);
		participationCSV.addLine(header);
		participationMinCSV.addLine(header);
		participationMaxCSV.addLine(header);

		for (Integer parentID : plot.plotNuclIncrMFDs.keySet()) {
			ArrayList<IncrementalMagFreqDist> ucerf2NuclMFDs = UCERF2_Section_MFDsCalc
					.getMeanMinAndMaxMFD(parentID, false, false);
			ArrayList<IncrementalMagFreqDist> ucerf2NuclCmlMFDs = UCERF2_Section_MFDsCalc
					.getMeanMinAndMaxMFD(parentID, false, true);
			ArrayList<IncrementalMagFreqDist> ucerf2PartMFDs = UCERF2_Section_MFDsCalc
					.getMeanMinAndMaxMFD(parentID, true, false);
			ArrayList<IncrementalMagFreqDist> ucerf2PartCmlMFDs = UCERF2_Section_MFDsCalc
					.getMeanMinAndMaxMFD(parentID, true, true);

			String name = plot.namesMap.get(parentID);

			List<IncrementalMagFreqDist> nuclMFDs = plot.plotNuclIncrMFDs
					.get(parentID);
			List<EvenlyDiscretizedFunc> nuclCmlMFDs = plot.plotNuclCmlMFDs
					.get(parentID);
			List<IncrementalMagFreqDist> partMFDs = plot.plotPartIncrMFDs
					.get(parentID);
			List<EvenlyDiscretizedFunc> partCmlMFDs = plot.plotPartCmlMFDs
					.get(parentID);

			for (int i = 0; i < nuclMFDs.size(); i++) {
				nuclMFDs.get(i).setInfo(" ");
				nuclCmlMFDs.get(i).setInfo(" ");
				partMFDs.get(i).setInfo(" ");
				partCmlMFDs.get(i).setInfo(" ");
			}

			EvenlyDiscretizedFunc nuclCmlMFD = nuclCmlMFDs.get(nuclCmlMFDs
					.size() - 3);
			nuclCmlMFD.setInfo(getCmlMFDInfo(nuclCmlMFD, false));
			EvenlyDiscretizedFunc partCmlMFD = partCmlMFDs.get(partCmlMFDs
					.size() - 3);
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

			nucleationCSV.addLine(getCSVLine(xVals, parentID, name, nuclCmlMFD,
					ucerf2NuclCmlMFD));
			nucleationMinCSV.addLine(getCSVLine(xVals, parentID, name,
					nuclCmlMFDs.get(nuclCmlMFDs.size() - 2),
					ucerf2NuclCmlMinMFD));
			nucleationMaxCSV.addLine(getCSVLine(xVals, parentID, name,
					nuclCmlMFDs.get(nuclCmlMFDs.size() - 1),
					ucerf2NuclCmlMaxMFD));
			participationCSV.addLine(getCSVLine(xVals, parentID, name,
					partCmlMFD, ucerf2PartCmlMFD));
			participationMinCSV.addLine(getCSVLine(xVals, parentID, name,
					partCmlMFDs.get(partCmlMFDs.size() - 2),
					ucerf2PartCmlMinMFD));
			participationMaxCSV.addLine(getCSVLine(xVals, parentID, name,
					partCmlMFDs.get(partCmlMFDs.size() - 1),
					ucerf2PartCmlMaxMFD));

			List<IncrementalMagFreqDist> subSeismoMFDs = plot.plotSubSeismoIncrMFDs
					.get(parentID);
			List<IncrementalMagFreqDist> subPlusSupraSeismoMFDs = plot.plotSubPlusSupraSeismoMFDs
					.get(parentID);
			List<EvenlyDiscretizedFunc> subPlusSupraSeismoCmlMFDs = plot.plotSubPlusSupraSeismoCmlMFDs
					.get(parentID);

			writeParentSectionMFDPlot(dir, nuclMFDs, nuclCmlMFDs,
					ucerf2NuclMFDs, ucerf2NuclCmlMFDs, subSeismoMFDs,
					subPlusSupraSeismoMFDs, subPlusSupraSeismoCmlMFDs,
					parentID, name, true);
			writeParentSectionMFDPlot(dir, partMFDs, partCmlMFDs,
					ucerf2PartMFDs, ucerf2PartCmlMFDs, null, null, null,
					parentID, name, false);
		}

		nucleationCSV.writeToFile(new File(dir,
				"cumulative_nucleation_mfd_comparisons.csv"));
		nucleationMinCSV.writeToFile(new File(dir,
				"cumulative_nucleation_min_mfd_comparisons.csv"));
		nucleationMaxCSV.writeToFile(new File(dir,
				"cumulative_nucleation_max_mfd_comparisons.csv"));
		participationCSV.writeToFile(new File(dir,
				"cumulative_participation_mfd_comparisons.csv"));
		participationMinCSV.writeToFile(new File(dir,
				"cumulative_participation_min_mfd_comparisons.csv"));
		participationMaxCSV.writeToFile(new File(dir,
				"cumulative_participation_max_mfd_comparisons.csv"));
	}

	private static List<String> getCSVLine(List<Double> xVals, int parentID,
			String name, EvenlyDiscretizedFunc cmlMFD,
			EvenlyDiscretizedFunc ucerf2CmlMFD) {
		List<String> line = Lists.newArrayList(parentID + "", name);

		for (int i = 0; i < xVals.size(); i++) {
			line.add(cmlMFD.getY(i) + "");
			double x = xVals.get(i);
			if (ucerf2CmlMFD != null && ucerf2CmlMFD.getMinX() <= x
					&& ucerf2CmlMFD.getMaxX() >= x) {
				line.add(ucerf2CmlMFD.getClosestY(x) + "");
			} else {
				line.add("");
			}
		}

		return line;
	}

	private static String getCmlMFDInfo(EvenlyDiscretizedFunc cmlMFD,
			boolean isAlreadyRI) {
		double totRate = cmlMFD.getMaxY();
		// double rate6p7 = cmlMFD.getClosestY(6.7d);
		double rate6p7 = cmlMFD.getInterpolatedY_inLogYDomain(6.7d);
		// for (int i=0; i<cmlMFD.getNum(); i++)
		// System.out.println("CML: "+i+": "+cmlMFD.getX(i)+","+cmlMFD.getY(i));
		String info;
		if (isAlreadyRI) {
			info = "\t\tTotal RI: " + (int) Math.round(totRate) + "\n";
			info += "\t\tRI M>=6.7: " + (int) Math.round(rate6p7);
		} else {
			info = "\t\tTotal Rate: " + (float) totRate + "\n";
			info += "\t\tRate M>=6.7: " + (float) rate6p7 + "\n";
			double totRI = 1d / totRate;
			double ri6p7 = 1d / rate6p7;
			info += "\t\tTotal RI: " + (int) Math.round(totRI) + "\n";
			info += "\t\tRI M>=6.7: " + (int) Math.round(ri6p7);
		}
		// System.out.println(info);

		return info;
	}

	private static void writeParentSectionMFDPlot(File dir,
			List<IncrementalMagFreqDist> mfds,
			List<EvenlyDiscretizedFunc> cmlMFDs,
			List<IncrementalMagFreqDist> ucerf2MFDs,
			List<IncrementalMagFreqDist> ucerf2CmlMFDs,
			List<IncrementalMagFreqDist> subSeismoMFDs,
			List<IncrementalMagFreqDist> subPlusSupraSeismoMFDs,
			List<EvenlyDiscretizedFunc> subPlusSupraSeismoCmlMFDs, int id,
			String name, boolean nucleation) throws IOException {
		CommandLineInversionRunner.writeParentSectMFDPlot(dir, mfds, cmlMFDs,
				false, ucerf2MFDs, ucerf2CmlMFDs, subSeismoMFDs,
				subPlusSupraSeismoMFDs, subPlusSupraSeismoCmlMFDs, id, name,
				nucleation);
	}

	public static class ParentSectMFDsPlot extends CompoundFSSPlots {

		private transient BranchWeightProvider weightProvider;

		// none (except min/mean/max which are always included)
		private double[] fractiles;

		private ConcurrentMap<FaultModels, HashSet<Integer>> parentMapsCache = Maps
				.newConcurrentMap();

		// these are organized as (region, solution)
		private Map<Integer, XY_DataSetList> nuclIncrMFDs = Maps.newHashMap();
		private Map<Integer, XY_DataSetList> nuclSubSeismoMFDs = Maps
				.newHashMap();
		private Map<Integer, XY_DataSetList> partIncrMFDs = Maps.newHashMap();

		private Map<Integer, List<Double>> weightsMap = Maps.newHashMap();
		private ConcurrentMap<Integer, String> namesMap = Maps
				.newConcurrentMap();

		private static final double minX = 5.05d;
		private static final double maxX = 9.05d;
		private static final double delta = 0.1d;
		private static final int num = (int) ((maxX - minX) / delta) + 1;

		private Map<Integer, List<IncrementalMagFreqDist>> plotNuclIncrMFDs = Maps
				.newHashMap();
		private Map<Integer, List<IncrementalMagFreqDist>> plotSubSeismoIncrMFDs = Maps
				.newHashMap();
		private Map<Integer, List<IncrementalMagFreqDist>> plotSubPlusSupraSeismoMFDs = Maps
				.newHashMap();
		private Map<Integer, List<EvenlyDiscretizedFunc>> plotSubPlusSupraSeismoCmlMFDs = Maps
				.newHashMap();
		private Map<Integer, List<IncrementalMagFreqDist>> plotPartIncrMFDs = Maps
				.newHashMap();
		private Map<Integer, List<EvenlyDiscretizedFunc>> plotNuclCmlMFDs = Maps
				.newHashMap();
		private Map<Integer, List<EvenlyDiscretizedFunc>> plotPartCmlMFDs = Maps
				.newHashMap();

		private static double[] getDefaultFractiles() {
			// double[] ret = { 0.5 };
			double[] ret = {};
			return ret;
		}

		public ParentSectMFDsPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultFractiles());
		}

		public ParentSectMFDsPlot(BranchWeightProvider weightProvider,
				double[] fractiles) {
			this.weightProvider = weightProvider;
			this.fractiles = fractiles;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			FaultModels fm = rupSet.getFaultModel();

			debug(solIndex, "cache fetching");
			HashSet<Integer> parentIDs = parentMapsCache.get(fm);
			if (parentIDs == null) {
				parentIDs = new HashSet<Integer>();
				for (int sectIndex = 0; sectIndex < rupSet.getNumSections(); sectIndex++) {
					FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
					Integer parentID = sect.getParentSectionId();
					if (!parentIDs.contains(parentID)) {
						parentIDs.add(parentID);
						namesMap.putIfAbsent(parentID,
								sect.getParentSectionName());
					}
				}
				parentMapsCache.putIfAbsent(fm, parentIDs);
			}

			double weight = weightProvider.getWeight(branch);

			debug(solIndex, "calculating");
			for (Integer parentID : parentIDs) {
				SummedMagFreqDist nuclMFD = sol
						.calcNucleationMFD_forParentSect(parentID, minX, maxX,
								num);
				SummedMagFreqDist nuclSubSeismoMFD = sol.getFinalSubSeismoOnFaultMFDForParent(parentID);
				IncrementalMagFreqDist partMFD = sol.calcParticipationMFD_forParentSect(parentID, minX,
								maxX, num);

				synchronized (this) {
					if (!nuclIncrMFDs.containsKey(parentID)) {
						nuclIncrMFDs.put(parentID, new XY_DataSetList());
						nuclSubSeismoMFDs.put(parentID, new XY_DataSetList());
						partIncrMFDs.put(parentID, new XY_DataSetList());
						weightsMap.put(parentID, new ArrayList<Double>());
					}
					nuclIncrMFDs.get(parentID).add(nuclMFD);
					nuclSubSeismoMFDs.get(parentID).add(nuclSubSeismoMFD);
					partIncrMFDs.get(parentID).add(partMFD);
					weightsMap.get(parentID).add(weight);
				}
			}
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ParentSectMFDsPlot o = (ParentSectMFDsPlot) otherCalc;

				for (Integer parentID : o.nuclIncrMFDs.keySet()) {
					if (!nuclIncrMFDs.containsKey(parentID)) {
						nuclIncrMFDs.put(parentID, new XY_DataSetList());
						nuclSubSeismoMFDs.put(parentID, new XY_DataSetList());
						partIncrMFDs.put(parentID, new XY_DataSetList());
						weightsMap.put(parentID, new ArrayList<Double>());
					}
					nuclIncrMFDs.get(parentID).addAll(
							o.nuclIncrMFDs.get(parentID));
					nuclSubSeismoMFDs.get(parentID).addAll(
							o.nuclSubSeismoMFDs.get(parentID));
					partIncrMFDs.get(parentID).addAll(
							o.partIncrMFDs.get(parentID));
					weightsMap.get(parentID).addAll(o.weightsMap.get(parentID));
					if (!namesMap.containsKey(parentID))
						namesMap.put(parentID, o.namesMap.get(parentID));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			for (Integer parentID : nuclIncrMFDs.keySet()) {
				plotNuclIncrMFDs.put(
						parentID,
						asIncr(getFractiles(nuclIncrMFDs.get(parentID),
								weightsMap.get(parentID),
								"Incremental Nucleation MFD", fractiles)));
				plotNuclCmlMFDs.put(
						parentID,
						asEvenly(getFractiles(
								asCml(nuclIncrMFDs.get(parentID)),
								weightsMap.get(parentID),
								"Cumulative Nucleation MFD", new double[0])));
				plotPartIncrMFDs.put(
						parentID,
						asIncr(getFractiles(partIncrMFDs.get(parentID),
								weightsMap.get(parentID),
								"Incremental Participation MFD", fractiles)));
				plotPartCmlMFDs
						.put(parentID,
								asEvenly(getFractiles(
										asCml(partIncrMFDs.get(parentID)),
										weightsMap.get(parentID),
										"Cumulative Participation MFD",
										new double[0])));
				plotSubSeismoIncrMFDs.put(
						parentID,
						asIncr(getFractiles(nuclSubSeismoMFDs.get(parentID),
								weightsMap.get(parentID),
								"Incremental Sub Seismogenic Nucleation MFD",
								fractiles)));
				XY_DataSetList subPlusSupraMFDs = getSummed(
						nuclSubSeismoMFDs.get(parentID),
						nuclIncrMFDs.get(parentID));
				plotSubPlusSupraSeismoMFDs
						.put(parentID,
								asIncr(getFractiles(
										subPlusSupraMFDs,
										weightsMap.get(parentID),
										"Incremental Sub+Supra Seismogenic Nucleation MFD",
										fractiles)));
				plotSubPlusSupraSeismoCmlMFDs
						.put(parentID,
								asEvenly(getFractiles(
										asCml(subPlusSupraMFDs),
										weightsMap.get(parentID),
										"Cumulative Sub+Supra Seismogenic Nucleation MFD",
										new double[0])));
			}
		}

		private static XY_DataSetList asCml(XY_DataSetList xyList) {
			XY_DataSetList cmlList = new XY_DataSetList();
			for (XY_DataSet xy : xyList)
				cmlList.add(((IncrementalMagFreqDist) xy).getCumRateDist());
			return cmlList;
		}

		private static List<IncrementalMagFreqDist> asIncr(
				List<DiscretizedFunc> funcs) {
			List<IncrementalMagFreqDist> incrMFDs = Lists.newArrayList();
			for (DiscretizedFunc func : funcs)
				incrMFDs.add((IncrementalMagFreqDist) func);
			return incrMFDs;
		}

		private static List<EvenlyDiscretizedFunc> asEvenly(
				List<DiscretizedFunc> funcs) {
			List<EvenlyDiscretizedFunc> incrMFDs = Lists.newArrayList();
			for (DiscretizedFunc func : funcs)
				incrMFDs.add((EvenlyDiscretizedFunc) func);
			return incrMFDs;
		}

		private XY_DataSetList getSummed(XY_DataSetList list1,
				XY_DataSetList list2) {
			XY_DataSetList sumList = new XY_DataSetList();

			for (int i = 0; i < list1.size(); i++) {
				IncrementalMagFreqDist mfd1 = (IncrementalMagFreqDist) list1
						.get(i);
				IncrementalMagFreqDist mfd2 = (IncrementalMagFreqDist) list2
						.get(i);
				SummedMagFreqDist sum = new SummedMagFreqDist(
						InversionTargetMFDs.MIN_MAG, InversionTargetMFDs.NUM_MAG,
						InversionTargetMFDs.DELTA_MAG);
				sum.addIncrementalMagFreqDist(resizeToDimensions(mfd1,
						InversionTargetMFDs.MIN_MAG, InversionTargetMFDs.NUM_MAG,
						InversionTargetMFDs.DELTA_MAG));
				sum.addIncrementalMagFreqDist(resizeToDimensions(mfd2,
						InversionTargetMFDs.MIN_MAG, InversionTargetMFDs.NUM_MAG,
						InversionTargetMFDs.DELTA_MAG));

				sumList.add(sum);
			}

			return sumList;
		}

		private static IncrementalMagFreqDist resizeToDimensions(
				IncrementalMagFreqDist mfd, double min, int num, double delta) {
			if (mfd.getMinX() == min && mfd.getNum() == num
					&& mfd.getDelta() == delta)
				return mfd;
			IncrementalMagFreqDist resized = new IncrementalMagFreqDist(min,
					num, delta);

			for (int i = 0; i < mfd.getNum(); i++)
				if (mfd.getY(i) > 0)
					resized.set(mfd.get(i));

			return resized;
		}

	}

	public static void writeJumpPlots(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir, String prefix)
			throws IOException {
		RupJumpPlot plot = new RupJumpPlot(weightProvider);
		plot.buildPlot(fetch);

		writeJumpPlots(plot, dir, prefix);
	}

	public static void writeJumpPlots(RupJumpPlot plot, File dir, String prefix)
			throws IOException {
		System.out.println("Making rup jump plots for " + plot.weights.size()
				+ " sols");
		
		File subDir = new File(dir, "rup_jump_plots");
		if (!subDir.exists())
			subDir.mkdir();

		for (int i = 0; i < plot.minMags.length; i++) {
			CommandLineInversionRunner.writeJumpPlot(subDir, prefix,
					plot.plotSolFuncs.get(i), plot.plotRupSetFuncs.get(i),
					RupJumpPlot.jumpDist, plot.minMags[i], plot.paleoProbs[i]);
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

		private transient ConcurrentMap<FaultModels, Map<IDPairing, Double>> distancesCache = Maps
				.newConcurrentMap();

		private List<XY_DataSetList> solFuncs = Lists.newArrayList();
		private List<XY_DataSetList> rupSetFuncs = Lists.newArrayList();
		private List<Double> weights = Lists.newArrayList();

		private List<DiscretizedFunc[]> plotSolFuncs = Lists.newArrayList();
		private List<DiscretizedFunc[]> plotRupSetFuncs = Lists.newArrayList();

		public RupJumpPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, new double[0]);
		}

		public RupJumpPlot(BranchWeightProvider weightProvider,
				double[] fractiles) {
			this.weightProvider = weightProvider;
			this.fractiles = fractiles;

			try {
				paleoProbModel = UCERF3_PaleoProbabilityModel.load();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}

			for (int i = 0; i < minMags.length; i++) {
				solFuncs.add(new XY_DataSetList());
				rupSetFuncs.add(new XY_DataSetList());
			}
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			FaultModels fm = sol.getRupSet().getFaultModel();

			Map<IDPairing, Double> distances = distancesCache.get(fm);
			debug(solIndex, "cache fetching");
			if (distances == null) {
				synchronized (this) {
					distances = distancesCache.get(fm);
					if (distances == null) {
						distances = DeformationModelFetcher.calculateDistances(
								5d, sol.getRupSet().getFaultSectionDataList());
						for (IDPairing pairing : Lists.newArrayList(distances.keySet()))
								distances.put(pairing.getReversed(), distances.get(pairing));
						distancesCache.putIfAbsent(fm, distances);
					}
				}
			}

			double weight = weightProvider.getWeight(branch);

			debug(solIndex, "calculating");
			List<EvenlyDiscretizedFunc[]> myFuncs = Lists.newArrayList();
			for (int i = 0; i < minMags.length; i++) {
				EvenlyDiscretizedFunc[] funcs = CommandLineInversionRunner
						.getJumpFuncs(sol, distances, jumpDist, minMags[i],
								paleoProbModel);
				myFuncs.add(funcs);
			}
			debug(solIndex, "archiving");
			synchronized (this) {
				for (int i = 0; i < myFuncs.size(); i++) {
					EvenlyDiscretizedFunc[] funcs = myFuncs.get(i);
					solFuncs.get(i).add(funcs[0]);
					rupSetFuncs.get(i).add(funcs[1]);
				}
				weights.add(weight);
			}
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				RupJumpPlot o = (RupJumpPlot) otherCalc;

				for (int i = 0; i < minMags.length; i++) {
					solFuncs.get(i).addAll(o.solFuncs.get(i));
					rupSetFuncs.get(i).addAll(o.rupSetFuncs.get(i));
				}
				weights.addAll(o.weights);
			}
		}

		private static DiscretizedFunc[] toArray(List<DiscretizedFunc> funcs) {
			DiscretizedFunc[] array = new DiscretizedFunc[funcs.size()];
			for (int i = 0; i < funcs.size(); i++)
				array[i] = funcs.get(i);
			return array;
		}

		@Override
		protected void doFinalizePlot() {
			for (int i = 0; i < solFuncs.size(); i++) {
				List<DiscretizedFunc> solFractiles = getFractiles(
						solFuncs.get(i), weights, "Solution Jumps", fractiles);
				List<DiscretizedFunc> rupSetFractiles = getFractiles(
						rupSetFuncs.get(i), weights, "Rup Set Jumps", fractiles);
				plotSolFuncs.add(toArray(solFractiles));
				plotRupSetFuncs.add(toArray(rupSetFractiles));
			}
		}

	}

	public static void writeMiniSectRITables(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir, String prefix)
			throws IOException {
		MiniSectRIPlot plot = new MiniSectRIPlot(weightProvider);
		plot.buildPlot(fetch);

		writeMiniSectRITables(plot, dir, prefix);
	}

	public static void writeMiniSectRITables(MiniSectRIPlot plot, File dir,
			String prefix) throws IOException {
		System.out.println("Making mini sect RI plot!");

		for (FaultModels fm : plot.solRatesMap.keySet()) {
			Map<Integer, DeformationSection> dm = plot.loadDM(fm);

			for (int i = 0; i < plot.minMags.length; i++) {
				File file = new File(dir, prefix + "_" + fm.getShortName()
						+ "_mini_sect_RIs_" + (float) plot.minMags[i] + "+.csv");
				MiniSectRecurrenceGen.writeRates(file, dm, plot.avgRatesMap
						.get(fm).get(i));
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

		private transient ConcurrentMap<FaultModels, Map<Integer, List<List<Integer>>>> fmMappingsMap = Maps
				.newConcurrentMap();

		private Map<FaultModels, List<List<Map<Integer, List<Double>>>>> solRatesMap = Maps
				.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		private Map<FaultModels, List<Map<Integer, List<Double>>>> avgRatesMap = Maps
				.newHashMap();

		public MiniSectRIPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
		}

		private transient Map<FaultModels, Map<Integer, DeformationSection>> fmDMsMap = Maps
				.newHashMap();

		private synchronized Map<Integer, DeformationSection> loadDM(
				FaultModels fm) {
			Map<Integer, DeformationSection> dm = fmDMsMap.get(fm);
			if (dm == null) {
				try {
					dm = DeformationModelFileParser
							.load(DeformationModels.GEOLOGIC.getDataFileURL(fm));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
				fmDMsMap.put(fm, dm);
				List<List<Map<Integer, List<Double>>>> solRates = Lists
						.newArrayList();
				for (int i = 0; i < minMags.length; i++)
					solRates.add(new ArrayList<Map<Integer, List<Double>>>());
				List<Double> weights = Lists.newArrayList();
				solRatesMap.put(fm, solRates);
				weightsMap.put(fm, weights);
			}
			return dm;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			FaultModels fm = sol.getRupSet().getFaultModel();

			Map<Integer, DeformationSection> dm = loadDM(fm);

			debug(solIndex, "cache fetching");
			Map<Integer, List<List<Integer>>> mappings = fmMappingsMap.get(fm);
			if (mappings == null) {
				synchronized (this) {
					mappings = fmMappingsMap.get(fm);
					if (mappings == null) {
						mappings = MiniSectRecurrenceGen.buildSubSectMappings(
								dm, sol.getRupSet().getFaultSectionDataList());
						fmMappingsMap.putIfAbsent(fm, mappings);
					}
				}
			}

			double weight = weightProvider.getWeight(branch);

			debug(solIndex, "calculating");
			List<Map<Integer, List<Double>>> myRates = Lists.newArrayList();
			for (int i = 0; i < minMags.length; i++) {
				myRates.add(MiniSectRecurrenceGen.calcMinisectionParticRates(
						sol, mappings, minMags[i], false));
			}
			debug(solIndex, "archiving");
			synchronized (this) {
				for (int i = 0; i < minMags.length; i++) {
					solRatesMap.get(fm).get(i).add(myRates.get(i));
				}
				weightsMap.get(fm).add(weight);
			}
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				MiniSectRIPlot o = (MiniSectRIPlot) otherCalc;
				for (FaultModels fm : o.solRatesMap.keySet()) {
					if (!solRatesMap.containsKey(fm)) {
						List<List<Map<Integer, List<Double>>>> solRates = Lists
								.newArrayList();
						List<Double> weights = Lists.newArrayList();
						solRatesMap.put(fm, solRates);
						weightsMap.put(fm, weights);
					}
					for (int i = 0; i < minMags.length; i++) {
						solRatesMap.get(fm).get(i)
								.addAll(o.solRatesMap.get(fm).get(i));
					}
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			for (int i = 0; i < minMags.length; i++) {
				for (FaultModels fm : solRatesMap.keySet()) {
					List<Map<Integer, List<Double>>> avgRatesList = avgRatesMap
							.get(fm);
					if (!avgRatesMap.containsKey(fm)) {
						avgRatesList = Lists.newArrayList();
						avgRatesMap.put(fm, avgRatesList);
					}
					List<Map<Integer, List<Double>>> solRates = solRatesMap
							.get(fm).get(i);
					double[] weights = Doubles.toArray(weightsMap.get(fm));
					Set<Integer> parents = solRates.get(0).keySet();
					Map<Integer, List<Double>> avg = Maps.newHashMap();

					for (Integer parentID : parents) {
						List<double[]> solRatesList = Lists.newArrayList();
						int numMinis = solRates.get(0).get(parentID).size();
						for (int m = 0; m < numMinis; m++)
							solRatesList.add(new double[solRates.size()]);
						for (int s = 0; s < solRates.size(); s++) {
							List<Double> rates = solRates.get(s).get(parentID);
							for (int m = 0; m < rates.size(); m++)
								solRatesList.get(m)[s] = rates.get(m);
						}

						List<Double> avgRates = Lists.newArrayList();

						for (int m = 0; m < numMinis; m++) {
							double avgRate = FaultSystemSolutionFetcher
									.calcScaledAverage(solRatesList.get(m),
											weights);
							double ri = 1d / avgRate;
							avgRates.add(ri);
						}
						avg.put(parentID, avgRates);

						// if (parentID == 651) {
						// System.out.println("Avg: "+Joiner.on(",").join(avgRates));
						// System.out.println("Branches:");
						// for (int j=0; j<solRates.size(); j++) {
						// Map<Integer, List<Double>> sol = solRates.get(j);
						// System.out.println("\t"+Joiner.on(",").join(sol.get(parentID))+" (weight="+(float)weights[j]+")");
						// }
						// }
					}
					avgRatesList.add(avg);
				}
			}
		}

	}

	public static void writeMisfitTables(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir, String prefix)
			throws IOException {
		MisfitTable plot = new MisfitTable();
		plot.buildPlot(fetch);

		writeMisfitTables(plot, dir, prefix);
	}

	public static void writeMisfitTables(MisfitTable plot, File dir,
			String prefix) throws IOException {
		System.out.println("Making mini sect RI plot!");

		BatchPlotGen.writeMisfitsCSV(dir, prefix, plot.misfitsMap);
	}

	public static class MisfitTable extends CompoundFSSPlots {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ConcurrentMap<VariableLogicTreeBranch, Map<String, Double>> misfitsMap = Maps
				.newConcurrentMap();

		public MisfitTable() {

		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			VariableLogicTreeBranch vbr = new VariableLogicTreeBranch(branch,
					null);
			FaultModels fm = sol.getRupSet().getFaultModel();

			debug(solIndex, "calc/archiving");
			misfitsMap.putIfAbsent(vbr, sol.getMisfits());
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				MisfitTable o = (MisfitTable) otherCalc;
				misfitsMap.putAll(o.misfitsMap);
			}
		}

		@Override
		protected void doFinalizePlot() {

		}

	}

	public static void writePaleoRatesTables(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir, String prefix)
			throws IOException {
		PaleoRatesTable plot = new PaleoRatesTable(weightProvider);
		plot.buildPlot(fetch);

		writePaleoRatesTables(plot, dir, prefix);
	}

	public static void writePaleoRatesTables(PaleoRatesTable plot, File dir,
			String prefix) throws IOException {
		System.out.println("Making paleo/ave slip tables!");

		for (FaultModels fm : plot.aveSlipCSVOutputMap.keySet()) {
			File subDir = new File(dir, "paleo_fault_based");
			if (!subDir.exists())
				subDir.mkdir();
			File aveSlipFile = new File(subDir, fm.getShortName()
					+ "_ave_slip_rates.csv");
			plot.aveSlipCSVOutputMap.get(fm).writeToFile(aveSlipFile);
			File paleoFile = new File(subDir, fm.getShortName()
					+ "_paleo_rates.csv");
			plot.paleoCSVOutputMap.get(fm).writeToFile(paleoFile);
		}
	}

	public static class PaleoRatesTable extends CompoundFSSPlots {

		private transient BranchWeightProvider weightProvider;
		private transient PaleoProbabilityModel paleoProbModel;

		private ConcurrentMap<FaultModels, List<PaleoRateConstraint>> paleoConstraintsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<AveSlipConstraint>> aveSlipConstraintsMap = Maps
				.newConcurrentMap();
		private transient ConcurrentMap<FaultModels, ConcurrentMap<Integer, List<Integer>>> rupsForSectsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> reducedSlipsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> proxyAveSlipRatesMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> aveSlipObsRatesMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, List<double[]>> paleoObsRatesMap = Maps
				.newConcurrentMap();

		private ConcurrentMap<FaultModels, List<Double>> weightsMap = Maps
				.newConcurrentMap();

		private transient Map<FaultModels, CSVFile<String>> aveSlipCSVOutputMap = Maps
				.newHashMap();
		private transient Map<FaultModels, CSVFile<String>> paleoCSVOutputMap = Maps
				.newHashMap();

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
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			FaultModels fm = rupSet.getFaultModel();

			debug(solIndex, "cache fetching");
			List<AveSlipConstraint> aveSlipConstraints = aveSlipConstraintsMap
					.get(fm);
			if (aveSlipConstraints == null) {
				synchronized (this) {
					aveSlipConstraints = aveSlipConstraintsMap.get(fm);
					List<PaleoRateConstraint> paleoConstraints = null;
					if (aveSlipConstraints == null) {
						try {
							aveSlipConstraints = AveSlipConstraint.load(rupSet
									.getFaultSectionDataList());
							paleoConstraints = UCERF3_PaleoRateConstraintFetcher
									.getConstraints(rupSet.getFaultSectionDataList());
						} catch (IOException e) {
							ExceptionUtils.throwAsRuntimeException(e);
						}
						paleoConstraintsMap.putIfAbsent(fm, paleoConstraints);
						ConcurrentMap<Integer, List<Integer>> rupsForSectsLists = Maps
								.newConcurrentMap();
						for (AveSlipConstraint constr : aveSlipConstraints)
							rupsForSectsLists.putIfAbsent(constr
									.getSubSectionIndex(), rupSet.getRupturesForSection(
											constr.getSubSectionIndex()));
						for (PaleoRateConstraint constr : paleoConstraints)
							rupsForSectsLists.putIfAbsent(constr.getSectionIndex(),
									rupSet.getRupturesForSection(constr.getSectionIndex()));
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
						aveSlipConstraintsMap.putIfAbsent(fm,
								aveSlipConstraints);
					}
				}
			}

			double[] slips = new double[aveSlipConstraints.size()];
			double[] proxyRates = new double[aveSlipConstraints.size()];
			double[] obsRates = new double[aveSlipConstraints.size()];

			Map<Integer, List<Integer>> rupsForSectsLists = rupsForSectsMap
					.get(fm);

			debug(solIndex, "calculating ave slip");

			for (int i = 0; i < aveSlipConstraints.size(); i++) {
				AveSlipConstraint constr = aveSlipConstraints.get(i);
				int subsectionIndex = constr.getSubSectionIndex();

				slips[i] = rupSet.getSlipRateForSection(subsectionIndex);
				proxyRates[i] = slips[i] / constr.getWeightedMean();
				double obsRate = 0d;
				for (int rupID : rupsForSectsLists.get(constr
						.getSubSectionIndex())) {
					int sectIndexInRup = rupSet.getSectionsIndicesForRup(rupID)
							.indexOf(subsectionIndex);
					double slipOnSect = rupSet.getSlipOnSectionsForRup(rupID)[sectIndexInRup];
					double probVisible = AveSlipConstraint
							.getProbabilityOfObservedSlip(slipOnSect);
					obsRate += sol.getRateForRup(rupID) * probVisible;
				}
				obsRates[i] = obsRate;
			}

			List<PaleoRateConstraint> paleoConstraints = paleoConstraintsMap
					.get(fm);

			debug(solIndex, "calculating paleo rates");
			double[] paleoRates = new double[paleoConstraints.size()];
			for (int i = 0; i < paleoConstraints.size(); i++) {
				PaleoRateConstraint constr = paleoConstraints.get(i);

				double obsRate = 0d;
				for (int rupID : rupsForSectsLists
						.get(constr.getSectionIndex())) {
					obsRate += sol.getRateForRup(rupID)
							* paleoProbModel.getProbPaleoVisible(rupSet, rupID,
									constr.getSectionIndex());
				}
				paleoRates[i] = obsRate;
			}

			debug(solIndex, "archiving");
			synchronized (this) {
				weightsMap.get(fm).add(weightProvider.getWeight(branch));
				reducedSlipsMap.get(fm).add(slips);
				proxyAveSlipRatesMap.get(fm).add(proxyRates);
				aveSlipObsRatesMap.get(fm).add(obsRates);
				paleoObsRatesMap.get(fm).add(paleoRates);
			}
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				PaleoRatesTable o = (PaleoRatesTable) otherCalc;

				for (FaultModels fm : o.weightsMap.keySet()) {
					if (!weightsMap.containsKey(fm)) {
						weightsMap.put(fm, new ArrayList<Double>());
						aveSlipConstraintsMap.put(fm,
								o.aveSlipConstraintsMap.get(fm));
						paleoConstraintsMap.put(fm,
								o.paleoConstraintsMap.get(fm));
						reducedSlipsMap.put(fm, new ArrayList<double[]>());
						proxyAveSlipRatesMap.put(fm, new ArrayList<double[]>());
						aveSlipObsRatesMap.put(fm, new ArrayList<double[]>());
						paleoObsRatesMap.put(fm, new ArrayList<double[]>());
					}

					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
					reducedSlipsMap.get(fm).addAll(o.reducedSlipsMap.get(fm));
					proxyAveSlipRatesMap.get(fm).addAll(
							o.proxyAveSlipRatesMap.get(fm));
					aveSlipObsRatesMap.get(fm).addAll(
							o.aveSlipObsRatesMap.get(fm));
					paleoObsRatesMap.get(fm).addAll(o.paleoObsRatesMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			InversionFaultSystemSolution ucerf2Sol = UCERF2_ComparisonSolutionFetcher
					.getUCERF2Solution(FaultModels.FM2_1);
			List<AveSlipConstraint> ucerf2AveSlipConstraints;
			List<PaleoRateConstraint> ucerf2PaleoConstraints;
			try {
				ucerf2AveSlipConstraints = AveSlipConstraint.load(
						ucerf2Sol.getRupSet().getFaultSectionDataList());
				ucerf2PaleoConstraints = UCERF3_PaleoRateConstraintFetcher
						.getConstraints(ucerf2Sol.getRupSet().getFaultSectionDataList());
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}

			// ave slip table
			for (FaultModels fm : weightsMap.keySet()) {
				CSVFile<String> csv = new CSVFile<String>(true);

				List<String> header = Lists.newArrayList(fm.getShortName()
						+ " Mapping", "Latitude", "Longitude",
						"Weighted Mean Slip", "UCERF2 Reduced Slip Rate",
						"UCERF2 Proxy Event Rate",
						"UCERF3 Mean Reduced Slip Rate",
						"UCERF3 Mean Proxy Event Rate",
						"UCERF3 Mean Paleo Visible Rate",
						"UCERF3 Min Paleo Visible Rate",
						"UCERF3 Max Paleo Visible Rate");

				csv.addLine(header);

				List<AveSlipConstraint> constraints = aveSlipConstraintsMap
						.get(fm);

				for (int i = 0; i < constraints.size(); i++) {
					AveSlipConstraint constr = constraints.get(i);

					// find matching UCERF2 constraint
					AveSlipConstraint ucerf2Constraint = null;
					for (AveSlipConstraint u2Constr : ucerf2AveSlipConstraints) {
						if (u2Constr.getSiteLocation().equals(
								constr.getSiteLocation())) {
							ucerf2Constraint = u2Constr;
							break;
						}
					}

					List<String> line = Lists.newArrayList();
					line.add(constr.getSubSectionName());
					line.add(constr.getSiteLocation().getLatitude() + "");
					line.add(constr.getSiteLocation().getLongitude() + "");
					line.add(constr.getWeightedMean() + "");
					if (ucerf2Constraint == null) {
						line.add("");
						line.add("");
					} else {
						double ucerf2SlipRate = ucerf2Sol.getRupSet().getSlipRateForSection(
								ucerf2Constraint.getSubSectionIndex());
						line.add(ucerf2SlipRate + "");
						double ucerf2ProxyRate = ucerf2SlipRate
								/ constr.getWeightedMean();
						line.add(ucerf2ProxyRate + "");
					}
					List<double[]> reducedSlipList = reducedSlipsMap.get(fm);
					List<double[]> proxyRatesList = proxyAveSlipRatesMap
							.get(fm);
					List<double[]> obsRatesList = aveSlipObsRatesMap.get(fm);

					int numSols = reducedSlipList.size();
					double[] slips = new double[numSols];
					double[] proxyRates = new double[numSols];
					double[] rates = new double[numSols];
					double[] weigths = Doubles.toArray(weightsMap.get(fm));

					for (int j = 0; j < numSols; j++) {
						slips[j] = reducedSlipList.get(j)[i];
						proxyRates[j] = proxyRatesList.get(j)[i];
						rates[j] = obsRatesList.get(j)[i];
					}

					line.add(FaultSystemSolutionFetcher.calcScaledAverage(
							slips, weigths) + "");
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(
							proxyRates, weigths) + "");
					line.add(FaultSystemSolutionFetcher.calcScaledAverage(
							rates, weigths) + "");
					line.add(StatUtils.min(rates) + "");
					line.add(StatUtils.max(rates) + "");

					csv.addLine(line);
				}

				aveSlipCSVOutputMap.put(fm, csv);
			}

			// paleo table
			for (FaultModels fm : weightsMap.keySet()) {
				CSVFile<String> csv = new CSVFile<String>(true);

				List<String> header = Lists.newArrayList(fm.getShortName()
						+ " Mapping", "Latitude", "Longitude",
						"Paleo Observed Rate", "Paleo Observed Lower Bound",
						"Paleo Observed Upper Bound",
						"UCERF2 Proxy Event Rate",
						"UCERF3 Mean Paleo Visible Rate",
						"UCERF3 Min Paleo Visible Rate",
						"UCERF3 Max Paleo Visible Rate");

				csv.addLine(header);

				List<PaleoRateConstraint> constraints = paleoConstraintsMap
						.get(fm);

				for (int i = 0; i < constraints.size(); i++) {
					PaleoRateConstraint constr = constraints.get(i);

					// find matching UCERF2 constraint
					PaleoRateConstraint ucerf2Constraint = null;
					for (PaleoRateConstraint u2Constr : ucerf2PaleoConstraints) {
						if (u2Constr.getPaleoSiteLoction().equals(
								constr.getPaleoSiteLoction())) {
							ucerf2Constraint = u2Constr;
							break;
						}
					}

					List<String> line = Lists.newArrayList();
					line.add(constr.getFaultSectionName());
					line.add(constr.getPaleoSiteLoction().getLatitude() + "");
					line.add(constr.getPaleoSiteLoction().getLongitude() + "");
					line.add(constr.getMeanRate() + "");
					line.add(constr.getLower95ConfOfRate() + "");
					line.add(constr.getUpper95ConfOfRate() + "");
					if (ucerf2Constraint == null) {
						line.add("");
					} else {
						line.add(PaleoFitPlotter.getPaleoRateForSect(ucerf2Sol,
								ucerf2Constraint.getSectionIndex(),
								paleoProbModel)
								+ "");
					}
					List<double[]> obsRatesList = paleoObsRatesMap.get(fm);

					int numSols = obsRatesList.size();
					double[] rates = new double[numSols];
					double[] weigths = Doubles.toArray(weightsMap.get(fm));

					for (int j = 0; j < numSols; j++)
						rates[j] = obsRatesList.get(j)[i];

					line.add(FaultSystemSolutionFetcher.calcScaledAverage(
							rates, weigths) + "");
					line.add(StatUtils.min(rates) + "");
					line.add(StatUtils.max(rates) + "");

					csv.addLine(line);
				}

				paleoCSVOutputMap.put(fm, csv);
			}
		}

	}

	public static void writeMeanSolutions(FaultSystemSolutionFetcher fetch,
			BranchWeightProvider weightProvider, File dir, String prefix)
			throws IOException {
		MeanFSSBuilder plot = new MeanFSSBuilder(weightProvider);
		plot.buildPlot(fetch);

		writeMeanSolutions(plot, dir, prefix);
	}

	public static void writeMeanSolutions(MeanFSSBuilder plot, File dir,
			String prefix) throws IOException {
		System.out.println("Making mean solutions!");
		
		LaughTestFilter laughTest = LaughTestFilter.getDefault();
		
		GriddedRegion region = plot.region;

		boolean multiFM = plot.weightsMap.keySet().size()>1;
		for (FaultModels fm : plot.weightsMap.keySet()) {
			String myPrefix = prefix;
			if (multiFM)
				myPrefix += "_"+fm.getShortName();
			File outputFile = new File(dir, myPrefix+"_MEAN_BRANCH_AVG_SOL.zip");
			
			double[] rates = plot.ratesMap.get(fm);
			double[] mags = plot.magsMap.get(fm);
			
			if (rates.length == 229104 || rates.length == 249656) {
				System.err.println("WARNING: Using UCERF3.2 laugh test filter!");
				laughTest = LaughTestFilter.getUCERF3p2Filter();
			}
			
			DeformationModels dm;
			if (plot.defModelsMap.get(fm).size() == 1)
				// for single dm, use that one
				dm = plot.defModelsMap.get(fm).iterator().next();
			else
				// mutiple dms, use mean
				dm = DeformationModels.MEAN_UCERF3;
			
			InversionFaultSystemRupSet reference = InversionFaultSystemRupSetFactory.forBranch(laughTest,
					InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE, LogicTreeBranch.getMEAN_UCERF3(fm, dm));
			
			String info = reference.getInfoString();
			
			info = "****** BRANCH AVERAGED SOLUTION FOR "+plot.weightsMap.get(fm).size()+" SOLUTIONS ******\n\n"+info;
			
			List<List<Integer>> clusterRups = Lists.newArrayList();
			List<List<Integer>> clusterSects = Lists.newArrayList();
			for (int i=0; i<reference.getNumClusters(); i++) {
				clusterRups.add(reference.getRupturesForCluster(i));
				clusterSects.add(reference.getSectionsForCluster(i));
			}
			
			// first build the rup set
			InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(
					reference, reference.getLogicTreeBranch(), laughTest,
					reference.getAveRakeForAllRups(), reference.getCloseSectionsListList(),
					reference.getRupturesForClusters(), reference.getSectionsForClusters());
			rupSet.setMagForallRups(mags);
			
			GridSourceProvider gridSources = new GridSourceFileReader(region,
					plot.nodeSubSeisMFDsMap.get(fm), plot.nodeUnassociatedMFDsMap.get(fm));
			InversionFaultSystemSolution sol = new InversionFaultSystemSolution(rupSet, rates);
			sol.setGridSourceProvider(gridSources);
			
			FaultSystemIO.writeSol(sol, outputFile);
		}
	}
	
	public static class MeanFSSBuilder extends CompoundFSSPlots {
		
		private transient BranchWeightProvider weightProvider;
		
		private GriddedRegion region;
		private Map<FaultModels, Map<Integer, IncrementalMagFreqDist>> nodeSubSeisMFDsMap = Maps.newHashMap();
		private Map<FaultModels, Map<Integer, IncrementalMagFreqDist>> nodeUnassociatedMFDsMap = Maps.newHashMap();
		
		private Map<FaultModels, double[]> ratesMap = Maps.newConcurrentMap();
		private Map<FaultModels, double[]> magsMap = Maps.newConcurrentMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newConcurrentMap();
		
		private Map<FaultModels, HashSet<DeformationModels>> defModelsMap = Maps.newHashMap();
		
		public MeanFSSBuilder(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			FaultModels fm = rupSet.getFaultModel();
			
			int numRups = rupSet.getNumRuptures();
			
			double weight = weightProvider.getWeight(branch);
			
			GridSourceProvider gridSources = sol.getGridSourceProvider();
			
			Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = Maps.newHashMap();
			Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = Maps.newHashMap();
			
			if (region == null)
				region = gridSources.getGriddedRegion();
			
			for (int i=0; i<region.getNumLocations(); i++) {
				nodeSubSeisMFDs.put(i, gridSources.getNodeSubSeisMFD(i));
				nodeUnassociatedMFDs.put(i, gridSources.getNodeUnassociatedMFD(i));
			}
			
			synchronized (fm) {
				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
					
					nodeSubSeisMFDsMap.put(fm, new HashMap<Integer, IncrementalMagFreqDist>());
					nodeUnassociatedMFDsMap.put(fm, new HashMap<Integer, IncrementalMagFreqDist>());
					ratesMap.put(fm, new double[numRups]);
					magsMap.put(fm, new double[numRups]);
					defModelsMap.put(fm, new HashSet<DeformationModels>());
				}
				weightsList.add(weight);
				Map<Integer, IncrementalMagFreqDist> runningNodeSubSeisMFDs = nodeSubSeisMFDsMap.get(fm);
				Map<Integer, IncrementalMagFreqDist> runningNodeUnassociatedMFDs = nodeUnassociatedMFDsMap.get(fm);
				
				for (int i=0; i<region.getNumLocations(); i++) {
					addWeighted(runningNodeSubSeisMFDs, i, nodeSubSeisMFDs.get(i), weight);
					addWeighted(runningNodeUnassociatedMFDs, i, nodeUnassociatedMFDs.get(i), weight);
				}
				
				addWeighted(ratesMap.get(fm), sol.getRateForAllRups(), weight);
				addWeighted(magsMap.get(fm), rupSet.getMagForAllRups(), weight);
				
				synchronized (defModelsMap) {
					defModelsMap.get(fm).add(branch.getValue(DeformationModels.class));
				}
			}
		}
		
		private void addWeighted(Map<Integer, IncrementalMagFreqDist> mfdMap, int index,
				IncrementalMagFreqDist newMFD, double weight) {
			if (newMFD == null)
				// simple case
				return;
			IncrementalMagFreqDist runningMFD = mfdMap.get(index);
			if (runningMFD == null) {
				runningMFD = new IncrementalMagFreqDist(newMFD.getMinX(), newMFD.getNum(), newMFD.getDelta());
				mfdMap.put(index, runningMFD);
			} else {
				Preconditions.checkState(runningMFD.getNum() == newMFD.getNum(), "MFD sizes inconsistent");
				Preconditions.checkState((float)runningMFD.getMinX() == (float)newMFD.getMinX(), "MFD min x inconsistent");
				Preconditions.checkState((float)runningMFD.getDelta() == (float)newMFD.getDelta(), "MFD delta inconsistent");
			}
			for (int i=0; i<runningMFD.getNum(); i++)
				runningMFD.add(i, newMFD.getY(i)*weight);
		}
		
		private void addWeighted(double[] running, double[] vals, double weight) {
			Preconditions.checkState(running.length == vals.length);
			for (int i=0; i<running.length; i++)
				running[i] += vals[i]*weight;
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				MeanFSSBuilder o = (MeanFSSBuilder) otherCalc;
				if (region == null)
					region = o.region;
				for (FaultModels fm : o.weightsMap.keySet()) {
					if (!weightsMap.containsKey(fm)) {
						weightsMap.put(fm, new ArrayList<Double>());
						nodeSubSeisMFDsMap.put(fm, new HashMap<Integer, IncrementalMagFreqDist>());
						nodeUnassociatedMFDsMap.put(fm, new HashMap<Integer, IncrementalMagFreqDist>());
						ratesMap.put(fm, new double[o.ratesMap.get(fm).length]);
						magsMap.put(fm, new double[o.magsMap.get(fm).length]);
						defModelsMap.put(fm, new HashSet<DeformationModels>());
					}
					Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = o.nodeSubSeisMFDsMap.get(fm);
					Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = o.nodeUnassociatedMFDsMap.get(fm);
					Map<Integer, IncrementalMagFreqDist> runningNodeSubSeisMFDs = nodeSubSeisMFDsMap.get(fm);
					Map<Integer, IncrementalMagFreqDist> runningNodeUnassociatedMFDs = nodeUnassociatedMFDsMap.get(fm);
					for (int i=0; i<region.getNumLocations(); i++) {
						// weight is one because these have already been scaled
						addWeighted(runningNodeSubSeisMFDs, i, nodeSubSeisMFDs.get(i), 1d);
						addWeighted(runningNodeUnassociatedMFDs, i, nodeUnassociatedMFDs.get(i), 1d);
					}
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
					
					addWeighted(ratesMap.get(fm), o.ratesMap.get(fm), 1d);
					addWeighted(magsMap.get(fm), o.magsMap.get(fm), 1d);
					defModelsMap.get(fm).addAll(o.defModelsMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			// scale everything by total weight
			
			for (FaultModels fm : weightsMap.keySet()) {
				double sum = 0d;
				for (double weight : weightsMap.get(fm))
					sum += weight;
				
				double scale = 1d/sum;
				
				for (IncrementalMagFreqDist mfd : nodeSubSeisMFDsMap.get(fm).values())
					mfd.scale(scale);
				
				for (IncrementalMagFreqDist mfd : nodeUnassociatedMFDsMap.get(fm).values())
					mfd.scale(scale);
				
				double[] rates = ratesMap.get(fm);
				double[] mags = magsMap.get(fm);
				
				for (int i=0; i<rates.length; i++) {
					rates[i] *= scale;
					mags[i] *= scale;
				}
			}
		}
		
	}

	private static List<DiscretizedFunc> getFractiles(XY_DataSetList data,
			List<Double> weights, String name, double[] fractiles) {
		List<DiscretizedFunc> funcs = Lists.newArrayList();

		FractileCurveCalculator calc = new FractileCurveCalculator(data,
				weights);
		for (double fractile : fractiles) {
			DiscretizedFunc func = (DiscretizedFunc) calc.getFractile(fractile);
			func.setName(name + " (fractile at " + fractile + ")");
			funcs.add(func);
		}
		DiscretizedFunc meanFunc = (DiscretizedFunc) calc.getMeanCurve();
		meanFunc.setName(name + " (weighted mean)");
		funcs.add(meanFunc);
		DiscretizedFunc minFunc = (DiscretizedFunc) calc.getMinimumCurve();
		minFunc.setName(name + " (minimum)");
		funcs.add(minFunc);
		DiscretizedFunc maxFunc = (DiscretizedFunc) calc.getMaximumCurve();
		maxFunc.setName(name + " (maximum)");
		funcs.add(maxFunc);

		return funcs;
	}

	public static class SlipRatePlots extends MapBasedPlot {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final String PLOT_DATA_FILE_NAME = "slip_misfit_plots.xml";

		private transient BranchWeightProvider weightProvider;

		private ConcurrentMap<FaultModels, List<FaultSectionPrefData>> sectDatasMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, Map<String, List<Integer>>> parentSectsMap = Maps.newConcurrentMap();
		private Map<FaultModels, List<double[]>> solSlipsMap = Maps.newHashMap();
		private Map<FaultModels, List<double[]>> targetSlipsMap = Maps.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		private List<MapPlotData> plots;

		private static int cnt;
		
		private Map<FaultModels, CSVFile<String>> subSectCSVs = Maps.newHashMap();
		private Map<FaultModels, CSVFile<String>> parentSectCSVs = Maps.newHashMap();

		public SlipRatePlots(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;

			cnt = 0;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			int myCnt = cnt++;
			debug(solIndex, "Processing solution " + myCnt);

			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;

			double[] solSlips = sol.calcSlipRateForAllSects();
			double[] targetSlips = rupSet.getSlipRateForAllSections();
//			double[] ratios = new double[solSlips.length];
//			double[] fractDiffs = new double[solSlips.length];
//			for (int i = 0; i < solSlips.length; i++) {
//				// if (targetSlips[i] < 1e-5)
//				// System.out.println(branch.buildFileName()+": target["+i+"]="
//				// +targetSlips[i]+", sol["+i+"]="+solSlips[i]);
//				if (targetSlips[i] < 1e-8) {
//					ratios[i] = 1;
//					fractDiffs[i] = 0;
//				} else {
//					ratios[i] = solSlips[i] / targetSlips[i];
//					fractDiffs[i] = (solSlips[i] - targetSlips[i])
//							/ targetSlips[i];
//				}
//			}

			FaultModels fm = rupSet.getFaultModel();

			if (!sectDatasMap.containsKey(fm)) {
				sectDatasMap.putIfAbsent(fm, rupSet.getFaultSectionDataList());
			}
			
			if (!parentSectsMap.containsKey(fm)) {
				Map<String, List<Integer>> parentsMap = Maps.newHashMap();
				List<Integer> sects = Lists.newArrayList();
				String prevParentName =rupSet.getFaultSectionData(0).getParentSectionName();
				for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++) {
					String parentName = rupSet.getFaultSectionData(sectIndex).getParentSectionName();
					if (!parentName.equals(prevParentName)) {
						parentsMap.put(prevParentName, sects);
						prevParentName = parentName;
						sects = Lists.newArrayList();
					}
					sects.add(sectIndex);
				}
				if (!sects.isEmpty()) {
					parentsMap.put(prevParentName, sects);
				}
				parentSectsMap.putIfAbsent(fm, parentsMap);
			}

			debug(solIndex, "Archiving solution " + myCnt);

			synchronized (this) {
				List<double[]> solSlipsList = solSlipsMap.get(fm);
				if (solSlipsList == null) {
					solSlipsList = Lists.newArrayList();
					solSlipsMap.put(fm, solSlipsList);
				}
				solSlipsList.add(solSlips);
				
				List<double[]> targetsList = targetSlipsMap.get(fm);
				if (targetsList == null) {
					targetsList = Lists.newArrayList();
					targetSlipsMap.put(fm, targetsList);
				}
				targetsList.add(targetSlips);

				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
				}
				weightsList.add(weight);
			}

			debug(solIndex, "Done with solution " + myCnt);
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				SlipRatePlots o = (SlipRatePlots) otherCalc;
				for (FaultModels fm : o.weightsMap.keySet()) {
					if (!sectDatasMap.containsKey(fm)) {
						sectDatasMap.put(fm, o.sectDatasMap.get(fm));
						parentSectsMap.put(fm, o.parentSectsMap.get(fm));
						solSlipsMap.put(fm, new ArrayList<double[]>());
						targetSlipsMap.put(fm, new ArrayList<double[]>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					solSlipsMap.get(fm).addAll(o.solSlipsMap.get(fm));
					targetSlipsMap.get(fm).addAll(o.targetSlipsMap.get(fm));
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}
		
		private static double meanFromIndexes(double[] array, List<Integer> indexes) {
			double sum = 0;
			for (int index : indexes)
				sum += array[index];
			return sum / (double)indexes.size();
		}

		@Override
		protected void doFinalizePlot() {
			plots = Lists.newArrayList();

			boolean multipleFMs = sectDatasMap.keySet().size() > 1;

			CPT linearCPT = FaultBasedMapGen.getLinearRatioCPT();
			CPT logCPT = FaultBasedMapGen.getLogRatioCPT().rescale(-1, 1);
			CPT slipRateCPT = FaultBasedMapGen.getSlipRateCPT();

			Region region = new CaliforniaRegions.RELM_TESTING();

			boolean skipNans = false;

			for (FaultModels fm : sectDatasMap.keySet()) {
				List<FaultSectionPrefData> sectDatas = sectDatasMap.get(fm);
				List<LocationList> faults = FaultBasedMapGen.getTraces(sectDatas);
				List<double[]> solSlipsList = solSlipsMap.get(fm);
				List<double[]> targetsList = targetSlipsMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);

				// TODO reinstate filter?????
//				double[] ratios = getWeightedAvg(faults.size(), ratiosList,
//						weightsList);
				double[] solSlips = getWeightedAvg(faults.size(), solSlipsList,
						weightsList);
				double[] targets = getWeightedAvg(faults.size(), targetsList,
						weightsList);
				
				double[] ratios = new double[solSlips.length];
				for (int i=0; i<ratios.length; i++)
					ratios[i] = solSlips[i] / targets[i];
				
				// make CSV file
				CSVFile<String> subSectCSV = new CSVFile<String>(true);
				subSectCSV.addLine("Sub Section Index", "Parent Section ID",
						"Mean Target Slip Rate (m/yr)", "Mean Solution Slip Rate (m/yr)", "Mean Slip Rate Misfit Ratio");
				for (int sectIndex=0; sectIndex<solSlips.length; sectIndex++) {
					subSectCSV.addLine(sectIndex+"", sectDatas.get(sectIndex).getParentSectionId()+"",
							targets[sectIndex]+"", solSlips[sectIndex]+"", ratios[sectIndex]+"");
				}
				subSectCSVs.put(fm, subSectCSV);
				CSVFile<String> parentSectCSV = new CSVFile<String>(true);
				Map<Integer, String> parentNamesMap = Maps.newHashMap();
				for (FaultSectionPrefData sect : sectDatas)
					parentNamesMap.put(sect.getParentSectionId(), sect.getParentSectionName());
				List<Integer> parentIDs = Lists.newArrayList(parentNamesMap.keySet());
				Collections.sort(parentIDs);
				parentSectCSV.addLine("Parent Section ID", "Parent Section Name",
						"Mean Target Slip Rate (m/yr)", "Mean Solution Slip Rate (m/yr)", "Mean Slip Rate Misfit Ratio");
				for (Integer parentID : parentIDs) {
					String parentName = parentNamesMap.get(parentID);
					List<Integer> indexes = parentSectsMap.get(fm).get(parentName);
					parentSectCSV.addLine(parentID+"", parentName+"", meanFromIndexes(targets, indexes)+"",
							meanFromIndexes(solSlips, indexes)+"", meanFromIndexes(ratios, indexes)+"");
				}
				parentSectCSVs.put(fm, parentSectCSV);

				String label = "Mean(Solution Slip Rate / Target Slip Rate)";
				String prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName() + "_";
					label = fm.getShortName() + " " + label;
				}
				MapPlotData plot = new MapPlotData(linearCPT, faults, ratios, region,
						skipNans, label, prefix + "slip_rate_misfit");
				plot.subDirName = "slip_rate_plots";
				plots.add(plot);

				label = "Log10(" + label + ")";
				double[] log10Values = FaultBasedMapGen.log10(ratios);
				plots.add(new MapPlotData(logCPT, faults, log10Values, region,
						skipNans, label, prefix + "slip_rate_misfit_log"));

//				List<double[]> fractDiffList = fractDiffMap.get(fm);
//				double[] fractDiffs = getWeightedAvg(faults.size(),
//						fractDiffList, weightsList);

				label = "Mean((Solution Slip Rate - Target Slip Rate) / Target)";
				prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName() + "_";
					label = fm.getShortName() + " " + label;
				}

				plots.add(new MapPlotData(slipRateCPT, faults, FaultBasedMapGen.scale(solSlips, 1e3), region,
						skipNans, label, prefix + "sol_slip_rate"));

				label = "Mean Target Slip Rate (mm/yr)";
				prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName() + "_";
					label = fm.getShortName() + " " + label;
				}

				plots.add(new MapPlotData(slipRateCPT, faults, FaultBasedMapGen.scale(targets, 1e3), region,
						skipNans, label, prefix + "target_slip_rate"));
			}
		}
		
		@Override
		protected void writeExtraData(File dir, String prefix) {
			boolean multipleFMs = subSectCSVs.keySet().size() > 1;
			
			for (FaultModels fm : subSectCSVs.keySet()) {
				CSVFile<String> subSectCSV = subSectCSVs.get(fm);
				CSVFile<String> parentSectCSV = parentSectCSVs.get(fm);
				
				String fname = prefix;
				if (multipleFMs)
					fname += "_"+fm.getShortName();
				try {
					subSectCSV.writeToFile(new File(dir, fname+"_slip_rates_sub_sections.csv"));
					parentSectCSV.writeToFile(new File(dir, fname+"_slip_rates_parent_sections.csv"));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
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

	public static class AveSlipMapPlot extends MapBasedPlot {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final String PLOT_DATA_FILE_NAME = "ave_slip_plots.xml";

		private transient BranchWeightProvider weightProvider;

		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps
				.newConcurrentMap();
		private Map<FaultModels, List<double[]>> aveSlipsMap = Maps
				.newHashMap();
		private Map<FaultModels, List<double[]>> avePaleoSlipsMap = Maps
				.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		private List<MapPlotData> plots;

		private static int cnt;

		public AveSlipMapPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;

			cnt = 0;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			InversionFaultSystemRupSet rupSet = sol.getRupSet();
			int myCnt = cnt++;
			debug(solIndex, "Processing solution " + myCnt);

			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;

			FaultModels fm = rupSet.getFaultModel();

			double[] aveSlips = new double[rupSet.getNumSections()];
			double[] avePaleoSlips = new double[rupSet.getNumSections()];
			for (int i = 0; i < aveSlips.length; i++) {
				aveSlips[i] = sol.calcSlipPFD_ForSect(i).getMean();
				avePaleoSlips[i] = sol.calcPaleoObsSlipPFD_ForSect(i).getMean();
			}

			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(rupSet.getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}

			debug(solIndex, "Archiving solution " + myCnt);

			synchronized (this) {
				List<double[]> aveSlipsList = aveSlipsMap.get(fm);
				if (aveSlipsList == null) {
					aveSlipsList = Lists.newArrayList();
					aveSlipsMap.put(fm, aveSlipsList);
				}
				aveSlipsList.add(aveSlips);

				List<double[]> avePaleoSlipsList = avePaleoSlipsMap.get(fm);
				if (avePaleoSlipsList == null) {
					avePaleoSlipsList = Lists.newArrayList();
					avePaleoSlipsMap.put(fm, avePaleoSlipsList);
				}
				avePaleoSlipsList.add(avePaleoSlips);

				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
				}
				weightsList.add(weight);
			}

			debug(solIndex, "Done with solution " + myCnt);
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				AveSlipMapPlot o = (AveSlipMapPlot) otherCalc;
				for (FaultModels fm : o.aveSlipsMap.keySet()) {
					if (!faultsMap.containsKey(fm)) {
						faultsMap.put(fm, o.faultsMap.get(fm));
						aveSlipsMap.put(fm, new ArrayList<double[]>());
						avePaleoSlipsMap.put(fm, new ArrayList<double[]>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					aveSlipsMap.get(fm).addAll(o.aveSlipsMap.get(fm));
					avePaleoSlipsMap.get(fm).addAll(o.avePaleoSlipsMap.get(fm));
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			plots = Lists.newArrayList();

			boolean multipleFMs = faultsMap.keySet().size() > 1;

			CPT cpt;
			try {
				cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, 8);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}

			Region region = new CaliforniaRegions.RELM_TESTING();

			boolean skipNans = false;

			for (FaultModels fm : faultsMap.keySet()) {
				List<LocationList> faults = faultsMap.get(fm);
				List<double[]> aveSlipsList = aveSlipsMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);

				double[] ratios = getWeightedAvg(faults.size(), aveSlipsList,
						weightsList);

				String label = "Average Slip (m)";
				String prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName() + "_";
					label = fm.getShortName() + " " + label;
				}

				plots.add(new MapPlotData(cpt, faults, ratios, region,
						skipNans, label, prefix + "ave_slip"));

				List<double[]> avePaleoSlipsList = avePaleoSlipsMap.get(fm);
				double[] fractDiffs = getWeightedAvg(faults.size(),
						avePaleoSlipsList, weightsList);

				label = "Paleo Observable Average Slip (m)";
				prefix = "";
				if (multipleFMs) {
					prefix += fm.getShortName() + "_";
					label = fm.getShortName() + " " + label;
				}

				MapPlotData plot = new MapPlotData(cpt, faults, fractDiffs, region,
						skipNans, label, prefix + "ave_paleo_obs_slip");
				plot.subDirName = "ave_slip_plots";
				plots.add(plot);
			}
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

	public static class MultiFaultParticPlot extends MapBasedPlot {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final String PLOT_DATA_FILE_NAME = "multi_fault_rates.xml";
		public static final String SUB_DIR_NAME = "multi_fault_partics";

		private static final double minMag = 6.7;

		private transient BranchWeightProvider weightProvider;

		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, Map<Integer, int[]>> sectsByParentsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, Map<Integer, int[]>> parentsByParentsMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<Integer, String> parentNamesMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<FaultModels, Map<Integer, List<Integer>>> rupsForParentsMap = Maps
				.newConcurrentMap();
		private Map<FaultModels, List<Map<Integer, double[]>>> ratesMap = Maps
				.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		private List<MapPlotData> plots;

		private static int cnt;

		public MultiFaultParticPlot(BranchWeightProvider weightProvider) {
			this.weightProvider = weightProvider;

			cnt = 0;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			int myCnt = cnt++;
			debug(solIndex, "Processing solution " + myCnt);
			
			InversionFaultSystemRupSet rupSet = sol.getRupSet();

			FaultModels fm = rupSet.getFaultModel();

			debug(solIndex, "cache fetching");
			Map<Integer, int[]> sectsByParents = sectsByParentsMap.get(fm);
			if (sectsByParents == null) {
				synchronized (this) {
					if (sectsByParents == null) {
						// use hashset to avoid duplicates

						sectsByParents = Maps.newHashMap();
						HashSet<Integer> parentsSet = new HashSet<Integer>();
						for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
							parentsSet.add(sect.getParentSectionId());
							parentNamesMap.putIfAbsent(
									sect.getParentSectionId(),
									sect.getParentSectionName());
						}

						Map<Integer, List<Integer>> rupsForParents = Maps
								.newHashMap();

						Map<Integer, int[]> parentsByParents = Maps
								.newHashMap();

						for (Integer parentID : parentsSet) {
							// use hashset to avoid duplicates
							HashSet<Integer> subSectsSet = new HashSet<Integer>();
							// use hashset to avoid duplicates
							HashSet<Integer> parentsByParentsSet = new HashSet<Integer>();

							List<Integer> rups = rupSet.getRupturesForParentSection(parentID);
							rupsForParents.put(parentID, rups);

							for (Integer rupID : rups) {
								for (Integer sectIndex : rupSet.getSectionsIndicesForRup(rupID)) {
									subSectsSet.add(sectIndex);
									parentsByParentsSet.add(
											rupSet.getFaultSectionData(sectIndex).getParentSectionId());
								}
							}

							List<Integer> subSects = Lists
									.newArrayList(subSectsSet);
							// sort to ensure correct order between different
							// sols
							Collections.sort(subSects);

							sectsByParents
									.put(parentID, Ints.toArray(subSects));

							List<Integer> parentsByParentsList = Lists
									.newArrayList(parentsByParentsSet);
							// sort to ensure correct order between different
							// sols
							Collections.sort(parentsByParentsList);

							parentsByParents.put(parentID,
									Ints.toArray(parentsByParentsList));
						}

						rupsForParentsMap.put(fm, rupsForParents);
						parentsByParentsMap.put(fm, parentsByParents);

						// this MUST be the last line of this synchronized block
						sectsByParentsMap.put(fm, sectsByParents);
					}
				}
				sectsByParents = sectsByParentsMap.get(fm);
			}

			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;

			debug(solIndex, "calculating");

			Map<Integer, List<Integer>> rupsMap = rupsForParentsMap.get(fm);

			Map<Integer, double[]> rates = Maps.newHashMap();

			for (Integer parentID : sectsByParents.keySet()) {
				int[] sectsInvolved = sectsByParents.get(parentID);
				double[] parentRates = new double[sectsInvolved.length];
				for (Integer rupID : rupsMap.get(parentID)) {
					if (rupSet.getMagForRup(rupID) < minMag)
						continue;
					double rate = sol.getRateForRup(rupID);
					for (int sectID : rupSet.getSectionsIndicesForRup(rupID)) {
						int sectIndexInArray = Arrays.binarySearch(
								sectsInvolved, sectID);
						parentRates[sectIndexInArray] += rate;
					}
				}
				rates.put(parentID, parentRates);
			}

			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(
						rupSet.getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}

			debug(solIndex, "Archiving solution " + myCnt);

			synchronized (this) {
				List<Map<Integer, double[]>> ratesList = ratesMap.get(fm);
				if (ratesList == null) {
					ratesList = Lists.newArrayList();
					ratesMap.put(fm, ratesList);
				}
				ratesList.add(rates);

				List<Double> weightsList = weightsMap.get(fm);
				if (weightsList == null) {
					weightsList = Lists.newArrayList();
					weightsMap.put(fm, weightsList);
				}
				weightsList.add(weight);
			}

			debug(solIndex, "Done with solution " + myCnt);
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				MultiFaultParticPlot o = (MultiFaultParticPlot) otherCalc;
				for (FaultModels fm : o.ratesMap.keySet()) {
					if (!faultsMap.containsKey(fm)) {
						faultsMap.put(fm, o.faultsMap.get(fm));
						sectsByParentsMap.put(fm, o.sectsByParentsMap.get(fm));
						parentsByParentsMap.put(fm,
								o.parentsByParentsMap.get(fm));
						parentNamesMap.putAll(o.parentNamesMap);
						ratesMap.put(fm,
								new ArrayList<Map<Integer, double[]>>());
						weightsMap.put(fm, new ArrayList<Double>());
					}
					ratesMap.get(fm).addAll(o.ratesMap.get(fm));
					weightsMap.get(fm).addAll(o.weightsMap.get(fm));
				}
			}
		}

		@Override
		protected void doFinalizePlot() {
			plots = Lists.newArrayList();

			boolean multipleFMs = faultsMap.keySet().size() > 1;

			CPT cpt;
			try {
				cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-10, -2);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			cpt.setNanColor(Color.GRAY);

			Region region = new CaliforniaRegions.RELM_TESTING();

			boolean skipNans = false;

			List<FaultModels> fmList = Lists.newArrayList(faultsMap.keySet());

			for (int f = 0; f < fmList.size(); f++) {
				FaultModels fm = fmList.get(f);

				List<LocationList> faults = faultsMap.get(fm);
				List<Map<Integer, double[]>> ratesList = ratesMap.get(fm);
				List<Double> weightsList = weightsMap.get(fm);
				Map<Integer, int[]> sectsByParents = sectsByParentsMap.get(fm);

				Map<Integer, FaultSectionPrefData> parentSectsMap = fm
						.fetchFaultSectionsMap();

				for (Integer parentID : ratesList.get(0).keySet()) {
					if (!parentSectsMap.containsKey(parentID))
						continue; // removed
					int[] sectsInvolved = sectsByParents.get(parentID);

					List<double[]> solRates = Lists.newArrayList();
					for (Map<Integer, double[]> solRatesMap : ratesList)
						solRates.add(solRatesMap.get(parentID));
					List<Double> myWeightsList = weightsList;

					boolean comboFM = false;

					String parentName = parentNamesMap.get(parentID);

					if (f == 0 && fmList.size() > 1) {
						// see if we can combine all FMs here
						int[] parentsByParent = parentsByParentsMap.get(fm)
								.get(parentID);

						boolean match = true;

						for (int i = 1; i < fmList.size(); i++) {
							int[] otherParentsByParent = parentsByParentsMap
									.get(fmList.get(i)).get(parentID);
							if (otherParentsByParent == null
									|| !Arrays.equals(parentsByParent,
											otherParentsByParent)) {
								match = false;
								break;
							}
						}

						if (match) {
							comboFM = true;

							// System.out.println("Merging FMs for: "+parentName);

							myWeightsList = Lists.newArrayList(myWeightsList);
							for (int i = 1; i < fmList.size(); i++) {
								FaultModels ofm = fmList.get(i);
								myWeightsList.addAll(weightsMap.get(ofm));
								for (Map<Integer, double[]> solRatesMap : ratesMap
										.get(ofm))
									solRates.add(solRatesMap.remove(parentID));
							}
						}
					}

					double[] rates = getWeightedAvg(sectsInvolved.length,
							solRates, weightsList);

					// +1 because we add the highlight at the end
					double[] allRates = new double[faults.size() + 1];
					// initialize to NaN
					for (int i = 0; i < allRates.length; i++)
						allRates[i] = Double.NaN;

					for (int i = 0; i < sectsInvolved.length; i++) {
						int sectIndex = sectsInvolved[i];

						allRates[sectIndex] = rates[i];
					}

					allRates = FaultBasedMapGen.log10(allRates);
					List<LocationList> myFaults = Lists.newArrayList(faults);
					// add highlight
					myFaults.add(parentSectsMap.get(parentID).getFaultTrace());
					allRates[allRates.length - 1] = FaultBasedMapGen.FAULT_HIGHLIGHT_VALUE;

					String label = parentNamesMap.get(parentID) + " ("
							+ parentID + ")";
					String prefix = parentName.replaceAll("\\W+", "_");
					if (!comboFM) {
						label = fm.getShortName() + " " + label;
						prefix += "_" + fm.getShortName();
					}
					MapPlotData plot = new MapPlotData(cpt, myFaults, allRates,
							region, skipNans, label, prefix);
					plot.subDirName = SUB_DIR_NAME;
					plots.add(plot);
				}
			}
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

		private ConcurrentMap<FaultModels, List<LocationList>> faultsMap = Maps
				.newConcurrentMap();
		private Map<FaultModels, List<List<double[]>>> valuesMap = Maps
				.newHashMap();
		private Map<FaultModels, List<Double>> weightsMap = Maps.newHashMap();

		private List<MapPlotData> plots;

		public ParticipationMapPlot(BranchWeightProvider weightProvider) {
			this(weightProvider, getDefaultRanges());
		}

		public ParticipationMapPlot(BranchWeightProvider weightProvider,
				List<double[]> ranges) {
			this.weightProvider = weightProvider;
			this.ranges = ranges;
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			double weight = weightProvider.getWeight(branch);
			if (weight == 0)
				return;

			debug(solIndex, "calculating");

			List<double[]> myValues = Lists.newArrayList();
			for (double[] range : ranges) {
				myValues.add(sol.calcParticRateForAllSects(range[0], range[1]));
			}

			FaultModels fm = sol.getRupSet().getFaultModel();

			debug(solIndex, "trace building");
			if (!faultsMap.containsKey(fm)) {
				List<LocationList> faults = FaultBasedMapGen.getTraces(
						sol.getRupSet().getFaultSectionDataList());
				faultsMap.putIfAbsent(fm, faults);
			}

			debug(solIndex, "archiving");
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
			debug(solIndex, "done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				ParticipationMapPlot o = (ParticipationMapPlot) otherCalc;
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
		protected void doFinalizePlot() {
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

				FaultSystemSolution ucerf2 = UCERF2_ComparisonSolutionFetcher
						.getUCERF2Solution(fm);

				for (int i = 0; i < ranges.size(); i++) {
					double minMag = ranges.get(i)[0];
					double maxMag = ranges.get(i)[1];

					List<double[]> rangeValsList = Lists.newArrayList();
					for (int s = 0; s < valuesList.size(); s++)
						rangeValsList.add(valuesList.get(s).get(i));

					// double[] values = getWeightedAvg(faults.size(),
					// rangeValsList, weightsList);
					double[] values = new double[faults.size()];
					// TODO
					// double[] stdDevs = getStdDevs(faults.size(),
					// valuesList.get(i));
					double[] stdDevs = new double[values.length];
					for (int s = 0; s < values.length; s++) {
						ArbDiscrEmpiricalDistFunc func = new ArbDiscrEmpiricalDistFunc();
						for (int j = 0; j < weightsList.size(); j++)
							// val, weight
							func.set(rangeValsList.get(j)[s],
									weightsList.get(j));
						stdDevs[s] = func.getStdDev();
						values[s] = func.getMean();
					}
					double[] logValues = FaultBasedMapGen.log10(values);

					String name = "partic_rates_" + (float) minMag;
					String title = "Log10(Participation Rates "
							+ (float) +minMag;
					if (maxMag < 9) {
						name += "_" + (float) maxMag;
						title += "=>" + (float) maxMag;
					} else {
						name += "+";
						title += "+";
					}
					title += ")";

					if (multipleFMs) {
						name = fm.getShortName() + "_" + name;
						title = fm.getShortName() + " " + title;
					}

					plots.add(new MapPlotData(participationCPT, faults,
							logValues, region, skipNans, title, name));

					double[] ucerf2Vals = ucerf2.calcParticRateForAllSects(
							minMag, maxMag);

					double[] ratios = new double[ucerf2Vals.length];
					for (int j = 0; j < values.length; j++) {
						ratios[j] = values[j] / ucerf2Vals[j];
						if (omitInfinites && Double.isInfinite(ratios[j]))
							ratios[j] = Double.NaN;
					}
					ratios = FaultBasedMapGen.log10(ratios);

					name = "partic_ratio_" + (float) minMag;
					title = "Log10(Participation Ratios " + (float) +minMag;
					if (maxMag < 9) {
						name += "_" + (float) maxMag;
						title += "=>" + (float) maxMag;
					} else {
						name += "+";
						title += "+";
					}
					title += ")";

					if (multipleFMs) {
						name = fm.getShortName() + "_" + name;
						title = fm.getShortName() + " " + title;
					}

					plots.add(new MapPlotData(logCPT, faults, ratios, region,
							skipNans, title, name));

					double[] stdNormVals = new double[values.length];

					for (int s = 0; s < stdNormVals.length; s++) {
						if (ucerf2Vals[s] == 0)
							stdNormVals[s] = Double.NaN;
						else
							stdNormVals[s] = (values[s] - ucerf2Vals[s])
									/ stdDevs[s];
					}

					name = "partic_diffs_norm_std_dev_" + (float) minMag;
					title = "(U3mean - U2mean)/U3std " + (float) +minMag;
					if (maxMag < 9) {
						name += "_" + (float) maxMag;
						title += "=>" + (float) maxMag;
					} else {
						name += "+";
						title += "+";
					}
					// title += ")";

					if (multipleFMs) {
						name = fm.getShortName() + "_" + name;
						title = fm.getShortName() + " " + title;
					}

					MapPlotData plot = new MapPlotData(logCPT, faults, stdNormVals,
							region, skipNans, title, name);
					plot.subDirName = "fault_participation_plots";
					plots.add(plot);
				}
			}
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

		private List<List<GeoDataSet>> particDatas;
		private List<List<GeoDataSet>> nuclDatas;
		private List<Double> weights;
		private GriddedRegion griddedRegion;

		private transient Map<FaultModels, FSSRupNodesCache> rupNodesCache = Maps.newHashMap();

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

		public GriddedParticipationMapPlot(BranchWeightProvider weightProvider,
				double spacing) {
			this(weightProvider, getDefaultRanges(), spacing);
		}

		public GriddedParticipationMapPlot(BranchWeightProvider weightProvider,
				List<double[]> ranges, double spacing) {
			this.weightProvider = weightProvider;
			this.ranges = ranges;
			this.spacing = spacing;
			griddedRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);

			particDatas = Lists.newArrayList();
			nuclDatas = Lists.newArrayList();
			weights = Lists.newArrayList();
		}

		@Override
		protected String getPlotDataFileName() {
			return "gridded_participation_plots.xml";
		}

		@Override
		protected void processSolution(LogicTreeBranch branch,
				InversionFaultSystemSolution sol, int solIndex) {
			processERF(branch, new UCERF3_FaultSysSol_ERF(sol), 0);
		}

		@Override
		protected void processERF(LogicTreeBranch branch,
				UCERF3_FaultSysSol_ERF erf, int solIndex) {

			debug(solIndex, "cache check");
			FaultModels fm = branch.getValue(FaultModels.class);
			synchronized (this) {
				if (!rupNodesCache.containsKey(fm)) {
					FSSRupNodesCache cache = new FSSRupNodesCache();
					rupNodesCache.put(fm, cache);
				}
			}
			debug(solIndex, "cache check done");
			FSSRupNodesCache cache = rupNodesCache.get(fm);
			List<GeoDataSet> particData = Lists.newArrayList();
			List<GeoDataSet> nuclData = Lists.newArrayList();
			for (int i = 0; i < ranges.size(); i++) {
				double[] range = ranges.get(i);
				double minMag = range[0];
				double maxMag = range[1];
				debug(solIndex, "calc partic range " + i);
				particData.add(ERF_Calculator.getParticipationRatesInRegion(erf,
							griddedRegion, minMag, maxMag, cache));
				debug(solIndex, "done partic range " + i);
			}
			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
			erf.updateForecast();
			for (int i = 0; i < ranges.size(); i++) {
				double[] range = ranges.get(i);
				double minMag = range[0];
				double maxMag = range[1];
				if (minMag > 5d)
					break;
				debug(solIndex, "calc nucl range " + i);
				nuclData.add(ERF_Calculator.getNucleationRatesInRegion(erf,
							griddedRegion, minMag, maxMag, cache));
				debug(solIndex, "done nucl range " + i);
			}
			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
			erf.updateForecast();
			debug(solIndex, "archive");
			synchronized (this) {
				particDatas.add(particData);
				nuclDatas.add(nuclData);
				weights.add(weightProvider.getWeight(branch));
			}
			debug(solIndex, "archive done");
		}

		@Override
		protected void combineDistributedCalcs(
				Collection<CompoundFSSPlots> otherCalcs) {
			for (CompoundFSSPlots otherCalc : otherCalcs) {
				GriddedParticipationMapPlot o = (GriddedParticipationMapPlot) otherCalc;
				particDatas.addAll(o.particDatas);
				nuclDatas.addAll(o.nuclDatas);
				weights.addAll(o.weights);
			}
		}

		@Override
		protected void doFinalizePlot() {
			debug(-1, "Finalizing plot");
			boolean debug = false;

			plots = Lists.newArrayList();

			CPT particCPT = FaultBasedMapGen.getParticipationCPT()
					.rescale(-5, -1);
			CPT nuclCPT = FaultBasedMapGen.getParticipationCPT()
					.rescale(-6, -1);
			CPT ratioCPT = (CPT) FaultBasedMapGen.getLogRatioCPT().clone();
			ratioCPT.setNanColor(Color.WHITE);
			ratioCPT.setAboveMaxColor(Color.BLACK);

			MeanUCERF2 ucerf2 = new MeanUCERF2();
			ucerf2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME,
					UCERF2.PROB_MODEL_POISSON);
			ucerf2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
					UCERF2.FULL_DDW_FLOATER);
			ucerf2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
			ucerf2.setParameter(UCERF2.BACK_SEIS_RUP_NAME,
					UCERF2.BACK_SEIS_RUP_POINT);
			ucerf2.getTimeSpan().setDuration(1d);
			ucerf2.updateForecast();

			for (int c = 0; c < ranges.size()*2; c++) {
				boolean nucleation = c >= ranges.size();
				int r = c % ranges.size();
				
				if (nucleation && r == 0) {
					debug(-1, "Setting up UCERF2 comp erf for only back seis");
					ucerf2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
					ucerf2.updateForecast();
				}
				
				CPT cpt;
				List<List<GeoDataSet>> datas;
				if (nucleation) {
					datas = nuclDatas;
					cpt = nuclCPT;
				} else {
					datas = particDatas;
					cpt = particCPT;
				}
				
				if (datas.get(0).size() <= r) {
					debug(-1, "SKIPPING r="+r+", nucleation="+nucleation);
					continue;
				}
				debug(-1, "Building r="+r+", nucleation="+nucleation);
				
				double[] range = ranges.get(r);
				double minMag = range[0];
				double maxMag = range[1];

				XY_DataSetList funcs = new XY_DataSetList();

				for (int i = 0; i < datas.size(); i++) {
					GeoDataSet data = datas.get(i).get(r);
					EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d,
							data.size(), 1d);
					for (int j = 0; j < data.size(); j++)
						func.set(j, data.get(j));

					funcs.add(func);
				}

				FractileCurveCalculator calc = new FractileCurveCalculator(
						funcs, weights);

				GriddedGeoDataSet data = new GriddedGeoDataSet(griddedRegion,
						true);
				AbstractXY_DataSet meanDataFunc = calc.getMeanCurve();
				Preconditions.checkState(meanDataFunc.getNum() == data.size());
				for (int i = 0; i < data.size(); i++)
					data.set(i, meanDataFunc.getY(i));

				double[] weightsArray = Doubles.toArray(weights);

				data = new GriddedGeoDataSet(griddedRegion, true);
				for (int i = 0; i < data.size(); i++) {
					double[] vals = new double[datas.size()];
					for (int j = 0; j < datas.size(); j++)
						vals[j] = datas.get(j).get(r).get(i);
					data.set(i, FaultSystemSolutionFetcher.calcScaledAverage(
							vals, weightsArray));
				}

				if (debug && r == 0) {
					for (int i = 0; i < datas.size() && i < 10; i++) {
						GeoDataSet subData = datas.get(i).get(r).copy();
						subData.log10();
						plots.add(new MapPlotData(cpt, subData,
								spacing, griddedRegion, true,
								"Sub Participation 5+ " + i, "sub_partic_5+_"
										+ i));
					}
				}

				// take log10
				GriddedGeoDataSet logData = data.copy();
				logData.log10();

				String name;
				String title;
				if (nucleation) {
					name = "gridded_sub_seis_nucl_rates_" + (float) minMag;
					title = "Log10(Sub Seis Nucleation Rates " + (float) +minMag;
				} else {
					name = "gridded_partic_rates_" + (float) minMag;
					title = "Log10(Participation Rates " + (float) +minMag;
				}
				
				if (maxMag < 9) {
					name += "_" + (float) maxMag;
					title += "=>" + (float) maxMag;
				} else {
					name += "+";
					title += "+";
				}
				title += ")";

				MapPlotData plot = new MapPlotData(cpt, logData, true,
						title, name);
				plot.subDirName = "gridded_participation_plots";
				plots.add(plot);

				GriddedGeoDataSet ucerf2Vals;
				if (nucleation)
					ucerf2Vals = ERF_Calculator.getNucleationRatesInRegion(
							ucerf2, griddedRegion, range[0], range[1]);
				else
					ucerf2Vals = ERF_Calculator.getParticipationRatesInRegion(
							ucerf2, griddedRegion, range[0], range[1]);

				// first plot UCERF2 on its own
				GriddedGeoDataSet ucerf2LogVals = ucerf2Vals.copy();
				ucerf2LogVals.log10();

				if (nucleation) {
					name = "gridded_sub_seis_nucl_rates_ucerf2_" + (float) minMag;
					title = "Log10(UCERF2 Sub Seis Nucleation Rates " + (float) +minMag;
				} else {
					name = "gridded_partic_rates_ucerf2_" + (float) minMag;
					title = "Log10(UCERF2 Participation Rates " + (float) +minMag;
				}
				
				if (maxMag < 9) {
					name += "_" + (float) maxMag;
					title += "=>" + (float) maxMag;
				} else {
					name += "+";
					title += "+";
				}
				title += ")";

				plots.add(new MapPlotData(cpt, ucerf2LogVals,
						spacing, griddedRegion, true, title, name));

				// now plot ratios
				GeoDataSet ratios = GeoDataSetMath.divide(data, ucerf2Vals);

				ratios.log10();

				if (nucleation) {
					name = "gridded_sub_seis_nucl_ratio_" + (float) minMag;
					title = "Log10(Sub Seis Nucleation Ratios " + (float) +minMag;
				} else {
					name = "gridded_partic_ratio_" + (float) minMag;
					title = "Log10(Participation Ratios " + (float) +minMag;
				}
				
				if (maxMag < 9) {
					name += "_" + (float) maxMag;
					title += "=>" + (float) maxMag;
				} else {
					name += "+";
					title += "+";
				}
				title += ")";

				plots.add(new MapPlotData(ratioCPT, ratios, spacing,
						griddedRegion, true, title, name));

				// double[] stdNormVals = new double[values.length];
				//
				// for (int s=0; s<stdNormVals.length; s++) {
				// if (ucerf2Vals[s] == 0)
				// stdNormVals[s] = Double.NaN;
				// else
				// stdNormVals[s] = (values[s] - ucerf2Vals[s]) / stdDevs[s];
				// }
				//
				// name = "partic_diffs_norm_std_dev_"+(float)minMag;
				// title = "(U3mean - U2mean)/U3std "+(float)+minMag;
				// if (maxMag < 9) {
				// name += "_"+(float)maxMag;
				// title += "=>"+(float)maxMag;
				// } else {
				// name += "+";
				// title += "+";
				// }
				// // title += ")";
				//
				// if (multipleFMs) {
				// name = fm.getShortName()+"_"+name;
				// title = fm.getShortName()+" "+title;
				// }
				//
				// plots.add(new MapPlotData(logCPT, faults, stdNormVals,
				// region,
				// skipNans, title, name));
			}
			debug(-1, "done finalizing");
		}

		@Override
		protected List<MapPlotData> getPlotData() {
			return plots;
		}

		@Override
		protected boolean usesERFs() {
			return true;
		}

	}
	
	public static class FSSRupNodesCache implements RupNodesCache {
		
		private ConcurrentMap<Region, ConcurrentMap<Integer, int[]>> nodesMap = Maps
				.newConcurrentMap();
		private ConcurrentMap<Region, ConcurrentMap<Integer, double[]>> fractsMap = Maps
				.newConcurrentMap();

		@Override
		public int[] getNodesForRup(ProbEqkSource source, EqkRupture rup,
				int srcIndex, int rupIndex, GriddedRegion region) {
			RuptureSurface surf = rup.getRuptureSurface();
			if (surf instanceof CompoundGriddedSurface) {
				int invIndex = getInversionIndex(source);
				ConcurrentMap<Integer, int[]> regMap = nodesMap.get(region);
				if (regMap == null) {
					regMap = Maps.newConcurrentMap();
					nodesMap.putIfAbsent(region, regMap);
					ConcurrentMap<Integer, double[]> fractMap = Maps.newConcurrentMap();
					fractsMap.putIfAbsent(region, fractMap);
					// in case another thread put it in
					// first
					regMap = nodesMap.get(region);
				}
				int[] nodes = regMap.get(invIndex);
				if (nodes == null) {
					ConcurrentMap<Integer, double[]> fractMap = fractsMap.get(region);
					LocationList surfLocs = surf.getEvenlyDiscritizedListOfLocsOnSurface();
					double ptFract = 1d/(double)surfLocs.size();
					List<Integer> nodesList = Lists.newArrayList();
					List<Double> fractsList = Lists.newArrayList();
					for(Location loc : surfLocs) {
						int index = region.indexForLocation(loc);
						if(index >= 0) {
							int indexInList = nodesList.indexOf(index);
							if (indexInList >= 0) {
								fractsList.set(indexInList, fractsList.get(indexInList)+ptFract);
							} else {
								nodesList.add(index);
								fractsList.add(ptFract);
							}
						}
					}
					nodes = Ints.toArray(nodesList);
					double[] fracts = Doubles.toArray(fractsList);
					regMap.putIfAbsent(invIndex, nodes);
					fractMap.putIfAbsent(invIndex, fracts);
				}
				return nodes;
			}
			return null;
		}

		@Override
		public double[] getFractsInNodesForRup(ProbEqkSource source,
				EqkRupture rup, int srcIndex, int rupIndex, GriddedRegion region) {
			RuptureSurface surf = rup.getRuptureSurface();
			if (surf instanceof CompoundGriddedSurface) {
				int invIndex = getInversionIndex(source);
				ConcurrentMap<Integer, double[]> fractMap = fractsMap.get(region);
				double[] fracts = fractMap.get(invIndex);
				if (fracts == null) {
					// not cached yet
					getNodesForRup(source, rup, srcIndex, rupIndex, region);
					fracts = fractMap.get(invIndex);
				}
				return fracts;
			}
			return null;
		}
		
	}

	public static class MapPlotData implements XMLSaveable, Serializable {

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
		private String subDirName;

		public MapPlotData(CPT cpt, List<LocationList> faults,
				double[] faultValues, Region region, boolean skipNans,
				String label, String fileName) {
			this(cpt, faults, faultValues, null, 1d, region, skipNans, label,
					fileName);
		}

		public MapPlotData(CPT cpt, GriddedGeoDataSet griddedData,
				boolean skipNans, String label, String fileName) {
			this(cpt, griddedData, griddedData.getRegion().getSpacing(),
					griddedData.getRegion(), skipNans, label, fileName);
		}

		public MapPlotData(CPT cpt, GeoDataSet griddedData, double spacing,
				Region region, boolean skipNans, String label, String fileName) {
			this(cpt, null, null, griddedData, spacing, region, skipNans,
					label, fileName);
		}

		public MapPlotData(CPT cpt, List<LocationList> faults,
				double[] faultValues, GeoDataSet griddedData, double spacing,
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
					locs.add(Location.fromXMLMetadata(nodeElem
							.element(Location.XML_METADATA_NAME)));
					double nodeVal = Double.parseDouble(nodeElem
							.attributeValue("value"));
					// if (Double.isNaN(nodeVal))
					// System.out.println("NaN!!!!");
					nodeVals.add(nodeVal);
				}
				griddedData = new ArbDiscrGeoDataSet(true);
				for (int i = 0; i < locs.size(); i++)
					griddedData.set(locs.get(i), nodeVals.get(i));
			} else {
				// fault based
				griddedData = null;

				faults = Lists.newArrayList();
				List<Double> valuesList = Lists.newArrayList();
				Iterator<Element> it = xml.elementIterator("Fault");
				while (it.hasNext()) {
					Element faultElem = it.next();
					faults.add(LocationList.fromXMLMetadata(faultElem
							.element(LocationList.XML_METADATA_NAME)));
					valuesList.add(Double.parseDouble(faultElem
							.attributeValue("value")));
				}
				values = Doubles.toArray(valuesList);
			}

			Region region = Region.fromXMLMetadata(xml
					.element(Region.XML_METADATA_NAME));

			boolean skipNans = Boolean.parseBoolean(xml
					.attributeValue("skipNans"));
			String label = xml.attributeValue("label");
			String fileName = xml.attributeValue("fileName");
			Attribute subDirName = xml.attribute("subDir");
			MapPlotData data = new MapPlotData(cpt, faults, values,
					griddedData, spacing, region, skipNans, label, fileName);
			if (subDirName != null)
				data.subDirName = subDirName.getStringValue();

			return data;
		}

		@Override
		public Element toXMLMetadata(Element root) {
			Element xml = root.addElement(XML_METADATA_NAME);

			cpt.toXMLMetadata(xml);

			if (faults != null) {
				for (int i = 0; i < faults.size(); i++) {
					Element faultElem = xml.addElement("Fault");
					faultElem.addAttribute("value", faultValues[i] + "");
					faults.get(i).toXMLMetadata(faultElem);
				}
			}
			if (griddedData != null) {
				Element geoEl = xml.addElement("GeoDataSet");
				geoEl.addAttribute("spacing", spacing + "");
				for (int i = 0; i < griddedData.size(); i++) {
					Location loc = griddedData.getLocation(i);
					double val = griddedData.get(i);
					Element nodeEl = geoEl.addElement("Node");
					nodeEl.addAttribute("value", val + "");
					loc.toXMLMetadata(nodeEl);
				}
			}

			if (region instanceof GriddedRegion) {
				GriddedRegion gridded = (GriddedRegion) region;
				if (gridded.getSpacing() <= 0.11)
					new Region(region.getBorder(), null).toXMLMetadata(xml);
				else
					new Region(new Location(gridded.getMaxGridLat(),
							gridded.getMaxGridLon()), new Location(
							gridded.getMinGridLat(), gridded.getMinGridLon()))
							.toXMLMetadata(xml);
			} else {
				region.toXMLMetadata(xml);
			}

			xml.addAttribute("skipNans", skipNans + "");
			xml.addAttribute("label", label);
			xml.addAttribute("fileName", fileName);
			if (subDirName != null)
				xml.addAttribute("subDir", subDirName);

			return root;
		}

		public GeoDataSet getGriddedData() {
			return griddedData;
		}

		public String getLabel() {
			return label;
		}

		public CPT getCPT() {
			return cpt;
		}

		public Region getRegion() {
			return region;
		}

		public String getFileName() {
			return fileName;
		}

		public String getSubDirName() {
			return subDirName;
		}
	}

	public static abstract class MapBasedPlot extends CompoundFSSPlots {

		protected abstract List<MapPlotData> getPlotData();

		protected abstract String getPlotDataFileName();

		protected double[] getWeightedAvg(int numFaults,
				List<double[]> valuesList, List<Double> weightsList) {

			double[] weights = Doubles.toArray(weightsList);

			double[] values = new double[numFaults];
			for (int i = 0; i < numFaults; i++) {
				double[] faultVals = new double[weights.length];
				for (int s = 0; s < weights.length; s++)
					faultVals[s] = valuesList.get(s)[i];
				values[i] = FaultSystemSolutionFetcher.calcScaledAverage(
						faultVals, weights);
			}

			return values;
		}

		protected double[] getStdDevs(int numFaults, List<double[]> valuesList) {

			double[] stdDevs = new double[numFaults];
			for (int i = 0; i < numFaults; i++) {
				double[] faultVals = new double[valuesList.size()];
				for (int s = 0; s < valuesList.size(); s++)
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

		public static List<MapPlotData> loadPlotData(File file)
				throws MalformedURLException, DocumentException {
			Document doc = XMLUtils.loadDocument(file);
			Element root = doc.getRootElement();

			List<MapPlotData> plots = Lists.newArrayList();

			Iterator<Element> it = root
					.elementIterator(MapPlotData.XML_METADATA_NAME);
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

		public static void makeMapPlots(File dir, String prefix,
				List<MapPlotData> plots) throws GMT_MapException,
				RuntimeException, IOException {
			// if (plots.size() < 30) {
			System.out.println("*** Making " + plots.size() + " Map Plots ***");
			for (MapPlotData plot : plots) {
				doMakePlot(dir, prefix, plot);
			}
			// } else {
			// int numThreads = 10;
			// System.out.println("*** Making "+plots.size()
			// +" Map Plots ("+numThreads+" THREADS) ***");
			// ExecutorService ex = Executors.newFixedThreadPool(numThreads);
			// CompletionService<Integer> ecs = new
			// ExecutorCompletionService<Integer>(ex);
			//
			// for (MapPlotData plot : plots) {
			// ecs.submit(new MapPlotCallable(dir, prefix, plot));
			// }
			//
			// ex.shutdown();
			//
			// for (int i=0; i<plots.size(); i++) {
			// try {
			// ecs.take();
			// } catch (InterruptedException e) {
			// ExceptionUtils.throwAsRuntimeException(e);
			// }
			// }
			// }
		}

		private static void doMakePlot(File dir, String prefix, MapPlotData plot)
				throws GMT_MapException, RuntimeException, IOException {
			String plotPrefix;
			if (prefix != null && !prefix.isEmpty() && plot.subDirName == null)
				plotPrefix = prefix + "_";
			else
				plotPrefix = "";
			plotPrefix += plot.fileName;
			File writeDir = dir;
			if (plot.subDirName != null)
				writeDir = new File(dir, plot.subDirName);
			if (!writeDir.exists())
				writeDir.mkdir();
			System.out.println("Making fault plot with title: " + plot.label);
			if (plot.griddedData == null)
				FaultBasedMapGen.makeFaultPlot(plot.cpt, plot.faults,
						plot.faultValues, plot.region, writeDir, plotPrefix,
						false, plot.skipNans, plot.label);
			else
				FaultBasedMapGen.plotMap(writeDir, plotPrefix, false,
						FaultBasedMapGen.buildMap(plot.cpt, null, null,
								plot.griddedData, plot.spacing, plot.region,
								plot.skipNans, plot.label));
			System.out.println("DONE.");
		}
		
		protected void writeExtraData(File dir, String prefix) {
			// do nothing unless overridden
		}

	}

	private static class MapPlotCallable implements Callable<Integer> {

		private File dir;
		private String prefix;
		private MapPlotData plot;

		public MapPlotCallable(File dir, String prefix, MapPlotData plot) {
			this.dir = dir;
			this.prefix = prefix;
			this.plot = plot;
		}

		@Override
		public Integer call() throws Exception {
			MapBasedPlot.doMakePlot(dir, prefix, plot);
			return 0;
		}

	}

	public static List<PlotCurveCharacterstics> getFractileChars(Color color,
			int numFractiles) {
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();

		PlotCurveCharacterstics thinChar = new PlotCurveCharacterstics(
				PlotLineType.SOLID, 1f, color);
		PlotCurveCharacterstics medChar = new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, color);
		PlotCurveCharacterstics thickChar = new PlotCurveCharacterstics(
				PlotLineType.SOLID, 4f, color);

		for (int i = 0; i < numFractiles; i++)
			chars.add(medChar);
		chars.add(thickChar);
		chars.add(thinChar);
		chars.add(thinChar);

		return chars;
	}

	/**
	 * Called once for each solution
	 * 
	 * @param branch
	 * @param sol
	 * @param solIndex
	 *            TODO
	 */
	protected abstract void processSolution(LogicTreeBranch branch,
			InversionFaultSystemSolution sol, int solIndex);

	/**
	 * This is used when doing distributed calculations. This method will be
	 * called on the root method to combine all of the other plots with this
	 * one.
	 * 
	 * @param otherCalcs
	 */
	protected abstract void combineDistributedCalcs(
			Collection<CompoundFSSPlots> otherCalcs);

	/**
	 * Called at the end to finalize the plot
	 */
	protected abstract void doFinalizePlot();
	
	/**
	 * Called at the end to finalize the plot
	 */
	protected void finalizePlot() {
		Stopwatch watch = new Stopwatch();
		watch.start();
		doFinalizePlot();
		watch.stop();
		addToFinalizeTimeCount(watch.elapsed(TimeUnit.MILLISECONDS));
	}

	protected boolean usesERFs() {
		return false;
	}

	protected void processERF(LogicTreeBranch branch,
			UCERF3_FaultSysSol_ERF erf, int solIndex) {
		// do nothing unless overridden
		if (usesERFs())
			throw new IllegalStateException(
					"Must be overridden if usesERFs() == true");
		else
			throw new IllegalStateException(
					"Should not be called if usesERFs() == false");
	}

	private static String hostname;

	private synchronized static String getHostname() {
		if (hostname == null) {
			try {
				hostname = java.net.InetAddress.getLocalHost().getHostName();
//				System.out.println(hostname);
				if (hostname.contains("."))
					hostname = hostname.split("\\.")[0];
//				System.out.println(hostname);
			} catch (UnknownHostException e) {
			}
		}
		return hostname;
	}

	protected static final SimpleDateFormat df = new SimpleDateFormat(
			"HH:mm:ss.SSS");

	private String className;

	private synchronized String getClassName() {
		if (className == null) {
			className = ClassUtils.getClassNameWithoutPackage(this.getClass());
			while (className.contains("$"))
				className = className.substring(className.indexOf("$")+1);
		}
		return className;
	}

	protected void debug(int solIndex, String message) {
		System.out.println("[" + df.format(new Date()) + " (" + getHostname() + ") "
				+ getClassName() + "(" + solIndex + ")]: " + message);
	}
	
	private long computeTimeMillis = 0;
	
	protected synchronized void addToComputeTimeCount(long time) {
		computeTimeMillis += time;
	}
	
	protected long getComputeTimeCount() {
		return computeTimeMillis;
	}
	
	private long finalizeTimeMillis = 0;
	
	protected synchronized void addToFinalizeTimeCount(long time) {
		finalizeTimeMillis += time;
	}
	
	protected long getFinalizeTimeCount() {
		return finalizeTimeMillis;
	}

	/**
	 * This builds the plot individually (without utilizing efficiencies of
	 * working on multiple plots at once as you iterate over the solutions).
	 * 
	 * @param fetcher
	 */
	public void buildPlot(FaultSystemSolutionFetcher fetcher) {
		ArrayList<CompoundFSSPlots> plots = new ArrayList<CompoundFSSPlots>();
		plots.add(this);
		batchPlot(plots, fetcher);
	}

	/**
	 * This builds multiple plots at once, only iterating through the solutions
	 * once. This should be much faster than calling buildPlot() on each plot.
	 * 
	 * @param plots
	 * @param fetcher
	 */
	public static void batchPlot(Collection<CompoundFSSPlots> plots,
			FaultSystemSolutionFetcher fetcher) {
		int threads = Runtime.getRuntime().availableProcessors();
		threads *= 3;
		threads /= 4;
		if (threads < 1)
			threads = 1;
		batchPlot(plots, fetcher, threads);
	}

	// protected static class RupSetCacheManager {
	//
	// private Cache<String, SimpleFaultSystemRupSet> cache;
	// private ConcurrentMap<FaultModels, SimpleFaultSystemRupSet> fmCache =
	// Maps.newConcurrentMap();
	//
	// public RupSetCacheManager(int maxNum) {
	// cache = CacheBuilder.newBuilder().maximumSize(maxNum).build();
	// }
	//
	// public void cache(LogicTreeBranch branch, FaultSystemSolution sol) {
	// String cacheName = branch.getValue(FaultModels.class).getShortName()+"_"
	// +branch.getValue(DeformationModels.class).getShortName()+"_"
	// +branch.getValue(ScalingRelationships.class).getShortName();
	//
	// SimpleFaultSystemRupSet cacheMatch = cache.getIfPresent(cacheName);
	//
	// if (cacheMatch != null) {
	// sol.copyCacheFrom(cacheMatch);
	// return;
	// }
	//
	// SimpleFaultSystemRupSet fmMatch =
	// fmCache.get(branch.getValue(FaultModels.class));
	// if (fmMatch != null)
	// sol.copyCacheFrom(fmMatch);
	//
	// SimpleFaultSystemRupSet rupSet;
	// if (sol instanceof SimpleFaultSystemSolution) {
	// rupSet =
	// SimpleFaultSystemRupSet.toSimple(((SimpleFaultSystemSolution)sol).getRupSet());
	// } else {
	//
	// }
	// }
	// }

	protected static class PlotSolComputeTask implements Task {

		private Collection<CompoundFSSPlots> plots;
		private FaultSystemSolutionFetcher fetcher;
		private LogicTreeBranch branch;
		private boolean mpj;
		private UCERF3_FaultSysSol_ERF erf;
		private int index;
		
		private long overheadMillis;

		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher, LogicTreeBranch branch,
				int index) {
			this(plots, fetcher, branch, false, index);
		}

		public PlotSolComputeTask(Collection<CompoundFSSPlots> plots,
				FaultSystemSolutionFetcher fetcher, LogicTreeBranch branch,
				boolean mpj, int index) {
			this.plots = plots;
			this.fetcher = fetcher;
			this.branch = branch;
			this.mpj = mpj;
			this.index = index;
		}

		private void debug(String message) {
			System.out.println("["+df.format(new Date())+" ("+getHostname()+") PlotSolComputeTask]: "
					+message+" ["+getMemoryDebug()+"]");
		}

		private String getMemoryDebug() {
			System.gc();
			Runtime rt = Runtime.getRuntime();
			long totalMB = rt.totalMemory() / 1024 / 1024;
			long freeMB = rt.freeMemory() / 1024 / 1024;
			long usedMB = totalMB - freeMB;
			return "mem t/u/f: "+totalMB+"/"+usedMB+"/"+freeMB;
		}

		@Override
		public void compute() {
			try {
				Stopwatch overheadWatch = new Stopwatch();
				
				overheadWatch.start();
				debug("Fetching solution");
				InversionFaultSystemSolution sol = fetcher.getSolution(branch);

				overheadWatch.stop();

				for (CompoundFSSPlots plot : plots) {
					Stopwatch computeWatch = new Stopwatch();
					if (plot.usesERFs()) {
						if (erf == null) {
							overheadWatch.start();
							debug("Building ERF");
							erf = new UCERF3_FaultSysSol_ERF(
									(InversionFaultSystemSolution) sol);
							erf.getParameter(
									ApplyGardnerKnopoffAftershockFilterParam.NAME)
									.setValue(true);
							erf.updateForecast();
							overheadWatch.stop();
						}
						debug("Processing ERF plot: "
								+ ClassUtils.getClassNameWithoutPackage(plot
										.getClass()));
						computeWatch.start();
						plot.processERF(branch, erf, index);
						computeWatch.stop();
					} else {
						debug("Processing Regular plot: "
								+ ClassUtils.getClassNameWithoutPackage(plot
										.getClass()));
						computeWatch.start();
						plot.processSolution(branch, sol, index);
						computeWatch.stop();
					}
					plot.addToComputeTimeCount(computeWatch.elapsed(TimeUnit.MILLISECONDS));
				}
				debug("DONE");
				erf = null;
				overheadMillis = overheadWatch.elapsed(TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				debug("EXCEPTION: "+e.getClass()+": "+e.getMessage());
				e.printStackTrace();
				if (mpj)
					MPJTaskCalculator.abortAndExit(1);
				else
					System.exit(1);
			}
		}

	}

	public static void batchPlot(Collection<CompoundFSSPlots> plots,
			FaultSystemSolutionFetcher fetcher, int threads) {

		List<Task> tasks = Lists.newArrayList();
		int index = 0;
		for (LogicTreeBranch branch : fetcher.getBranches()) {
			tasks.add(new PlotSolComputeTask(plots, fetcher, branch,
					index++));
		}

		System.out.println("Making " + plots.size() + " plot(s) with "
				+ tasks.size() + " branches");

		ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
		try {
			comp.computThreaded(threads);
		} catch (InterruptedException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}

		for (CompoundFSSPlots plot : plots)
			plot.finalizePlot();
		
		long overheadTime = 0;
		for (Task task : tasks)
			overheadTime += ((PlotSolComputeTask)task).overheadMillis;
		
		float overheadMins = (float)((double)overheadTime / (double)MILLIS_PER_MIN);
		System.out.println("TOTAL OVERHEAD TIME: "+overheadMins+"m");
		
		printComputeTimes(plots);
	}
	
	private static long MILLIS_PER_SEC = 1000;
	private static long MILLIS_PER_MIN = MILLIS_PER_SEC*60l;
	
	protected static void printComputeTimes(Collection<CompoundFSSPlots> plots) {
		long totCompTime = 0;
		long totFinalizeTime = 0;
		for (CompoundFSSPlots plot : plots) {
			totCompTime += plot.computeTimeMillis;
			totFinalizeTime += plot.finalizeTimeMillis;
		}
		
		double totCompTimeMins = (double)totCompTime / (double)MILLIS_PER_MIN;
		double totFinalizeTimeMins = (double)totFinalizeTime / (double)MILLIS_PER_MIN;
		
		System.out.println("*** CompoundFSSPlots Compute Times ***");
		System.out.println("TOTAL COMPUTE TIME: "+totCompTimeMins+" m");
		
		for (CompoundFSSPlots plot : plots) {
			float compTimeMins = (float)((double)plot.computeTimeMillis / (double)MILLIS_PER_MIN);
			float compTimePercent = (float)(100d*compTimeMins/totCompTimeMins);
			
			System.out.println("\t"+ClassUtils.getClassNameWithoutPackage(plot.getClass())
					+": "+compTimeMins+" m ("+compTimePercent+" %)");
		}
		System.out.println("***************************************");
		
		System.out.println("*** CompoundFSSPlots Finalize Times ***");
		System.out.println("TOTAL FINALIZE TIME: "+totFinalizeTimeMins+" m");
		
		for (CompoundFSSPlots plot : plots) {
			float finalizeTimeMins = (float)((double)plot.finalizeTimeMillis / (double)MILLIS_PER_MIN);
			float finalizeTimePercent = (float)(100d*finalizeTimeMins/totFinalizeTimeMins);
			
			System.out.println("\t"+ClassUtils.getClassNameWithoutPackage(plot.getClass())
					+": "+finalizeTimeMins+" m ("+finalizeTimePercent+" %)");
		}
		System.out.println("***************************************");
	}

	public static void batchWritePlots(Collection<CompoundFSSPlots> plots,
			File dir, String prefix) throws Exception {
		batchWritePlots(plots, dir, prefix, true);
	}

	public static void batchWritePlots(Collection<CompoundFSSPlots> plots,
			File dir, String prefix, boolean makeMapPlots) throws Exception {
		for (CompoundFSSPlots plot : plots) {
			if (plot instanceof RegionalMFDPlot) {
				RegionalMFDPlot mfd = (RegionalMFDPlot) plot;

				CompoundFSSPlots.writeRegionalMFDPlots(mfd.getSpecs(),
						mfd.getRegions(), dir, prefix);
			} else if (plot instanceof ERFBasedRegionalMFDPlot) {
				ERFBasedRegionalMFDPlot mfd = (ERFBasedRegionalMFDPlot) plot;
				writeERFBasedRegionalMFDPlots(mfd.specs, mfd.regions, dir,
						prefix);
			} else if (plot instanceof PaleoFaultPlot) {
				PaleoFaultPlot paleo = (PaleoFaultPlot) plot;
				File paleoPlotsDir = new File(dir,
						CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME);
				if (!paleoPlotsDir.exists());
					paleoPlotsDir.mkdir();
				CompoundFSSPlots.writePaleoFaultPlots(paleo.getPlotsMap(),
						paleoPlotsDir);
			} else if (plot instanceof PaleoSiteCorrelationPlot) {
				PaleoSiteCorrelationPlot paleo = (PaleoSiteCorrelationPlot) plot;
				File paleoPlotsDir = new File(dir,
						CommandLineInversionRunner.PALEO_CORRELATION_DIR_NAME);
				if (!paleoPlotsDir.exists())
					paleoPlotsDir.mkdir();
				CompoundFSSPlots.writePaleoCorrelationPlots(
						paleo.getPlotsMap(), paleoPlotsDir);
			} else if (plot instanceof ParentSectMFDsPlot) {
				ParentSectMFDsPlot parentPlots = (ParentSectMFDsPlot) plot;
				File parentPlotsDir = new File(dir,
						CommandLineInversionRunner.PARENT_SECT_MFD_DIR_NAME);
				if (!parentPlotsDir.exists())
					parentPlotsDir.mkdir();
				CompoundFSSPlots.writeParentSectionMFDPlots(parentPlots,
						parentPlotsDir);
			} else if (plot instanceof RupJumpPlot) {
				RupJumpPlot jumpPlot = (RupJumpPlot) plot;
				CompoundFSSPlots.writeJumpPlots(jumpPlot, dir, prefix);
			} else if (plot instanceof MiniSectRIPlot) {
				MiniSectRIPlot miniPlot = (MiniSectRIPlot) plot;
				CompoundFSSPlots.writeMiniSectRITables(miniPlot, dir, prefix);
			} else if (plot instanceof PaleoRatesTable) {
				PaleoRatesTable aveSlip = (PaleoRatesTable) plot;
				CompoundFSSPlots.writePaleoRatesTables(aveSlip, dir, prefix);
			} else if (plot instanceof MisfitTable) {
				MisfitTable table = (MisfitTable) plot;
				BatchPlotGen.writeMisfitsCSV(dir, prefix, table.misfitsMap);
			} else if (plot instanceof MeanFSSBuilder) {
				MeanFSSBuilder builder = (MeanFSSBuilder) plot;
				writeMeanSolutions(builder, dir, prefix);
			} else if (plot instanceof MapBasedPlot) {
				MapBasedPlot faultPlot = (MapBasedPlot) plot;
				faultPlot.writePlotData(dir);
				faultPlot.writeExtraData(dir, prefix);
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
			for (int i = 2; i < args.length; i++) {
				File plotFile = new File(dir, args[i]);
				MapBasedPlot.makeMapPlots(dir, prefix,
						MapBasedPlot.loadPlotData(plotFile));
			}
			System.exit(0);
		}

		UCERF2_TimeIndependentEpistemicList ucerf2_erf_list = new UCERF2_TimeIndependentEpistemicList();
		ucerf2_erf_list.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
				UCERF2.FULL_DDW_FLOATER);
		ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_NAME,
				UCERF2.BACK_SEIS_INCLUDE);
		ucerf2_erf_list.setParameter(UCERF2.BACK_SEIS_RUP_NAME,
				UCERF2.BACK_SEIS_RUP_POINT);
		ucerf2_erf_list.getTimeSpan().setDuration(1d);
		ucerf2_erf_list.updateForecast();

		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
		// File dir = new
		// File("/tmp/2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL");
		// File file = new File(dir,
		// "2012_10_12-fm3-ref-branch-weight-vars-zengfix_COMPOUND_SOL.zip");
		File dir = new File("/tmp/comp_plots");
		File file = new File(dir,
				"2013_01_14-stampede_3p2_production_runs_combined_COMPOUND_SOL.zip");
//		File dir = new File("/tmp/paleo_comp_plots/Paleo1.5");
//		File file = new File(dir, "2013_04_02-new-paleo-weights-tests_VarPaleo1.5_COMPOUND_SOL.zip");
		// File file = new File(dir, "zeng_convergence_compound.zip");
		// File file = new
		// File("/tmp/2012_10_10-fm3-logic-tree-sample_COMPOUND_SOL.zip");
		FaultSystemSolutionFetcher fetch = CompoundFaultSystemSolution
				.fromZipFile(file);
		double wts = 0;
		for (LogicTreeBranch branch : fetch.getBranches())
			wts += weightProvider.getWeight(branch);
		System.out.println("Total weight: " + wts);
		// System.exit(0);
		fetch = FaultSystemSolutionFetcher.getRandomSample(fetch, 4,
				FaultModels.FM3_1);

		new DeadlockDetectionThread(3000).start();

		// List<Region> regions = RegionalMFDPlot.getDefaultRegions();
		// List<Region> regions = RegionalMFDPlot.getDefaultRegions().subList(3,
		// 5);
		List<Region> regions = RegionalMFDPlot.getDefaultRegions()
				.subList(0, 1);
		regions = Lists.newArrayList(regions);

		// File dir = new File("/tmp");
		// String prefix = "2012_10_10-fm3-logic-tree-sample-first-247";
		String prefix = dir.getName();
		// for (PlotSpec spec : getRegionalMFDPlotSpecs(fetch, weightProvider,
		// regions)) {
		// GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(spec);
		// gw.setYLog(true);
		// }
		// writeRegionalMFDPlots(fetch, weightProvider, regions, dir, prefix);
		// File paleoDir = new File(dir, prefix+"-paleo-faults");
		// writePaleoFaultPlots(fetch, weightProvider, paleoDir);
		// File paleoCorrDir = new File(dir, prefix+"-paleo-corr");
		// writePaleoCorrelationPlots(fetch, weightProvider, paleoCorrDir);
		// File parentSectMFDsDir = new File(dir, prefix+"-parent-sect-mfds");
		// writeParentSectionMFDPlots(fetch, weightProvider, parentSectMFDsDir);
		// writeJumpPlots(fetch, weightProvider, dir, prefix);
		List<CompoundFSSPlots> plots = Lists.newArrayList();
		plots.add(new RegionalMFDPlot(weightProvider, regions));
		plots.add(new PaleoFaultPlot(weightProvider));
		plots.add(new PaleoSiteCorrelationPlot(weightProvider));
		plots.add(new ParentSectMFDsPlot(weightProvider));
		plots.add(new RupJumpPlot(weightProvider));
		plots.add(new SlipRatePlots(weightProvider));
//		plots.add(new ParticipationMapPlot(weightProvider));
		plots.add(new GriddedParticipationMapPlot(weightProvider, 0.1d));
		plots.add(new ERFBasedRegionalMFDPlot(weightProvider));
		plots.add(new MiniSectRIPlot(weightProvider));
		plots.add(new PaleoRatesTable(weightProvider));
		plots.add(new AveSlipMapPlot(weightProvider));
		plots.add(new MultiFaultParticPlot(weightProvider));
		plots.add(new MeanFSSBuilder(weightProvider));

		batchPlot(plots, fetch, 2);

		// for (CompoundFSSPlots plot : plots)
		// FileUtils.saveObjectInFile("/tmp/asdf.obj", plot);
		batchWritePlots(plots, dir, prefix, true);
		// MapBasedPlot.makeMapPlots(dir, prefix,
		// MapBasedPlot.loadPlotData(new File(dir,
		// SlipMisfitPlot.PLOT_DATA_FILE_NAME)));
		// MapBasedPlot.makeMapPlots(dir, prefix,
		// MapBasedPlot.loadPlotData(new File(dir,
		// AveSlipPlot.PLOT_DATA_FILE_NAME)));
		// MapBasedPlot.makeMapPlots(dir, prefix,
		// MapBasedPlot.loadPlotData(new File(dir,
		// MultiFaultParticPlot.PLOT_DATA_FILE_NAME)));

		System.exit(0);

	}

}
